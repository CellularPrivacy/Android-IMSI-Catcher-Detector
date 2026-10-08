/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.protection;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import com.secupwn.aimsicd.constants.ProtectionConstants;
import com.secupwn.aimsicd.enums.Status;
import com.secupwn.aimsicd.service.SimSwapper;
import com.secupwn.aimsicd.utils.RealmHelper;
import com.secupwn.aimsicd.utils.TinyDB;

import io.realm.Realm;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

/**
 * Binds the protection subsystem to the user's Access Point Name (APN).
 *
 * <p>A rogue base station cannot silently re-route data without the device sooner or later
 * attaching through an unexpected packet-data configuration; likewise, malware with elevated
 * privileges may rewrite the APN to proxy traffic. This guard therefore watches the
 * <em>preferred APN</em> reported by the telephony provider and raises an alarm when it stops
 * matching the APN the user expects.</p>
 *
 * <p><b>Sources of truth, in precedence order:</b></p>
 * <ol>
 *     <li><b>User binding</b> — the APN typed by the user in
 *         <em>Settings → Protection → My APN</em>. When set, it always wins and is
 *         <b>never</b> overridden automatically.</li>
 *     <li><b>Learned baseline</b> — the APN recorded by the auto-bind step below, kept in
 *         TinyDB under {@link ProtectionConstants#PREF_APN_BASELINE}.</li>
 * </ol>
 *
 * <p><b>Auto-bind &amp; adaptation algorithm</b> (see {@link #checkApn()}):</p>
 * <ol>
 *     <li><b>Observe.</b> Triggered at startup and on every telephony-provider change
 *         notification (debounced). Reads the preferred APN ({@code name} + {@code apn}
 *         columns).</li>
 *     <li><b>Auto-bind.</b> If no reference exists yet (no user value, no baseline) and
 *         auto-bind is enabled, the current APN is silently recorded as the baseline and an
 *         informational {@link ProtectionConstants#EVENT_APN_BASELINED} row is logged.</li>
 *     <li><b>Compare.</b> The active APN matches when it equals the reference
 *         case-insensitively in either the {@code apn} or the {@code name} field.</li>
 *     <li><b>Mismatch episode.</b> The first sighting of a differing APN opens an episode and
 *         raises a single {@code HIGH} alarm ({@link ProtectionConstants#EVENT_APN_MISMATCH});
 *         repeats are cooldown-gated. Each distinct APN value starts its own episode, so
 *         flapping values and user edits of the expected APN reset the timers naturally.</li>
 *     <li><b>Adapt.</b> If auto-adapt is enabled, the reference is the <em>learned</em>
 *         baseline (not a user binding), and the same differing APN persists for
 *         {@link ProtectionConstants#APN_ADAPT_AFTER_MILLIS}, the new APN is adopted as the
 *         baseline ({@link ProtectionConstants#EVENT_APN_REBOUND}) instead of alarming
 *         forever — e.g. after a legitimate carrier-side APN migration.</li>
 *     <li><b>Restore.</b> When the active APN returns to the reference while an episode is
 *         open, the episode closes with {@link ProtectionConstants#EVENT_APN_RESTORED}.</li>
 * </ol>
 *
 * <p><b>Platform limits:</b> reading the preferred APN is allowed for normal apps on most
 * firmware (some ROMs require {@code READ_PHONE_STATE}, which AIMSICD already holds); <em>writing</em>
 * APNs requires {@code WRITE_APN_SETTINGS}, which is system-app-only since Android 4.2. The
 * guard is therefore a read-only tripwire: it detects and escalates (notification +
 * configured countermeasures such as airplane mode) but it cannot force the APN back — the
 * user restores it in the system <em>Access Point Names</em> screen.</p>
 *
 * <p>Minimum API: 16. The telephony provider URIs/columns are referenced as literals on
 * purpose ({@code android.provider.Telephony.Carriers} only exists since API 19).</p>
 */
@Slf4j
public class ApnGuard {

    /** Preferred-APN row of the telephony provider. */
    static final String URI_PREFER_APN = "content://telephony/carriers/preferapn";

    /** Carriers table; observed with descendants so provider rewrites are caught on all ROMs. */
    static final String URI_CARRIERS = "content://telephony/carriers";

    static final String COLUMN_NAME = "name";
    static final String COLUMN_APN = "apn";

    /** Debounce for provider change bursts (one APN switch emits several notifications). */
    private static final long OBSERVER_DEBOUNCE_MILLIS = 1_000L;

    private final Context mContext;
    private final SimSwapper.Reactor mReactor;
    private final RealmHelper mDbHelper;
    private final TinyDB mTinydb;
    private final Handler mHandler = new Handler();

    private ContentObserver mObserver;
    private boolean mStarted;

    // Configuration pushed by CellTracker from SharedPreferences.
    private boolean mEnabled;
    private boolean mAutoBind = true;
    private boolean mAutoAdapt;
    private String mExpectedApn = "";

    private final Runnable mCheckRunnable = new Runnable() {
        @Override
        public void run() {
            checkApn();
        }
    };

    /**
     * @param context  application context.
     * @param reactor  receives the {@code HIGH} mismatch alarms; the service's
     *                 {@link com.secupwn.aimsicd.service.CellTracker CellTracker} is the
     *                 natural implementor because it owns the status notification.
     * @param dbHelper used to write rows into the EventLog table.
     */
    public ApnGuard(Context context, SimSwapper.Reactor reactor, RealmHelper dbHelper) {
        mContext = context.getApplicationContext();
        mReactor = reactor;
        mDbHelper = dbHelper;
        mTinydb = TinyDB.getInstance();
    }

    /* ---------------------------------------------------------------------
     * Configuration (pushed from SharedPreferences by CellTracker)
     * ------------------------------------------------------------------ */

    /** Master switch for the guard. When off, {@link #checkApn()} is a no-op. */
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    /** When on (default), the first seen APN is silently recorded as the baseline. */
    public void setAutoBind(boolean autoBind) {
        mAutoBind = autoBind;
    }

    /** When on, a persistently changed APN is adopted as the new learned baseline. */
    public void setAutoAdapt(boolean autoAdapt) {
        mAutoAdapt = autoAdapt;
    }

    /** User-typed expected APN; empty means "no user binding, use the learned baseline". */
    public void setExpectedApn(String expectedApn) {
        mExpectedApn = expectedApn == null ? "" : expectedApn;
    }

    /* ---------------------------------------------------------------------
     * Lifecycle
     * ------------------------------------------------------------------ */

    /**
     * Registers the telephony-provider observer and performs the first check.
     * Call from the main thread once, when the protection service starts. Idempotent.
     */
    public void start() {
        if (mStarted) {
            return;
        }
        mStarted = true;
        try {
            mObserver = new ContentObserver(mHandler) {
                @Override
                public void onChange(boolean selfChange) {
                    // Coalesce bursts: one APN switch emits several notifications.
                    mHandler.removeCallbacks(mCheckRunnable);
                    mHandler.postDelayed(mCheckRunnable, OBSERVER_DEBOUNCE_MILLIS);
                }
            };
            mContext.getContentResolver().registerContentObserver(
                    Uri.parse(URI_CARRIERS), true, mObserver);
        } catch (Exception e) {
            log.warn("APN observer registration failed: {}", e.getMessage());
            mObserver = null;
        }
        checkApn();
    }

    /**
     * Unregisters the telephony-provider observer. Call from the main thread when the
     * protection service stops. Idempotent.
     */
    public void stop() {
        if (!mStarted) {
            return;
        }
        mStarted = false;
        mHandler.removeCallbacks(mCheckRunnable);
        if (mObserver != null) {
            try {
                mContext.getContentResolver().unregisterContentObserver(mObserver);
            } catch (Exception e) {
                log.warn("APN observer unregistration failed: {}", e.getMessage());
            }
            mObserver = null;
        }
    }

    /* ---------------------------------------------------------------------
     * Core algorithm
     * ------------------------------------------------------------------ */

    /**
     * Runs one observe → bind → compare → (alarm | adapt | restore) cycle.
     * Safe to call from any thread and at any time; a no-op while disabled.
     */
    public void checkApn() {
        if (!mEnabled) {
            return;
        }

        CurrentApn current = readCurrentApn();
        if (current == null || current.isEmpty()) {
            log.debug("APN guard: preferred APN not readable on this device, skipping check.");
            return;
        }

        String userBinding = normalizeApn(mExpectedApn);
        String baseline = normalizeApn(mTinydb.getString(ProtectionConstants.PREF_APN_BASELINE));
        boolean hasUserBinding = !TextUtils.isEmpty(userBinding);
        String reference = hasUserBinding ? userBinding : baseline;

        // --- Auto-bind: nothing to compare against yet ---------------------------
        if (TextUtils.isEmpty(reference)) {
            if (mAutoBind) {
                String learned = pickBaselineValue(current.name, current.apn);
                mTinydb.putString(ProtectionConstants.PREF_APN_BASELINE, learned);
                clearMismatchEpisode();
                log.info("APN guard: baselined '{}'.", learned);
                logOnly(ProtectionConstants.EVENT_APN_BASELINED,
                        "APN baselined: " + learned);
            } else {
                log.debug("APN guard: no reference APN and auto-bind is off, skipping check.");
            }
            return;
        }

        // --- Compare --------------------------------------------------------------
        if (apnMatches(reference, current.name, current.apn)) {
            if (hasMismatchEpisode()) {
                clearMismatchEpisode();
                String restored = pickBaselineValue(current.name, current.apn);
                log.info("APN guard: active APN restored to '{}'.", restored);
                logOnly(ProtectionConstants.EVENT_APN_RESTORED,
                        "APN restored: " + restored);
            }
            return;
        }

        // --- Mismatch --------------------------------------------------------------
        String currentValue = pickBaselineValue(current.name, current.apn);
        String episodeValue = mTinydb.getString(ProtectionConstants.PREF_APN_MISMATCH_VALUE);
        long since = mTinydb.getLong(ProtectionConstants.PREF_APN_MISMATCH_SINCE);
        long now = System.currentTimeMillis();

        if (!normalizeApn(currentValue).equals(normalizeApn(episodeValue))) {
            // A new episode: first sighting of this particular APN value.
            mTinydb.putString(ProtectionConstants.PREF_APN_MISMATCH_VALUE, currentValue);
            mTinydb.putLong(ProtectionConstants.PREF_APN_MISMATCH_SINCE, now);
            mTinydb.putLong(ProtectionConstants.PREF_APN_LAST_ALARM_MILLIS, now);
            log.warn("APN guard: mismatch, expected '{}' but active is '{}'.",
                    reference, currentValue);
            alarm(Status.HIGH, ProtectionConstants.EVENT_APN_MISMATCH,
                    "APN mismatch: active '" + currentValue + "'");
            return;
        }

        // Ongoing episode with the same APN value.
        if (mAutoAdapt && !hasUserBinding
                && now - since >= ProtectionConstants.APN_ADAPT_AFTER_MILLIS) {
            mTinydb.putString(ProtectionConstants.PREF_APN_BASELINE, currentValue);
            clearMismatchEpisode();
            log.warn("APN guard: auto-adapted baseline to persistently changed APN '{}'.",
                    currentValue);
            logOnly(ProtectionConstants.EVENT_APN_REBOUND,
                    "APN auto-adapted to: " + currentValue);
            return;
        }

        long lastAlarm = mTinydb.getLong(ProtectionConstants.PREF_APN_LAST_ALARM_MILLIS);
        if (now - lastAlarm >= ProtectionConstants.APN_ALARM_COOLDOWN_MILLIS) {
            mTinydb.putLong(ProtectionConstants.PREF_APN_LAST_ALARM_MILLIS, now);
            log.warn("APN guard: mismatch persists, expected '{}' but active is '{}'.",
                    reference, currentValue);
            alarm(Status.HIGH, ProtectionConstants.EVENT_APN_MISMATCH,
                    "APN mismatch: active '" + currentValue + "'");
        }
    }

    /* ---------------------------------------------------------------------
     * Provider access
     * ------------------------------------------------------------------ */

    /**
     * Reads the preferred APN row from the telephony provider.
     *
     * @return the current {@code name}/{@code apn} pair, or {@code null} when the provider
     *         is not readable on this device/ROM (some firmware restricts it).
     */
    CurrentApn readCurrentApn() {
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                    Uri.parse(URI_PREFER_APN),
                    new String[]{COLUMN_NAME, COLUMN_APN},
                    null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                return null;
            }
            int nameIdx = cursor.getColumnIndex(COLUMN_NAME);
            int apnIdx = cursor.getColumnIndex(COLUMN_APN);
            String name = nameIdx >= 0 ? cursor.getString(nameIdx) : null;
            String apn = apnIdx >= 0 ? cursor.getString(apnIdx) : null;
            return new CurrentApn(name, apn);
        } catch (Exception e) {
            // SecurityException on locked-down ROMs, IllegalArgumentException on exotic ones.
            log.debug("APN guard: cannot read preferred APN: {}", e.getMessage());
            return null;
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /** Preferred-APN row: display {@code name} plus the actual {@code apn} address. */
    static final class CurrentApn {
        final String name;
        final String apn;

        CurrentApn(String name, String apn) {
            this.name = name;
            this.apn = apn;
        }

        boolean isEmpty() {
            return ApnGuard.isEmpty(name) && ApnGuard.isEmpty(apn);
        }
    }

    /* ---------------------------------------------------------------------
     * Pure matching helpers (no Android logging — JVM-testable)
     * ------------------------------------------------------------------ */

    /**
     * Normalizes an APN value for comparison: null-safe trim + lowercase.
     * Package-visible for unit tests.
     */
    static String normalizeApn(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(java.util.Locale.US);
    }

    /**
     * Matches an expected (already normalized or raw) value against the active APN row.
     * A match on either the {@code apn} address or the display {@code name} counts, so the
     * user may type whichever one they see in the system APN screen.
     * Package-visible for unit tests.
     */
    static boolean apnMatches(String expected, String currentName, String currentApn) {
        String want = normalizeApn(expected);
        if (isEmpty(want)) {
            return false;
        }
        return want.equals(normalizeApn(currentName)) || want.equals(normalizeApn(currentApn));
    }

    /**
     * Picks the value to store/alarm on from an APN row: the {@code apn} address wins
     * (it is what actually routes traffic); the display {@code name} is the fallback.
     * Package-visible for unit tests.
     */
    static String pickBaselineValue(String name, String apn) {
        if (!isEmpty(apn)) {
            return apn.trim();
        }
        return name == null ? "" : name.trim();
    }

    /** Plain-JVM equivalent of {@code TextUtils.isEmpty} for the testable helpers above. */
    static boolean isEmpty(CharSequence value) {
        return value == null || value.length() == 0;
    }

    /* ---------------------------------------------------------------------
     * Episode state + event dispatch
     * ------------------------------------------------------------------ */

    private boolean hasMismatchEpisode() {
        return !TextUtils.isEmpty(
                mTinydb.getString(ProtectionConstants.PREF_APN_MISMATCH_VALUE));
    }

    private void clearMismatchEpisode() {
        mTinydb.remove(ProtectionConstants.PREF_APN_MISMATCH_VALUE);
        mTinydb.remove(ProtectionConstants.PREF_APN_MISMATCH_SINCE);
    }

    /**
     * Records the event into the EventLog and hands it to the {@link SimSwapper.Reactor} so
     * the app status notification is updated and the automatic countermeasures triggered.
     */
    private void alarm(Status threatLevel, int eventId, String description) {
        @Cleanup Realm realm = Realm.getDefaultInstance();
        mDbHelper.toEventLog(realm, eventId, description);
        if (mReactor != null) {
            mReactor.onProtectionEvent(threatLevel, eventId, description);
        }
    }

    /** Records an informational event into the EventLog without raising the threat status. */
    private void logOnly(int eventId, String description) {
        @Cleanup Realm realm = Realm.getDefaultInstance();
        mDbHelper.toEventLog(realm, eventId, description);
    }
}

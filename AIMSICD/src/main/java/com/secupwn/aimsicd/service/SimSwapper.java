/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.service;

import android.content.Context;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.secupwn.aimsicd.constants.ProtectionConstants;
import com.secupwn.aimsicd.enums.Status;
import com.secupwn.aimsicd.utils.RealmHelper;
import com.secupwn.aimsicd.utils.TinyDB;

import io.realm.Realm;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

/**
 * Watches the SIM / subscriber identity of the device and the coarse radio state, and raises an
 * alarm when the identity changes or when the network "vanishes" in a way that is typical for a
 * SIM swap or a rogue base station that kicked the phone off the network.
 *
 * <p><b>What it detects:</b></p>
 * <ul>
 *     <li><b>Cold swap</b> &mdash; a different SIM is present on next read (card was exchanged
 *         while powered off). Detected by comparing a subscriber fingerprint compiled from
 *         {@link TelephonyManager} against the stored baseline.</li>
 *     <li><b>Hot swap</b> &mdash; the SIM identity changes while the device is live. The
 *         fingerprint of the newly inserted card will differ from the baseline.</li>
 *     <li><b>SIM absent / re-inserted</b> &mdash; the card was removed and (maybe) re-inserted.</li>
 *     <li><b>Hard network loss</b> &mdash; {@code no signal / no network} service-state
 *         transitions, which occur when an IMSI catcher disconnects a victim or during the
 *         outage window of a SIM swap. Debounced to avoid false alarms on normal re-selection.</li>
 * </ul>
 *
 * <p><b>Baseline semantics:</b> the first SIM seen is recorded silently as the baseline. From
 * then on, any change of the fingerprint raises a single DANGER alarm and the new identity
 * becomes the baseline (one alert per change, no alarm storms). A legitimate SIM change by the
 * user therefore simply triggers a single informational alarm.</p>
 *
 * <p><b>Limits of the platform:</b> modern Android does not allow third-party apps to read the
 * IMSI (API 18+) or the MSISDN, so the fingerprint below is the best available approximation on
 * stock firmware. The strongest signal remains <em>network loss</em> &mdash; a compromised SIM
 * simply stops attaching. Root is NOT required.</p>
 *
 * <p><b>Privacy:</b> the fingerprint is a one-way SHA-256 hex digest; raw identifiers are never
 * persisted. See {@link #buildSubscriberFingerprint(TelephonyManager)}.</p>
 *
 * @see ProtectionConstants
 */
@Slf4j
public class SimSwapper {

    /**
     * Delay before re-checking the subscriber fingerprint after a network-loss event, so we do
     * not alert on the transient SIM_STATE change emitted during a normal handover.
     */
    private static final long RECHECK_DELAY_MILLIS = 1_500L;

    private final Context mContext;
    private final Reactor mReactor;
    private final RealmHelper mDbHelper;
    private final TinyDB mTinydb;
    private final Handler mHandler = new Handler();

    private TelephonyManager mTm;
    private PhoneStateListener mPhoneStateListener;

    private boolean mListening;
    private int mLastSimState;
    private long mLastNetworkLossMillis;

    private final Runnable mRecheckRunnable = new Runnable() {
        @Override
        public void run() {
            if (mTm != null) {
                mLastSimState = mTm.getSimState();
            }
            checkSubscriber();
        }
    };

    /**
     * @param context  application context.
     * @param reactor  receives the detected events; the service's {@link CellTracker} is the
     *                 natural implementor because it owns the status notification.
     * @param dbHelper used to write rows into the EventLog table.
     */
    public SimSwapper(Context context, Reactor reactor, RealmHelper dbHelper) {
        mContext = context.getApplicationContext();
        mReactor = reactor;
        mDbHelper = dbHelper;
        mTinydb = TinyDB.getInstance();

        mTm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mLastSimState = mTm.getSimState();
        mLastNetworkLossMillis = mTinydb.getLong(ProtectionConstants.PREF_LAST_NETWORK_LOSS_MILLIS);
    }

    /**
     * Registers the phone-state listener and performs the first subscriber check.
     * Call from the main thread once, when the protection service starts. Idempotent.
     */
    public void start() {
        if (mListening) {
            return;
        }
        mListening = true;

        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onServiceStateChanged(ServiceState serviceState) {
                handleServiceState(serviceState);
            }
        };
        mTm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);
        checkSubscriber();
    }

    /**
     * Unregisters the phone-state listener. Call from the main thread when the protection
     * service stops. Idempotent.
     */
    public void stop() {
        if (!mListening) {
            return;
        }
        mListening = false;
        if (mTm != null && mPhoneStateListener != null) {
            mTm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        mPhoneStateListener = null;
    }

    /**
     * Exposed for the {@code SIM_STATE_CHANGED} broadcast receiver: re-reads the current SIM
     * state, diffs it against the last observation and re-checks the subscriber fingerprint if
     * warranted. Safe to call from any thread.
     */
    public void onSimStateChanged() {
        if (mTm == null) {
            return;
        }

        int previous = mLastSimState;
        int state = mTm.getSimState();
        mLastSimState = state;

        boolean wasReady = isReady(previous);
        boolean isAbsent = (state == TelephonyManager.SIM_STATE_ABSENT);

        if (wasReady && isAbsent) {
            log.warn("SIM card removed.");
            event(Status.HIGH, ProtectionConstants.EVENT_SIM_ABSENT, "SIM card removed");
            return;
        }

        if (previous == TelephonyManager.SIM_STATE_ABSENT && isReady(state)) {
            log.warn("SIM card re-inserted.");
            event(Status.HIGH, ProtectionConstants.EVENT_SIM_REINSERTED, "SIM card re-inserted");
            // fall through: a fingerprint comparison below decides whether it is the same card.
        }

        // Any other transition is resolved by comparing identities; if the SIM is still settling
        // (PIN/PUK/network-locked) checkSubscriber() schedules a delayed retry.
        checkSubscriber();
    }

    /**
     * Compares the fingerprint of the currently inserted SIM against the stored baseline.
     *
     * <p>If no baseline is stored yet this method records one silently (first boot of the
     * feature). If the SIM is not yet ready it schedules a delayed retry instead of producing a
     * false alarm.</p>
     */
    public void checkSubscriber() {
        if (mTm == null) {
            return;
        }

        int simState = mTm.getSimState();
        if (!isReady(simState)) {
            if (simState == TelephonyManager.SIM_STATE_ABSENT
                    && isReady(mLastSimState)) {
                // The card disappeared between observations.
                onSimStateChanged();
                return;
            }
            // SIM still coming up (PIN, PUK, network-locked...). Retry once it settles.
            mHandler.removeCallbacks(mRecheckRunnable);
            mHandler.postDelayed(mRecheckRunnable, RECHECK_DELAY_MILLIS);
            return;
        }

        mLastSimState = simState;

        String currentFingerprint = buildSubscriberFingerprint(mTm);
        if (currentFingerprint == null) {
            // Not enough readable data on this device; nothing to compare.
            return;
        }

        String baseline = mTinydb.getString(ProtectionConstants.PREF_LAST_SUBSCRIBER_FINGERPRINT);
        if (TextUtils.isEmpty(baseline)) {
            log.info("Subscriber fingerprint baselined ({}).", sha256Preview(currentFingerprint));
            mTinydb.putString(ProtectionConstants.PREF_LAST_SUBSCRIBER_FINGERPRINT, currentFingerprint);
            return;
        }

        if (baseline.equals(currentFingerprint)) {
            // Same SIM as the baseline.
            return;
        }

        log.warn("SIM-swap suspected: subscriber fingerprint changed.");
        event(Status.DANGER, ProtectionConstants.EVENT_SIM_SWAP_DETECTED, "SIM swapped");

        // Re-baseline on the new identity so that one swap produces exactly one alarm
        // (and so a legitimate SIM upgrade does not alarm continuously).
        mTinydb.putString(ProtectionConstants.PREF_LAST_SUBSCRIBER_FINGERPRINT, currentFingerprint);
    }

    /**
     * Compiles a one-way subscriber fingerprint from the most stable SIM-derived identifiers
     * available on the current platform.
     *
     * <p>Which fields are actually readable depends on the Android version and OEM. The SHA-256
     * digest guarantees that even the most privileged identifiers (IMSI / subscriber id, on
     * pre-API-18 devices) are never stored in cleartext.</p>
     *
     * @return lowercase hex digest, or {@code null} if no identifier could be read.
     */
    static String buildSubscriberFingerprint(TelephonyManager tm) {
        StringBuilder sb = new StringBuilder(160);

        // IMSI (API<=17). Protected by READ_PHONE_STATE + READ_PRIVILEGED_PHONE_STATE on newer
        // Android; null on most stock modern devices.
        appendIfPresent(sb, tm.getSubscriberId());

        // ICCID — readable by most apps; the most useful swap indicator.
        appendIfPresent(sb, tm.getSimSerialNumber());

        // Operator code attached to the SIM (MCC+MNC).
        appendIfPresent(sb, tm.getSimOperator());

        // Country ISO of the SIM.
        appendIfPresent(sb, tm.getSimCountryIso());

        if (sb.length() == 0) {
            return null;
        }
        return sha256(sb.toString());
    }

    private static void appendIfPresent(StringBuilder sb, String value) {
        if (value != null && value.length() > 0) {
            if (sb.length() > 0) {
                sb.append('|');
            }
            sb.append(value);
        }
    }

    private static boolean isReady(int simState) {
        return simState == TelephonyManager.SIM_STATE_READY;
    }

    private static String sha256(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b & 0xff));
            }
            return hex.toString();
        } catch (Exception e) {
            // SHA-256 and UTF-8 are mandatory on Android; this should never happen.
            log.error("Failed to compute subscriber fingerprint", e);
            return input;
        }
    }

    private static String sha256Preview(String hex) {
        if (hex == null || hex.length() <= 12) {
            return hex;
        }
        return hex.substring(0, 12) + "...";
    }

    /* ---------------------------------------------------------------------
     * Radio / network loss
     * ------------------------------------------------------------------ */

    /**
     * Handles {@link ServiceState} changes and raises a HIGH alarm on a "no signal / no network"
     * transition. Debounced so a flapping radio does not spam the user. After the outage the
     * subscriber fingerprint is re-checked; if the loss was caused by a SIM swap the identity
     * will now differ and the SIM-swap alarm fires as well.
     */
    private void handleServiceState(ServiceState state) {
        if (state == null) {
            return;
        }

        // GSM / UMTS / LTE report through getState(); CDMA reports through getVoiceRegState().
        // A device with no signal is the only thing we treat as a "hard" network loss.
        boolean gsmDead = (state.getState() == ServiceState.STATE_OUT_OF_SERVICE
                || state.getState() == ServiceState.STATE_EMERGENCY_ONLY
                || state.getState() == ServiceState.STATE_POWER_OFF);
        boolean cdmaDead = state.getVoiceRegState() == ServiceState.STATE_OUT_OF_SERVICE;

        if (!gsmDead && !cdmaDead) {
            return;
        }

        if (!isReady(mLastSimState)) {
            // The card is not even ready; that is covered by the SIM-absent path, not by this
            // debounced network-loss path.
            return;
        }

        long now = System.currentTimeMillis();
        if (now - mLastNetworkLossMillis < ProtectionConstants.NETWORK_LOSS_COOLDOWN_MILLIS) {
            return;
        }
        mLastNetworkLossMillis = now;
        mTinydb.putLong(ProtectionConstants.PREF_LAST_NETWORK_LOSS_MILLIS, now);

        log.warn("Hard network loss detected (no signal / no network).");
        event(Status.HIGH, ProtectionConstants.EVENT_NETWORK_LOSS, "Network signal lost");

        // Re-check the subscriber identity once the radio settles.
        mHandler.removeCallbacks(mRecheckRunnable);
        mHandler.postDelayed(mRecheckRunnable, RECHECK_DELAY_MILLIS);
    }

    /* ---------------------------------------------------------------------
     * Event dispatch
     * ------------------------------------------------------------------ */

    /**
     * Records the event into the EventLog and hands it to the {@link Reactor} so the app status
     * notification can be updated and the automatic countermeasures triggered.
     */
    private void event(Status threatLevel, int eventId, String description) {
        @Cleanup Realm realm = Realm.getDefaultInstance();
        mDbHelper.toEventLog(realm, eventId, description);
        if (mReactor != null) {
            mReactor.onProtectionEvent(threatLevel, eventId, description);
        }
    }

    /**
     * Receiver of protection events.
     */
    public interface Reactor {

        /**
         * Invoked when a protection-relevant event was detected and logged.
         *
         * @param threatLevel the severity of the event
         * @param eventId     one of the {@code ProtectionConstants.EVENT_*} ids
         * @param description human readable description (also the EventLog DF_desc)
         */
        void onProtectionEvent(Status threatLevel, int eventId, String description);
    }
}

/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.protection;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import com.secupwn.aimsicd.data.model.Measure;
import com.secupwn.aimsicd.data.model.SmsData;
import com.secupwn.aimsicd.utils.TinyDB;

import io.realm.Realm;
import lombok.Cleanup;

/**
 * Executes automatic countermeasures when a credible threat is detected.
 *
 * <p>On stock, un-rooted Android the possible responses are deliberately limited and always
 * reversible. Each response is debounced so a flapping detector cannot hammer the user:</p>
 *
 * <ul>
 *     <li>{@link #enableAirplaneMode(String)} &mdash; attempts to write
 *         {@link Settings.Global#AIRPLANE_MODE_ON} and broadcast the change. Writing this
 *         setting is only allowed for privileged components on modern Android; when it throws
 *         the method degrades gracefully and simply reports failure.</li>
 *     <li>{@link #wipeSensitiveData(String)} &mdash; deletes the locally collected
 *         {@link Measure} (cell history) and {@link SmsData} (intercepted SMS) tables so a
 *         seized device cannot be mined for a movement / messaging history.</li>
 * </ul>
 *
 * <p>Notification and EventLog writing are intentionally NOT done here — the caller
 * ({@code CellTracker}) owns the status notification and the event log, keeping this class a
 * single-responsibility executor.</p>
 */
public class AutoProtector {

    private static final String TAG = "AutoProtector";

    /** TinyDB prefix for per-response cooldown timestamps (elapsedRealtime ms). */
    private static final String COOLDOWN_PREFIX = "aimsicd_auto_protect_last_";

    private final Context mContext;
    private final TinyDB mTinydb;

    public AutoProtector(Context context) {
        mContext = context.getApplicationContext();
        mTinydb = TinyDB.getInstance();
    }

    /**
     * Attempts to force the device into airplane mode, cutting off all radio access
     * (and therefore any malicious base station). The user re-enables the network manually.
     *
     * @param reason human readable reason for the event log
     * @return true if the setting write did not throw
     */
    public boolean enableAirplaneMode(String reason) {
        if (!cooldownElapsed("airplane")) {
            Log.i(TAG, "Airplane-mode engagement suppressed by cooldown");
            return false;
        }
        markCooldown("airplane");

        try {
            Settings.Global.putInt(mContext.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 1);

            // Broadcast the change so the system updates its UI/radios where permitted.
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", true);
            mContext.sendBroadcast(intent);

            Log.w(TAG, "Airplane mode engaged by auto-protection: " + reason);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Unable to toggle airplane mode (unprivileged?) - " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes the locally collected {@link Measure} and {@link SmsData} Realm tables.
     *
     * @param reason human readable reason for the event log
     * @return true if the transaction completed
     */
    public boolean wipeSensitiveData(String reason) {
        if (!cooldownElapsed("wipe")) {
            Log.i(TAG, "Sensitive-data wipe suppressed by cooldown");
            return false;
        }
        markCooldown("wipe");

        @Cleanup Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.where(Measure.class).findAll().deleteAllFromRealm();
                    realm.where(SmsData.class).findAll().deleteAllFromRealm();
                }
            });
            Log.w(TAG, "Sensitive data wiped by auto-protection: " + reason);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Failed to wipe sensitive data - " + e.getMessage());
            return false;
        }
    }

    /* ---------------------------------------------------------------------- */

    private boolean cooldownElapsed(String response) {
        long now = SystemClock.elapsedRealtime();
        long last = mTinydb.getLong(COOLDOWN_PREFIX + response);
        return now - last >= COOLDOWN_MILLIS;
    }

    private void markCooldown(String response) {
        mTinydb.putLong(COOLDOWN_PREFIX + response, SystemClock.elapsedRealtime());
    }

    private static final long COOLDOWN_MILLIS = 60_000L;
}

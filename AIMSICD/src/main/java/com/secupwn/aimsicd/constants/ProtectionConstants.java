/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.constants;

/**
 * Constants shared by the SIM-swap / automatic protection components.
 *
 * <p>The {@code EVENT_*} values extend the DF_id / DF_desc scheme already used by
 * {@link com.secupwn.aimsicd.data.model.Event Event} and
 * {@link com.secupwn.aimsicd.utils.RealmHelper#toEventLog(io.realm.Realm, int, String)}.
 * The original, hand-maintained IDs and their meaning:</p>
 *
 * <pre>
 * 1  changing locationAreaCode
 * 2  cell not in OCID
 * 3  Detected Type-0 SMS
 * 4  Detected MWI SMS
 * 5  Detected WAP PUSH SMS
 * 6  Detected WAP PUSH (2) SMS
 * 7  (free)
 * </pre>
 *
 * IDs {@code 100..109} are reserved for the auto-protection subsystem so that they can never
 * collide with the historical identifiers above.
 */
public final class ProtectionConstants {

    private ProtectionConstants() {
        // Prevent instantiation.
    }

    /* ---------------------------------------------------------------------
     * EventLog detection-factor IDs (DF_id).
     * ------------------------------------------------------------------ */

    /** SimSwapper detected that the SIM / subscriber identity changed. */
    public static final int EVENT_SIM_SWAP_DETECTED = 100;

    /** SimSwapper detected that the SIM card entered the ABSENT state. */
    public static final int EVENT_SIM_ABSENT = 101;

    /** SimSwapper detected that the SIM card entered the READY state after being absent. */
    public static final int EVENT_SIM_REINSERTED = 102;

    /** ApnGuard silently recorded the currently active APN as the baseline (auto-bind). */
    public static final int EVENT_APN_BASELINED = 103;

    /** SimSwapper detected a hard radio/network loss (no signal / no network). */
    public static final int EVENT_NETWORK_LOSS = 104;

    /** An automatic countermeasure switched the device into airplane mode. */
    public static final int EVENT_AIRPLANE_ENGAGED = 105;

    /** An automatic countermeasure wiped the locally collected measurement / SMS tables. */
    public static final int EVENT_SENSITIVE_DATA_WIPED = 106;

    /** ApnGuard detected that the active APN differs from the expected / baseline APN. */
    public static final int EVENT_APN_MISMATCH = 107;

    /** ApnGuard auto-adapted: a persistently changed APN was adopted as the new baseline. */
    public static final int EVENT_APN_REBOUND = 108;

    /** ApnGuard observed the active APN return to the expected / baseline APN. */
    public static final int EVENT_APN_RESTORED = 109;

    /* ---------------------------------------------------------------------
     * SharedPreferences (TinyDB) keys.
     * ------------------------------------------------------------------ */

    /** Stores the one-way subscriber fingerprint of the baseline SIM. */
    public static final String PREF_LAST_SUBSCRIBER_FINGERPRINT = "aimsicd_last_subscriber_fingerprint";

    /** Epoch milliseconds of the last detected hard network loss (used for cooldown). */
    public static final String PREF_LAST_NETWORK_LOSS_MILLIS = "aimsicd_last_network_loss_millis";

    /** Minimum interval (ms) between two consecutive network-loss events before re-alerting. */
    public static final long NETWORK_LOSS_COOLDOWN_MILLIS = 30_000L;

    /** Stores the learned baseline APN value (raw {@code apn} column, see {@code ApnGuard}). */
    public static final String PREF_APN_BASELINE = "aimsicd_apn_baseline";

    /** APN value of the currently open mismatch episode (empty when there is none). */
    public static final String PREF_APN_MISMATCH_VALUE = "aimsicd_apn_mismatch_value";

    /** Epoch milliseconds of the first sighting of the currently open mismatch episode. */
    public static final String PREF_APN_MISMATCH_SINCE = "aimsicd_apn_mismatch_since";

    /** Epoch milliseconds of the last APN-mismatch alarm (used for cooldown). */
    public static final String PREF_APN_LAST_ALARM_MILLIS = "aimsicd_apn_last_alarm_millis";

    /** Minimum interval (ms) between two consecutive APN-mismatch alarms. */
    public static final long APN_ALARM_COOLDOWN_MILLIS = 5 * 60_000L;

    /**
     * How long (ms) a mismatching APN must persist before auto-adapt adopts it as the new
     * baseline. Only applies to the learned baseline — a user-typed expected APN is never
     * overridden automatically.
     */
    public static final long APN_ADAPT_AFTER_MILLIS = 10 * 60_000L;
}

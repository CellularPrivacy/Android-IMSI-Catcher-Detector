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

    /** SimSwapper detected a hard radio/network loss (no signal / no network). */
    public static final int EVENT_NETWORK_LOSS = 104;

    /** An automatic countermeasure switched the device into airplane mode. */
    public static final int EVENT_AIRPLANE_ENGAGED = 105;

    /** An automatic countermeasure wiped the locally collected measurement / SMS tables. */
    public static final int EVENT_SENSITIVE_DATA_WIPED = 106;

    /* ---------------------------------------------------------------------
     * SharedPreferences (TinyDB) keys.
     * ------------------------------------------------------------------ */

    /** Stores the one-way subscriber fingerprint of the baseline SIM. */
    public static final String PREF_LAST_SUBSCRIBER_FINGERPRINT = "aimsicd_last_subscriber_fingerprint";

    /** Epoch milliseconds of the last detected hard network loss (used for cooldown). */
    public static final String PREF_LAST_NETWORK_LOSS_MILLIS = "aimsicd_last_network_loss_millis";

    /** Minimum interval (ms) between two consecutive network-loss events before re-alerting. */
    public static final long NETWORK_LOSS_COOLDOWN_MILLIS = 30_000L;
}

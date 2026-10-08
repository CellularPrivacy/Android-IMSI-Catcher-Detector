/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.defender;

/**
 * Callbacks for defender state changes. All callbacks may arrive on a
 * background thread; UI code must post to the main thread.
 */
public interface DefenderListener {

    /** Current aggregated threat level changed. */
    void onThreatLevelChanged(DefenderAgent.ThreatLevel newLevel, DefenderEvent cause);

    /** A new defender incident was recorded. */
    void onDefenderEvent(DefenderEvent event);

    /** Auto-protect / firewall / monitor toggles changed. */
    void onDefenderStateChanged();
}

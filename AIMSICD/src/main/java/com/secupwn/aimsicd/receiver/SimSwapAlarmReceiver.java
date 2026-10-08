/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.secupwn.aimsicd.service.AimsicdService;
import com.secupwn.aimsicd.service.SimSwapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Receives the {@code SIM_STATE_CHANGED} broadcast and forwards it to the running
 * {@link AimsicdService} so that hot SIM swaps (card exchanged while the device is powered on)
 * are detected in near real time.
 *
 * <p>The service may not be running: if so we only perform lightweight best-effort logging
 * while the broadcast is live.</p>
 */
@Slf4j
public class SimSwapAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_SIM_STATE_CHANGED.equals(intent.getAction())) {
            return;
        }

        // Modern Android does not cede the SIM state to unprivileged receivers, so we rely on
        // TelephonyManager as the source of truth.
        if (AimsicdService.isRunning()) {
            SimSwapper simSwapper = AimsicdService.getSimSwapper();
            if (simSwapper != null) {
                // Reads the current SIM state internally and diffs against the previous one.
                simSwapper.onSimStateChanged();
            } else {
                log.debug("SIM state changed, but the SimSwapper is not attached to the service.");
            }
        } else {
            log.debug("SIM state changed while the service was not running; nothing to do.");
        }
    }
}

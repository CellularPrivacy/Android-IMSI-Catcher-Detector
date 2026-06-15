/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.service.AimsicdService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(
                AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
        final String AUTO_START = context.getString(R.string.pref_autostart_key);
        boolean mAutoStart = prefs.getBoolean(AUTO_START, false);
        if (mAutoStart) {
            log.info("System booted starting service.");
            context.startService(new Intent(context, AimsicdService.class));
        }
    }
}

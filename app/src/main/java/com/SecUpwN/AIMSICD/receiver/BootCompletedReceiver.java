package com.SecUpwN.AIMSICD.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.SecUpwN.AIMSICD.service.AimsicdService;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(
                AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
        boolean mAutoStart = prefs.getBoolean("pref_autostart", true);
        if (mAutoStart) {
            Log.i("AIMSICD", "System booted starting service.");
            context.startService(new Intent(context, AimsicdService.class));
        }
    }
}

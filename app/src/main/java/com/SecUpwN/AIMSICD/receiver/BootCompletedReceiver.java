package com.SecUpwN.AIMSICD.receiver;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.service.AimsicdService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(
                AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
        final String AUTO_START = context.getString(R.string.pref_autostart_key);
        boolean mAutoStart = prefs.getBoolean(AUTO_START, false);
        if (mAutoStart) {
            Log.i("AIMSICD", "System booted starting service.");
            context.startService(new Intent(context, AimsicdService.class));
        }
    }
}

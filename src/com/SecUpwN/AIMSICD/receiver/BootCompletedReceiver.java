package com.SecUpwN.AIMSICD.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.SecUpwN.AIMSICD.service.AimsicdService;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
            Log.i("AIMSICD", "System booted starting service.");
            context.startService(new Intent(context, AimsicdService.class));
    }
}

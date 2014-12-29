package com.SecUpwN.AIMSICD.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.utils.Icon;
import com.SecUpwN.AIMSICD.utils.Status;

/**
 * Base activity class, handles code that is shared between all activites
 *
 * @author Tor Henning Ueland
 */
public class BaseActivity extends FragmentActivity {
    private static String TAG = "BaseActivity";

    /**
     * Triggered when GUI is opened
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "StatusWatcher starting watching");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("StatusChange"));
        updateIcon(this);
    }

    /**
     * Message reciever that handles icon update when status changes
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "StatusWatcher recieved status change to " + Status.getStatus().name()+", updating icon");
                    updateIcon(context);
        }
    };

    private void updateIcon(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
        final String iconType = prefs.getString(context.getString(R.string.pref_ui_icons_key), "SENSE").toUpperCase();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActionBar().setIcon(Icon.getIcon(Icon.Type.valueOf(iconType)));
            }
        });
    }

    /**
     * Triggered when GUI is closed/put to background
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "StatusWatcher stopped watching");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }
}

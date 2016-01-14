/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import com.SecUpwN.AIMSICD.AppAIMSICD;
import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.utils.Icon;

import io.freefair.android.injection.annotation.Inject;
import io.freefair.android.injection.app.InjectionAppCompatActivity;
import io.freefair.android.util.logging.Logger;

/**
 * Base activity class, handles code that is shared between all activities
 *
 * @author Tor Henning Ueland
 */
public class BaseActivity extends InjectionAppCompatActivity {

    @Inject
    private Logger log;

    /**
     * Triggered when GUI is opened
     */
    @Override
    protected void onResume() {
        super.onResume();
        log.debug("StatusWatcher starting watching");
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
            log.debug("StatusWatcher received status change to " + ((AppAIMSICD)getApplication()).getStatus().name() + ", updating icon");
            updateIcon(context);
        }
    };

    private void updateIcon(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
        final String iconType = prefs.getString(context.getString(R.string.pref_ui_icons_key), "SENSE").toUpperCase();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(getSupportActionBar() != null) {
                    getSupportActionBar().setIcon(Icon.getIcon(Icon.Type.valueOf(iconType), ((AppAIMSICD)getApplication()).getStatus()));
                }
            }
        });
    }

    /**
     * Triggered when GUI is closed/put to background
     */
    @Override
    protected void onPause() {
        super.onPause();
        log.debug("StatusWatcher stopped watching");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }
}

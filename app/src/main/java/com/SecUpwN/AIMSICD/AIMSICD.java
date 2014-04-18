/* Android IMSI Catcher Detector
 *      Copyright (C) 2014
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You may obtain a copy of the License at
 *      https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/LICENSE
 */

package com.SecUpwN.AIMSICD;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.SecUpwN.AIMSICD.service.AimsicdService;

public class AIMSICD extends Activity {

    private final String TAG = "AIMSICD";

    private final Context mContext = this;
    private Menu mMenu;
    private boolean mBound;
    private boolean mDisplayCurrent;
    private SharedPreferences prefs;
    private AIMSICDDbAdapter dbHelper;

    private AimsicdService mAimsicdService;

    //Back press to exit timer
    private long mLastPress = 0;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Bind to LocalService
        Intent intent = new Intent(this, AimsicdService.class);
        //Start Service before binding to keep it resident when activity is destroyed
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        //Create DB Instance
        dbHelper = new AIMSICDDbAdapter(mContext);

        prefs = mContext.getSharedPreferences(
                AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        final String KEY_KILL_SERVICE = mContext.getString(R.string.pref_killservice_key);
        boolean killService = prefs.getBoolean(KEY_KILL_SERVICE, false);
        if (killService) {
            Intent intent = new Intent(this, AimsicdService.class);
            stopService(intent);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mAimsicdService = ((AimsicdService.AimscidBinder) service).getService();
            mBound = true;
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG, "Service Disconnected");
            mBound = false;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (!mDisplayCurrent)
            updateUI();

    }

    private void updateUI() {
        TextView content;
        TableLayout tableLayout;
        TableRow tr;
        if (mBound) {
            switch (mAimsicdService.getPhoneID())
            {
                case TelephonyManager.PHONE_TYPE_GSM: {
                    tableLayout = (TableLayout) findViewById(R.id.cdmaView);
                    tableLayout.setVisibility(View.INVISIBLE);
                    tr = (TableRow) findViewById(R.id.gsm_cellid);
                    tr.setVisibility(View.VISIBLE);
                    content = (TextView) findViewById(R.id.network_lac);
                    content.setText(mAimsicdService.getLAC(true));
                    content = (TextView) findViewById(R.id.network_cellid);
                    content.setText(mAimsicdService.getCellId());
                    break;
                }
                case TelephonyManager.PHONE_TYPE_CDMA:
                {
                    tableLayout = (TableLayout) findViewById(R.id.cdmaView);
                    tableLayout.setVisibility(View.VISIBLE);
                    tr = (TableRow) findViewById(R.id.gsm_cellid);
                    tr.setVisibility(View.INVISIBLE);
                    content = (TextView) findViewById(R.id.network_netid);
                    content.setText(mAimsicdService.getLAC(true));
                    content = (TextView) findViewById(R.id.network_sysid);
                    content.setText(mAimsicdService.getSID());
                    content = (TextView) findViewById(R.id.network_baseid);
                    content.setText(mAimsicdService.getCellId());
                    break;
                }
            }

            content = (TextView) findViewById(R.id.sim_country);
            content.setText(mAimsicdService.getSimCountry(false));
            content = (TextView) findViewById(R.id.sim_operator_id);
            content.setText(mAimsicdService.getSimOperator(false));
            content = (TextView) findViewById(R.id.sim_operator_name);
            content.setText(mAimsicdService.getSimOperatorName(false));
            content = (TextView) findViewById(R.id.sim_imsi);
            content.setText(mAimsicdService.getSimSubs(false));
            content = (TextView) findViewById(R.id.sim_serial);
            content.setText(mAimsicdService.getSimSerial(false));

            int netID = mAimsicdService.getNetID(true);
            content = (TextView) findViewById(R.id.device_type);
            content.setText(mAimsicdService.getPhoneType(false));
            content = (TextView) findViewById(R.id.device_imei);
            content.setText(mAimsicdService.getIMEI(false));
            content = (TextView) findViewById(R.id.device_version);
            content.setText(mAimsicdService.getIMEIv(false));
            content = (TextView) findViewById(R.id.device_number);
            content.setText(mAimsicdService.getPhoneNumber(false));
            content = (TextView) findViewById(R.id.network_name);
            content.setText(mAimsicdService.getNetworkName(false));
            content = (TextView) findViewById(R.id.network_code);
            content.setText(mAimsicdService.getSmmcMcc(false));
            content = (TextView) findViewById(R.id.network_type);
            content.setText(mAimsicdService.getNetworkTypeName(netID, false));

            content = (TextView) findViewById(R.id.data_activity);
            content.setText(mAimsicdService.getActivityDesc(netID));
            content = (TextView) findViewById(R.id.data_status);
            content.setText(mAimsicdService.getStateDesc(netID));
            content = (TextView) findViewById(R.id.network_roaming);
            content.setText(mAimsicdService.isRoaming());

            Log.i(TAG, "**** AIMSICD ****");
            Log.i(TAG, "Device type   : " + mAimsicdService.getPhoneType(false));
            Log.i(TAG, "Device IMEI   : " + mAimsicdService.getIMEI(false));
            Log.i(TAG, "Device version: " + mAimsicdService.getIMEIv(false));
            Log.i(TAG, "Device num    : " + mAimsicdService.getPhoneNumber(false));
            Log.i(TAG, "Network type  : " + mAimsicdService.getNetworkTypeName(netID, false));
            Log.i(TAG, "Network CellID: " + mAimsicdService.getCellId());
            Log.i(TAG, "Network LAC   : " + mAimsicdService.getLAC(false));
            Log.i(TAG, "Network code  : " + mAimsicdService.getSmmcMcc(false));
            Log.i(TAG, "Network name  : " + mAimsicdService.getNetworkName(false));
            Log.i(TAG, "Roaming       : " + mAimsicdService.isRoaming());
            mDisplayCurrent = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mDisplayCurrent = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mTrackCell = menu.findItem(R.id.track_cell);
        MenuItem mTrackSignal = menu.findItem(R.id.track_signal);
        MenuItem mTrackLocation = menu.findItem(R.id.track_location);
        MenuItem mTrackFemtocell = menu.findItem(R.id.track_femtocell);

        if (mAimsicdService.TrackingCell) {
            mTrackCell.setTitle(R.string.untrack_cell);
            mTrackCell.setIcon(R.drawable.track_cell);
        } else {
            mTrackCell.setTitle(R.string.track_cell);
            mTrackCell.setIcon(R.drawable.untrack_cell);
        }

        if (mAimsicdService.TrackingSignal) {
            mTrackSignal.setTitle(R.string.untrack_signal);
            mTrackSignal.setIcon(R.drawable.ic_action_network_cell);
        } else {
            mTrackSignal.setTitle(R.string.track_signal);
            mTrackSignal.setIcon(R.drawable.ic_action_network_cell_not_tracked);
        }

        if (mAimsicdService.TrackingLocation) {
            mTrackLocation.setTitle(R.string.untrack_location);
            mTrackLocation.setIcon(R.drawable.ic_action_location_found);
        } else {
            mTrackLocation.setTitle(R.string.track_location);
            mTrackLocation.setIcon(R.drawable.ic_action_location_off);
        }

        if (mAimsicdService.TrackingFemtocell) {
            mTrackFemtocell.setTitle(R.string.untrack_femtocell);
            mTrackFemtocell.setIcon(R.drawable.ic_action_network_cell);
        } else {
            mTrackFemtocell.setTitle(R.string.track_femtocell);
            mTrackSignal.setIcon(R.drawable.ic_action_network_cell_not_tracked);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.track_cell:
                trackcell();
                if (Build.VERSION.SDK_INT > 11) {
                    onPrepareOptionsMenu(mMenu);
                }
                return true;
            case R.id.track_signal:
                tracksignal();
                if (Build.VERSION.SDK_INT > 11) {
                    onPrepareOptionsMenu(mMenu);
                }
                return true;
            case R.id.track_location:
                tracklocation();
                if (Build.VERSION.SDK_INT > 11) {
                    onPrepareOptionsMenu(mMenu);
                }
                return true;
            case R.id.track_femtocell:
                trackFemtocell();
                if (Build.VERSION.SDK_INT > 11) {
                    onPrepareOptionsMenu(mMenu);
                }
                return true;
            case R.id.show_map:
                showmap();
                return true;
            case R.id.preferences:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.export_database:
                dbHelper.exportDB();
                return true;
            case R.id.app_exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Exit application if back pressed twice
     */
    @Override
    public void onBackPressed() {
        Toast onBackPressedToast = Toast.makeText(this, R.string.press_once_again_to_exit, Toast.LENGTH_SHORT);
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastPress > 5000) {
            onBackPressedToast.show();
            mLastPress = currentTime;
        } else {
            onBackPressedToast.cancel();
            super.onBackPressed();
            finish();
        }
    }

    /**
     * Show the Map Viewer Activity
     */
    private void showmap() {
        Intent myIntent = new Intent(this, MapViewer.class);
        startActivity(myIntent);
    }

    public void tracksignal() {
        if (mAimsicdService.TrackingSignal) {
            mAimsicdService.setSignalTracking(false);
        } else {
            mAimsicdService.setSignalTracking(true);
        }
    }

    public void trackcell() {
        if (mAimsicdService.TrackingCell) {
            mAimsicdService.setCellTracking(false);
        } else {
            mAimsicdService.setCellTracking(true);
        }
    }

    public void tracklocation() {
        if (mAimsicdService.TrackingLocation) {
            mAimsicdService.setLocationTracking(false);
        } else {
            mAimsicdService.setLocationTracking(true);
        }
    }

    public void trackFemtocell() {
        if (mAimsicdService.TrackingFemtocell) {
            mAimsicdService.stopTrackingFemto();
        } else {
            mAimsicdService.startTrackingFemto();
        }
    }

    public AIMSICD getAimsicd() {
        return this;
    }

}

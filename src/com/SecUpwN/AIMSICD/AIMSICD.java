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
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.SecUpwN.AIMSICD.cmdprocessor.Helpers;
import com.SecUpwN.AIMSICD.service.AimsicdService;

public class AIMSICD extends Activity {

    private final String TAG = "AIMSICD";

    public static final String SHARED_PREFERENCES_BASENAME = "com.SecUpwN.AIMSICD";

    private final Context mContext = this;
    private Menu mMenu;
    private boolean mBound;
    private boolean mDisplayCurrent;
    public PhoneStateListener mSignalListenerStrength;
    public TelephonyManager tm;
    public LocationManager lm;
    public LocationListener mLocationListener;
    private AIMSICDDbAdapter dbHelper;

    private boolean TrackingCell;
    private boolean TrackingSignal;
    private boolean TrackingLocation;

    private AimsicdService mAimsicdService;

    //Back press to exit timer
    private long mLastPress = 0;

    //Notification ID
    private int mID = 1;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Create DB Instance
        dbHelper = new AIMSICDDbAdapter(mContext);

        // Bind to LocalService
        Intent intent = new Intent(this, AimsicdService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
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
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (!mDisplayCurrent)
            updateUI();
    }

    private void updateUI() {
        TextView content = (TextView) findViewById(R.id.sim_country);
        if (mBound) {
            if (mAimsicdService.getPhoneID() == TelephonyManager.PHONE_TYPE_GSM) {
                content.setText(mAimsicdService.getSimCountry(false));
                content = (TextView) findViewById(R.id.sim_operator_id);
                content.setText(mAimsicdService.getSimOperator(false));
                content = (TextView) findViewById(R.id.sim_operator_name);
                content.setText(mAimsicdService.getSimOperatorName(false));
                content = (TextView) findViewById(R.id.sim_imsi);
                content.setText(mAimsicdService.getSimSubs(false));
                content = (TextView) findViewById(R.id.sim_serial);
                content.setText(mAimsicdService.getSimSerial(false));
            } else {
                content.setText(R.string.gsm_only);
                content = (TextView) findViewById(R.id.sim_operator_id);
                content.setText(R.string.gsm_only);
                content = (TextView) findViewById(R.id.sim_operator_name);
                content.setText(R.string.gsm_only);
                content = (TextView) findViewById(R.id.sim_imsi);
                content.setText(R.string.gsm_only);
                content = (TextView) findViewById(R.id.sim_serial);
                content.setText(R.string.gsm_only);
            }

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
            content.setText(mAimsicdService.getNetworkTypeName());
            content = (TextView) findViewById(R.id.network_lac);
            content.setText(mAimsicdService.getLAC(false));
            content = (TextView) findViewById(R.id.network_cellid);
            content.setText(mAimsicdService.getCellId(false));

            content = (TextView) findViewById(R.id.data_activity);
            content.setText(mAimsicdService.getActivityDesc(netID));
            content = (TextView) findViewById(R.id.data_status);
            content.setText(mAimsicdService.getStateDesc(netID));

            Log.i(TAG, "**** AIMSICD ****");
            Log.i(TAG, "Device type   : " + mAimsicdService.getPhoneType(false));
            Log.i(TAG, "Device IMEI   : " + mAimsicdService.getIMEI(false));
            Log.i(TAG, "Device version: " + mAimsicdService.getIMEIv(false));
            Log.i(TAG, "Device num    : " + mAimsicdService.getPhoneNumber(false));
            Log.i(TAG, "Network type  : " + mAimsicdService.getNetworkTypeName());
            Log.i(TAG, "Network CellID: " + mAimsicdService.getCellId(false));
            Log.i(TAG, "Network LAC   : " + mAimsicdService.getLAC(false));
            Log.i(TAG, "Network code  : " + mAimsicdService.getSmmcMcc(false));
            Log.i(TAG, "Network name  : " + mAimsicdService.getNetworkName(false));
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

        if (isTrackingCell()) {
            mTrackCell.setTitle(R.string.track_cell);
            mTrackCell.setIcon(R.drawable.track_cell);
        } else {
            mTrackCell.setTitle(R.string.untrack_cell);
            mTrackCell.setIcon(R.drawable.untrack_cell);
        }

        if (isTrackingSignal()) {
            mTrackSignal.setTitle(R.string.track_signal);
            mTrackSignal.setIcon(R.drawable.ic_action_network_cell);
        } else {
            mTrackSignal.setTitle(R.string.untrack_signal);
            mTrackSignal.setIcon(R.drawable.ic_action_network_cell_not_tracked);
        }
        if (isTrackingLocation()) {
            mTrackLocation.setTitle(R.string.track_location);
            mTrackLocation.setIcon(R.drawable.ic_action_location_found);
        } else {
            mTrackLocation.setTitle(R.string.untrack_location);
            mTrackLocation.setIcon(R.drawable.ic_action_location_off);
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
            case R.id.show_map:
                showmap();
                return true;
            case R.id.export_database:
                dbHelper.exportDB();
                return true;
            case R.id.at_injector:
                Intent intent = new Intent(this, ATRilHook.class);
                startActivity(intent);
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
        if (TrackingSignal) {
            tm.listen(mSignalListenerStrength, PhoneStateListener.LISTEN_NONE);
            Helpers.msgShort(mContext, "Stopped tracking signal strength");
            TrackingSignal = false;
            mAimsicdService.mSignalInfo = 0;
        } else {
            tm.listen(mSignalListenerStrength, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            Helpers.msgShort(mContext, "Tracking signal strength");
            TrackingSignal = true;
        }
    }

    public void trackcell() {
        if (TrackingCell) {
            tm.listen(mSignalListenerLocation, PhoneStateListener.LISTEN_NONE);
            Helpers.msgShort(mContext, "Stopped tracking cell information");
            TrackingCell = false;
            mAimsicdService.mCellInfo = "[0,0]|nn|nn|";
        } else {
            tm.listen(mSignalListenerLocation, PhoneStateListener.LISTEN_CELL_LOCATION);
            Helpers.msgShort(mContext, "Tracking cell information");
            TrackingCell = true;
        }
    }

    public void tracklocation() {
        if (TrackingLocation) {
            lm.removeUpdates(mLocationListener);
            Helpers.msgShort(mContext, "Stopped tracking location");
            TrackingLocation = false;
            mAimsicdService.mLongitude = 0.0;
            mAimsicdService.mLatitude = 0.0;
        } else {
            if (lm != null) {
                Log.i(TAG, "LocationManager already existed");
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                Helpers.msgShort(mContext, "Tracking location");
                TrackingLocation = true;
            } else {
                Log.i(TAG, "LocationManager did not existed");
                lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
                if (lm != null) {
                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Log.i(TAG, "LocationManager created");
                        mLocationListener = new MyLocationListener();
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                        Helpers.msgShort(mContext, "Tracking location");
                        TrackingLocation = true;
                    } else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setMessage(R.string.location_error_message)
                                .setTitle(R.string.location_error_title);
                        builder.create().show();
                    }
                }
            }
        }
    }

    private PhoneStateListener mSignalListenerLocation = new PhoneStateListener() {
        public void onCellLocationChanged(CellLocation location) {
            mAimsicdService.mNetID = mAimsicdService.getNetID(true);
            mAimsicdService.mNetType = tm.getNetworkTypeName();

            int dataActivityType = tm.getDataActivity();
            String dataActivity = "un";
            switch (dataActivityType) {
                case TelephonyManager.DATA_ACTIVITY_NONE:
                    dataActivity = "No";
                    break;
                case TelephonyManager.DATA_ACTIVITY_IN:
                    dataActivity = "In";
                    break;
                case TelephonyManager.DATA_ACTIVITY_OUT:
                    dataActivity = "Ou";
                    break;
                case TelephonyManager.DATA_ACTIVITY_INOUT:
                    dataActivity = "IO";
                    break;
                case TelephonyManager.DATA_ACTIVITY_DORMANT:
                    dataActivity = "Do";
                    break;
            }

            int dataType = tm.getDataState();
            String dataState = "un";
            switch (dataType) {
                case TelephonyManager.DATA_DISCONNECTED:
                    dataState = "Di";
                    break;
                case TelephonyManager.DATA_CONNECTING:
                    dataState = "Ct";
                    break;
                case TelephonyManager.DATA_CONNECTED:
                    dataState = "Cd";
                    break;
                case TelephonyManager.DATA_SUSPENDED:
                    dataState = "Su";
                    break;
            }

            switch (mAimsicdService.mPhoneID) {
                case TelephonyManager.PHONE_TYPE_GSM:
                    GsmCellLocation gsmCellLocation = (GsmCellLocation) location;
                    if (gsmCellLocation != null) {
                        mAimsicdService.mCellInfo = gsmCellLocation.toString() + dataActivity + "|"
                                + dataState + "|" + mAimsicdService.mNetType + "|";
                        mAimsicdService.mLacID = gsmCellLocation.getLac();
                        mAimsicdService.mCellID = gsmCellLocation.getCid();
                        dbHelper.open();
                        if (isTrackingCell() && !dbHelper.cellExists(mAimsicdService.mCellID)){
                            mAimsicdService.mSimCountry = mAimsicdService.getSimCountry(true);
                            mAimsicdService.mSimOperator = mAimsicdService.getSimOperator(true);
                            mAimsicdService.mSimOperatorName = mAimsicdService.getSimOperatorName(true);
                            dbHelper.insertCell(mAimsicdService.mLacID, mAimsicdService.mCellID,
                                    mAimsicdService.mNetID, mAimsicdService.mLatitude,
                                    mAimsicdService.mLongitude, mAimsicdService.mSignalInfo,
                                    mAimsicdService.mCellInfo, mAimsicdService.mSimCountry,
                                    mAimsicdService.mSimOperator, mAimsicdService.mSimOperatorName);
                        }
                    }
                    break;
                case TelephonyManager.PHONE_TYPE_CDMA:
                    CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) location;
                    if (cdmaCellLocation != null) {
                        mAimsicdService.mCellInfo = cdmaCellLocation.toString() + dataActivity
                                + "|" + dataState + "|" + mAimsicdService.mNetType + "|";
                        mAimsicdService.mLacID = cdmaCellLocation.getNetworkId();
                        mAimsicdService.mCellID = cdmaCellLocation.getBaseStationId();
                        if (isTrackingCell() && !dbHelper.cellExists(mAimsicdService.mCellID)){
                            mAimsicdService.mSimCountry = mAimsicdService.getSimCountry(true);
                            mAimsicdService.mSimOperator = mAimsicdService.getSimOperator(true);
                            mAimsicdService.mSimOperatorName = mAimsicdService.getNetworkName(true);
                        }
                    }
            }

            if (TrackingCell && !dbHelper.cellExists(mAimsicdService.mCellID)) {
                dbHelper.insertCell(mAimsicdService.mLacID, mAimsicdService.mCellID,
                        mAimsicdService.mNetID, mAimsicdService.mLatitude,
                        mAimsicdService.mLongitude, mAimsicdService.mSignalInfo,
                        mAimsicdService.mCellInfo, mAimsicdService.mSimCountry,
                        mAimsicdService.mSimOperator, mAimsicdService.mSimOperatorName);
            }


        }
    };

    public class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                mAimsicdService.mLongitude = loc.getLongitude();
                mAimsicdService.mLatitude = loc.getLatitude();
            }
            if (TrackingLocation) {
                dbHelper.insertLocation(mAimsicdService.mLacID, mAimsicdService.mCellID,
                        mAimsicdService.mNetID, mAimsicdService.mLatitude,
                        mAimsicdService.mLongitude, mAimsicdService.mSignalInfo,
                        mAimsicdService.mCellInfo);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status,
                Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

    public Boolean isTrackingSignal() {
        return TrackingSignal;
    }

    public Boolean isTrackingCell() {
        return TrackingCell;
    }

    public Boolean isTrackingLocation() {
        return TrackingLocation;
    }


    public AIMSICDDbAdapter getDbHelper() {
        return dbHelper;
    }

}

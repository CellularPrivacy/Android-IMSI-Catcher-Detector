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

/*
 * Portions of this software have been copied and modified from
 * Femtocatcher https://github.com/iSECPartners/femtocatcher
 *
 * Copyright (C) 2013 iSEC Partners
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.SecUpwN.AIMSICD.service;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.cmdprocessor.Helpers;

public class AimsicdService extends Service implements OnSharedPreferenceChangeListener {

    private final String TAG = "AIMSICD_Service";
    public static final String SHARED_PREFERENCES_BASENAME = "com.SecUpwN.AIMSICD_preferences";

   /*
    * System and helper declarations
    */
    private final AimscidBinder mBinder = new AimscidBinder();
    public final AIMSICDDbAdapter dbHelper = new AIMSICDDbAdapter(this);
    private TelephonyManager tm;
    private LocationManager lm;
    private SharedPreferences prefs;
    private PhoneStateListener mPhoneStateListener;
    private LocationListener mLocationListener;

   /*
    * Device Declarations
    */
    public int mPhoneID = -1;
    public int mSignalInfo = -1;
    public int mNetID = -1;
    public int mLacID = -1;
    public int mCellID = -1;
    public double mLongitude = 0.0;
    public double mLatitude = 0.0;
    public String mNetType = "";
    public String mPhoneNum = "", mCellType = "", mLac = "", mCellInfo = "", mDataState = "";
    public String mNetName = "", mMmcmcc = "", mSimCountry = "", mPhoneType = "";
    public String mIMEI = "", mIMEIV = "", mSimOperator = "", mSimOperatorName = "";
    public String mSimSerial = "", mSimSubs = "", mDataActivityType = "";

   /*
    * Tracking and Alert Declarations
    */
    public boolean TrackingCell;
    public boolean TrackingSignal;
    public boolean TrackingLocation;
    public boolean TrackingFemtocell;
    private boolean mFemtoDetected;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class AimscidBinder extends Binder {
        public AimsicdService getService() {
            return AimsicdService.this;
        }
    }

    public void onCreate() {
        //TelephonyManager provides system details
        tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //Phone type and associated details
        mIMEI = tm.getDeviceId();
        mIMEIV = tm.getDeviceSoftwareVersion();
        mPhoneNum = tm.getLine1Number();
        mPhoneID = tm.getPhoneType();
        switch (mPhoneID) {
            case TelephonyManager.PHONE_TYPE_GSM:
                mPhoneType = "GSM";
                mMmcmcc = tm.getNetworkOperator();
                mNetName = tm.getNetworkOperatorName();
                GsmCellLocation gsmCellLocation = (GsmCellLocation) tm.getCellLocation();
                if (gsmCellLocation != null) {
                    mCellType = "" + gsmCellLocation.getCid();
                    mLac = "" + gsmCellLocation.getLac();
                }
                mSimCountry = tm.getSimCountryIso();
                mSimOperator = tm.getSimOperator();
                mSimOperatorName = tm.getSimOperatorName();
                mSimSerial = tm.getSimSerialNumber();
                mSimSubs = tm.getSubscriberId();
                break;
            case TelephonyManager.PHONE_TYPE_CDMA:
                mPhoneType = "CDMA";
                break;
        }

        //Network type
        mNetID = getNetID(true);
        mNetType = getNetworkTypeName(mNetID, true);

        int mDataActivity = tm.getDataActivity();
        mDataActivityType = getActivityDesc(mDataActivity);

        mDataActivity = tm.getDataState();
        mDataState = getStateDesc(mDataActivity);

        prefs = this.getSharedPreferences(
                AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
        prefs.registerOnSharedPreferenceChangeListener(this);
        setNotification();

        Log.i(TAG, "Service launched successfully");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        cancelNotification();

        Log.i(TAG, "Service destroyed");
    }

    public int getPhoneID() {
        if (mPhoneID <= 0 || mPhoneID > 6) {
            mPhoneID = tm.getPhoneType();
        }

        return mPhoneID;
    }

    public String getSimCountry(boolean force) {
        if (mSimCountry.isEmpty() || force) {
            mSimCountry = tm.getSimCountryIso();
        }

        return mSimCountry;
    }

    public String getSimOperator(boolean force) {
        if (mSimOperator.isEmpty() || force) {
            mSimOperator = tm.getSimOperator();
        }

        return mSimOperator;
    }

    public String getSimOperatorName(boolean force) {
        if (mSimOperatorName.isEmpty() || force) {
            mSimOperatorName = tm.getSimOperatorName();
        }

        return mSimOperatorName;
    }

    public String getSimSubs(boolean force) {
        if (mSimSubs.isEmpty() || force) {
            mSimSubs = tm.getSubscriberId();
        }

        return mSimSubs;
    }

    public String getSimSerial(boolean force) {
        if (mSimSerial.isEmpty() || force) {
            mSimSerial = tm.getSimSerialNumber();
        }

        return mSimSerial;
    }

    public String getPhoneType(boolean force) {
        if (mPhoneType.isEmpty() || force) {
            if (getPhoneID() == TelephonyManager.PHONE_TYPE_GSM) {
                mPhoneType = "GSM";
            } else if (getPhoneID() == TelephonyManager.PHONE_TYPE_CDMA) {
                mPhoneType = "CDMA";
            } else {
                mPhoneType = "Unknown";
            }
        }

        return mPhoneType;
    }

    public String getIMEI(boolean force) {
        if (mIMEI.isEmpty() || force) {
            mIMEI = tm.getDeviceId();
        }

        return mIMEI;
    }

    public String getIMEIv(boolean force) {
        if (mIMEIV.isEmpty() || force) {
            mIMEIV = tm.getDeviceSoftwareVersion();
        }

        return mIMEIV;
    }

    public String getPhoneNumber(boolean force) {
        if (mPhoneNum.isEmpty() || force) {
            mPhoneNum = tm.getLine1Number();
        }

        return mPhoneNum;
    }

    public String getNetworkName(boolean force) {
        if (mNetName.isEmpty() || force) {
            mNetName = tm.getNetworkOperatorName();
        }

        return mNetName;
    }

    public String getSmmcMcc(boolean force) {
        if (mMmcmcc.isEmpty() || force) {
            mMmcmcc = tm.getNetworkOperator();
        }

        return mMmcmcc;
    }

    public String getNetworkTypeName(int netID, boolean force) {
        if (mNetType.isEmpty() || force) {
            switch (netID) {
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    mNetType = "Unknown";
                    break;
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    mNetType = "GPRS";
                    break;
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    mNetType = "EDGE";
                    break;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    mNetType = "UMTS";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    mNetType = "HDSPA";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    mNetType = "HSUPA";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    mNetType = "HSPA";
                    break;
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    mNetType = "CDMA";
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    mNetType = "EVDO_0";
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    mNetType = "EVDO_A";
                    break;
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    mNetType = "1xRTT";
                    break;
                default:
                    mNetType = "Unknown";
                    break;
            }
        }

        return mNetType;
    }

    public int getNetID(boolean force) {
        if (mNetID == -1 || force) {
            mNetID = tm.getNetworkType();
        }

        return mNetID;
    }

    public String getLAC(boolean force) {
        if (mLac.isEmpty() || force) {
            GsmCellLocation gsmCellLocation = (GsmCellLocation) tm.getCellLocation();
            if (gsmCellLocation != null) {
                mLac = "" + gsmCellLocation.getLac();
                mLacID = gsmCellLocation.getLac();
            }
        }

        return mLac;
    }

    public String getCellId(boolean force) {
        if (mCellType.isEmpty() || force) {
            GsmCellLocation gsmCellLocation = (GsmCellLocation) tm.getCellLocation();
            if (gsmCellLocation != null) {
                mCellType = "" + gsmCellLocation.getCid();
                mCellID = gsmCellLocation.getCid();
            }
        }

        return mCellType;
    }

    public String getActivityDesc(int dataID) {
        mDataActivityType = "undef";
        switch (dataID) {
            case TelephonyManager.DATA_ACTIVITY_NONE:
                mDataActivityType = "None";
                break;
            case TelephonyManager.DATA_ACTIVITY_IN:
                mDataActivityType = "In";
                break;
            case TelephonyManager.DATA_ACTIVITY_OUT:
                mDataActivityType = "Out";
                break;
            case TelephonyManager.DATA_ACTIVITY_INOUT:
                mDataActivityType = "In-Out";
                break;
            case TelephonyManager.DATA_ACTIVITY_DORMANT:
                mDataActivityType = "Dormant";
                break;
        }
        return mDataActivityType;
    }

    public String getStateDesc(int dataID) {
        mDataState = "undef";
        switch (dataID) {
            case TelephonyManager.DATA_DISCONNECTED:
                mDataActivityType = "Disconnected";
                break;
            case TelephonyManager.DATA_CONNECTING:
                mDataActivityType = "Connecting";
                break;
            case TelephonyManager.DATA_CONNECTED:
                mDataActivityType = "Connected";
                break;
            case TelephonyManager.DATA_SUSPENDED:
                mDataActivityType = "Suspended";
                break;
        }

        return mDataState;
    }

    /**
     * Set or update the Notification
     *
     */
    public void setNotification() {

        Intent notificationIntent = new Intent(this, AIMSICD.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 0);

        String tickerText;
        String contentText = "Phone Type " + getPhoneType(false);

        SharedPreferences prefs = getSharedPreferences(
                SHARED_PREFERENCES_BASENAME, 0);
        String iconType = prefs.getString(
                "pref_ui_icons", "flat");

        int status;

        if (mFemtoDetected) {
            status = 3; //ALARM
        } else if (TrackingFemtocell || TrackingCell || TrackingLocation || TrackingSignal) {
            status = 2; //Good
            if (TrackingFemtocell)
                contentText = "FemtoCell Detection Active";
            else
                contentText = "Cell, Signal or Location Tracking Active";
        } else {
            status = 1; //Idle
        }

        int icon = R.drawable.sense_idle;

        switch (status) {
            case 1: //Idle
                if (iconType.equals("flat")) {
                    icon = R.drawable.flat_idle;
                } else if (iconType.equals("sense")) {
                    icon = R.drawable.sense_idle;
                } else if (iconType.equals("white")) {
                    icon = R.drawable.white_idle;
                }
                tickerText = getResources().getString(R.string.app_name_short)
                        + " - Status: Idle";
                break;
            case 2: //Good
                if (iconType.equals("flat")) {
                    icon = R.drawable.flat_good;
                } else if (iconType.equals("sense")) {
                    icon = R.drawable.sense_good;
                } else if (iconType.equals("white")) {
                    icon = R.drawable.white_good;
                }
                tickerText = getResources().getString(R.string.app_name_short)
                        + " - Status: Good No Threats Detected";
                break;
            case 3: //ALARM
                if (iconType.equals("flat")) {
                    icon = R.drawable.flat_alarm;
                } else if (iconType.equals("sense")) {
                    icon = R.drawable.sense_alarm;
                } else if (iconType.equals("white")) {
                    icon = R.drawable.white_alarm;
                }
                tickerText = getResources().getString(R.string.app_name_short)
                        + " - ALERT!! Threat Detected";
                contentText = "ALERT!! Threat Detected";
                break;
            default:
                icon = R.drawable.sense_idle;
                tickerText = getResources().getString(R.string.app_name);
                break;
        }

        Notification mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(icon)
                        .setTicker(tickerText)
                        .setContentTitle(this.getResources().getString(R.string.app_name))
                        .setContentText(contentText)
                        .setOngoing(true)
                        .setAutoCancel(false)
                        .setContentIntent(contentIntent)
                        .build();

        NotificationManager mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0x1212, mBuilder);
    }

    private void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(
                NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(0x1212);
        }
    }

    public void setCellTracking(boolean track) {
        if (track) {
            tm.listen(mCellSignalListener, PhoneStateListener.LISTEN_CELL_LOCATION);
            tm.listen(mSignalListenerStrength, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            Helpers.msgShort(this, "Tracking cell information");
            TrackingCell = true;
        } else {
            tm.listen(mCellSignalListener, PhoneStateListener.LISTEN_NONE);
            tm.listen(mSignalListenerStrength, PhoneStateListener.LISTEN_NONE);
            Helpers.msgShort(this, "Stopped tracking cell information");
            TrackingCell = false;
            mCellInfo = "[0,0]|nn|nn|";
        }
        setNotification();
    }

    public void setSignalTracking(boolean track) {
        if (track) {
            tm.listen(mSignalListenerStrength, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            Helpers.msgShort(this, "Tracking signal strength");
            TrackingSignal = true;
        } else {
            tm.listen(mSignalListenerStrength, PhoneStateListener.LISTEN_NONE);
            Helpers.msgShort(this, "Stopped tracking signal strength");
            TrackingSignal = false;
            mSignalInfo = 0;
        }
        setNotification();
    }

    private PhoneStateListener mSignalListenerStrength = new PhoneStateListener() {
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            switch (mPhoneID) {
                case TelephonyManager.PHONE_TYPE_GSM:
                    mSignalInfo = signalStrength.getGsmSignalStrength();
                    break;
                case TelephonyManager.PHONE_TYPE_CDMA:
                    mSignalInfo = signalStrength.getCdmaDbm();
                    break;
                default:
                    mSignalInfo = 0;
            }

            //Insert database record if tracking signal
            if (TrackingSignal) {
                dbHelper.open();
                dbHelper.insertSignal(mLacID,mCellID,
                        mNetID, mLatitude,
                        mLongitude,mSignalInfo,
                        mCellInfo);
            }
        }
    };

    private PhoneStateListener mCellSignalListener = new PhoneStateListener() {
        public void onCellLocationChanged(CellLocation location) {
            mNetID = getNetID(true);
            mNetType = getNetworkTypeName(mNetID, true);

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

            switch (mPhoneID) {
                case TelephonyManager.PHONE_TYPE_GSM:
                    GsmCellLocation gsmCellLocation = (GsmCellLocation) location;
                    if (gsmCellLocation != null) {
                        mCellInfo = gsmCellLocation.toString() + dataActivity + "|"
                                + dataState + "|" + mNetType + "|";
                        mLacID = gsmCellLocation.getLac();
                        mCellID = gsmCellLocation.getCid();
                        dbHelper.open();
                        mSimCountry = getSimCountry(true);
                        mSimOperator = getSimOperator(true);
                        mSimOperatorName = getSimOperatorName(true);
                    }
                    break;
                case TelephonyManager.PHONE_TYPE_CDMA:
                    CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) location;
                    if (cdmaCellLocation != null) {
                        mCellInfo = cdmaCellLocation.toString() + dataActivity
                                + "|" + dataState + "|" + mNetType + "|";
                        mLacID = cdmaCellLocation.getNetworkId();
                        mCellID = cdmaCellLocation.getBaseStationId();
                        mSimCountry = getSimCountry(true);
                        mSimOperator = getSimOperator(true);
                        mSimOperatorName = getNetworkName(true);
                    }
            }

            if (TrackingCell && !dbHelper.cellExists(mCellID)) {
                dbHelper.open();
                dbHelper.insertCell(mLacID, mCellID,
                        mNetID, mLatitude,
                        mLongitude, mSignalInfo,
                        mCellInfo, mSimCountry,
                        mSimOperator, mSimOperatorName);
            }
        }
    };

    public void setLocationTracking(boolean track) {
        if (track) {
            if (lm != null) {
                mLocationListener = new MyLocationListener();
                Log.i(TAG, "LocationManager already existed");
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                Helpers.msgShort(this, "Tracking location");
                TrackingLocation = true;
            } else {
                Log.i(TAG, "LocationManager did not existed");
                lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                if (lm != null) {
                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Log.i(TAG, "LocationManager created");
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                        Helpers.msgShort(this, "Tracking location");
                        TrackingLocation = true;
                    } else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage(R.string.location_error_message)
                                .setTitle(R.string.location_error_title);
                        builder.create().show();
                    }
                }
            }
        } else {
            lm.removeUpdates(mLocationListener);
            Helpers.msgShort(this, "Stopped tracking location");
            TrackingLocation = false;
            mLongitude = 0.0;
            mLatitude = 0.0;
        }
        setNotification();
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                mLongitude = loc.getLongitude();
                mLatitude = loc.getLatitude();
            }
            if (TrackingLocation) {
                dbHelper.open();
                dbHelper.insertLocation(mLacID, mCellID,
                        mNetID, mLatitude,
                        mLongitude, mSignalInfo,
                        mCellInfo);
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

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        final String KEY_UI_ICONS = getBaseContext().getString(R.string.pref_ui_icons_key);

        if (key.equals(KEY_UI_ICONS)) {
            //Update Notification to display selected icon type
            setNotification();
        }

    }

/*
 * The below code section was copied and modified from
 * Femtocatcher https://github.com/iSECPartners/femtocatcher
 *
 * Copyright (C) 2013 iSEC Partners
 */
    public void startTrackingFemto() {

        /* Check if it is a CDMA phone */
        if (getPhoneID() != TelephonyManager.PHONE_TYPE_CDMA) {
            Helpers.sendMsg(this, "AIMSICD can only detect Femtocell connections on CDMA devices.");
            return;
        }

        TrackingFemtocell = true;
        mPhoneStateListener = new PhoneStateListener() {
            public void onServiceStateChanged(ServiceState s) {
                Log.d(TAG, "Service State changed!");
                getServiceStateInfo(s);
            }
        };
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);
        setNotification();
    }

    public void stopTrackingFemto() {
        if (mPhoneStateListener != null) {
            tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            TrackingFemtocell = false;
            setNotification();
            Log.v(TAG, "Stopped tracking Femtocell connections");
        }
    }

    public void getServiceStateInfo(ServiceState s) {
        if (s != null) {
            if (IsConnectedToCdmaFemto(s)) {
                Helpers.sendMsg(this, "ALERT!! Femtocell Connection Detected!!");
                mFemtoDetected = true;
                setNotification();
                //toggleRadio();
            } else {
                mFemtoDetected = false;
                setNotification();
            }
        }
    }

    private boolean IsConnectedToCdmaFemto(ServiceState s) {
        if (s == null) {
            return false;
        }

        /* Get International Roaming indicator
         * if indicator is not 0 return false
         */

        /* Get the radio technology */
        int networkType = getNetID(true);

        /* Check if it is EvDo network */
        boolean evDoNetwork = isEvDoNetwork(networkType);

        /* If it is not an evDo network check the network ID range.
         * If it is connected to femtocell, the nid should be lie between [0xfa, 0xff)
         */
        if (!evDoNetwork) {
            /* get network ID */
            if (tm != null) {
                CdmaCellLocation c = (CdmaCellLocation) tm.getCellLocation();

                if (c != null) {
                    int networkID = c.getNetworkId();
                    int FEMTO_NID_MAX = 0xff;
                    int FEMTO_NID_MIN = 0xfa;
                    return !((networkID < FEMTO_NID_MIN) || (networkID >= FEMTO_NID_MAX));

                } else {
                    Log.v(TAG, "Cell location info is null.");
                    return false;
                }
            } else {
                Log.v(TAG, "Telephony Manager is null.");
                return false;
            }
        }

        /* if it is an evDo network */
        // TODO
        else {
            /* get network ID */
            if (tm != null) {
                CdmaCellLocation c = (CdmaCellLocation) tm.getCellLocation();

                if (c != null) {
                    int networkID = c.getNetworkId();

                    int FEMTO_NID_MAX = 0xff;
                    int FEMTO_NID_MIN = 0xfa;
                    return !((networkID < FEMTO_NID_MIN) || (networkID >= FEMTO_NID_MAX));
                } else {
                    Log.v(TAG, "Cell location info is null.");
                    return false;
                }
            } else {
                Log.v(TAG, "Telephony Manager is null.");
                return false;
            }
        }

    }

    private boolean isEvDoNetwork(int networkType) {
        if (Build.VERSION.SDK_INT > 11) {
            if ((networkType == TelephonyManager.NETWORK_TYPE_EVDO_0) ||
                    (networkType == TelephonyManager.NETWORK_TYPE_EVDO_A) ||
                    (networkType == TelephonyManager.NETWORK_TYPE_EVDO_B) ||
                    (networkType == TelephonyManager.NETWORK_TYPE_EHRPD)) {
                return true;
            }
        } else {
            if ((networkType == TelephonyManager.NETWORK_TYPE_EVDO_0) ||
                    (networkType == TelephonyManager.NETWORK_TYPE_EVDO_A) ||
                    (networkType == TelephonyManager.NETWORK_TYPE_EVDO_B)) {
                return true;
            }
        }
        return false;
    }
/*
 * The above code section was copied and modified from
 * Femtocatcher https://github.com/iSECPartners/femtocatcher
 *
 * Copyright (C) 2013 iSEC Partners
 */

}

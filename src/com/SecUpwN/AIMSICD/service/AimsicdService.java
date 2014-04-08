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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.R;

public class AimsicdService extends Service {

    private final String TAG = "AIMSICD_Service";

    //TODO: Clean this mess up!!
    private final AimscidBinder mBinder = new AimscidBinder();
    private TelephonyManager tm;
    public int mPhoneID = -1;
    public int mSignalInfo = -1;
    public int mNetID = -1;
    public int mLacID = -1;
    public int mCellID = -1;
    public double mLongitude;
    public double mLatitude;
    public String mNetType = "";
    public String mPhoneNum = "", mCellType = "", mLac = "", mCellInfo = "", mDataState = "";
    public String mNetName = "", mMmcmcc = "", mSimCountry = "", mPhoneType = "";
    public String mIMEI = "", mIMEIV = "", mSimOperator = "", mSimOperatorName = "";
    public String mSimSerial = "", mSimSubs = "", mDataActivityType = "";

    //Femtocell Detection
    private int FEMTO_NID_MIN = 0xfa;
    private int FEMTO_NID_MAX = 0xff;
    private PhoneStateListener mPhoneStateListener;

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
        mNetType = tm.getNetworkTypeName();

        int mDataActivity = tm.getDataActivity();
        mDataActivityType = getActivityDesc(mDataActivity);

        mDataActivity = tm.getDataState();
        mDataState = getStateDesc(mDataActivity);

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

    public String getNetworkTypeName() {
        return tm.getNetworkTypeName();

    }

    public int getNetID(boolean force) {
        if (mNetID < 0 || force) {
            mNetID = tm.getNetworkType();
        }

        return mNetID;
    }

    public String getLAC(boolean force) {
        if (mLac.isEmpty() || force) {
            GsmCellLocation gsmCellLocation = (GsmCellLocation) tm.getCellLocation();
            if (gsmCellLocation != null) {
                mLac = "" + gsmCellLocation.getLac();
            }
        }

        return mLac;
    }

    public String getCellId(boolean force) {
        if (mCellType.isEmpty() || force) {
            GsmCellLocation gsmCellLocation = (GsmCellLocation) tm.getCellLocation();
            if (gsmCellLocation != null) {
                mCellType = "" + gsmCellLocation.getCid();
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
     * Set or modify the Notification
     */
    public void setNotification() {
        Intent notificationIntent = new Intent(this, AIMSICD.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 0);

        Notification mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.iconbn)
                        .setContentTitle(this.getResources().getString(R.string.app_name))
                        .setContentText("Phone Type " + getPhoneType(false))
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

/*
 * The below code section was copied and modified from
 * Femtocatcher https://github.com/iSECPartners/femtocatcher
 *
 * Copyright (C) 2013 iSEC Partners
 */
    //TODO: Finish modification & incorporate into alert system
    public void startTracking() {

        /* Check if it is a CDMA phone */
        if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
            //tv1.setText("This application can detect a femtocell on a CDMA phone only.");
            return;
        }

        mPhoneStateListener = new PhoneStateListener() {
            public void onServiceStateChanged(ServiceState s) {
                Log.d(TAG, "Service State changed!");
                getServiceStateInfo(s);
            }
        };
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);
    }

    public void stopTracking() {
        if (mPhoneStateListener != null) {
            tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            Log.v(TAG, "stopped tracking");
        }
    }

    public void getServiceStateInfo(ServiceState s) {
        if (s != null && IsConnectedToCdmaFemto(s)) {
            //setNotification();
            //toggleRadio();
        }

    }

    private boolean IsConnectedToCdmaFemto(ServiceState s) {
        if (s == null) {
            return false;
        }

        /* Get International Roaming indicator
         * if indicator is not 0 return false
         */

        //TODO

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

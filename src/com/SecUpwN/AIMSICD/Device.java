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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import com.SecUpwN.AIMSICD.cmdprocessor.Helpers;

import java.util.ArrayList;

public class Device {

    private String TAG = "AIMSICD_Device";

    private PhoneStateListener mSignalListenerStrength;
    private PhoneStateListener mSignalListenerLocation;
    private LocationManager lm;
    private LocationListener mLocationListener;
    private AIMSICDDbAdapter dbHelper;
    private Context mContext;
    private Activity mActivity;
    public final int START_LOCATION_SERVICES = 1;

    private int mPhoneID;
    private int mSignalInfo;
    private int mNetID;
    private int mLacID;
    private int mCellID;
    private double mLongitude;
    private double mLatitude;
    private String mNetType = "", mCellInfo = "", mDataState = "";
    private String mPhoneNum = "", mCellType = "", mLac = "";
    private String mNetName = "", mMmcmcc = "", mSimCountry = "", mPhoneType = "";
    private String mIMEI = "", mIMEIV = "", mSimOperator = "", mSimOperatorName = "";
    private String mSimSerial = "", mSimSubs = "", mDataActivityType = "";

    private boolean TrackingCell;
    private boolean TrackingSignal;
    private boolean TrackingLocation;

    private ArrayList<String> alPosition;

    private TelephonyManager tm;

    Device(Context context, Activity activity) {
        mContext = context;
        mActivity = activity;
        //TelephonyManager provides system details
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

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

        //Create DB Instance
        dbHelper = new AIMSICDDbAdapter(mContext);

        mSignalListenerLocation = new PhoneStateListener() {
            public void onCellLocationChanged(CellLocation location) {
                mNetID = getNetID(true);
                mNetType = tm.getNetworkTypeName();

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
                            mCellInfo = gsmCellLocation.toString() + dataActivity + "|" + dataState + "|" + mNetType + "|";
                            mLacID = gsmCellLocation.getLac();
                            mCellID = gsmCellLocation.getCid();
                            dbHelper.open();
                            if (isTrackingCell() && !dbHelper.cellExists(mCellID)){
                                mSimCountry = getSimCountry(true);
                                mSimOperator = getSimOperator(true);
                                mSimOperatorName = getSimOperatorName(true);
                                dbHelper.insertCell(mLacID, mCellID, mNetID, mLatitude,
                                        mLongitude, mSignalInfo, mCellInfo, mSimCountry,
                                        mSimOperator, mSimOperatorName);
                            }
                        }
                        break;
                    case TelephonyManager.PHONE_TYPE_CDMA:
                        CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) location;
                        if (cdmaCellLocation != null) {
                            mCellInfo = cdmaCellLocation.toString() + dataActivity + "|" + dataState + "|" + mNetType + "|";
                            mLacID = cdmaCellLocation.getNetworkId();
                            mCellID = cdmaCellLocation.getBaseStationId();
                            if (isTrackingCell() && !dbHelper.cellExists(mCellID)){
                                mSimCountry = getSimCountry(true);
                                mSimOperator = getSimOperator(true);
                                mSimOperatorName = getNetworkName(true);
                            }
                        }
                }

                if (TrackingCell && !dbHelper.cellExists(mCellID)) {
                    dbHelper.insertCell(mLacID, mCellID, mNetID, mLatitude, mLongitude,
                            mSignalInfo, mCellInfo, mSimCountry, mSimOperator, mSimOperatorName);
                }


            }
        };

        mSignalListenerStrength = new PhoneStateListener() {
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

                if (TrackingSignal) {
                    dbHelper.insertSignal(mLacID, mCellID, mNetID, mLatitude, mLongitude,
                            mSignalInfo, mCellInfo);
                }
            }
        };

    }

    public int getPhoneID() {
        if (mPhoneID <= 0 || mPhoneID > 6)
            mPhoneID = tm.getPhoneType();

        return mPhoneID;
    }

    public String getSimCountry(boolean force) {
        if (mSimCountry.isEmpty() || force)
            mSimCountry = tm.getSimCountryIso();

        return mSimCountry;
    }

    public String getSimOperator(boolean force) {
        if (mSimOperator.isEmpty() || force)
            mSimOperator = tm.getSimOperator();

        return mSimOperator;
    }

    public String getSimOperatorName(boolean force) {
        if (mSimOperatorName.isEmpty() || force)
            mSimOperatorName = tm.getSimOperatorName();

        return mSimOperatorName;
    }

    public String getSimSubs(boolean force) {
        if (mSimSubs.isEmpty() || force)
            mSimSubs = tm.getSubscriberId();

        return mSimSubs;
    }

    public String getSimSerial(boolean force) {
        if (mSimSerial.isEmpty() || force)
            mSimSerial = tm.getSimSerialNumber();

        return mSimSerial;
    }

    public String getPhoneType(boolean force) {
        if (mPhoneType.isEmpty()|| force) {
            if (getPhoneID() == TelephonyManager.PHONE_TYPE_GSM)
                mPhoneType = "GSM";
            else if (getPhoneID() == TelephonyManager.PHONE_TYPE_CDMA)
                mPhoneType = "CDMA";
            else
                mPhoneType = "Unknown";
        }

        return mPhoneType;
    }

    public String getIMEI(boolean force) {
        if (mIMEI.isEmpty() || force)
            mIMEI = tm.getDeviceId();

        return mIMEI;
    }

    public String getIMEIv(boolean force) {
        if (mIMEIV.isEmpty() || force)
            mIMEIV = tm.getDeviceSoftwareVersion();

        return mIMEIV;
    }

    public String getPhoneNumber(boolean force) {
        if (mPhoneNum.isEmpty() || force)
            mPhoneNum = tm.getLine1Number();

        return mPhoneNum;
    }

    public String getNetworkName(boolean force) {
        if (mNetName.isEmpty() || force)
            mNetName = tm.getNetworkOperatorName();

        return mNetName;
    }

    public String getSmmcMcc(boolean force) {
        if (mMmcmcc.isEmpty() || force)
            mMmcmcc = tm.getNetworkOperator();

        return mMmcmcc;
    }

    public String getNetworkTypeName() {
        return tm.getNetworkTypeName();

    }

    public int getNetID (boolean force) {
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

    public AIMSICDDbAdapter getDbHelper() {
        return dbHelper;
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

    public void tracksignal() {
        if (TrackingSignal) {
            tm.listen(mSignalListenerStrength, PhoneStateListener.LISTEN_NONE);
            Helpers.msgShort(mContext, "Stopped tracking signal strength");
            TrackingSignal = false;
            mSignalInfo = 0;
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
            mCellInfo = "[0,0]|nn|nn|";
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
            mLongitude = 0.0;
            mLatitude = 0.0;
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
                        builder.setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Display Location Services Menu Fragment
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                mActivity.startActivityForResult(intent, START_LOCATION_SERVICES);
                            }
                        });
                        builder.setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                        builder.create().show();
                    }
                }
            }
        }
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                mLongitude = loc.getLongitude();
                mLatitude = loc.getLatitude();
            }
            if (TrackingLocation) {
                dbHelper.insertLocation(mLacID, mCellID, mNetID, mLatitude, mLongitude,
                        mSignalInfo, mCellInfo);
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

}

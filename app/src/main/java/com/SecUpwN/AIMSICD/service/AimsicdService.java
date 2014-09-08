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

/*
 * Portions of this software have been copied and modified from
 * https://github.com/illarionov/SamsungRilMulticlient
 * Copyright (C) 2014 Alexey Illarionov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.SecUpwN.AIMSICD.service;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.rilexecutor.DetectResult;
import com.SecUpwN.AIMSICD.rilexecutor.OemRilExecutor;
import com.SecUpwN.AIMSICD.rilexecutor.RawResult;
import com.SecUpwN.AIMSICD.rilexecutor.SamsungMulticlientRilExecutor;
import com.SecUpwN.AIMSICD.utils.Cell;
import com.SecUpwN.AIMSICD.utils.Device;
import com.SecUpwN.AIMSICD.utils.Helpers;
import com.SecUpwN.AIMSICD.utils.OemCommands;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.WindowManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AimsicdService extends Service implements OnSharedPreferenceChangeListener {

    private final String TAG = "AIMSICD_Service";
    public static final String SHARED_PREFERENCES_BASENAME = "com.SecUpwN.AIMSICD_preferences";
    public static final String SILENT_SMS = "SILENT_SMS_INTERCEPTED";
    public static final String UPDATE_DISPLAY = "UPDATE_DISPLAY";
    public static int PHONE_TYPE;
    public static String OCID_API_KEY = "API KEY GOES HERE.......";

    /*
     * System and helper declarations
     */
    private final AimscidBinder mBinder = new AimscidBinder();
    private final AIMSICDDbAdapter dbHelper = new AIMSICDDbAdapter(this);
    private final Handler timerHandler = new Handler();
    private Context mContext;
    private final int NOTIFICATION_ID = 1;
    private long mDbResult;
    private TelephonyManager tm;
    private LocationManager lm;
    private SharedPreferences prefs;
    private PhoneStateListener mPhoneStateListener;
    private LocationListener mLocationListener;
    private static final long GPS_MIN_UPDATE_TIME = 1000;
    private static final float GPS_MIN_UPDATE_DISTANCE = 10;
    public boolean mMultiRilCompatible;
    public static long REFRESH_RATE;
    public static int LAST_DB_BACKUP_VERSION;
    public static boolean OCID_UPLOAD_PREF;
    private boolean CELL_TABLE_CLEANSED;

    public final Device mDevice = new Device();

    /*
     * Tracking and Alert Declarations
     */
    private boolean mLoaded;
    private boolean mMonitoringCell;
    private boolean mTrackingCell;
    private boolean mTrackingFemtocell;
    private boolean mFemtoDetected;
    private boolean mLocationPrompted;
    private boolean mTypeZeroSmsDetected;
    private boolean mChangedLAC;
    private Cell mMonitorCell;

    /*
     * Samsung MultiRil Implementation
     */
    private static final int ID_REQUEST_START_SERVICE_MODE_COMMAND = 1;
    private static final int ID_REQUEST_FINISH_SERVICE_MODE_COMMAND = 2;
    private static final int ID_REQUEST_PRESS_A_KEY = 3;
    private static final int ID_REQUEST_REFRESH = 4;
    private static final int ID_RESPONSE = 101;
    private static final int ID_RESPONSE_FINISH_SERVICE_MODE_COMMAND = 102;
    private static final int ID_RESPONSE_PRESS_A_KEY = 103;
    private static final int REQUEST_TIMEOUT = 10000; // ms

    private final ConditionVariable mRequestCondvar = new ConditionVariable();
    private final Object mLastResponseLock = new Object();
    private volatile List<String> mLastResponse;
    private DetectResult mRilExecutorDetectResult;
    private OemRilExecutor mRequestExecutor;
    private HandlerThread mHandlerThread;
    private Handler mHandler;



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
        mContext = getApplicationContext();

        PHONE_TYPE = tm.getPhoneType();

        prefs = this.getSharedPreferences(
                AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
        prefs.registerOnSharedPreferenceChangeListener(this);
        loadPreferences();

        if (!CELL_TABLE_CLEANSED) {
            dbHelper.open();
            dbHelper.cleanseCellTable();
            dbHelper.close();
            Editor prefsEditor;
            prefsEditor = prefs.edit();
            prefsEditor.putBoolean(this.getString(R.string.pref_cell_table_cleansed), true);
            prefsEditor.apply();
        }

        mDevice.refreshDeviceInfo(tm, this); //Telephony Manager
        setNotification();

        mRequestExecutor = new SamsungMulticlientRilExecutor();
        mRilExecutorDetectResult = mRequestExecutor.detect();
        if (!mRilExecutorDetectResult.available) {
            mMultiRilCompatible = false;
            Log.e(TAG, "Samsung multiclient ril not available: " + mRilExecutorDetectResult.error);
            mRequestExecutor = null;
        } else {
            mRequestExecutor.start();
            mMultiRilCompatible = true;
            //Sumsung MultiRil Initialization
            mHandlerThread = new HandlerThread("ServiceModeSeqHandler");
            mHandlerThread.start();

            Looper l = mHandlerThread.getLooper();
            if (l != null) {
                mHandler = new Handler(l, new MyHandler());
            }
        }

        //Register receiver for Silent SMS Interception Notification
        mContext.registerReceiver(mMessageReceiver, new IntentFilter(SILENT_SMS));

        mMonitorCell = new Cell();

        Log.i(TAG, "Service launched successfully");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        cancelNotification();
        dbHelper.close();
        mContext.unregisterReceiver(mMessageReceiver);
        if (mTrackingCell) {
            tm.listen(mCellSignalListener, PhoneStateListener.LISTEN_NONE);
            lm.removeUpdates(mLocationListener);
        }

        //Samsung MultiRil Cleanup
        if (mRequestExecutor != null) {
            mRequestExecutor.stop();
            mRequestExecutor = null;
            mHandler = null;
            mHandlerThread.quit();
            mHandlerThread = null;
        }

        Log.i(TAG, "Service destroyed");
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Bundle bundle = intent.getExtras();
            if (bundle != null) {
                dbHelper.open();
                dbHelper.insertSilentSms(bundle);
                dbHelper.close();
                setSilentSmsStatus(true);
            }
        }
    };

    /**
     * Check the status of the Rill Executor
     *
     * @return DetectResult providing access status of the Ril Executor
     */
    public DetectResult getRilExecutorStatus() {
        return mRilExecutorDetectResult;
    }

    /**
     * Executes and receives the Ciphering Information request using
     * the Rill Executor
     *
     * @return String list response from Rill Executor
     */
    public List<String> getCipheringInfo() {
        return executeServiceModeCommand(
                OemCommands.OEM_SM_TYPE_TEST_MANUAL,
                OemCommands.OEM_SM_TYPE_SUB_CIPHERING_PROTECTION_ENTER,
                null
        );
    }

    /**
     * Executes and receives the Neighbouring Cell request using
     * the Rill Executor
     *
     * @return String list response from Rill Executor
     */
    public List<String> getNeighbours() {
        KeyStep getNeighboursKeySeq[] = new KeyStep[]{
                new KeyStep('\0', false),
                new KeyStep('1', false), // [1] DEBUG SCREEN
                new KeyStep('4', true), // [4] NEIGHBOUR CELL
        };

        return executeServiceModeCommand(
                OemCommands.OEM_SM_TYPE_TEST_MANUAL,
                OemCommands.OEM_SM_TYPE_SUB_ENTER,
                Arrays.asList(getNeighboursKeySeq)
        );

    }

    /**
     * Updates Neighbouring Cell details
     */
    public List<Cell> updateNeighbouringCells() {
        List<Cell> neighboringCells = new ArrayList<>();

        List<NeighboringCellInfo> neighboringCellInfo;
        neighboringCellInfo = tm.getNeighboringCellInfo();
        if (neighboringCellInfo.size() == 0) {
            // try to poll the neighboring cells for a few seconds
            final LinkedBlockingQueue<NeighboringCellInfo> neighboringCellBlockingQueue =
                    new LinkedBlockingQueue<>(100);
            final PhoneStateListener listener = new PhoneStateListener() {
                private void handle() {
                    List<NeighboringCellInfo> neighboringCellInfo;
                    neighboringCellInfo = tm.getNeighboringCellInfo();
                    if (neighboringCellInfo.size() == 0) {
                        return;
                    }
                    Log.i(TAG, "neighbouringCellInfo empty - event based polling succeeded!");
                    tm.listen(this, PhoneStateListener.LISTEN_NONE);
                    neighboringCellBlockingQueue.addAll(neighboringCellInfo);
                }

                @Override
                public void onServiceStateChanged(ServiceState serviceState) {
                    handle();
                }

                @Override
                public void onDataConnectionStateChanged(int state) {
                    handle();
                }

                @Override
                public void onDataConnectionStateChanged(int state, int networkType) {
                    handle();
                }

                @Override
                public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                    handle();
                }

                @Override
                public void onCellInfoChanged(List<CellInfo> cellInfo) {
                    handle();
                }
            };
            Log.i(TAG, "neighbouringCellInfo empty - start polling");

            //LISTEN_CELL_INFO added in API 17
            if (Build.VERSION.SDK_INT > 16) {
                tm.listen(listener,
                        PhoneStateListener.LISTEN_CELL_INFO
                                | PhoneStateListener.LISTEN_CELL_LOCATION |
                                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                                | PhoneStateListener.LISTEN_SERVICE_STATE |
                                PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            } else {
                tm.listen(listener,
                        PhoneStateListener.LISTEN_CELL_LOCATION |
                                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                                | PhoneStateListener.LISTEN_SERVICE_STATE |
                                PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            }

            for (int i = 0; i < 10 && neighboringCellInfo.size() == 0; i++) {
                try {
                    Log.i(TAG, "neighbouringCellInfo empty - try " + i);
                    NeighboringCellInfo info = neighboringCellBlockingQueue
                            .poll(1, TimeUnit.SECONDS);
                    if (info == null) {
                        neighboringCellInfo = tm.getNeighboringCellInfo();
                        if (neighboringCellInfo.size() > 0) {
                            Log.i(TAG, "neighbouringCellInfo empty - try " + i
                                    + " succeeded time based");
                            break;
                        } else {
                            continue;
                        }
                    }
                    ArrayList<NeighboringCellInfo> cellInfoList =
                            new ArrayList<NeighboringCellInfo>(
                                    neighboringCellBlockingQueue.size() + 1);
                    while (info != null) {
                        cellInfoList.add(info);
                        info = neighboringCellBlockingQueue.poll(1, TimeUnit.SECONDS);
                    }
                    neighboringCellInfo = cellInfoList;
                } catch (InterruptedException e) {
                    // normal
                }
            }
        }

        Log.i(TAG, "neighbouringCellInfo Size - " + neighboringCellInfo.size());
        for (NeighboringCellInfo neighbourCell : neighboringCellInfo) {
            Log.i(TAG, "neighbouringCellInfo - CID:" + neighbourCell.getCid() +
                    " LAC:" + neighbourCell.getLac() + " RSSI:" + neighbourCell.getRssi() +
                    " PSC:" + neighbourCell.getPsc());

            final Cell cell = new Cell(neighbourCell.getCid(), neighbourCell.getLac(),
                    neighbourCell.getRssi(), neighbourCell.getPsc(),
                    neighbourCell.getNetworkType(), false);
            neighboringCells.add(cell);
        }

        return neighboringCells;
    }

    /**
     * Service Mode Command Helper to call with Timeout value
     *
     * @return executeServiceModeCommand adding REQUEST_TIMEOUT
     */
    private List<String> executeServiceModeCommand(int type, int subtype,
            java.util.Collection<KeyStep> keySeqence) {
        return executeServiceModeCommand(type, subtype, keySeqence, REQUEST_TIMEOUT);
    }

    /**
     * Service Mode Command Helper to call with Timeout value
     *
     * @return executeServiceModeCommand adding REQUEST_TIMEOUT
     */
    private synchronized List<String> executeServiceModeCommand(int type, int subtype,
            java.util.Collection<KeyStep> keySeqence, int timeout) {
        if (mRequestExecutor == null) {
            return Collections.emptyList();
        }

        mRequestCondvar.close();
        mHandler.obtainMessage(ID_REQUEST_START_SERVICE_MODE_COMMAND,
                type,
                subtype,
                keySeqence).sendToTarget();
        if (!mRequestCondvar.block(timeout)) {
            Log.e(TAG, "request timeout");
            return Collections.emptyList();
        } else {
            synchronized (mLastResponseLock) {
                return mLastResponse;
            }
        }
    }

    private static class KeyStep {

        public final char keychar;

        public final boolean captureResponse;

        public KeyStep(char keychar, boolean captureResponse) {
            this.keychar = keychar;
            this.captureResponse = captureResponse;
        }

        public static final KeyStep KEY_START_SERVICE_MODE = new KeyStep('\0', true);
    }

    private class MyHandler implements Handler.Callback {

        private int mCurrentType;
        private int mCurrentSubtype;
        private Queue<KeyStep> mKeySequence;

        @Override
        public boolean handleMessage(Message msg) {
            byte[] requestData;
            Message responseMsg;
            KeyStep lastKeyStep;

            switch (msg.what) {
                case ID_REQUEST_START_SERVICE_MODE_COMMAND:
                    mCurrentType = msg.arg1;
                    mCurrentSubtype = msg.arg2;
                    mKeySequence = new ArrayDeque<>(3);
                    if (msg.obj != null) {
                        mKeySequence.addAll((java.util.Collection<KeyStep>) msg.obj);
                    } else {
                        mKeySequence.add(KeyStep.KEY_START_SERVICE_MODE);
                    }
                    synchronized (mLastResponseLock) {
                        mLastResponse = new ArrayList<>();
                    }
                    requestData = OemCommands.getEnterServiceModeData(
                            mCurrentType, mCurrentSubtype, OemCommands.OEM_SM_ACTION);
                    responseMsg = mHandler.obtainMessage(ID_RESPONSE);
                    mRequestExecutor.invokeOemRilRequestRaw(requestData, responseMsg);
                    break;
                case ID_REQUEST_FINISH_SERVICE_MODE_COMMAND:
                    requestData = OemCommands.getEndServiceModeData(mCurrentType);
                    responseMsg = mHandler.obtainMessage(ID_RESPONSE_FINISH_SERVICE_MODE_COMMAND);
                    mRequestExecutor.invokeOemRilRequestRaw(requestData, responseMsg);
                    break;
                case ID_REQUEST_PRESS_A_KEY:
                    requestData = OemCommands.getPressKeyData(msg.arg1, OemCommands.OEM_SM_ACTION);
                    responseMsg = mHandler.obtainMessage(ID_RESPONSE_PRESS_A_KEY);
                    mRequestExecutor.invokeOemRilRequestRaw(requestData, responseMsg);
                    break;
                case ID_REQUEST_REFRESH:
                    requestData = OemCommands.getPressKeyData('\0', OemCommands.OEM_SM_QUERY);
                    responseMsg = mHandler.obtainMessage(ID_RESPONSE);
                    mRequestExecutor.invokeOemRilRequestRaw(requestData, responseMsg);
                    break;
                case ID_RESPONSE:
                    lastKeyStep = mKeySequence.poll();
                    try {
                        RawResult result = (RawResult) msg.obj;
                        if (result == null) {
                            Log.e(TAG, "result is null");
                            break;
                        }
                        if (result.exception != null) {
                            Log.e(TAG, "", result.exception);
                            break;
                        }
                        if (result.result == null) {
                            Log.v(TAG, "No need to refresh.");
                            break;
                        }
                        if (lastKeyStep.captureResponse) {
                            synchronized (mLastResponseLock) {
                                mLastResponse
                                        .addAll(Helpers.unpackByteListOfStrings(result.result));
                            }
                        }
                    } finally {
                        if (mKeySequence.isEmpty()) {
                            mHandler.obtainMessage(ID_REQUEST_FINISH_SERVICE_MODE_COMMAND)
                                    .sendToTarget();
                        } else {
                            mHandler.obtainMessage(ID_REQUEST_PRESS_A_KEY,
                                    mKeySequence.element().keychar, 0).sendToTarget();
                        }
                    }
                    break;
                case ID_RESPONSE_PRESS_A_KEY:
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(ID_REQUEST_REFRESH), 10);
                    break;
                case ID_RESPONSE_FINISH_SERVICE_MODE_COMMAND:
                    mRequestCondvar.open();
                    break;

            }
            return true;
        }
    }

    public void refreshDevice() {
        mDevice.refreshDeviceInfo(tm, this);
    }

    /**
     * Process User Preferences
     */
    private void loadPreferences() {
        boolean trackFemtoPref = prefs.getBoolean(
                this.getString(R.string.pref_femto_detection_key), false);

        boolean trackCellPref = prefs.getBoolean(
                this.getString(R.string.pref_enable_cell_key), false);

        LAST_DB_BACKUP_VERSION = prefs.getInt(
                this.getString(R.string.pref_last_database_backup_version), 1);

        OCID_UPLOAD_PREF = prefs.getBoolean(
                this.getString(R.string.pref_ocid_upload), false);

        CELL_TABLE_CLEANSED = prefs.getBoolean(this.getString(R.string.pref_cell_table_cleansed),
                false);

        String refreshRate = prefs.getString(getString(R.string.pref_refresh_key), "1");
        if (refreshRate.isEmpty()) {
            refreshRate = "1";
        }

        int rate = Integer.parseInt(refreshRate);
        long t;
        switch (rate) {
            case 1:
                t = 15L;
                break;
            default:
                t = (rate * 1L);
                break;
        }
        REFRESH_RATE = TimeUnit.SECONDS.toMillis(t);

        if (trackFemtoPref) {
            startTrackingFemto();
        }

        if (trackCellPref) {
            setCellTracking(true);
        }
    }

    /**
     * Tracking Cell Information
     *
     * @return boolean indicating Cell Information Tracking State
     */
    public boolean isTrackingCell() {
        return mTrackingCell;
    }

    /**
     * Tracking Femotcell Connections
     *
     * @return boolean indicating Femtocell Connection Tracking State
     */
    public boolean isTrackingFemtocell() {
        return mTrackingFemtocell;
    }

    void setSilentSmsStatus(boolean state) {
        mTypeZeroSmsDetected = state;
        setNotification();
        if (state) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.sms_message)
                    .setTitle(R.string.sms_title);
            AlertDialog alert = builder.create();
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alert.show();
            mTypeZeroSmsDetected = false;
        }
    }

    /**
     * Set or update the Notification
     */
    public void setNotification() {

        String tickerText;
        String contentText = "Phone Type " + mDevice.getPhoneType();

        String iconType = prefs.getString(
                this.getString(R.string.pref_ui_icons_key), "sense");

        int status;

        if (mFemtoDetected || mTypeZeroSmsDetected) {
            status = 4; //ALARM
        } else if (mChangedLAC) {
            status = 3; //MEDIUM
            contentText = "Hostile Service Area: Changing LAC Detected";
        } else if (mTrackingFemtocell || mTrackingCell || mLoaded) {
            status = 2; //NORMAL
            if (mTrackingFemtocell) {
                contentText = "FemtoCell Detection Active";
            } else if (mTrackingCell) {
                contentText = "Cell Tracking Active";
            }
        } else {
            status = 1; //IDLE
        }

        int icon = R.drawable.sense_idle;

        switch (status) {
            case 1: //IDLE
                switch (iconType) {
                    case "flat":
                        icon = R.drawable.flat_idle;
                        break;
                    case "sense":
                        icon = R.drawable.sense_idle;
                        break;
                    case "white":
                        icon = R.drawable.white_idle;
                        break;
                }
                tickerText = getResources().getString(R.string.app_name_short)
                        + " - Status: Idle";
                break;
            case 2: //NORMAL
                switch (iconType) {
                    case "flat":
                        icon = R.drawable.flat_ok;
                        break;
                    case "sense":
                        icon = R.drawable.sense_ok;
                        break;
                    case "white":
                        icon = R.drawable.white_ok;
                        break;
                }
                tickerText = getResources().getString(R.string.app_name_short)
                        + " - Status: Good No Threats Detected";
                break;
            case 3: //MEDIUM
                switch (iconType) {
                    case "flat":
                        icon = R.drawable.flat_medium;
                        break;
                    case "sense":
                        icon = R.drawable.sense_medium;
                        break;
                    case "white":
                        icon = R.drawable.white_medium;
                        break;
                }
                tickerText = getResources().getString(R.string.app_name_short)
                        + " - Hostile Service Area: Changing LAC Detected";
                break;
            case 4: //DANGER
                switch (iconType) {
                    case "flat":
                        icon = R.drawable.flat_danger;
                        break;
                    case "sense":
                        icon = R.drawable.sense_danger;
                        break;
                    case "white":
                        icon = R.drawable.white_danger;
                        break;
                }
                tickerText = getResources().getString(R.string.app_name_short)
                        + " - ALERT!! Threat Detected";
                if (mFemtoDetected) {
                    contentText = "ALERT!! FemtoCell Connection Threat Detected";
                } else if (mTypeZeroSmsDetected) {
                    contentText = "ALERT!! Type Zero Silent SMS Intercepted";
                }

                break;
            default:
                icon = R.drawable.sense_idle;
                tickerText = getResources().getString(R.string.app_name);
                break;
        }

        Intent notificationIntent = new Intent(mContext, AIMSICD.class);
        notificationIntent.putExtra("silent_sms", mTypeZeroSmsDetected);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_FROM_BACKGROUND);
        PendingIntent contentIntent = PendingIntent.getActivity(
                mContext, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
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
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder);
    }

    /**
     * Cancel and remove the persistent notification
     */
    private void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(
                NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    public Location lastKnownLocation() {
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null || (location.getLatitude() == 0.0 && location.getLongitude() == 0.0)) {
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location == null || (location.getLatitude() == 0.0 && location.getLongitude() == 0.0)) {

            }
        }

        return location;
    }

    public void setLoaded() {
        mLoaded = true;
        setNotification();
    }

    public boolean isMonitoringCell() {
        return mMonitoringCell;
    }

    private LocationListener cellMonitorListener = new MyLocationListener();

    /**
     * Cell Information Monitoring
     *
     * @param monitor Enable/Disable monitoring
     */
    public void setCellMonitoring(boolean monitor) {
        if (monitor) {
            timerHandler.postDelayed(timerRunnable, 0);
            mMonitoringCell = true;
            Helpers.msgShort(this, "Monitoring cell information");
        } else {
            timerHandler.removeCallbacks(timerRunnable);
            mMonitoringCell = false;
            Helpers.msgShort(this, "Stopped monitoring cell information");
        }
    }

    private final Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            switch (mDevice.getPhoneID()) {
                case TelephonyManager.PHONE_TYPE_GSM:
                    GsmCellLocation gsmCellLocation = (GsmCellLocation) tm.getCellLocation();
                    if (gsmCellLocation != null) {
                        mMonitorCell.setLAC(gsmCellLocation.getLac());
                        mMonitorCell.setCID(gsmCellLocation.getCid());
                        dbHelper.open();
                        boolean lacOK = dbHelper.checkLAC(mMonitorCell);
                        if (!lacOK) {
                            mChangedLAC = true;
                            setNotification();
                        }
                        dbHelper.close();
                    }
                    break;
                case TelephonyManager.PHONE_TYPE_CDMA:
                    CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) tm.getCellLocation();
                    if (cdmaCellLocation != null) {
                        mMonitorCell.setLAC(cdmaCellLocation.getNetworkId());
                        mMonitorCell.setCID(cdmaCellLocation.getBaseStationId());
                        dbHelper.open();
                        boolean lacOK = dbHelper.checkLAC(mMonitorCell);
                        if (!lacOK) {
                            mChangedLAC = true;
                            setNotification();
                        }
                        dbHelper.close();
                    }
            }
            if (REFRESH_RATE != 0) {
                timerHandler.postDelayed(this, REFRESH_RATE);
            } else {
                //Default to 25 seconds refresh rate
                timerHandler.postDelayed(this,TimeUnit.SECONDS.toMillis(25) );
            }
        }
    };

    /**
     * Cell Information Tracking and database logging
     *
     * @param track Enable/Disable tracking
     */
    public void setCellTracking(boolean track) {
        if (track) {
            tm.listen(mCellSignalListener,
                    PhoneStateListener.LISTEN_CELL_LOCATION |
                            PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |
                            PhoneStateListener.LISTEN_DATA_ACTIVITY |
                            PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
            );
            if (lm != null) {
                mLocationListener = new MyLocationListener();
                Log.i(TAG, "LocationManager already existed");
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_UPDATE_TIME,
                        GPS_MIN_UPDATE_DISTANCE, mLocationListener);
            } else {
                Log.i(TAG, "LocationManager did not exist");
                lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                if (lm != null) {
                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Log.i(TAG, "LocationManager created");
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_UPDATE_TIME,
                                GPS_MIN_UPDATE_DISTANCE, mLocationListener);
                    }
                }
            }
            Helpers.msgShort(this, "Tracking cell information");
            mTrackingCell = true;
        } else {
            tm.listen(mCellSignalListener, PhoneStateListener.LISTEN_NONE);
            lm.removeUpdates(mLocationListener);
            mDevice.mCell.setLon(0.0);
            mDevice.mCell.setLat(0.0);
            mTrackingCell = false;
            mDevice.setCellInfo("[0,0]|nn|nn|");
            Helpers.msgShort(this, "Stopped tracking cell information");
        }
        setNotification();
    }

    private final PhoneStateListener mCellSignalListener = new PhoneStateListener() {
        public void onCellLocationChanged(CellLocation location) {
            mDevice.setNetID(tm);
            mDevice.getNetworkTypeName();

            switch (mDevice.getPhoneID()) {
                case TelephonyManager.PHONE_TYPE_GSM:
                    GsmCellLocation gsmCellLocation = (GsmCellLocation) location;
                    if (gsmCellLocation != null) {
                        mDevice.setCellInfo(gsmCellLocation.toString() + mDevice.getDataActivityTypeShort() + "|"
                                + mDevice.getDataStateShort() + "|" + mDevice.getNetworkTypeName() + "|");
                        mDevice.mCell.setLAC(gsmCellLocation.getLac());
                        mDevice.mCell.setCID(gsmCellLocation.getCid());
                        if (gsmCellLocation.getPsc() != -1)
                            mDevice.mCell.setPSC(gsmCellLocation.getPsc());

                    }
                    break;
                case TelephonyManager.PHONE_TYPE_CDMA:
                    CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) location;
                    if (cdmaCellLocation != null) {
                        mDevice.setCellInfo(
                                cdmaCellLocation.toString() + mDevice.getDataActivityTypeShort()
                                        + "|" + mDevice.getDataStateShort() + "|" + mDevice
                                        .getNetworkTypeName() + "|");
                        mDevice.mCell.setLAC(cdmaCellLocation.getNetworkId());
                        mDevice.mCell.setCID(cdmaCellLocation.getBaseStationId());
                        mDevice.mCell.setSID(cdmaCellLocation.getSystemId());
                        mDevice.mCell.setMNC(cdmaCellLocation.getSystemId());
                        mDevice.setNetworkName(tm.getNetworkOperatorName());
                    }
            }

        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            //Update Signal Strength
            if (signalStrength.isGsm()) {
                mDevice.setSignalDbm(signalStrength.getGsmSignalStrength());
            } else {
                int evdoDbm = signalStrength.getEvdoDbm();
                int cdmaDbm = signalStrength.getCdmaDbm();

                //Use lowest signal to be conservative
                mDevice.setSignalDbm((cdmaDbm < evdoDbm) ? cdmaDbm : evdoDbm);
            }
        }

        public void onDataActivity(int direction) {
            switch (direction) {
                case TelephonyManager.DATA_ACTIVITY_NONE:
                    mDevice.setDataActivityTypeShort("No");
                    mDevice.setDataActivityType("None");
                    break;
                case TelephonyManager.DATA_ACTIVITY_IN:
                    mDevice.setDataActivityTypeShort("In");
                    mDevice.setDataActivityType("In");
                    break;
                case TelephonyManager.DATA_ACTIVITY_OUT:
                    mDevice.setDataActivityTypeShort("Ou");
                    mDevice.setDataActivityType("Out");
                    break;
                case TelephonyManager.DATA_ACTIVITY_INOUT:
                    mDevice.setDataActivityTypeShort("IO");
                    mDevice.setDataActivityType("In-Out");
                    break;
                case TelephonyManager.DATA_ACTIVITY_DORMANT:
                    mDevice.setDataActivityTypeShort("Do");
                    mDevice.setDataActivityType("Dormant");
                    break;
            }
        }

        public void onDataConnectionStateChanged(int state) {
            switch (state) {
                case TelephonyManager.DATA_DISCONNECTED:
                    mDevice.setDataState("Disconnected");
                    mDevice.setDataStateShort("Di");
                    break;
                case TelephonyManager.DATA_CONNECTING:
                    mDevice.setDataState("Connecting");
                    mDevice.setDataStateShort("Ct");
                    break;
                case TelephonyManager.DATA_CONNECTED:
                    mDevice.setDataState("Connected");
                    mDevice.setDataStateShort("Cd");
                    break;
                case TelephonyManager.DATA_SUSPENDED:
                    mDevice.setDataState("Suspended");
                    mDevice.setDataStateShort("Su");
                    break;
            }
        }

    };

    private void enableLocationServices() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.location_error_message)
                .setTitle(R.string.location_error_title)
                .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent gpsSettings = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        gpsSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(gpsSettings);
                    }
                })
                .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setCellTracking(false);
                    }
                });
        AlertDialog alert = builder.create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.show();
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            if (mDevice.isBetterLocation(loc, mDevice.getLastLocation())) {
                if (Build.VERSION.SDK_INT > 16) {
                    List<CellInfo> cellinfolist = tm.getAllCellInfo();
                    if (cellinfolist != null) {
                        for (final CellInfo cellinfo : cellinfolist) {
                            if (cellinfo instanceof CellInfoGsm) {
                                final CellSignalStrengthGsm signalStrengthGsm = ((CellInfoGsm) cellinfo)
                                        .getCellSignalStrength();
                                final CellIdentityGsm identityGsm = ((CellInfoGsm) cellinfo)
                                        .getCellIdentity();
                                if (identityGsm != null) {
                                    mDevice.mCell.setCID(identityGsm.getCid());
                                    mDevice.mCell.setLAC(identityGsm.getLac());
                                    mDevice.mCell.setMCC(identityGsm.getMcc());
                                    mDevice.mCell.setMNC(identityGsm.getMnc());
                                }
                                if (signalStrengthGsm != null) {
                                    mDevice.mCell.setDBM(signalStrengthGsm.getDbm());
                                }
                                break;
                            } else if (cellinfo instanceof CellInfoCdma) {
                                final CellSignalStrengthCdma signalStrengthCdma = ((CellInfoCdma) cellinfo)
                                        .getCellSignalStrength();
                                final CellIdentityCdma identityCdma = ((CellInfoCdma) cellinfo)
                                        .getCellIdentity();
                                if (identityCdma != null) {
                                    mDevice.mCell.setCID(identityCdma.getBasestationId());
                                    mDevice.mCell.setLAC(identityCdma.getNetworkId());
                                    mDevice.mCell.setMNC(identityCdma.getSystemId());
                                    mDevice.mCell.setSID(identityCdma.getSystemId());
                                }

                                if (signalStrengthCdma != null) {
                                    mDevice.setSignalDbm(signalStrengthCdma.getDbm());
                                }
                                break;
                            } else if (cellinfo instanceof CellInfoLte) {
                                final CellSignalStrengthLte signalStrengthLte = ((CellInfoLte) cellinfo)
                                        .getCellSignalStrength();
                                final CellIdentityLte identityLte = ((CellInfoLte) cellinfo)
                                        .getCellIdentity();

                                if (identityLte != null) {
                                    mDevice.mCell.setCID(identityLte.getPci());
                                    mDevice.mCell.setLAC(identityLte.getTac());
                                    mDevice.mCell.setMCC(identityLte.getMcc());
                                    mDevice.mCell.setMNC(identityLte.getMnc());
                                }

                                if (signalStrengthLte != null) {
                                    mDevice.setSignalDbm(signalStrengthLte.getDbm());
                                }
                                break;
                            } else if (cellinfo instanceof CellInfoWcdma) {
                                final CellSignalStrengthWcdma signalStrengthWcdma = ((CellInfoWcdma) cellinfo)
                                        .getCellSignalStrength();
                                final CellIdentityWcdma identityWcdma = ((CellInfoWcdma) cellinfo)
                                        .getCellIdentity();
                                if (identityWcdma != null) {
                                    mDevice.mCell.setCID(identityWcdma.getCid());
                                    mDevice.mCell.setLAC(identityWcdma.getLac());
                                    mDevice.mCell.setMCC(identityWcdma.getMcc());
                                    mDevice.mCell.setMNC(identityWcdma.getMnc());
                                }

                                if (signalStrengthWcdma != null) {
                                    mDevice.setSignalDbm(signalStrengthWcdma.getDbm());
                                }
                                break;
                            }
                        }
                    }
                }

                if (!mDevice.mCell.isValid()) {
                    CellLocation cellLocation = tm.getCellLocation();
                    if (cellLocation != null) {
                        switch (mDevice.getPhoneID()) {
                            case TelephonyManager.PHONE_TYPE_GSM:
                                GsmCellLocation gsmCellLocation
                                        = (GsmCellLocation) cellLocation;
                                mDevice.mCell.setCID(gsmCellLocation.getCid());
                                mDevice.mCell.setLAC(gsmCellLocation.getLac());
                                mDevice.mCell.setPSC(gsmCellLocation.getPsc());
                                break;
                            case TelephonyManager.PHONE_TYPE_CDMA:
                                CdmaCellLocation cdmaCellLocation
                                        = (CdmaCellLocation) cellLocation;
                                mDevice.mCell.setCID(cdmaCellLocation.getBaseStationId());
                                mDevice.mCell.setLAC(cdmaCellLocation.getNetworkId());
                                mDevice.mCell.setSID(cdmaCellLocation.getSystemId());
                                mDevice.mCell.setMNC(cdmaCellLocation.getSystemId());
                        }
                    }
                }

                if (loc != null && (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0)) {
                    mDevice.mCell.setLon(loc.getLongitude());
                    mDevice.mCell.setLat(loc.getLatitude());
                    mDevice.mCell.setSpeed(loc.getSpeed());
                    mDevice.mCell.setAccuracy(loc.getAccuracy());
                    mDevice.mCell.setBearing(loc.getBearing());
                    mDevice.setLastLocation(loc);

                    if (mTrackingCell ) {
                        dbHelper.open();
                        mDbResult = dbHelper.insertLocation(mDevice.mCell.getLAC(),
                                mDevice.mCell.getCID(), mDevice.mCell.getNetType(), mDevice.mCell.getLat(),
                                mDevice.mCell.getLon(), mDevice.mCell.getDBM(),
                                mDevice.getCellInfo());

                        mDbResult = dbHelper.insertCell(mDevice.mCell.getLAC(), mDevice.mCell.getCID(),
                                mDevice.mCell.getNetType(), mDevice.mCell.getLat(),
                                mDevice.mCell.getLon(), mDevice.mCell.getDBM(),
                                mDevice.mCell.getMCC(), mDevice.mCell.getMNC(),
                                mDevice.mCell.getAccuracy(), mDevice.mCell.getSpeed(), mDevice.mCell.getBearing(),
                                mDevice.getNetworkTypeName(), SystemClock.currentThreadTimeMillis());

                        if (mDbResult == -1) {
                            Log.e(TAG, "Error writing to database");
                        }
                    }
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            if (!mLocationPrompted) {
                mLocationPrompted = true;
                enableLocationServices();
            }
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



    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final String KEY_UI_ICONS = this.getString(R.string.pref_ui_icons_key);
        final String FEMTO_DECTECTION = this
                .getString(R.string.pref_femto_detection_key);
        final String REFRESH = this.getString(R.string.pref_refresh_key);
        final String DB_VERSION = this.getString(R.string.pref_last_database_backup_version);
        final String OCID_UPLOAD = this.getString(R.string.pref_ocid_upload);

        if (key.equals(KEY_UI_ICONS)) {
            //Update Notification to display selected icon type
            setNotification();
        } else if (key.equals(FEMTO_DECTECTION)) {
            boolean trackFemtoPref = sharedPreferences.getBoolean(FEMTO_DECTECTION, false);
            if (trackFemtoPref) {
                startTrackingFemto();
            } else {
                stopTrackingFemto();
            }
        } else if (key.equals(REFRESH)) {
            String refreshRate = sharedPreferences.getString(REFRESH, "1");
            if (refreshRate.isEmpty()) {
                refreshRate = "1";
            }

            int rate = Integer.parseInt(refreshRate);
            long t;
            switch (rate) {
                case 1:
                    t = 15L;
                    break;
                default:
                    t = (rate * 1L);
                    break;
            }
            REFRESH_RATE = TimeUnit.SECONDS.toMillis(t);
        } else if (key.equals(DB_VERSION)) {
            LAST_DB_BACKUP_VERSION = sharedPreferences.getInt(DB_VERSION, 1);
        } else if (key.equals(OCID_UPLOAD)) {
            OCID_UPLOAD_PREF = sharedPreferences.getBoolean(OCID_UPLOAD, false);
        }
    }

    /*
     * The below code section was copied and modified from
     * Femtocatcher https://github.com/iSECPartners/femtocatcher
     *
     * Copyright (C) 2013 iSEC Partners
     */

    /**
     * Start FemtoCell detection tracking
     * CDMA Devices ONLY
     */
    public void startTrackingFemto() {

        /* Check if it is a CDMA phone */
        if (mDevice.getPhoneID() != TelephonyManager.PHONE_TYPE_CDMA) {
            Helpers.msgShort(this, "AIMSICD can only detect Femtocell connections on CDMA devices.");
            return;
        }

        mTrackingFemtocell = true;
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

    /**
     * Stop FemtoCell detection tracking
     * CDMA Devices ONLY
     */
    public void stopTrackingFemto() {
        if (mPhoneStateListener != null) {
            tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            mTrackingFemtocell = false;
            setNotification();
            Log.v(TAG, "Stopped tracking Femtocell connections");
        }
    }

    private void getServiceStateInfo(ServiceState s) {
        if (s != null) {
            if (IsConnectedToCdmaFemto(s)) {
                Helpers.msgShort(this, "ALERT!! Femtocell Connection Detected!!");
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
        int networkType = mDevice.mCell.getNetType();

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

    /**
     * Confirmation of connection to an EVDO Network
     *
     * @return EVDO network connection returns TRUE
     */
    private boolean isEvDoNetwork(int networkType) {
        return (networkType == TelephonyManager.NETWORK_TYPE_EVDO_0) ||
                (networkType == TelephonyManager.NETWORK_TYPE_EVDO_A) ||
                (networkType == TelephonyManager.NETWORK_TYPE_EVDO_B) ||
                (networkType == TelephonyManager.NETWORK_TYPE_EHRPD);
    }
    /*
     * The above code section was copied and modified from
     * Femtocatcher https://github.com/iSECPartners/femtocatcher
     *
     * Copyright (C) 2013 iSEC Partners
     */

}

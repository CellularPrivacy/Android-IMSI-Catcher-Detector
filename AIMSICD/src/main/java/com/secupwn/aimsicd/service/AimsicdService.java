/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
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

package com.secupwn.aimsicd.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.rilexecutor.RilExecutor;
import com.secupwn.aimsicd.smsdetection.SmsDetector;
import com.secupwn.aimsicd.utils.Cell;
import com.secupwn.aimsicd.utils.GeoLocation;

import io.freefair.android.injection.annotation.Inject;
import io.freefair.android.injection.app.InjectionService;
import io.freefair.android.util.logging.Logger;

/**
 * This starts the (background?) AIMSICD service to check for SMS and track
 * cells with or without GPS enabled.
 */
public class AimsicdService extends InjectionService {

    public static boolean isGPSchoiceChecked;
    public static final String GPS_REMEMBER_CHOICE = "remember choice";
    SharedPreferences gpsPreferences;

    @Inject
    private Logger log;

    // /data/data/com.SecUpwN.AIMSICD/shared_prefs/com.SecUpwN.AIMSICD_preferences.xml
    public static final String SHARED_PREFERENCES_BASENAME = "com.SecUpwN.AIMSICD_preferences";
    public static final String UPDATE_DISPLAY = "UPDATE_DISPLAY";

    /*
     * System and helper declarations
     */
    private final AimscidBinder mBinder = new AimscidBinder();
    private static final Handler timerHandler = new Handler();

    private CellTracker mCellTracker;
    private AccelerometerMonitor mAccelerometerMonitor;
    private SignalStrengthTracker signalStrengthTracker;
    private LocationTracker mLocationTracker;
    private RilExecutor mRilExecutor;
    private SmsDetector smsdetector;

    private boolean isLocationRequestShowing = false;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class AimscidBinder extends Binder {

        public AimsicdService getService() {
            return AimsicdService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.AppTheme);


        signalStrengthTracker = new SignalStrengthTracker(getBaseContext());

        mAccelerometerMonitor = new AccelerometerMonitor(this, new Runnable() {
            @Override
            public void run() {
                // movement detected, so enable GPS

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        timerHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mLocationTracker.start();
                            }
                        });
                    }
                };
                new Thread(runnable).start();

                signalStrengthTracker.onSensorChanged();

                // check again in a while to see if GPS should be disabled
                // this runnable also re-enables this movement sensor
                timerHandler.postDelayed(batterySavingRunnable, AccelerometerMonitor.MOVEMENT_THRESHOLD_MS);
            }
        });

        mLocationTracker = new LocationTracker(this, mLocationListener);
        mRilExecutor = new RilExecutor(this);
        mCellTracker = new CellTracker(this, signalStrengthTracker);

        log.info("Service launched successfully.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        gpsPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isGPSchoiceChecked = gpsPreferences.getBoolean(GPS_REMEMBER_CHOICE, false);
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCellTracker.stop();
        mLocationTracker.stop();
        mAccelerometerMonitor.stop();
        mRilExecutor.stop();

        if (SmsDetector.getSmsDetectionState()) {
            smsdetector.stopSmsDetection();
        }
        log.info("Service destroyed.");
    }

    public GeoLocation lastKnownLocation() {
        return mLocationTracker.lastKnownLocation();
    }

    public RilExecutor getRilExecutor() {
        return mRilExecutor;
    }

    public CellTracker getCellTracker() {
        return mCellTracker;
    }

    public Cell getCell() {
        return mCellTracker.getDevice().cell;
    }

    public void setCell(Cell cell) {
        mCellTracker.getDevice().cell = cell;
    }

    public boolean isTrackingCell() {
        return mCellTracker.isTrackingCell();
    }

    public boolean isMonitoringCell() {
        return mCellTracker.isMonitoringCell();
    }

    public void setCellMonitoring(boolean monitor) {
        mCellTracker.setCellMonitoring(monitor);
    }

    public boolean isTrackingFemtocell() {
        return mCellTracker.isTrackingFemtocell();
    }

    public void setTrackingFemtocell(boolean track) {
        if (track) {
            mCellTracker.startTrackingFemto();
        } else {
            mCellTracker.stopTrackingFemto();
        }
    }

    // SMS Detection Thread
    public boolean isSmsTracking() {
        return SmsDetector.getSmsDetectionState();
    }

    public void startSmsTracking() {
        if (!isSmsTracking()) {
            log.info("Sms Detection Thread Started");
            smsdetector = new SmsDetector(this);
            smsdetector.startSmsDetection();
        }
    }

    public void stopSmsTracking() {
        if (isSmsTracking()) {
            smsdetector.stopSmsDetection();
            log.info("Sms Detection Thread Stopped");
        }
    }

    // while tracking a cell, manage the power usage by switching off GPS if no movement
    private final Runnable batterySavingRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCellTracker.isTrackingCell()) {
                // if no movement in a while, shut off GPS. Gets re-enabled when there is movement
                if (mAccelerometerMonitor.notMovedInAWhile() ||
                        mLocationTracker.notMovedInAWhile()) {
                    mLocationTracker.stop();
                }
                mAccelerometerMonitor.start();
            }
        }
    };

    /**
     * Cell Information Tracking and database logging
     *
     * @param track Enable/Disable tracking
     */
    public void setCellTracking(boolean track) {
        mCellTracker.setCellTracking(track);

        if (track) {
            mLocationTracker.start();
            mAccelerometerMonitor.start();
        } else {
            mLocationTracker.stop();
            mAccelerometerMonitor.stop();
        }
    }

    public void checkLocationServices() {
        if (mCellTracker.isTrackingCell() && !mLocationTracker.isGPSOn() && !isGPSchoiceChecked) {
            enableLocationServices();
        }
    }

    private void enableLocationServices() {
        if (isLocationRequestShowing) {
            return; // only show dialog once
        }

        LayoutInflater dialogInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = dialogInflater.inflate(R.layout.dialog_request_gps, null, false);
        CheckBox rememberChoice = (CheckBox) dialogView.findViewById(R.id.check_choice);
        Button notNow = (Button) dialogView.findViewById(R.id.not_now_button);
        Button enableGPS = (Button) dialogView.findViewById(R.id.enable_gps_button);

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                /*.setMessage(R.string.location_error_message)
                .setTitle(R.string.location_error_title)*/
                .setView(dialogView)
                .setCancelable(false)
                /*.setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        isLocationRequestShowing = false;
                        Intent gpsSettings = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        gpsSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(gpsSettings);
                    }
                })
                .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        isLocationRequestShowing = false;
                        setCellTracking(false);
                    }
                })*/
                .create();

        rememberChoice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    isGPSchoiceChecked = true;
                    gpsPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = gpsPreferences.edit();
                    editor.putBoolean(GPS_REMEMBER_CHOICE, isGPSchoiceChecked);
                    editor.apply();

                } else {
                    isGPSchoiceChecked = false;
                }
            }
        });

        notNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isLocationRequestShowing = false;
                setCellTracking(false);
                alertDialog.cancel();
                alertDialog.dismiss();
            }
        });

        enableGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isLocationRequestShowing = false;
                Intent gpsSettings = new Intent(
                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                gpsSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                alertDialog.cancel();
                alertDialog.dismiss();
                startActivity(gpsSettings);
            }
        });

        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
        isLocationRequestShowing = true;
    }

    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location loc) {
            timerHandler.postDelayed(batterySavingRunnable, AccelerometerMonitor.MOVEMENT_THRESHOLD_MS);
            mCellTracker.onLocationChanged(loc);
        }

        @Override
        public void onProviderDisabled(String provider) {
            if (mCellTracker.isTrackingCell() && provider.equals(LocationManager.GPS_PROVIDER) &&
                    !isGPSchoiceChecked) {
                enableLocationServices();
            }
        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }
    };
}

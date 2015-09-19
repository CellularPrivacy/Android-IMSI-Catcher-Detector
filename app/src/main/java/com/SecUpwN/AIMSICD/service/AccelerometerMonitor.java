/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.service;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Class to handle monitoring the Accelerometer to enable/disable GPS for battery saving
 */
public class AccelerometerMonitor {
    // How long with no movement detected, before we assume we are not moving
    static final long MOVEMENT_THRESHOLD_MS = 20*1000;
    static final float ACCELEROMETER_NOISE = 2.0f; // amount of sensor noise to ignore

    private long lastMovementTime = 0;
    private float mLastX, mLastY, mLastZ;
    private boolean mInitialized;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private SensorEventListener mSensorListener;
    private Runnable onMovement;

    public AccelerometerMonitor(Context context, Runnable onMovement) {
        setupAccelerometer(context);
        this.onMovement = onMovement;
    }

    /**
     * Set up the accelerometer so that when movement is detected, the GPS is enabled.
     * GPS is normally disabled to save battery power.
     */
    // TODO:
    // E:V:A  We might need to loop this once and wait a few seconds, to prevent
    //        GPS from starting by accidental vibrations.
    private void setupAccelerometer(Context context) {
        // set up accelerometer sensor
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                if (!mInitialized) {
                    mInitialized = true;
                    mLastX = x;
                    mLastY = y;
                    mLastZ = z;
                } else {
                    float deltaX = Math.abs(mLastX - x);
                    float deltaY = Math.abs(mLastY - y);
                    float deltaZ = Math.abs(mLastZ - z);

                    if (deltaX < ACCELEROMETER_NOISE) deltaX = 0.0f;
                    if (deltaY < ACCELEROMETER_NOISE) deltaY = 0.0f;
                    if (deltaZ < ACCELEROMETER_NOISE) deltaZ = 0.0f;

                    mLastX = x;
                    mLastY = y;
                    mLastZ = z;

                    if (deltaX > 0 || deltaY > 0 || deltaZ > 0) {
                        // movement detected
                        // disable the movement sensor to save power
                        stop();

                        lastMovementTime = System.currentTimeMillis();

                        if (onMovement != null) {
                            Thread runThread = new Thread(onMovement);
                            runThread.start();
                        }
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        start();
    }

    public void start() {
        mSensorManager.registerListener(mSensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop() {
        mSensorManager.unregisterListener(mSensorListener);
    }

    public boolean notMovedInAWhile() {
        return System.currentTimeMillis() - lastMovementTime >= MOVEMENT_THRESHOLD_MS;
    }
}

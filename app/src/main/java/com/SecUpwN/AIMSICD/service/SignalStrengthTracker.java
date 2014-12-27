package com.SecUpwN.AIMSICD.service;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * Class that calculates cell signal strength averages and decides if a given cell + strength
 * appears to be mysteriously (low or high)
 *
 * https://cloud.githubusercontent.com/assets/2507905/4428863/c85c8366-45d4-11e4-89da-c650cdb56caf.jpg
 *
 * @author Tor Henning Ueland
 */
public class SignalStrengthTracker implements SensorEventListener {

    public static final String TAG = "SignalStrengthMonitor";
    private static int sleepTimeBetweenSignalRegistration = 2; //seconds
    private static int sleepTimeBetweenCalculations = 30; //seconds
    private static int minimumNumberOfSamplesNeeded = 10;
    private static int minimumIdleTime              = 10; //seconds

    private HashMap<Integer, Long> lastRegistration = new HashMap<>();
    private HashMap<Integer, ArrayList<Integer>> toCalculate = new HashMap<>();
    private long lastCalculationTime = 0;
    private long lastMovementDetected = 0l;

    /**
     * Registers a new cell signal strength for future calculation,
     * only values older than $sleepTimeBetweenSignalRegistration seconds
     * since last registration is saved for processing.
     * @param cellID
     * @param signalStrength
     */
    public void registerSignalStrength(int cellID, int signalStrength) {

        if(deviceIsMoving()) {
            Log.i(TAG, "Ignored signal strength sample for cell ID #"+cellID+" as the device is currently moving around.");
        }

        long now = System.currentTimeMillis();
        if(!lastRegistration.containsKey(cellID) || now-(sleepTimeBetweenSignalRegistration*1000) > lastRegistration.get(cellID)) {
            long diff = now - lastRegistration.get(cellID);
            lastRegistration.put(cellID, now);
            if(toCalculate.get(cellID) == null) {
                toCalculate.put(cellID, new ArrayList<Integer>(1));
            }
            Log.i(TAG, "Scheduling signal strength calculation from cell #" + cellID + "@" + signalStrength + "DB, last registration was "+diff+"ms ago");
            toCalculate.get(cellID).add(signalStrength);
        }

        if(now-(sleepTimeBetweenCalculations*1000) > lastCalculationTime) {
            Log.i(TAG, "Calculating cell signal averages, last calculation was "+(now-lastCalculationTime)+"ms ago");
        }
    }

    private boolean deviceIsMoving() {
        return System.currentTimeMillis() - lastMovementDetected > minimumIdleTime*1000;
    }

    /**
     * Uses previously saved calculations and signal measurements to guesstimate if a given signal
     * strength for a given cell ID looks mysterious or not. This requires enough samples for the
     * given cel ID to give any sane result and will return false if not.
     * @param cellID
     * @param signalStrength
     */
    public boolean isMysterious(int cellID, int signalStrength) {

        //If moving, return false
        if(deviceIsMoving()) {
            Log.i(TAG, "Cannot check if the signal strength for cell ID #"+cellID+" as the device is currently moving around.");
        }
        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.equals(Sensor.TYPE_ACCELEROMETER)) {
            lastMovementDetected = System.currentTimeMillis();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}

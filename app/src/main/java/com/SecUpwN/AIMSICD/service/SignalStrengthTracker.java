package com.SecUpwN.AIMSICD.service;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;

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
public class SignalStrengthTracker {

    public static final String TAG = "SignalStrengthMonitor";
    private static int sleepTimeBetweenSignalRegistration = 10; //seconds
    private static int sleepTimeBetweenPersistation = 60; //seconds
    private static int minimumNumberOfSamplesNeeded = 10;
    private static int minimumIdleTime              = 60; //seconds
    private static int maximumNumberOfDaysSaved     = 90; //days
    private static int mysteriousSignalDifference   = 10; //DB

    private HashMap<Integer, Long> lastRegistration = new HashMap<>();
    private HashMap<Integer, ArrayList<Integer>> toCalculate = new HashMap<>();
    private HashMap<Integer, Integer> averageSignalCache = new HashMap<>();
    private long lastPersistTime = 0;
    private long lastMovementDetected = 0l;
    private AIMSICDDbAdapter mDbHelper;

    public SignalStrengthTracker(Context context) {
        lastMovementDetected = System.currentTimeMillis();
        lastPersistTime = System.currentTimeMillis();
        mDbHelper = new AIMSICDDbAdapter(context);
    }

    /**
     * Registers a new cell signal strength for future calculation,
     * only values older than $sleepTimeBetweenSignalRegistration seconds
     * since last registration is saved for processing.
     * @param cellID
     * @param signalStrength
     */
    public void registerSignalStrength(int cellID, int signalStrength) {

        long now = System.currentTimeMillis();

        if(deviceIsMoving()) {
            Log.i(TAG, "Ignored signal strength sample for cell ID #"+cellID+" as the device is currently moving around, will not accept anything for another "+((minimumIdleTime*1000) - (now - lastMovementDetected))+"ms");
            return;
        }

        if(!lastRegistration.containsKey(cellID) || now-(sleepTimeBetweenSignalRegistration*1000) > lastRegistration.get(cellID)) {
            long diff = -1;
            if(lastRegistration.get(cellID) != null) {
                diff = now - lastRegistration.get(cellID);
            }
            lastRegistration.put(cellID, now);
            if(toCalculate.get(cellID) == null) {
                toCalculate.put(cellID, new ArrayList<Integer>(1));
            }
            Log.i(TAG, "Scheduling signal strength calculation from cell #" + cellID + " @ " + signalStrength + "DB, last registration was "+diff+"ms ago");
            toCalculate.get(cellID).add(signalStrength);
            lastRegistration.put(cellID, now);
        }

        if(now-(sleepTimeBetweenPersistation*1000) > lastPersistTime) {
            Log.i(TAG, "Saving cell signal data, last save was "+(now-lastPersistTime)+"ms ago");
            cleanupOldData();
            persistData();
            lastPersistTime = now;
        }
    }

    private void persistData() {
        for(int cellID : toCalculate.keySet()) {
            for(int signal : toCalculate.get(cellID)) {
                mDbHelper.addSignalStrength(cellID, signal, lastRegistration.get(cellID));
            }
        }
        toCalculate.clear();
    }

    private void cleanupOldData() {
        long maxTime = (System.currentTimeMillis() - ((maximumNumberOfDaysSaved*86400))*1000);
        mDbHelper.cleanseCellStrengthTables(maxTime);
        averageSignalCache.clear();
    }

    private boolean deviceIsMoving() {
        return System.currentTimeMillis() - lastMovementDetected < minimumIdleTime*1000;
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
            return false;
        }

        //Do we have enough samples?
        int samplesForCell = mDbHelper.countSignalMeasurements(cellID);
        if(samplesForCell < minimumNumberOfSamplesNeeded) {
            return false;
        }

        int storedAvg = mDbHelper.getAverageSignalStrength(cellID);
        if(storedAvg > signalStrength) {
            return storedAvg - signalStrength > mysteriousSignalDifference;
        } else {
            return signalStrength-  storedAvg > mysteriousSignalDifference;
        }
    }

    public void onSensorChanged() {
        //Log.d(TAG, "We are moving...");
        lastMovementDetected = System.currentTimeMillis();
    }
}

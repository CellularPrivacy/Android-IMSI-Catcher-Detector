package com.SecUpwN.AIMSICD.service;

import android.content.Context;
import android.util.Log;

import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *  Description:    Class that calculates cell signal strength averages and decides if a
 *                  given cell + strength appears to be mysteriously (low or high).
 *                  Signal strengths are shown in units of:
 *
 *          See:    http://wiki.opencellid.org/wiki/API#Filtering_of_data
 *
 *      GSM     RSSI in dBm in the range of [-51 to -113] or ASU in the range of [0 to 31]
 *      UMTS    RSCP in dBm in the range of [-25 to -121] or ASU in the range of [-5 to 91]
 *      LTE     RSRP in dBm in the range of [-45 to -137] or ASU in the range of [0 to 95]
 *      CDMA    RSSI in dBm in the range of [-75 to -100] or ASU in the range of [1 to 16]
 *
 *      https://cloud.githubusercontent.com/assets/2507905/4428863/c85c8366-45d4-11e4-89da-c650cdb56caf.jpg
 *
 * @author Tor Henning Ueland
 */
public class SignalStrengthTracker {
    //FIXME The logging tag can be at most 23 characters, was 29 (AIMSICD_SignalStrengthTracker)
    public static final String TAG = "AIMSICD_SignalStrength";
    private static int sleepTimeBetweenSignalRegistration = 60; // [seconds]
    private static int minimumIdleTime              = 30; // [seconds]
    private static int maximumNumberOfDaysSaved     = 60; // [days] = 2 months
    private static int mysteriousSignalDifference   = 10; // [dBm] or [ASU]?
    private static int sleepTimeBetweenCleanup      = 3600; // Once per hour
    private Long lastRegistrationTime;  //Timestamp for last registration to DB
    private Long lastCleanupTime;       //Timestamp for last cleanup of DB
    private HashMap<Integer, Integer> averageSignalCache = new HashMap<>();
    private long lastMovementDetected = 0l;
    private AIMSICDDbAdapter mDbHelper;

    public SignalStrengthTracker(Context context) {
        lastMovementDetected = System.currentTimeMillis();
        lastRegistrationTime = System.currentTimeMillis();
        lastCleanupTime      = System.currentTimeMillis();
        mDbHelper = new AIMSICDDbAdapter(context);
    }

    /**
     * Registers a new cell signal strength for future calculation,
     * only values older than $sleepTimeBetweenSignalRegistration seconds
     * since last registration is saved for processing.
     *
     * @param cellID
     * @param signalStrength
     */
    public void registerSignalStrength(int cellID, int signalStrength) {

        long now = System.currentTimeMillis();

        if(deviceIsMoving()) {
            Log.i(TAG, "Ignored signal strength sample for CID: " + cellID +
                    " as the device is currently moving around, will not accept anything for another " +
                    ((minimumIdleTime*1000) - (now - lastMovementDetected)) + " ms.");
            return;
        }

        if( now - (sleepTimeBetweenSignalRegistration*1000) > lastRegistrationTime) {
            long diff = now - lastRegistrationTime;
            Log.i(TAG, "Scheduling signal strength calculation from CID: " + cellID +
                    " @ " + signalStrength + " dBm. Last registration was " + diff + "ms ago.");
            lastRegistrationTime = now;

            mDbHelper.open();
            mDbHelper.addSignalStrength(cellID, signalStrength, now);
            mDbHelper.close();
        }

        if(now-(sleepTimeBetweenCleanup*1000) > lastCleanupTime) {
            Log.i(TAG, "Removing old signal strength entries");
            cleanupOldData();
        }
    }

    /**
     *  Remove Signal Strength data from DB, that is older than N days:
     *  (days * number of seconds in a day) * seconds to milliseconds
     */
    private void cleanupOldData() {
        long maxTime = (System.currentTimeMillis() - ((maximumNumberOfDaysSaved*86400))*1000);
        mDbHelper.open();
        mDbHelper.cleanseCellStrengthTables(maxTime);
        mDbHelper.close();
        averageSignalCache.clear();
    }

    private boolean deviceIsMoving() {
        return System.currentTimeMillis() - lastMovementDetected < minimumIdleTime*1000;
    }

    /**
     * Uses previously saved calculations and signal measurements to guesstimate if a given signal
     * strength for a given cell ID looks mysterious or not.
     *
     * @param cellID
     * @param signalStrength
     */
    public boolean isMysterious(int cellID, int signalStrength) {

        //If moving, return false
        if(deviceIsMoving()) {
            Log.i(TAG, "Cannot check signal strength for CID: " + cellID +
                        " as the device is currently moving around.");
            return false;
        }

        int storedAvg;

        //Cached?
        if(averageSignalCache.get(cellID) != null) {
            storedAvg = averageSignalCache.get(cellID);
            Log.d(TAG, "Cached average SS for CID: " + cellID + " is: " + storedAvg);
        } else {
            //Not cached, check DB
            mDbHelper.open();
            storedAvg = mDbHelper.getAverageSignalStrength(cellID);
            averageSignalCache.put(cellID, storedAvg);
            Log.d(TAG, "Average SS in DB for  CID: " + cellID + " is: " + storedAvg);
            mDbHelper.close();
        }

        boolean result;
        if(storedAvg > signalStrength) {
            result = storedAvg - signalStrength > mysteriousSignalDifference;
        } else {
            result = signalStrength - storedAvg > mysteriousSignalDifference;
        }
        Log.d(TAG, "Signal Strength mystery check for CID: " + cellID +
                " is " + result + ", avg:" + storedAvg + ", this signal: " + signalStrength);
        return result;
    }

    public void onSensorChanged() {
        //Log.d(TAG, "We are moving...");
        lastMovementDetected = System.currentTimeMillis();
    }
}

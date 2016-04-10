/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.service;

import android.content.Context;

import com.secupwn.aimsicd.utils.RealmHelper;

import java.util.HashMap;

import io.freefair.android.util.logging.AndroidLogger;
import io.freefair.android.util.logging.Logger;
import io.realm.Realm;
import lombok.Cleanup;

/**
 *  Description:    Class that calculates cell signal strength averages and decides if a
 *                  given cell + strength appears to be mysteriously (low or high).
 *                  Signal strengths are shown in units of:
 *
 *          See:    http://wiki.opencellid.org/wiki/API#Filtering_of_data
 *
 *          GSM     RSSI in dBm in the range of [-51 to -113] or ASU in the range of [0 to 31]
 *          UMTS    RSCP in dBm in the range of [-25 to -121] or ASU in the range of [-5 to 91]
 *          LTE     RSRP in dBm in the range of [-45 to -137] or ASU in the range of [0 to 95]
 *          CDMA    RSSI in dBm in the range of [-75 to -100] or ASU in the range of [1 to 16]
 *
 *          Detection Flowchart:
 *          https://cloud.githubusercontent.com/assets/2507905/4428863/c85c8366-45d4-11e4-89da-c650cdb56caf.jpg
 *
 *
 *  Dependency:
 *              AIMSICDDbAdapter:   getAverageSignalStrength etc..
 *
 *  Issues:
 *
 *      [ ] Need to add RAT detection for each signal, as to above. Since we could have different
 *          signal due to different RAT, for the same cell (LAC/CID). (@He3556 please confirm.)
 *          This means that the SQL query will be a little more complicated.
 * @author Tor Henning Ueland
 */
public class SignalStrengthTracker {

    private final Logger log = AndroidLogger.forClass(SignalStrengthTracker.class);

    private static int sleepTimeBetweenSignalRegistration = 60; // [seconds]
    private static int minimumIdleTime              = 30;       // [seconds]
    private static int maximumNumberOfDaysSaved     = 60;       // [days] = 2 months
    private static int mysteriousSignalDifference   = 10;       // [dBm] or [ASU]?
    private static int sleepTimeBetweenCleanup      = 3600;     // [seconds] Once per hour

    private Long lastRegistrationTime;  // Timestamp for last registration to DB
    private Long lastCleanupTime;       // Timestamp for last cleanup of DB
    private HashMap<Integer, Integer> averageSignalCache = new HashMap<>();
    private long lastMovementDetected = 0l; // ??
    private RealmHelper mDbHelper;

    public SignalStrengthTracker(Context context) {
        lastMovementDetected = System.currentTimeMillis();
        lastRegistrationTime = System.currentTimeMillis();
        lastCleanupTime      = System.currentTimeMillis();
        mDbHelper = new RealmHelper(context);
    }

    /**
     * Registers a new cell signal strength for future calculation, only values older
     * than $sleepTimeBetweenSignalRegistration seconds since last registration, is
     * saved for processing.
     *
     * @param cellID
     * @param signalStrength
     */
    public void registerSignalStrength(int cellID, int signalStrength) {

        // Returns the current time in milliseconds since January 1, 1970 00:00:00.0 UTC.
        //   "This method shouldn't be used for measuring timeouts or other elapsed time
        //   measurements, as changing the system time can affect the results.
        //   Use nanoTime() for that."
        // TODO: We probably need to convert this into seconds for easy use in DB
        long now = System.currentTimeMillis(); // [ms]

        if (deviceIsMoving()) {
            log.info("Ignored signal sample for CID: " + cellID +
                    " due to device movement. Waiting for " + ((minimumIdleTime * 1000) - (now - lastMovementDetected)) + " ms.");
            return;
        }

        if (now - (sleepTimeBetweenSignalRegistration * 1000) > lastRegistrationTime) {
            long diff = now - lastRegistrationTime;
            log.info("Scheduling signal strength calculation from CID: " + cellID +
                    " @ " + signalStrength + " dBm. Last registration was " + diff + "ms ago.");
            lastRegistrationTime = now;

            //mDbHelper.addSignalStrength(cellID, signalStrength, String.valueOf(System.currentTimeMillis()));

        }

        if (now - (sleepTimeBetweenCleanup * 1000) > lastCleanupTime) {
            log.info("Removing old signal strength entries from DB.");

            // cleanupOldData();//
            // TODO cleanupOldData() need to change query as now time is a string value
            // String query = String.format("DELETE FROM DBi_measure WHERE time < %d", maxTime);
        }
    }

    /**
     *  Remove Signal Strength data from DB, that is older than N days:
     *  (days * number of seconds in a day) * seconds to milliseconds
     */
    private void cleanupOldData() {
        long maxTime = (System.currentTimeMillis() - ((maximumNumberOfDaysSaved * 86400)) * 1000); // [ms] Number of days
        //TODO
        //mDbHelper.cleanseCellStrengthTables(maxTime);
        averageSignalCache.clear();
    }

    private boolean deviceIsMoving() {
        // Is device moving?
        return System.currentTimeMillis() - lastMovementDetected < minimumIdleTime * 1000; // [ms]
    }

    /**
     * Uses previously saved calculations and signal measurements to guesstimate if a given signal
     * strength for a given cell ID looks mysterious or not.
     *
     * @param cellID
     * @param signalStrength
     */
    public boolean isMysterious(int cellID, int signalStrength) {

        // If moving, return false
        if (deviceIsMoving()) {
            log.info("Cannot check signal strength for CID: " + cellID + " because of device movements.");
            return false;
        }

        int storedAvg;

        // Cached?
        if (averageSignalCache.get(cellID) != null) {
            storedAvg = averageSignalCache.get(cellID);
            log.debug("Cached average SS for CID: " + cellID + " is: " + storedAvg);
        } else {
            // Not cached, check DB
            @Cleanup Realm realm = Realm.getDefaultInstance();
            storedAvg = mDbHelper.getAverageSignalStrength(realm, cellID);
            averageSignalCache.put(cellID, storedAvg);
            log.debug("Average SS in DB for  CID: " + cellID + " is: " + storedAvg);
        }

        boolean result;
        if (storedAvg > signalStrength) {
            result = storedAvg - signalStrength > mysteriousSignalDifference;
        } else {
            result = signalStrength - storedAvg > mysteriousSignalDifference;
        }
        log.debug("Signal Strength mystery check for CID: " + cellID +
                " is " + result + ", avg:" + storedAvg + ", this signal: " + signalStrength);
        return result;
    }

    public void onSensorChanged() {
        //log.debug("We are moving...");
        lastMovementDetected = System.currentTimeMillis();
    }
}

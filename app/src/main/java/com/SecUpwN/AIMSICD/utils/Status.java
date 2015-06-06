/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Class that sets, holds and returns current system status
 *
 * @author Tor Henning Ueland
 */
public class Status {
    // TODO: Seem we're missing the other colors here: ORANGE and BLACK (skull)
    // See: https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Status-Icons
    // Change names from "IDLE,NORMAL,MEDIUM,ALARM" to:"GRAY,GREEN,YELLOW,ORANGE,RED,BLACK",
    // to reflect detection Icon colors.
    // Dependencies:  Status.java, CellTracker.java, Icon.java ( + others?)
    // They should be based on the detection scores here: <TBA>
    // -- E:V:A 2015-01-19
    public static Type currentStatus;
    public enum Type {
        ALARM, // Which is this?
        // RUN, // BLACK
        // DANGEROUS, // RED
        // HIGH, // ORANGE
        MEDIUM, // YELLOW
        NORMAL, // GREEN
        IDLE, // GREY
    }

    /*
     * Changes the current status, this will also trigger a local broadcast event
     * if the new status is different from the previous one
     */
    public static void setCurrentStatus(Type t, Context context) {
        if(t != currentStatus) {
            Intent intent = new Intent("StatusChange");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
        currentStatus = t;
    }

    /*
     * Returns the current status
     */
    public static Type getStatus() {
        if(currentStatus == null) {
            return Type.IDLE;
        }
        return currentStatus;
    }
}

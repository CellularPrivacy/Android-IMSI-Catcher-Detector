/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
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
    private static Type currentStatus;

    public enum Type {
        ALARM(3), // Which is this?
        // RUN, // BLACK
        // DANGEROUS, // RED
        // HIGH, // ORANGE
        MEDIUM(2), // YELLOW
        NORMAL(1), // GREEN
        IDLE(0); // GREY

        // Added for sake of deciding if current level is
        // higher or lower than threshold
        public final int level;
        Type(int level){
            this.level = level;
        }
    }

    /*
     * Changes the current status, this will also trigger a local broadcast event
     * if the new status is different from the previous one
     */
    public static void setCurrentStatus(Type t, Context context, boolean vibrate, int minVibrateLevel) {
        if(t == null) {
            t = Type.IDLE;
        }
        if(t != currentStatus) {
            Intent intent = new Intent("StatusChange");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

            if(vibrate && t.level >= minVibrateLevel) {
                ((Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);
            }
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

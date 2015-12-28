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
    // See: https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Status-Icons
    // Dependencies:  Status.java, CellTracker.java, Icon.java ( + others?)
    // They should be based on the detection scores here: <TBA>
    // -- E:V:A 2015-01-19
    private static Type currentStatus;

    public enum Type {
        IDLE(0), // GREY
        OK(1), // GREEN
        MEDIUM(2), // YELLOW
        HIGH(3), // ORANGE
        DANGER(4), // RED
        SKULL(5); // BLACK

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

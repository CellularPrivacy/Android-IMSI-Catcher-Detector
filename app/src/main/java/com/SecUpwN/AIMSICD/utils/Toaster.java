/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.Toast;

public class Toaster {

    private static final int SHORT_TOAST_DURATION = 2000;
    private static final long TOAST_DURATION_MILLS = 6000; //change if need longer
    private static Toast toast;

    // Private constructor. Prevents instantiation from other classes.
    private Toaster() { }
 
    /**
     * Initializes singleton.
     *
     * ToasterHolder is loaded on the first execution of Toaster.getInstance()
     * or the first access to ToasterHolder.INSTANCE, not before.
     */
    private static class ToasterHolder {
        private static final Toaster INSTANCE = new Toaster();
    }
 
    public static Toaster getInstance() {
        return ToasterHolder.INSTANCE;
    }

    /**
     * Long toast message
     * TOAST_DURATION_MILLS controls the duration
     * currently set to 6 seconds
     * @param context Application Context
     * @param msg     Message to send
     */
    public static void msgLong(final Context context, final String msg) {
        if (context != null && msg != null) {
            if (toast!=null){
                toast.cancel();
            }

            new Handler(context.getMainLooper()).post(new Runnable() {
                @SuppressLint("ShowToast")
                @Override
                public void run() {
                    toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
                    new CountDownTimer(Math.max(TOAST_DURATION_MILLS - SHORT_TOAST_DURATION, 1000), 1000) {
                        @Override
                        public void onFinish() {
                            toast.show();
                        }

                        @Override
                        public void onTick(long millisUntilFinished) {
                            toast.show();
                        }
                    }.start();
                }
            });
        }
    }
    /**
     * Short toast message
     * (Predefined in AOS to 2000 ms = 2 sec)
     *
     * @param context Application Context
     * @param msg     Message to send
     */
    public static void msgShort(final Context context, final String msg) {
        if (context != null && msg != null) {
            if (toast!=null){
                toast.cancel();
            }
            new Handler(context.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    toast = Toast.makeText(context, msg.trim(), Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }
    }
}
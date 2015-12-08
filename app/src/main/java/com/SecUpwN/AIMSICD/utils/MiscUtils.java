/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import io.freefair.android.util.logging.AndroidLogger;
import io.freefair.android.util.logging.Logger;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Description:     TODO
 *
 *
 * ChangeLog:
 *
 *      banjaxbanjo     20150304    First PR
 *      E:V:A           20150704    Changed TAGs and fixed some formatting
 *
 */
public class MiscUtils {

    private static final Logger log = AndroidLogger.forClass(MiscUtils.class);

    public static String setAssetsString(Context context){
        BufferedReader reader = null;
        StringBuilder buildassets = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open("CREDITS")));
            String rline = reader.readLine().replace("'","\\'").replace("\\n","");

            while (rline != null ) {
                buildassets.append(rline).append("\n");
                rline = reader.readLine().replace("'","\\'").replace("\\n","");
            }
        } catch (Exception ee) {
            log.error(ee.getMessage());
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (Exception ee) {
                    log.error(ee.getMessage());
                }
            }
        }

        return buildassets.toString();
    }

    public static String getCurrentTimeStamp(){
        //yyyyMMddHHmmss <-- this format is needed for OCID upload
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
    }

    /*
      Call This function from any activity to set notification:
      Example:
                          MiscUtils.showNotification(getApplicationContext(),
                          getResources().getString(R.string.app_name_short),
                          getResources().getString(R.string.app_name_short)+" - "+getResources().getString(R.string.status_good)                            ,
                          R.drawable.sense_ok,false);
   */
    public static void showNotification(Context context ,String tickertext,String contentText, int drawable_id,boolean auto_cancel){
        int NOTIFICATION_ID = 1;

        Intent notificationIntent = new Intent(context, AIMSICD.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_FROM_BACKGROUND);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), drawable_id);
        Notification mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(drawable_id)
                        .setLargeIcon(largeIcon)
                        .setTicker(tickertext)
                        .setContentTitle(context.getResources().getString(R.string.main_app_name))
                        .setContentText(contentText)
                        .setOngoing(true)
                        .setAutoCancel(auto_cancel)
                        .setContentIntent(contentIntent)
                        .build();
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder);
    }


    /*
     *  Description:    Converts logcat timstamp to SQL friendly timstamps
     *                  We use this to determine if an sms has already been found
     *
     *      Converts a timstamp in this format:     06-17 22:06:05.988 D/dalvikvm(24747):
     *      Returns a timestamp in this format:     20150617223311
     */
    public static String logcatTimeStampParser(String line){
        String[] buffer = line.split(" ");

        line = String.valueOf(Calendar.getInstance().get(Calendar.YEAR)) + buffer[0] + buffer[1];
        //   We don't need the last 4 digits in timestamp ".988" or it is too accurate.
        return line.substring(0, line.length() - 4)
                .replace(":", "")
                .replace(".", "")
                .replace("-", "");
    }
}

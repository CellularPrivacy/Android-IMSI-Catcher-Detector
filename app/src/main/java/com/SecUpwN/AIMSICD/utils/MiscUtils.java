package com.SecUpwN.AIMSICD.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.activities.CustomPopUp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Paul Kinsella on 04/03/15.
 * paulkinsella29@yahoo.ie
 */

public class MiscUtils {

    public static String setAssetsString(Context context){
        BufferedReader reader = null;
        StringBuilder buildassets = new StringBuilder();
        try{
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open("CREDITS")));
            String rline = reader.readLine().replace("'","\\'").replace("\\n","");

            while (rline != null ){
                buildassets.append(rline).append("\n");
                rline = reader.readLine().replace("'","\\'").replace("\\n","");
            }
        } catch (Exception ee){
            ee.printStackTrace();
        }finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (Exception ee){
                    ee.printStackTrace();
                }
            }
        }

        return buildassets.toString();
    }

    public static void startPopUpInfo(Context context,int mode){
        Intent i = new Intent(context, CustomPopUp.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("display_mode",mode);
        context.startActivity(i);
    }

    public static String getCurrentTimeStamp(){

        Date now = new Date();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(now);
        return timestamp;
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
        Notification mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(drawable_id)
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
}

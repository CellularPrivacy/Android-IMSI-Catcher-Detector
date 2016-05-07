/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.ui.activities.MainActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiscUtils {

    /*
      Call This function from any activity to set notification:
      Example:
                          MiscUtils.showNotification(getApplicationContext(),
                          getResources().getString(R.string.app_name_short),
                          getResources().getString(R.string.app_name_short)+" - "+getResources().getString(R.string.status_good)                            ,
                          R.drawable.sense_ok,false);
   */
    public static void showNotification(Context context, String tickertext, String contentText, @DrawableRes int drawable_id, boolean auto_cancel) {
        int NOTIFICATION_ID = 1;

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_FROM_BACKGROUND);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), drawable_id);
        Notification notification =
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
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification);
    }

    public static final Pattern LOGCAT_TIMESTAMP_PATTERN = Pattern.compile("^(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2}).(\\d{3})");

    public static Date parseLogcatTimeStamp(String line) {
        Matcher matcher = LOGCAT_TIMESTAMP_PATTERN.matcher(line);

        if (matcher.find()) {
            int month = Integer.valueOf(matcher.group(1));
            int day = Integer.valueOf(matcher.group(2));

            int hour = Integer.valueOf(matcher.group(3));
            int minute = Integer.valueOf(matcher.group(4));
            int second = Integer.valueOf(matcher.group(5));
            int ms = Integer.valueOf(matcher.group(6));

            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DAY_OF_MONTH, day);

            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, second);
            calendar.set(Calendar.MILLISECOND, ms);

            return calendar.getTime();
        } else {
            throw new IllegalArgumentException("Invalid Line");
        }
    }
}

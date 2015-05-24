/*
*
@author Copyright Paul Kinsella paulkinsella29@yahoo.ie

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    */

package com.SecUpwN.AIMSICD.smsdetection;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.utils.Device;
import com.SecUpwN.AIMSICD.utils.MiscUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Copyright Paul Kinsella paulkinsella29@yahoo.ie on 19/04/15.
 *
 *
 * For this to work better Samsung users will have to
 * set debug level to high in SysDump menu *#8080# or *#*#8080#*#*
 *
 * To Use:
 *    SmsDetector smsdetector = new SmsDetector(context);
 *
 *    smsdetector.startSmsDetection();
 *    smsdetector.stopSmsDetection();
 *
 *    This is by no means a complete detection method but gives us something to work off.
 *
 *    TODO if you feel like this class needs improvement feel free to make a PR.
 *
 *    TODO @SecUpwn make a list of phones that this method works on with feedback from users.
 *
 *    PHONE:Samsung S5      MODEL:SM-G900F      ANDROID_VER:4.4.2   TYPE0:YES SILENTVOICE:YES
 *    PHONE:Sony Xperia J   MODEL:ST260i        ANDROID_VER:4.1.2   TYPE0:NO  SILENTVOICE:YES
 *
 */
public class SmsDetector extends Thread {
    final static String TAG = "SmsDetector";
    private DataInputStream dis;
    private DataOutputStream dos;
    private AimsicdService mAimsicdService;
    private SharedPreferences prefs;
    private boolean mBound;
    SmsDetectionDbAccess dbacess;
    static Context tContext;

    String[] SILENT_ONLY_TAGS;
    String XPERIA_J_INDICATOR = "QCRIL_RPC#POSSIBLE TYPE 0 DETECTED",
    /*Experimental code to check for SMS_ACKNOWLEDGE true in logcat and sms true in Broadcast Receiver*/
    CHECK_BROADCAST_REC = " SMS_ACKNOWLEDGE true #UNKNOWN_SILENT_SMS_DETECTED";

    /* this array holds known values to get the senders number and sms data */
    String DETECTION_PHONENUM_SMS_DATA[]={
            "SMS originating address:",
            "SMS message body (raw):",
            "OrigAddr"};


    public static boolean isrunning = false;
    public static boolean getSmsDetectionState() {
        return isrunning;
    }

    public static void setSmsDetectionState(boolean isrunning) {
        SmsDetector.isrunning = isrunning;
    }

    public SmsDetector(Context newcontext){
        tContext = newcontext;
        dbacess =  new SmsDetectionDbAccess(newcontext);

        dbacess.open();
        ArrayList<AdvanceUserItems> silent_string = dbacess.getDetectionStrings();
        dbacess.close();
        SILENT_ONLY_TAGS = new String[silent_string.size()];
        for(int x = 0;x <silent_string.size();x++){
        SILENT_ONLY_TAGS[x] = silent_string.get(x).getDetection_string()+"#"+silent_string.get(x).getDetection_type();
        prefs = newcontext.getSharedPreferences(AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
        }
    }

    public void startSmsDetection(){
        Intent intent = new Intent(tContext, AimsicdService.class);
        tContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            start();
            Log.i(TAG, "sms detection started");
   }

    public void stopSmsDetection(){

        setSmsDetectionState(false);
        // Unbind from the service
        if (mBound) {
            tContext.unbindService(mConnection);
            mBound = false;
        }
        Log.i(TAG, "sms detection stopped");
    }
    @Override
    public void run() {

        setSmsDetectionState(true);

        try {

            try {
                new Thread().sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                String MODE = "logcat -b radio\n";// default
                Runtime r = Runtime.getRuntime();
                Process process = r.exec("su");
                dos = new DataOutputStream(process.getOutputStream());

                dos.writeBytes(MODE);
                dos.flush();

                dis = new DataInputStream(process.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (getSmsDetectionState()) {
                try {


                    int bufferlen = dis.available();
                    //System.out.println("DEBUG>> Buff Len " +bufferlen);

                    if (bufferlen != 0) {
                        byte[] b = new byte[bufferlen];
                        dis.read(b);

                        String split[] = new String(b).split("\n");
                        checkForSilentSms(split);

                    } else {

                        Thread.sleep(1000);
                    }

                } catch (IOException e) {
                    if (e.getMessage() != null)
                        System.out.println(e.getMessage());
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }


    public void checkForSilentSms(String[] progress){

        for(int arrayindex = 0;arrayindex < SILENT_ONLY_TAGS.length;arrayindex++)
        {
            int MAX_INT = progress.length -4;
            for(int x =0;x <progress.length;x++) {//check all progress buffer for strings

                if(progress[x].length() < 250){//check only short strings for faster processing
                     /*

                    CHECK THAT THE BROADCAST RECEIVER PICKED UP AN SMS.
                    future code
                     */
                 /*   if (progress[x].contains(CHECK_BROADCAST_REC.split("#")[0])) {
                        try {
                            Thread.sleep(2000);
                            if (!TextMessageReceiver.isIS_MESSAGE_RECEIVED()) {
                                Toast.makeText(mContext, CHECK_BROADCAST_REC.split("#")[1], Toast.LENGTH_LONG).show();
                                break;
                            }
                        } catch (Exception err) {

                        }
                    }*/


                    /*
                    THE ONLY INDICATION OF A TYPE 0 ON SONY XPERIA J IS 4 OF THIS STRING [QCRIL_RPC]
                    RIGHT AFTER EACH OTHER.
                    Code is to buggy to many QCRIL_RPC strings
                    Disabled for now because its to unstable
                     */
/*                    if (x <= MAX_INT) {
                        if ((progress[x].contains(XPERIA_J_INDICATOR.split("#")[0]) && progress[x].length() < 11) &&
                                (progress[x + 1].contains(XPERIA_J_INDICATOR.split("#")[0])&& progress[x].length() < 11) &&
                                (progress[x + 2].contains(XPERIA_J_INDICATOR.split("#")[0])&& progress[x].length() < 11) &&
                                (progress[x + 3].contains(XPERIA_J_INDICATOR.split("#")[0])&& progress[x].length() < 11)) {
                            //Toast.makeText(mContext, XPERIA_J_INDICATOR.split("#")[1], Toast.LENGTH_LONG).show();
                            break;
                        }
                    }
*/
                    if (progress[x].contains(SILENT_ONLY_TAGS[arrayindex].split("#")[0])) {

                        System.out.println("Detected>>>>"+SILENT_ONLY_TAGS[arrayindex].split("#")[1]);
                        if(SILENT_ONLY_TAGS[arrayindex].split("#")[1].equals("TYPE0")){

                            CapturedSmsData setmsg = new CapturedSmsData();
                            setmsg.setSenderNumber("unknown");//default
                            setmsg.setSenderMsg("no data");//default

                            int newcount = (x-15);//count back to get senders number if any

                            //System.out.println("NewCount >>> "+newcount+" Xcount>>>>> "+x);
                            if(newcount > 0) {//only check if array length is not -minus
                                while (newcount < x) {
                                    if (progress[newcount].contains(DETECTION_PHONENUM_SMS_DATA[2].toString())) {
                                        try {
                                            //Looking for OrigAddr this is where type0 sender number is
                                            String number = progress[newcount].substring(progress[newcount].indexOf("OrigAddr")).replace(DETECTION_PHONENUM_SMS_DATA[2].toString(), "").trim();
                                            setmsg.setSenderNumber(number);//default
                                        }catch (Exception ee){}
                                        //System.out.println("Number>>>"+number);
                                    } else if (progress[newcount].contains(DETECTION_PHONENUM_SMS_DATA[1].toString())) {
                                        try {
                                            String smsdata = progress[newcount].substring(
                                                    progress[newcount].indexOf("'") + 1,
                                                    progress[newcount].length() - 1);
                                            //System.out.println("SMS Data>>>"+smsdata);
                                            setmsg.setSenderMsg(smsdata);
                                        }catch (Exception ee){}
                                    }
                                    newcount++;
                                }
                            }
                            setmsg.setSmsTimestamp(MiscUtils.getCurrentTimeStamp());
                            setmsg.setSmsType("TYPE0");
                            setmsg.setCurrent_lac(mAimsicdService.getCellTracker().getMonitorCell().getLAC());
                            setmsg.setCurrent_cid(mAimsicdService.getCellTracker().getMonitorCell().getCID());
                            setmsg.setCurrent_nettype(Device.getNetworkTypeName(mAimsicdService.getCell().getNetType()));
                            setmsg.setCurrent_roam_status(mAimsicdService.getCellTracker().getDevice().isRoaming());
                            //TODO is this the right place to get upto date geo location?
                            setmsg.setCurrent_gps_lat(mAimsicdService.lastKnownLocation().getLatitudeInDegrees());
                            setmsg.setCurrent_gps_lon(mAimsicdService.lastKnownLocation().getLongitudeInDegrees());
                            dbacess.open();
                            dbacess.storeCapturedSms(setmsg);
                            dbacess.close();
                            showNotification(tContext,"Type0 Sms Intercepted");
                            MiscUtils.startPopUpInfo(tContext, 6);

                        }else if(SILENT_ONLY_TAGS[arrayindex].split("#")[1].trim().equals("SILENTVOICE")){
                            Log.i(TAG, "SILENT DETECTED");
                            CapturedSmsData setmsg = new CapturedSmsData();
                            setmsg.setSenderNumber("unknown");//default
                            setmsg.setSenderMsg("no data");//default
                            int newcount = (x-15);
                            //System.out.println("NewCount >>> "+newcount+" Xcount>>>>> "+x);
                            if(newcount > 0) {//only check if array length is not -minus
                                while (newcount < x) {
                                    if (progress[newcount].contains(DETECTION_PHONENUM_SMS_DATA[0].toString())) {
                                        try {
                                            String number = progress[newcount].substring(progress[newcount].indexOf("+"));
                                            setmsg.setSenderNumber(number);//default
                                        }catch (Exception ee){}
                                        //System.out.println("Number>>>"+number);
                                    }else if (progress[newcount].contains(DETECTION_PHONENUM_SMS_DATA[2].toString())) {
                                        try {
                                            //Looking for OrigAddr this is where type0 sender number is
                                            String number = progress[newcount].substring(progress[newcount].indexOf("OrigAddr")).replace(DETECTION_PHONENUM_SMS_DATA[2].toString(), "").trim();
                                            setmsg.setSenderNumber(number);//default
                                        }catch (Exception ee){}
                                        //System.out.println("Number>>>"+number);
                                    } else if (progress[newcount].contains(DETECTION_PHONENUM_SMS_DATA[1].toString())) {
                                        try {
                                            String smsdata = progress[newcount].substring(
                                                    progress[newcount].indexOf("'") + 1,
                                                    progress[newcount].length() - 1);
                                            //System.out.println("SMS Data>>>"+smsdata);
                                            setmsg.setSenderMsg(smsdata);
                                        }catch (Exception ee){}
                                    }
                                    newcount++;
                                }
                            }

                            setmsg.setSmsTimestamp(MiscUtils.getCurrentTimeStamp());
                            setmsg.setSmsType("SILENTVOICE");
                            setmsg.setCurrent_lac(mAimsicdService.getCellTracker().getMonitorCell().getLAC());
                            setmsg.setCurrent_cid(mAimsicdService.getCellTracker().getMonitorCell().getCID());
                            setmsg.setCurrent_nettype(Device.getNetworkTypeName(mAimsicdService.getCell().getNetType()));
                            setmsg.setCurrent_roam_status(mAimsicdService.getCellTracker().getDevice().isRoaming());
                            //TODO is this the right place to get upto date geo location?
                            setmsg.setCurrent_gps_lat(mAimsicdService.lastKnownLocation().getLatitudeInDegrees());
                            setmsg.setCurrent_gps_lon(mAimsicdService.lastKnownLocation().getLongitudeInDegrees());
                            dbacess.open();
                            dbacess.storeCapturedSms(setmsg);
                            dbacess.close();
                            showNotification(tContext,"SilentVoice Sms Intercepted");
                            MiscUtils.startPopUpInfo(tContext, 7);
                        }
                        break;
                    }
                }
            }//for loop
            //TextMessageReceiver.setIS_MESSAGE_RECEIVED(false);//reset the boolean to false

        }
    }

    public void showNotification(Context context ,String contentText){
    int NOTIFICATION_ID = 1;
    String tickerText = context.getResources().getString(R.string.app_name_short);

    Intent notificationIntent = new Intent(context, AIMSICD.class);
    notificationIntent.putExtra("silent_sms", true);
    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_FROM_BACKGROUND);

    PendingIntent contentIntent = PendingIntent.getActivity(
            context, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

    Notification mBuilder =
            new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.sense_danger)
                    .setTicker(tickerText)
                    .setContentTitle(context.getResources().getString(R.string.main_app_name))
                    .setContentText(contentText)
                    .setOngoing(true)
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent)
                    .build();
    NotificationManager mNotificationManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(NOTIFICATION_ID, mBuilder);

}
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAimsicdService = ((AimsicdService.AimscidBinder) service).getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG, "Service Disconnected Sms Detection");
            mBound = false;

        }
    };
}

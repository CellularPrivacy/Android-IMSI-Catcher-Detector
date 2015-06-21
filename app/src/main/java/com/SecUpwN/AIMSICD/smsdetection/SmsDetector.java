/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */

package com.SecUpwN.AIMSICD.smsdetection;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.utils.Device;
import com.SecUpwN.AIMSICD.utils.MiscUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * Coded by Paul Kinsella @banjaxbanjo
 * For this to work better Samsung users will have to
 * set debug level to high in SysDump menu *#9900# or *#*#9900#*#*
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
        for(int x = 0;x <silent_string.size();x++)
        {
        SILENT_ONLY_TAGS[x] = silent_string.get(x).getDetection_string()+"#"+silent_string.get(x).getDetection_type();
        }
        prefs = newcontext.getSharedPreferences(AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
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
                String MODE = "logcat -v time -b radio\n";// default
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

                    if (bufferlen != 0) {
                        byte[] b = new byte[bufferlen];
                        dis.read(b);

                        String split[] = new String(b).split("\n");
                        checkForSilentSms(split);

                    } else { Thread.sleep(1000); }

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG,e.toString());
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    Log.e(TAG,e.toString());
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,e.toString());
        } finally {

        }
    }


    public void checkForSilentSms(String[] progress){

        for(int arrayindex = 0;arrayindex < SILENT_ONLY_TAGS.length;arrayindex++)
        {

            for(int x =0;x <progress.length;x++) {//check all progress buffer for strings

                if(progress[x].length() < 250){//check only short strings for faster processing

                    if (progress[x].contains(SILENT_ONLY_TAGS[arrayindex].split("#")[0])) {
                        /* if we get to this loop we detected a known detection string
                        *  and the next if blocks detect what type of sms it is
                        * */

                        /*
                        * saving the logcat timestamp so we can check db if timestamp
                        * was already saved
                        * */
                        String logcat_timestamp = MiscUtils.logcatTimeStampParser(progress[x]);
                        Log.i(TAG,"TIME::"+logcat_timestamp);

                        Log.i(TAG,"Detected>>>>"+SILENT_ONLY_TAGS[arrayindex].split("#")[1]);
                        if(SILENT_ONLY_TAGS[arrayindex].split("#")[1].equals("TYPE0")){

                            CapturedSmsData setmsg = new CapturedSmsData();
                            setmsg.setSenderNumber("unknown");//default
                            setmsg.setSenderMsg("no data");//default
                            /*
                            *    count back to get senders number if any
                            *    senders numuber is usually back about -15
                            *    in the array.
                            * */
                            int newcount = x - 15;

                            if(newcount > 0) {//only check if array length is not -minus (if minus we cant count back so skip)
                                while (newcount < x) {//loop thru array and try find number and sms data if any
                                    if (progress[newcount].contains(DETECTION_PHONENUM_SMS_DATA[2].toString())) {
                                        try {
                                            //Looking for OrigAddr this is where type0 sender number is
                                            String number = progress[newcount].substring(progress[newcount].indexOf("OrigAddr")).replace(DETECTION_PHONENUM_SMS_DATA[2].toString(), "").trim();
                                            setmsg.setSenderNumber(number);//default
                                        } catch (Exception ee) {
                                            Log.e(TAG, "Error parsing number\n"+ ee.toString());
                                        }
                                    }else if (progress[newcount].contains(DETECTION_PHONENUM_SMS_DATA[1].toString())) {
                                        try {
                                            String smsdata = progress[newcount].substring(
                                                    progress[newcount].indexOf("'") + 1,
                                                    progress[newcount].length() - 1);

                                            setmsg.setSenderMsg(smsdata);
                                        }catch (Exception ee){
                                            Log.e(TAG,"Error parsing sms data\n"+ ee.toString());}
                                    }
                                    newcount++;
                                }
                            }
                            setmsg.setSmsTimestamp(logcat_timestamp);
                            setmsg.setSmsType("TYPE0");
                            setmsg.setCurrent_lac(mAimsicdService.getCellTracker().getMonitorCell().getLAC());
                            setmsg.setCurrent_cid(mAimsicdService.getCellTracker().getMonitorCell().getCID());
                            setmsg.setCurrent_nettype(Device.getNetworkTypeName(mAimsicdService.getCell().getNetType()));
                            setmsg.setCurrent_roam_status(mAimsicdService.getCellTracker().getDevice().isRoaming());
                            //TODO is this the right place to get upto date geo location?
                            setmsg.setCurrent_gps_lat(mAimsicdService.lastKnownLocation().getLatitudeInDegrees());
                            setmsg.setCurrent_gps_lon(mAimsicdService.lastKnownLocation().getLongitudeInDegrees());

                            //only alert if timestamp is not in the data base
                            dbacess.open();
                            if(!dbacess.isTimeStampInDB(logcat_timestamp)) {
                                 dbacess.storeCapturedSms(setmsg);
                                 MiscUtils.startPopUpInfo(tContext, 6);
                            }
                            dbacess.close();

                        //SILENT_ONLY_TAGS[arrayindex].split("#")[0] <-- index 0 is the detection string
                        //SILENT_ONLY_TAGS[arrayindex].split("#")[1] <-- index 1 is the sms TYPE WAPPUSH TYPE0 ETC...
                        }else if(SILENT_ONLY_TAGS[arrayindex].split("#")[1].trim().equals("SILENTVOICE")){
                            Log.i(TAG, "SILENT DETECTED");
                            CapturedSmsData setmsg = new CapturedSmsData();
                            setmsg.setSenderNumber("unknown");//default
                            setmsg.setSenderMsg("no data");//default
                            int newcount = x - 15;
                            if(newcount > 0) {//only check if array length is not -minus
                                while (newcount < x) {
                                    if (progress[newcount].contains(DETECTION_PHONENUM_SMS_DATA[0].toString())) {
                                        /* This first try usually has the number of the sender
                                        *  and second try is just there incase OrigAddr string shows.
                                        * */
                                        try {
                                            String number = progress[newcount].substring(progress[newcount].indexOf("+"));
                                            setmsg.setSenderNumber(number);//default
                                        }catch (Exception ee){Log.e(TAG, "Error parsing number\n"+ ee.toString());}
                                    }else if (progress[newcount].contains(DETECTION_PHONENUM_SMS_DATA[2].toString())) {
                                        try {
                                            //Looking for OrigAddr this is where sender number is
                                            String number = progress[newcount].substring(progress[newcount].indexOf("OrigAddr")).replace(DETECTION_PHONENUM_SMS_DATA[2].toString(), "").trim();
                                            setmsg.setSenderNumber(number);//default
                                        }catch (Exception ee){Log.e(TAG, "Error parsing number\n"+ ee.toString());}
                                    } else if (progress[newcount].contains(DETECTION_PHONENUM_SMS_DATA[1].toString())) {
                                        try {
                                            String smsdata = progress[newcount].substring(
                                                    progress[newcount].indexOf("'") + 1,
                                                    progress[newcount].length() - 1);
                                            setmsg.setSenderMsg(smsdata);
                                        }catch (Exception ee){Log.e(TAG, "Error parsing sms data\n"+ ee.toString());;}
                                    }
                                    newcount++;
                                }
                            }

                            setmsg.setSmsTimestamp(logcat_timestamp);
                            setmsg.setSmsType("SILENTVOICE");
                            setmsg.setCurrent_lac(mAimsicdService.getCellTracker().getMonitorCell().getLAC());
                            setmsg.setCurrent_cid(mAimsicdService.getCellTracker().getMonitorCell().getCID());
                            setmsg.setCurrent_nettype(Device.getNetworkTypeName(mAimsicdService.getCell().getNetType()));
                            setmsg.setCurrent_roam_status(mAimsicdService.getCellTracker().getDevice().isRoaming());
                            //TODO is this the right place to get upto date geo location?
                            setmsg.setCurrent_gps_lat(mAimsicdService.lastKnownLocation().getLatitudeInDegrees());
                            setmsg.setCurrent_gps_lon(mAimsicdService.lastKnownLocation().getLongitudeInDegrees());

                            //only alert if timestamp is not in the data base
                            dbacess.open();
                            if(!dbacess.isTimeStampInDB(logcat_timestamp)) {
                                dbacess.storeCapturedSms(setmsg);
                                MiscUtils.startPopUpInfo(tContext, 7);
                            }
                            dbacess.close();
                        }else if(SILENT_ONLY_TAGS[arrayindex].split("#")[1].trim().equals("WAPPUSH")){
                            /*
                                Wap Push in logcat shows no data only senders number
                                TODO: data is probably in db content://raw/1?
                             */
                            CapturedSmsData setmsg = new CapturedSmsData();
                            setmsg.setSenderNumber("unknown");//default
                            setmsg.setSenderMsg("no data");//default

                            int startindex = x-2;
                            int endindex = x+3;
                            /*  wap push port DestPort 0x0B84
                             *  its usually at this index of +3 in array                             *
                              * */
                            if (progress[x+3].contains("DestPort 0x0B84"))
                            {
                                Log.i(TAG, "WAPPUSH DETECTED");
                                /* loop thru array to find number */
                                if (endindex+3 <= progress.length) {
                                    while (startindex < endindex)
                                    {
                                        if (progress[startindex].contains(DETECTION_PHONENUM_SMS_DATA[2].toString())) {
                                            try {
                                                //Looking for OrigAddr this is where sender number is
                                                String number = progress[startindex].substring(
                                                        progress[startindex].indexOf("OrigAddr")).replace(
                                                        DETECTION_PHONENUM_SMS_DATA[2].toString(), "").trim();

                                                setmsg.setSenderNumber(number);//default
                                                break;
                                            } catch (Exception ee) {
                                                Log.e(TAG, "Error parsing number\n"+ ee.toString());
                                            }

                                        }
                                        startindex++;

                                    }
                                }

                                setmsg.setSmsTimestamp(logcat_timestamp);
                                setmsg.setSmsType("WAPPUSH");
                                setmsg.setCurrent_lac(mAimsicdService.getCellTracker().getMonitorCell().getLAC());
                                setmsg.setCurrent_cid(mAimsicdService.getCellTracker().getMonitorCell().getCID());
                                setmsg.setCurrent_nettype(Device.getNetworkTypeName(mAimsicdService.getCell().getNetType()));
                                setmsg.setCurrent_roam_status(mAimsicdService.getCellTracker().getDevice().isRoaming());
                                //TODO is this the right place to get upto date geo location?
                                setmsg.setCurrent_gps_lat(mAimsicdService.lastKnownLocation().getLatitudeInDegrees());
                                setmsg.setCurrent_gps_lon(mAimsicdService.lastKnownLocation().getLongitudeInDegrees());

                                //only alert if timestamp is not in the data base
                                dbacess.open();
                                if(!dbacess.isTimeStampInDB(logcat_timestamp)) {
                                    dbacess.storeCapturedSms(setmsg);
                                    MiscUtils.startPopUpInfo(tContext, 8);
                                }
                                dbacess.close();
                            }// end of if contains("DestPort 0x0B84")
                        }
                        break;
                    }
                }
            }

        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAimsicdService = ((AimsicdService.AimscidBinder) service).getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.i(TAG, "Service Disconnected Sms Detection");
            mBound = false;

        }
    };
}

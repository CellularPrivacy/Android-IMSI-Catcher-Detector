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

import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.utils.Device;
import com.SecUpwN.AIMSICD.utils.MiscUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Description: Detects mysterious SMS by scraping Logcat entries.
 *
 *
 * NOTES:   For this to work better Samsung users might have to set their Debug Level to High
 *          in SysDump menu *#9900# or *#*#9900#*#*
 *
 *          This is by no means a complete detection method but gives us something to work off.
 *
 *          For latest list of working phones/models, please see:
 *          https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/532
 *
 *          PHONE:Samsung S5      MODEL:SM-G900F      ANDROID_VER:4.4.2   TYPE0:YES MWI:YES
 *          PHONE:Samsung S4-min  MODEL:GT-I9195      ANDROID_VER:4.2.2   TYPE0:YES MWI:YES
 *          PHONE:Sony Xperia J   MODEL:ST260i        ANDROID_VER:4.1.2   TYPE0:NO  MWI:YES
 *
 * To Use:
 *
 *    SmsDetector smsdetector = new SmsDetector(context);
 *
 *    smsdetector.startSmsDetection();
 *    smsdetector.stopSmsDetection();
 *
 *
 *  TODO:
 *          [ ] Add more mTAG to the detection Log items
 *
 *
 *  @author: Paul Kinsella @banjaxbanjo
 */
public class SmsDetector extends Thread {

    private final static String TAG = "SmsDetector";

    private DataInputStream dis;
    private DataOutputStream dos;
    private AimsicdService mAimsicdService;
    private SharedPreferences prefs;
    private boolean mBound;
    private AIMSICDDbAdapter dbacess;
    private Context tContext;
    private String[] SILENT_ONLY_TAGS;

    /**
     *  Holds known values to get the senders number and sms data
     */
    private String DETECTION_PHONENUM_SMS_DATA[] = {
            "SMS originating address:",
            "SMS message body (raw):",
            "OrigAddr"};

    private static boolean isRunning = false;

    public SmsDetector(Context newcontext){
        tContext = newcontext;
        dbacess =  new AIMSICDDbAdapter(newcontext);


        ArrayList<AdvanceUserItems> silent_string = dbacess.getDetectionStrings();

        SILENT_ONLY_TAGS = new String[silent_string.size()];
        for(int x = 0; x <silent_string.size(); x++) {
            SILENT_ONLY_TAGS[x] = silent_string.get(x).getDetection_string()
                    + "#"+silent_string.get(x).getDetection_type();
        }
        prefs = newcontext.getSharedPreferences(AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
    }

    public static boolean getSmsDetectionState() {
        return isRunning;
    }

    public static void setSmsDetectionState(boolean isrunning) {
        SmsDetector.isRunning = isrunning;
    }

    public void startSmsDetection() {
        Intent intent = new Intent(tContext, AimsicdService.class);
        tContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            start();
            Log.i(TAG, "SMS detection started");
   }

    public void stopSmsDetection() {
        setSmsDetectionState(false);
        // Unbind from the service
        if (mBound) {
            tContext.unbindService(mConnection);
            mBound = false;
        }
        Log.i(TAG, "SMS detection stopped");
    }

    @Override
    public void run() {
        setSmsDetectionState(true);

        try {
            new Thread().sleep(500);

            String MODE = "logcat -v time -b radio -b main\n";
            Runtime r = Runtime.getRuntime();
            Process process = r.exec("su");
            dos = new DataOutputStream(process.getOutputStream());

            dos.writeBytes(MODE);
            dos.flush();
            dos.close();

            dis = new DataInputStream(process.getInputStream());
        } catch (InterruptedException e) {
            Log.e(TAG, "thread interrupted", e);
        } catch (IOException e) {
            Log.e(TAG, "thread interrupted", e);
        }

        while (getSmsDetectionState()) {
            try {
                int bufferlen = dis.available();

                if (bufferlen != 0) {
                    byte[] b = new byte[bufferlen];
                    dis.read(b);

                    String split[] = new String(b).split("\n");
                    checkForSilentSms(split);

                } else {
                    Thread.sleep(1000);
                }

            } catch (IOException e) {
                Log.e(TAG, "IOE Stacktrace", e);
            } catch (InterruptedException e) {
                Log.e(TAG, "IE Exception", e);
            }
        }
    }


    public void checkForSilentSms(String[] progress){

        for(int index = 0; index < SILENT_ONLY_TAGS.length; index++) {

            for(int x =0;x <progress.length;x++) {//check all progress buffer for strings

                if(progress[x].length() < 250){//check only short strings for faster processing

                    if (progress[x].contains(SILENT_ONLY_TAGS[index].split("#")[0])) {
                        /* if we get to this loop we detected a known detection string
                        *  and the next if blocks detect what type of sms it is
                        * */

                        /*
                        * saving the logcat timestamp so we can check db if timestamp
                        * was already saved
                        * */
                        String logcat_timestamp = MiscUtils.logcatTimeStampParser(progress[x]);
                        Log.i(TAG, "TIME::" + logcat_timestamp);

                        Log.i(TAG, "Detected>>>>" + SILENT_ONLY_TAGS[index].split("#")[1]);

                        if(SILENT_ONLY_TAGS[index].split("#")[1].equals("TYPE0")) {

                            CapturedSmsData setmsg = new CapturedSmsData();
                            setmsg.setSenderNumber("unknown");
                            setmsg.setSenderMsg("no data");

                            // Count backward to get the senders number (if any). The senders
                            // number is usually back about -15 in the array.
                            int newCount = x - 15;

                            if(newCount > 0) { // Only check if array length is not -minus (if minus we can't count back so skip)
                                while (newCount < x) {  // Loop through array and try find number and sms data if any
                                    if (progress[newCount].contains(DETECTION_PHONENUM_SMS_DATA[2].toString())) {
                                        try {
                                            //Looking for OrigAddr this is where type0 sender number is
                                            String number = progress[newCount].substring(progress[newCount].indexOf("OrigAddr")).replace(DETECTION_PHONENUM_SMS_DATA[2].toString(), "").trim();
                                            setmsg.setSenderNumber(number);
                                        } catch (Exception ee) {
                                            Log.e(TAG, "Error parsing number", ee);
                                        }
                                    }else if (progress[newCount].contains(DETECTION_PHONENUM_SMS_DATA[1].toString())) {
                                        try {
                                            String smsdata = progress[newCount].substring(
                                                    progress[newCount].indexOf("'") + 1,
                                                    progress[newCount].length() - 1);

                                            setmsg.setSenderMsg(smsdata);
                                        } catch (Exception ee) {
                                            Log.e(TAG, "Error parsing SMS data:\n"+ ee.toString(), ee);
                                        }
                                    }
                                    newCount++;
                                }
                            }
                            setmsg.setSmsTimestamp(logcat_timestamp);
                            setmsg.setSmsType("TYPE0");
                            setmsg.setCurrent_lac(mAimsicdService.getCellTracker().getMonitorCell().getLAC());
                            setmsg.setCurrent_cid(mAimsicdService.getCellTracker().getMonitorCell().getCID());
                            setmsg.setCurrent_nettype(Device.getNetworkTypeName(mAimsicdService.getCell().getNetType()));
                            int isRoaming = 0;

                            if("true".equals(mAimsicdService.getCellTracker().getDevice().isRoaming())) {
                                isRoaming = 1;
                            }
                            setmsg.setCurrent_roam_status(isRoaming);
                            // TODO is this the right place to get upto date geo location?
                            setmsg.setCurrent_gps_lat(mAimsicdService.lastKnownLocation().getLatitudeInDegrees());
                            setmsg.setCurrent_gps_lon(mAimsicdService.lastKnownLocation().getLongitudeInDegrees());

                            // Only alert if the timestamp is not in the data base
                            if(!dbacess.isTimeStampInDB(logcat_timestamp)) {
                                dbacess.storeCapturedSms(setmsg);
                                /*dbacess.insertEventLog(
                                        MiscUtils.getCurrentTimeStamp(),
                                        mAimsicdService.getCellTracker().getMonitorCell().getLAC(),
                                        mAimsicdService.getCellTracker().getMonitorCell().getCID(),
                                        mAimsicdService.getCellTracker().getMonitorCell().getPSC(),
                                        String.valueOf(mAimsicdService.lastKnownLocation().getLatitudeInDegrees()),
                                        String.valueOf(mAimsicdService.lastKnownLocation().getLatitudeInDegrees()),
                                        (int)mAimsicdService.getCell().getAccuracy(),
                                        3,
                                        "Detected Type-0 SMS"
                                );*/
                                dbacess.toEventLog(3, "Detected Type-0 SMS");
                                MiscUtils.startPopUpInfo(tContext, 6);
                            } else {
                                Log.d(TAG, "Detected Sms already logged");
                            }


                        }else if("MWI".equals(SILENT_ONLY_TAGS[index].split("#")[1].trim())) {
                            Log.i(TAG, "MWI DETECTED");
                            CapturedSmsData setmsg = new CapturedSmsData();
                            setmsg.setSenderNumber("unknown");
                            setmsg.setSenderMsg("no data");
                            int newCount = x - 15;

                            //only check if array length is not -minus
                            if(newCount > 0) {
                                while (newCount < x) {
                                    if (progress[newCount].contains(DETECTION_PHONENUM_SMS_DATA[0].toString())) {

                                        // This first try usually has the number of the sender
                                        // and second try is just there incase OrigAddr string shows.
                                        try {
                                            String number = progress[newCount].substring(progress[newCount].indexOf("+"));
                                            setmsg.setSenderNumber(number);
                                        } catch (Exception ee) {
                                            Log.e(TAG, "Error parsing number", ee);
                                        }
                                    }else if (progress[newCount].contains(DETECTION_PHONENUM_SMS_DATA[2].toString())) {
                                        try {
                                            //Looking for OrigAddr this is where sender number is
                                            String number = progress[newCount].substring(progress[newCount].indexOf("OrigAddr")).replace(DETECTION_PHONENUM_SMS_DATA[2].toString(), "").trim();
                                            setmsg.setSenderNumber(number);
                                        } catch (Exception ee) {
                                            Log.e(TAG, "Error parsing number:"+ ee.getMessage(), ee);
                                        }
                                    } else if (progress[newCount].contains(DETECTION_PHONENUM_SMS_DATA[1].toString())) {
                                        try {
                                            String smsData = progress[newCount].substring(
                                                    progress[newCount].indexOf("'") + 1,
                                                    progress[newCount].length() - 1);

                                            setmsg.setSenderMsg(smsData);
                                        } catch (Exception ee) {
                                            Log.e(TAG, "Error parsing SMS data" + ee.getMessage(), ee);
                                        }
                                    }
                                    newCount++;
                                }
                            }

                            setmsg.setSmsTimestamp(logcat_timestamp);
                            setmsg.setSmsType("MWI");
                            setmsg.setCurrent_lac(mAimsicdService.getCellTracker().getMonitorCell().getLAC());
                            setmsg.setCurrent_cid(mAimsicdService.getCellTracker().getMonitorCell().getCID());
                            setmsg.setCurrent_nettype(Device.getNetworkTypeName(mAimsicdService.getCell().getNetType()));
                            int isRoaming = 0;
                            if("true".equals(mAimsicdService.getCellTracker().getDevice().isRoaming())) {
                                isRoaming = 1;
                            }
                            setmsg.setCurrent_roam_status(isRoaming);
                            //TODO is this the right place to get upto date geo location?
                            setmsg.setCurrent_gps_lat(mAimsicdService.lastKnownLocation().getLatitudeInDegrees());
                            setmsg.setCurrent_gps_lon(mAimsicdService.lastKnownLocation().getLongitudeInDegrees());

                            //only alert if timestamp is not in the data base

                            if(!dbacess.isTimeStampInDB(logcat_timestamp)) {
                                dbacess.storeCapturedSms(setmsg);

                                /*dbacess.insertEventLog(
                                        MiscUtils.getCurrentTimeStamp(),
                                        mAimsicdService.getCellTracker().getMonitorCell().getLAC(),
                                        mAimsicdService.getCellTracker().getMonitorCell().getCID(),
                                        mAimsicdService.getCellTracker().getMonitorCell().getPSC(),
                                        String.valueOf(mAimsicdService.lastKnownLocation().getLatitudeInDegrees()),
                                        String.valueOf(mAimsicdService.lastKnownLocation().getLatitudeInDegrees()),
                                        (int)mAimsicdService.getCell().getAccuracy(),
                                        3,//TODO what are the DF_ids? 1 = changing lac 2 = cell no in OCID 3 = detected sms?
                                        "Detected MWI sms"
                                );*/
                                dbacess.toEventLog(4,"Detected MWI SMS");
                                MiscUtils.startPopUpInfo(tContext, 7);
                            } else {
                                Log.d(TAG, " Detected Sms already logged");
                            }

                        } else if("WAPPUSH".equals(SILENT_ONLY_TAGS[index].split("#")[1].trim())) {
                            /*
                                Wap Push in logcat shows no data only senders number
                                TODO: data is probably in db content://raw/1?
                             */
                            CapturedSmsData setmsg = new CapturedSmsData();
                            setmsg.setSenderNumber("unknown");
                            setmsg.setSenderMsg("no data");

                            int startindex = x - 2;
                            int endindex = x + 3;
                            /*  wap push port DestPort 0x0B84
                             *  its usually at this index of +3 in array                             *
                              * */
                            //This is index on Samsungs is different for other phone makes
                            if (progress[x + 3].contains("DestPort 0x0B84")) {
                                Log.d(TAG, "WAPPUSH DETECTED");
                                /* loop thru array to find number */
                                if (endindex+3 <= progress.length) {
                                    while (startindex < endindex)
                                    {
                                        if (progress[startindex].contains(DETECTION_PHONENUM_SMS_DATA[2].toString())) {
                                            try {
                                                // Looking for OrigAddr this is where sender number is
                                                String number = progress[startindex].substring(
                                                        progress[startindex].indexOf("OrigAddr")).replace(
                                                        DETECTION_PHONENUM_SMS_DATA[2].toString(), "").trim();

                                                setmsg.setSenderNumber(number);
                                                break;
                                            } catch (Exception ee) {
                                                Log.e(TAG, "Error parsing number", ee);
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
                                int isRoaming = 0;
                                if("true".equals(mAimsicdService.getCellTracker().getDevice().isRoaming())){
                                    isRoaming = 1;
                                }
                                setmsg.setCurrent_roam_status(isRoaming);
                                //TODO is this the right place to get upto date geo location?
                                setmsg.setCurrent_gps_lat(mAimsicdService.lastKnownLocation().getLatitudeInDegrees());
                                setmsg.setCurrent_gps_lon(mAimsicdService.lastKnownLocation().getLongitudeInDegrees());

                                //only alert if timestamp is not in the data base

                                if(!dbacess.isTimeStampInDB(logcat_timestamp)) {
                                    dbacess.storeCapturedSms(setmsg);

                                    /*dbacess.insertEventLog(
                                            MiscUtils.getCurrentTimeStamp(),
                                            mAimsicdService.getCellTracker().getMonitorCell().getLAC(),
                                            mAimsicdService.getCellTracker().getMonitorCell().getCID(),
                                            mAimsicdService.getCellTracker().getMonitorCell().getPSC(),
                                            String.valueOf(mAimsicdService.lastKnownLocation().getLatitudeInDegrees()),
                                            String.valueOf(mAimsicdService.lastKnownLocation().getLatitudeInDegrees()),
                                            (int)mAimsicdService.getCell().getAccuracy(),
                                            3,
                                            "Detected WAP PUSH sms"
                                    );*/
                                    dbacess.toEventLog(5, "Detected WAP PUSH SMS");
                                    MiscUtils.startPopUpInfo(tContext, 8);
                                } else {
                                    Log.d(TAG, "Detected Sms already logged");
                                }

                            }// end of if contains("DestPort 0x0B84")

                            //This is index on Samsung's is different for other phone makes
                            else if (progress[x-1].contains("SMS originating address:")) {
                                Log.i(TAG, "WAPPUSH DETECTED");
                                /* loop thru array to find number */
                                endindex = x+3;
                                startindex = x-3;

                                if (endindex <= progress.length) {
                                    while (startindex < endindex)
                                    {
                                        if (progress[startindex].contains("SMS originating address:")) {
                                            try {
                                                String number = progress[startindex].substring(progress[startindex].indexOf("+"));
                                                setmsg.setSenderNumber(number);
                                            } catch (Exception ee) {
                                                Log.e(TAG, "Error parsing number "+ ee.toString());
                                            }

                                        }
                                        if (progress[startindex].contains("SMS SC address:")) {
                                            try {
                                                String number = progress[startindex].substring(progress[startindex].indexOf("+"));
                                                Log.d(TAG, "Detected msg smsc: " + number);
                                            } catch (Exception ee) {
                                                Log.e(TAG, "Error parsing smsc number: " + ee.toString());
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
                                int isRoaming = 0;
                                if("true".equals(mAimsicdService.getCellTracker().getDevice().isRoaming())) {
                                    isRoaming = 1;
                                }
                                setmsg.setCurrent_roam_status(isRoaming);
                                //TODO is this the right place to get upto date geo location?
                                setmsg.setCurrent_gps_lat(mAimsicdService.lastKnownLocation().getLatitudeInDegrees());
                                setmsg.setCurrent_gps_lon(mAimsicdService.lastKnownLocation().getLongitudeInDegrees());

                                //only alert if timestamp is not in the data base
                                if(!dbacess.isTimeStampInDB(logcat_timestamp)) {
                                    dbacess.storeCapturedSms(setmsg);

                                    /*dbacess.insertEventLog(
                                    MiscUtils.getCurrentTimeStamp(),
                                            mAimsicdService.getCellTracker().getMonitorCell().getLAC(),
                                            mAimsicdService.getCellTracker().getMonitorCell().getCID(),
                                            mAimsicdService.getCellTracker().getMonitorCell().getPSC(),
                                            String.valueOf(mAimsicdService.lastKnownLocation().getLatitudeInDegrees()),
                                            String.valueOf(mAimsicdService.lastKnownLocation().getLatitudeInDegrees()),
                                            (int)mAimsicdService.getCell().getAccuracy(),
                                            3,
                                            "Detected WAP PUSH sms"
                                    );*/
                                    dbacess.toEventLog(6, "Detected WAP PUSH (2) SMS");
                                    MiscUtils.startPopUpInfo(tContext, 8);
                                } else {
                                    Log.d(TAG, "Detected SMS already logged");
                                }

                            }// end of if contains("SMS originating address:")
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
            Log.i(TAG, "Disconnected SMS Detection Service");
            mBound = false;
        }
    };
}

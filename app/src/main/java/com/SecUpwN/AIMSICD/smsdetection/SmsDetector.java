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
import android.os.IBinder;
import android.util.Log;

import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.utils.Device;
import com.SecUpwN.AIMSICD.utils.MiscUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private DataOutputStream dos;
    BufferedReader in;
    private AimsicdService mAimsicdService;
    private boolean mBound;
    private AIMSICDDbAdapter dbacess;
    private Context tContext;
    private String[] LOADED_DETECTION_STRINGS;
    private int B_LINED_SIZE = 30;// how many previous lines to hold
    private String[] BUFFEREDLINES = new String[B_LINED_SIZE];//for holding previous lines from logcat that hold data we need
    private final int TYPE0 = 1,MWI =2,WAP =3;


    private static boolean isRunning = false;

    public SmsDetector(Context newcontext){
        tContext = newcontext;
        dbacess =  new AIMSICDDbAdapter(newcontext);


        ArrayList<AdvanceUserItems> silent_string = dbacess.getDetectionStrings();

        LOADED_DETECTION_STRINGS = new String[silent_string.size()];
        for(int x = 0; x <silent_string.size(); x++) {
            LOADED_DETECTION_STRINGS[x] = silent_string.get(x).getDetection_string()
                    + "#"+silent_string.get(x).getDetection_type();
        }
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
            String MODE = "logcat -v time -b radio -b main\n";
            Runtime r = Runtime.getRuntime();
            Process process = r.exec("su");
            dos = new DataOutputStream(process.getOutputStream());
            dos.writeBytes(MODE);
            dos.flush();
            dos.close();
            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        } catch (IOException e) {
            Log.e(TAG, "IOException ", e);
        }

        int count =0;
        while (getSmsDetectionState()) {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    //reset to 0 if count bigger then array size
                    if(count >= B_LINED_SIZE){
                        count = 0;
                    }

                    BUFFEREDLINES[count] = line;
                    count++;
                    switch (checkForSms(line)){
                        case TYPE0:
                            parseTypeZeroSms(BUFFEREDLINES,MiscUtils.logcatTimeStampParser(line));
                            break;
                        case MWI:
                            parseMwiSms(BUFFEREDLINES, MiscUtils.logcatTimeStampParser(line));
                            break;
                        case WAP:
                            /*
                            we need to go forward a few more lines to get data
                            and store it in post buffer array
                            */
                            String[]POSTLINES = new String[10];
                            for(int x=0;x< 10;x++){
                                if((line = in.readLine()) != null){
                                    POSTLINES[x] = line;
                                }
                            }
                            parseWapPushSms(BUFFEREDLINES,POSTLINES, MiscUtils.logcatTimeStampParser(line));
                            break;
                    }
                }

            } catch (IOException e) {
                Log.e(TAG, "IOE Stacktrace", e);
            }
        }
        try{
            in.close();
        }catch (IOException ee){
            Log.e(TAG, "IOE Error closing BufferedReader", ee);
        }

    }


    private int checkForSms(String line){
        //0 - null 1 = TYPE0, 2 = MWI, 3 = WAPPUSH
        for(int i = 0; i < LOADED_DETECTION_STRINGS.length; i++) {
            //looping thru detection strings to see does logcat line match
            if(line.contains(LOADED_DETECTION_STRINGS[i].split("#")[0])) {

                if(LOADED_DETECTION_STRINGS[i].split("#")[1].equals("TYPE0")) {
                    Log.i(TAG, "TYPE0 detected");
                    return TYPE0;
                }else if(LOADED_DETECTION_STRINGS[i].split("#")[1].equals("MWI")) {
                    Log.i(TAG, "MWI detected");
                    return MWI;
                }else if(LOADED_DETECTION_STRINGS[i].split("#")[1].equals("WAPPUSH")) {
                    Log.i(TAG, "WAPPUSH detected");
                    return WAP;
                }

            }else if(line.contains("BroadcastReceiver action: android.provider.Telephony.SMS_RECEIVED")){
                Log.i(TAG, "SMS found");
                return 0;
            }
        }
        return 0;
    }

    private void parseTypeZeroSms(String[] bufflines,String logcat_timestamp){

        CapturedSmsData setmsg = new CapturedSmsData();
        String smstext = findSmsData(bufflines,null);
        String num = findSmsNumber(bufflines,null);

        if(smstext == null){smstext = "null";}
        if(num == null){num = "null";}

        setmsg.setSenderNumber(num);
        setmsg.setSenderMsg(smstext);
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
        setmsg.setCurrent_gps_lat(mAimsicdService.lastKnownLocation().getLatitudeInDegrees());
        setmsg.setCurrent_gps_lon(mAimsicdService.lastKnownLocation().getLongitudeInDegrees());

        // Only alert if the timestamp is not in the data base
        if(!dbacess.isTimeStampInDB(logcat_timestamp)) {
            dbacess.storeCapturedSms(setmsg);
            dbacess.toEventLog(3, "Detected Type-0 SMS");
            MiscUtils.startPopUpInfo(tContext, 6);
        } else {
            Log.d(TAG, "Detected Sms already logged");
        }

    }

    private void parseMwiSms(String[] bufflines,String logcat_timestamp){

        CapturedSmsData setmsg = new CapturedSmsData();
        String smstext = findSmsData(bufflines,null);
        String num = findSmsNumber(bufflines,null);

        if(smstext == null){smstext = "null";}
        if(num == null){num = "null";}

        setmsg.setSenderNumber(num);
        setmsg.setSenderMsg(smstext);
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
        setmsg.setCurrent_gps_lat(mAimsicdService.lastKnownLocation().getLatitudeInDegrees());
        setmsg.setCurrent_gps_lon(mAimsicdService.lastKnownLocation().getLongitudeInDegrees());

        //only alert if timestamp is not in the data base
        if(!dbacess.isTimeStampInDB(logcat_timestamp)) {
            dbacess.storeCapturedSms(setmsg);
            dbacess.toEventLog(4,"Detected MWI SMS");
            MiscUtils.startPopUpInfo(tContext, 7);
        } else {
            Log.d(TAG, " Detected Sms already logged");
        }
    }

    private void parseWapPushSms(String[] bufflines,String[] postlines,String logcat_timestamp){
        CapturedSmsData setmsg = new CapturedSmsData();
        String smstext = findSmsData(bufflines,postlines);
        String num = findSmsNumber(bufflines,postlines);
        if(smstext == null){smstext = "null";}
        if(num == null){num = "null";}
        setmsg.setSenderNumber(num);
        setmsg.setSenderMsg(smstext);
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
        setmsg.setCurrent_gps_lat(mAimsicdService.lastKnownLocation().getLatitudeInDegrees());
        setmsg.setCurrent_gps_lon(mAimsicdService.lastKnownLocation().getLongitudeInDegrees());

        //only alert if timestamp is not in the data base
        if(!dbacess.isTimeStampInDB(logcat_timestamp)) {
            dbacess.storeCapturedSms(setmsg);
            dbacess.toEventLog(6, "Detected WAPPUSH SMS");
            MiscUtils.startPopUpInfo(tContext, 8);
        } else {
            Log.d(TAG, "Detected SMS already logged");
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

    private String findSmsData(String[] prebuffer,String[] postbuffer){
        //check pre buffer for number and sms msg
        if(prebuffer != null) {
            for (int x = 0; x < prebuffer.length; x++) {
                if (prebuffer[x] != null) {
                    String testline = prebuffer[x];
                    if (testline.contains("SMS message body (raw):") && testline.contains("'")) {
                        testline = testline.substring(testline.indexOf("'") + 1,
                                testline.length() - 1);
                        return testline;
                    }
                }
            }
        }
        //check post buffer for number and sms msg
        if(postbuffer != null) {
            for (int x = 0; x < postbuffer.length; x++) {
                if (postbuffer[x] != null) {
                    String testline = prebuffer[x];
                    if (testline.contains("SMS message body (raw):") && testline.contains("'")) {
                        testline = testline.substring(testline.indexOf("'") + 1,
                                testline.length() - 1);
                        return testline;
                    }
                }
            }
        }
        return null;
    }

    private String findSmsNumber(String[] prebuffer,String[] postbuffer){
        //check pre buffer for number and sms msg
        if(prebuffer != null) {
            for (int x = 0; x < prebuffer.length; x++) {
                if (prebuffer[x] != null) {
                    String testline = prebuffer[x];
                    if(testline.contains("SMS originating address:") && testline.contains("+")){
                        String number = testline.substring(testline.indexOf("+"));
                        return number;
                    }else if(testline.contains("OrigAddr")){
                        testline = testline.substring(testline.indexOf("OrigAddr")).replace("OrigAddr", "").trim();
                        return testline;
                    }
                }
            }
        }
        //check post buffer for number and sms msg
        if(postbuffer != null) {
            for (int x = 0; x < postbuffer.length; x++) {
                if (postbuffer[x] != null) {
                    String testline = postbuffer[x];
                    if(testline.contains("SMS originating address:") && testline.contains("+")){
                        String number = testline.substring(testline.indexOf("+"));
                        return number;
                    }else if(testline.contains("OrigAddr")){
                        testline = testline.substring(testline.indexOf("OrigAddr")).replace("OrigAddr", "").trim();
                        return testline;
                    }
                }
                }

            }
        return null;
    }
}

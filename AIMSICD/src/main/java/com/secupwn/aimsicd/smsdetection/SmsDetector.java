/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */

package com.secupwn.aimsicd.smsdetection;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.data.model.GpsLocation;
import com.secupwn.aimsicd.utils.RealmHelper;
import com.secupwn.aimsicd.data.model.SmsData;
import com.secupwn.aimsicd.data.model.SmsDetectionString;
import com.secupwn.aimsicd.service.AimsicdService;
import com.secupwn.aimsicd.utils.MiscUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.freefair.android.util.logging.AndroidLogger;
import io.freefair.android.util.logging.Logger;
import io.realm.Realm;
import lombok.Cleanup;
import lombok.Getter;

/**
 * Description: Detects mysterious SMS by scraping Logcat entries.
 * <p/>
 * <p/>
 * NOTES:   For this to work better Samsung users might have to set their Debug Level to High
 * in SysDump menu *#9900# or *#*#9900#*#*
 * <p/>
 * This is by no means a complete detection method but gives us something to work off.
 * <p/>
 * For latest list of working phones/models, please see:
 * https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/issues/532
 * <p/>
 * PHONE:Samsung S5      MODEL:SM-G900F      ANDROID_VER:4.4.2   TYPE0:YES MWI:YES
 * PHONE:Samsung S4-min  MODEL:GT-I9195      ANDROID_VER:4.2.2   TYPE0:YES MWI:YES
 * PHONE:Sony Xperia J   MODEL:ST260i        ANDROID_VER:4.1.2   TYPE0:NO  MWI:YES
 * <p/>
 * To Use:
 * <p/>
 * SmsDetector smsDetector = new SmsDetector(context);
 * <p/>
 * smsDetector.startSmsDetection();
 * smsDetector.stopSmsDetection();
 * <p/>
 * <p/>
 * TODO:
 * [ ] Add more mTAG to the detection Log items
 *
 * @author Paul Kinsella @banjaxbanjo
 */
public final class SmsDetector extends Thread {

    private final Logger log = AndroidLogger.forClass(SmsDetector.class);

    private AimsicdService mAIMSICDService;
    private boolean mBound;
    private RealmHelper mDbAdapter;
    private Context mContext;
    private static final int TYPE0 = 1, MWI = 2, WAP = 3;
    // TODO: replace this with retrieval from AIMSICDDbAdapter
    private static final int LOGCAT_BUFFER_MAX_SIZE = 100;

    /**
     * To correctly detect sms data and phone numbers on wap, we need at least
     * 10 lines after line which indicates wap communication
     */
    private static final int LOGCAT_WAP_EXTRA_LINES = 10;

    private static boolean isRunning = false;

    public SmsDetector(Context context) {
        mContext = context;
        mDbAdapter = new RealmHelper(context);
    }

    public static boolean getSmsDetectionState() {
        return isRunning;
    }

    public static void setSmsDetectionState(boolean isRunning) {
        SmsDetector.isRunning = isRunning;
    }

    public void startPopUpInfo(SmsType smsType) {
        MiscUtils.showNotification(
                mContext,
                mContext.getString(smsType.getAlert()),
                mContext.getString(R.string.app_name_short) + " - " + mContext.getString(smsType.getTitle()),
                R.drawable.sense_danger,
                true);

        AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setTitle(smsType.getTitle())
                .setMessage(smsType.getMessage())
                .setIcon(R.drawable.sense_danger)
                .create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }

    public void startSmsDetection() {
        Intent intent = new Intent(mContext, AimsicdService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        start();
        log.info("SMS detection started");
    }

    public void stopSmsDetection() {
        setSmsDetectionState(false);
        // Unbind from the service
        if (mBound) {
            mContext.unbindService(mConnection);
            mBound = false;
        }
        log.info("SMS detection stopped");
    }

    @Override
    public void run() {
        setSmsDetectionState(true);

        BufferedReader mLogcatReader;
        try {
            Thread.sleep(500);

            String MODE = "logcat -v time -b radio -b main\n";
            Runtime r = Runtime.getRuntime();
            Process process = r.exec("su");
            @Cleanup DataOutputStream dos = new DataOutputStream(process.getOutputStream());

            dos.writeBytes(MODE);
            dos.flush();

            mLogcatReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        } catch (InterruptedException | IOException e) {
            log.error("Exception while initializing LogCat (time, radio, main) reader", e);
            return;
        }

        String logcatLine;
        List<String> logcatLines = new ArrayList<>();
        while (getSmsDetectionState()) {
            try {
                logcatLine = mLogcatReader.readLine();
                if (logcatLines.size() <= LOGCAT_BUFFER_MAX_SIZE || logcatLine != null) {
                    logcatLines.add(logcatLine);
                } else if (logcatLines.size() == 0) {
                    /**
                     * Sleep only when there is no more input, not after going through buffer
                     * to not unnecessary slow down the process
                     * */
                    Thread.sleep(1000);
                } else {
                    /**
                     * In moment, where there are no data
                     * we check the current buffer and clear it
                     * */
                    String[] outLines = new String[logcatLines.size()];
                    logcatLines.toArray(outLines);

                    for (int counter = 0; counter < logcatLines.size(); counter++) {
                        String bufferedLine = logcatLines.get(counter);
                        switch (checkForSms(bufferedLine)) {
                            case TYPE0:
                                parseTypeZeroSms(outLines, MiscUtils.parseLogcatTimeStamp(bufferedLine));
                                break;
                            case MWI:
                                parseMwiSms(outLines, MiscUtils.parseLogcatTimeStamp(bufferedLine));
                                break;
                            case WAP:
                                int remainingLinesInBuffer = logcatLines.size() - counter - LOGCAT_WAP_EXTRA_LINES;
                                if (remainingLinesInBuffer < 0) {
                                    /**
                                     * we need to go forward a few more lines to get data
                                     * and store it in post buffer array
                                     * */
                                    String[] wapPostLines = new String[Math.abs(remainingLinesInBuffer)];
                                    String extraLine;
                                    for (int x = 0; x < Math.abs(remainingLinesInBuffer); x++) {
                                        extraLine = mLogcatReader.readLine();
                                        if (extraLine != null) {
                                            wapPostLines[x] = extraLine;
                                        }
                                    }

                                    /**
                                     * We'll add the extra lines to logcat buffer, so we don't miss anything
                                     * on detection cycle continue
                                     * */
                                    int insertCounter = logcatLines.size();
                                    for (String postLine : wapPostLines) {
                                        logcatLines.add(counter + insertCounter, postLine);
                                        insertCounter++;
                                    }
                                }

                                /**
                                 * Will readout from LogcatBuffer remaining lines, or next LOGCAT_WAP_EXTRA_LINES lines
                                 * depending on how many are available
                                 * */
                                int availableLines = Math.min(logcatLines.size() - counter - LOGCAT_WAP_EXTRA_LINES, LOGCAT_WAP_EXTRA_LINES);
                                String[] nextAvailableLines = new String[availableLines];
                                for (int nextLine = 0; nextLine < availableLines; nextLine++) {
                                    nextAvailableLines[nextLine] = logcatLines.get(counter + nextLine);
                                }

                                parseWapPushSms(outLines, nextAvailableLines, MiscUtils.parseLogcatTimeStamp(bufferedLine));
                                break;
                        }
                        counter++;
                    }

                    logcatLines.clear();
                }

            } catch (IOException e) {
                log.error("IO Exception", e);
            } catch (InterruptedException e) {
                log.error("Interrupted Exception", e);
            }
        }

        try {
            mLogcatReader.close();
        } catch (IOException ee) {
            log.error("IOE Error closing BufferedReader", ee);
        }
    }

    private int checkForSms(String line) {

        Realm realm = Realm.getDefaultInstance();


        //0 - null 1 = TYPE0, 2 = MWI, 3 = WAPPUSH
        for (SmsDetectionString detectionString : realm.allObjects(SmsDetectionString.class)) {
            //looping through detection strings to see does logcat line match
            if (line.contains(detectionString.getDetectionString())) {
                if ("TYPE0".equalsIgnoreCase(detectionString.getSmsType())) {
                    log.info("TYPE0 detected");
                    return TYPE0;
                } else if ("MWI".equalsIgnoreCase(detectionString.getSmsType())) {
                    log.info("MWI detected");
                    return MWI;
                } else if ("WAPPUSH".equalsIgnoreCase(detectionString.getSmsType())) {
                    log.info("WAPPUSH detected");
                    return WAP;
                }

            }
            // This is currently unused, but keeping as an example of possible data contents
            // else if (line.contains("BroadcastReceiver action: android.provider.Telephony.SMS_RECEIVED")) {
            // log.info("SMS found");
            // return 0;
            // }
        }
        realm.close();

        return 0;
    }

    private void parseTypeZeroSms(String[] bufferLines, Date logcat_timestamp) {

        @Cleanup Realm realm = Realm.getDefaultInstance();

        long count = realm.where(SmsData.class).equalTo("timestamp", logcat_timestamp).count();
        // Only alert if the timestamp is not in the data base
        if (count == 0) {
            realm.beginTransaction();

            SmsData capturedSms = realm.createObject(SmsData.class);
            String smsText = findSmsData(bufferLines, null);
            String num = findSmsNumber(bufferLines, null);

            capturedSms.setSenderNumber(num);
            capturedSms.setMessage(smsText);
            capturedSms.setTimestamp(logcat_timestamp);
            capturedSms.setType("TYPE0");
            setCurrentLocationData(realm, capturedSms);

            realm.commitTransaction();

            mDbAdapter.toEventLog(realm, 3, "Detected Type-0 SMS");
            startPopUpInfo(SmsType.SILENT);
        } else {
            log.debug("Detected Sms already logged");
        }
    }

    private void parseMwiSms(String[] logcatLines, Date logcat_timestamp) {

        @Cleanup Realm realm = Realm.getDefaultInstance();

        long count = realm.where(SmsData.class).equalTo("timestamp", logcat_timestamp).count();
        // Only alert if the timestamp is not in the data base
        if (count == 0) {
            realm.beginTransaction();

            SmsData capturedSms = realm.createObject(SmsData.class);
            String smsText = findSmsData(logcatLines, null);
            String num = findSmsNumber(logcatLines, null);

            capturedSms.setSenderNumber(num);
            capturedSms.setMessage(smsText);
            capturedSms.setTimestamp(logcat_timestamp);
            capturedSms.setType("MWI");
            setCurrentLocationData(null, capturedSms);

            realm.commitTransaction();

            mDbAdapter.toEventLog(realm, 4, "Detected MWI SMS");
            startPopUpInfo(SmsType.MWI);
        } else {
            log.debug("Detected Sms already logged");
        }
    }

    private void parseWapPushSms(String[] logcatLines, String[] postWapMessageLines, Date logcat_timestamp) {

        @Cleanup Realm realm = Realm.getDefaultInstance();

        long count = realm.where(SmsData.class).equalTo("timestamp", logcat_timestamp).count();
        // Only alert if the timestamp is not in the data base
        if (count == 0) {
            realm.beginTransaction();

            SmsData capturedSms = realm.createObject(SmsData.class);
            String smsText = findSmsData(logcatLines, postWapMessageLines);
            String num = findSmsNumber(logcatLines, postWapMessageLines);

            capturedSms.setSenderNumber(num);
            capturedSms.setMessage(smsText);
            capturedSms.setTimestamp(logcat_timestamp);
            capturedSms.setType("WAPPUSH");
            setCurrentLocationData(realm, capturedSms);

            realm.commitTransaction();

            mDbAdapter.toEventLog(realm, 6, "Detected WAPPUSH SMS");
            startPopUpInfo(SmsType.WAP_PUSH);
        } else {
            log.debug("Detected SMS already logged");
        }
    }

    private void setCurrentLocationData(Realm realm, SmsData capturedSms) {
        capturedSms.setLocationAreaCode(mAIMSICDService.getCellTracker().getMonitorCell().getLocationAreaCode());
        capturedSms.setCellId(mAIMSICDService.getCellTracker().getMonitorCell().getCellId());
        capturedSms.setRadioAccessTechnology(mAIMSICDService.getCell().getRat());
        boolean isRoaming = false;

        if (mAIMSICDService.getCellTracker().getDevice().isRoaming()) {
            isRoaming = true;
        }
        capturedSms.setRoaming(isRoaming);

        GpsLocation gpsLocation = realm.createObject(GpsLocation.class);
        gpsLocation.setLatitude(mAIMSICDService.lastKnownLocation().getLatitudeInDegrees());
        gpsLocation.setLongitude(mAIMSICDService.lastKnownLocation().getLongitudeInDegrees());
        capturedSms.setGpsLocation(gpsLocation);
    }

    private String findSmsData(String[] preBuffer, String[] postBuffer) {
        //check pre buffer for number and sms msg
        if (preBuffer != null) {
            for (String preBufferLine : preBuffer) {
                if (preBufferLine != null) {
                    if (preBufferLine.contains("SMS message body (raw):") && preBufferLine.contains("'")) {
                        preBufferLine = preBufferLine.substring(preBufferLine.indexOf("'") + 1,
                                preBufferLine.length() - 1);
                        return preBufferLine;
                    }
                }
            }
            //check post buffer for number and sms msg
            if (postBuffer != null) {
                for (int x = 0; x < postBuffer.length; x++) {
                    if (postBuffer[x] != null) {
                        String testLine = preBuffer[x];
                        if (testLine.contains("SMS message body (raw):") && testLine.contains("'")) {
                            testLine = testLine.substring(testLine.indexOf("'") + 1,
                                    testLine.length() - 1);
                            return testLine;
                        }
                    }
                }
            }
        }
        return null;
    }

    private String findSmsNumber(String[] preBuffer, String[] postBuffer) {
        //check pre buffer for number and sms msg
        if (preBuffer != null) {
            for (String preBufferLine : preBuffer) {
                if (preBufferLine != null) {
                    if (preBufferLine.contains("SMS originating address:") && preBufferLine.contains("+")) {
                        return preBufferLine.substring(preBufferLine.indexOf("+"));
                    } else if (preBufferLine.contains("OrigAddr")) {
                        preBufferLine = preBufferLine.substring(preBufferLine.indexOf("OrigAddr")).replace("OrigAddr", "").trim();
                        return preBufferLine;
                    }
                }
            }
        }
        //check post buffer for number and sms msg
        if (postBuffer != null) {
            for (String postBufferLine : postBuffer) {
                if (postBufferLine != null) {
                    if (postBufferLine.contains("SMS originating address:") && postBufferLine.contains("+")) {
                        return postBufferLine.substring(postBufferLine.indexOf("+"));
                    } else if (postBufferLine.contains("OrigAddr")) {
                        postBufferLine = postBufferLine.substring(postBufferLine.indexOf("OrigAddr")).replace("OrigAddr", "").trim();
                        return postBufferLine;
                    }
                }
            }

        }
        return null;
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAIMSICDService = ((AimsicdService.AimscidBinder) service).getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            log.info("Disconnected SMS Detection Service");
            mBound = false;
        }
    };

    @Getter
    public enum SmsType {
        SILENT(
                R.string.alert_silent_sms_detected,
                R.string.typezero_header,
                R.string.typezero_data
        ),
        MWI(
                R.string.alert_mwi_detected,
                R.string.typemwi_header,
                R.string.typemwi_data
        ),
        WAP_PUSH(
                R.string.alert_silent_wap_sms_detected,
                R.string.typewap_header,
                R.string.typewap_data
        );

        @StringRes
        private int alert;

        @StringRes
        private int title;

        @StringRes
        private int message;

        SmsType(@StringRes int alert,
                @StringRes int title,
                @StringRes int message) {
            this.alert = alert;
            this.title = title;
            this.message = message;
        }
    }
}

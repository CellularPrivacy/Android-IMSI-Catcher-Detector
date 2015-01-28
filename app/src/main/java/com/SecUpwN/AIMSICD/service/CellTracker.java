package com.SecUpwN.AIMSICD.service;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.WindowManager;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.BuildConfig;
import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.utils.Cell;
import com.SecUpwN.AIMSICD.utils.Device;
import com.SecUpwN.AIMSICD.utils.DeviceApi17;
import com.SecUpwN.AIMSICD.utils.Helpers;
import com.SecUpwN.AIMSICD.utils.Icon;
import com.SecUpwN.AIMSICD.utils.Status;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *  Class to handle tracking of cell information
 *
 *  Description:  TODO: add more info
 *
 *  Issues:
 *
 *  ChangeLog
 *
 */
public class CellTracker implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String TAG = "CellTracker";
    public static String OCID_API_KEY = null; // see getOcidKey()
    private final int NOTIFICATION_ID = 1;

    private static TelephonyManager tm;
    private final SignalStrengthTracker signalStrengthTracker;
    private PhoneStateListener mPhoneStateListener;
    private SharedPreferences prefs;

    public static int PHONE_TYPE;
    public static long REFRESH_RATE;
    public static int LAST_DB_BACKUP_VERSION;
    public static boolean OCID_UPLOAD_PREF;
    public static final String SILENT_SMS = "SILENT_SMS_INTERCEPTED";

    private boolean CELL_TABLE_CLEANSED;
    private final Device mDevice = new Device();

    /*
     * Tracking and Alert Declarations
     */
    private boolean mMonitoringCell;
    private boolean mTrackingCell;
    private boolean mTrackingFemtocell;
    private boolean mFemtoDetected;
    private boolean mChangedLAC;
    private boolean mCellIdNotInOpenDb;
    private Cell mMonitorCell;
    private boolean mTypeZeroSmsDetected;
    private LinkedBlockingQueue<NeighboringCellInfo> neighboringCellBlockingQueue;

    private final AIMSICDDbAdapter dbHelper;
    // TEST to fix toast in OCID api key was:
    // private Context context;
    private static Context context;
    private final Handler timerHandler = new Handler();

    public CellTracker(Context context, SignalStrengthTracker sst) {
        this.context = context;
        this.signalStrengthTracker = sst;
        // TelephonyManager provides system details
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        prefs = context.getSharedPreferences(AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
        prefs.registerOnSharedPreferenceChangeListener(this);
        loadPreferences();
        setNotification();

        PHONE_TYPE = tm.getPhoneType();

        dbHelper = new AIMSICDDbAdapter(context);
        if (!CELL_TABLE_CLEANSED) {
            dbHelper.open();
            dbHelper.cleanseCellTable();
            dbHelper.close();
            SharedPreferences.Editor prefsEditor;
            prefsEditor = prefs.edit();
            prefsEditor.putBoolean(context.getString(R.string.pref_cell_table_cleansed), true);
            prefsEditor.apply();
        }

        mDevice.refreshDeviceInfo(tm, context); //Telephony Manager

        mMonitorCell = new Cell();

        //Register receiver for Silent SMS Interception Notification
        context.registerReceiver(mMessageReceiver, new IntentFilter(SILENT_SMS));
    }

    public boolean isTrackingCell() {
        return mTrackingCell;
    }

    public boolean isMonitoringCell() {
        return mMonitoringCell;
    }

    /**
     * Cell Information Monitoring
     * TODO: What exactly are we monitoring here??
     *
     *
     * @param monitor Enable/Disable monitoring
     */
    public void setCellMonitoring(boolean monitor) {
        if (monitor) {
            timerHandler.postDelayed(timerRunnable, 0);
            mMonitoringCell = true;
            Helpers.msgShort(context, "Monitoring Cell Information.");
        } else {
            timerHandler.removeCallbacks(timerRunnable);
            mMonitoringCell = false;
            Helpers.msgShort(context, "Stopped monitoring Cell Information.");
        }

        setNotification();
    }

    public Device getDevice() {
        return mDevice;
    }

    /**
     * Tracking Femotcell Connections
     *
     * @return boolean indicating Femtocell Connection Tracking State
     */
    public boolean isTrackingFemtocell() {
        return mTrackingFemtocell;
    }

    public void stop() {
        if(isMonitoringCell()) {
            setCellMonitoring(false);
        }
        if(isTrackingCell()){
            setCellTracking(false);
        }
        if(isTrackingFemtocell()){
            stopTrackingFemto();
        }
        cancelNotification();
        tm.listen(mCellSignalListener, PhoneStateListener.LISTEN_NONE);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        context.unregisterReceiver(mMessageReceiver);

    }

    /**
     * Cell Information Tracking and database logging
     *
     * TODO: This is the "Tracking cell information" that need better explanation
     * and clarification. (What exactly are we "tracking" here? The GPS, the LAC/CID,
     * and where doe it go?)
     *
     *
     * @param track Enable/Disable tracking
     */
    public void setCellTracking(boolean track) {
        if (track) {
            tm.listen(mCellSignalListener,
                    PhoneStateListener.LISTEN_CELL_LOCATION |
                    PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |
                    PhoneStateListener.LISTEN_DATA_ACTIVITY |
                    PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
            );
            mTrackingCell = true;
            Helpers.msgShort(context, "Tracking Cell Information.");
        } else {
            tm.listen(mCellSignalListener, PhoneStateListener.LISTEN_NONE);
            mDevice.mCell.setLon(0.0);
            mDevice.mCell.setLat(0.0);
            mDevice.setCellInfo("[0,0]|nn|nn|");
            mTrackingCell = false;
            Helpers.msgShort(context, "Stopped tracking Cell Information.");
        }
        setNotification();
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Bundle bundle = intent.getExtras();
            if (bundle != null) {
                dbHelper.open();
                dbHelper.insertSilentSms(bundle);
                dbHelper.close();
                setSilentSmsStatus(true);
            }
        }
    };

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final String KEY_UI_ICONS = context.getString(R.string.pref_ui_icons_key);
        final String FEMTO_DECTECTION = context.getString(R.string.pref_femto_detection_key);
        final String REFRESH = context.getString(R.string.pref_refresh_key);        // Refresh rate of ??? in [seconds]?
        final String DB_VERSION = context.getString(R.string.pref_last_database_backup_version);
        final String OCID_UPLOAD = context.getString(R.string.pref_ocid_upload);    // BOOLEAN to enable OCID data upload
        final String OCID_KEY = context.getString(R.string.pref_ocid_key);

        if (key.equals(KEY_UI_ICONS)) {
            //Update Notification to display selected icon type
            setNotification();
        } else if (key.equals(FEMTO_DECTECTION)) {
            boolean trackFemtoPref = sharedPreferences.getBoolean(FEMTO_DECTECTION, false);
            if (trackFemtoPref) {
                startTrackingFemto();
            } else {
                stopTrackingFemto();
            }
        } else if (key.equals(REFRESH)) {
            String refreshRate = sharedPreferences.getString(REFRESH, "1");
            if (refreshRate.isEmpty()) {
                refreshRate = "1";
            }

            int rate = Integer.parseInt(refreshRate);
            long t;
            switch (rate) {
                case 1:
                    t = 15L;
                    break;
                default:
                    t = (rate * 1L);
                    break;
            }
            REFRESH_RATE = TimeUnit.SECONDS.toMillis(t);
        } else if (key.equals(DB_VERSION)) {
            LAST_DB_BACKUP_VERSION = sharedPreferences.getInt(DB_VERSION, 1);
        } else if (key.equals(OCID_UPLOAD)) {
            OCID_UPLOAD_PREF = sharedPreferences.getBoolean(OCID_UPLOAD, false);
        } else if (key.equals(OCID_KEY)) {
            getOcidKey();
        }
    }

    public void getOcidKey() {
        final String OCID_KEY = context.getString(R.string.pref_ocid_key);
        OCID_API_KEY = prefs.getString(OCID_KEY, BuildConfig.OPEN_CELLID_API_KEY);
        if (OCID_API_KEY == null) {
            OCID_API_KEY = "NA"; // avoid null api key
        }
    }

    /**
     * Get an API key for Open Cell ID. Do not call this from the UI/Main thread.
     *
     * @return null or newly generated key
     *
     * TODO: We need to check for better HTTP request error handling.
     * See: https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/267
     */
    public static String requestNewOCIDKey() throws Exception {
        String responseFromServer = null;
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://opencellid.org/gsmCell/user/generateApiKey");
        HttpResponse result = httpclient.execute(httpGet);
        StatusLine status = result.getStatusLine();

        if (status.getStatusCode() == 200) {
            if (result.getEntity() != null) {
                InputStream is = result.getEntity().getContent();
                ByteArrayOutputStream content = new ByteArrayOutputStream();
                // Read response into a buffered stream
                int readBytes = 0;
                byte[] sBuffer = new byte[4096];
                while ((readBytes = is.read(sBuffer)) != -1) {
                    content.write(sBuffer, 0, readBytes);
                }
                responseFromServer = content.toString("UTF-8");
                result.getEntity().consumeContent();
            }
            Log.d("OCID", responseFromServer);
            return responseFromServer;

        } else if (status.getStatusCode() == 503) {
        // Check for HTTP error code 503 which is returned when user is trying to request
        // a new API key within 24 hours of the last request.
        // Make toast message:  "Only one new API key request per 24 hours. Please try again later."

            Helpers.msgLong(context,
                    "Only one new API key request per 24 hours!\nPlease try again later.");
            if (result.getEntity() != null) {
                InputStream is = result.getEntity().getContent();
                ByteArrayOutputStream content = new ByteArrayOutputStream();
                // Read response into a buffered stream
                int readBytes = 0;
                byte[] sBuffer = new byte[4096];
                while ((readBytes = is.read(sBuffer)) != -1) {
                    content.write(sBuffer, 0, readBytes);
                }
                responseFromServer = content.toString("UTF-8");
                result.getEntity().consumeContent();
            }
            Log.d("OCID Reached 24hr API key limit:", responseFromServer);
            return responseFromServer;

        } else {

            // TODO add code here or elsewhere to check for NO network exceptions...
            // See: https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/293
            httpclient = null;
            httpGet = null;
            result = null;

            Log.d("OCID", "OCID Returned " + status.getStatusCode() + " " + status.getReasonPhrase());
            throw new Exception("OCID Returned " + status.getStatusCode() + " " + status.getReasonPhrase());
        }
    }


    /**
     * Updates Neighbouring Cell details
     */
    public List<Cell> updateNeighbouringCells() {

        List<Cell> neighboringCells = new ArrayList<>();

        List<NeighboringCellInfo> neighboringCellInfo;
        neighboringCellInfo = tm.getNeighboringCellInfo();
        if (neighboringCellInfo.size() == 0) {
            // try to poll the neighboring cells for a few seconds
            neighboringCellBlockingQueue = new LinkedBlockingQueue<>(100);
            Log.i(TAG, "neighbouringCellInfo empty - start polling");
            //Log.i(TAG, "signal: "+mDevice.getSignalDBm());

            //LISTEN_CELL_INFO added in API 17
            if (Build.VERSION.SDK_INT > 16) {
                DeviceApi17.startListening(tm, phoneStatelistener);
            } else {
                tm.listen(phoneStatelistener,
                        PhoneStateListener.LISTEN_CELL_LOCATION |
                        PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
                        PhoneStateListener.LISTEN_SERVICE_STATE |
                        PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            }

            for (int i = 0; i < 10 && neighboringCellInfo.size() == 0; i++) {
                try {
                    Log.i(TAG, "neighbouringCellInfo empty: trying " + i);
                    NeighboringCellInfo info = neighboringCellBlockingQueue.poll(1, TimeUnit.SECONDS);
                    if (info == null) {
                        neighboringCellInfo = tm.getNeighboringCellInfo();
                        if (neighboringCellInfo.size() > 0) {
                            // Can we think of a better log message here?
                            Log.i(TAG, "neighbouringCellInfo found on " + i + " try. (time based)");
                            break;
                        } else {
                            continue;
                        }
                    }
                    ArrayList<NeighboringCellInfo> cellInfoList =
                            new ArrayList<>(neighboringCellBlockingQueue.size() + 1);
                    while (info != null) {
                        cellInfoList.add(info);
                        info = neighboringCellBlockingQueue.poll(1, TimeUnit.SECONDS);
                    }
                    neighboringCellInfo = cellInfoList;
                } catch (InterruptedException e) {
                    // Maybe a more valuable message here?
                    // normal
                }
            }
        }

        Log.i(TAG, "neighbouringCellInfo size: " + neighboringCellInfo.size());
        for (NeighboringCellInfo neighbourCell : neighboringCellInfo) {
            Log.i(TAG,
                    "neighbouringCellInfo -" +
                    " LAC:" + neighbourCell.getLac() +
                    " CID:" + neighbourCell.getCid() +
                    " PSC:" + neighbourCell.getPsc() +
                    " RSSI:" + neighbourCell.getRssi() );

            final Cell cell = new Cell(
                    neighbourCell.getCid(),
                    neighbourCell.getLac(),
                    neighbourCell.getRssi(),
                    neighbourCell.getPsc(),
                    neighbourCell.getNetworkType(), false);
            neighboringCells.add(cell);
        }
        return neighboringCells;
    }

    private void handlePhoneStateChange() {
        List<NeighboringCellInfo> neighboringCellInfo;
        neighboringCellInfo = tm.getNeighboringCellInfo();
        if (neighboringCellInfo.size() == 0) {
            return;
        }
        // Does this make sense? Is it empty or not?
        Log.i(TAG, "neighbouringCellInfo empty - event based polling succeeded!");
        tm.listen(phoneStatelistener, PhoneStateListener.LISTEN_NONE);
        neighboringCellBlockingQueue.addAll(neighboringCellInfo);
    }

    public void refreshDevice() {
        mDevice.refreshDeviceInfo(tm, context);
    }

    /**
     * Process User Preferences
     *
     * Description:  TODO: add more info
     *
     *      Handles the Preferences as set in the XXXXX file
     *
     *
     */
    private void loadPreferences() {
        boolean trackFemtoPref  = prefs.getBoolean( context.getString(R.string.pref_femto_detection_key), false);
        boolean trackCellPref   = prefs.getBoolean( context.getString(R.string.pref_enable_cell_key), true);
        boolean monitorCellPref = prefs.getBoolean( context.getString(R.string.pref_enable_cell_monitoring_key), true);

        LAST_DB_BACKUP_VERSION  = prefs.getInt(     context.getString(R.string.pref_last_database_backup_version), 1);
        OCID_UPLOAD_PREF        = prefs.getBoolean( context.getString(R.string.pref_ocid_upload), false);
        CELL_TABLE_CLEANSED     = prefs.getBoolean( context.getString(R.string.pref_cell_table_cleansed), false);

        // TODO: Explain what this rate is doing...
        String refreshRate      = prefs.getString(  context.getString(R.string.pref_refresh_key), "1");
        if (refreshRate.isEmpty()) { refreshRate = "1";  }

        int rate = Integer.parseInt(refreshRate);
        long t;
        switch (rate) {
            case 1:
                t = 15L;
                break;
            default:
                t = (rate * 1L);
                break;
        }

        REFRESH_RATE = TimeUnit.SECONDS.toMillis(t);
        getOcidKey();

        if (trackFemtoPref) {   startTrackingFemto(); }
        if (trackCellPref) {    setCellTracking(true); }
        if (monitorCellPref) {  setCellMonitoring(true); }
    }


    /**
     * Description:  TODO: add more info
     *
     *    This SEEM TO add entries to the "locationinfo" DB table in the??
     *
     *    Issues:
     *
     *    [ ] We see that "Connection" items are messed up. What is the purpose of these?
     *
     *    $ sqlite3.exe -header -csv aimsicd.db 'select * from locationinfo;'
     *      _id,Lac,CellID,Net,Lat,Lng,Signal,Connection,Timestamp
     *      1,10401,6828111,10, 54.6787,25.2869, 24, "[10401,6828111,126]No|Di|HSPA|", "2015-01-21 20:45:10"
     *
     *    [ ]
     *
     *    ChangeLog:
     *
     *          2015-01-22  E:V:A   Changed what appears to be a typo in the character
     *                              following getNetworkTypeName(), "|" to "]"
     *
     */
    private final PhoneStateListener mCellSignalListener = new PhoneStateListener() {
        public void onCellLocationChanged(CellLocation location) {
            mDevice.setNetID(tm);           // ??
            mDevice.getNetworkTypeName();   // RAT??

            switch (mDevice.getPhoneID()) {

                case TelephonyManager.PHONE_TYPE_GSM:
                    GsmCellLocation gsmCellLocation = (GsmCellLocation) location;
                    if (gsmCellLocation != null) {
                        mDevice.setCellInfo(
                                gsmCellLocation.toString() +
                                mDevice.getDataActivityTypeShort() + "|" +  // ??
                                mDevice.getDataStateShort() + "|" +         // ??
                                mDevice.getNetworkTypeName() + "]"          // ??
                        );
                        mDevice.mCell.setLAC(gsmCellLocation.getLac());     // LAC
                        mDevice.mCell.setCID(gsmCellLocation.getCid());     // CID
                        if (gsmCellLocation.getPsc() != -1)
                            mDevice.mCell.setPSC(gsmCellLocation.getPsc()); // PSC

                    }
                    break;

                case TelephonyManager.PHONE_TYPE_CDMA:
                    CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) location;
                    if (cdmaCellLocation != null) {
                        mDevice.setCellInfo(
                                cdmaCellLocation.toString() +               // ??
                                mDevice.getDataActivityTypeShort() + "|" +  // ??
                                mDevice.getDataStateShort() + "|" +         // ??
                                mDevice.getNetworkTypeName() + "]"          // ??
                        );
                        mDevice.mCell.setLAC(cdmaCellLocation.getNetworkId());      // NID
                        mDevice.mCell.setCID(cdmaCellLocation.getBaseStationId());  // BID
                        mDevice.mCell.setSID(cdmaCellLocation.getSystemId());       // SID
                        mDevice.mCell.setMNC(cdmaCellLocation.getSystemId());   // <== BUG!?    // MNC
                        mDevice.setNetworkName(tm.getNetworkOperatorName());        // ??
                    }
            }

        }

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            // Update Signal Strength
            if (signalStrength.isGsm()) {
                int dbm;
                if(signalStrength.getGsmSignalStrength() <= 2 ||
                   signalStrength.getGsmSignalStrength() ==  NeighboringCellInfo.UNKNOWN_RSSI)
                {
                    // Unknown signal strength, get it another way
                    String[] bits = signalStrength.toString().split(" ");
                    dbm = Integer.parseInt(bits[9]);
                } else {
                    dbm = signalStrength.getGsmSignalStrength();
                }
                mDevice.setSignalDbm(dbm);
            } else {
                int evdoDbm = signalStrength.getEvdoDbm();
                int cdmaDbm = signalStrength.getCdmaDbm();

                // Use lowest signal to be conservative
                mDevice.setSignalDbm((cdmaDbm < evdoDbm) ? cdmaDbm : evdoDbm);
            }
            // Send it to signal tracker
            signalStrengthTracker.registerSignalStrength(mDevice.mCell.getCID(), mDevice.getSignalDBm());
            //signalStrengthTracker.isMysterious(mDevice.mCell.getCID(), mDevice.getSignalDBm());
        }

        public void onDataActivity(int direction) {
            switch (direction) {
                case TelephonyManager.DATA_ACTIVITY_NONE:
                    mDevice.setDataActivityTypeShort("No");
                    mDevice.setDataActivityType("None");
                    break;
                case TelephonyManager.DATA_ACTIVITY_IN:
                    mDevice.setDataActivityTypeShort("In");
                    mDevice.setDataActivityType("In");
                    break;
                case TelephonyManager.DATA_ACTIVITY_OUT:
                    mDevice.setDataActivityTypeShort("Ou");
                    mDevice.setDataActivityType("Out");
                    break;
                case TelephonyManager.DATA_ACTIVITY_INOUT:
                    mDevice.setDataActivityTypeShort("IO");
                    mDevice.setDataActivityType("In-Out");
                    break;
                case TelephonyManager.DATA_ACTIVITY_DORMANT:
                    mDevice.setDataActivityTypeShort("Do");
                    mDevice.setDataActivityType("Dormant");
                    break;
            }
        }

        public void onDataConnectionStateChanged(int state) {
            switch (state) {
                case TelephonyManager.DATA_DISCONNECTED:
                    mDevice.setDataState("Disconnected");
                    mDevice.setDataStateShort("Di");
                    break;
                case TelephonyManager.DATA_CONNECTING:
                    mDevice.setDataState("Connecting");
                    mDevice.setDataStateShort("Ct");
                    break;
                case TelephonyManager.DATA_CONNECTED:
                    mDevice.setDataState("Connected");
                    mDevice.setDataStateShort("Cd");
                    break;
                case TelephonyManager.DATA_SUSPENDED:
                    mDevice.setDataState("Suspended");
                    mDevice.setDataStateShort("Su");
                    break;
            }
        }

    };

    void setSilentSmsStatus(boolean state) {
        mTypeZeroSmsDetected = state;
        setNotification();
        if (state) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.sms_message).setTitle(R.string.sms_title);
            AlertDialog alert = builder.create();
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alert.show();
            mTypeZeroSmsDetected = false;
        }
    }

    /**
     * Description:  TODO: add more info
     *
     *    This SEEM TO add entries to the "locationinfo" DB table ???
     *
     * From "locationinfo":
     *
     *      $ sqlite3.exe -header aimsicd.db 'select * from locationinfo;'
     *      _id|Lac|CellID|Net|Lat|Lng|Signal|Connection|Timestamp
     *      1|10401|6828xxx|10|54.67874392|25.28693531|24|[10401,6828320,126]No|Di|HSPA||2015-01-21 20:45:10
     *
     *  From "cellinfo":
     *
     *      $ sqlite3.exe -header aimsicd.db 'select * from cellinfo;'
     *      _id|Lac|CellID|Net|Lat|Lng|Signal|Mcc|Mnc|Accuracy|Speed|Direction|NetworkType|MeasurementTaken|OCID_SUBMITTED|Timestamp
     *      1|10401|6828xxx|10|54.67874392|25.28693531|24|246|2|69.0|0.0|0.0|HSPA|82964|0|2015-01-21 20:45:10
     *
     *    Issues:
     */
     public void onLocationChanged(Location loc) {

        if (Build.VERSION.SDK_INT > 16) {
            DeviceApi17.loadCellInfo(tm, mDevice.mCell);
        }

        if (!mDevice.mCell.isValid()) {
            CellLocation cellLocation = tm.getCellLocation();
            if (cellLocation != null) {
                switch (mDevice.getPhoneID()) {
                    case TelephonyManager.PHONE_TYPE_GSM:
                        GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;
                        mDevice.mCell.setCID(gsmCellLocation.getCid()); // CID
                        mDevice.mCell.setLAC(gsmCellLocation.getLac()); // LAC
                        mDevice.mCell.setPSC(gsmCellLocation.getPsc()); // PSC
                        break;
                    case TelephonyManager.PHONE_TYPE_CDMA:
                        CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cellLocation;
                        mDevice.mCell.setCID(cdmaCellLocation.getBaseStationId()); // BSID ??
                        mDevice.mCell.setLAC(cdmaCellLocation.getNetworkId());     // NID
                        mDevice.mCell.setSID(cdmaCellLocation.getSystemId());      // SID
                        mDevice.mCell.setMNC(cdmaCellLocation.getSystemId()); // <== BUG!??     // MNC
                }
            }
        }

        if (loc != null && (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0)) {
            mDevice.mCell.setLon(loc.getLongitude());       // gpsd_lon
            mDevice.mCell.setLat(loc.getLatitude());        // gpsd_lat
            mDevice.mCell.setSpeed(loc.getSpeed());         // speed
            mDevice.mCell.setAccuracy(loc.getAccuracy());   // gpsd_accu
            mDevice.mCell.setBearing(loc.getBearing());     // -- [deg]??
            mDevice.setLastLocation(loc);                   //

            //Store last known location in preference
            SharedPreferences.Editor prefsEditor;
            prefsEditor = prefs.edit();
            prefsEditor.putString(context.getString(R.string.data_last_lat_lon),
                    String.valueOf(loc.getLatitude()) + ":" +
                    String.valueOf(loc.getLongitude()));
            prefsEditor.apply();

            if (mTrackingCell) {
                dbHelper.open();

                // LOCATION_TABLE (locationinfo)    ==>  DBi_measure + DBi_bts
                dbHelper.insertLocation(
                        mDevice.mCell.getLAC(),     // Lac
                        mDevice.mCell.getCID(),     // CellID
                        mDevice.mCell.getNetType(), // Net
                        mDevice.mCell.getLat(),     // Lat
                        mDevice.mCell.getLon(),     // Lng
                        mDevice.mCell.getDBM(),     // Signal
                        mDevice.getCellInfo()       // Connection
                );

                // CELL_TABLE (cellinfo))           ==>  DBi_measure + DBi_bts
                dbHelper.insertCell(
                        mDevice.mCell.getLAC(),     // Lac
                        mDevice.mCell.getCID(),     // CellID
                        mDevice.mCell.getNetType(), // Net
                        mDevice.mCell.getLat(),     // Lat
                        mDevice.mCell.getLon(),     // Lng
                        mDevice.mCell.getDBM(),     // Signal
                        mDevice.mCell.getMCC(),     // Mcc
                        mDevice.mCell.getMNC(),     // Mnc
                        mDevice.mCell.getAccuracy(),// Accuracy
                        mDevice.mCell.getSpeed(),   // Speed
                        mDevice.mCell.getBearing(), // Direction
                        mDevice.getNetworkTypeName(), // NetworkType
                        SystemClock.currentThreadTimeMillis() // MeasurementTaken
                );
                dbHelper.close();
            }
        }
    }

    /**
     * Cancel and remove the persistent notification
     */
    public void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    /**
     * Set or update the Detection/Status Notification
     *
     *  Description:    TODO: Please add details!
     *
     *  Issues:
     *
     *  [ ] TODO: Seem we're missing the other colors here: ORANGE and BLACK (skull)
     *      See:  https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Status-Icons
     *      and:  https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/11#issuecomment-44670204
     *
     *      Change names from "IDLE,NORMAL,MEDIUM,ALARM" to:"GRAY,GREEN,YELLOW,ORANGE,RED,BLACK",
     *      to reflect detection Icon colors. They should be based on the detection scores here:
     *      <TBA>
     *
     *  [ ] We need to standardize the "contentText" and "tickerText" format
     *
     *  [ ] From #91: https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/91
     *
     *      Problem:
     *          Having multiple notifications will cause an issue with
     *      notifications themselves AND tickerText.  It seems that the
     *      most recent notification raised would overwrite any previous,
     *      notification or tickerText.  This results in loss of information
     *      for any notification before the last one.
     *
     *      Possible Solution:
     *          Perhaps arranging a queue implementation to deal with text
     *      being passed into tickerText only when any previous text has
     *      been entirely displayed.
     *
     *
     *  Dependencies:    Status.java, CellTracker.java, Icon.java ( + others?)
     *
     *  ChangeLog:
     *
     *     2015-01-22   E:V:A  Added placeholder for "Missing Neighboring Cells Alert"
     *
     *
     */
    void setNotification() {
        String tickerText;
        String contentText = "Phone Type " + mDevice.getPhoneType();

        if (mFemtoDetected || mTypeZeroSmsDetected) {
            Status.setCurrentStatus(Status.Type.ALARM, this.context);
        } else if (mChangedLAC) {
            Status.setCurrentStatus(Status.Type.MEDIUM, this.context);
            contentText = "Hostile Service Area: Changing LAC Detected!";
        } else if(mCellIdNotInOpenDb){
            Status.setCurrentStatus(Status.Type.MEDIUM, this.context);
            contentText = "Cell ID does not exist in OpenCellID Database!";
        } else if (mTrackingFemtocell || mTrackingCell || mMonitoringCell) {
            Status.setCurrentStatus(Status.Type.NORMAL, this.context);
            if (mTrackingFemtocell) {
                contentText = "FemtoCell Detection Active.";
            } else if (mTrackingCell) {
                contentText = "Cell Tracking Active.";
            } else {
                contentText = "Cell Monitoring Active.";
            }
        } else {
            Status.setCurrentStatus(Status.Type.IDLE, this.context);
        }

        switch (Status.getStatus()) {
            case IDLE: // GRAY
                contentText = "Phone Type " + mDevice.getPhoneType();
                tickerText = context.getResources().getString(R.string.app_name_short) + " - Status: Idle.";
                break;

            case NORMAL: // GREEN
                tickerText = context.getResources().getString(R.string.app_name_short) + " - Status: Good. No Threats Detected.";
                break;

            case MEDIUM: // YELLOW
                // Initialize tickerText as the app name string
                // See multiple detection comments above.
                tickerText = context.getResources().getString(R.string.app_name_short);
                if (mChangedLAC) {
                    //Append changing LAC text
                    tickerText += " - Hostile Service Area: Changing LAC Detected!";
                    contentText = "Hostile Service Area: Changing LAC Detected!";
                // See #264
                //} else if (NoNCL)  {
                //    tickerText += " - BTS doesn't provide any neighbors!";
                //    contentText = "CID: " + cellid + " is not providing a neighboring cell list!";

                } else if (mCellIdNotInOpenDb) {
                    //Append Cell ID not existing in external db text
                    tickerText += " - Cell ID does not exist in OpenCellID Database!";
                    contentText = "Cell ID does not exist in OpenCellID Database!";
                }
                break;

            case ALARM: // ORANGE, RED or BLACK ?
                tickerText = context.getResources().getString(R.string.app_name_short) + " - ALERT: Some Threat Detected!"; // Hmm, this is vague!
                if (mFemtoDetected) {
                    contentText = "ALERT: FemtoCell Connection Detected!";
                } else if (mTypeZeroSmsDetected) {
                    contentText = "ALERT: Type-0 Silent SMS Intercepted!";
                }

                break;
            default:
                tickerText = context.getResources().getString(R.string.app_name);
                break;
        }

        // TODO: Explanation (see above)
        Intent notificationIntent = new Intent(context, AIMSICD.class);
        notificationIntent.putExtra("silent_sms", mTypeZeroSmsDetected);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_FROM_BACKGROUND);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        String iconType = prefs.getString(context.getString(R.string.pref_ui_icons_key), "SENSE").toUpperCase();
        Notification mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(Icon.getIcon(Icon.Type.valueOf(iconType)))
                        .setTicker(tickerText)
                        .setContentTitle(context.getResources().getString(R.string.app_name))
                        .setContentText(contentText)
                        .setOngoing(true)
                        .setAutoCancel(false)
                        .setContentIntent(contentIntent)
                        .build();
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder);
    }


    /**
     * Description:     TODO: Please add
     *
     * Variables:
     *                  There is a "timer" here (REFRESH_RATE), what exactly is it timing?
     *
     */
    private final Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            switch (mDevice.getPhoneID()) {
                case TelephonyManager.PHONE_TYPE_GSM:
                    GsmCellLocation gsmCellLocation = (GsmCellLocation) tm.getCellLocation();
                    if (gsmCellLocation != null) {
                        mMonitorCell.setLAC(gsmCellLocation.getLac());
                        mMonitorCell.setCID(gsmCellLocation.getCid());
                        dbHelper.open();
                        boolean lacOK = dbHelper.checkLAC(mMonitorCell);
                        if (!lacOK) {
                            mChangedLAC = true;
                            setNotification();
                        } else {
                            mChangedLAC = false;
                        }
                        //Check if CellID (CID) is in DBe_import (OpenCell) database (issue #91)
                        if (!dbHelper.openCellExists(mMonitorCell.getCID())){
                            mCellIdNotInOpenDb = true;
                            setNotification();
                        } else {
                            mCellIdNotInOpenDb = false;
                        }
                        dbHelper.close();
                    }
                    break;
                case TelephonyManager.PHONE_TYPE_CDMA:
                    CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) tm.getCellLocation();
                    if (cdmaCellLocation != null) {
                        mMonitorCell.setLAC(cdmaCellLocation.getNetworkId());
                        mMonitorCell.setCID(cdmaCellLocation.getBaseStationId());
                        dbHelper.open();
                        boolean lacOK = dbHelper.checkLAC(mMonitorCell);
                        if (!lacOK) {
                            mChangedLAC = true;
                            setNotification();
                        } else {
                            mChangedLAC = false;
                        }
                        dbHelper.close();
                    }
            }


            if (REFRESH_RATE != 0) {
                timerHandler.postDelayed(this, REFRESH_RATE);
            } else {
                //Default to 25 seconds refresh rate
                timerHandler.postDelayed(this, TimeUnit.SECONDS.toMillis(25));
            }
        }
    };


//=================================================================================================
    /**
     * The below code section was copied and modified with permission from
     * Femtocatcher https://github.com/iSECPartners/femtocatcher
     *
     * Copyright (C) 2013 iSEC Partners
     */

    /**
     * Start FemtoCell detection tracking (For CDMA Devices ONLY!)
     */
    public void startTrackingFemto() {

        /* Check if it is a CDMA phone */
        if (mDevice.getPhoneID() != TelephonyManager.PHONE_TYPE_CDMA) {
            Helpers.msgShort(context, "AIMSICD can only detect FemtoCell connections on CDMA devices.");
            return;
        }

        mTrackingFemtocell = true;
        mPhoneStateListener = new PhoneStateListener() {
            public void onServiceStateChanged(ServiceState s) {
                Log.d(TAG, "Service State changed!");
                getServiceStateInfo(s);
            }
        };
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);
        setNotification();
    }

    /**
     * Stop FemtoCell detection tracking (For CDMA Devices ONLY!)
     */
    public void stopTrackingFemto() {
        if (mPhoneStateListener != null) {
            tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
            mTrackingFemtocell = false;
            setNotification();
            Log.v(TAG, "Stopped tracking FemtoCell connections.");
        }
    }

    private void getServiceStateInfo(ServiceState s) {
        if (s != null) {
            if (IsConnectedToCdmaFemto(s)) {
                Helpers.msgShort(context, "ALERT!! FemtoCell Connection Detected!!");
                mFemtoDetected = true;
                setNotification();
                //toggleRadio();
            } else {
                mFemtoDetected = false;
                setNotification();
            }
        }
    }

    private boolean IsConnectedToCdmaFemto(ServiceState s) {
        if (s == null) {
            return false;
        }

        /* Get International Roaming indicator
         * if indicator is not 0 return false
         */

        /* Get the radio technology */
        int networkType = mDevice.mCell.getNetType();

        /* Check if it is EvDo network */
        boolean evDoNetwork = isEvDoNetwork(networkType);

        /* If it is not an evDo network check the network ID range.
         * If it is connected to Femtocell, the NID should be between [0xfa, 0xff)
         */
        if (!evDoNetwork) {
            /* get network ID */
            if (tm != null) {
                CdmaCellLocation c = (CdmaCellLocation) tm.getCellLocation();

                if (c != null) {
                    int networkID = c.getNetworkId();
                    int FEMTO_NID_MAX = 0xff;
                    int FEMTO_NID_MIN = 0xfa;
                    return !((networkID < FEMTO_NID_MIN) || (networkID >= FEMTO_NID_MAX));

                } else {
                    Log.v(TAG, "Cell location info is null.");
                    return false;
                }
            } else {
                Log.v(TAG, "Telephony Manager is null.");
                return false;
            }
        }

        /* if it is an evDo network */
        // TODO
        else {
            /* get network ID */
            if (tm != null) {
                CdmaCellLocation c = (CdmaCellLocation) tm.getCellLocation();

                if (c != null) {
                    int networkID = c.getNetworkId();

                    int FEMTO_NID_MAX = 0xff;
                    int FEMTO_NID_MIN = 0xfa;
                    return !((networkID < FEMTO_NID_MIN) || (networkID >= FEMTO_NID_MAX));
                } else {
                    Log.v(TAG, "Cell location info is null.");
                    return false;
                }
            } else {
                Log.v(TAG, "Telephony Manager is null.");
                return false;
            }
        }

    }

    /**
     * Confirmation of connection to an EVDO Network
     *
     * @return EVDO network connection returns TRUE
     */
    private boolean isEvDoNetwork(int networkType) {
        return (networkType == TelephonyManager.NETWORK_TYPE_EVDO_0) ||
               (networkType == TelephonyManager.NETWORK_TYPE_EVDO_A) ||
               (networkType == TelephonyManager.NETWORK_TYPE_EVDO_B) ||
               (networkType == TelephonyManager.NETWORK_TYPE_EHRPD);
    }
    /*
     * The above code section was copied and modified from
     * Femtocatcher https://github.com/iSECPartners/femtocatcher
     *
     * Copyright (C) 2013 iSEC Partners
     *
     */
//=================================================================================================


    final PhoneStateListener phoneStatelistener = new PhoneStateListener() {
        private void handle() {
            handlePhoneStateChange();
        }
        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            handle();
        }
        @Override
        public void onDataConnectionStateChanged(int state) {
            handle();
        }
        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            handle();
        }
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            handle();
        }
        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            handle();
        }
    };

    /**
     * Getter for use in tests only
     * TODO: What tests?
     */
    public Cell getMonitorCell() {
        return mMonitorCell;
    }
}

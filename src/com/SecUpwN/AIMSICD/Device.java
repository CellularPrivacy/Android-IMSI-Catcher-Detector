package com.SecUpwN.AIMSICD;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Device {

    private static String TAG = "AIMSICD_Device";

    private static String LOCATION_TABLE = "locationinfo";
    private static String CELL_TABLE = "cellinfo";
    private static String SIGNAL_TABLE = "signalinfo";
    private static String DB_NAME = "myCellInfo";

    private static PhoneStateListener sSignalListenerStrength;
    private static PhoneStateListener sSignalListenerLocation;
    private static LocationManager lm;
    private static LocationListener sLocationListener;
    private static SQLiteDatabase sDB;
    private static MySQLiteHelper dbHelper;


    private static int sPhoneID;
    private static int sSignalInfo;
    private static int sDataActivity;
    private static int sNetID;
    private static int sLacID;
    private static int sCellID;
    private static double sLongitude;
    private static double sLatitude;
    private static String sCSV, sNetType, sCellInfo, sDataState;
    private static String sKML, sPhoneNum, sCellType, sLac, sNetName, sMmcmcc, sSimCountry, sPhoneType;
    private static String sIMEI, sIMEIV, sSimOperator, sSimOperatorName, sSimSerial, sSimSubs, sDataActivityType;

    private static boolean TrackingCell;
    private static boolean TrackingSignal;
    private static boolean TrackingLocation;

    private static ArrayList<String> alPosition;

    private static TelephonyManager tm;



    public static void InitDevice(Context mContext) {
        //TelephonyManager provides system details
        tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

        //Phone type and associated details
        sIMEI = tm.getDeviceId();
        sIMEIV = tm.getDeviceSoftwareVersion();
        sPhoneNum = tm.getLine1Number();
        sPhoneID = tm.getPhoneType();
        switch (sPhoneID) {
            case TelephonyManager.PHONE_TYPE_GSM:
                sPhoneType = "GSM";
                sMmcmcc = tm.getNetworkOperator();
                sNetName = tm.getNetworkOperatorName();
                GsmCellLocation gsmCellLocation = (GsmCellLocation) tm.getCellLocation();
                if (gsmCellLocation != null) {
                    sCellType = "" + gsmCellLocation.getCid();
                    sLac = "" + gsmCellLocation.getLac();
                }
                sSimCountry = tm.getSimCountryIso();
                sSimOperator = tm.getSimOperator();
                sSimOperatorName = tm.getSimOperatorName();
                sSimSerial = tm.getSimSerialNumber();
                sSimSubs = tm.getSubscriberId();
                break;
            case TelephonyManager.PHONE_TYPE_CDMA:
                sPhoneType = "CDMA";
                break;
        }

        //Network type
        sNetID = tm.getNetworkType();
        switch (sNetID) {
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                sNetType = "Unknown";
                break;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                sNetType = "GPRS";
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                sNetType = "EDGE";
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                sNetType = "UMTS";
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                sNetType = "HSPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                sNetType = "HDSPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                sNetType = "HUSPA";
                break;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                sNetType = "CDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                sNetType = "EVDO_0";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                sNetType = "EVDO_A";
                break;
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                sNetType = "1xRTT";
                break;
            default:
                sNetType = "Unknown";
                break;
        }

        sDataActivity = tm.getDataActivity();
        sDataActivityType = "undef";
        switch (sDataActivity) {
            case TelephonyManager.DATA_ACTIVITY_NONE:
                sDataActivityType = "None";
                break;
            case TelephonyManager.DATA_ACTIVITY_IN:
                sDataActivityType = "In";
                break;
            case TelephonyManager.DATA_ACTIVITY_OUT:
                sDataActivityType = "Out";
                break;
            case TelephonyManager.DATA_ACTIVITY_INOUT:
                sDataActivityType = "In-Out";
                break;
            case TelephonyManager.DATA_ACTIVITY_DORMANT:
                sDataActivityType = "Dormant";
                break;
        }

        sDataActivity = tm.getDataState();
        sDataState = "undef";
        switch (sDataActivity) {
            case TelephonyManager.DATA_DISCONNECTED:
                sDataActivityType = "Disconnected";
                break;
            case TelephonyManager.DATA_CONNECTING:
                sDataActivityType = "Connecting";
                break;
            case TelephonyManager.DATA_CONNECTED:
                sDataActivityType = "Connected";
                break;
            case TelephonyManager.DATA_SUSPENDED:
                sDataActivityType = "Suspended";
                break;
        }

        //Create DB Instance
        dbHelper = new MySQLiteHelper(mContext);
        sDB = dbHelper.getWritableDatabase();

        sSignalListenerLocation = new PhoneStateListener() {
            public void onCellLocationChanged(CellLocation location) {
                sNetID = tm.getNetworkType();
                switch (sNetID) {
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                        sNetType = "Unknown";
                        break;
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        sNetType = "GPRS";
                        break;
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        sNetType = "EDGE";
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                        sNetType = "UMTS";
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                        sNetType = "HDSPA";
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                        sNetType = "HSUPA";
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                        sNetType = "HSPA";
                        break;
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                        sNetType = "CDMA";
                        break;
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        sNetType = "EVDO_0";
                        break;
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        sNetType = "EVDO_A";
                        break;
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                        sNetType = "1xRTT";
                        break;
                    default:
                        sNetType = "Unknown";
                        break;
                }
                int dataActivityType = tm.getDataActivity();
                String dataActivity = "un";
                switch (dataActivityType) {
                    case TelephonyManager.DATA_ACTIVITY_NONE:
                        dataActivity = "No";
                        break;
                    case TelephonyManager.DATA_ACTIVITY_IN:
                        dataActivity = "In";
                        break;
                    case TelephonyManager.DATA_ACTIVITY_OUT:
                        dataActivity = "Ou";
                        break;
                    case TelephonyManager.DATA_ACTIVITY_INOUT:
                        dataActivity = "IO";
                        break;
                    case TelephonyManager.DATA_ACTIVITY_DORMANT:
                        dataActivity = "Do";
                        break;
                }

                int dataType = tm.getDataState();
                String dataState = "un";
                switch (dataType) {
                    case TelephonyManager.DATA_DISCONNECTED:
                        dataState = "Di";
                        break;
                    case TelephonyManager.DATA_CONNECTING:
                        dataState = "Ct";
                        break;
                    case TelephonyManager.DATA_CONNECTED:
                        dataState = "Cd";
                        break;
                    case TelephonyManager.DATA_SUSPENDED:
                        dataState = "Su";
                        break;
                }

                switch (sPhoneID) {
                    case TelephonyManager.PHONE_TYPE_GSM:
                        GsmCellLocation gsmCellLocation = (GsmCellLocation) location;
                        if (gsmCellLocation != null) {
                            sCellInfo = gsmCellLocation.toString() + dataActivity + "|" + dataState + "|" + sNetType + "|";
                            sLacID = gsmCellLocation.getLac();
                            sCellID = gsmCellLocation.getCid();
                            if (isTrackingCell() && !dbHelper.cellExists(sCellID)){
                                sSimCountry = getSimCountry(true);
                                sSimOperator = getSimOperator(true);
                                sSimOperatorName = getSimOperatorName(true);
                                dbHelper.insertCell(sDB, sLacID, sCellID, sNetID, sLatitude,
                                        sLongitude, sSignalInfo, sCellInfo, sSimCountry,                                        sSimOperator, sSimOperatorName);
                            }
                            kmlpoints(sLacID, sCellID, sCellInfo, sLongitude, sLatitude);
                        }
                        break;
                    case TelephonyManager.PHONE_TYPE_CDMA:
                        CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) location;
                        if (cdmaCellLocation != null) {
                            sCellInfo = cdmaCellLocation.toString() + dataActivity + "|" + dataState + "|" + sNetType + "|";
                            sLacID = cdmaCellLocation.getNetworkId();
                            sCellID = cdmaCellLocation.getBaseStationId();
                            if (isTrackingCell() && !dbHelper.cellExists(sCellID)){
                                sSimCountry = getSimCountry(true);
                                sSimOperator = getSimOperator(true);
                                sSimOperatorName = getNetworkName(true);
                            }
                            kmlpoints(sLacID, sCellID, sCellInfo, sLongitude, sLatitude);
                        }
                }

                if (TrackingLocation) {
                    dbHelper.insertLocation(sDB, sLacID, sCellID, sNetID, sLatitude, sLongitude,
                            sSignalInfo, sCellInfo);
                }
                if (TrackingCell && !dbHelper.cellExists(sCellID)) {
                    dbHelper.insertCell(sDB, sLacID, sCellID, sNetID, sLatitude, sLongitude,
                            sSignalInfo, sCellInfo, sSimCountry, sSimOperator, sSimOperatorName);
                }


            }
        };

        sSignalListenerStrength = new PhoneStateListener() {
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                switch (sPhoneID) {
                    case TelephonyManager.PHONE_TYPE_GSM:
                        sSignalInfo = signalStrength.getGsmSignalStrength();
                        break;
                    case TelephonyManager.PHONE_TYPE_CDMA:
                        sSignalInfo = signalStrength.getCdmaDbm();
                        break;
                    default:
                        sSignalInfo = 0;
                }
                kmlpoints(sLacID, sCellID, sCellInfo, sLongitude, sLatitude);
                if (TrackingSignal) {
                    dbHelper.insertSignal(sDB, sLacID, sCellID, sNetID, sLatitude, sLongitude,
                            sSignalInfo, sCellInfo);
                }
            }
        };

    }

    public static int getPhoneID() {
        if (sPhoneID <= 0 || sPhoneID > 6)
            sPhoneID = tm.getPhoneType();

        return sPhoneID;
    }

    public static String getSimCountry(boolean force) {
        if (sSimCountry.isEmpty() || force)
            sSimCountry = tm.getSimCountryIso();

        return sSimCountry;
    }

    public static String getSimOperator(boolean force) {
        if (sSimOperator.isEmpty() || force)
            sSimOperator = tm.getSimOperator();

        return sSimOperator;
    }

    public static String getSimOperatorName(boolean force) {
        if (sSimOperatorName.isEmpty() || force)
            sSimOperatorName = tm.getSimOperatorName();

        return sSimOperatorName;
    }

    public static String getSimSubs(boolean force) {
        if (sSimSubs.isEmpty() || force)
            sSimSubs = tm.getSubscriberId();

        return sSimSubs;
    }

    public static String getSimSerial(boolean force) {
        if (sSimSerial.isEmpty() || force)
            sSimSerial = tm.getSimSerialNumber();

        return sSimSerial;
    }

    public static String getPhoneType(boolean force) {
        if (sPhoneType.isEmpty()|| force) {
            if (getPhoneID() == TelephonyManager.PHONE_TYPE_GSM)
                sPhoneType = "GSM";
            else if (getPhoneID() == TelephonyManager.PHONE_TYPE_CDMA)
                sPhoneType = "CDMA";
            else
                sPhoneType = "Unknown";
        }

        return sPhoneType;
    }

    public static String getIMEI(boolean force) {
        if (sIMEI.isEmpty() || force)
            sIMEI = tm.getDeviceId();

        return sIMEI;
    }

    public static String getIMEIv(boolean force) {
        if (sIMEIV.isEmpty() || force)
            sIMEIV = tm.getDeviceSoftwareVersion();

        return sIMEIV;
    }

    public static String getPhoneNumber(boolean force) {
        if (sPhoneNum.isEmpty() || force)
            sPhoneNum = tm.getLine1Number();

        return sPhoneNum;
    }

    public static String getNetworkName(boolean force) {
        if (sNetName.isEmpty() || force)
            sNetName = tm.getNetworkOperatorName();

        return sNetName;
    }

    public static String getSmmcMcc(boolean force) {
        if (sMmcmcc.isEmpty() || force)
            sMmcmcc = tm.getNetworkOperator();

        return sMmcmcc;
    }

    public static String getsNetworkType(boolean force) {
        if (sNetType.isEmpty() || force) {
            switch (tm.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    sNetType = "Unknown";
                    break;
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    sNetType = "GPRS";
                    break;
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    sNetType = "EDGE";
                    break;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    sNetType = "UMTS";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    sNetType = "HDSPA";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    sNetType = "HSUPA";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    sNetType = "HSPA";
                    break;
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    sNetType = "CDMA";
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    sNetType = "EVDO_0";
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    sNetType = "EVDO_A";
                    break;
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    sNetType = "1xRTT";
                    break;
                default:
                    sNetType = "Unknown";
                    break;
            }
        }

        return sNetType;
    }

    public static String getsLAC(boolean force) {
        if (sLac.isEmpty() || force) {
            GsmCellLocation gsmCellLocation = (GsmCellLocation) tm.getCellLocation();
            if (gsmCellLocation != null) {
                sLac = "" + gsmCellLocation.getLac();
            }
        }

        return sLac;
    }

    public static String getsCellId(boolean force) {
        if (sCellType.isEmpty() || force) {
            GsmCellLocation gsmCellLocation = (GsmCellLocation) tm.getCellLocation();
            if (gsmCellLocation != null) {
                sCellType = "" + gsmCellLocation.getCid();
            }
        }

        return sCellType;
    }

    public static String getsDataActivity(boolean force) {
        if (sDataActivityType.isEmpty() || force) {
            sDataActivity = tm.getDataActivity();
            sDataActivityType = "undef";
            switch (sDataActivity) {
                case TelephonyManager.DATA_ACTIVITY_NONE:
                    sDataActivityType = "None";
                    break;
                case TelephonyManager.DATA_ACTIVITY_IN:
                    sDataActivityType = "In";
                    break;
                case TelephonyManager.DATA_ACTIVITY_OUT:
                    sDataActivityType = "Out";
                    break;
                case TelephonyManager.DATA_ACTIVITY_INOUT:
                    sDataActivityType = "In-Out";
                    break;
                case TelephonyManager.DATA_ACTIVITY_DORMANT:
                    sDataActivityType = "Dormant";
                    break;
            }
        }

        return sDataActivityType;
    }

    public static String getsDataState(boolean force) {
        if (sDataState.isEmpty() || force) {
            sDataActivity = tm.getDataState();
            sDataState = "undef";
            switch (sDataActivity) {
                case TelephonyManager.DATA_DISCONNECTED:
                    sDataActivityType = "Disconnected";
                    break;
                case TelephonyManager.DATA_CONNECTING:
                    sDataActivityType = "Connecting";
                    break;
                case TelephonyManager.DATA_CONNECTED:
                    sDataActivityType = "Connected";
                    break;
                case TelephonyManager.DATA_SUSPENDED:
                    sDataActivityType = "Suspended";
                    break;
            }
        }

        return sDataState;
    }

    public static Boolean isTrackingSignal() {
        return TrackingSignal;
    }

    public static Boolean isTrackingCell() {
        return TrackingCell;
    }

    public static Boolean isTrackingLocation() {
        return TrackingLocation;
    }

    public static void tracksignal() {
        if (TrackingSignal) {
            tm.listen(sSignalListenerStrength, PhoneStateListener.LISTEN_NONE);
            TrackingSignal = false;
            sSignalInfo = 0;
        } else {
            tm.listen(sSignalListenerStrength, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            TrackingSignal = true;
        }
    }

    public static void trackcell() {
        if (TrackingCell) {
            tm.listen(sSignalListenerLocation, PhoneStateListener.LISTEN_NONE);
            TrackingCell = false;
            sCellInfo = "[0,0]|nn|nn|";
        } else {
            tm.listen(sSignalListenerLocation, PhoneStateListener.LISTEN_CELL_LOCATION);
            TrackingCell = true;
        }
    }

    public static void tracklocation(Context ctx) {
        if (TrackingLocation) {
            lm.removeUpdates(sLocationListener);
            TrackingLocation = false;
            sLongitude = 0.0;
            sLatitude = 0.0;
        } else {
            if (lm != null) {
                Log.i(TAG, "LocationManager already existed");
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, sLocationListener);
                TrackingLocation = true;
            } else {
                Log.i(TAG, "LocationManager did not existed");
                lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
                if (lm != null) {
                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Log.i(TAG, "LocationManager created");
                        sLocationListener = new MyLocationListener();
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, sLocationListener);
                        TrackingLocation = true;
                    } else {
                        // GPS No es permet
                        Log.i(TAG, "GPS not allowed");
                        AlertDialog.Builder msg = new AlertDialog.Builder(ctx);
                        msg.setMessage("GPS is not enabled!. You won�t be able to use GPS data until you enable it");
                        AlertDialog alert = msg.create();
                        alert.setTitle("Error:");
                        alert.show();
                        lm = null;
                    }
                }
            }
        }
    }

    private static class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                sLongitude = loc.getLongitude();
                sLatitude = loc.getLatitude();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status,
                Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

    public static void kmlheader() {
        sKML = "<kml xmlns=\"http://earth.google.com/kml/2.0\">\n";
        sKML += "<Folder>\n";
        sKML += "<name>AIMSICD</name>\n";
        sKML += "<description><![CDATA[AIMSICD LAC and CellID logs]]></description>\n";
    }

    private static void kmlpoints(int lac, int cellid, String info, double lng, double lat) {
        if ((lng != 0.0) || (lat != 0.0)) {
            //String timestamp = new java.text.SimpleDateFormat("yyyy-MM-ddTHH:mm:ss").format(new java.util.Date (epoch*1000));
            alPosition.add(lng + "," + lat);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date date = new java.util.Date();
            String datetime = dateFormat.format(date);
            sKML += "<Placemark>\n";
            sKML += "<TimeStamp><when>" + datetime + "</when></TimeStamp>\n";
            sKML += "<name>" + lac + " " + cellid + "</name>\n";
            sKML += "<description><![CDATA[" + info + "]]></description>\n";
            sKML += "<Style><IconStyle><color>ffffbebe</color><scale>0.5</scale></IconStyle></Style><Point><coordinates>" + lng + "," + lat + ",0</coordinates></Point>\n";
            sKML += "</Placemark>\n";
        }
    }

    private static String kmlLAC(String content) {
        return poligon(sLacID, alPosition, content);
    }

    private static String poligon(int lac, ArrayList<String> alPosition, String content) {
        //Collections.sort(alPosition);
        if (alPosition.size() > 0) {
            String[] aPosition = new String[alPosition.size()];
            alPosition.toArray(aPosition);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date date = new java.util.Date();
            String datetime = dateFormat.format(date);

            content += "<Placemark>\n";
            content += "  <TimeStamp><when>" + datetime + "</when></TimeStamp>\n";
            content += "  <name>LAC " + lac + "</name>\n";
            content += "  <description><![CDATA[LAC " + lac + "]]></description>\n";
            content += "  <Style><PolyStyle><color>7f9e9eff</color></PolyStyle></Style><MultiGeometry><Polygon><outerBoundaryIs><LinearRing><coordinates>";
            String initpos = aPosition[0];
            for (String pos : aPosition) {
                content += " " + pos;
            }
            content += " " + initpos + "\n</coordinates></LinearRing></outerBoundaryIs></Polygon></MultiGeometry>\n";
            content += "</Placemark>\n";
        }
        return content;
    }

    public static void dumpinfokml(Context context) {
        try {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                File rootcasirectory = new File(Environment.getExternalStorageDirectory() + "/AIMSICD/");
                // have the object build the directory structure, if needed.
                rootcasirectory.mkdirs();
                // create a File object for the output file

                // Make a copy of current content
                String buff = sKML;
                buff = kmlLAC(buff);
                buff += "</Folder>\n</kml>\n";

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
                java.util.Date date = new java.util.Date();
                String datetime = dateFormat.format(date);

                File file = new File(rootcasirectory, "aimsicd-"+datetime+".kml");
                try {
                    OutputStream os = new FileOutputStream(file);

                    os.write(buff.getBytes());
                } catch (IOException e) {
                    // Unable to create file, likely because external storage is
                    // not currently mounted.
                    Log.e(TAG, "ExternalStorage: Error writing " + file + e.getMessage());
                }

                AlertDialog.Builder msg = new AlertDialog.Builder(context);
                msg.setMessage("KML log copied in " + file);
                AlertDialog alert = msg.create();
                alert.setTitle("Log:");
                alert.show();
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                // We can only read the media
                AlertDialog.Builder msg = new AlertDialog.Builder(context);
                msg.setMessage("Sorry, your SD card is mounted in read only mode. Write access is required to your SD card.");
                AlertDialog alert = msg.create();
                alert.setTitle("Error:");
                alert.show();
            } else {
                // Something else is wrong. It may be one of many other states, but all we need
                //  to know is we can neither read nor write
                AlertDialog.Builder msg = new AlertDialog.Builder(context);
                msg.setMessage("Sorry, I could not find an SD card to copy the log.");
                AlertDialog alert = msg.create();
                alert.setTitle("Error:");
                alert.show();
            }
        } catch (Exception e) {
            AlertDialog.Builder msg = new AlertDialog.Builder(context);
            msg.setMessage("Something unexpected happened: " + e.getMessage());
            AlertDialog alert = msg.create();
            alert.setTitle("Error!");
            alert.setIcon(R.drawable.icon);
            alert.show();
            e.printStackTrace();
        }
    }

    public static void exportDB () {
        try {
            dbHelper.export(LOCATION_TABLE);
            dbHelper.export(CELL_TABLE);
            dbHelper.export(SIGNAL_TABLE);
        } catch (IOException ioe) {
            Log.e (TAG, "exportDB() " + ioe.getMessage());
        }
    }

    /**
     * SQLiteHelper class for the Location, Cell and Signal Strength Databases
     */
    public static class MySQLiteHelper extends SQLiteOpenHelper {

        public static final String COLUMN_ID = "_id";
        private static final int DATABASE_VERSION = 1;

        // Database creation statements
        private final String LOC_DATABASE_CREATE = "create table " +
                LOCATION_TABLE + " (" + COLUMN_ID +
                " integer primary key autoincrement, Lac INTEGER, CellID INTEGER, " +
                "Net VARCHAR, Lat VARCHAR, Lng VARCHAR, Signal INTEGER, Connection VARCHAR, " +
                "Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);";

        private final String CELL_DATABASE_CREATE = "create table " +
                CELL_TABLE + " (" + COLUMN_ID +
                " integer primary key autoincrement, Lac INTEGER, CellID INTEGER, " +
                "Net VARCHAR, Lat VARCHAR, Lng VARCHAR, Signal INTEGER, Connection VARCHAR, " +
                "Country VARCHAR, Operator VARCHAR, OperatorName VARCHAR, " +
                "Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);";

        private final String SIG_DATABASE_CREATE = "create table " +
                SIGNAL_TABLE + " (" + COLUMN_ID +
                " integer primary key autoincrement, Lac INTEGER, CellID INTEGER, " +
                "Net VARCHAR, Lat VARCHAR, Lng VARCHAR, Signal INTEGER, Connection VARCHAR, " +
                "Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);";

        public MySQLiteHelper(Context context) {
            super(context, DB_NAME, null, DATABASE_VERSION);
        }

        public void insertCell(SQLiteDatabase db, int lac, int cellID,
                int netType, double latitude, double longitude,
                int signalInfo, String cellInfo, String simCountry,
                String simOperator, String simOperatorName) {

            db.execSQL("INSERT INTO " + CELL_TABLE +
                    " (Lac , CellID, Net, Lat, Lng, Signal, Connection," +
                    "Country, Operator, OperatorName)" +
                    " VALUES(" + lac + "," + cellID + "," + netType + ","
                    + latitude + "," + longitude + "," + signalInfo + ",\""
                    + cellInfo + "\", \"" + simCountry + "\"," + simOperator + ",\""
                    + simOperatorName + "\");");
        }

        public void insertLocation(SQLiteDatabase db, int lac, int cellID,
                int netType, double latitude, double longitude,
                int signalInfo, String cellInfo) {

            db.execSQL("INSERT INTO " + LOCATION_TABLE +
                    " (Lac , CellID, Net, Lat, Lng, Signal, Connection)" +
                    " VALUES(" + lac + "," + cellID + "," + netType + ","
                    + latitude + "," + longitude + "," + signalInfo + ",\""
                    + cellInfo + "\");");
        }

        public void insertSignal(SQLiteDatabase db, int lac, int cellID,
                int netType, double latitude, double longitude,
                int signalInfo, String cellInfo) {

            db.execSQL("INSERT INTO " + SIGNAL_TABLE +
                    " (Lac , CellID, Net, Lat, Lng, Signal, Connection)" +
                    " VALUES(" + lac + "," + cellID + "," + netType + ","
                    + latitude + "," + longitude + "," + signalInfo + ",\""
                    + cellInfo + "\");");
        }

        public boolean cellExists(int cellID) {
            Cursor cursor = sDB.rawQuery("SELECT * FROM " + CELL_TABLE + " WHERE CellID = " +
                    cellID, null);

            return cursor.getCount()>0;
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL(LOC_DATABASE_CREATE);
            database.execSQL(CELL_DATABASE_CREATE);
            database.execSQL(SIG_DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(MySQLiteHelper.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + CELL_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + SIGNAL_TABLE);
            onCreate(db);
        }

        public void export(String tableName) throws IOException {
            Log.i(TAG, "exporting database - " + DB_NAME);

            XmlBuilder xmlBuilder = new XmlBuilder();
            xmlBuilder.start(DB_NAME);
            Log.d(TAG, "table name " + tableName);

            exportTable(tableName, xmlBuilder);
            String xmlString = xmlBuilder.end();
            writeToFile(xmlString, "aimsicd-" + tableName + ".xml");

            Log.i(TAG, "exporting database complete");
        }

        private void exportTable(final String tableName, XmlBuilder xmlBuilder) throws IOException {
            Log.d(TAG, "exporting table - " + tableName);
            xmlBuilder.openTable(tableName);
            String sql = "select * from " + tableName;
            Cursor c = sDB.rawQuery(sql, new String[0]);
            if (c.moveToFirst()) {
                int cols = c.getColumnCount();
                do {
                    xmlBuilder.openRow();
                    for (int i = 0; i < cols; i++) {
                        xmlBuilder.addColumn(c.getColumnName(i), c.getString(i));
                    }
                    xmlBuilder.closeRow();
                } while (c.moveToNext());
            }
            c.close();
            xmlBuilder.closeTable();
        }

        private void writeToFile(String xmlString, String exportFileName) throws IOException {
            File dir = new File(Environment.getExternalStorageDirectory() + "/AIMSICD/");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, exportFileName);
            file.createNewFile();

            ByteBuffer buff = ByteBuffer.wrap(xmlString.getBytes());
            FileChannel channel = new FileOutputStream(file).getChannel();
            try {
                channel.write(buff);
            } finally {
                if (channel != null)
                    channel.close();
            }
        }
    }

    /**
     * XmlBuilder is used to write XML tags (open and close, and a few attributes)
     * to a StringBuilder. Here we have nothing to do with IO or SQL, just a fancy StringBuilder.
     *
     * @author ccollins
     *
     */
    private static class XmlBuilder {
        private static final String OPEN_XML_STANZA = "";
        private static final String CLOSE_WITH_TICK = "'>";
        private static final String DB_OPEN = "<database name='";
        private static final String DB_CLOSE = "";
        private static final String TABLE_OPEN = "<table name='";
        private static final String TABLE_CLOSE = "";
        private static final String ROW_OPEN = "";
        private static final String ROW_CLOSE = "";
        private static final String COL_OPEN = "<col name='";
        private static final String COL_CLOSE = "";

        private final StringBuilder sb;

        public XmlBuilder() throws IOException {
            this.sb = new StringBuilder();
        }

        void start(String dbName) {
            this.sb.append(OPEN_XML_STANZA);
            this.sb.append(DB_OPEN).append(dbName).append(CLOSE_WITH_TICK);
        }

        String end() throws IOException {
            this.sb.append(DB_CLOSE);
            return this.sb.toString();
        }

        void openTable(String tableName) {
            this.sb.append(TABLE_OPEN).append(tableName).append(CLOSE_WITH_TICK);
        }

        void closeTable() {
            this.sb.append(TABLE_CLOSE);
        }

        void openRow() {
            this.sb.append(ROW_OPEN);
        }

        void closeRow() {
            this.sb.append(ROW_CLOSE);
        }

        void addColumn(final String name, final String val) throws IOException {
            this.sb.append(COL_OPEN).append(name).append(CLOSE_WITH_TICK).append(val).append(COL_CLOSE);
        }
    }

}

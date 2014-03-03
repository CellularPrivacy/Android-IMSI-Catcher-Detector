package com.jofrepalau.rawphone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
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

public class Device {

    private static PhoneStateListener signalListenerstrength;
    private static PhoneStateListener signalListenerlocation;
    private static LocationManager lm;
    private static LocationListener locationListener;
    public static SQLiteDatabase myDB;

    private static int phonetype;
    private static int signalinfo;
    private static int dataactivity;
    private static int nettype;
    private static int lac;
    private static int cellid;
    private static double slng;
    private static double slat;
    private static String csv, snettype, cellinfo, sdatastate;
    private static String kml, phonenum, sCellId, sLAC, snetname, smmcmcc, simcountry, sphonetype;
    private static String imei, imeiv, simoperator, simoperatorname, simserial, simsubs, sdataactivity;

    private static boolean TrackingCell;
    private static boolean TrackingSignal;
    private static boolean TrackingLocation;

    private static ArrayList<String> alPosition;

    private static TelephonyManager tm;

    private static String TABLE_NAME = "locationinfo";

    public static void InitDevice(Context mContext) {
        //TelephonyManager provides system details
        tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

        //Phone type and associated details
        imei = tm.getDeviceId();
        imeiv = tm.getDeviceSoftwareVersion();
        phonenum = tm.getLine1Number();
        phonetype = tm.getPhoneType();
        switch (phonetype) {
            case TelephonyManager.PHONE_TYPE_GSM:
                sphonetype = "GSM";
                smmcmcc = tm.getNetworkOperator();
                snetname = tm.getNetworkOperatorName();
                GsmCellLocation gsmCellLocation = (GsmCellLocation) tm.getCellLocation();
                if (gsmCellLocation != null) {
                    sCellId = "" + gsmCellLocation.getCid();
                    sLAC = "" + gsmCellLocation.getLac();
                }
                simcountry = tm.getSimCountryIso();
                simoperator = tm.getSimOperator();
                simoperatorname = tm.getSimOperatorName();
                simserial = tm.getSimSerialNumber();
                simsubs = tm.getSubscriberId();
                break;
            case TelephonyManager.PHONE_TYPE_CDMA:
                sphonetype = "CDMA";
                break;
        }

        //Network type
        nettype = tm.getNetworkType();
        switch (nettype) {
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                snettype = "Unknown";
                break;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                snettype = "GPRS";
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                snettype = "EDGE";
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                snettype = "UMTS";
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                snettype = "HSPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                snettype = "HDSPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                snettype = "HUSPA";
                break;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                snettype = "CDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                snettype = "EVDO_0";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                snettype = "EVDO_A";
                break;
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                snettype = "1xRTT";
                break;
            default:
                snettype = "Unknown";
                break;
        }

        dataactivity = tm.getDataActivity();
        sdataactivity = "undef";
        switch (dataactivity) {
            case TelephonyManager.DATA_ACTIVITY_NONE:
                sdataactivity = "None";
                break;
            case TelephonyManager.DATA_ACTIVITY_IN:
                sdataactivity = "In";
                break;
            case TelephonyManager.DATA_ACTIVITY_OUT:
                sdataactivity = "Out";
                break;
            case TelephonyManager.DATA_ACTIVITY_INOUT:
                sdataactivity = "In-Out";
                break;
            case TelephonyManager.DATA_ACTIVITY_DORMANT:
                sdataactivity = "Dormant";
                break;
        }

        dataactivity = tm.getDataState();
        sdatastate = "undef";
        switch (dataactivity) {
            case TelephonyManager.DATA_DISCONNECTED:
                sdataactivity = "Disconnected";
                break;
            case TelephonyManager.DATA_CONNECTING:
                sdataactivity = "Connecting";
                break;
            case TelephonyManager.DATA_CONNECTED:
                sdataactivity = "Connected";
                break;
            case TelephonyManager.DATA_SUSPENDED:
                sdataactivity = "Suspended";
                break;
        }

        //Load or create the location database
        try {
            String DB_NAME = "myCellInfo";
            myDB = mContext.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
            myDB.execSQL("CREATE TABLE IF NOT EXISTS " +
                    TABLE_NAME + " (_id INTEGER primary key autoincrement, Lac INTEGER, CellID INTEGER, Net VARCHAR, Lat VARCHAR, Lng VARCHAR, Signal INTEGER, Connection VARCHAR, Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);");

        } catch (SQLiteException se) {
            Log.e(mContext.getClass().getSimpleName(), "Could not create or Open the database:" + se);
            myDB = null;
        }

        signalListenerlocation = new PhoneStateListener() {
            public void onCellLocationChanged(CellLocation location) {
                nettype = tm.getNetworkType();
                switch (nettype) {
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                        snettype = "Unknown";
                        break;
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        snettype = "GPRS";
                        break;
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        snettype = "EDGE";
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                        snettype = "UMTS";
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                        snettype = "HDSPA";
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                        snettype = "HSUPA";
                        break;
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                        snettype = "HSPA";
                        break;
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                        snettype = "CDMA";
                        break;
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        snettype = "EVDO_0";
                        break;
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        snettype = "EVDO_A";
                        break;
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                        snettype = "1xRTT";
                        break;
                    default:
                        snettype = "Unknown";
                        break;
                }
                int dataactivity = tm.getDataActivity();
                String sdataactivity = "un";
                switch (dataactivity) {
                    case TelephonyManager.DATA_ACTIVITY_NONE:
                        sdataactivity = "No";
                        break;
                    case TelephonyManager.DATA_ACTIVITY_IN:
                        sdataactivity = "In";
                        break;
                    case TelephonyManager.DATA_ACTIVITY_OUT:
                        sdataactivity = "Ou";
                        break;
                    case TelephonyManager.DATA_ACTIVITY_INOUT:
                        sdataactivity = "IO";
                        break;
                    case TelephonyManager.DATA_ACTIVITY_DORMANT:
                        sdataactivity = "Do";
                        break;
                }

                int datastate = tm.getDataState();
                String sdatastate = "un";
                switch (datastate) {
                    case TelephonyManager.DATA_DISCONNECTED:
                        sdatastate = "Di";
                        break;
                    case TelephonyManager.DATA_CONNECTING:
                        sdatastate = "Ct";
                        break;
                    case TelephonyManager.DATA_CONNECTED:
                        sdatastate = "Cd";
                        break;
                    case TelephonyManager.DATA_SUSPENDED:
                        sdatastate = "Su";
                        break;
                }

                switch (phonetype) {
                    case TelephonyManager.PHONE_TYPE_GSM:
                        GsmCellLocation gsmCellLocation = (GsmCellLocation) location;
                        if (gsmCellLocation != null) {
                            cellinfo = gsmCellLocation.toString() + sdataactivity + "|" + sdatastate + "|" + snettype + "|";
                            lac = gsmCellLocation.getLac();
                            cellid = gsmCellLocation.getCid();
                            kmlpoints(lac, cellid, cellinfo, slng, slat);
                        }
                        break;
                    case TelephonyManager.PHONE_TYPE_CDMA:
                        CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) location;
                        if (cdmaCellLocation != null) {
                            cellinfo = cdmaCellLocation.toString() + sdataactivity + "|" + sdatastate + "|" + snettype + "|";
                            lac = cdmaCellLocation.getNetworkId();
                            cellid = cdmaCellLocation.getBaseStationId();
                            kmlpoints(lac, cellid, cellinfo, slng, slat);
                        }
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
                java.util.Date date = new java.util.Date();
                String datetime = dateFormat.format(date);
                insertrow(lac, cellid, nettype, slat, slng, signalinfo, cellinfo);

                csv += lac + "," + cellid + "," + snettype + "," + slat + "," + slng + "," + signalinfo + "," + datetime + "\n";
            }
        };

        signalListenerstrength = new PhoneStateListener() {
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                switch (phonetype) {
                    case TelephonyManager.PHONE_TYPE_GSM:
                        signalinfo = signalStrength.getGsmSignalStrength();
                        break;
                    case TelephonyManager.PHONE_TYPE_CDMA:
                        signalinfo = signalStrength.getCdmaDbm();
                        break;
                    default:
                        signalinfo = 0;
                }
                kmlpoints(lac, cellid, cellinfo, slng, slat);
                insertrow(lac, cellid, nettype, slat, slng, signalinfo, cellinfo);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
                java.util.Date date = new java.util.Date();
                String datetime = dateFormat.format(date);
            }
        };

        csv = "LAC,CellID,NetType,LAT,LNG,Strength\n";
    }

    public static int getPhonetype() {
        if (phonetype <= 0 || phonetype > 6)
            phonetype = tm.getPhoneType();

        return phonetype;
    }

    public static String getSimCountry() {
        if (simcountry.isEmpty())
            simcountry = tm.getSimCountryIso();

        return simcountry;
    }

    public static String getSimOperator() {
        if (simoperator.isEmpty())
            simoperator = tm.getSimOperator();

        return simoperator;
    }

    public static String getSimOperatorName() {
        if (simoperatorname.isEmpty())
            simoperatorname = tm.getSimOperatorName();

        return simoperatorname;
    }

    public static String getSimSubs() {
        if (simsubs.isEmpty())
            simsubs = tm.getSubscriberId();

        return simsubs;
    }

    public static String getSimSerial() {
        if (simserial.isEmpty())
            simserial = tm.getSimSerialNumber();

        return simserial;
    }

    public static String getSPhonetype() {
        if (sphonetype.isEmpty()) {
            if (getPhonetype() == TelephonyManager.PHONE_TYPE_GSM)
                sphonetype = "GSM";
            else if (getPhonetype() == TelephonyManager.PHONE_TYPE_CDMA)
                sphonetype = "CDMA";
            else
                sphonetype = "Unknown";
        }

        return sphonetype;
    }

    public static String getIMEI() {
        if (imei.isEmpty())
            imei = tm.getDeviceId();

        return imei;
    }

    public static String getIMEIv() {
        if (imeiv.isEmpty())
            imeiv = tm.getDeviceSoftwareVersion();

        return imeiv;
    }

    public static String getPhoneNumber() {
        if (phonenum.isEmpty())
            phonenum = tm.getLine1Number();

        return phonenum;
    }

    public static String getsNetworkName() {
        if (snetname.isEmpty())
            snetname = tm.getNetworkOperatorName();

        return snetname;
    }

    public static String getSmmcMcc() {
        if (smmcmcc.isEmpty())
            smmcmcc = tm.getNetworkOperator();

        return smmcmcc;
    }

    public static String getsNetworkType() {
        if (snettype.isEmpty()) {
            switch (tm.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    snettype = "Unknown";
                    break;
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    snettype = "GPRS";
                    break;
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    snettype = "EDGE";
                    break;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    snettype = "UMTS";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    snettype = "HDSPA";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    snettype = "HSUPA";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    snettype = "HSPA";
                    break;
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    snettype = "CDMA";
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    snettype = "EVDO_0";
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    snettype = "EVDO_A";
                    break;
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    snettype = "1xRTT";
                    break;
                default:
                    snettype = "Unknown";
                    break;
            }
        }

        return snettype;
    }

    public static String getsLAC() {
        if (sLAC.isEmpty()) {
            GsmCellLocation gsmCellLocation = (GsmCellLocation) tm.getCellLocation();
            if (gsmCellLocation != null) {
                sLAC = "" + gsmCellLocation.getLac();
            }
        }

        return sLAC;
    }

    public static String getsCellId() {
        if (sCellId.isEmpty()) {
            GsmCellLocation gsmCellLocation = (GsmCellLocation) tm.getCellLocation();
            if (gsmCellLocation != null) {
                sCellId = "" + gsmCellLocation.getCid();
            }
        }

        return sCellId;
    }

    public static String getsDataActivity() {
        if (sdataactivity.isEmpty()) {
            dataactivity = tm.getDataActivity();
            sdataactivity = "undef";
            switch (dataactivity) {
                case TelephonyManager.DATA_ACTIVITY_NONE:
                    sdataactivity = "None";
                    break;
                case TelephonyManager.DATA_ACTIVITY_IN:
                    sdataactivity = "In";
                    break;
                case TelephonyManager.DATA_ACTIVITY_OUT:
                    sdataactivity = "Out";
                    break;
                case TelephonyManager.DATA_ACTIVITY_INOUT:
                    sdataactivity = "In-Out";
                    break;
                case TelephonyManager.DATA_ACTIVITY_DORMANT:
                    sdataactivity = "Dormant";
                    break;
            }
        }

        return sdataactivity;
    }

    public static String getsDataState() {
        if (sdatastate.isEmpty()) {
            dataactivity = tm.getDataState();
            sdatastate = "undef";
            switch (dataactivity) {
                case TelephonyManager.DATA_DISCONNECTED:
                    sdataactivity = "Disconnected";
                    break;
                case TelephonyManager.DATA_CONNECTING:
                    sdataactivity = "Connecting";
                    break;
                case TelephonyManager.DATA_CONNECTED:
                    sdataactivity = "Connected";
                    break;
                case TelephonyManager.DATA_SUSPENDED:
                    sdataactivity = "Suspended";
                    break;
            }
        }

        return sdatastate;
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
            tm.listen(signalListenerstrength, PhoneStateListener.LISTEN_NONE);
            TrackingSignal = false;
            signalinfo = 0;
        } else {
            tm.listen(signalListenerstrength, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            TrackingSignal = true;
        }
    }

    public static void trackcell() {
        if (TrackingCell) {
            tm.listen(signalListenerlocation, PhoneStateListener.LISTEN_NONE);
            TrackingCell = false;
            cellinfo = "[0,0]|nn|nn|";
        } else {
            tm.listen(signalListenerlocation, PhoneStateListener.LISTEN_CELL_LOCATION);
            TrackingCell = true;
        }
    }

    public static void tracklocation(Context ctx) {
        if (TrackingLocation) {
            lm.removeUpdates(locationListener);
            TrackingLocation = false;
            slng = 0.0;
            slat = 0.0;
        } else {
            if (lm != null) {
                Log.i("rawphone", "LocationManager already existed");
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                TrackingLocation = true;
            } else {
                Log.i("rawphone", "LocationManager did not existed");
                lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
                if (lm != null) {
                    if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Log.i("rawphone", "LocationManager created");
                        locationListener = new MyLocationListener();
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                        TrackingLocation = true;
                    } else {
                        // GPS No es permet
                        Log.i("rawphone", "GPS not allowed");
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
                slng = loc.getLongitude();
                slat = loc.getLatitude();
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

    protected static void insertrow(int lac, int cellid, int nettype, double dlat, double dlng, int signalinfo, String cellinfo) {
        // _id INTEGER primary key autoincrement, Lac INTEGER, CellID INTEGER, Net VARCHAR, Lat VARCHAR, Lng VARCHAR, Signal INTEGER, Connection VARCHAR, Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);"
        Log.i("rawphone", " ==> Exec: insertrow (" + lac + "," + cellid + "," + nettype + "," + dlat + "," + dlng + "," + signalinfo + "," + cellinfo + ")");
        try {
            if ((dlng != 0.0) || (dlat != 0.0)) {
                if (myDB != null) {
                    Cursor mCursor = myDB.query(true, TABLE_NAME, new String[]{"_id", "Signal"}, "CellID =" + cellid, null, null, null, "Signal", null);
                    if (mCursor.moveToFirst()) {
                        do {
                            int iSignal = mCursor.getInt(1);
                            if (iSignal <= signalinfo) {
                                Log.i("rawphone", " ==> Removing " + mCursor.getInt(0) + " adding cell " + cellid + " with signal " + signalinfo);
                                myDB.delete(TABLE_NAME, "_id =" + mCursor.getInt(0), null);
                                //mCursor.deactivate();
                                myDB.execSQL("INSERT INTO " + TABLE_NAME +
                                        " (Lac , CellID, Net, Lat, Lng, Signal, Connection)" +
                                        " VALUES(" + lac + "," + cellid + "," + nettype + "," + dlat + "," + dlng + "," + signalinfo + ",\"" + cellinfo + "\");");
                                //mCursor.deactivate();
                            } else {
                                Log.i("rawphone", " ==> Keep entry " + mCursor.getInt(0) + " with signal " + mCursor.getInt(1));
                            }

                        } while (mCursor.moveToNext());
                    } else {
                        Log.i("rawphone", " ==> Adding VALUES(" + lac + "," + cellid + "," + nettype + "," + dlat + "," + dlng + "," + signalinfo + ",\"" + cellinfo + "\");");
                        myDB.execSQL("INSERT INTO " + TABLE_NAME +
                                " (Lac , CellID, Net, Lat, Lng, Signal, Connection)" +
                                " VALUES(" + lac + "," + cellid + "," + nettype + "," + dlat + "," + dlng + "," + signalinfo + ",\"" + cellinfo + "\");");

                    }

                    mCursor.close();
                } else {
                    Log.e("rawphone", " ==>  Database not initialized!");
                }
            }
        } catch (Exception e) {
            // Something wierd happended
            System.out.println("Strange Error caught: " + e.getMessage());
        }
    }

    public static void quit() {
        if (lm != null) {
            lm.removeUpdates(locationListener);
        }
        if (myDB != null) {
            myDB.close();
        }
    }

    public static void dumpinfokml(Context ctx) {
        try {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                // We can read and write the media
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                // We can only read the media
                AlertDialog.Builder msg = new AlertDialog.Builder(ctx);
                msg.setMessage("Sorry, your SD card is mounted as ready only. We can't copy the log if we can't write there");
                AlertDialog alert = msg.create();
                alert.setTitle("Error:");
                alert.show();
                return;
            } else {
                // Something else is wrong. It may be one of many other states, but all we need
                //  to know is we can neither read nor write
                AlertDialog.Builder msg = new AlertDialog.Builder(ctx);
                msg.setMessage("Sorry, I could not find an SD card where to copy the log");
                AlertDialog alert = msg.create();
                alert.setTitle("Error:");
                alert.show();
                return;
            }
            File rootcasirectory = new File(Environment.getExternalStorageDirectory() + "/rawphone/");
            // have the object build the directory structure, if needed.
            rootcasirectory.mkdirs();
            // create a File object for the output file

            // Make a copy of current content
            String buff = kml;
            buff = kmlLAC(buff);
            buff += "</Folder>\n</kml>\n";

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
            java.util.Date date = new java.util.Date();
            String datetime = dateFormat.format(date);

            File file = new File(rootcasirectory, "rawphone-" + datetime + ".kml");
            try {
                OutputStream os = new FileOutputStream(file);

                os.write(buff.getBytes());
            } catch (IOException e) {
                // Unable to create file, likely because external storage is
                // not currently mounted.
                System.out.println("ExternalStorage: Error writing " + file + e.getMessage());
            }

            AlertDialog.Builder msg = new AlertDialog.Builder(ctx);
            msg.setMessage("KML log copied in " + file);
            AlertDialog alert = msg.create();
            alert.setTitle("Log:");
            alert.show();
        } catch (Exception e) {
            AlertDialog.Builder msg = new AlertDialog.Builder(ctx);
            msg.setMessage("Something unexpected happened: " + e.getMessage());
            AlertDialog alert = msg.create();
            alert.setTitle("Error!");
            alert.setIcon(R.drawable.icon);
            alert.show();
            e.printStackTrace();
        }
    }

    private static void kmlheader() {
        kml = "<kml xmlns=\"http://earth.google.com/kml/2.0\">\n";
        kml += "<Folder>\n";
        kml += "<name>RawPhone</name>\n";
        kml += "<description><![CDATA[RawPhone LAC and CellID logs]]></description>\n";
    }

    private static void kmlpoints(int lac, int cellid, String info, double lng, double lat) {
        if ((slng != 0.0) || (slat != 0.0)) {
            //String timestamp = new java.text.SimpleDateFormat("yyyy-MM-ddTHH:mm:ss").format(new java.util.Date (epoch*1000));
            alPosition.add(slng + "," + slat);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date date = new java.util.Date();
            String datetime = dateFormat.format(date);
            kml += "<Placemark>\n";
            kml += "<TimeStamp><when>" + datetime + "</when></TimeStamp>\n";
            kml += "<name>" + lac + " " + cellid + "</name>\n";
            kml += "<description><![CDATA[" + info + "]]></description>\n";
            kml += "<Style><IconStyle><color>ffffbebe</color><scale>0.5</scale></IconStyle></Style><Point><coordinates>" + lng + "," + lat + ",0</coordinates></Point>\n";
            kml += "</Placemark>\n";
        }
    }

    private static String kmlLAC(String content) {
        return poligon(lac, alPosition, content);
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

    public static void dumpinfocsv(Context mContext) {
        try {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                // We can read and write the media
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                // We can only read the media
                AlertDialog.Builder msg = new AlertDialog.Builder(mContext);
                msg.setMessage("Sorry, your SD card is mounted as ready only. We can�t copy the log if we can� write there");
                AlertDialog alert = msg.create();
                alert.setTitle("Error:");
                alert.show();
                return;
            } else {
                // Something else is wrong. It may be one of many other states, but all we need
                //  to know is we can neither read nor write
                AlertDialog.Builder msg = new AlertDialog.Builder(mContext);
                msg.setMessage("Sorry, I could not find an SD card where to copy the log");
                AlertDialog alert = msg.create();
                alert.setTitle("Error:");
                alert.show();
                return;
            }
            File rootcasirectory = new File(Environment.getExternalStorageDirectory() + "/rawphone/");
            // have the object build the directory structure, if needed.
            rootcasirectory.mkdirs();
            // create a File object for the output file

            // Make a copy of current content
            String buff = csv;

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
            java.util.Date date = new java.util.Date();
            String datetime = dateFormat.format(date);

            File file = new File(rootcasirectory, "rawphone-" + datetime + ".csv");
            try {
                OutputStream os = new FileOutputStream(file);

                os.write(buff.getBytes());
            } catch (IOException e) {
                // Unable to create file, likely because external storage is
                // not currently mounted.
                System.out.println("ExternalStorage: Error writing " + file + e.getMessage());
            }

            AlertDialog.Builder msg = new AlertDialog.Builder(mContext);
            msg.setMessage("CSV log copied in " + file);
            AlertDialog alert = msg.create();
            alert.setTitle("Log:");
            alert.show();
        } catch (Exception e) {
            AlertDialog.Builder msg = new AlertDialog.Builder(mContext);
            msg.setMessage("Something unexpected happened: " + e.getMessage());
            AlertDialog alert = msg.create();
            alert.setTitle("Error!");
            alert.setIcon(R.drawable.icon);
            alert.show();
            e.printStackTrace();
        }
    }

}

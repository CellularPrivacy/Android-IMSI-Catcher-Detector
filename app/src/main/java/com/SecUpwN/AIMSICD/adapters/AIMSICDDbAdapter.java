package com.SecUpwN.AIMSICD.adapters;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.utils.Cell;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class AIMSICDDbAdapter {

    /*
     * This handles the AMISICD DataBase tables:
     *
     * As of 2015-01-01 we'll be slowly migrating from the old DB structure
     * to the new one as detailed here:
     * https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/215
     *
     * As you work on these tables, please try to implement the new tables by
     * first changing the table names and then the table columns.
     */
    private final String TAG = "AISMICD_DbAdaptor";
    public static final String FOLDER = Environment.getExternalStorageDirectory() + "/AIMSICD/";
    private static final String COLUMN_ID = "_id";              // Underscore is no longer required...

    private final String LOCATION_TABLE = "locationinfo";       // TABLE_DBI_MEASURE:DBi_measure (volatile)
    private final String CELL_TABLE = "cellinfo";               // TABLE_DBI_BTS:DBi_bts (physical)
    private final String OPENCELLID_TABLE = "opencellid";       // TABLE_DBE_IMPORT:DBe_import
    private final String DEFAULT_MCC_TABLE = "defaultlocation"; // TABLE_DEFAUKT_MCC:defaultlocation
    private final String SILENT_SMS_TABLE = "silentsms";        // TABLE_SILENT_SMS:silentsms

    // Some placeholders for the use of the new tables:

    // private final String TABLE_DBE_IMPORT       = "DBe_import";       // External: BTS import table
    // private final String TABLE_DBE_CAPABILITIES = "DBe_capabilities"  // External: MNO & BTS network capabilities
    // private final String TABLE_DBI_BTS          = "DBi_bts";          // Internal: (physical) BTS data
    // private final String TABLE_DBI_MEASURE      = "DBi_measure";      // Internal: (volatile) network measurements
    // private final String TABLE_DEFAULT_MCC      = "defaultlocation";  // Deafult MCC for each country
    // private final String TABLE_DETECTION_FLAGS  = "DetectionFlags"    // Detection Flag description, settings and scoring table
    // private final String TABLE_EVENT_LOG        = "EventLog"          // Detection and general EventLog (persistent)
    // private final String TABLE_SECTORTYPE       = "SectorType"        // BTS tower sector configuration (Many CID, same BTS)
    // private final String TABLE_SILENT_SMS       = "silentsms";        // Silent SMS details
    // private final String TABLE_CMEASURES        = "CounterMeasures"   // Counter Measures thresholds and description

    // cell tower signal strength collected by the device
    // ToDo: Remove this table and use "rx_signal" in the "TABLE_DBI_MEASURE:DBi_measure" table..
    private final String CELL_SIGNAL_TABLE = "cellSignal";      //
    private final String DB_NAME = "aimsicd.db";

    private final String[] mTables;
    private final DbHelper mDbHelper;
    private SQLiteDatabase mDb;
    private final Context mContext;

    public static final int DATABASE_VERSION = 8;
    private Cursor signalStrengthMeasurementDatA;

    public AIMSICDDbAdapter(Context context) {
        mContext = context;
        mDbHelper = new DbHelper(context);
        mTables = new String[]{LOCATION_TABLE, CELL_TABLE, OPENCELLID_TABLE,
                SILENT_SMS_TABLE};
    }

    public AIMSICDDbAdapter open() throws SQLException {
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public long insertSilentSms(Bundle bundle) {
        ContentValues smsValues = new ContentValues();
        smsValues.put("Address", bundle.getString("address"));
        smsValues.put("Display", bundle.getString("display_address"));
        smsValues.put("Class", bundle.getString("class"));
        smsValues.put("ServiceCtr", bundle.getString("service_centre"));
        smsValues.put("Message", bundle.getString("message"));

        return mDb.insert(SILENT_SMS_TABLE, null, smsValues);
    }

    /**
     * Inserts (API?) Cell Details into Database (cellinfo) TABLE_DBI_BTS:DBi_bts/measure
     *
     * @return row id or -1 if error
     *
     * TODO: This should become TABLE_DBI_BTS: DBi_bts | measure
     *
     */
    public long insertCell(int lac, int cellID, int netType, double latitude, double longitude,
            int signalInfo, int mcc, int mnc, double accuracy, double speed, double direction,
            String networkType, long measurementTaken) {

        if (cellID != -1 && (latitude != 0.0 && longitude != 0.0)) {
            //Populate Content Values for Insert or Update
            ContentValues cellValues = new ContentValues();
            cellValues.put("Lac", lac);
            cellValues.put("CellID", cellID);
            cellValues.put("Net", netType);
            cellValues.put("Lat", latitude);
            cellValues.put("Lng", longitude);
            cellValues.put("Signal", signalInfo);
            cellValues.put("Mcc", mcc);
            cellValues.put("Mnc", mnc);
            cellValues.put("Accuracy", accuracy);
            cellValues.put("Speed", speed);
            cellValues.put("Direction", direction);
            cellValues.put("NetworkType", networkType);
            cellValues.put("MeasurementTaken", measurementTaken);

            if (cellExists(cellID)) {
                Log.v(TAG, "Cell info updated in local db: " + cellID);
                return mDb.update(CELL_TABLE, cellValues,
                        "CellID=?",
                        new String[]{Integer.toString(cellID)});
            } else {
                Log.v(TAG, "New Cell found, insert into local db:: " + cellID);
                return mDb.insert(CELL_TABLE, null, cellValues);
            }
        }
        return 0;
    }

    /**
     * Inserts (API?) Cell Details into Database (cellinfo) TABLE_DBI_BTS:DBi_bts/measure
     *
     * @return row id or -1 if error
     *
     * TODO: This should become TABLE_DBI_BTS: DBi_bts | measure
     * and we might wanna rename "insertCell" to "addMeasurement" ??
     *
     */
    public long insertCell(Cell cell) {

        // I'm not convinced we should not add a BTS even if Lat/Lon is 0?
        // since lat/lon can be 0 if no location have been found.
        // --E:V:A
        //
        if (cell.getCID() != Integer.MAX_VALUE && (cell.getLat() != 0.0 && cell.getLon() != 0.0)) {
            //Populate Content Values for Insert or Update
            ContentValues cellValues = new ContentValues();
            cellValues.put("Lac", cell.getLAC());
            cellValues.put("CellID", cell.getCID());
            cellValues.put("Net", cell.getNetType());
            cellValues.put("Lat", cell.getLat());
            cellValues.put("Lng", cell.getLon());
            cellValues.put("Signal", cell.getDBM());
            cellValues.put("Mcc", cell.getMCC());
            cellValues.put("Mnc", cell.getMNC());
            cellValues.put("Accuracy", cell.getAccuracy());
            cellValues.put("Speed", cell.getSpeed());
            cellValues.put("Direction", cell.getBearing());
            cellValues.put("MeasurementTaken", cell.getTimestamp());

            if (cellExists(cell.getCID())) {
                Log.v(TAG, "CID info updated in local db (DBi): " + cell.getCID());
                return mDb.update(CELL_TABLE, cellValues,
                        "CellID=?", new String[]{Integer.toString(cell.getCID())});
            } else {
                Log.v(TAG, "New CID found, insert into local db (DBi):: " + cell.getCID());
                return mDb.insert(CELL_TABLE, null, cellValues);
            }
        }
        return 0;
    }

    /**
     * Inserts OCID (CSV?) details into Database (opencellid) DBe_import
     *
     * @return row id or -1 if error
     *
     * TODO: Is this where CSV data is populating the opencellid table?
     *
     */
    long insertOpenCell(double latitude, double longitude, int mcc, int mnc, int lac,
                        int cellID, int avgSigStr, int samples) {

        //Populate Content Values for Insert or Update
        ContentValues cellIDValues = new ContentValues();
        cellIDValues.put("Lat", latitude);
        cellIDValues.put("Lng", longitude);
        cellIDValues.put("Mcc", mcc);
        cellIDValues.put("Mnc", mnc);
        cellIDValues.put("Lac", lac);
        cellIDValues.put("CellID", cellID);
        cellIDValues.put("AvgSigStr", avgSigStr);
        //cellIDValues.put("Range", range );
        cellIDValues.put("Samples", samples);

        if (openCellExists(cellID)) {
             Log.v(TAG, "CID already in OCID DB (db update): " + cellID);
            return mDb.update(OPENCELLID_TABLE, cellIDValues,
                    "CellID=?", new String[]{Integer.toString(cellID)});
        } else {
            //DETECTION 1  ???
            // TODO: Why are we inserting this into DBe_import?
            // This doesn't sound right, unless importing OCID
            // --E:V:A
            Log.v(TAG, "ALERT: CID -NOT- in OCID DB (db insert): " + cellID);
            return mDb.insert(OPENCELLID_TABLE, null, cellIDValues);
        }
    }

    /**
     * Inserts API location details into the measurement Database (locationinfo)
     *
     * @return row id or -1 if error
     *
     * TODO: TABLE_DBI_MEASURE:DBi_measure
     */
    public long insertLocation(int lac, int cellID,
            int netType, double latitude, double longitude,
            int signalInfo, String cellInfo) {

        if (latitude != 0.0 && longitude != 0.0) {
            //Populate Content Values for Insert or Update
            ContentValues locationValues = new ContentValues();
            locationValues.put("Lac", lac);
            locationValues.put("CellID", cellID);
            locationValues.put("Net", netType);
            locationValues.put("Lat", latitude);
            locationValues.put("Lng", longitude);
            locationValues.put("Signal", signalInfo);
            locationValues.put("Connection", cellInfo); // This is funny, with multiple items...

            if (locationExists(cellID, latitude, longitude, signalInfo)) {
                return mDb.update(LOCATION_TABLE, locationValues, "CellID=?",
                        new String[]{Integer.toString(cellID)});
            } else {
                return mDb.insert(LOCATION_TABLE, null, locationValues);
            }
        }

        return 0;
    }

    /**
     * Delete cell info - for use in tests
     *
     * @param cellId
     * @return
     *
     */
    public int deleteCell(int cellId) {
        Log.i(TAG, "Cell deleted: " + cellId);
        return mDb.delete(CELL_TABLE, "CellID = ?", new String[]{ String.valueOf(cellId) });
    }

    /**
     * Returns Silent SMS database (silentsms) contents
     */
    public Cursor getSilentSmsData() {
        return mDb.query(SILENT_SMS_TABLE, new String[]{"Address", "Display", "Class", "ServiceCtr",
                        "Message", "Timestamp"},
                null, null, null, null, COLUMN_ID + " DESC"
        );
    }

    /**
     * Returns Cell Information (DBi_bts) database contents
     */
    public Cursor getCellData() {
        return mDb.query(CELL_TABLE, new String[]{"CellID", "Lac", "Net", "Lat", "Lng",
                        "Signal", "Mcc", "Mnc", "Accuracy", "Speed", "Direction"},
                null, null, null, null, null
        );
    }

    /**
     * Returns Cell Information for contribution to the OpenCellID Project
     */
    public Cursor getOPCIDSubmitData() {
        return mDb.query(CELL_TABLE, new String[]{ "Lng", "Lat", "Mcc", "Mnc", "Lac", "CellID",
                        "Signal", "Timestamp", "Accuracy", "Speed", "Direction", "NetworkType"},
                "OCID_SUBMITTED <> 1", null, null, null, null
        );
    }

    /**
     * Returns Location Information (DBi_meas) database contents
     */
    public Cursor getLocationData() {
        return mDb.query( LOCATION_TABLE,
                new String[]{"CellID", "Lac", "Net", "Lat", "Lng", "Signal"},
                null, null, null, null, null
        );
    }

    /**
     * Returns OpenCellID (DBe_import) database contents
     */
    public Cursor getOpenCellIDData() {
        return mDb.query(OPENCELLID_TABLE,
                new String[]{"CellID", "Lac", "Mcc", "Mnc", "Lat", "Lng", "AvgSigStr", "Samples"},
                null, null, null, null, null
        );
    }

    /**
     * Returns Default MCC Locations (defaultlocation) database contents
     */
    public Cursor getDefaultMccLocationData() {
        return mDb.query(DEFAULT_MCC_TABLE,
                new String[]{"Country", "Mcc", "Lat", "Lng"},
                null, null, null, null, null);
    }

    /**
     * Checks to see if Location already exists in database
     */
    boolean locationExists(int cellID, double lat, double lng, int signal) {
        Cursor cursor = mDb.rawQuery("SELECT * FROM " + LOCATION_TABLE + " WHERE CellID = " +
                cellID + " AND Lat = " + lat + " AND Lng = " + lng + " AND Signal = " + signal,
                null);
        boolean exists = cursor.getCount() > 0;
        Log.i(TAG, "Cell exists in location table?: " + exists);
        cursor.close();

        return exists;
    }

    /**
     * Checks to see if Cell already exists in database
     */
    boolean cellExists(int cellID) {
        Cursor cursor = mDb.rawQuery("SELECT 1 FROM " + CELL_TABLE + " WHERE CellID = " + cellID,
                null);

        boolean exists = cursor.getCount() > 0;
        Log.i(TAG, "Cell exists in local DB?: " + exists);
        cursor.close();

        return exists;
    }

    /**
     * Checks to see if Cell already exists in OpenCellID database
     */
    public boolean openCellExists(int cellID) {
        Cursor cursor = mDb.rawQuery("SELECT * FROM " + OPENCELLID_TABLE + " WHERE CellID = " +
                cellID, null);

        boolean exists = cursor.getCount() > 0;
        Log.i(TAG, "Cell exists in OCID?: " + exists);
        cursor.close();

        return exists;
    }

    public boolean checkLAC(Cell cell) {
        Cursor cursor = mDb.query(CELL_TABLE, new String[]{"Lac"}, "CellID=" + cell.getCID(),
                null,null,null,null);

        // 2015-01-20
        // This is using the LAC found by API and comparing to LAC found from a previous
        // measurement in the "DBi_measure". This is NOT depending on "DBe_import".
        // This works for now...but we probably should consider populating "DBi_measure"
        // as soon as the API gets a new LAC. Then the detection can be done by SQL.
        // -- E:V:A
        while (cursor.moveToNext()) {
            if (cell.getLAC() != cursor.getInt(0)) {
                //Log.i(TAG, "ALERT: Changing LAC on CID: " + cell.getCID()
                //        + " Current LAC(DBi): " + cell.getLAC()
                //        + " Database LAC(DBe): " + cursor.getInt(0));
                Log.i(TAG, "ALERT: Changing LAC on CID: " + cell.getCID()
                        + " LAC(API): " + cell.getLAC()
                        + " LAC(DBi): " + cursor.getInt(0) );
                cursor.close();
                return false;
            } else {
                //Log.v(TAG, "LAC checked - no change.  CID:" + cell.getCID() + " LAC(DBi):"+ cell.getLAC() +
                //    " LAC(DBe): " + cursor.getInt(0) );
                Log.v(TAG, "LAC checked - no change on CID:" + cell.getCID()
                        + " LAC(API): " + cell.getLAC()
                        + " LAC(DBi): " + cursor.getInt(0) );
            }
        }

        cursor.close();
        return true;
    }

    /**
     * Updates Cell (cellinfo) records to indicate OpenCellID contribution has been made
     * TODO: This should be done on TABLE_DBI_MEASURE::DBi_measure:isSubmitted
     */
    public void ocidProcessed() {
        ContentValues ocidValues = new ContentValues();
        ocidValues.put("OCID_SUBMITTED", 1); // isSubmitted
        mDb.update(CELL_TABLE, ocidValues, "OCID_SUBMITTED<>?", new String[]{"1"}); //isSubmitted
    }

    public double[] getDefaultLocation(int mcc) {
        double[] loc = new double[2];
        Cursor cursor = mDb.rawQuery("SELECT Lat, Lng FROM " +
                DEFAULT_MCC_TABLE + " WHERE Mcc = " + mcc, null);

        if (cursor.moveToFirst()) {
            loc[0] = Double.parseDouble(cursor.getString(0));
            loc[1] = Double.parseDouble(cursor.getString(1));
        } else {
            loc[0] = 0.0;
            loc[1] = 0.0;
        }
        cursor.close();
        return loc;
    }

    // TODO: What is this used for??
    // Seem to remove all but the last row, unless its invalid?
    public void cleanseCellTable() {
        // This (seem?) to remove all but the last row in the "cellinfo" table
        mDb.execSQL("DELETE FROM " + CELL_TABLE + " WHERE " + COLUMN_ID + " NOT IN (SELECT MAX(" + COLUMN_ID + ") FROM " + CELL_TABLE + " GROUP BY CellID)");
        // This removes all cells with trouble CID numbers (MAX, -1)
        mDb.execSQL("DELETE FROM " + CELL_TABLE + " WHERE CellID = " + Integer.MAX_VALUE + " OR CellID = -1");
    }

    public boolean prepareOpenCellUploadData() {
        boolean result;
        // Q: Where is this? A: It is wherever your device has mounted its SDCard.
        // For example:  /data/media/0/AIMSICD/OpenCellID
        File dir = new File(FOLDER + "OpenCellID/");
        if (!dir.exists()) {
            result = dir.mkdirs();
            if (!result) {
                return false;
            }
        }
        File file = new File(dir, "aimsicd-ocid-data.csv");

        try {
            result = file.createNewFile();
            if (!result) {
                return false;
            }
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            open();
            Cursor c = getOPCIDSubmitData();

            csvWrite.writeNext("mcc,mnc,lac,cellid,lon,lat,signal,measured_at,rating,speed,direction,act");
            String[] rowData = new String[c.getColumnCount()];
            int size = c.getColumnCount();
            AIMSICD.mProgressBar.setProgress(0);
            AIMSICD.mProgressBar.setMax(size);
            while (c.moveToNext()) {
                for (int i = 0; i < size; i++) {
                    rowData[i] = c.getString(i);
                    AIMSICD.mProgressBar.setProgress(i);
                }
                csvWrite.writeNext(rowData);
            }

            csvWrite.close();
            c.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error creating OpenCellID Upload Data: " + e);
            return false;
        } finally {
            AIMSICD.mProgressBar.setProgress(0);
        }
    }

    /**
     * Populates the Default MCC Location table using the CSV file found in the
     * application ASSETS folder
     */
    private void populateDefaultMCC(SQLiteDatabase db) {
        AssetManager mngr = mContext.getAssets();
        InputStream csvDefaultMcc;
        FileOutputStream fout;

        try {
            csvDefaultMcc = mngr.open("default_mcc_locations.csv");
            File tempfile = File.createTempFile("tempFile", ".tmp");
            tempfile.deleteOnExit();

            fout = new FileOutputStream(tempfile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = csvDefaultMcc.read(buf)) != -1) {
                fout.write(buf, 0, len);
            }
            fout.close();
            csvDefaultMcc.close();

            CSVReader csvReader = new CSVReader(new FileReader(tempfile));
            List<String[]> csvMcc = csvReader.readAll();
            //Populate Content Values for Insert or Update
            ContentValues defaultMccValues = new ContentValues();

            for (int i = 1; i < csvMcc.size(); i++) {
                defaultMccValues.put("Country", csvMcc.get(i)[0]);
                defaultMccValues.put("Mcc", csvMcc.get(i)[1]);
                defaultMccValues.put("Lng", csvMcc.get(i)[2]);
                defaultMccValues.put("Lat", csvMcc.get(i)[3]);
                db.insert(DEFAULT_MCC_TABLE, null, defaultMccValues);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error populating Default MCC Data: " + e);
        }
    }

    /**
     * Parses the downloaded CSV from OpenCellID and uses it to populate "DBe_import".
     * ("opencellid" table.)
     *
     * Why are we only populating 8 items out of 19?
     *
     * From downloaded OCID CSV file:  (19 items)
     *   # head -2 opencellid.csv
     *   lat,lon,mcc,mnc,lac,cellid,averageSignalStrength,range,samples,changeable,radio,rnc,cid,psc,tac,pci,sid,nid,bid
     *   54.63376,25.160243,246,3,20,1294,0,-1,1,1,GSM,,,,,,,,
     *
     *  Unfortunately there are 2 important missing items in the OCID CSV file:
     *   - "time_first"
     *   - "time_last"
     *
     *   In addition the OCID data often contain unexplained negative values for one or both of:
     *    - "samples"
     *    - "range"
     *
     *   Also we should probably change this function name from:
     *     "updateOpenCellID" to "populateDBe_import"
     */
    public boolean updateOpenCellID() {
        String fileName = Environment.getExternalStorageDirectory()
                + "/AIMSICD/OpenCellID/opencellid.csv";
        File file = new File(fileName);
        try {
            if (file.exists()) {
                CSVReader csvReader = new CSVReader(new FileReader(file));
                List<String[]> csvCellID = new ArrayList<>();
                String next[];
                int count = 0;
                AIMSICD.mProgressBar.setProgress(0);
                AIMSICD.mProgressBar.setMax(csvCellID.size());
                while ((next = csvReader.readNext()) != null) {
                    csvCellID.add(next);
                    AIMSICD.mProgressBar.setProgress(count++);
                }

                AIMSICD.mProgressBar.setProgress(0);
                if (!csvCellID.isEmpty()) {
                    int lines = csvCellID.size();
                    for (int i = 1; i < lines; i++) {
                        AIMSICD.mProgressBar.setProgress(i);

                        // Insert details into OpenCellID Database
                        // Beware of negative values of "range" and "samples"!!
                        insertOpenCell( Double.parseDouble(csvCellID.get(i)[0]), // gps_lat
                                        Double.parseDouble(csvCellID.get(i)[1]), // gps_lon
                                        Integer.parseInt(csvCellID.get(i)[2]),   // MCC
                                        Integer.parseInt(csvCellID.get(i)[3]),   // MNC
                                        Integer.parseInt(csvCellID.get(i)[4]),   // LAC
                                        Integer.parseInt(csvCellID.get(i)[5]),   // CID (cellid) ?
                                        Integer.parseInt(csvCellID.get(i)[6]),   // avg_signal [dBm]
                                        //Integer.parseInt(csvCellID.get(i)[7]), // avg_range [m]
                                        Integer.parseInt(csvCellID.get(i)[8])    // samples
                                        //Integer.parseInt(csvCellID.get(i)[9]), // isGPSexact
                                        //Integer.parseInt(csvCellID.get(i)[10]), // RAT
                                        //Integer.parseInt(csvCellID.get(i)[11]), // --- RNC
                                        //Integer.parseInt(csvCellID.get(i)[12]), // --- (cid) ?
                                        //Integer.parseInt(csvCellID.get(i)[13]), // --- PSC
                                        //Integer.parseInt(csvCellID.get(i)[14]), // --- TAC
                                        //Integer.parseInt(csvCellID.get(i)[15]), // --- PCI
                                        //Integer.parseInt(csvCellID.get(i)[16]), // --- SID
                                        //Integer.parseInt(csvCellID.get(i)[17]), // --- NID
                                        //Integer.parseInt(csvCellID.get(i)[18]), // --- BID

                        );
                    }
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing OpenCellID data: " + e.getMessage());
            return false;
        } finally {
            AIMSICD.mProgressBar.setProgress(0);
        }
    }

    /**
     * Imports a previously exported CSV file into the database
     */

    // Rename to importDB ? (See Log TAG below)
    public boolean restoreDB() {
        try {
            for (String table : mTables) {
                File file = new File(FOLDER + "aimsicd-" + table + ".csv");
                if (file.exists()) {
                    List<String[]> records = new ArrayList<>();
                    String next[];
                    CSVReader csvReader = new CSVReader(new FileReader(file));
                    while ((next = csvReader.readNext()) != null) {
                        records.add(next);
                    }

                    if (!records.isEmpty()) {
                        int lines = records.size();
                        AIMSICD.mProgressBar.setMax(lines);
                        AIMSICD.mProgressBar.setProgress(0);
                        for (int i = 1; i < lines; i++) {
                            AIMSICD.mProgressBar.setProgress(i);
                            switch (table) {

                                // TODO: Please add // comments to describe each field!!
                                case CELL_TABLE:
                                    insertCell(
                                            Integer.parseInt(records.get(i)[1]),    //
                                            Integer.parseInt(records.get(i)[2]),    //
                                            Integer.parseInt(records.get(i)[3]),    //
                                            Double.parseDouble(records.get(i)[4]),  //
                                            Double.parseDouble(records.get(i)[5]),  //
                                            Integer.parseInt(records.get(i)[6]),    //
                                            Integer.valueOf(records.get(i)[7]),     //
                                            Integer.valueOf(records.get(i)[8]),     //
                                            Double.valueOf(records.get(i)[9]),      //
                                            Double.valueOf(records.get(i)[10]),     //
                                            Double.valueOf(records.get(i)[11]),     //
                                            String.valueOf(records.get(i)[10]),     //
                                            Long.valueOf(records.get(i)[11]));      //
                                    break;

                                case LOCATION_TABLE:
                                    insertLocation(
                                            Integer.parseInt(records.get(i)[1]),    //
                                            Integer.parseInt(records.get(i)[2]),    //
                                            Integer.parseInt(records.get(i)[3]),    //
                                            Double.parseDouble(records.get(i)[4]),  //
                                            Double.parseDouble(records.get(i)[5]),  //
                                            Integer.parseInt(records.get(i)[6]),    //
                                            String.valueOf(records.get(i)[7]));     //
                                    break;

                                case OPENCELLID_TABLE:
                                    insertOpenCell(
                                            Double.parseDouble(records.get(i)[1]),  //
                                            Double.parseDouble(records.get(i)[2]),  //
                                            Integer.parseInt(records.get(i)[3]),    //
                                            Integer.parseInt(records.get(i)[4]),    //
                                            Integer.parseInt(records.get(i)[5]),    //
                                            Integer.parseInt(records.get(i)[6]),    //
                                            Integer.parseInt(records.get(i)[7]),    //
                                            Integer.parseInt(records.get(i)[8]));   //
                                    break;

                                case SILENT_SMS_TABLE:
                                    Bundle bundle = new Bundle();
                                    bundle.putString("address",         String.valueOf(records.get(i)[1]));
                                    bundle.putString("display_address", String.valueOf(records.get(i)[2]));
                                    bundle.putString("message_class",   String.valueOf(records.get(i)[3]));
                                    bundle.putString("service_centre",  String.valueOf(records.get(i)[4]));
                                    bundle.putString("message",         String.valueOf(records.get(i)[5]));
                                    insertSilentSms(bundle);
                                    break;
                            }

                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "importDB() " + e);
            return false;
        } finally {
            AIMSICD.mProgressBar.setProgress(0);
        }

    }

    /**
     * Backup the database tables to CSV files (or monolithic dump file)
     *
     * @return boolean indicating backup outcome
     *
     * TODO: Change backup from using CSV files to using a complete SQLite dump
     * This might require using a shell command:
     *   # sqlite3 aimsicd.db '.dump' | gzip -c >aimsicd.dump.gz
     * To re-import use:
     *   # zcat aimsicd.dump.gz | sqlite3 aimsicd.db
     *
     */

    // Rename to exportDB ? (See Log TAG below)
    public boolean backupDB() {
        try {
            for (String table : mTables) {
                backup(table);
            }
            return true;
        } catch (Exception ioe) {
            Log.e(TAG, "exportDB() " + ioe);
            return false;
        }
    }

    /**
     * Exports the database tables to CSV files
     *
     * @param tableName String representing table name to export
     */

    // TODO: We should consider having a better file selector here, so that
    // the user can select his own location for storing the backup files.
    private void backup(String tableName) {
        Log.i(TAG, "Database Backup: " + DB_NAME);

        File dir = new File(FOLDER);
        if (!dir.exists()) { dir.mkdirs(); }
        File file = new File(dir, "aimsicd-" + tableName + ".csv");

        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            Log.d(TAG, "table name " + tableName);
            open();
            Cursor c = mDb.rawQuery("SELECT * FROM " + tableName, new String[0]);

            csvWrite.writeNext(c.getColumnNames());
            String[] rowData = new String[c.getColumnCount()];
            int size = c.getColumnCount();
            AIMSICD.mProgressBar.setProgress(0);
            AIMSICD.mProgressBar.setMax(size);
            while (c.moveToNext()) {
                for (int i = 0; i < size; i++) {
                    rowData[i] = c.getString(i);
                    AIMSICD.mProgressBar.setProgress(i);
                }
                csvWrite.writeNext(rowData);
            }

            csvWrite.close();
            c.close();

        } catch (Exception e) {
            Log.e(TAG, "Error exporting table: " + tableName + " " + e);
        } finally {
            AIMSICD.mProgressBar.setProgress(0);
        }

        Log.i(TAG, "Database Export complete.");
    }

    // =======================================================================================
    // TODO: @Tor, please add some comments, even if trivial.
    public void cleanseCellStrengthTables(long maxTime) {
        Log.d(TAG, "Cleaning "+CELL_SIGNAL_TABLE+" WHERE timestamp < "+maxTime);
        mDb.execSQL("DELETE FROM "+CELL_SIGNAL_TABLE+" WHERE timestamp < "+maxTime);
    }

    public void addSignalStrength(int cellID, int signal, Long timestamp) {
        ContentValues row = new ContentValues();
        row.put("cellID", cellID);
        row.put("signal", signal);
        row.put("timestamp", timestamp);
        mDb.insert(CELL_SIGNAL_TABLE, null, row);
    }

    public int countSignalMeasurements(int cellID) {
        Cursor c = mDb.rawQuery("SELECT COUNT(cellID) FROM " + CELL_SIGNAL_TABLE +" WHERE cellID=" + cellID, new String[0]);
        c.moveToFirst();
        return c.getInt(0);
    }

    public int getAverageSignalStrength(int cellID) {
        Cursor c = mDb.rawQuery("SELECT AVG(signal) FROM " + CELL_SIGNAL_TABLE +" WHERE cellID=" + cellID, new String[0]);
        c.moveToFirst();
        return c.getInt(0);
    }

    public Cursor getSignalStrengthMeasurementData() {
        return mDb.rawQuery("SELECT cellID, signal, timestamp FROM " + CELL_SIGNAL_TABLE +" ORDER BY timestamp DESC", new String[0]);
    }
    // =======================================================================================


    /**
     * DbHelper class for the SQLite Database functions
     */
    public class DbHelper extends SQLiteOpenHelper {

        DbHelper(Context context) {
            super(context, DB_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {

            /*
             * Cell Signal Measurements
             * TODO move table into column "DBi_measure::rx_signal"
             */
            database.execSQL("create table " +
                    CELL_SIGNAL_TABLE + " (" + COLUMN_ID + " integer primary key autoincrement, cellID INTEGER, signal INTEGER, timestamp INTEGER);");
            database.execSQL("create index cellID_index ON "+CELL_SIGNAL_TABLE+" (cellID);");
            database.execSQL("create index cellID_timestamp ON "+CELL_SIGNAL_TABLE+" (timestamp);");

            /*
             * Silent Sms Database
             */
            String SMS_DATABASE_CREATE = "create table " +
                    SILENT_SMS_TABLE + " (" + COLUMN_ID +
                    " integer primary key autoincrement, Address VARCHAR, Display VARCHAR, Class VARCHAR, "
                    + "ServiceCtr VARCHAR, Message VARCHAR, " +
                    "Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);";
            database.execSQL(SMS_DATABASE_CREATE);

             /*
              * Location Tracking Database
              * TODO: rename to TABLE_DBI_MEASURE ("DBi_measure")
              */
            String LOC_DATABASE_CREATE = "create table " +
                    LOCATION_TABLE + " (" + COLUMN_ID +
                    " integer primary key autoincrement, Lac INTEGER, CellID INTEGER, " +
                    "Net VARCHAR, Lat VARCHAR, Lng VARCHAR, Signal INTEGER, Connection VARCHAR, " +
                    "Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);";
            database.execSQL(LOC_DATABASE_CREATE);

            /*
             * Cell Information Tracking Database
             * TODO: rename to TABLE_DBI_BTS ("DBi_bts")
             */
            String CELL_DATABASE_CREATE = "create table " +
                    CELL_TABLE + " (" + COLUMN_ID +
                    " integer primary key autoincrement, Lac INTEGER, CellID INTEGER, " +
                    "Net INTEGER, Lat VARCHAR, Lng VARCHAR, Signal INTEGER, Mcc INTEGER, Mnc INTEGER, " +
                    "Accuracy REAL, Speed REAL, Direction REAL, NetworkType VARCHAR, " +
                    "MeasurementTaken VARCHAR, OCID_SUBMITTED INTEGER DEFAULT 0, " +
                    "Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);";
            database.execSQL(CELL_DATABASE_CREATE);

            /*
             * OpenCellID Cell Information Database
             * TODO: rename to TABLE_DBE_IMPORT ("DBe_import".)
             */
            String OPENCELLID_DATABASE_CREATE = "create table " +
                    OPENCELLID_TABLE + " (" + COLUMN_ID +
                    " integer primary key autoincrement, Lat VARCHAR, Lng VARCHAR, Mcc INTEGER, " +
                    "Mnc INTEGER, Lac INTEGER, CellID INTEGER, AvgSigStr INTEGER, Samples INTEGER, "
                    + "Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);";
            database.execSQL(OPENCELLID_DATABASE_CREATE);

            /*
             * Default MCC Location Database
             */
            String DEFAULT_MCC_DATABASE_CREATE = "create table " +
                    DEFAULT_MCC_TABLE + " (" + COLUMN_ID +
                    " integer primary key autoincrement, Country VARCHAR, Mcc INTEGER, "
                    + "Lat VARCHAR, Lng VARCHAR);";
            database.execSQL(DEFAULT_MCC_DATABASE_CREATE);

            /*
             * Repopulate the default MCC location table
             */
            populateDefaultMCC(database);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG,
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data"
            );
            db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + CELL_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + OPENCELLID_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + SILENT_SMS_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + DEFAULT_MCC_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + CELL_SIGNAL_TABLE);

            onCreate(db);
        }
    }

}

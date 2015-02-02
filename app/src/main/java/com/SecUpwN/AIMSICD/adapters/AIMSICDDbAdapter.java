package com.SecUpwN.AIMSICD.adapters;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.utils.Cell;
import com.SecUpwN.AIMSICD.utils.CMDProcessor;

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


/**
 * Brief:   Handles the AMISICD DataBase tables (creation, population, updates,
 *
 * Description:
 *
 *      This class handle all the AMISICD DataBase maintenance operations, like
 *      creation, population, updates, backup, restore and various selections.
 *
 *
 *
 * Current Issues:
 *
 *      As of 2015-01-01 we will start migrating from the old DB structure
 *      to the new one as detailed here:
 *      https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/215
 *      Please try to work on only one table at the time, before making
 *      new PRs or committing to "develop" branch.
 *
 *      [ ] We'd like to Export the entire DB (like a dump), so we need ...
 *      [ ] Clarify the difference between cell.getCID() and CellID (see insertCell() below.)
 *
 *  ChangeLog:
 *
 *      2015-01-22  E:V:A   Started DBe_import migration
 *      2015-01-23  E:V:A   ~~changed silent sms column names~~ NOT!
 *                          Added EventLog table
 *                          
 *
 *  Notes:
 *
 *  ======  !! IMPORTANT !!  ======================================================================
 *  For damn good reasons, we should try to stay with mDb.rawQuery() and NOT with mDb.query().
 *  In fact we should try to avoid the entire AOS SQLite API as much as possible, to keep our
 *  queries and SQL related clean, portable and neat. That's what most developers understand.
 *
 *  See:
 *  [1] http://stackoverflow.com/questions/1122679/querying-and-working-with-cursors-in-sqlite-on-android
 *  [2] http://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html#rawQuery%28java.lang.String,%20java.lang.String%5B%5D%29
 *  ===============================================================================================
 *
 *  +   Some examples we can use:
 *
 *   1) "Proper" style:
 *      rawQuery("SELECT id, name FROM people WHERE name = ? AND id = ?", new String[] {"David", "2"});
 *
 *   2) Hack style: (avoiding the use of "?")
 *      String q = "SELECT * FROM customer WHERE _id = " + customerDbId  ;
 *      Cursor mCursor = mDb.rawQuery(q, null);
 *
 *   3) Info on execSQL():
 *      Execute a single SQL statement that is NOT a SELECT or when passed with an argument a
 *      SELECT/INSERT/UPDATE/DELETE statement. Suggested use with: ALTER, CREATE or DROP.
 *
 *  +   A few words about DB "Cursors":
 *      http://developer.android.com/reference/android/database/Cursor.html
 *      http://stackoverflow.com/questions/3861558/what-are-the-benefits-of-using-database-cursor
 *      
 */

public class AIMSICDDbAdapter {

    public static final String FOLDER = Environment.getExternalStorageDirectory() + "/AIMSICD/";
    public static final int DATABASE_VERSION = 8; // Is this "pragma user_version;" ?

    private final Boolean MONO_DB_DUMP = true; // Also back-up DB with one monolithic dump file?

    private final String TAG = "AISMICD_DbAdaptor";
    private final String DB_NAME = "aimsicd.db";
    private static final String COLUMN_ID   = "_id"; // Underscore is no longer required...

    private final String LOCATION_TABLE     = "locationinfo";    // TABLE_DBI_MEASURE:DBi_measure (volatile)
    private final String CELL_TABLE         = "cellinfo";        // TABLE_DBI_BTS:DBi_bts (physical)
    //private final String OPENCELLID_TABLE   = "opencellid";      // TABLE_DBE_IMPORT:DBe_import
    private final String OPENCELLID_TABLE   = "opencellid";      // TABLE_DBE_IMPORT:DBe_import
    private final String TABLE_DEFAULT_MCC  = "defaultlocation"; // TABLE_DEFAULT_MCC:defaultlocation
    private final String SILENT_SMS_TABLE   = "silentsms";       // TABLE_SILENT_SMS:silentsms

    // cell tower signal strength collected by the device
    // ToDo: Remove this table and use "rx_signal" in the "TABLE_DBI_MEASURE:DBi_measure" table..
    private final String CELL_SIGNAL_TABLE  = "cellSignal";      // TABLE_DBI_MEASURE::DBi_measure:rx_signal

    // Some placeholders for the use of the new tables:

    // private final String TABLE_DBE_IMPORT  = "DBe_import";       // External: BTS import table
    // private final String TABLE_DBE_CAPAB   = "DBe_capabilities"; // External: MNO & BTS network capabilities
    // private final String TABLE_DBI_BTS     = "DBi_bts";          // Internal: (physical) BTS data
    // private final String TABLE_DBI_MEASURE = "DBi_measure";      // Internal: (volatile) network measurements
    // private final String TABLE_DEFAULT_MCC = "defaultlocation";  // Default MCC for each country
    // private final String TABLE_DET_FLAGS   = "DetectionFlags";   // Detection Flag description, settings and scoring table
    private final String TABLE_EVENTLOG    = "EventLog";          // Detection and general EventLog (persistent)
    // private final String TABLE_SECTORTYPE  = "SectorType";       // BTS tower sector configuration (Many CID, same BTS)
    // private final String TABLE_SILENTSMS   = "silentsms";        // Silent SMS details
    // private final String TABLE_CMEASURES   = "CounterMeasures";  // Counter Measures thresholds and description

    private final String[] mTables;
    private final DbHelper mDbHelper;
    private SQLiteDatabase mDb;
    private final Context mContext;

    private Cursor signalStrengthMeasurementDatA; // AS says this is never used. Can we remove it?


    /**
     * Description:
     *              These tables are the ones that can be individually backed-up or restored in
     *              the backupDB() and restoreDB(). That's why the pre-loaded tables are NOT
     *              backed up, nor restored. They are:
     *                      TABLE_DEFAULT_MCC
     *                      TABLE_DET_FLAGS
     *                      TABLE_DBE_CAPAB
     *                      TABLE_SECTORTYPE
     *
     * @param context   Tables that can be used in:  backupDB() and restoreDB()
     */
    public AIMSICDDbAdapter(Context context) {
        mContext = context;
        mDbHelper = new DbHelper(context);
        mTables = new String[]{
                // Oldies...
                LOCATION_TABLE,
                CELL_TABLE,
                OPENCELLID_TABLE,
                SILENT_SMS_TABLE,
                // New...
                /*TABLE_DBE_IMPORT,
                TABLE_DBI_BTS,
                TABLE_DBI_MEASURE,
                TABLE_EVENTLOG,
                TABLE_SILENTSMS,
                TABLE_CMEASURES*/
        };
    }

    public AIMSICDDbAdapter open() throws SQLException {
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    // ====================================================================
    //      Populate the DB tables  (INSERT)
    // ====================================================================


    public long insertSilentSms(Bundle bundle) {
        ContentValues smsValues = new ContentValues();
        smsValues.put("Address",    bundle.getString("address"));           // address
        smsValues.put("Display",    bundle.getString("display_address"));   // display
        smsValues.put("Class",      bundle.getString("class"));             // class
        smsValues.put("ServiceCtr", bundle.getString("service_centre"));    // SMSC
        smsValues.put("Message",    bundle.getString("message"));           // message

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
                return mDb.update( CELL_TABLE, cellValues, "CellID=?", new String[]{Integer.toString(cellID)} );
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
                return mDb.update(CELL_TABLE, cellValues,"CellID=?", new String[]{Integer.toString(cell.getCID())});
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
    long insertOpenCell(double latitude,
                        double longitude,
                        int mcc,
                        int mnc,
                        int lac,
                        int cellID,
                        int avgSigStr,
                        int range,      // new
                        int samples,
                        int isGPSexact, // new
                        String RAT      // new
                        //int rej_cause // new
                        ) {

        //Populate Content Values for Insert or Update
        ContentValues cellIDValues = new ContentValues();
        cellIDValues.put("Lat", latitude);
        cellIDValues.put("Lng", longitude);
        cellIDValues.put("Mcc", mcc);
        cellIDValues.put("Mnc", mnc);
        cellIDValues.put("Lac", lac);
        cellIDValues.put("CellID", cellID);
        cellIDValues.put("AvgSigStr", avgSigStr);
        cellIDValues.put("avg_range", range );          // new
        cellIDValues.put("Samples", samples);
        cellIDValues.put("isGPSexact", isGPSexact );    // new
        cellIDValues.put("Type", RAT );                 // new
        //cellIDValues.put("rej_cause", rej_cause );    // new

        // TODO: Do we need to remove this as well? (See below.)
        if (openCellExists(cellID)) {
            Log.v(TAG, "CID already in OCID DB (db update): " + cellID);
            return mDb.update(OPENCELLID_TABLE, cellIDValues,
                    "CellID=?", new String[]{Integer.toString(cellID)});
        /*
        // Move to:  CellTracker.java  see:
        // https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/290#issuecomment-72303486
        } else {
            //DETECTION 1  ???
            // TODO: Why are we inserting this into DBe_import?
            // This doesn't sound right, unless importing OCID
            // --E:V:A
            Log.v(TAG, "ALERT: CID -NOT- in OCID DB (db insert): " + cellID);
            return mDb.insert(OPENCELLID_TABLE, null, cellIDValues);
            */
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
     * TODO: What tests?
     *
     * @param cellId    This method deletes a cell with CID from CELL_TABLE
     * @return result of deleting that CID
     *
     */
    public int deleteCell(int cellId) {
        Log.i(TAG, "Cell deleted: " + cellId);
        return mDb.delete(CELL_TABLE, "CellID = ?", new String[]{ String.valueOf(cellId) });
    }



    // ====================================================================
    //      mDb.query statements (get)        SELECT
    // ====================================================================
    
    
    // =========== NEW ============================================================================
    // TODO: 
    public Cursor getEventLogData() {
        return mDb.query(TABLE_EVENTLOG,
                new String[]{"time", "LAC", "CID", "PSC", "gpsd_lat","gpsd_lon", "gpsd_accu", "DF_id", "DF_desc"},
                null, null, null, null, null
        );
    }


    // =========== OLD ============================================================================
    /**
     * Returns Silent SMS database (silentsms) contents
     */
    public Cursor getSilentSmsData() {
        return mDb.query(SILENT_SMS_TABLE,
                new String[]{"Address", "Display", "Class", "ServiceCtr", "Message", "Timestamp"},
                null, null, null, null, COLUMN_ID + " DESC"
        );
    }

    /**
     * Returns Cell Information (DBi_bts) database contents
     */
    public Cursor getCellData() {
        return mDb.query( CELL_TABLE,
                new String[]{"CellID", "Lac", "Net", "Lat", "Lng", "Signal", "Mcc", "Mnc",
                        "Accuracy", "Speed", "Direction"},
                null, null, null, null, null
        );
    }

    /**
     * Returns Cell Information for contribution to the OpenCellID Project
     *
     * Function:    Seem to Return a list of all rows where OCID_SUBMITTED is not 1.
     */
    public Cursor getOPCIDSubmitData() {
        return mDb.query( CELL_TABLE,
                new String[]{ "Lng", "Lat", "Mcc", "Mnc", "Lac", "CellID", "Signal", "Timestamp",
                        "Accuracy", "Speed", "Direction", "NetworkType"}, "OCID_SUBMITTED <> 1",
                null, null, null, null
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
     *
     * TODO:    Need to implement new items!!
     * URGENT:  Maybe not so urgent, but need looking after, since we added items.
     *
     * Used in:
     *          DbViewerFragment.java
     *          MapViewerOsmDroid.java
     *
     *
     */
    public Cursor getOpenCellIDData() {
        return mDb.query( OPENCELLID_TABLE,
                new String[]{"CellID", "Lac", "Mcc", "Mnc", "Lat", "Lng", "AvgSigStr", "Samples"},
                // avg_range, rej_cause, Type
                null, null, null, null, null
        );
    }

    /**
     * Returns Default MCC Locations (defaultlocation) database contents
     */
    public Cursor getDefaultMccLocationData() {
        return mDb.query( TABLE_DEFAULT_MCC,
                new String[]{"Country", "Mcc", "Lat", "Lng"}, null, null, null, null, null);
    }

    // ====================================================================
    //      Various DB operations
    // ====================================================================

    
    /**
     *  Description:    This checks if a cell with a given (CID,Lat,Lon,Signal) already exists
     *                  in the "locationinfo" (DBi_measure) database.
     */
    boolean locationExists(int cellID, double lat, double lng, int signal) {
        Cursor cursor = mDb.rawQuery("SELECT * FROM " + LOCATION_TABLE +
                        " WHERE CellID = " + cellID +
                        " AND Lat = " + lat + " AND Lng = " + lng + " AND Signal = " + signal, null);
        boolean exists = cursor.getCount() > 0;
        Log.i(TAG, "Cell exists in location table?: " + exists);
        cursor.close();
        return exists;
    }

    /**
     *  Description:    This checks if a cell with a given CID already exists
     *                  in the "cellinfo" (DBi_bts) database.
     */
    boolean cellExists(int cellID) {
        Cursor cursor = mDb.rawQuery("SELECT 1 FROM " + CELL_TABLE +
                        " WHERE CellID = " + cellID, null);
        boolean exists = cursor.getCount() > 0;
        Log.v(TAG, "CID: "+ cellID + " exists in local DB (DBi_bts) ?: " + exists);
        cursor.close();
        return exists;
    }

    /**
     *  Description:    This checks if a cell with a given CID already exists
     *                  in the "opencellid" (DBe_import) database.
     */
    public boolean openCellExists(int cellID) {
        Cursor cursor = mDb.rawQuery("SELECT * FROM " + OPENCELLID_TABLE +
                        " WHERE CellID = " + cellID, null);
        boolean exists = cursor.getCount() > 0;
        Log.v(TAG, "CID: " + cellID + " exists in OCID (DBe_import)?: " + exists);
        cursor.close();
        return exists;
    }

    public boolean checkLAC(Cell cell) {
        Cursor cursor = mDb.query( CELL_TABLE,
                new String[]{"Lac"}, "CellID=" + cell.getCID(), null,null,null,null);

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
                //Log.v(TAG, "LAC checked - no change.  CID:" + cell.getCID() + " LAC(DBi):" + cell.getLAC() +
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
     * 
     */
    public void ocidProcessed() {
        ContentValues ocidValues = new ContentValues();
        ocidValues.put("OCID_SUBMITTED", 1); // isSubmitted
        mDb.update(CELL_TABLE, ocidValues, "OCID_SUBMITTED<>?", new String[]{"1"}); // isSubmitted
    }

    public double[] getDefaultLocation(int mcc) {
        double[] loc = new double[2];
        Cursor cursor = mDb.rawQuery("SELECT Lat, Lng FROM " + TABLE_DEFAULT_MCC + " WHERE Mcc = " + mcc, null);

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

    /**
     * Prepares the CSV file used to upload to OCID server.
     * 
     */
    public boolean prepareOpenCellUploadData() {
        boolean result;
        // Q: Where is this? 
        // A: It is wherever your device has mounted its SDCard.
        //    For example:  /data/media/0/AIMSICD/OpenCellID
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
     *
     * Issues:  TODO: Check if we got a Lat/Lng confusion...
     *                  -- Seem ok, but the order is weird..
     *                  ~~ E:V:A
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
                defaultMccValues.put("Lng", csvMcc.get(i)[2]); // Lat bug?
                defaultMccValues.put("Lat", csvMcc.get(i)[3]); // Lng bug?
                db.insert(TABLE_DEFAULT_MCC, null, defaultMccValues);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error populating Default MCC Data: " + e);
        }
    }

    /**
     *  Description:    Parses the downloaded CSV from OpenCellID and uses it to populate "DBe_import".
     *                  ("opencellid" table.)
     *
     *
     *  Dependency:     RequestTask.java :: onPostExecute()
     *                  insertOpenCell()
     *  Issues:
     *
     *          [ ]     Progress is not shown or is the operation too quick?
     *
     *          [ ]     Why are we only populating 8 items out of 19?
     *                  From downloaded OCID CSV file:  (19 items)
     *
     *  NOTES:
     *
     *              a)  We do not include "rej_cause" in backups.
     *              b)
     *
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
     *   TODO:  Also we should probably change this function name from:
     *          "updateOpenCellID" to "populateDBe_import"
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
                Log.i(TAG, "updateOpenCellID: OCID CSV size (lines?): " + csvCellID.size() );
                while ((next = csvReader.readNext()) != null) {
                    csvCellID.add(next);
                    AIMSICD.mProgressBar.setProgress(count++);
                }

                AIMSICD.mProgressBar.setProgress(0);
                if (!csvCellID.isEmpty()) {
                    int lines = csvCellID.size();
                    for (int i = 1; i < lines; i++) {
                        AIMSICD.mProgressBar.setProgress(i);

                        // Insert details into OpenCellID Database using:  insertOpenCell()
                        // Beware of negative values of "range" and "samples"!!
                        insertOpenCell( Double.parseDouble(csvCellID.get(i)[0]), // gps_lat
                                        Double.parseDouble(csvCellID.get(i)[1]), // gps_lon
                                        Integer.parseInt(csvCellID.get(i)[2]),   // MCC
                                        Integer.parseInt(csvCellID.get(i)[3]),   // MNC
                                        Integer.parseInt(csvCellID.get(i)[4]),   // LAC
                                        Integer.parseInt(csvCellID.get(i)[5]),   // CID (cellid) ?
                                        Integer.parseInt(csvCellID.get(i)[6]),   // avg_signal [dBm]
                                        Integer.parseInt(csvCellID.get(i)[7]),   // avg_range [m]
                                        Integer.parseInt(csvCellID.get(i)[8]),   // samples
                                        Integer.parseInt(csvCellID.get(i)[9]),   // isGPSexact
                                        String.valueOf(csvCellID.get(i)[10])     // RAT
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

    //=============================================================================================
    // Database Backup and Restore
    //=============================================================================================

    /**
     *  Description:    Imports a previously exported CSV file into the database
     *
     *  Issue:
     *                  Rename to importDB ? (See Log TAG below)
     */
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
                                            // not sure about the naming of these, need CHECK!
                                            Double.parseDouble(records.get(i)[1]),  // lat
                                            Double.parseDouble(records.get(i)[2]),  // lng
                                            Integer.parseInt(records.get(i)[3]),    // mcc
                                            Integer.parseInt(records.get(i)[4]),    // mnc
                                            Integer.parseInt(records.get(i)[5]),    // lac
                                            Integer.parseInt(records.get(i)[6]),    // cid
                                            Integer.parseInt(records.get(i)[7]),    // avg_sig..
                                            Integer.parseInt(records.get(i)[8]),   // ); range
                                            Integer.parseInt(records.get(i)[9]),    // new  samples
                                            Integer.parseInt(records.get(i)[10]),   // new  isGPSexact
                                            String.valueOf(records.get(i)[11])      // new  RAT
                                            //Integer.parseInt(records.get(i)[10]),   // new  rej_cause
                                    );
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
     *  Description:    Dumps the entire aimsicd.db to a dump file called "aimsicd_dump.db".
     *
     *  Requires:
     *
     *  Dev Status:     INCOMPLETE !!  Either fix or do not try to use..
     *
     *  Template:       DebugLogs.java
     *
     *  Author:         E:V:A
     *
     *                  TODO: Change backup from using CSV files to/also using a complete SQLite dump
     *
     *        This might require using a shell command:
     *            # sqlite3 aimsicd.db '.dump' | gzip -c >aimsicd.dump.gz
     *        To re-import use:
     *            # zcat aimsicd.dump.gz | sqlite3 aimsicd.db
     *
     *
     * @return
     */
    private void dumpDB()  {
        File dumpdir = new File(FOLDER);
        //if (!dir.exists()) { dir.mkdirs(); }
        File file = new File(dumpdir, "aimsicd_dump.db");

        //Bad coding:
        String aimdir = "/data/data/com.SecUpwN.AIMSICD/databases/";
        //Context.getFilesDir().getPath("com.SecUpwN.AIMSICD/databases"); ????

        // We probably also need to test if we have the sqlite3 binary. (See Busybox checking code.)
        // Apparently pipes doesn't work from Java... (No idea why!?)
        //String execString = "/system/xbin/sqlite3 " + dir + "aimsicd.db '.dump' | gzip -c >" + file;

        // Both of these work, but "dump" makes an SQL file, whereas "backup" make a binary SQLite DB.
        //String execString = "/system/xbin/sqlite3 " + aimdir + "aimsicd.db '.dump' >" + file;
        String execString = "/system/xbin/sqlite3 " + aimdir + "aimsicd.db '.backup " +file + "'";

        try {
            Log.i(TAG, "dumpDB() Attempting to dump DB to: " + file + "\nUsing: \"" + execString + "\"\n");
            // We need SU here and cd...
            CMDProcessor.runSuCommand(execString);
            //CMDProcessor.runSuCommand(execString).getStdout(); // Need import!
            //Process process = Runtime.getRuntime().exec(execString);

        } catch (Exception e) {
            Log.e(TAG, "dumpDB() Failed to export DB dump file: " + e);
        }
        Log.i(TAG, "dumpDB() Database dumped to: " + file);

    }


    /**
     *  Description:    Backup the database tables to CSV files (or monolithic dump file)
     *
     * @return boolean indicating backup outcome
     *
     *
     */
    public boolean backupDB() {
        try {
            for (String table : mTables) {
                backup(table);
            }
            if (MONO_DB_DUMP) {
                dumpDB();
            }
            return true;
        } catch (Exception ioe) {
            Log.e(TAG, "backupDB() " + ioe);
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
        if (!dir.exists()) { dir.mkdirs(); }  // We should probably add some more error handling here.
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


    // ====================================================================
    //      Cleanup and filtering of DB tables
    // ====================================================================

    /**
     *  What:           This is the DBe_import data consistency check
     *
     *  Description:    This method checks each imported BTS data for consistency
     *                  and correctness according to general 3GPP LAC/CID/RAT rules
     *                  and according to the app settings:
     *
     *                  tf_settings         (currently hard-coded)
     *                  min_gps_precision   (currently hard-coded)
     *
     *                  So there are really two steps in this procedure:
     *                  a) Remove bad BTSs from DBe_import
     *                  b) Mark unsafe BTSs in the DBe_import with "rej_cause" value.
     *
     *                  See:    #253    http://tinyurl.com/lybrfxb
     *                          #203    http://tinyurl.com/mzgjdcz
     *
     *                  We filter:
     *
     *  Used:
     *                  RequestTask.java :: onPostExecute()
     *
     *  Issues:
     *
     *          [x] OPENCELLID_TABLE doesn't have a "Net" entry!
     *          [x] OPENCELLID_TABLE doesn't have a "Range" entry!
     *
     *          [ ] Look into "long CID" and "Short CID" for UMTS/LTE...
     *              http://wiki.opencellid.org/wiki/FAQ
     *
     *              The formula for the long cell ID is as follows:
     *                  Long CID = 65536 * RNC + CID
     *
     *              If you have the Long CID, you can get RNC and CID in the following way:
     *                  RNC = Long CID / 65536 (integer division)
     *                  CID = Long CID mod 65536 (modulo operation)
     *
     *  ChangeLog:
     *          2015-01-29  E:V:A   Added
     *
     *  TODO:   (1) Implement some kind of counter, to count how many cells was removed.
     *  TODO:   (2) Better description of what was removed.
     *  TODO:   (3) Give a return value for success/failure
     *  TODO:   (4) Implement the "rej_cause" check and UPDATE table.
     */
    //public void checkDBe( String tf_settings, int min_gps_precision ) {
    public void checkDBe() {
        // We hard-code these for now, but should be in the settings eventually
        int tf_settings=30;         // [days] Minimum acceptable number of days since "time_first" seen.
        int min_gps_precision=50;   // [m]    Minimum acceptable GPS accuracy in meters.

        String sqlq;                // SQL Query string

        //=============================================================
        //===  DELETE bad cells from BTS data
        //=============================================================

        Log.d("checkDBe()", "Attempting to delete bad import data from DBe_import table...");

        // =========== samples ===========
        sqlq = "DELETE FROM " + OPENCELLID_TABLE + " WHERE Samples < 1";
        mDb.rawQuery(sqlq, null);

        // =========== range (DBe_import::avg_range) ===========
        // TODO: OCID data marks many good BTS with a negative range so we can't use this yet.
        //sqlq = "DELETE FROM " + OPENCELLID_TABLE + " WHERE Range < 1";
        //mDb.rawQuery(sqlq, null);

        // =========== LAC ===========
        sqlq = "DELETE FROM " + OPENCELLID_TABLE + " WHERE Lac < 1";
        mDb.rawQuery(sqlq, null);

        // We should delete cells with CDMA (4) LAC not in [1,65534] but we can simplify this to:
        // Delete ANY cells with a LAC not in [1,65534]
        sqlq = "DELETE FROM " + OPENCELLID_TABLE + " WHERE Lac > 65534";
        mDb.rawQuery(sqlq, null);
        // Delete cells with GSM/UMTS/LTE (1/2/3/13 ??) (or all others?) LAC not in [1,65533]
        //sqlq = "DELETE FROM " + OPENCELLID_TABLE + " WHERE Lac > 65533 AND Type!='CDMA'";
        //mDb.rawQuery(sqlq, null);

        // =========== CID ===========
        sqlq = "DELETE FROM " + OPENCELLID_TABLE + " WHERE CellID < 1";
        mDb.rawQuery(sqlq, null);

        // We should delete cells with UMTS/LTE (3,13) CID not in [1,268435455] (0xFFF FFFF) but
        // we can simplify this to:
        // Delete ANY cells with a CID not in [1,268435455]
        sqlq = "DELETE FROM " + OPENCELLID_TABLE + " WHERE CellID > 268435455";
        mDb.rawQuery(sqlq, null);
        // Delete cells with GSM/CDMA (1-3,4) CID not in [1,65534]
        //sqlq = "DELETE FROM " + OPENCELLID_TABLE + " WHERE CellID > 65534 AND (Net!=3 OR Net!=13)";
        //mDb.rawQuery(sqlq, null);

        Log.i("checkDBe()", "Deleted BTS entries from DBe_import table with bad LAC/CID...");

        //=============================================================
        //===  UPDATE "rej_cause" in BTS data (DBe_import)
        //=============================================================

        // =========== isGPSexact ===========
        // NOTE!!  OCID present "changeable"=1 ==> isGPSexact (until we get new import!)
        // UPADTE opencellid SET rej_cause = rej_cause + 3 WHERE isGPSexact=1;

        // =========== avg_range ===========
        // "UPDATE opencellid SET rej_cause = rej_cause + 3 WHERE avg_range < " + min_gps_precision;

        // =========== time_first ===========
        // "UPDATE opencellid SET rej_cause = rej_cause + 1 WHERE time_first < " + tf_settings;

    }



    // =======================================================================================
    //      Signal Strengths Table
    // =======================================================================================
    public void cleanseCellStrengthTables(long maxTime) {
        Log.d(TAG, "Cleaning " + CELL_SIGNAL_TABLE + " WHERE timestamp < " + maxTime);
        mDb.execSQL("DELETE FROM " + CELL_SIGNAL_TABLE + " WHERE timestamp < " + maxTime);
    }

    public void addSignalStrength( int cellID, int signal, Long timestamp ) {
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


    /*****************************************************************************************
     *  What:           DbHelper class for the SQLite Database functions
     *
     *  Description:    This class creates all the tables and DB structure in aimsicd.db when
     *                  AIMSICD is first started or updated when DB version changed.
     *
     *  Issues:
     *              [ ] Migrate table creation to use an SQL file import instead.
     *                  This will simplify the maintenance of the tables and the
     *                  create create process.
     *
     *              [ ]
     *
     ******************************************************************************************/
    public class DbHelper extends SQLiteOpenHelper {

        DbHelper(Context context) {
            super(context, DB_NAME, null, DATABASE_VERSION);
        }
        

        // Create aimsicd.db table structure 
        @Override
        public void onCreate(SQLiteDatabase database) {

            //=============================================================
            //  OLD tables
            //=============================================================
            
            /**
             *  Table:      CELL_SIGNAL_TABLE
             *  What:       Cell Signal Measurements
             *  Columns:    _id,cellID,signal,timestamp
             *  
             *  TODO:     move table into column "DBi_measure::rx_signal"
             */
            database.execSQL("create table " + 
                    CELL_SIGNAL_TABLE + " (" + COLUMN_ID + 
                    " integer primary key autoincrement, " +
                    "cellID INTEGER, signal INTEGER, " +
                    "timestamp INTEGER);");
            database.execSQL("create index cellID_index ON " + CELL_SIGNAL_TABLE + " (cellID);");
            database.execSQL("create index cellID_timestamp ON " + CELL_SIGNAL_TABLE + " (timestamp);");

            /**
             *  Table:      SILENT_SMS_TABLE
             *  What:       Silent Sms Database
             *  Columns:    _id,Address,Display,Class,ServiceCtr,Message,Timestamp
             * 
             *  TODO:
             */
            String SMS_DATABASE_CREATE = "create table " +
                    SILENT_SMS_TABLE + " (" + COLUMN_ID +
                    " integer primary key autoincrement, " +
                    "Address VARCHAR, " +
                    "Display VARCHAR, " +
                    "Class VARCHAR, "
                    + "ServiceCtr VARCHAR, " +
                    "Message VARCHAR, " +
                    "Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);";
            database.execSQL(SMS_DATABASE_CREATE);

            /**
             *  Table:      LOCATION_TABLE
             *  What:       Location Tracking Database
             *  Columns:    _id,Lac,CellID,Net,Lat,Lng,Signal,Connection,Timestamp
             * 
             *  TODO: rename to TABLE_DBI_MEASURE ("DBi_measure")
             */
            String LOC_DATABASE_CREATE = "create table " +
                    LOCATION_TABLE + " (" + COLUMN_ID +
                    " integer primary key autoincrement, " +
                    "Lac INTEGER, CellID INTEGER, " +
                    "Net VARCHAR, " +
                    "Lat VARCHAR, " +
                    "Lng VARCHAR, " +
                    "Signal INTEGER, " +
                    "Connection VARCHAR, " +
                    "Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);";
            database.execSQL(LOC_DATABASE_CREATE);

            /**
             *  Table:      CELL_TABLE
             *  What:       Cell Information Tracking Database
             *  Columns:    _id,Lac,CellID,Net,Lat,Lng,Signal,Mcc,Mnc,Accuracy,Speed,Direction,NetworkType,MeasurementTaken,OCID_SUBMITTED,Timestamp
             *
             * TODO: rename to TABLE_DBI_BTS ("DBi_bts")
             */
            String CELL_DATABASE_CREATE = "create table " +
                    CELL_TABLE + " (" + COLUMN_ID +
                    " integer primary key autoincrement, " +
                    "Lac INTEGER, " +
                    "CellID INTEGER, " +
                    "Net INTEGER, " +
                    "Lat VARCHAR, " +
                    "Lng VARCHAR, " +
                    "Signal INTEGER, " +
                    "Mcc INTEGER, " +
                    "Mnc INTEGER, " +
                    "Accuracy REAL, " +
                    "Speed REAL, " +
                    "Direction REAL, " +
                    "NetworkType VARCHAR, " +
                    "MeasurementTaken VARCHAR, " +
                    "OCID_SUBMITTED INTEGER DEFAULT 0, " +
                    "Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);";
            database.execSQL(CELL_DATABASE_CREATE);


            /**
             *  Table:      OPENCELLID_TABLE
             *  What:       OpenCellID Cell Information Database
             *  Columns:    _id,Lat,Lng,Mcc,Mnc,Lac,CellID,AvgSigStr,Samples,Timestamp
             *
             *  Additional items (CSV):
             *              lat,lon,mcc,mnc,lac,cellid,averageSignalStrength,range,samples,changeable,radio,rnc,cid,psc,tac,pci,sid,nid,bid
             *  We need:
             *              lat,lon,mcc,mnc,lac,cellid,averageSignalStrength,range,samples,changeable,radio
             *
             *  Dependencies:
             *              updateOpenCellID
             *              + "backup"
             *
             *  ChengeLog:
             *          2015-01-29  E:V:A   Added:  CSV: range, changeable, radio  as:
             *                                      DBe: avg_range, isGPSexact, Type.
             *
             * TODO:    (1) rename to TABLE_DBE_IMPORT ("DBe_import".)
             * TODO:    (2) add more items from CSV file to table.
             */
            String OPENCELLID_DATABASE_CREATE = "create table " +
                    OPENCELLID_TABLE + " (" + COLUMN_ID +
                    " integer primary key autoincrement, " +
                    "Lat VARCHAR, " +
                    "Lng VARCHAR, " +
                    "Mcc INTEGER, " +
                    "Mnc INTEGER, " +
                    "Lac INTEGER, " +
                    "CellID INTEGER, " +
                    "AvgSigStr INTEGER, " +
                    "avg_range INTEGER, " +     // new
                    "Samples INTEGER, " +
                    "isGPSexact INTEGER, " +    // new
                    "Type TEXT, " +             // new
                    // "rej_cause"              // new
                    "Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp" +
                    //"Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp, " +
                    ");";
            database.execSQL(OPENCELLID_DATABASE_CREATE);

            /**
             *  Table:      TABLE_DEFAULT_MCC
             *  What:       MCC Location Database
             *  Columns:    _id,Country,Mcc,Lat,Lng
             */
            String DEFAULT_MCC_DATABASE_CREATE = "create table " +
                    TABLE_DEFAULT_MCC + " (" + COLUMN_ID +
                    " integer primary key autoincrement, " +
                    "Country VARCHAR, " +
                    "Mcc INTEGER, " + 
                    "Lat VARCHAR, " +
                    "Lng VARCHAR);";
            database.execSQL(DEFAULT_MCC_DATABASE_CREATE);

            //=============================================================
            //  NEW tables
            //=============================================================

            /**
             *  Table:      TABLE_EVENTLOG (EventLog)
             *  What:       Event Log Database
             *  Columns:    
             */
            String TABLE_EVENTLOG_CREATE = 
            "CREATE TABLE EventLog  (" +
                    "_id            INTEGER PRIMARY KEY AUTOINCREMENT," + 
                    "time     		TEXT NOT NULL,"  +
                    "LAC           	INTEGER NOT NULL," +
                    "CID           	INTEGER NOT NULL," +
                    "PSC           	INTEGER," +
                    "gpsd_lat      	REAL," +
                    "gpsd_lon      	REAL," +
                    "gpsd_accu     	INTEGER," +
                    "DF_id         	INTEGER," +
                    "DF_desc	    TEXT" +
            ");";
            database.execSQL(TABLE_EVENTLOG_CREATE);

            // Re-populate the default MCC location table
            populateDefaultMCC(database);

            // NEW ====================================================
            // Populate the Silent SMS table with test entry
            //populateSilentSMS(database);

            // Populate the Silent SMS table with test entry
            //populateSilentSMS(database);


        }

        // This function drops all tables when SQLIte version has been upped,
        // and then calls the table create process.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", and destroy all old data.");

            db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + CELL_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + OPENCELLID_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + SILENT_SMS_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEFAULT_MCC);
            db.execSQL("DROP TABLE IF EXISTS " + CELL_SIGNAL_TABLE);

            // 	db.execSQL("DROP TABLE IF EXISTS " + TABLE_DBE_IMPORT);
            // 	db.execSQL("DROP TABLE IF EXISTS " + TABLE_DBE_CAPAB);
            // 	db.execSQL("DROP TABLE IF EXISTS " + TABLE_DBI_BTS);
            // 	db.execSQL("DROP TABLE IF EXISTS " + TABLE_DBI_MEASURE);
            // 	db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEFAULT_MCC);
            // 	db.execSQL("DROP TABLE IF EXISTS " + TABLE_DET_FLAGS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTLOG);
            // 	db.execSQL("DROP TABLE IF EXISTS " + TABLE_SECTORTYPE);
            // 	db.execSQL("DROP TABLE IF EXISTS " + TABLE_SILENTSMS);
            // 	db.execSQL("DROP TABLE IF EXISTS " + TABLE_CMEASURES);

            onCreate(db);
        }

    }

}

package com.SecUpwN.AIMSICD.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.SparseArray;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.constants.DBTableColumnIds;
import com.SecUpwN.AIMSICD.service.CellTracker;
import com.SecUpwN.AIMSICD.smsdetection.AdvanceUserItems;
import com.SecUpwN.AIMSICD.smsdetection.CapturedSmsData;
import com.SecUpwN.AIMSICD.utils.CMDProcessor;
import com.SecUpwN.AIMSICD.utils.Cell;
import com.SecUpwN.AIMSICD.utils.MiscUtils;
import com.SecUpwN.AIMSICD.utils.Status;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import io.freefair.android.util.logging.AndroidLogger;
import io.freefair.android.util.logging.Logger;

/**
 * Description:
 * <p/>
 * This class handle all the AMISICD DataBase maintenance operations, like
 * creation, population, updates, backup, restore and various selections.
 * <p/>
 * <p/>
 * <p/>
 * Current Issues: TODO
 * <p/>
 * [ ] We'd like to Export the entire DB (like a dump), so we need ...
 * [x] Clarify the difference between cell.getCID() and CellID (see insertCell() below.)
 * [ ] prepareOpenCellUploadData() this needs to be updated and re-coded
 * [ ] all functions related to SignalStrengthTracker.java need to be updated
 * [ ] cleanseCellTable()  I think this is complete it's used in CellTracker.java
 * not SignalStrengthTracker.java
 * [ ] addSignalStrength( int cellID, int signal, String timestamp )
 * The timestamp is stored as String.valueOf(System.currentTimeMillis());
 * because the new db column for this is TEXT?
 * [ ] getAverageSignalStrength() // rx_signal
 * <p/>
 * <p/>
 * ChangeLog:
 * <p/>
 * 2015-01-22  E:V:A   Started DBe_import migration
 * 2015-01-23  E:V:A   ~~changed silent sms column names~~ NOT!
 * Added EventLog table
 * 2015-07-16  E:V:A   Post DB design migration cleanup, see special notes below and
 * ref issue #214.
 * <p/>
 * <p/>
 * <p/>
 * Notes:
 * <p/>
 * ======  !! IMPORTANT !!  ======================================================================
 * For damn good reasons, we should try to stay with mDb.rawQuery() and NOT with mDb.query().
 * In fact we should try to avoid the entire AOS SQLite API as much as possible, to keep our
 * queries and SQL related clean, portable and neat. That's what most developers understand.
 * <p/>
 * See:
 * [1] http://stackoverflow.com/questions/1122679/querying-and-working-with-cursors-in-sqlite-on-android
 * [2] http://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html#rawQuery%28java.lang.String,%20java.lang.String%5B%5D%29
 * ===============================================================================================
 * <p/>
 * +   Some examples we can use:
 * <p/>
 * 1) "Proper" style:
 * rawQuery("SELECT id, name FROM people WHERE name = ? AND id = ?", new String[] {"David", "2"});
 * <p/>
 * 2) Hack style: (avoiding the use of "?")
 * String q = "SELECT * FROM customer WHERE _id = " + customerDbId  ;
 * Cursor mCursor = mDb.rawQuery(q, null);
 * <p/>
 * 3) Info on execSQL():
 * Execute a single SQL statement that is NOT a SELECT or when passed with an argument a
 * SELECT/INSERT/UPDATE/DELETE statement. Suggested use with: ALTER, CREATE or DROP.
 * <p/>
 * +   A few words about DB "Cursors":
 * http://developer.android.com/reference/android/database/Cursor.html
 * http://stackoverflow.com/questions/3861558/what-are-the-benefits-of-using-database-cursor
 * <p/>
 * <p/>
 * ===============================================================================================
 * POST DB Overhaul Notes (by banjaxbanjo)
 * ===============================================================================================
 * <p/>
 * [x] Database is now kept open and is only closed on app exit (no need to .open/close )
 * ^^^ This is correct!  DO NOT CHANGE THIS BEHAVIOUR EVER!!
 * <p/>
 * [x] Default Locations now preloaded in DB
 * [x] BackupDB() is now working with all new tables
 * [x] RestoreDB() is now working with all new tables
 * [x] Download OCID is working with new DbeImport Table
 * [x] EventLog has been updated
 * [x] insertBTS/insertBtsMeasure replaces insertCell/insertLocation
 * [x] insertDBeImport replaces insertOpenCell
 * [x] insertEventLog replaces insertDetection
 * [x] returnEventLogData() replaces getEventLogData()
 * [x] returnSmsData( replaces getSilentSmsData()
 * [x] returnDBiBts() replaces getCellData()
 * [x] returnDBiMeasure() replaces getLocationData()
 * [x] returnDBeImport() replaces getOpenCellIDData()
 * [x] "updateOpenCellID" renamed to "populateDBe_import"
 * [x] removed populateDefaultMCC() as now these are preloaded
 * [x] restoreDB()/backupDB() now restores/backup with new tables
 * [x] removed: public class DbHelper extends SQLiteOpenHelper as we are now going with pre populated DB
 * [x] A lot of code refactored to suit new DB changes
 */
public final class AIMSICDDbAdapter extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1; // Is this "pragma user_version;" ?

    // TODO: This should be implemented as a SharedPreference...
    private static final Boolean MONO_DB_DUMP = true; // Also back-up DB with one monolithic dump file?

    private final Logger log = AndroidLogger.forClass(AIMSICDDbAdapter.class);
    private static final String DB_NAME = "aimsicd.db";

    private String[] mTables;
    private SQLiteDatabase mDb;
    private Context mContext;
    private SharedPreferences mPreferences;
    public static String mDatabasePath;
    public static String mExternalFilesDirPath;

    public AIMSICDDbAdapter(Context context) {
        super(context, DB_NAME, null, 1);
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mDatabasePath = mContext.getDatabasePath(DB_NAME).getAbsolutePath();
        mExternalFilesDirPath = mContext.getExternalFilesDir(null) + File.separator;
        //e.g. /storage/emulated/0/Android/data/com.SecUpwN.AIMSICD/

        // Create a new blank DB then write pre-compiled DB in assets folder to blank DB.
        // This will throw error on first create because there is no DB to open and this is normal.
        createDataBase();

        //return writable database
        mDb = SQLiteDatabase.openDatabase(mDatabasePath, null, SQLiteDatabase.OPEN_READWRITE);

        // This will return the database as open so we don't need to use .open . Then when app
        // is exiting we use new AIMSICDDbAdapter(getApplicationContext()).close(); to close it
        this.getWritableDatabase();
        mTables = new String[]{

                // I am trying to keep in same order and aimsicd.sql script
                // Only backing up useful tables, uncomment if you want to backup
                DBTableColumnIds.DEFAULT_LOCATION_TABLE_NAME,   // defaultLocation:     Default MCC for each country
                //DBTableColumnIds.API_KEYS_TABLE_NAME,         // API_keys:            API keys for OpenCellID, MLS etc.
                //DBTableColumnIds.COUNTER_MEASURES_TABLE_NAME, // CounterMeasures:     Counter Measures thresholds and description
                //DBTableColumnIds.DBE_CAPABILITIES_TABLE_NAME, // DBe_capabilities:    External: MNO & BTS network capabilities
                DBTableColumnIds.DBE_IMPORT_TABLE_NAME,         // DBe_import:          External: BTS import table
                DBTableColumnIds.DBI_BTS_TABLE_NAME,            // DBi_bts:             Internal: (physical) BTS data
                DBTableColumnIds.DBI_MEASURE_TABLE_NAME,        // DBi_measure:         Internal: (volatile) network measurements
                //DBTableColumnIds.DETECTION_FLAGS_TABLE_NAME,  // DetectionFlags:      Detection Flag description, settings and scoring table
                DBTableColumnIds.EVENTLOG_TABLE_NAME,           //                      Detection and general EventLog (persistent)
                //DBTableColumnIds.SECTOR_TYPE_TABLE_NAME,      // SectorType:          BTS tower sector configuration (Many CID, same BTS)
                DBTableColumnIds.DETECTION_STRINGS_TABLE_NAME,  //                      Detection strings to will be picked up in logcat
                DBTableColumnIds.SMS_DATA_TABLE_NAME,           //                      Silent SMS details
        };
    }

    /**
     * Description:     Creates an empty SQLite Database file on the system and rewrites it with
     * our own pre-fabricated AIMSICD.db.
     * <p/>
     * NOTES:           This is a modified version to suit of needs of this guys great guide on
     * how to build a pre compiled db for android. Cheers Juan-Manuel Fluxà
     * See:
     * http://www.reigndesign.com/blog/using-your-own-sqlite-database-in-android-applications/
     **/
    public boolean createDataBase() {
        if (!checkDataBase()) {
            // By calling this method, an empty database will be created into the default system path
            // of your application so that we can overwrite that database with our own database.
            this.getReadableDatabase();
            try {
                copyDataBase();
                log.info( "Database created");
                return true;
            } catch (IOException e) {
                log.error( "Error creating database", e);
                throw new Error("Error copying database", e);
            }

        }
        return false;
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {

        SQLiteDatabase checkDB = null;

        try {
            log.info( "Checking for db first install this will throw an error on install and is normal");
            checkDB = SQLiteDatabase.openDatabase(mDatabasePath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            log.error( "database not yet created", e);
        }

        if (checkDB != null) {
            checkDB.close();
            return true;
        }
        return false;
    }

    /**
     * Description:     Copies your database from your local assets-folder to the just created
     * empty database in the system folder, from where it can be accessed and handled.
     * This is done by transferring byte stream.
     */
    private void copyDataBase() throws IOException {
        // Open your local DB as the input stream
        InputStream myInput = mContext.getAssets().open(DB_NAME);
        // Open the empty DB as the output stream
        OutputStream myOutput = new FileOutputStream(mDatabasePath);

        // Transfer bytes from the input file to the output file
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public AIMSICDDbAdapter open() throws SQLException {
        mDb = this.getWritableDatabase();
        return this;
    }

    public void close() {
        mDb.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // To use foreign keys in SQLite we use:
        //db.execSQL("PRAGMA foreign_keys = ON;");
        // But this is already created inside SQL script!!
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }


    // ====================================================================
    //      Populate the DB tables  (INSERT)
    // ====================================================================

    /**
     * Description:     This is used in the AIMSICD framework Tests to delete cells.
     * see: ../src/androidTest/java/com.SecUpwN.test/.
     * <p/>
     * Issues:          TODO: See comments below!
     *
     * @param cellId This method deletes a cell with CID from CELL_TABLE
     * @return result of deleting that CID
     */
    public int deleteCell(int cellId) {
        log.info( "Deleted CID: " + cellId);
        // TODO Instead we need to delete this cell from DBi_measure, since:
        // we are using foreign_key enforced DB, that doesn't allow you to
        // remove Dbi_bts without corresponding DBi_measures that uses them.
        // Rewrite this query!
        return mDb.delete("DBi_bts", "CID=" + cellId, null);
    }

    /* ====================================================================
     *      mDb.query statements (get)        SELECT
     * ====================================================================
     *  IMPORTANT!  The SQL queries as presented here are not in the same order
     *              as in the DB tables themselves, therefore it may be hard
     *              to easily match the various items when using the Cursors
     *              as shown below.
     *
     *              For example, in the opencellid (DBe_import) table, the items are ordered as:
     *                 Lat,Lng,Mcc,Mnc,CellID,...
     *              whereas in the getOpenCellIDData() cursor, they are arranged as:
     *                 CellID,Lac,Mcc,Mnc,Lat,Lng,AvgSigStr,Samples
     *
     *              Thus when used in MapViewerOsmDroid.java at loadEntries() and
     *              loadOpenCellIDMarkers(), the index used there is completely different
     *              than what could be expected.
     *
     *  ISSUES:     [ ] To avoid un-necessary future code obscurity, we should rearrange
     *                  all Cursor queries to reflect the actual DB table order.
     *                  Todo: This is a tedious job...
     *                  TODO: Also AVOID using this type of Query!!
     *
     * ====================================================================
     */


    /**
     * Description:     Returns Cell Information (DBi_bts) database contents
     * this returns BTSs that we logged and is called from
     * MapViewerOsmDroid.java to display cells on map
     */
    public Cursor getCellData() {
        return returnDBiBts();
    }

    /**
     * Description:     Returns Cell Information for contribution to the OpenCellID project
     * <p/>
     * Function:        Return a list of all rows from the DBi_measure table
     * where isSubmitted is not 1.
     * <p/>
     * Dependencies:
     */
    public Cursor getOCIDSubmitData() {

        // This is used (prepareOpenCellUploadData) when uploading data to OCID
        // TODO: Use something like this instead... VVV Need testing may need ...,RAT FROM DBi_measure ..
        // @EVA created a new function getRatFromDBimeasure(CellId) to get RAT with CID
        //      this can be used when creating the upload data to add the RAT to the CSV file

        // IMPORTANT: Note the order of the items (to match CSV)!
        //TODO @EVA this is working but its returning 2 of each rows any ideas??
        String query = "SELECT DISTINCT MCC,MNC,LAC,CID,gpsd_lon,gpsd_lat,rx_signal,time,gpsd_accu FROM DBi_measure, DBi_bts WHERE isSubmitted <> 1 ORDER BY time;";
        //String query = "SELECT MCC,MNC,LAC,CID,gpsd_lon,gpsd_lat,rx_signal,time,gpsd_accu FROM DBi_bts WHERE isSubmitted <> 1"; // OLD query for reference
        return mDb.rawQuery(query, null);
    }


    // ====================================================================
    //      Various DB operations
    // ====================================================================

    /**
     * Description:    This take a "Cell" bundle (from API) as input and uses its CID to check
     * in the DBi_measure (?) if there is already an associated LAC. It then
     * compares the API LAC to that of the DBi_Measure LAC.
     * <p/>
     * Issues:     [ ] We should make all detections outside of AIMSICDDbAdapter.java in a
     * separate module as described in the diagram in GH issue #215.
     * https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/215
     * where it is referred to as "Detection Module" (DET)...
     * <p/>
     * [ ] Seem we're querying too much, when we only need items: 1,3,4,8,11
     * (Try to avoid over query to improve performance.)
     * <p/>
     * [ ]  V V V V
     * <p/>
     * This is using the LAC found by API and comparing to LAC found from a previous
     * measurement in the "DBi_measure". This is NOT depending on "DBe_import".
     * This works for now...but we probably should consider populating "DBi_measure"
     * as soon as the API gets a new LAC. Then the detection can be done by SQL,
     * and by just comparing last 2 LAC entries for same CID.
     */
    public boolean checkLAC(Cell cell) {
        String query = String.format("SELECT * FROM DBi_bts WHERE CID = %d", cell.getCID());  //CID

        Cursor bts_cursor = mDb.rawQuery(query, null);

        while (bts_cursor.moveToNext()) {
            // 1=LAC, 8=Accuracy, 11=Time
            if (cell.getLAC() != bts_cursor.getInt(bts_cursor.getColumnIndex("LAC"))) {
                log.info( "ALERT: Changing LAC on CID: " + cell.getCID()
                        + " LAC(API): " + cell.getLAC()
                        + " LAC(DBi): " + bts_cursor.getInt(bts_cursor.getColumnIndex("LAC")));

                bts_cursor.close();
                return false;
            } else {
                log.verbose( "LAC checked - no change on CID:" + cell.getCID()
                        + " LAC(API): " + cell.getLAC()
                        + " LAC(DBi): " + bts_cursor.getInt(bts_cursor.getColumnIndex("LAC")));
            }
        }
        bts_cursor.close();
        return true;
    }


    /**
     * Description:     UPDATE DBi_measure to indicate if OpenCellID DB contribution has been made
     */
    public void ocidProcessed() {
        ContentValues ocidValues = new ContentValues();
        ocidValues.put("isSubmitted", 1); // isSubmitted
        // TODO:    rewrite mDb.query to use mDb.rawQuery ??
        // Perhaps: "UPDATE DBi_measure VALUES isSubmitted=1 WHERE isSubmitted<>1;" ???
        mDb.update("DBi_measure", ocidValues, "isSubmitted<>?", new String[]{"1"}); // isSubmitted
    }


    /**
     * Description:     This returns all BTS in the DBe_import by current sim card network
     * rather than returning other bts from different networks and slowing
     * down map view
     * <p/>
     * Note:            TODO:   This might be unnecessary as the DBe_import should only use MCC/MNC
     * as currently used by SIM service provider
     */
    public Cursor returnOcidBtsByNetwork(int mcc, int mnc) {
        String query = String.format(
                "SELECT * FROM DBe_import WHERE MCC = %d AND MNC = %d", mcc, mnc);
        return mDb.rawQuery(query, null);
    }

    public double[] getDefaultLocation(int mcc) {
        String query = String.format(
                "SELECT lat, lon FROM defaultlocation WHERE MCC = %d", mcc);

        double[] loc = new double[2];
        Cursor cursor = mDb.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            loc[0] = Double.parseDouble(cursor.getString(cursor.getColumnIndex("lat")));
            loc[1] = Double.parseDouble(cursor.getString(cursor.getColumnIndex("lon")));
        } else {
            loc[0] = 0.0;
            loc[1] = 0.0;
        }
        if (cursor != null) {
            cursor.close();
        }
        return loc;
    }

    /**
     * Description:     Remove all but the last row, unless its CID is invalid...
     * <p/>
     * Dependencies:    CellTracker.java:  ( dbHelper.cleanseCellTable(); )
     * <p/>
     * Issues:          [ ] This will not work if: PRAGMA foreign_key=ON, then we need to delete
     * the corresponding DBi_measure entries before / as well.
     * <p/>
     * [ ] TODO: It is UNCLEAR why this is needed!! It's probably an artifact of old DB tables??
     * TODO: Consider changing or removing!
     * <p/>
     * Notes:           Do we need to clean LAC as well? (Test with airplane-mode or roaming)
     * - probably not since a APM would give both LAC and CID as "-1".
     */
    public void cleanseCellTable() {
        // This removes all but the last row in the "DBi_bts" table
        //"DELETE FROM DBi_bts WHERE _id NOT IN (SELECT MAX(_id) FROM DBi_bts) GROUP BY CID"
        mDb.execSQL("DELETE FROM DBi_bts WHERE _id NOT IN (SELECT MAX(_id) FROM DBi_bts)");

        // TODO: MOVE this, as this is only executed once!!
        // This removes erroneous BTS entries due to API giving you CID/LAC of "-1" or MAX_INT,
        // when either roaming, in airplane mode or during crappy hand-overs.
        String query2 = String.format("DELETE FROM DBi_bts WHERE CID = %d OR CID = -1",
                Integer.MAX_VALUE);
        mDb.execSQL(query2);
    }

    /**
     * Description:     Prepares the CSV file used to upload new data to the OCID server.
     * <p/>
     * Issues:          TODO:
     * [ ] Add "act" in upload data for the DBi_measure:RAT
     * [ ] function getOCIDSubmitData() is not fully working ==> DB join not yet implemented
     * [ ] skip (or change) progress bar, since CSV write is too fast to be seen.
     * <p/>
     * Note:            Q: Where is this file?
     * A: It is wherever your device has mounted its SDCard.
     * For example, in:  /data/media/0/AIMSICD/OpenCellID
     * <p/>
     * OCID CSV upload format:
     * "cellid"        = CID (in UMTS long format)
     * "measured_at"   = time
     * "rating"        = gpsd_accu
     * "act"           = RAT (TEXT):
     * 1xRTT, CDMA, eHRPD, IS95A, IS95B, EVDO_0, EVDO_A, EVDO_B,
     * UMTS, HSPA+, HSDPA, HSUPA, HSPA, LTE, EDGE, GPRS, GSM
     */
    public boolean prepareOpenCellUploadData() {
        boolean result;

        File dir = new File(mExternalFilesDirPath + "OpenCellID/");
        if (!dir.exists()) {
            result = dir.mkdirs();
            if (!result) {
                return false;
            }
        }
        File file = new File(dir, "aimsicd-ocid-data.csv");

        try {
            // Get data not yet submitted:
            Cursor c = getOCIDSubmitData();
            // Check if we have something to upload:
            if (c.getCount() > 0) {
                if (!file.exists()) {
                    result = file.createNewFile();
                    if (!result) {
                        c.close();
                        return false;
                    }

                    // OCID CSV upload format and items
                    // mcc,mnc,lac,cellid,lon,lat,signal,measured_at,rating,speed,direction,act,ta,psc,tac,pci,sid,nid,bid
                    CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                    // TODO: Add "act"
                    csvWrite.writeNext("mcc,mnc,lac,cellid,lon,lat,signal,measured_at,rating");

                    int size = c.getCount();
                    log.debug( "OCID UPLOAD: row count = " + size);

                    while (c.moveToNext()) {
                        csvWrite.writeNext(
                                String.valueOf(c.getInt(c.getColumnIndex("MCC"))),
                                String.valueOf(c.getInt(c.getColumnIndex("MNC"))),
                                String.valueOf(c.getInt(c.getColumnIndex("LAC"))),
                                String.valueOf(c.getInt(c.getColumnIndex("CID"))),
                                c.getString(c.getColumnIndex("gpsd_lon")),
                                c.getString(c.getColumnIndex("gpsd_lat")),
                                c.getString(c.getColumnIndex("rx_signal")),
                                c.getString(c.getColumnIndex("time")),
                                //c.getString(c.getColumnIndex("RAT")),                     // OCID: "act" TODO
                                String.valueOf(c.getInt(c.getColumnIndex("gpsd_accu"))));
                    }
                    csvWrite.close();
                    c.close();
                }
                return true;
            }
            c.close();
            return false;
        } catch (Exception e) {
            log.error( "prepareOpenCellUploadData(): Error creating OpenCellID Upload Data: ", e);
            return false;
        }
    }


    /**
     * Description:    Parses the downloaded CSV from OpenCellID and uses it to populate
     * "DBe_import" table.
     * <p/>
     * <p/>
     * Dependency:     RequestTask.java :: onPostExecute()
     * insertDBeImport()
     * Issues:
     * <p/>
     * [ ]     Progress bar is not shown or is the operation too quick to be seen?
     * [ ]     Why are we only populating 8 items out of 19?
     * From downloaded OCID CSV file:  (19 items)
     * <p/>
     * NOTES:
     * <p/>
     * a)  We do not include "rej_cause" in backups. set to 0 as default
     * b)  Unfortunately there are 2 important missing items in the OCID CSV file:
     * - "time_first"
     * - "time_last"
     * c)  In addition the OCID data often contain unexplained negative values for one or both of:
     * - "samples"
     * - "range"
     * <p/>
     * d) The difference between "Cellid" and "cid", is that "cellid" is the "Long CID",
     * consisting of RNC and a multiplier:
     * Long CID = 65536 * RNC + CID
     * See FAQ.
     * <p/>
     * ========================================================================
     * For details on available OpenCellID API DB values, see:
     * http://wiki.opencellid.org/wiki/API
     * http://wiki.opencellid.org/wiki/FAQ#Long_CellID_vs._short_Cell_ID
     * ========================================================================
     * # head -2 opencellid.csv
     * lat,lon,mcc,mnc,lac,cellid,averageSignalStrength,range,samples,changeable,radio,rnc,cid,psc,tac,pci,sid,nid,bid
     * <p/>
     * 0 lat                      TEXT
     * 1 lon                      TEXT
     * 2 mcc                      INTEGER
     * 3 mnc                      INTEGER
     * 4 lac                      INTEGER
     * 5 cellid                   INTEGER     (Long CID) = 65536 * RNC + CID
     * 6 averageSignalStrength    INTEGER     (rx_power)
     * 7 range                    INTEGER     (accu)
     * 8 samples                  INTEGER
     * 9 changeable               INTEGER     (isGPSexact)
     * 10 radio                   TEXT        (RAT)
     * 11 rnc                     INTEGER
     * 12 cid                     INTEGER     CID (Short)= "Long CID" mod 65536
     * 13 psc                     INTEGER
     * --------- vvv  See OCID API  vvv ---------
     * 14 tac                     -
     * 15 pci                     -
     * 16 sid                     -
     * 17 nid                     -
     * 18 bid                     -
     * <p/>
     * 54.63376,25.160243,246,3,20,1294,0,-1,1,1,GSM,,,,,,,,
     * ========================================================================
     */
    public boolean populateDBeImport() {
        // This was not finding the file on a Samsung S5
        // String fileName = Environment.getExternalStorageDirectory()+ "/AIMSICD/OpenCellID/opencellid.csv";
        String fileName = mContext.getExternalFilesDir(null) + File.separator + "OpenCellID/opencellid.csv";
        File file = new File(fileName);

        try {
            if (file.exists()) {

                CSVReader csvReader = new CSVReader(new FileReader(file));
                List<String[]> csvCellID = new ArrayList<>();
                String next[];

                while ((next = csvReader.readNext()) != null) {
                    csvCellID.add(next);
                }

                if (!csvCellID.isEmpty()) {
                    int lines = csvCellID.size();
                    log.info( "UpdateOpenCellID: OCID CSV size (lines): " + lines);

                    // TODO: WHAT IS THIS DOING?? (Why is it needed?)
                    // This counts how many CIDs we have in DBe_import
                    Cursor lCursor = mDb.rawQuery("SELECT CID, COUNT(CID) FROM DBe_import GROUP BY CID", null);
                    SparseArray<Boolean> lPresentCellID = new SparseArray<>();
                    if (lCursor.getCount() > 0) {
                        while (lCursor.moveToNext()) {
                            lPresentCellID.put(lCursor.getInt(0), true);
                        }
                    }
                    lCursor.close();


                    int rowCounter;
                    for (rowCounter = 1; rowCounter < lines; rowCounter++) {
                        // TODO: IS this needed!???
                        // Inserted into the table only unique values CID
                        // without opening additional redundant cursor before each insert.
                        if (lPresentCellID.get(Integer.parseInt(csvCellID.get(rowCounter)[5]), false)) {
                            continue;
                        }
                        // Insert details into OpenCellID Database using:  insertDBeImport()
                        // Beware of negative values of "range" and "samples"!!
                        String lat = csvCellID.get(rowCounter)[0],          //TEXT
                                lon = csvCellID.get(rowCounter)[1],          //TEXT
                                mcc = csvCellID.get(rowCounter)[2],          //int
                                mnc = csvCellID.get(rowCounter)[3],          //int
                                lac = csvCellID.get(rowCounter)[4],          //int
                                cellid = csvCellID.get(rowCounter)[5],       //int   long CID [>65535]
                                range = csvCellID.get(rowCounter)[6],        //int
                                avg_sig = csvCellID.get(rowCounter)[7],      //int
                                samples = csvCellID.get(rowCounter)[8],      //int
                                change = csvCellID.get(rowCounter)[9],       //int
                                radio = csvCellID.get(rowCounter)[10],       //TEXT
//                                rnc = csvCellID.get(rowCounter)[11],         //int
//                                cid = csvCellID.get(rowCounter)[12],         //int   short CID [<65536]
                                psc = csvCellID.get(rowCounter)[13];         //int

                        // TODO: WHAT IS THIS DOING? Can we remove?
                        // (There shouldn't be any bad PSCs in the import...)
                        int iPsc = 666;
                        if (psc != null && !psc.isEmpty()) {
                            iPsc = Integer.parseInt(psc);
                        }

                        //Reverse order 1 = 0 & 0 = 1
                        // what if ichange is 4? ~ agilob
                        int ichange = Integer.parseInt(change);
                        ichange = (ichange == 0 ? 1 : 0);

                        insertDBeImport(
                                "OCID",                     // DBsource
                                radio,                      // RAT
                                Integer.parseInt(mcc),      // MCC
                                Integer.parseInt(mnc),      // MNC
                                Integer.parseInt(lac),      // LAC
                                Integer.parseInt(cellid),   // CID (cellid) ?
                                iPsc,                       // psc
                                lat,                        // gps_lat
                                lon,                        // gps_lon
                                ichange,                    // isGPSexact
                                Integer.parseInt(avg_sig),  // avg_signal [dBm]
                                Integer.parseInt(range),    // avg_range [m]
                                Integer.parseInt(samples),  // samples
                                "n/a",                      // time_first  (not in OCID)
                                "n/a",                      // time_last   (not in OCID)
                                0                           // TODO: rej_cause , set default 0
                        );
                    }
                    log.debug( "PopulateDBeImport(): inserted " + rowCounter + " cells.");
                }
            } else {
                log.error( "Opencellid.csv file does not exist!");
            }
            return true;
        } catch (Exception e) {
            log.error( "Error parsing OpenCellID data: " + e.getMessage());
            return false;
        } finally {
            try {
                Thread.sleep(1000); // wait 1 second to allow user to see progress bar.
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    //=============================================================================================
    // Database Backup and Restore
    //=============================================================================================

    /**
     * Description:    Restores the database tables from a previously Exported CSV files.
     * One CSV file per table with the name:  "aimsicd-<table_name>.csv"
     * <p/>
     * Issues:         [ ]
     * <p/>
     * Notes:      1) Restoring the DB can be done from a monolithic SQLite3 DB by (check!):
     * # sqlite3 aimsicd.db <aimsicd.dump
     */
    public boolean restoreDB() {
        try {

            for (String table : mTables) {

                File file = new File(mExternalFilesDirPath + "aimsicd-" + table + ".csv");
                if (file.exists()) {
                    List<String[]> records = new ArrayList<>();
                    String next[];
                    CSVReader csvReader = new CSVReader(new FileReader(file));
                    while ((next = csvReader.readNext()) != null) {
                        records.add(next);
                    }

                    if (!records.isEmpty()) {
                        int lines = records.size();
                        for (int i = 1; i < lines; i++) {

                            switch (table) {
                                case "defaultlocation":
                                    try {
                                        insertDefaultLocation(
                                                records.get(i)[1],       // country
                                                Integer.parseInt(records.get(i)[2]),// MCC
                                                records.get(i)[3],       // lat
                                                records.get(i)[4]        // lon
                                        );
                                    } catch (Exception ee) {
                                        log.error( "RestoreDB: Error in insertDefaultLocation()", ee);
                                    }
                                    break;

                                case "API_keys":
                                    insertApiKeys(
                                            records.get(i)[1],           //name
                                            records.get(i)[2],           //type
                                            records.get(i)[3],           //key
                                            records.get(i)[4],           //time_add
                                            records.get(i)[5]            //time_exp
                                    );
                                    break;

                                case "CounterMeasures":
                                    insertCounterMeasures(
                                            records.get(i)[1],           //name
                                            records.get(i)[2],           //description
                                            Integer.parseInt(records.get(i)[3]),    //thresh
                                            Double.parseDouble(records.get(i)[4])   //thfine
                                    );
                                    break;

                                case "DBe_capabilities":
                                    insertDBeCapabilities(
                                            records.get(i)[1],           //MCC
                                            records.get(i)[2],           //MNC
                                            records.get(i)[3],           //LAC
                                            records.get(i)[4],           //op_name
                                            records.get(i)[5],           //band_plan
                                            records.get(i)[6]            //__EXPAND___
                                    );

                                    break;

                                case "DBe_import":
                                    try {
                                        insertDBeImport(
                                                records.get(i)[1],           // DBsource
                                                records.get(i)[2],           // RAT
                                                Integer.parseInt(records.get(i)[3]),    // MCC
                                                Integer.parseInt(records.get(i)[4]),    // MNC
                                                Integer.parseInt(records.get(i)[5]),    // LAC
                                                Integer.parseInt(records.get(i)[6]),    // CID
                                                Integer.parseInt(records.get(i)[7]),    // PSC ??
                                                records.get(i)[8],           // gps_lat
                                                records.get(i)[9],           // gps_lon
                                                Integer.parseInt(records.get(i)[10]),   // isGPSexact
                                                Integer.parseInt(records.get(i)[11]), // avg_range
                                                Integer.parseInt(records.get(i)[12]),   // avg_signal
                                                Integer.parseInt(records.get(i)[13]),   // samples
                                                records.get(i)[14],          // time_first
                                                records.get(i)[15],          // time_last
                                                0 //Integer.parseInt(records.get(i)[16])  // TODO: rej_cause
                                        );
                                    } catch (Exception ee) {
                                        log.error( "RestoreDB: Error in insertDBeImport()", ee);
                                    }
                                    break;

                                case "DBi_bts":
                                    try {
                                        insertBTS(
                                                Integer.parseInt(records.get(i)[1]),    // MCC
                                                Integer.parseInt(records.get(i)[2]),    // MNC
                                                Integer.parseInt(records.get(i)[3]),    // LAC
                                                Integer.parseInt(records.get(i)[4]),    // CID
                                                Integer.parseInt(records.get(i)[5]),    // PSC
                                                Integer.parseInt(records.get(i)[6]),    // T3212
                                                Integer.parseInt(records.get(i)[7]),    // A5x
                                                Integer.parseInt(records.get(i)[8]),    // ST_id
                                                records.get(i)[9],           // time_first
                                                records.get(i)[10],          // time_last
                                                Double.parseDouble(records.get(i)[11]), // lat
                                                Double.parseDouble(records.get(i)[12])  // lon
                                        );
                                    } catch (Exception ee) {
                                        log.error( "RestoreDB: Error in insertBTS()", ee);
                                    }
                                    break;

                                case "DBi_measure":
                                    try {
                                        insertDbiMeasure(
                                                Integer.parseInt(records.get(i)[1]),      // bts_id
                                                records.get(i)[2],             // nc_list
                                                records.get(i)[3],             // time
                                                records.get(i)[4],             // gpsd_lat
                                                records.get(i)[5],             // gpsd_lon
                                                Integer.parseInt(records.get(i)[6]),      // gpsd_accu
                                                records.get(i)[7],             // gpse_lat TODO: remove!
                                                records.get(i)[8],             // gpse_lon TODO: remove!
                                                records.get(i)[9],             // bb_power
                                                records.get(i)[10],            // bb_rf_temp
                                                records.get(i)[11],            // tx_power
                                                records.get(i)[12],            // rx_signal
                                                records.get(i)[13],            // rx_stype
                                                records.get(i)[14],            // RAT
                                                records.get(i)[15],            // BCCH
                                                records.get(i)[16],            // TMSI
                                                Integer.parseInt(records.get(i)[17]),     // TA
                                                Integer.parseInt(records.get(i)[18]),     // PD
                                                Integer.parseInt(records.get(i)[19]),     // BER
                                                records.get(i)[20],            // AvgEcNo
                                                Integer.parseInt(records.get(i)[21]),     // isSubmitted
                                                Integer.parseInt(records.get(i)[22])      // isNeighbour
                                                //records.get(i)[23].toString()           // TODO: con_state
                                        );
                                    } catch (Exception ee) {
                                        log.error( "RestoreDB: Error in insertDbiMeasure()", ee);
                                    }
                                    break;

                                case "DetectionFlags":
                                    insertDetectionFlags(
                                            Integer.parseInt(records.get(i)[1]),    //code
                                            records.get(i)[2],           //name
                                            records.get(i)[3],           //description
                                            Integer.parseInt(records.get(i)[4]),    //p1
                                            Integer.parseInt(records.get(i)[5]),    //p2
                                            Integer.parseInt(records.get(i)[6]),    //p3
                                            Double.parseDouble(records.get(i)[7]),  //p1_fine
                                            Double.parseDouble(records.get(i)[8]),  //p2_fine
                                            Double.parseDouble(records.get(i)[9]),  //p3_fine
                                            records.get(i)[10],          //app_text
                                            records.get(i)[11],          //func_use
                                            Integer.parseInt(records.get(i)[12]),   //istatus
                                            Integer.parseInt(records.get(i)[13])    //CM_id

                                    );
                                    break;

                                case "EventLog":
                                    insertEventLog(
                                            records.get(i)[1],           //time
                                            Integer.parseInt(records.get(i)[2]),    //LAC
                                            Integer.parseInt(records.get(i)[3]),    //CID
                                            Integer.parseInt(records.get(i)[4]),    //PSC
                                            records.get(i)[5],           //gpsd_lat
                                            records.get(i)[6],           //gpsd_lon
                                            Integer.parseInt(records.get(i)[7]),    //gpsd_accu
                                            Integer.parseInt(records.get(i)[8]),    //DF_id
                                            records.get(i)[9]            //DF_desc
                                    );
                                    break;

                                case "SectorType":
                                    insertSectorType(records.get(i)[1]);
                                    break;

                                case "DetectionStrings":
                                    insertDetectionStrings(
                                            records.get(i)[1],
                                            records.get(i)[2]);
                                    break;

                                case "SmsData":
                                    insertSmsData(
                                            records.get(i)[1],           //time
                                            records.get(i)[2],           //number
                                            records.get(i)[3],           //smsc
                                            records.get(i)[4],           //message
                                            records.get(i)[5],           //type
                                            records.get(i)[6],           //class
                                            Integer.parseInt(records.get(i)[7]),    //lac
                                            Integer.parseInt(records.get(i)[8]),    //cid
                                            records.get(i)[9],           //rat
                                            Double.parseDouble(records.get(i)[10]), //gps_lat
                                            Double.parseDouble(records.get(i)[11]), //gps_lon
                                            Integer.parseInt(records.get(i)[12])    //isRoaming
                                    );
                                    break;
                            }
                        }
                    }
                }
            }
            log.info( "RestoreDB() Finished");
            return true;
        } catch (Exception e) {
            log.error( "RestoreDB() Error", e);
            return false;
        }

    }

    /**
     * Description:    Dumps the internal aimsicd.db to a file called "aimsicd_dump.db".
     * <p/>
     * Requires:       root + SQLite3 binary
     * <p/>
     * Where?          Used in backupDB() and depend on the  MONO_DB_DUMP  boolean.
     * <p/>
     * Notes:  1) We probably also need to test if we have the sqlite3 binary. (See Busybox checking code.)
     * <p/>
     * 2) Apparently pipes doesn't work from Java... No idea why, as they appear to work
     * in the AtCommandFragment.java... for checking for /dev/ files.
     * <p/>
     * 3) We can use either ".dump" or ".backup", but "dump" makes an SQL file,
     * whereas "backup" make a binary SQLite DB.
     * <p/>
     * a) # sqlite3 aimsicd.db '.dump' | gzip -c >aimsicd.dump.gz
     * b) # sqlite3 aimsicd.db '.dump' >aimsicd.dump
     * c) # sqlite3 aimsicd.db '.backup aimsicd.back'
     * <p/>
     * execString = "/system/xbin/sqlite3 " + dir + "aimsicd.db '.dump' | gzip -c >" + file;
     * execString = "/system/xbin/sqlite3 " + aimdir + "aimsicd.db '.dump' >" + file;
     * execString = "/system/xbin/sqlite3 " + aimdir + "aimsicd.db '.backup " +file + "'";
     * <p/>
     * 4) To re-import use:
     * # zcat aimsicd.dump.gz | sqlite3 aimsicd.db
     */
    private void dumpDB() {

        File dumpdir = new File(mExternalFilesDirPath);
        //if (!dir.exists()) { dir.mkdirs(); }
        File file = new File(dumpdir, "aimsicd_dump.db");
        //Bad coding? (What is AOS standard?)
        //Context.getFilesDir().getPath("com.SecUpwN.AIMSICD/databases"); ????
        String aimdir = new File(mDatabasePath).getParent();
        String execString = "/system/xbin/sqlite3 " + aimdir + "aimsicd.db '.backup " + file + "'";

        try {
            log.info( "DumpDB() Attempting to dump DB to: " + file + "\nUsing: \"" + execString + "\"\n");
            CMDProcessor.runSuCommand(execString); // We need SU for this...
        } catch (Exception e) {
            log.error( "DumpDB() Failed to export DB dump file: ", e);
        }
        log.info( "DumpDB() Dumped internal database to: " + aimdir + file);
    }


    /**
     * Description:    Backup the database tables to CSV files (or monolithic dump file)
     * <p/>
     * Depends:        On the Boolean MONO_DB_DUMP to indicate if we want to try to
     * dump a monolithic DB using the rooted shell + sqlite3 binary
     * method above.
     *
     * @return boolean indicating backup outcome
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
            log.error( "BackupDB() Error: ", ioe);
            return false;
        }
    }

    /**
     * TODO:  Is this redundant? REMOVE?
     * Description:    Exports the database tables to CSV files
     * <p/>
     * Issues:         [ ] We should consider having a better file selector here, so that
     * the user can select his own location for storing the backup files.
     * [ ] Don't use progress bar for each column item, but instead each table.
     *
     * @param tableName String representing table name to export
     */
    private void backup(String tableName) {
        log.info( "Database Backup: " + DB_NAME);

        File dir = new File(mExternalFilesDirPath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                log.error( "Backup(): Cannot create directory structure to " + dir.getAbsolutePath());
            }
        }  // We should probably add some more error handling here.
        File file = new File(dir, "aimsicd-" + tableName + ".csv");

        try {
            log.info( "Backup(): Backup file was created? " + file.createNewFile());
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            log.debug( "DB backup() tableName: " + tableName);

            Cursor c = mDb.rawQuery("SELECT * FROM " + tableName, new String[0]);

            csvWrite.writeNext(c.getColumnNames());
            String[] rowData = new String[c.getColumnCount()];
            int size = c.getColumnCount();

            while (c.moveToNext()) {
                for (int i = 0; i < size; i++) {
                    rowData[i] = c.getString(i);
                }
                csvWrite.writeNext(rowData);
            }
            csvWrite.close();
            c.close();

        } catch (Exception e) {
            log.error( "Error exporting table: " + tableName, e);
        }
        log.info( "Backup(): Successfully exported DB table to: " + file);
    }


    // ====================================================================
    //      Cleanup and filtering of DB tables
    // ====================================================================

    /**
     * What:           This is the DBe_import data consistency check
     * <p/>
     * Description:    This method checks each imported BTS data for consistency
     * and correctness according to general 3GPP LAC/CID/RAT rules
     * and according to the app settings:
     * <p/>
     * tf_settings         (currently hard-coded)
     * min_gps_precision   (currently hard-coded)
     * <p/>
     * So there are really two steps in this procedure:
     * a) Remove bad BTSs from DBe_import
     * b) Mark unsafe BTSs in the DBe_import with "rej_cause" value.
     * <p/>
     * See:    #253    http://tinyurl.com/lybrfxb
     * #203    http://tinyurl.com/mzgjdcz
     * <p/>
     * We filter:
     * <p/>
     * Used:
     * RequestTask.java :: onPostExecute()
     * <p/>
     * Issues:
     * <p/>
     * [ ] Look into "long CID" and "Short CID" for UMTS/LTE...
     * http://wiki.opencellid.org/wiki/FAQ
     * <p/>
     * The formula for the long cell ID is as follows:
     * Long CID = 65536 * RNC + CID
     * <p/>
     * If you have the Long CID, you can get RNC and CID in the following way:
     * RNC = Long CID / 65536 (integer division)
     * CID = Long CID mod 65536 (modulo operation)
     * <p/>
     * TODO:   (1) Implement some kind of counter, to count how many cells was removed.
     * TODO:   (2) Better description of what was removed.
     * TODO:   (3) Give a return value for success/failure
     * TODO:   (4) Implement the "rej_cause" check and UPDATE table.
     * <p/>
     * Notes:   (a) By using rawQuery, we could count the number of items affected.
     * mDb.rawQuery(sqlq, null);
     * But rawQuery() is not executed until there is an associated Cursor operation!
     * <p/>
     * (b)
     * <p/>
     * <p/>
     * ChangeLog:
     * 2015-08-01  E:V:A           Updated Queries to reflect new DB structure
     */
    public void checkDBe() {
        // We hard-code these for now, but should be in the settings eventually
//        int tf_settings = 30;         // [days] Minimum acceptable number of days since "time_first" seen.
        int min_gps_precision = 50;   // [m]    Minimum acceptable GPS accuracy in meters.

        String sqlQuery;                // SQL Query string

        //=============================================================
        //===  DELETE bad cells from BTS data
        //=============================================================

        log.debug( "CheckDBe() Attempting to delete bad import data from DBe_import table...");

        // =========== samples ===========
        sqlQuery = "DELETE FROM DBe_import WHERE samples < 1";
        mDb.execSQL(sqlQuery);

        // =========== avg_range ===========
        // TODO: OCID data marks many good BTS with a negative range so we can't use this yet.
        // TODO: Also delete cells where the avg_range is way too large, say > 2000 meter
        //sqlQuery = "DELETE FROM DBe_import WHERE avg_range < 1 OR avg_range > 2000";
        //mDb.rawQuery(sqlQuery, null);

        // =========== LAC ===========
        sqlQuery = "DELETE FROM DBe_import WHERE LAC < 1";
        mDb.execSQL(sqlQuery);

        // We should delete cells with CDMA (4) LAC not in [1,65534] but we can simplify this to:
        // Delete ANY cells with a LAC not in [1,65534]
        sqlQuery = "DELETE FROM DBe_import WHERE LAC > 65534";
        mDb.execSQL(sqlQuery);

        // Delete cells with GSM/UMTS/LTE (1/2/3/13 ??) (or all others?) LAC not in [1,65533]
        //sqlQuery = "DELETE FROM DBe_import WHERE LAC > 65533 AND RAT != 'CDMA'";
        //mDb.rawQuery(sqlQuery, null);

        // =========== CID ===========
        sqlQuery = "DELETE FROM DBe_import WHERE CID < 1";
        mDb.execSQL(sqlQuery);

        // We should delete cells with UMTS/LTE (3,13) CID not in [1,268435455] (0xFFF FFFF) but
        // we can simplify this to:
        // Delete ANY cells with a CID not in [1,268435455]
        sqlQuery = "DELETE FROM DBe_import WHERE CID > 268435455";
        mDb.execSQL(sqlQuery);

        // Delete cells with GSM/CDMA (1-3,4) CID not in [1,65534]
        sqlQuery = "DELETE FROM DBe_import WHERE CID > 65534 AND (RAT='GSM' OR RAT='CDMA')";
        mDb.execSQL(sqlQuery);

        // SELECT count(*) from DBe_import;
        log.info("CheckDBe() Deleted BTS entries from DBe_import table with bad LAC/CID...");

        //=============================================================
        //===  UPDATE "rej_cause" in BTS data (DBe_import)
        //=============================================================

        // =========== isGPSexact ===========
        // Increase rej_cause, when:  the GPS position of the BTS is not exact:
        // NOTE:  In OCID: "changeable"=1 ==> isGPSexact=0
        sqlQuery = "UPDATE DBe_import SET rej_cause = rej_cause + 3 WHERE isGPSexact=0";
        mDb.execSQL(sqlQuery);

        // =========== avg_range ===========
        // Increase rej_cause, when:  the average range is < a minimum GPS precision
        sqlQuery = "UPDATE DBe_import SET rej_cause = rej_cause + 3 WHERE avg_range < " + min_gps_precision;
        mDb.execSQL(sqlQuery);

        // =========== time_first ===========
        // Increase rej_cause, when:  the time first seen is less than a number of days.
        // TODO: We need to convert tf_settings to seconds since epoch/unix time...
        //      int tf_settings = current_time[s] - (3600 * 24 * tf_settings) ???
        //sqlQuery = "UPDATE DBe_import SET rej_cause = rej_cause + 1 WHERE time_first < " + tf_settings;
        //mDb.execSQL(sqlQuery);

    }


    // =======================================================================================
    //      Signal Strengths Table
    // =======================================================================================

    /**
     * Description:     Remove too old signal strengths entries from DBi_measure table,
     * given a particular LAC,CID,PSC,RAT (or all?).
     * <p/>
     * TODO: Why do we need this at all?
     * <p/>
     * Note:            WARNING!    Do not remove based upon time only, as that would remove
     * all other measurement entries as well.
     * <p/>
     * Issues:          TODO:   timestamp in DBi_measure is a String,
     * but the one from SignalStrengthTracker is a long
     */

    public void cleanseCellStrengthTables(long maxTime) {
        log.debug("CleanseCellStrengthTables(): Cleaning DBi_measure WHERE time < " + maxTime);

        //TODO Change "time" to INTEGER in DB   -- currently not working
        String query = String.format(
                "DELETE FROM DBi_measure WHERE time < %d",
                maxTime);
        mDb.execSQL(query);
    }

    // TODO: Where is this used?
    public int getAverageSignalStrength(int cellID) {
        String query = String.format(
                // todo: "SELECT AVG(rx_signal) FROM DBi_measure,DBi_bts WHERE CID= %d",  // E:V:A
                "SELECT avg(rx_signal) FROM DBi_measure WHERE bts_id= %d",
                cellID);
        Cursor c = mDb.rawQuery(query, null);
        c.moveToFirst();
        int lAnswer = c.getInt(0);
        c.close();
        return lAnswer;
    }

    //=============================================================================================
    // ********************** ALL NEW FUNCTIONS ADDED AFTER THIS LINE *****************************
    //=============================================================================================

    // ====================================================================
    //      Get all detection strings
    // ====================================================================
    public ArrayList<AdvanceUserItems> getDetectionStrings() {

        Cursor cursor = mDb.rawQuery("SELECT * FROM DetectionStrings", null);

        ArrayList<AdvanceUserItems> detectionStrings = new ArrayList<>();

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                AdvanceUserItems setItems = new AdvanceUserItems();
                setItems.setDetection_string(cursor.getString(cursor.getColumnIndex("det_str")));
                setItems.setDetection_type(cursor.getString(cursor.getColumnIndex("sms_type")));
                detectionStrings.add(setItems);

            }
        } else {
            AdvanceUserItems setItems = new AdvanceUserItems();
            setItems.setDetection_string("No data");
            setItems.setDetection_type("No data");
            detectionStrings.add(setItems);
        }
        cursor.close();
        return detectionStrings;
    }

    public boolean deleteDetectedSms(long deleteme) {
        String TAG = "AIMSICD";
        String mTAG = "AIMSICDDbAdapter";

        try {
            mDb.delete("SmsData", "_id=" + deleteme, null);
            return true;
        } catch (Exception ee) {
            log.info(mTAG + ": Deleting SMS data failed", ee);
        }
        return false;
    }

    public boolean deleteDetectionString(String deleteme) {

        try {
            mDb.delete("DetectionStrings", "det_str='" + deleteme + "'", null);
            return true;
        } catch (Exception ee) {
            log.info("Deleting detection string failed", ee);
        }
        return false;

    }

    //====================================================================
    //      Insert new detection strings into database
    //====================================================================

    /**
     * Description:     When inserting strings it has to be in the format:
     * "i am a type 0 string". These strings can be found in main logcat.
     * <p/>
     * Issues:          [ ] Need to change time data type to INTEGER in DB
     * <p/>
     * NOTES:
     * <p/>
     * TYPE0 SILENTVOICE FLASH <--- These have to be in CAPS
     * ContentValues newconvalues = new ContentValues();
     * newconvalues.put(DETECTION_STRINGS_LOGCAT_STRING, "your string goes here");
     * newconvalues.put(DETECTION_STRINGS_SMS_TYPE, "TYPE0");
     * database.insert(DETECTION_STRINGS_TABLE_NAME,,null,newconvalues);
     * <p/>
     * ChangeLog:
     */
    public boolean insertNewDetectionString(ContentValues newString) {

        // First check that string not in DB
        String check4String = String.format(
                "SELECT * FROM DetectionStrings WHERE det_str = \"%s\"",
                newString.get("det_str").toString());

        Cursor cursor = mDb.rawQuery(check4String, null);
        final boolean exists = cursor.getCount() > 0;
        cursor.close();

        if (exists) {
            log.info("Detection String already in Database");
        } else {
            try {
                mDb.insert("DetectionStrings", null, newString);
                log.info("New detection string added.");
                return true;
            } catch (Exception ee) {
                log.info("Adding detection string Failed! ", ee);
            }
        }
        return false;
    }

    public CapturedSmsData storeCapturedSms(CapturedSmsData smsdata) {

        ContentValues values = new ContentValues();

        values.put("number", smsdata.getSenderNumber());
        values.put("message", smsdata.getSenderMsg());
        values.put("time", smsdata.getSmsTimestamp());
        values.put("type", smsdata.getSmsType());
        values.put("lac", smsdata.getCurrent_lac());
        values.put("cid", smsdata.getCurrent_cid());
        values.put("rat", smsdata.getCurrent_nettype());
        values.put("isRoaming", smsdata.getCurrent_roam_status());
        values.put("gps_lat", smsdata.getCurrent_gps_lat());
        values.put("gps_lon", smsdata.getCurrent_gps_lon());

        long insertId = mDb.insert("SmsData", null, values);
        smsdata.setId(insertId);
        return smsdata;
    }

    // Boolean Check if a give timestamp already exists in SmsData table
    public boolean isTimeStampInDB(String timestamp) {
        String check4timestamp = String.format(
                "SELECT time FROM SmsData WHERE time = \"%s\"", timestamp);
        Cursor cursor = mDb.rawQuery(check4timestamp, null);
        final boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    //====================================================================
    //      RETURN DATABASE CURSORS START HERE
    //
    // @banjaxbanjo:    (1) What do you mean with this actually?
    //                  (2) Where are these used?
    //                  (3) Are you making cursors?
    //
    // TODO:  Because of many changes to DB design, please keep these
    // TODO:  "Returned Columns" comments updated.
    //====================================================================

    /*
        Returned Columns:
        "_id"     	INTEGER PRIMARY KEY,
        "country"	TEXT,
        "MCC"    	INTEGER,
        "lat"    	TEXT,
        "lon"    	TEXT
        returns Default Mcc Locations
     */
    public Cursor returnDefaultLocation() {
        return mDb.rawQuery("SELECT * FROM defaultlocation", null);
    }

    /**
     * Returns DBe_import contents
     * <p/>
     * Used in:
     * DbViewerFragment.java
     * MapViewerOsmDroid.java
     * <p/>
     * Returned Columns:
     * "_id"        	INTEGER PRIMARY KEY AUTOINCREMENT,
     * "DBsource"  	TEXT NOT NULL,
     * "RAT"       	TEXT,
     * "MCC"       	INTEGER,
     * "MNC"       	INTEGER,
     * "LAC"       	INTEGER,
     * "CID"       	INTEGER,
     * "PSC"       	INTEGER,
     * "gps_lat"   	REAL,
     * "gps_lon"   	REAL,
     * "isGPSexact"	INTEGER,
     * "avg_range" 	INTEGER,
     * "avg_signal"	INTEGER,
     * "samples"   	INTEGER,
     * "time_first"	INTEGER,
     * "time_last" 	INTEGER,
     * "rej_cause" 	INTEGER
     */
    public Cursor returnDBeImport() {
        return mDb.rawQuery("SELECT * FROM DBe_import", null);
    }

    // TODO: THESE ARE OUTDATED!! Please see design and update
    /*
    Returned Columns:
        "_id"        	INTEGER PRIMARY KEY AUTOINCREMENT,
        "MCC"       	INTEGER NOT NULL,
        "MNC"       	INTEGER NOT NULL,
        "LAC"       	INTEGER NOT NULL,
        "CID"       	INTEGER NOT NULL,
        "PSC"       	INTEGER,
        "T3212"     	INTEGER,
        "A5x"       	INTEGER,
        "ST_id"     	INTEGER,
        "time_first"	INTEGER,
        "time_last" 	INTEGER,
        "LAT"	        REAL NOT NULL,  -- NULL should be okay, as GPS is not always available
        "LON"	        REAL NOT NULL
    */
    public Cursor returnDBiBts() {
        return mDb.rawQuery("SELECT * FROM DBi_bts", null);
    }

    // TODO: THESE ARE OUTDATED!! Please see design and update
    /*
    Returned Columns:
        "_id"           INTEGER PRIMARY KEY AUTOINCREMENT,
        "bts_id"       	INTEGER NOT NULL,
        "nc_list"      	TEXT,
        "time"         	TEXT NOT NULL,
        "gpsd_lat"     	TEXT NOT NULL,
        "gpsd_lon"     	TEXT NOT NULL,
        "gpsd_accuracy"	INTEGER,
        "gpse_lat"     	TEXT,
        "gpse_lon"     	TEXT,
        "bb_power"     	TEXT,
        "bb_rf_temp"   	TEXT,
        "tx_power"     	TEXT,
        "rx_signal"    	TEXT,
        "rx_stype"     	TEXT,
        "RAT"     	TEXT,
        "BCCH"         	TEXT,
        "TMSI"         	TEXT,
        "TA"           	INTEGER,
        "PD"           	INTEGER,
        "BER"          	INTEGER,
        "AvgEcNo"      	TEXT,
        "isSubmitted"  	INTEGER DEFAULT 0,
        "isNeighbour"  	INTEGER DEFAULT 0,
*/
    public Cursor returnDBiMeasure() {
        // This is special since this table is linked to DBi_bts by a foreign key,
        // so if you're not able to get LAC/CID, it's probably due to the FOREIGN KEY Pragma being OFF,
        // then try with:
        // "SELECT * FROM DBi_measure, DBi_bts WHERE DBi_measure.bts_id=DBi_bts.CID;"
        // NOTE:  TODO: bts_id should not be populated, then replace with "DBi_measure.bts_id=DBi_bts._id;"
        return mDb.rawQuery("SELECT * FROM DBi_measure", null);
    }

    /*
    Returned Columns
        "_id"         	INTEGER PRIMARY KEY,
        "code"       	INTEGER,
        "name"       	TEXT,
        "description"	TEXT,
        "p1"         	INTEGER,
        "p2"         	INTEGER,
        "p3"         	INTEGER,
        "p1_fine"    	REAL,
        "p2_fine"    	REAL,
        "p3_fine"    	REAL,
        "app_text"   	TEXT,
        "func_use"   	TEXT,
        "istatus"    	INTEGER,
        "CM_id"      	INTEGER
     */
    public Cursor returnDetectionFlags() {
        return mDb.rawQuery("SELECT * FROM DetectionFlags", null);
    }

    // TODO: THESE ARE OUTDATED!! Please see design and update

    /**
     * Returned Columns:
     * "_id"            	INTEGER PRIMARY KEY AUTOINCREMENT,
     * "time"     		TEXT NOT NULL,
     * "LAC"           	INTEGER NOT NULL,
     * "CID"           	INTEGER NOT NULL,
     * "PSC"           	INTEGER,
     * "gpsd_lat"      	TEXT,--Should this be double?
     * "gpsd_lon"      	TEXT,--Should this be double?
     * "gpsd_accu"     	INTEGER,
     * "DF_id"         	INTEGER,
     * "DF_description"	TEXT,
     */
    public Cursor returnEventLogData() {
        return mDb.rawQuery("SELECT * FROM EventLog", null);
    }

    /**
     * Returned Columns:
     * "_id"     	        INTEGER PRIMARY KEY AUTOINCREMENT,
     * "detection_str"   	TEXT,
     * "sms_type"			TEXT--(WapPush MWI TYPE0 etc..)
     */
    public Cursor returnDetectionStrings() {
        return mDb.rawQuery("SELECT * FROM DetectionStrings", null);
    }

    // TODO: THESE ARE OUTDATED!! Please see design and update
    /*
    Returned Columns:
    "_id"         INTEGER PRIMARY KEY AUTOINCREMENT,
    "time"        TEXT,
    "number"      TEXT,
    "smsc"        TEXT,
    "message"     TEXT,
    "type"     	  TEXT,        -- WapPush MWI TYPE0 etc..)
    "class"  	  TEXT,			--(CLASS 0 etc...)
    "lac"         INTEGER,
    "cid"         INTEGER,
    "rat"         TEXT,
    "gps_lat"     REAL,
    "gps_lon"     REAL,
    "isRoaming"   INTEGER
 */
    public Cursor returnSmsData() {
        return mDb.rawQuery("SELECT * FROM SmsData", null);
    }

    //----END OF RETURN DATABASE CURSORS------//

    //====================================================================
    //      START OF INSERT FUNCTIONS
    //====================================================================

    // TODO: Remove this, it's confusing and doesn't seem necessary!
    public void insertDefaultLocation(String country, int mcc, String lat, String lon) {

        ContentValues def_location = new ContentValues();
        def_location.put("country", country);
        def_location.put("MCC", mcc);
        def_location.put("lat", lat);
        def_location.put("lon", lon);

        // Check that the country and MCC not known in the DefaultLocation DB to avoid duplicates
        // TODO: @banjaxbanjo:  Please clarify its use:
        //                      Why would there be duplicates?
        //                      All countries are pre-loaded??
        String query = String.format(
                "SELECT * FROM defaultlocation WHERE country = \"%s\" AND MCC = %d ",
                country, mcc);

        Cursor cursor = mDb.rawQuery(query, null);
        if (cursor.getCount() <= 0) {
            // <= 0 means country is not in database yet
            mDb.insert("defaultlocation", null, def_location);
        }
        cursor.close();
    }

    public boolean insertApiKeys(String name,
                                 String type,
                                 String key,
                                 String time_add,
                                 String time_exp) {

        ContentValues ApiKeys = new ContentValues();
        ApiKeys.put("name", name);
        ApiKeys.put("type", type);
        ApiKeys.put("key", key);
        ApiKeys.put("time_add", time_add);
        ApiKeys.put("time_exp", time_exp);

        String query = String.format("SELECT * FROM API_keys WHERE key = \"%s\"", key);
        Cursor cursor = mDb.rawQuery(query, null);

        // Only insert a new key if the key not already in DB
        if (cursor.getCount() <= 0) {
            mDb.insert("API_keys", null, ApiKeys);
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public void insertCounterMeasures(String name, String description,
                                      int thresh, double thfine) {

        ContentValues counterMeasures = new ContentValues();
        counterMeasures.put("name", name);
        counterMeasures.put("description", description);
        counterMeasures.put("thresh", thresh);
        counterMeasures.put("thfine", thfine);

        mDb.insert("CounterMeasures", null, counterMeasures);
    }

    /*
        "MCC"       	TEXT,
        "MNC"       	TEXT,
        "LAC"       	TEXT,
        "op_name"   	TEXT,
        "band_plan" 	TEXT,
        "__EXPAND__"	TEXT

     */
    public void insertDBeCapabilities(String mcc, String mnc, String lac, String op_name,
                                      String bandPlan, String __EXPAND__) {

        ContentValues dbeCapabilities = new ContentValues();

        dbeCapabilities.put("MCC", mcc);
        dbeCapabilities.put("MNC", mnc);
        dbeCapabilities.put("LAC", lac);
        dbeCapabilities.put("op_name", op_name);
        dbeCapabilities.put("band_plan", bandPlan);
        dbeCapabilities.put("__EXPAND__", __EXPAND__);

        mDb.insert("DBe_capabilities", null, dbeCapabilities);
    }

    /**
     * Description:    This method is used to insert and populate the downloaded or previously
     * backed up OCID details into the DBe_import database table.
     * <p/>
     * It also prevents adding multiple entries of the same cell-id, when OCID
     * downloads are repeated.
     * <p/>
     * Issues:     [ ] None, but see GH issue #303 for a smarter OCID download handler.
     * <p/>
     * Notes:       a) Move to:  CellTracker.java  see:
     * https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/290#issuecomment-72303486
     * b) OCID CellID is of the "long form" (>65535) when available...
     * c) is also used to where CSV data is populating the opencellid table.
     * <p/>
     * -
     * <p/>
     * TODO:       @EVA update comments as not I don't think there is an issue with this
     * <p/>
     * banjaxbanjo: What do you mean?
     */
    public void insertDBeImport(String db_src, String rat, int mcc, int mnc, int lac,
                                int cid, int psc, String lat, String lon, int isGpsExact,
                                int avg_range, int avg_signal, int samples, String time_first,
                                String time_last, int rej_cause) {


        ContentValues dbeImport = new ContentValues();

        dbeImport.put("DBsource", db_src);
        dbeImport.put("RAT", rat);
        dbeImport.put("MCC", mcc);
        dbeImport.put("MNC", mnc);
        dbeImport.put("LAC", lac);
        dbeImport.put("CID", cid);
        dbeImport.put("PSC", psc);
        dbeImport.put("gps_lat", lat);
        dbeImport.put("gps_lon", lon);
        dbeImport.put("isGPSexact", isGpsExact);
        dbeImport.put("avg_range", avg_range);
        dbeImport.put("avg_signal", avg_signal);
        dbeImport.put("samples", samples);
        dbeImport.put("time_first", time_first);
        dbeImport.put("time_last", time_last);
        dbeImport.put("rej_cause", rej_cause);

        // Check that the LAC/CID is not already in the DBe_import (to avoid adding duplicate cells)
        String query = String.format(
                "SELECT LAC,CID FROM DBe_import WHERE LAC = %d AND CID = %d ",
                lac, cid);
        Cursor cursor = mDb.rawQuery(query, null);
        if (cursor.getCount() <= 0) { // <= 0 means cell is not in database yet
            mDb.insert("DBe_import", null, dbeImport);
        }
        cursor.close();
    }

    /**
     * Description:     Created this because we don't need to insert all the data in this table
     * since we don't yet have items like TMSI etc.
     */
    public void insertBTS(Cell cell) {

        // If LAC and CID are not already in DBi_bts, then add them.
        if (!cellInDbiBts(cell.getLAC(), cell.getCID())) {
            ContentValues values = new ContentValues();

            values.put("MCC", cell.getMCC());
            values.put("MNC", cell.getMNC());
            values.put("LAC", cell.getLAC());
            values.put("CID", cell.getCID());
            values.put("PSC", cell.getPSC());

            // TODO: setting these to 0 because if empty causing error in DB Restore
            values.put("T3212", 0);  // TODO
            values.put("A5x", 0);    // TODO
            values.put("ST_id", 0);  // TODO

            values.put("time_first", MiscUtils.getCurrentTimeStamp());
            values.put("time_last", MiscUtils.getCurrentTimeStamp());

            values.put("gps_lat", cell.getLat());  // TODO NO! These should be exact GPS from DBe_import or by manual addition!
            values.put("gps_lon", cell.getLon());  // TODO NO! These should be exact GPS from DBe_import or by manual addition!

            mDb.insert("DBi_bts", null, values);

            log.info("DBi_bts was populated.");

        } else {
            // If cell is already in the DB, update it to last time seen and
            // update its GPS coordinates, if not 0.0
            ContentValues values = new ContentValues();
            values.put("time_last", MiscUtils.getCurrentTimeStamp());

            // TODO NO! These should be exact GPS from DBe_import or by manual addition!
            // Only update if GPS coordinates are good
            if (Double.doubleToRawLongBits(cell.getLat()) != 0
                    && Double.doubleToRawLongBits(cell.getLat()) != 0
                    && Double.doubleToRawLongBits(cell.getLon()) != 0
                    && Double.doubleToRawLongBits(cell.getLon()) != 0) {
                values.put("gps_lat", cell.getLat());
                values.put("gps_lon", cell.getLon());
            }

            // This is the native update equivalent to:
            // "UPDATE Dbi_bts time_last=...,gps_lat=..., gps_lon=... WHERE CID=..."
            // update (String table, ContentValues values, String whereClause, String[] whereArgs)
            mDb.update("DBi_bts", values, "CID=?", new String[]{Integer.toString(cell.getCID())});

            log.info("DBi_bts updated: CID=" + cell.getCID() + " LAC=" + cell.getLAC());
        }

        // TODO: This doesn't make sense, if it's in DBi_bts it IS part of DBi_measure!
        // Checking to see if CID (now bts_id) is already in DBi_measure, if not add it.
        if (!cellInDbiMeasure(cell.getCID())) {
            ContentValues dbiMeasure = new ContentValues();

            dbiMeasure.put("bts_id", cell.getCID());                    // TODO: No!! Comment this out!
            dbiMeasure.put("nc_list", "no_data");                       // TODO: Better with "n/a" or "0", where are we getting this?
            dbiMeasure.put("time", MiscUtils.getCurrentTimeStamp());

            String slat = String.valueOf(cell.getLat());
            String slon = String.valueOf(cell.getLon());
            if (slat.isEmpty()) {
                slat = "0.0";
            }
            if (slon.isEmpty()) {
                slat = "0.0";
            }
            dbiMeasure.put("gpsd_lat", slat);
            dbiMeasure.put("gpsd_lon", slon);
            dbiMeasure.put("gpsd_accu", cell.getAccuracy());

            dbiMeasure.put("bb_power", "0");                            //TODO: This is not yet available, setting to "0"
            dbiMeasure.put("tx_power", "0");                             //TODO putting 0 here as we don't have this value yet
            dbiMeasure.put("rx_signal", String.valueOf(cell.getDBM())); //TODO putting cell.getDBM() here so we have some signal for OCID upload.
            dbiMeasure.put("RAT", String.valueOf(cell.getNetType()));
            dbiMeasure.put("TA", cell.getTimingAdvance());              //TODO does this actually get timing advance?
            dbiMeasure.put("BER", 0);                                    //TODO setting 0 because we don't have data yet.
            dbiMeasure.put("isSubmitted", 0);
            dbiMeasure.put("isNeighbour", 0);

            mDb.insert("DBi_measure", null, dbiMeasure);
            log.info("DBi_measure inserted bts_id=" + cell.getCID());  // TODO: NO!!

        } else {
            // Updating DBi_measure tables if already exists.
            //TODO commented out items are because we don't have this data yet or it doesn't need updating
            ContentValues dbiMeasure = new ContentValues();
            //dbiMeasure.put("nc_list", nc_list);
            //dbiMeasure.put("time", MiscUtils.getCurrentTimeStamp());

            if (Double.doubleToRawLongBits(cell.getLat()) != 0
                    && Double.doubleToRawLongBits(cell.getLon()) != 0) {
                dbiMeasure.put("gpsd_lat", cell.getLat());
                dbiMeasure.put("gpsd_lon", cell.getLon());
            }
            if (Double.doubleToRawLongBits(cell.getAccuracy()) != 0
                    && cell.getAccuracy() > 0) {
                dbiMeasure.put("gpsd_accu", cell.getAccuracy());
            }

            //if(cell.getDBM() > 0) { //TODO: Wrong, bb_power is not DBM!
            //    dbiMeasure.put("bb_power", String.valueOf(cell.getDBM()));
            //}

            //dbiMeasure.put("bb_rf_temp",bb_rf_temp);

            //TODO set correct value for tx_power and rx_signal and un comment when working again
            //  if(cell.getRssi() >0) {
            //    dbiMeasure.put("tx_power", String.valueOf(cell.getRssi()));
            //    dbiMeasure.put("rx_signal",String.valueOf(cell.getRssi()));
            //}
            if (cell.getDBM() > 0) {
                dbiMeasure.put("rx_signal", String.valueOf(cell.getDBM())); // [dBm]
            }

            //dbiMeasure.put("rx_stype",rx_stype);
            //dbiMeasure.put("RAT", String.valueOf(cell.getNetType()));
            //dbiMeasure.put("BCCH",BCCH);
            //dbiMeasure.put("TMSI",TMSI);
            if (cell.getTimingAdvance() > 0) {
                dbiMeasure.put("TA", cell.getTimingAdvance());  // Only available on API >16 on LTE
            }

            //dbiMeasure.put("PD",PD);                          // TODO: LTE: I need to check this...
            //dbiMeasure.put("BER",0);                          // getGsmBitErrorRate() for API 17+
            // See: http://developer.android.com/reference/android/telephony/SignalStrength.html

            //dbiMeasure.put("AvgEcNo",AvgEcNo);                // TODO: I need to check this...

            mDb.update("DBi_measure", dbiMeasure, "bts_id=?", new String[]{Integer.toString(cell.getCID())});
            log.info("DBi_measure updated bts_id=" + cell.getCID());

        }

    }


    /**
     * Inserts (API?) Cell Details into Database (DBi_bts)
     * Used be restoreDB()
     */
    // TODO: THESE ARE OUTDATED!! Please see design and update
    public void insertBTS(int mcc, int mnc, int lac, int cid, int psc, int t3212, int a5x,
                          int st_id, String time_first, String time_last, double lat, double lon) {

        if (cid != -1) {
            // Populate Content Values for INSERT or UPDATE
            ContentValues btsValues = new ContentValues();
            btsValues.put("MCC", mcc);
            btsValues.put("MNC", mnc);
            btsValues.put("LAC", lac);
            btsValues.put("CID", cid);
            btsValues.put("PSC", psc);
            btsValues.put("T3212", t3212);
            btsValues.put("A5x", a5x);
            btsValues.put("ST_id", st_id);
            btsValues.put("time_first", time_first);
            btsValues.put("time_last", time_last);
            btsValues.put("gps_lat", lat);
            btsValues.put("gps_lon", lon);

            // Only insert new cell if its not in DBi_bts
            if (!cellInDbiBts(lac, cid)) {
                mDb.insert("DBi_bts", null, btsValues);
            } else {
                // =======================================================================
                // TODO: EVA do I need to update an already known cell?
                // Good question! The DBi_bts is meant to be physical (non-volatile) and
                // stay the same, but I now see that PSC, T3212, A5x etc., might change,
                // under certain and special circumstances.
                // However, if they do, we should log this as an (suspicious?) event in EventLog
                // TODO: Addendum:
                // We could consider adding already known cells, if ANY of their parameters (not time)
                // has changed. At the moment this is only PSC, since we don't have the others...
                // =======================================================================
                mDb.update("DBi_bts", btsValues, "CID=?", new String[]{Integer.toString(cid)});
                log.info("Warning: Physical cell data in DBi_bts has changed! CID=" + cid);
            }
        }

    }

    /**
     * Description:     Inserts a measurement into the DBi_measure and DBi_bts tables
     * Used be restoreDB()
     * Issues:
     * [ ] Still not adding entry to DBi_bts
     * [ ] Where is this used and how?
     * [ ] MAYBE it's current use is still okay?
     * [ ] TODO: Using bts_id is WRONG! That is a foreign key id in the DBi_bts and used in DBi_measure
     */
    public void insertDbiMeasure(int bts_id,
                                 String nc_list,
                                 String time,
                                 String gpsd_lat,
                                 String gpsd_lon,
                                 int gpsd_accuracy,
                                 String gpse_lat,
                                 String gpse_lon,
                                 String bb_power,
                                 String bb_rf_temp,
                                 String tx_power,
                                 String rx_signal,
                                 String rx_stype,
                                 String rat,
                                 String BCCH,
                                 String TMSI,
                                 int TA,
                                 int PD,
                                 int BER,
                                 String AvgEcNo,
                                 int isSubmitted,
                                 int isNeighbour) {

        // Check if a bts_id is already stored in DBi_measure. Only adds new cell if false
        // @banjaxbanjo:  No this is wrong... We should always add new measurements
        //
        if (cellInDbiMeasure(bts_id)) {
            ContentValues dbiMeasure = new ContentValues();

            dbiMeasure.put("bts_id", bts_id);    // TODO: comment this out!

            dbiMeasure.put("nc_list", nc_list);
            dbiMeasure.put("time", time);
            dbiMeasure.put("gpsd_lat", gpsd_lat);
            dbiMeasure.put("gpsd_lon", gpsd_lon);
            dbiMeasure.put("gpsd_accu", gpsd_accuracy);
            dbiMeasure.put("gpse_lat", gpse_lat);
            dbiMeasure.put("gpse_lon", gpse_lon);
            dbiMeasure.put("bb_power", bb_power);
            dbiMeasure.put("bb_rf_temp", bb_rf_temp);
            dbiMeasure.put("tx_power", tx_power);
            dbiMeasure.put("rx_signal", rx_signal);
            dbiMeasure.put("rx_stype", rx_stype);
            dbiMeasure.put("RAT", rat);
            dbiMeasure.put("BCCH", BCCH);
            dbiMeasure.put("TMSI", TMSI);
            dbiMeasure.put("TA", TA);
            dbiMeasure.put("PD", PD);
            dbiMeasure.put("BER", BER);
            dbiMeasure.put("AvgEcNo", AvgEcNo);
            dbiMeasure.put("isSubmitted", isSubmitted);
            dbiMeasure.put("isNeighbour", isNeighbour);

            mDb.insert("DBi_measure", null, dbiMeasure);
            // TODO: try with:
            // mDb.insert("DBi_measure, DBi_bts", null, dbiMeasure); // but you'll need the DBi_bts data then as well...
        }

    }

    /**
     * Description:     This inserts Detection Flag data that is used to fine tune the various
     * available detection mechanisms. (See Detection List in issue #230)
     * <p/>
     * These parameters are described elsewhere, but should be included here
     * for completeness and reference.
     * <p/>
     * There's also a CounterMeasure id to link to possible future counter measures.
     * Issues:
     * [ ]
     */
    public void insertDetectionFlags(int code,
                                     String name,
                                     String description,
                                     int p1, int p2, int p3,
                                     double p1_fine, double p2_fine, double p3_fine,
                                     String app_text,
                                     String func_use,
                                     int istatus,
                                     int CM_id
    ) {

        ContentValues detectionFlags = new ContentValues();
        detectionFlags.put("code", code);
        detectionFlags.put("name", name);
        detectionFlags.put("description", description);
        detectionFlags.put("p1", p1);
        detectionFlags.put("p2", p2);
        detectionFlags.put("p3", p3);
        detectionFlags.put("p1_fine", p1_fine);
        detectionFlags.put("p2_fine", p2_fine);
        detectionFlags.put("p3_fine", p3_fine);
        detectionFlags.put("app_text", app_text);
        detectionFlags.put("func_use", func_use);
        detectionFlags.put("istatus", istatus);
        detectionFlags.put("CM_id", CM_id);

        mDb.insert("DetectionFlags", null, detectionFlags);
    }

    /**
     * Description:     Inserts log data into the EventLog table, using data provided by
     * the TelephonyManager (TM) or an already backed up EvenLog database...
     * <p/>
     * If you just need to add an event with currently connected TM data such
     * as CID,LAC,PSC,GPS, then use the simple version called toEventLog(),
     * defined below.
     * <p/>
     * Used in:         AIMSICDDbAdapter.java (here)
     * CellTracker.java
     * SmsDetector.java
     * <p/>
     * Issues:          [ ] ALL events should be logged!!
     * [ ] To avoid repeated copies, only check last DB entries
     * [ ] Before inserting event, check that LAC/CID are not "-1".
     * [ ] Any related notifications are better put here as well, right?
     * [ ]
     * <p/>
     * Description of EventLog DF_id:   TODO: check and fix
     * <p/>
     * DF_id   DF_desc
     * ---------------
     * 1       changing lac
     * 2       cell no in OCID
     * 3       "Detected Type-0 SMS"
     * 4       "Detected MWI SMS"
     * 5       "Detected WAP PUSH SMS"
     * 6       "Detected WAP PUSH (2) SMS"
     * 7
     * <p/>
     * Notes:           a)  Table item order:
     * <p/>
     * time,LAC,CID,PSC,gpsd_lat,gpsd_lon,gpsd_accu,DF_id,DF_desc
     * <p/>
     * b)  We need to check if cell is not in OCID  Events are not continuously
     * logged to the database as it currently stands. If the same cell shows
     * up it will again be dumped to the event log and will fill up pretty
     * quickly.
     * <p/>
     * banjaxobanjo What do you mean here? ALL events should be logged!
     * <p/>
     * c) To select last lines use:
     * <p/>
     * SELECT * FROM EventLog WHERE LAC=30114 AND CID=779149 AND DF_id=1 ORDER BY _id DESC LIMIT 1;
     * SELECT * FROM EventLog WHERE _id=(SELECT max(_id) FROM EventLog) AND LAC=30114 AND CID=779149 AND DF_id=1;
     * <p/>
     * <p/>
     * Query examples for future devs:
     * <p/>
     * SELECT * FROM EventLog WHERE CID = 1234 AND LAC = 4321 AND DF_id BETWEEN 1 AND 4
     * SELECT * FROM EventLog WHERE CID = 1234 AND LAC = 4321 AND DF_id = 1" Changing LAC
     * SELECT * FROM EventLog WHERE CID = 1234 AND LAC = 4321 AND DF_id = 2" Cell not in OCID
     * SELECT * FROM EventLog WHERE CID = 1234 AND LAC = 4321 AND DF_id = 3" Detected SMS
     * SELECT * FROM EventLog WHERE CID = 1234 AND LAC = 4321 AND DF_id = 4" Unknown T.B.A...     *
     */
    public void insertEventLog(String time,
                               int lac,
                               int cid,
                               int psc,
                               String gpsd_lat,
                               String gpsd_lon,
                               int gpsd_accu,
                               int DF_id,
                               String DF_description) {

        if (cid != -1) { // skip CID of "-1" (due to crappy API or roaming or Air-Plane Mode)
            // ONLY check if LAST entry is the same!
            String query = String.format(
                    // "SELECT * FROM EventLog WHERE LAC=%d AND CID=%d AND DF_id=%d ORDER BY _id DESC LIMIT 1",
                    "SELECT * from EventLog WHERE _id=(SELECT max(_id) from EventLog) AND CID=%d AND LAC=%d AND DF_id=%d",
                    // was: "SELECT * FROM EventLog WHERE CID = %d AND LAC = %d AND DF_id = %d",
                    cid, lac, DF_id);
            Cursor cursor = mDb.rawQuery(query, null);

            boolean insertData = true;
            if (cursor.getCount() > 0) {
                insertData = false;
            }
            cursor.close();

            if (insertData) {
                ContentValues eventLog = new ContentValues();

                eventLog.put("time", time);
                eventLog.put("LAC", lac);
                eventLog.put("CID", cid);
                eventLog.put("PSC", psc);
                eventLog.put("gpsd_lat", gpsd_lat);
                eventLog.put("gpsd_lon", gpsd_lon);
                eventLog.put("gpsd_accu", gpsd_accu);
                eventLog.put("DF_id", DF_id);
                eventLog.put("DF_description", DF_description);

                mDb.insert("EventLog", null, eventLog);
                log.info("InsertEventLog(): Insert detection event into EventLog table with CID=" + cid);
            } else {
                // TODO This may need to be removed as it may spam the logcat buffer...
                log.verbose("InsertEventLog(): Skipped inserting duplicate event into EventLog table with CID=" + cid);
            }
        }
    }


    // Defining a new simpler version of insertEventLog for use in CellTracker.
    // Please note, that in AMSICDDbAdapter (here) it is also used to backup DB,
    // in which case we can not use this simpler version!
    public void toEventLog(int DF_id, String DF_desc) {

        String time = MiscUtils.getCurrentTimeStamp();                    // time
        int lac = CellTracker.mMonitorCell.getLAC();                // LAC
        int cid = CellTracker.mMonitorCell.getCID();                // CID
        int psc = CellTracker.mMonitorCell.getPSC();                // PSC [UMTS,LTE]
        String gpsd_lat = String.valueOf(CellTracker.mMonitorCell.getLat());// gpsd_lat
        String gpsd_lon = String.valueOf(CellTracker.mMonitorCell.getLon());// gpsd_lon
        int gpsd_accu = (int) CellTracker.mMonitorCell.getAccuracy();        // gpsd_accu

        // skip CID/LAC of "-1" (due to crappy API, Roaming or Air-Plane Mode)
        if (cid != -1 || lac != -1) {
            // Check if LAST entry is the same!
            String query = String.format(
                    "SELECT * from EventLog WHERE _id=(SELECT max(_id) from EventLog) AND CID=%d AND LAC=%d AND PSC=%d AND DF_id=%d",
                    cid, lac, psc, DF_id);
            Cursor cursor = mDb.rawQuery(query, null);

            // WARNING: By skipping duplicate events, we might be missing counts of Type-0 SMS etc.
            boolean insertData = true;
            if (cursor.getCount() > 0) {
                insertData = false;
            }
            cursor.close();

            if (insertData) {
                ContentValues eventLog = new ContentValues();

                eventLog.put("time", time);                // time
                eventLog.put("LAC", lac);                    // LAC
                eventLog.put("CID", cid);                    // CID
                eventLog.put("PSC", psc);                    // PSC [UMTS,LTE]
                eventLog.put("gpsd_lat", gpsd_lat);        // gpsd_lat
                eventLog.put("gpsd_lon", gpsd_lon);        // gpsd_lon
                eventLog.put("gpsd_accu", gpsd_accu);        // gpsd_accu
                eventLog.put("DF_id", DF_id);                // DF_id
                eventLog.put("DF_description", DF_desc);    // DF_desc

                mDb.insert("EventLog", null, eventLog);
                log.info("ToEventLog(): Added new event: id=" + DF_id + " time=" + time + " cid=" + cid);

                // Short 100 ms Vibration
                // TODO not elegant solution, vibrator invocation should be moved somewhere else imho
                boolean vibrationEnabled = mPreferences.getBoolean(mContext.getString(R.string.pref_notification_vibrate_enable), true);
                int thresholdLevel = Integer.valueOf(mPreferences.getString(mContext.getString(R.string.pref_notification_vibrate_min_level), String.valueOf(Status.Type.MEDIUM.level)));
                boolean higherLevelThanThreshold = Status.Type.MEDIUM.level <= thresholdLevel;

                if (vibrationEnabled && higherLevelThanThreshold) {
                    Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(100);
                }

                // Short sound:
                // TODO see issue #15

            }
//            else {
            // TODO This may need to be removed as it may spam the logcat buffer...
            //log.verbose(mTAG + ":toEventLog(): Skipped inserting duplicate event");
//            }
        }
        // TODO This may need to be removed as it may spam the logcat buffer...
        //log.verbose(mTAG + ":insertEventLog(): Skipped inserting bad CID/LAC data");
    }


    /**
     * Description:     Inserts BTS Sector Type data into the SectorType table
     * <p/>
     * Issues:
     * [ ]
     * <p/>
     * Notes:
     */
    public void insertSectorType(String description) {
        ContentValues sectorType = new ContentValues();
        sectorType.put("description", description);
        mDb.insert("SectorType", null, sectorType);
    }

    /**
     * Description:     Inserts SMS Detection Strings data into the "DetectionStrings" table
     * <p/>
     * Issues:
     * [ ]
     * <p/>
     * Notes:
     */
    public void insertDetectionStrings(String det_str, String sms_type) {

        ContentValues detectionStrings = new ContentValues();
        detectionStrings.put("det_str", det_str);
        detectionStrings.put("sms_type", sms_type);

        String query = String.format(
                "SELECT * FROM DetectionStrings WHERE det_str = \"%s\" AND sms_type = \"%s\"",
                det_str, sms_type);

        // Check that the new string is not already in DB then insert
        Cursor cursor = mDb.rawQuery(query, null);
        if (cursor.getCount() <= 0) {
            mDb.insert("DetectionStrings", null, detectionStrings);
            cursor.close();
        } else {
            cursor.close();
        }
    }

    /**
     * Description:     Inserts detected silent SMS data into "SmsData" table
     * <p/>
     * Issues:
     * [ ]
     * <p/>
     * Notes:
     */
    public void insertSmsData(String time,
                              String number,
                              String smsc,
                              String message,
                              String type,
                              String CLASS, //<-- had to put in uppercase class is used be api
                              int lac,
                              int cid,
                              String rat,
                              double gps_lat,
                              double gps_lon,
                              int isRoaming) {

        ContentValues smsData = new ContentValues();

        smsData.put("time", time);
        smsData.put("number", number);
        smsData.put("smsc", smsc);
        smsData.put("message", message);
        smsData.put("type", type);
        smsData.put("class", CLASS);
        smsData.put("lac", lac);
        smsData.put("cid", cid);
        smsData.put("rat", rat);
        smsData.put("gps_lat", gps_lat);
        smsData.put("gps_lon", gps_lon);
        smsData.put("isRoaming", isRoaming);

        // Check that SMS timestamp is not already in DB, then INSERT
        if (!isTimeStampInDB(time)) {
            mDb.insert("SmsData", null, smsData);
        }
    }

    /**
     * Description:    This checks if a cell with a given CID already exists
     * in the (DBe_import) database.
     * <p/>
     * Dependencies:   CellTracker()
     */
    public boolean openCellExists(int cellID) {
        String qry = String.format("SELECT CID FROM DBe_import WHERE CID = %d", cellID);
        Cursor cursor = mDb.rawQuery(qry, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Description:     Check if CID and LAC is already in DBi_bts
     * <p/>
     * NOTES:           Warning, this is only for checking, if used to get info,
     * replace "CID,LAC" with "*"
     */
    public boolean cellInDbiBts(int lac, int cellID) {

        String query = String.format("SELECT CID,LAC FROM DBi_bts WHERE LAC = %d AND CID = %d",
                lac, cellID);

        Cursor cursor = mDb.rawQuery(query, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Description:     Check if CID (currently bts_id) is already in DBi_measure
     * <p/>
     * Issues:          TODO: replace "bts_id" with DBi_bts:CID
     * [ ] This is redundant because of cellInDbiBts
     * <p/>
     * Dependencies:    TODO: where is this used?
     */
    public boolean cellInDbiMeasure(int cellID) {
        String query = String.format(
                // "SELECT CID FROM DBi_bts, DBi_measure WHERE bts_id = %d", // todo E:V:A
                "SELECT bts_id FROM DBi_measure WHERE bts_id = %d",
                cellID);

        Cursor cursor = mDb.rawQuery(query, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}

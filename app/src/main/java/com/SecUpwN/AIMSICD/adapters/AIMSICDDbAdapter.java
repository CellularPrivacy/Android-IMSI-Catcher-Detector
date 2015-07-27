package com.SecUpwN.AIMSICD.adapters;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.constants.DBTableColumnIds;
import com.SecUpwN.AIMSICD.smsdetection.AdvanceUserItems;
import com.SecUpwN.AIMSICD.smsdetection.CapturedSmsData;
import com.SecUpwN.AIMSICD.utils.Cell;
import com.SecUpwN.AIMSICD.utils.CMDProcessor;
import com.SecUpwN.AIMSICD.utils.MiscUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.SparseArray;
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

/**
 * Brief:   Handles the AMISICD DataBase tables (creation, population, updates, etc)
 *
 * Description:
 *
 *      This class handle all the AMISICD DataBase maintenance operations, like
 *      creation, population, updates, backup, restore and various selections.
 *
 *
 *
 * Current Issues: TODO
 *
 *      [ ] We'd like to Export the entire DB (like a dump), so we need ...
 *      [x] Clarify the difference between cell.getCID() and CellID (see insertCell() below.)
 *      [ ] prepareOpenCellUploadData() this needs to be updated and re-coded
 *      [ ] all functions related to SignalStrengthTracker.java need to be updated
 *      [ ] cleanseCellTable()  I think this is complete it's used in CellTracker.java
 *                              not SignalStrengthTracker.java
 *      [ ] addSignalStrength( int cellID, int signal, String timestamp )
 *              The timestamp is stored as String.valueOf(System.currentTimeMillis());
 *              because the new db column for this is TEXT?
 *      [ ] getAverageSignalStrength() // rx_signal
 *
 *
 *  ChangeLog:
 *
 *      2015-01-22  E:V:A   Started DBe_import migration
 *      2015-01-23  E:V:A   ~~changed silent sms column names~~ NOT!
 *                          Added EventLog table
 *      2015-07-16  E:V:A   Post DB design migration cleanup, see special notes below and
 *                          ref issue #214.
 *
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
 *
 *  ===============================================================================================
 *    POST DB Overhaul Notes (by banjaxbanjo)
 *  ===============================================================================================
 *
 *      [x] Database is now kept open and is only closeed on app exit (no need to .open/close )
 *          ^^^ This is correct!  DO NOT CHANGE THIS BEHAVIOUR EVER!!
 *
 *      [x] Default Locations now preloaded in DB
 *      [x] BackupDB() is now working with all new tables
 *      [x] RestoreDB() is now working with all new tables
 *      [x] Download OCID is working with new DbeImport Table
 *      [x] EventLog has been updated
 *      [x] insertBTS/insertBtsMeasure replaces insertCell/insertLocation
 *      [x] insertDBeImport replaces insertOpenCell
 *      [x] insertEventLog replaces insertDetection
 *      [x] returnEventLogData() replaces getEventLogData()
 *      [x] returnSmsData( replaces getSilentSmsData()
 *      [x] returnDBiBts() replaces getCellData()
 *      [x] returnDBiMeausre() replaces getLocationData()
 *      [x] returnDBeImport() replaces getOpenCellIDData()
 *      [x] "updateOpenCellID" renamed to "populateDBe_import"
 *      [x] removed populateDefaultMCC() as now these are preloaded
 *      [x] restoreDB()/backupDB() now restores/backup with new tables
 *      [x] removed: public class DbHelper extends SQLiteOpenHelper as we are now going with pre populated DB
 *      [x] A lot of code refactored to suit new DB changes
 */

public class AIMSICDDbAdapter extends SQLiteOpenHelper{

    public static String FOLDER;
    public static final int DATABASE_VERSION = 1; // Is this "pragma user_version;" ?

    // TODO: This should be implemented as a SharedPreference...
    private final Boolean MONO_DB_DUMP = true; // Also back-up DB with one monolithic dump file?

    private final String TAG = "AIMSICD";
    private final String mTAG = "AIMSICDDbAdapter";
    private static String DB_NAME = "aimsicd.db";
    private static String DB_PATH = "/data/data/com.SecUpwN.AIMSICD/databases/";
    private String DB_LOCATION = DB_PATH + DB_NAME;

    private final String[] mTables;
    private SQLiteDatabase mDb;
    private final Context mContext;

    public AIMSICDDbAdapter(Context context) {
        super(context, DB_NAME, null, 1);
        mContext = context;
        FOLDER = mContext.getExternalFilesDir(null) + File.separator; //e.g. /storage/emulated/0/Android/data/com.SecUpwN.AIMSICD/
        //mDbHelper = new DbHelper(context);

        // Create a new blank DB then write pre-compiled DB in assets folder to blank DB.
        // This will throw error on first create because there is no DB to open and this is normal.
        createDataBase();

        //return writable database
        mDb = SQLiteDatabase.openDatabase(DB_LOCATION, null, SQLiteDatabase.OPEN_READWRITE);

        // This will return the database as open so we don't need to use .open . Then when app
        // is exiting we use new AIMSICDDbAdapter(getApplicationContext()).close(); to close it
        this.getWritableDatabase();
        mTables = new String[]{

                // I am trying to keep in same order and aimsicd.sql script
                // Only backing up useful tables, uncomment if you want to backup
                DBTableColumnIds.DEFAULT_LOCATION_TABLE_NAME,   // defaultlocation:     Default MCC for each country
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
     *                  our own pre-fabricated AIMSICD.db.
     *
     * NOTES:           This is a modified version to suit of needs of this guys great guide on
     *                  how to build a pre compiled db for android. Cheers Juan-Manuel Fluxà
     *                  See:
     *                  http://www.reigndesign.com/blog/using-your-own-sqlite-database-in-android-applications/
     *
     **/
    public boolean createDataBase(){
        if(!checkDataBase()){
            // By calling this method, an empty database will be created into the default system path
            // of your application so that we can overwrite that database with our own database.
            this.getReadableDatabase();
            try {
                copyDataBase();
                Log.i(TAG, mTAG + ": Database created");

                return true;
            } catch (IOException e) {
                throw new Error("Error copying database\n" + e.toString());
            }

        }
        return false;
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            //Log.i(TAG,"Checking for db first install this will throw an error on install and is noraml");
            checkDB = SQLiteDatabase.openDatabase(DB_LOCATION, null, SQLiteDatabase.OPEN_READONLY);
        }catch(SQLiteException e){
            Log.e(TAG, mTAG + ": database not yet created: " + e.toString());
        }

        if(checkDB != null){
            checkDB.close();
        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{

        // Open your local db as the input stream
        InputStream myInput = mContext.getAssets().open(DB_NAME);

        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(DB_LOCATION);

        // Transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
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

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }


    // ====================================================================
    //      Populate the DB tables  (INSERT)
    // ====================================================================

    /**
     * Description:     Delete cell info - This is used in the AIMSICD frame work Tests
     *                  see: ../src/androidTest/java/com.SecUpwN.test/.
     *
     * @param cellId    This method deletes a cell with CID from CELL_TABLE
     *
     * @return result of deleting that CID
     *
     */
    public int deleteCell(int cellId) {
        Log.i(TAG, mTAG + ": Deleted CID: " + cellId);
        // TODO Do we also need to delete this cell from DBi_measure?
        // Rewrite this query!
        return mDb.delete("DBi_bts","CID=" + cellId, null);
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
     * Returns Cell Information (DBi_bts) database contents
     * this returns BTS's that we logged and is called from
     * MapViewerOsmDroid.java to display cells on map
     */
    public Cursor getCellData() {
        return returnDBiBts();
    }

    /**
     * Returns Cell Information for contribution to the OpenCellID project
     *
     * Function:    Return a list of all rows from the DBi_measure table
     *              where isSubmitted is not 1.
     *
     */
    public Cursor getOCIDSubmitData() {
        // This is used (prepareOpenCellUploadData) when uploading data to OCID

        // TODO: Use something like this instead... VVV Need testing may need ...,RAT FROM DBi_measure ..
        // @EVA created a new function getRatFromDBimeasure(CellId) to get RAT with CID
        //      this can be used when creating the upload data to add the RAT to the CSV file

        // IMPORTANT: Note the order of the items (to match CSV)!
        //TODO @EVA this is working but its returning 2 of each rows any ideas??
        //
        String query = "SELECT DISTINCT MCC,MNC,LAC,CID,gpsd_lon,gpsd_lat,rx_signal,time,gpsd_accu FROM DBi_measure, DBi_bts WHERE isSubmitted <> 1 ORDER BY time;";
        //String query = "SELECT MCC,MNC,LAC,CID,gpsd_lon,gpsd_lat,rx_signal,time,gpsd_accu FROM DBi_bts WHERE isSubmitted <> 1";//<<<OLD for refernce
        return mDb.rawQuery(query,null);
    }


    // ====================================================================
    //      Various DB operations
    // ====================================================================

    /**
     *  Description:    This take a "Cell" bundle (from API) as input and uses its CID to check
     *                  in the DBi_measure (?) if there is already an associated LAC. It then
     *                  compares the API LAC to that of the DBi_Measure LAC.
     *
     *  Issues:     [ ] We should make all detections outside of AIMSICDDbAdapter.java in a
     *                  separate module as described in the diagram in GH issue #215.
     *                  https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/215
     *                  where it is referred to as "Detection Module" (DET)...
     *
     *              [ ] Seem we're querying too much, when we only need items: 1,3,4,8,11
     *                  (Try to avoid over query to improve performance.)
     *
     *              [ ]  V V V V
     *
     *              This is using the LAC found by API and comparing to LAC found from a previous
     *              measurement in the "DBi_measure". This is NOT depending on "DBe_import".
     *              This works for now...but we probably should consider populating "DBi_measure"
     *              as soon as the API gets a new LAC. Then the detection can be done by SQL,
     *              and by just comparing last 2 LAC entries for same CID.
     *
     *
     */
    public boolean checkLAC(Cell cell) {
        String query = String.format("SELECT * FROM DBi_bts WHERE CID = %d",cell.getCID());  //CID

        Cursor bts_cursor = mDb.rawQuery(query,null);

        while (bts_cursor.moveToNext()) {
            // 1=LAC, 8=Accuracy, 11=Time
            if (cell.getLAC() != bts_cursor.getInt(bts_cursor.getColumnIndex("LAC"))) {
                Log.i(TAG, mTAG + ": ALERT: Changing LAC on CID: " + cell.getCID()
                        + " LAC(API): " + cell.getLAC()
                        + " LAC(DBi): " + bts_cursor.getInt(bts_cursor.getColumnIndex("LAC")));

                bts_cursor.close();
                return false;
            } else {
                Log.v(TAG, mTAG + ": LAC checked - no change on CID:" + cell.getCID()
                        + " LAC(API): " + cell.getLAC()
                        + " LAC(DBi): " + bts_cursor.getInt(bts_cursor.getColumnIndex("LAC")));
            }
        }
        bts_cursor.close();
        return true;
    }


    /**
     * Updates Cell (cellinfo) records to indicate OpenCellID contribution has been made
     * TODO: This should be done on TABLE_DBI_MEASURE::DBi_measure:isSubmitted << Fixed
     *
     */
    public void ocidProcessed() {
        ContentValues ocidValues = new ContentValues();
        ocidValues.put("isSubmitted", 1); // isSubmitted
        // TODO:    rewrite mDb.query to use mDb.rawQuery ??
        mDb.update("DBi_measure", ocidValues, "isSubmitted<>?", new String[]{"1"}); // isSubmitted
    }


    /*
        This returns all bts in the DBe_import by current sim card network
        rather than returning other bts from different networks and slowing
        down map view

     */
    public Cursor returnOcidBtsByNetwork(int mcc,int mnc){
        String query = String.format(
                "SELECT * FROM DBe_import WHERE MCC = %d AND MNC = %d",
                mcc,
                mnc);
        return mDb.rawQuery(query, null);
    }

    public double[] getDefaultLocation(int mcc) {
        String query = String.format(
                "SELECT lat,lon FROM defaultlocation WHERE MCC = %d",mcc);

        double[] loc = new double[2];
        Cursor cursor = mDb.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            loc[0] = Double.parseDouble(cursor.getString(cursor.getColumnIndex("lat")));
            loc[1] = Double.parseDouble(cursor.getString(cursor.getColumnIndex("lon")));
        } else {
            loc[0] = 0.0;
            loc[1] = 0.0;
        }
        cursor.close();
        return loc;
    }

    /**
     *  Description:    Remove all but the last row, unless its CID is invalid...
     *
     *  Note:           Q: What is this used for?
     *                  A: It used in the SignalStrengthTracker
     *
     *                  this isn't in SignalStrengthTracker it's
     *                  in CellTracker.java
     *                  dbHelper.cleanseCellTable();
     */
    public void cleanseCellTable() {
        // Creating queries with string format because easier to understand
        // This removes all but the last row in the "DBi_bts" table

        mDb.execSQL("DELETE FROM DBi_bts WHERE _id NOT IN (SELECT MAX(_id) FROM DBi_bts GROUP BY CID)");

        String query2 = String.format("DELETE FROM DBi_bts WHERE CID = %d OR CID = -1",Integer.MAX_VALUE);
        mDb.execSQL(query2);
    }

    /**
     * Description:     Prepares the CSV file used to upload new data to the OCID server.
     *
     * Note:            Q: Where is this?
     *                  A: It is wherever your device has mounted its SDCard.
     *                     For example, in:  /data/media/0/AIMSICD/OpenCellID
     *   TODO this needs to be fixed with new database upgrade 12/07/2015
     *          @banjaxbanjo: What do yuo mean? --E:V:A
     *          @EVA the columns need to be sorted because we dont hase some of these in DBi_bts
     *          //("mcc,mnc,lac,cellid,lon,lat,signal,measured_at,rating,speed,direction,act")
     *          //and also this function getOPCIDSubmitData() is not working because columns also not in DBi_bts
     */
    public boolean prepareOpenCellUploadData() {
        boolean result;

        File dir = new File(FOLDER + "OpenCellID/");
        if (!dir.exists()) {
            result = dir.mkdirs();
            if (!result) {
                return false;
            }
        }
        File file = new File(dir, "aimsicd-ocid-data.csv");

        try {
            Cursor c = getOCIDSubmitData(); // get data not submitted yet

            if(c.getCount() > 0) { // check if we have something to upload
                if (!file.exists()) {
                    result = file.createNewFile();
                    if (!result) {
                        c.close();
                        return false;
                    }

                    //OCID upload format
                    //measured_at =time gps accu = rating
                    //mcc,mnc,lac,cellid,lon,lat,signal,measured_at,rating,speed,direction,act,ta,psc,tac,pci,sid,nid,bid
                    CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                     csvWrite.writeNext("mcc,mnc,lac,cellid,lon,lat,signal,measured_at,rating");
                    int size = c.getCount();
                    Log.d(TAG, mTAG+" OCID UPLOAD: row count = "+size);
                    int startcount = 0;
                    AIMSICD.mProgressBar.setProgress(startcount);
                    AIMSICD.mProgressBar.setMax(size);
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
                                String.valueOf(c.getInt(c.getColumnIndex("gpsd_accu"))));
                                AIMSICD.mProgressBar.setProgress(++startcount);
                    }

                    csvWrite.close();
                    c.close();
                }
                return true;
            }
            c.close();
            return false;
        } catch (Exception e) {
            Log.e(TAG, mTAG + ": Error creating OpenCellID Upload Data: " + e.toString());
            return false;
        } finally {
            AIMSICD.mProgressBar.setProgress(0);
        }
    }


    /**
     *  Description:    Parses the downloaded CSV from OpenCellID and uses it to populate
     *                  "DBe_import" table.
     *
     *
     *  Dependency:     RequestTask.java :: onPostExecute()
     *                  insertDBeImport()
     *  Issues:
     *
     *          [ ]     Progress bar is not shown or is the operation too quick to be seen?
     *          [ ]     Why are we only populating 8 items out of 19?
     *                  From downloaded OCID CSV file:  (19 items)
     *
     *  NOTES:
     *
     *          a)  We do not include "rej_cause" in backups. set to 0 as default
     *          b)  Unfortunately there are 2 important missing items in the OCID CSV file:
     *                  - "time_first"
     *                  - "time_last"
     *          c)  In addition the OCID data often contain unexplained negative values for one or both of:
     *                  - "samples"
     *                  - "range"
     *
     *          d) The difference between "Cellid" and "cid", is that "cellid" is the "Long CID",
     *             consisting of RNC and a multiplier:
     *                      Long CID = 65536 * RNC + CID
     *             See FAQ.
     *
     * ========================================================================
     * For details on available OpenCellID API DB values, see:
     * http://wiki.opencellid.org/wiki/API
     * http://wiki.opencellid.org/wiki/FAQ#Long_CellID_vs._short_Cell_ID
     * ========================================================================
     *   # head -2 opencellid.csv
     *   lat,lon,mcc,mnc,lac,cellid,averageSignalStrength,range,samples,changeable,radio,rnc,cid,psc,tac,pci,sid,nid,bid
     *
     *   0 lat                      TEXT
     *   1 lon                      TEXT
     *   2 mcc                      INTEGER
     *   3 mnc                      INTEGER
     *   4 lac                      INTEGER
     *   5 cellid                   INTEGER     (Long CID) = 65536 * RNC + CID
     *   6 averageSignalStrength    INTEGER     (rx_power)
     *   7 range                    INTEGER     (accu)
     *   8 samples                  INTEGER
     *   9 changeable               INTEGER     (isGPSexact)
     *   10 radio                   TEXT        (RAT)
     *   11 rnc                     INTEGER
     *   12 cid                     INTEGER     CID (Short)= "Long CID" mod 65536
     *   13 psc                     INTEGER
     *   14 tac                     -
     *   15 pci                     -
     *   16 sid                     -
     *   17 nid                     -
     *   18 bid                     -
     *
     *   54.63376,25.160243,246,3,20,1294,0,-1,1,1,GSM,,,,,,,,
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

                //AIMSICD.mProgressBar.setProgress(0);
                //AIMSICD.mProgressBar.setMax(csvSize);
                while ((next = csvReader.readNext()) != null) {
                    csvCellID.add(next);
                }

                if (!csvCellID.isEmpty()) {
                    int lines = csvCellID.size();
                    Log.i(TAG, mTAG + ":updateOpenCellID: OCID CSV size (lines): " + lines );

                    // This counts how many CIDs we have in DBe_import
                    //TODO I removed ; from "SELECT CID, COUNT(CID) FROM DBe_import GROUP BY CID;" is this prob?
                    Cursor lCursor = mDb.rawQuery("SELECT CID, COUNT(CID) FROM DBe_import GROUP BY CID", null);
                    SparseArray<Boolean> lPresentCellID = new SparseArray<>();
                    if(lCursor.getCount() > 0) {
                        while(lCursor.moveToNext()) {
                            lPresentCellID.put(lCursor.getInt(0), true );
                        }
                    }
                    lCursor.close();

                    AIMSICD.mProgressBar.setProgress(0);
                    AIMSICD.mProgressBar.setMax(lines);
                    for (int i = 1; i < lines; i++) {
                        AIMSICD.mProgressBar.setProgress(i);

                        // Inserted into the table only unique values CID
                        // without opening additional redundant cursor before each insert.
                        if(lPresentCellID.get(Integer.parseInt(csvCellID.get(i)[5]), false)) {
                            continue;
                        }
                        // Insert details into OpenCellID Database using:  insertDBeImport()
                        // Beware of negative values of "range" and "samples"!!
                        String  lat = csvCellID.get(i)[0],          //TEXT
                                lon = csvCellID.get(i)[1],          //TEXT
                                mcc = csvCellID.get(i)[2],          //int
                                mnc = csvCellID.get(i)[3],          //int
                                lac = csvCellID.get(i)[4],          //int
                                cellid = csvCellID.get(i)[5],       //int
                                range = csvCellID.get(i)[6],        //int
                                avg_sig = csvCellID.get(i)[7],      //int
                                samples = csvCellID.get(i)[8],      //int
                                change = csvCellID.get(i)[9],       //int
                                radio = csvCellID.get(i)[10],       //TEXT
                                rnc = csvCellID.get(i)[11],         //int
                                cid = csvCellID.get(i)[12],         //int
                                psc = csvCellID.get(i)[13];         //int

                        int iPsc = 0;

                        if(psc != null && !psc.equals("")){iPsc = Integer.parseInt(psc);}

                        //Reverse order 1 = 0 & 0 = 1
                        int ichange = Integer.parseInt(change);

                        if (ichange == 0)
                        {
                            ichange = 1;
                        }else if(ichange == 1)
                        {
                            ichange = 0;
                        }

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
                                "no_time",                  // time_first in OCID
                                "no_time",                  // time_last in OCID
                                0                           //set default 0
                        );
                        //Log.d(TAG,"Dbe_import tables inserted=" + i);
                    }
                }
            }else{
                Log.e(TAG, mTAG + ": File does NOT exist! ");
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, mTAG + ": Error parsing OpenCellID data: " + e.getMessage());
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
     *  Used:           ??
     *
     *  Issues:
     *              [ ] CELL_TABLE items are missing or corrupt
     *              [ ]
     *
     *  Exported CSV format: TODO:
     *
     *  Notes:      1) Restoring the DB can be done from a monolithic SQLite3 DB by (check!):
     *                  # sqlite3 aimsicd.db <aimsicd.dump
     *
     *
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

                                case "defaultlocation":
                                    try{
                                        insertDefaultLocation(
                                                records.get(i)[1].toString(),           //country
                                                Integer.parseInt(records.get(i)[2]),    //MCC
                                                records.get(i)[3].toString(),           //lat
                                                records.get(i)[4].toString()            //lon
                                        );
                                    }catch(Exception ee){
                                        Log.e(TAG, mTAG + ": err DEFAULT_LOCATION");
                                    }
                                    break;

                                case "API_keys":
                                    insertApiKeys(
                                            records.get(i)[1].toString(),           //name
                                            records.get(i)[2].toString(),           //type
                                            records.get(i)[3].toString(),           //key
                                            records.get(i)[4].toString(),           //time_add
                                            records.get(i)[5].toString()            //time_exp
                                    );
                                    break;

                                case "CounterMeasures":
                                    insertCounterMeasures(
                                            records.get(i)[1].toString(),           //name
                                            records.get(i)[2].toString(),           //description
                                            Integer.parseInt(records.get(i)[3]),    //thresh
                                            Double.parseDouble(records.get(i)[4])   //thfine
                                    );
                                    break;

                                case "DBe_capabilities":

                                    insertDBeCapabilities(
                                            records.get(i)[1].toString(),           //MCC
                                            records.get(i)[2].toString(),           //MNC
                                            records.get(i)[3].toString(),           //LAC
                                            records.get(i)[4].toString(),           //op_name
                                            records.get(i)[5].toString(),           //band_plan
                                            records.get(i)[6].toString()            //__EXPAND___
                                    );

                                    break;

                                case "DBe_import":
                                    try{
                                        insertDBeImport(
                                                records.get(i)[1].toString(),           // DBsource
                                                records.get(i)[2].toString(),           // RAT
                                                Integer.parseInt(records.get(i)[3]),    // MCC
                                                Integer.parseInt(records.get(i)[4]),    // MNC
                                                Integer.parseInt(records.get(i)[5]),    // LAC
                                                Integer.parseInt(records.get(i)[6]),    // CID
                                                Integer.parseInt(records.get(i)[7]),    // PSC..
                                                records.get(i)[8].toString(),           // gps_lat
                                                records.get(i)[9].toString(),           // gps_lon
                                                Integer.parseInt(records.get(i)[10]),   // isGPSexact
                                                Integer.parseInt(records.get(i)[11])  , // avg_range
                                                Integer.parseInt(records.get(i)[12]),   // avg_signal
                                                Integer.parseInt(records.get(i)[13]),   // samples
                                                records.get(i)[14].toString(),          // time_first
                                                records.get(i)[15].toString(),          // time_last
                                                0                                       // rej_cause

                                        );
                                    }catch(Exception ee){
                                        Log.e(TAG, mTAG + ": err DBE_IMPORT_");
                                    }
                                    break;

                                case "DBi_bts":
                                    try{
                                        insertBTS(
                                                Integer.parseInt(records.get(i)[1]),    // MCC
                                                Integer.parseInt(records.get(i)[2]),    // MNC
                                                Integer.parseInt(records.get(i)[3]),    // LAC
                                                Integer.parseInt(records.get(i)[4]),    // CID
                                                Integer.parseInt(records.get(i)[5]),    // PSC
                                                Integer.parseInt(records.get(i)[6]),    // T3212
                                                Integer.parseInt(records.get(i)[7]),    // A5x
                                                Integer.parseInt(records.get(i)[8]),    // ST_id
                                                records.get(i)[9].toString(),           // First Time
                                                records.get(i)[10].toString(),          // last Time
                                                Double.parseDouble(records.get(i)[11]), // lat
                                                Double.parseDouble(records.get(i)[12])  // lon
                                        );
                                    }catch(Exception ee){
                                        Log.e(TAG, mTAG + ": err DBI_BTS");
                                    }
                                    break;

                                case "DBi_measure":
                                    try{
                                        insertDbiMeasure(
                                                Integer.parseInt(records.get(i)[1]),      // bts_id
                                                records.get(i)[2].toString(),             // nc_list
                                                records.get(i)[3].toString(),             // time
                                                records.get(i)[4].toString(),             // gpsd_lat
                                                records.get(i)[5].toString(),             // gpsd_lon
                                                Integer.parseInt(records.get(i)[6]),      // gpsd_accu
                                                records.get(i)[7].toString(),             // gpse_lat
                                                records.get(i)[8].toString(),             // gpse_lon
                                                records.get(i)[9].toString(),            // bb_power
                                                records.get(i)[10].toString(),            // bb_rf_temp
                                                records.get(i)[11].toString(),            // tx_power
                                                records.get(i)[12].toString(),            // rx_signal
                                                records.get(i)[13].toString(),            // rx_stype
                                                records.get(i)[14].toString(),             // rat
                                                records.get(i)[15].toString(),            // BCCH
                                                records.get(i)[16].toString(),            // TMSI
                                                Integer.parseInt(records.get(i)[17]),     // TA
                                                Integer.parseInt(records.get(i)[18]),     // PD
                                                Integer.parseInt(records.get(i)[19]),     // BER
                                                records.get(i)[20].toString(),            // AvgEcNo
                                                Integer.parseInt(records.get(i)[21]),     // isSubmitted
                                                Integer.parseInt(records.get(i)[22])     // isNeighbour
                                        );
                                    }catch(Exception ee){
                                        Log.e(TAG, mTAG + ": err DBI_MEASURE");
                                    }
                                    break;

                                case "DetectionFlags":
                                    insertDetectionFlags(
                                            Integer.parseInt(records.get(i)[1]),    //code
                                            records.get(i)[2].toString(),           //name
                                            records.get(i)[3].toString(),           //description
                                            Integer.parseInt(records.get(i)[4]),    //p1
                                            Integer.parseInt(records.get(i)[5]),    //p2
                                            Integer.parseInt(records.get(i)[6]),    //p3
                                            Double.parseDouble(records.get(i)[7]),  //p1_fine
                                            Double.parseDouble(records.get(i)[8]),  //p2_fine
                                            Double.parseDouble(records.get(i)[9]),  //p3_fine
                                            records.get(i)[10].toString(),          //app_text
                                            records.get(i)[11].toString(),          //func_use
                                            Integer.parseInt(records.get(i)[12]),   //istatus
                                            Integer.parseInt(records.get(i)[13])    //CM_id

                                    );
                                    break;

                                case "EventLog":
                                    insertEventLog(
                                            records.get(i)[1].toString(),           //time
                                            Integer.parseInt(records.get(i)[2]),    //LAC
                                            Integer.parseInt(records.get(i)[3]),    //CID
                                            Integer.parseInt(records.get(i)[4]),    //PSC
                                            records.get(i)[5].toString(),           //gpsd_lat
                                            records.get(i)[6].toString(),           //gpsd_lon
                                            Integer.parseInt(records.get(i)[7]),    //gpsd_accu
                                            Integer.parseInt(records.get(i)[8]),    //DF_id
                                            records.get(i)[9].toString()            //DF_description
                                    );
                                    break;

                                case "SectorType":
                                    insertSectorType(
                                            records.get(i)[1].toString()
                                    );
                                    break;

                                case "DetectionStrings":
                                    insertDetectionStrings(
                                            records.get(i)[1].toString(),
                                            records.get(i)[2].toString()
                                    );
                                    break;

                                case "SmsData":
                                    insertSmsData(
                                            records.get(i)[1].toString(),           //time
                                            records.get(i)[2].toString(),           //number
                                            records.get(i)[3].toString(),           //smsc
                                            records.get(i)[4].toString(),           //message
                                            records.get(i)[5].toString(),           //type
                                            records.get(i)[6].toString(),           //class
                                            Integer.parseInt(records.get(i)[7]),    //lac
                                            Integer.parseInt(records.get(i)[8]),    //cid
                                            records.get(i)[9].toString(),           //rat
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
            Log.i(TAG, mTAG + ": restoreDB() Finished ");
            return true;
        } catch (Exception e) {
            Log.e(TAG, mTAG + ": restoreDB() Error\n" + e.toString());
            return false;
        } finally {
            AIMSICD.mProgressBar.setProgress(0);
        }

    }

    /**
     *  Description:    Dumps the entire aimsicd.db to a dump file called "aimsicd_dump.db".
     *
     *  Requires:       root + SQLite3 binary
     *
     *  Dev Status:     INCOMPLETE !!  Either fix or do not try to use.. TODO: is this ok now?
     *
     *  Template:       DebugLogs.java
     *
     *
     *  Notes:  1) We probably also need to test if we have the sqlite3 binary. (See Busybox checking code.)
     *          2) Apparently pipes doesn't work from Java... No idea why, as they appear to work
     *              in the AtCommandFragment.java... for checking for /dev/ files.
     *          3) We can use either ".dump" or ".backup", but "dump" makes an SQL file,
     *             whereas "backup" make a binary SQLite DB.
     *
     *            a) # sqlite3 aimsicd.db '.dump' | gzip -c >aimsicd.dump.gz
     *            b) # sqlite3 aimsicd.db '.dump' >aimsicd.dump
     *            c) # sqlite3 aimsicd.db '.backup aimsicd.back'
     *
     *    execString = "/system/xbin/sqlite3 " + dir + "aimsicd.db '.dump' | gzip -c >" + file;
     *    execString = "/system/xbin/sqlite3 " + aimdir + "aimsicd.db '.dump' >" + file;
     *    execString = "/system/xbin/sqlite3 " + aimdir + "aimsicd.db '.backup " +file + "'";
     *
     *          4) To re-import use:
     *             # zcat aimsicd.dump.gz | sqlite3 aimsicd.db
     *
     */
    private void dumpDB()  {
        File dumpdir = new File(FOLDER);
        //if (!dir.exists()) { dir.mkdirs(); }
        File file = new File(dumpdir, "aimsicd_dump.db");
        //Bad coding? (What is AOS standard?)
        String aimdir = "/data/data/com.SecUpwN.AIMSICD/databases/";
        //Context.getFilesDir().getPath("com.SecUpwN.AIMSICD/databases"); ????
        String execString = "/system/xbin/sqlite3 " + aimdir + "aimsicd.db '.backup " + file + "'";

        try {
            Log.i(TAG, mTAG + ":dumpDB() Attempting to dump DB to: " + file + "\nUsing: \"" + execString + "\"\n");
            // We need SU here and cd...
            CMDProcessor.runSuCommand(execString);
            //CMDProcessor.runSuCommand(execString).getStdout(); // Need import!
            //Process process = Runtime.getRuntime().exec(execString);

        } catch (Exception e) {
            Log.e(TAG, mTAG + ":dumpDB() Failed to export DB dump file: " + e.toString());
        }
        Log.i(TAG, mTAG + ":dumpDB() Database dumped to: " + file);

    }


    /**
     *  Description:    Backup the database tables to CSV files (or monolithic dump file)
     *
     *  Depends:        On the Boolean MONO_DB_DUMP to indicate if we want to try to
     *                  dump a monolithic DB using the rooted shell + sqlite3 binary
     *                  method above.
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
            Log.e(TAG, mTAG + ":backupDB() Error: " + ioe);
            return false;
        }
    }

    /**
     *  Description:    Exports the database tables to CSV files
     *
     *  Issues:     We should consider having a better file selector here, so that
     *              the user can select his own location for storing the backup files.
     *
     * @param tableName String representing table name to export
     */
    private void backup(String tableName) {
        Log.i(TAG, mTAG + ": Database Backup: " + DB_NAME);

        File dir = new File(FOLDER);
        if (!dir.exists()) { dir.mkdirs(); }  // We should probably add some more error handling here.
        File file = new File(dir, "aimsicd-" + tableName + ".csv");

        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            Log.d(TAG, mTAG + ": table name " + tableName);

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
            Log.e(TAG, mTAG + ": Error exporting table: " + tableName + " " + e.toString());
        } finally {
            AIMSICD.mProgressBar.setProgress(0);
        }

        Log.i(TAG, mTAG + ": Database Export complete.");
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

        Log.d(TAG, mTAG + ":checkDBe() Attempting to delete bad import data from DBe_import table...");

        // =========== samples ===========
        sqlq = "DELETE FROM DBe_import WHERE samples < 1";
        mDb.execSQL(sqlq);

        // =========== range (DBe_import::avg_range) ===========
        // TODO: OCID data marks many good BTS with a negative range so we can't use this yet.
        //sqlq = "DELETE FROM " + OPENCELLID_TABLE + " WHERE Range < 1";
        //mDb.rawQuery(sqlq, null);

        // =========== LAC ===========
        sqlq = "DELETE FROM DBe_import WHERE LAC < 1";
        mDb.execSQL(sqlq);

        // We should delete cells with CDMA (4) LAC not in [1,65534] but we can simplify this to:
        // Delete ANY cells with a LAC not in [1,65534]
        sqlq = "DELETE FROM DBe_import WHERE LAC > 65534";
        mDb.execSQL(sqlq);
        // Delete cells with GSM/UMTS/LTE (1/2/3/13 ??) (or all others?) LAC not in [1,65533]
        //sqlq = "DELETE FROM " + OPENCELLID_TABLE + " WHERE Lac > 65533 AND Type!='CDMA'";
        //mDb.rawQuery(sqlq, null);

        // =========== CID ===========
        sqlq = "DELETE FROM DBe_import WHERE CID < 1";
        mDb.execSQL(sqlq);

        // We should delete cells with UMTS/LTE (3,13) CID not in [1,268435455] (0xFFF FFFF) but
        // we can simplify this to:
        // Delete ANY cells with a CID not in [1,268435455]
        sqlq = "DELETE FROM DBe_import WHERE CID > 268435455";
        mDb.execSQL(sqlq);
        // Delete cells with GSM/CDMA (1-3,4) CID not in [1,65534]
        //sqlq = "DELETE FROM " + OPENCELLID_TABLE + " WHERE CellID > 65534 AND (Net!=3 OR Net!=13)";
        //mDb.rawQuery(sqlq, null);

        Log.i(TAG, mTAG + ":checkDBe() Deleted BTS entries from DBe_import table with bad LAC/CID...");

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
        //TODO @EVA this is not working because time in DBi_measure = TEXT not Intgear
        String query = String.format("DELETE FROM DBi_measure WHERE time < %d",maxTime );
        Log.d(TAG, mTAG + ": Cleaning " + DBTableColumnIds.DBI_MEASURE_TABLE_NAME + " WHERE time < " + maxTime);
        mDb.execSQL(query);
    }

    /*
        TODO: timestamp in DBi_measure is a String, but the one from SignalStrengthTracker is a long
     */
    public void addSignalStrength( int cellID, int signal, String timestamp ) {
        ContentValues row = new ContentValues();
        row.put("bts_id", cellID);
        row.put("rx_signal", signal);
        row.put("time", timestamp);
        //TODO are we inserting or updating? if we insert will this not wipe out all data from last known row
        mDb.insert("DBi_measure", null, row);
    }

    public int countSignalMeasurements(int cellID) {
        String query = String.format(
                "SELECT COUNT(bts_id) FROM DBi_measure WHERE bts_id= %d", cellID);


        Cursor c = mDb.rawQuery(query,null);
        c.moveToFirst();
        int lAnswer = c.getInt(0);
        c.close();
        return lAnswer;
    }

    public int getAverageSignalStrength(int cellID) {
        String query = String.format("SELECT avg(rx_signal) FROM DBi_measure WHERE bts_id= %d", cellID);
        Cursor c = mDb.rawQuery(query,null);
        c.moveToFirst();
        int lAnswer = c.getInt(0);
        c.close();
        return lAnswer;
    }

    public Cursor getSignalStrengthMeasurementData() {
        return mDb.rawQuery("SELECT bts_id,rx_signal,time FROM DBi_measure ORDER BY time DESC",null);
    }

    //TODO: Do we need to remove this? It's used in MapViewer..
    public Cursor getOpenCellIDDataByRegion(Double lat1, Double lng1, Double lat2, Double lng2) {
        return mDb.query( DBTableColumnIds.DBE_IMPORT_TABLE_NAME,
                new String[]{"CID", "LAC", "MCC", "MNC", "gps_lat", "gps_lon", "avg_signal", "samples"},
                // avg_range, rej_cause, Type
                "? <= gps_lon AND gps_lon <= ? AND ? <= gps_lat AND gps_lat <= ?",
                new String[]{lng1.toString(), lng2.toString(), lat1.toString(), lat2.toString()},
                null, null, null
        );
    }
    //=============================================================================================
    // ********************** ALL NEW FUNCTIONS ADDED AFTER THIS LINE *****************************
    //=============================================================================================

    // ====================================================================
    //      Get all detection strings
    // ====================================================================
    public ArrayList<AdvanceUserItems> getDetectionStrings(){

        Cursor cursor = mDb.rawQuery("SELECT * FROM DetectionStrings",null);

        ArrayList<AdvanceUserItems> detection_strs = new ArrayList<>();

        if(cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                AdvanceUserItems setitems = new AdvanceUserItems();
                setitems.setDetection_string(cursor.getString(cursor.getColumnIndex("det_str")));
                setitems.setDetection_type(cursor.getString(cursor.getColumnIndex("sms_type")));
                detection_strs.add(setitems);

            }
        } else {
            AdvanceUserItems setitems = new AdvanceUserItems();
            setitems.setDetection_string("No data");
            setitems.setDetection_type("No data");
            detection_strs.add(setitems);
        }
        cursor.close();
        return  detection_strs;
    }

    public boolean deleteDetectedSms(long deleteme) {
        String TAG = "AIMSICD";
        String mTAG = "AIMSICDDbAdapter";

        try {
            // TODO Rewrite in cleartext!
            mDb.delete("SmsData","_id=" + deleteme,null);
            return true;
        } catch (Exception ee){
            Log.i(TAG, mTAG + ": Deleting SMS data failed:" + ee);
        }
        return false;
    }

    public boolean deleteDetectionString(String deleteme) {

        try {
            // TODO Rewrite in cleartext!
            mDb.delete("DetectionStrings","det_str='" + deleteme + "'",null);
            return true;
        }catch (Exception ee){
            Log.i(TAG, mTAG + ": Deleting detection string failed: " + ee);
        }
        return false;

    }

    //====================================================================
    //      Insert new detection strings into database
    //====================================================================
    /**
     * Description:     When inserting strings it has to be in the format:
     *                  "i am a type 0 string". These strings can be found in main logcat.
     *
     * NOTES:
     *
     *      TYPE0 SILENTVOICE FLASH <--- These have to be in CAPS
     *      ContentValues newconvalues = new ContentValues();
     *      newconvalues.put(DETECTION_STRINGS_LOGCAT_STRING, "your string goes here");
     *      newconvalues.put(DETECTION_STRINGS_SMS_TYPE, "TYPE0");
     *      database.insert(DETECTION_STRINGS_TABLE_NAME,,null,newconvalues);
     */
    public boolean insertNewDetectionString(ContentValues newstring) {
        // First check that string not in DB

        String check4String = String.format(
                "SELECT * FROM DetectionStrings WHERE det_str = \"%s\"",
                newstring.get("det_str").toString() );

        Cursor cursor = mDb.rawQuery(check4String, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        if (exists) {
            Log.i(TAG, mTAG +  ": Detection String already in Database");
        } else {

            try {

                mDb.insert("DetectionStrings", null, newstring);
                Log.i(TAG, mTAG +  ": New detection string added.");
                return true;
            } catch (Exception ee) {
                Log.i(TAG, mTAG +  ": Adding detection string Failed! " + ee.toString());
            }
        }
        return false;
    }

    public CapturedSmsData storeCapturedSms(CapturedSmsData smsdata){

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

        // TODO Rewrite in cleartext!
        long insertid = mDb.insert("SmsData",null,values);
        smsdata.setId(insertid);
        return  smsdata;
    }

    // Boolean Check if a give timestamp already exists in SmsData table
    public boolean isTimeStampInDB(String timestamp){
        String check4timestamp = String.format(
                "SELECT time FROM SmsData WHERE time = \"%s\"", timestamp);
        Cursor cursor = mDb.rawQuery(check4timestamp,null);
        boolean exists = cursor.getCount() > 0;
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
    public Cursor returnDefaultLocation(){
        return mDb.rawQuery("SELECT * FROM defaultlocation",null);
    }

    /*
      Returned Columns:
    	"_id"      	INTEGER PRIMARY KEY,
    	"name"    	TEXT,
    	"type"    	TEXT,
    	"key"     	TEXT,
    	"time_add"	INTEGER,
    	"time_exp"	INTEGER
    */
    public Cursor returnApiKeys(){
        return mDb.rawQuery("SELECT * FROM API_keys",null);
    }

    /*
      Returned Columns:
    	"_id"         	INTEGER PRIMARY KEY,
    	"name"       	TEXT,
    	"description"	TEXT,
    	"thresh"     	INTEGER,
    	"thfine"     	REAL
    */
    public Cursor returnCounterMeasures(){
        return mDb.rawQuery("SELECT * FROM CounterMeasures",null);
    }

    /*
      Returned Columns:
	    "_id"        	INTEGER PRIMARY KEY,
	    "MCC"       	TEXT,
	    "MNC"       	TEXT,
	    "LAC"       	TEXT,
	    "op_name"   	TEXT,
        "band_plan" 	TEXT,
        "__EXPAND__"	TEXT
    */
    public Cursor returnDBeCapabilities(){
        return mDb.rawQuery("SELECT * FROM DBe_capabilities",null);
    }

    /**
        Returns OpenCellID (DBe_import) database contents
        Used in:
               DbViewerFragment.java
               MapViewerOsmDroid.java

        Returned Columns:
        "_id"        	INTEGER PRIMARY KEY AUTOINCREMENT,
        "DBsource"  	TEXT NOT NULL,
        "RAT"       	TEXT,
        "MCC"       	INTEGER,
        "MNC"       	INTEGER,
        "LAC"       	INTEGER,
        "CID"       	INTEGER,
        "PSC"       	INTEGER,
        "gps_lat"   	REAL,
        "gps_lon"   	REAL,
        "isGPSexact"	INTEGER,
        "avg_range" 	INTEGER,
        "avg_signal"	INTEGER,
        "samples"   	INTEGER,
        "time_first"	INTEGER,
        "time_last" 	INTEGER,
        "rej_cause" 	INTEGER
     */
    public Cursor returnDBeImport(){
        return mDb.rawQuery("SELECT * FROM DBe_import",null);
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
    public Cursor returnDBiBts(){
        return mDb.rawQuery("SELECT * FROM DBi_bts",null);
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
    public Cursor returnDBiMeasure(){
        return mDb.rawQuery("SELECT * FROM DBi_measure",null);
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
    public Cursor returnDetectionFlags(){
        return mDb.rawQuery("SELECT * FROM DetectionFlags",null);
    }

    // TODO: THESE ARE OUTDATED!! Please see design and update
    /**
     Returned Columns:
     "_id"            	INTEGER PRIMARY KEY AUTOINCREMENT,
     "time"     		TEXT NOT NULL,
     "LAC"           	INTEGER NOT NULL,
     "CID"           	INTEGER NOT NULL,
     "PSC"           	INTEGER,
     "gpsd_lat"      	TEXT,--Should this be double?
     "gpsd_lon"      	TEXT,--Should this be double?
     "gpsd_accu"     	INTEGER,
     "DF_id"         	INTEGER,
     "DF_description"	TEXT,
     */
    public Cursor returnEventLogData() {
        return mDb.rawQuery("SELECT * FROM EventLog",null);
    }

    /**
     Returned Columns:
     "_id"         	INTEGER PRIMARY KEY,
     "description"	TEXT
     */
    public Cursor returnSectorType() {
        return mDb.rawQuery("SELECT * FROM SectorType",null);
    }

    /**
     Returned Columns:
     "_id"     	INTEGER PRIMARY KEY AUTOINCREMENT,
     "detection_str"   	TEXT,
     "sms_type"			TEXT--(WapPush MWI TYPE0 etc..)
     */
    public Cursor returnDetectionStrings() {
        return mDb.rawQuery("SELECT * FROM DetectionStrings",null);
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
    public Cursor returnSmsData(){
        return  mDb.rawQuery("SELECT * FROM SmsData",null);
    }

    //----END OF RETURN DATABASE CURSORS------//

    //====================================================================
    //      START OF INSERT FUNCTIONS
    //====================================================================

    public void insertDefaultLocation(String country, int mcc, String lat, String lon){

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

        Cursor cursor = mDb.rawQuery(query,null);
        if(cursor.getCount() <= 0){
            // <= 0 means country is not in database yet
            mDb.insert("defaultlocation", null, def_location);
        }
        cursor.close();

    }

    public boolean insertApiKeys(String name,
                                 String type,
                                 String key,
                                 String time_add,
                                 String time_exp){
        ContentValues ApiKeys = new ContentValues();
        ApiKeys.put("name",name);
        ApiKeys.put("type",type);
        ApiKeys.put("key",key);
        ApiKeys.put("time_add",time_add);
        ApiKeys.put("time_exp",time_exp);

        String query = String.format("SELECT * FROM API_keys WHERE key = \"%s\"",key);
        Cursor cursor = mDb.rawQuery(query,null);

        // Only insert a new key if the key not already in DB
        if( cursor.getCount() <= 0){
            mDb.insert("API_keys", null, ApiKeys);
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public void insertCounterMeasures(String name,
                                      String description,
                                      int thresh,
                                      double thfine){

        ContentValues counterMeasures = new ContentValues();
        counterMeasures.put("name",name);
        counterMeasures.put("description",description);
        counterMeasures.put("thresh",thresh);
        counterMeasures.put("thfine",thfine);

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
    public void insertDBeCapabilities(String mcc,
                                      String mnc,
                                      String lac,
                                      String op_name,
                                      String band_plan,
                                      String __EXPAND__){

        ContentValues dbeCapabilities = new ContentValues();

        dbeCapabilities.put("MCC",mcc);
        dbeCapabilities.put("MNC",mnc);
        dbeCapabilities.put("LAC",lac);
        dbeCapabilities.put("op_name",op_name);
        dbeCapabilities.put("band_plan",band_plan);
        dbeCapabilities.put("__EXPAND__",__EXPAND__);

        mDb.insert("DBe_capabilities", null, dbeCapabilities);
    }

    /**
     * //TODO: @EVA update comments as not I don't think there is an issue with this
     *  Description:    This method is used to insert and populate the downloaded or previously
     *                  backed up OCID details into the DBe_import database table.
     *                  It also prevents adding multiple entries of the same cell-id, when OCID
     *                  downloads are repeated.
     *
     *  Issues:     [ ] None, but see GH issue #303 for a smarter OCID download handler.
     *
     *  Notes:       a) Move to:  CellTracker.java  see:
     *                  https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/290#issuecomment-72303486
     *               b) OCID CellID is of the "long form" (>65535) when available...
     *               c) is also used to where CSV data is populating the opencellid table.
     *
     *
     *
     *
     *
     */
    public void insertDBeImport(String db_src,
                                String rat,
                                int mcc,
                                int mnc,
                                int lac,
                                int cid,
                                int psc,
                                String lat,             // TODO: change to real
                                String lon,             // TODO: change to real
                                int isGPSexact,
                                int avg_range,
                                int avg_signal,
                                int samples,
                                String time_first,      // TODO: change to int
                                String time_last,       // TODO: change to int
                                int rej_cause){


        ContentValues dbeImport = new ContentValues();

        dbeImport.put("DBsource",db_src);
        dbeImport.put("RAT",rat);
        dbeImport.put("MCC",mcc);
        dbeImport.put("MNC",mnc);
        dbeImport.put("LAC",lac);
        dbeImport.put("CID",cid);
        dbeImport.put("PSC",psc);
        dbeImport.put("gps_lat",lat);
        dbeImport.put("gps_lon",lon);
        dbeImport.put("isGPSexact",isGPSexact);
        dbeImport.put("avg_range",avg_range);
        dbeImport.put("avg_signal",avg_signal);
        dbeImport.put("samples",samples);
        dbeImport.put("time_first",time_first);
        dbeImport.put("time_last",time_last);
        dbeImport.put("rej_cause",rej_cause);

        String query = String.format("SELECT * FROM DBe_import WHERE LAC = %d AND CID = %d ",lac,cid);
        /*
            Check that the lac and cid not known in the DBe_import
            to avoid duplicate cells

            Replaces openCellExists()
         */
        Cursor cursor = mDb.rawQuery(query,null);
        if(cursor.getCount() <= 0){
            // <= 0 means cell is not in database yet
            mDb.insert("DBe_import",null,dbeImport);
        }
        cursor.close();

    }
    /**
     * Description:     Created this because we don't need to insert all the data in this table
     *                  since we don't yet have items like TMSI etc.
     */
    public void insertBTS(Cell cell){

        //If LAC and CID are not in the DBi_bts (which) store it
        if(!cellInDbiBts(cell.getLAC(),cell.getCID())) {

            ContentValues values = new ContentValues();

            values.put("MCC", cell.getMCC());
            values.put("MNC", cell.getMNC());
            values.put("LAC", cell.getLAC());
            values.put("CID", cell.getCID());
            values.put("PSC", cell.getPSC());
            //setting these to 0 because if empty causing error in db restore
            //values.put("T3212",0);
            //values.put("A5x",0);
            //values.put("ST_id",0);
            values.put("time_first", MiscUtils.getCurrentTimeStamp());
            values.put("time_last", MiscUtils.getCurrentTimeStamp());
            values.put("gps_lat", cell.getLat());
            values.put("gps_lon", cell.getLon());

            mDb.insert("DBi_bts", null, values);

            Log.i(TAG, mTAG + ": DBi_bts was populated.");
        }else{

             // If cell is in the DB, update it to last time seen and
             // update its GPS coordinates, if not 0.0
            ContentValues values = new ContentValues();
            values.put("time_last",MiscUtils.getCurrentTimeStamp());

            // Only update if GPS coordinates are good
            if(cell.getLat() != 0.0 && cell.getLat() != 0
                    && cell.getLon() != 0.0 && cell.getLon() != 0){
                values.put("gps_lat", cell.getLat());
                values.put("gps_lon", cell.getLon());
            }

            // This is the native update equivalent to:
            // "UPDATE Dbi_bts time_last=...,gps_lat=..., gps_lon=... WHERE CID=..."
            // update (String table, ContentValues values, String whereClause, String[] whereArgs)
            mDb.update("DBi_bts", values,"CID=?", new String[]{Integer.toString(cell.getCID())} );

            Log.i(TAG, mTAG + ": DBi_bts updated: CID=" + cell.getCID() + " LAC=" + cell.getLAC());
        }


        // Checking to see is cellID(bts_id) already in DBi_measure
        if(!cellInDbiMeasure(cell.getCID())){
            ContentValues dbiMeasure = new ContentValues();

            dbiMeasure.put("bts_id",cell.getCID());
            dbiMeasure.put("nc_list","no_data");                        // TODO: Better with "n/a" or "0", where are we getting this?
            dbiMeasure.put("time", MiscUtils.getCurrentTimeStamp());

            String slat = String.valueOf(cell.getLat());
            String slon = String.valueOf(cell.getLon());

            if (slat == null){slat = "0.0";}
            if (slon == null){slat = "0.0";}

            dbiMeasure.put("gpsd_lat", slat);
            dbiMeasure.put("gpsd_lon", slon);
            dbiMeasure.put("gpsd_accu", cell.getAccuracy());
            //dbiMeasure.put("gpse_lat",gpse_lat);
            //dbiMeasure.put("gpse_lat",gpse_lon);
            dbiMeasure.put("bb_power",String.valueOf(cell.getDBM()));
            //dbiMeasure.put("bb_rf_temp",bb_rf_temp);
            dbiMeasure.put("tx_power","0");                             //TODO putting 0 here as we dont have this value yet
            dbiMeasure.put("rx_signal",String.valueOf(cell.getDBM()));  //TODO putting cell.getDBM() here so we have some signal for OCID upload.
            //dbiMeasure.put("rx_stype",rx_stype);
            dbiMeasure.put("RAT", String.valueOf(cell.getNetType()));
            //dbiMeasure.put("BCCH",BCCH);
            //dbiMeasure.put("TMSI",TMSI);
            dbiMeasure.put("TA",cell.getTimingAdvance());               //TODO does this actually get timing advance?
            //dbiMeasure.put("PD",PD);
            dbiMeasure.put("BER",0);                                    //TODO setting 0 because we dont have data yet.
            //dbiMeasure.put("AvgEcNo",AvgEcNo);
            dbiMeasure.put("isSubmitted",0);
            dbiMeasure.put("isNeighbour",0);

            mDb.insert("DBi_measure", null, dbiMeasure);
            Log.i(mTAG, "DBi_measure inserted bts_id="+cell.getCID());

        }else{
            // Updating DBi_measure tables if already exists.
            //TODO commented out items are because we don't have this data yet or it doesn't need updating
            ContentValues dbiMeasure = new ContentValues();
            //dbiMeasure.put("nc_list", nc_list);
            //dbiMeasure.put("time", MiscUtils.getCurrentTimeStamp());

            if(cell.getLat() != 0.0 && cell.getLon() != 0.0){
                dbiMeasure.put("gpsd_lat", cell.getLat());
                dbiMeasure.put("gpsd_lon", cell.getLon());
            }
            if(cell.getAccuracy() != 0.0 && cell.getAccuracy() > 0) {
                dbiMeasure.put("gpsd_accu", cell.getAccuracy());
            }
            //dbiMeasure.put("gpse_lat",gpse_lat);
            //dbiMeasure.put("gpse_lat",gpse_lon);
            if(cell.getDBM() > 0) {
                dbiMeasure.put("bb_power", String.valueOf(cell.getDBM()));
            }
            //dbiMeasure.put("bb_rf_temp",bb_rf_temp);
            //TODO set correct value for tx_power and rx_signal and un comment when working again
 /*           if(cell.getRssi() >0) {
                dbiMeasure.put("tx_power", String.valueOf(cell.getRssi()));
                dbiMeasure.put("rx_signal",String.valueOf(cell.getRssi()));
            }
*/
            //dbiMeasure.put("rx_stype",rx_stype);
            //dbiMeasure.put("RAT", String.valueOf(cell.getNetType()));
            //dbiMeasure.put("BCCH",BCCH);
            //dbiMeasure.put("TMSI",TMSI);
            if(cell.getTimingAdvance() > 0) {
                dbiMeasure.put("TA", cell.getTimingAdvance());  // Only available on API >16 on LTE
            }

            //dbiMeasure.put("PD",PD);                          // TODO: LTE: I need to check this...
            //dbiMeasure.put("BER",0);                          // getGsmBitErrorRate() for API 17+
            // See: http://developer.android.com/reference/android/telephony/SignalStrength.html

            //dbiMeasure.put("AvgEcNo",AvgEcNo);                // TODO: I need to check this...

            mDb.update("DBi_measure",dbiMeasure,"bts_id=?",new String[]{Integer.toString(cell.getCID())});
            Log.i(TAG, mTAG + ": DBi_measure updated bts_id="+cell.getCID());

        }

    }


    /**
     * Inserts (API?) Cell Details into Database (DBi_bts)
     * Used be restoreDB()
     */
    // TODO: THESE ARE OUTDATED!! Please see design and update
    public void insertBTS(
            int mcc,
            int mnc,
            int lac,
            int cid,
            int psc,
            int t3231,
            int a5x,
            int st_id,
            String time_first,
            String time_last,
            double lat,
            double lon
    ) {

        if (cid != -1) {
            // Populate Content Values for INSERT or UPDATE
            ContentValues btsValues = new ContentValues();
            btsValues.put("MCC",        mcc);
            btsValues.put("MNC",        mnc);
            btsValues.put("LAC",        lac);
            btsValues.put("CID",        cid);
            btsValues.put("PSC",        psc);
            btsValues.put("T3212",    t3231);
            btsValues.put("A5x",        a5x);
            btsValues.put("ST_id",    st_id);
            btsValues.put("time_first", time_first);
            btsValues.put("time_last",  time_last);
            btsValues.put("gps_lat", lat);
            btsValues.put("gps_lon",  lon);

            // Only insert new cell if its not in DBi_bts
            if(!cellInDbiBts(lac,cid)){
                mDb.insert("DBi_bts", null, btsValues);
            }else{
                // =======================================================================
                // TODO: EVA do I need to update an already known cell?
                // Good question! The DBi_bts is meant to be physical (non-volatile) and
                // stay the same, but I now see that PSC, T3212, A5x etc., might change,
                // under certain and special circumstances.
                // However, if they do, we should log this as an (suspicious?) event in EventLog
                // =======================================================================
                mDb.update( "DBi_bts", btsValues, "CID=?", new String[]{Integer.toString(cid)} );
                Log.i(TAG, mTAG + ": Warning: Physical cell data in DBi_bts has changed! CID=" + cid);
            }
        }

    }

    /**
     * Description:     TODO: add description
     *                  Used be restoreDB()
     * Issues:
     *              [ ] Where is this used and how?
     *              [ ] MAYBE it's current use is still okay?
     *              [ ] TODO: Using bts_id is WRONG! That is a foreign key id in the DBi_bts and used in DBi_measure
     *
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
                                 int isNeighbour){

        // Check bts_id is not already stored int DBi_measure. Only adds new cell if false
        // @banjaxbanjo:  No this is wrong...
        //
        if(cellInDbiMeasure(bts_id)){
            ContentValues dbiMeasure = new ContentValues();

            dbiMeasure.put("bts_id",bts_id); // Try replacing this with cid

            dbiMeasure.put("nc_list",nc_list);
            dbiMeasure.put("time",time);
            dbiMeasure.put("gpsd_lat",gpsd_lat);
            dbiMeasure.put("gpsd_lon",gpsd_lon);
            dbiMeasure.put("gpsd_accu",gpsd_accuracy);
            dbiMeasure.put("gpse_lat",gpse_lat);
            dbiMeasure.put("gpse_lon",gpse_lon);
            dbiMeasure.put("bb_power",bb_power);
            dbiMeasure.put("bb_rf_temp",bb_rf_temp);
            dbiMeasure.put("tx_power",tx_power);
            dbiMeasure.put("rx_signal",rx_signal);
            dbiMeasure.put("rx_stype",rx_stype);
            dbiMeasure.put("RAT",rat);
            dbiMeasure.put("BCCH",BCCH);
            dbiMeasure.put("TMSI",TMSI);
            dbiMeasure.put("TA",TA);
            dbiMeasure.put("PD",PD);
            dbiMeasure.put("BER",BER);
            dbiMeasure.put("AvgEcNo",AvgEcNo);
            dbiMeasure.put("isSubmitted",isSubmitted);
            dbiMeasure.put("isNeighbour",isNeighbour);

            mDb.insert("DBi_measure", null, dbiMeasure);
            // try with:
            // mDb.insert("DBi_measure, DBi_bts", null, dbiMeasure); // but you'll need the DBi_bts data then as well...
        }

    }

    /**
     * Description:     This inserts Detection Flag data that is used to fine tune the various
     *                  available detection mechanisms. (See Detection List in issue #230)
     *
     *                  These parameters are described elsewhere, but should be included here
     *                  for completeness and reference.
     *
     *                  There's also a CounterMeasure id to link to possible future counter measures.
     * Issues:
     *              [ ]
     *
     */
    public void insertDetectionFlags(int code,
                                     String name,
                                     String description,
                                     int p1,int p2,int p3,
                                     double p1_fine,double p2_fine,double p3_fine,
                                     String app_text,
                                     String func_use,
                                     int istatus,
                                     int CM_id
    ){

        ContentValues detectionFlags = new ContentValues();
        detectionFlags.put("code",code);
        detectionFlags.put("name",name);
        detectionFlags.put("description",description);
        detectionFlags.put("p1",p1);
        detectionFlags.put("p2",p2);
        detectionFlags.put("p3",p3);
        detectionFlags.put("p1_fine",p1_fine);
        detectionFlags.put("p2_fine",p2_fine);
        detectionFlags.put("p3_fine",p3_fine);
        detectionFlags.put("app_text",app_text);
        detectionFlags.put("func_use",func_use);
        detectionFlags.put("istatus",istatus);
        detectionFlags.put("CM_id",CM_id);

        mDb.insert("DetectionFlags", null, detectionFlags);
    }

    /**
     * Description:     Inserts log data into the EventLog table
     *
     * Issues:          [ ] ALL events should be logged!!
     *
     * Notes:           Table item order:
     *                  time,LAC,CID,PSC,gpsd_lat,gpsd_lon,gpsd_accu,DF_id,DF_desc
     *
     *
     * This table was previously known as insertDetection
     */
    public void insertEventLog(String time,
                               int lac,
                               int cid,
                               int psc,
                               String gpsd_lat,
                               String gpsd_lon,
                               int gpsd_accu,
                               int DF_id,
                               String DF_description){

        //Query examples for future devs
        //SELECT * FROM EventLog WHERE CID = 1234 AND LAC = 4321 AND DF_id BETWEEN 1 AND 4
        //SELECT * FROM EventLog WHERE CID = 1234 AND LAC = 4321 AND DF_id = 1" Changing LAC
        //SELECT * FROM EventLog WHERE CID = 1234 AND LAC = 4321 AND DF_id = 2" Cell not in OCID
        //SELECT * FROM EventLog WHERE CID = 1234 AND LAC = 4321 AND DF_id = 3" Detected SMS
        //SELECT * FROM EventLog WHERE CID = 1234 AND LAC = 4321 AND DF_id = 4" Unknown T.B.A...

        /*
            We need to check if cell is not in OCID  Events are not continuously logged
            to the database as it currently stands. If the same cell shows up it will again be
            dumped to the event log and will fill up pretty quickly

         */
        String query = String.format(
                "SELECT * FROM EventLog WHERE CID = %d AND LAC = %d AND DF_id = %d",
                cid, lac, DF_id);

        // Check that the lac/cid/DF_id is not already known, if not, then INSERT
        Cursor cursor = mDb.rawQuery(query,null);

        // @banjaxobanjo    What do you mean here? ALL events should be logged!
        boolean EVENT_ALREADY_LOGGED = cursor.getCount() > 0;   // if > 0 Event is logged boolean will be true
        Log.d(TAG, mTAG + ": EVENT_ALREADY_LOGGED=" + EVENT_ALREADY_LOGGED );
        boolean insertData = true;                              // default
        cursor.close();

        switch (DF_id){
            case 1:
                // Is lac and cid already logged for  CHANGING LAC
                if(EVENT_ALREADY_LOGGED) {
                    insertData = false;
                }
                break;

            case 2:
                // Is lac and cid already logged for Cell not in OCID
                if(EVENT_ALREADY_LOGGED) {
                    insertData = false;
                }
                break;

            case 3:
                // Store detected SMS
                //
                // We don't really need to check this event because it will only be inserted if an
                // SMS is detected and we won't have duplicates
                //insertData = true;
                break;

            case 4:
                //future code TODO: where are the DF_id codes? Like 1 = Changing Lac?
                //insertData = true; //already set above
                break;
        }

        // Not sure this should be here as all event should be logged! --E:V:A
        if(insertData){

            ContentValues eventLog = new ContentValues();

            eventLog.put("time",time);
            eventLog.put("LAC",lac);
            eventLog.put("CID",cid);
            eventLog.put("PSC",psc);
            eventLog.put("gpsd_lat",gpsd_lat);
            eventLog.put("gpsd_lon",gpsd_lon);
            eventLog.put("gpsd_accu",gpsd_accu);
            eventLog.put("DF_id",DF_id);
            eventLog.put("DF_description",DF_description);

            mDb.insert("EventLog", null, eventLog);
            Log.v(TAG, mTAG + ": Insert Detection into EventLog Table: " + cid);

        }else{
            // TODO do we need to do anything if event already logged?
            // TODO See my comments above -- E:V:A
        }

    }

    /**
     * Description:     Inserts BTS Sector Type data into the SectorType table
     *
     * Issues:
     *          [ ]
     *
     * Notes:
     *
     */
    public void insertSectorType(String description){

        ContentValues sectorType = new ContentValues();
        sectorType.put("description",description);
        mDb.insert("SectorType", null, sectorType);

    }

    /**
     * Description:     Inserts SMS Detection Strings data into the DetectionStrings table
     *
     * Issues:
     *          [ ]
     *
     * Notes:
     *
     */
    public void insertDetectionStrings(String det_str,
                                       String sms_type){

        ContentValues detectionStrings = new ContentValues();
        detectionStrings.put("det_str",det_str);
        detectionStrings.put("sms_type",sms_type);

        String query = String.format(
                "SELECT * FROM DetectionStrings WHERE det_str = \"%s\" AND sms_type = \"%s\"",
                det_str, sms_type);

        // Check that string not in db then insert
        Cursor cursor = mDb.rawQuery(query,null);

        if( cursor.getCount() <= 0){
            mDb.insert("DetectionStrings", null, detectionStrings);
            cursor.close();
        }else{
            cursor.close();
        }
    }

    /**
     *  Description:     inserts detected silent sms data into TABLE: SmsData
     *
     *  Issues:
     *          [ ]
     *
     *  Notes:
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
                              int isRoaming){

        /*
            public static final String SMS_DATA_TABLE_NAME = "SmsData";
            public static final String SMS_DATA_ID = "_id";
            public static final String SMS_DATA_TIMESTAMP = "time";
            public static final String SMS_DATA_SENDER_NUMBER = "number";
            public static final String SMS_DATA_SENDER_SMSC = "smsc";
            public static final String SMS_DATA_SENDER_MSG = "message";
            public static final String SMS_DATA_SMS_TYPE = "type";
            public static final String SMS_DATA_SMS_CLASS = "class";
            public static final String SMS_DATA_LAC = "lac";
            public static final String SMS_DATA_CID = "cid";
            public static final String SMS_DATA_RAT = "rat";
            public static final String SMS_DATA_GPS_LAT = "gps_lat";
            public static final String SMS_DATA_GPS_LON = "gps_lon";
            public static final String SMS_DATA_ROAM_STATE = "isRoaming";
         */
        ContentValues smsData = new ContentValues();

        smsData.put("time",time);
        smsData.put("number",number);
        smsData.put("smsc",smsc);
        smsData.put("message",message);
        smsData.put("type",type);
        smsData.put("class",CLASS);
        smsData.put("lac",lac);
        smsData.put("cid",cid);
        smsData.put("rat",rat);
        smsData.put("gps_lat",gps_lat);
        smsData.put("gps_lon",gps_lon);
        smsData.put("isRoaming",isRoaming);

        // Check that timestamp not already in DB, then INSERT
        if(!isTimeStampInDB(time)){
            mDb.insert("SmsData", null, smsData);
        }

    }

    /**
     *  Description:    This checks if a cell with a given CID already exists
     *                  in the (DBe_import) database.
     *
     *                  used in CellTracker()
     */
    public boolean openCellExists(int cellID) {
        String qry = String.format(
                "SELECT * FROM DBe_import WHERE CID = %d",
                cellID);

        Cursor cursor = mDb.rawQuery(qry, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /*
        Check cid and lac is in DBi_bts
        Replaces cellExists()
    */
    public boolean cellInDbiBts(int lac,int cellID){
        String query = String.format(
                "SELECT * FROM DBi_bts WHERE LAC = %d AND CID = %d",
                lac, cellID);

        Cursor cursor = mDb.rawQuery(query,null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /*
        Check is cid(bts_id) in DBi_measure
    */
    public boolean cellInDbiMeasure(int cellID){
        String query = String.format(
                "SELECT * FROM DBi_measure WHERE bts_id = %d",
                cellID);

        Cursor cursor = mDb.rawQuery(query,null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /*
        Returns the RAT for CellId in Database from DBi_measure as DBi_bts does not
        have RAT column and RAT is needed for OCID upload

     */
    public String getRatFromDBimeasure(int cellID){
        String RAT = null;
        String query = String.format(
                "SELECT * FROM DBi_measure WHERE bts_id = %d",
                cellID);

        Cursor cursor = mDb.rawQuery(query,null);
        if(cursor != null && cursor.moveToNext()){
            RAT = cursor.getString(cursor.getColumnIndex("RAT"));
        }
        cursor.close();
        return RAT;
    }

}

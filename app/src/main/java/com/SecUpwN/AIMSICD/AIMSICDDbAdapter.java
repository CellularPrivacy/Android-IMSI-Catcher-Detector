package com.SecUpwN.AIMSICD;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;

import au.com.bytecode.opencsv.CSVWriter;

public class AIMSICDDbAdapter {

    private final String TAG = "AISMICD_DbAdaptor";

    private final DbHelper mDbHelper;
    private SQLiteDatabase mDb;
    private Context mContext;
    private static final int DATABASE_VERSION = 2;
    private static final String COLUMN_ID = "_id";
    private final String LOCATION_TABLE = "locationinfo";
    private final String CELL_TABLE = "cellinfo";
    private final String SIGNAL_TABLE = "signalinfo";
    private final String OPENCELLID_TABLE = "opencellid";
    private final String DB_NAME = "myCellInfo";
    private final String FOLDER = Environment.getExternalStorageDirectory() + "/AIMSICD/";

    public AIMSICDDbAdapter(Context context) {
        mContext = context;
        mDbHelper = new DbHelper(context);
    }

    public AIMSICDDbAdapter open() throws SQLException {
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    /**
     * Location Tracking Database
     */
    private final String LOC_DATABASE_CREATE = "create table " +
            LOCATION_TABLE + " (" + COLUMN_ID +
            " integer primary key autoincrement, Lac INTEGER, CellID INTEGER, " +
            "Net VARCHAR, Lat VARCHAR, Lng VARCHAR, Signal INTEGER, Connection VARCHAR, " +
            "Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);";

    /**
     * Cell Information Tracking Database
     */
    private final String CELL_DATABASE_CREATE = "create table " +
            CELL_TABLE + " (" + COLUMN_ID +
            " integer primary key autoincrement, Lac INTEGER, CellID INTEGER, " +
            "Net VARCHAR, Lat VARCHAR, Lng VARCHAR, Signal INTEGER, Connection VARCHAR, " +
            "Country VARCHAR, Operator VARCHAR, OperatorName VARCHAR, " +
            "Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);";

    /**
     * Signal Strength Tracking Database
     */
    private final String SIG_DATABASE_CREATE = "create table " +
            SIGNAL_TABLE + " (" + COLUMN_ID +
            " integer primary key autoincrement, Lac INTEGER, CellID INTEGER, " +
            "Net VARCHAR, Lat VARCHAR, Lng VARCHAR, Signal INTEGER, Connection VARCHAR, " +
            "Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);";

    /**
     * OpenCellID Cell Information Database
     */
    private final String OPENCELLID_DATABASE_CREATE = "create table " +
            OPENCELLID_TABLE + " (" + COLUMN_ID +
            " integer primary key autoincrement, Lat VARCHAR, Lng VARCHAR, Mcc INTEGER, " +
            "Mnc INTEGER, Lac INTEGER, CellID INTEGER, AvgSigStr INTEGER, Samples INTEGER, " +
            "Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);";

    /**
     * Inserts Cell Details into Database
     *
     * @return row id or -1 if error
     */
    public long insertCell( int lac, int cellID,
            int netType, double latitude, double longitude,
            int signalInfo, String cellInfo, String simCountry,
            String simOperator, String simOperatorName) {

        //Populate Content Values for Insert or Update
        ContentValues cellValues = new ContentValues();
        cellValues.put("Lac", lac);
        cellValues.put("CellID", cellID);
        cellValues.put("Net", netType);
        cellValues.put("Lat", latitude);
        cellValues.put("Lng", longitude);
        cellValues.put("Signal", signalInfo);
        cellValues.put("Connection", cellInfo);
        cellValues.put("Country", simCountry);
        cellValues.put("Operator", simOperator);
        cellValues.put("OperatorName", simOperatorName);

        if (cellExists(cellID)) {
            return mDb.update(CELL_TABLE, cellValues, "CellID=?",
                    new String[]{Integer.toString(cellID)});
        } else {
            return mDb.insert(CELL_TABLE, null, cellValues);
        }
    }

    /**
     * Inserts OpenCellID Details into Database
     *
     * @return row id or -1 if error
     */
    public long insertOpenCell( double latitude, double longitude,
            int mcc, int mnc, int lac, int cellID, int avgSigStr,
            int samples) {

        //Populate Content Values for Insert or Update
        ContentValues cellIDValues = new ContentValues();
        cellIDValues.put("Lat", latitude);
        cellIDValues.put("Lng", longitude);
        cellIDValues.put("Mcc", mcc);
        cellIDValues.put("Mnc", mnc);
        cellIDValues.put("Lac", lac);
        cellIDValues.put("CellID", cellID);
        cellIDValues.put("AvgSigStr", avgSigStr);
        cellIDValues.put("Samples", samples);

        if (openCellExists(cellID)) {
            return mDb.update(OPENCELLID_TABLE, cellIDValues,
                    "CellID=?",
                    new String[]{Integer.toString(cellID)});
        } else {
            return mDb.insert(OPENCELLID_TABLE, null, cellIDValues);
        }
    }

    /**
     * Inserts Location Details into Database
     *
     * @return row id or -1 if error
     */
    public long insertLocation(int lac, int cellID,
            int netType, double latitude, double longitude,
            int signalInfo, String cellInfo) {

        //Populate Content Values for Insert or Update
        ContentValues locationValues = new ContentValues();
        locationValues.put("Lac", lac);
        locationValues.put("CellID", cellID);
        locationValues.put("Net", netType);
        locationValues.put("Lat", latitude);
        locationValues.put("Lng", longitude);
        locationValues.put("Signal", signalInfo);
        locationValues.put("Connection", cellInfo);

        if (locationExists(cellID)) {
            return mDb.update(LOCATION_TABLE, locationValues,"CellID=?",
                    new String[]{Integer.toString(cellID)});
        } else {
            return mDb.insert(LOCATION_TABLE, null, locationValues);
        }
    }

    /**
     * Inserts Signal Strength Details into Database
     *
     * @return row id or -1 if error
     */
    public long insertSignal(int lac, int cellID,
            int netType, double latitude, double longitude,
            int signalInfo, String cellInfo) {
        //Populate Content Values for Insert or Update
        ContentValues signalValues = new ContentValues();
        signalValues.put("Lac", lac);
        signalValues.put("CellID", cellID);
        signalValues.put("Net", netType);
        signalValues.put("Lat", latitude);
        signalValues.put("Lng", longitude);
        signalValues.put("Signal", signalInfo);
        signalValues.put("Connection", cellInfo);

        if (cellSignalExists(cellID)) {
            return mDb.update(SIGNAL_TABLE, signalValues, "CellID=?",
                    new String[]{Integer.toString(cellID)});
        } else {
            return mDb.insert(LOCATION_TABLE, null, signalValues);
        }
    }

    /**
     * Returns Signal Strength database contents
     */
    public Cursor getSignalData() {
        return mDb.query(true, SIGNAL_TABLE, new String[] {"Net", "Lat", "Lng", "Signal", "CellID"},
                "Lat <> 0.0 AND lng <> 0.0",null,null,null,null,null);
    }

    /**
     * Checks to see if Location already exists in database
     */
    public boolean locationExists(int cellID) {
        Cursor cursor = mDb.rawQuery("SELECT * FROM " + LOCATION_TABLE + " WHERE CellID = " +
                cellID, null);

        return cursor.getCount()>0;
    }

    /**
     * Checks to see if Cell already exists in database
     */
    public boolean cellExists(int cellID) {
        Cursor cursor = mDb.rawQuery("SELECT * FROM " + CELL_TABLE + " WHERE CellID = " +
                cellID, null);

        return cursor.getCount()>0;
    }

    /**
     * Checks to see if Cell already exists in OpenCellID database
     */
    public boolean openCellExists(int cellID) {
        Cursor cursor = mDb.rawQuery("SELECT * FROM " + OPENCELLID_TABLE + " WHERE CellID = " +
                cellID, null);

        return cursor.getCount()>0;
    }

    /**
     * Checks to see if Cell Signal data already exists in database
     */
    public boolean cellSignalExists(int cellID) {
        Cursor cursor = mDb.rawQuery("SELECT * FROM " + SIGNAL_TABLE + " WHERE CellID = " +
                cellID, null);

        return cursor.getCount()>0;
    }

    /**
     * Exports the database tables to CSV files
     */
    public void exportDB () {
        try {
            export(LOCATION_TABLE);
            export(CELL_TABLE);
            export(SIGNAL_TABLE);
            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.database_export_successful)
                    .setMessage("Database tables exported succesfully to:\n" + FOLDER);
            builder.create().show();
        } catch (Exception ioe) {
            Log.e (TAG, "exportDB() " + ioe.getMessage());
        }
    }

    private void export(String tableName) {
        Log.i(TAG, "exporting database - " + DB_NAME);

        File dir = new File(FOLDER);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, "aimsicd-" + tableName + ".csv");


        try {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            Log.d(TAG, "table name " + tableName);
            open();
            Cursor c = mDb.rawQuery("SELECT * FROM " + tableName, new String[0]);

            csvWrite.writeNext(c.getColumnNames());
            String[] rowData = new String[c.getColumnCount()];

            while (c.moveToNext()) {
                for (int i = 0; i < c.getColumnCount(); i++) {
                    rowData[i] = c.getString(i);
                }
                csvWrite.writeNext(rowData);
            }

            csvWrite.close();
            c.close();
        } catch (Exception e) {
            Log.e(TAG, "Error exporting table " + tableName + " " + e);
        }

        Log.i(TAG, "exporting database complete");
    }

    /**
     * DbHelper class for the SQLite Database functions
     */
    public class DbHelper extends SQLiteOpenHelper {

        DbHelper(Context context) {
            super(context, DB_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL(LOC_DATABASE_CREATE);
            database.execSQL(CELL_DATABASE_CREATE);
            database.execSQL(SIG_DATABASE_CREATE);
            database.execSQL(OPENCELLID_DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG,
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data"
            );
            db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + CELL_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + SIGNAL_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + OPENCELLID_TABLE);
            onCreate(db);
        }
    }

}

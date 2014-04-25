package com.SecUpwN.AIMSICD;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVWriter;

public class AIMSICDDbAdapter {

    private final String TAG = "AISMICD_DbAdaptor";

    private final DbHelper mDbHelper;
    private SQLiteDatabase mDb;
    private Context mContext;
    private static final int DATABASE_VERSION = 1;
    private static final String COLUMN_ID = "_id";
    private final String LOCATION_TABLE = "locationinfo";
    private final String CELL_TABLE = "cellinfo";
    private final String SIGNAL_TABLE = "signalinfo";
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
     * Inserts Cell Details into Database
     */
    public void insertCell( int lac, int cellID,
            int netType, double latitude, double longitude,
            int signalInfo, String cellInfo, String simCountry,
            String simOperator, String simOperatorName) {

        mDb.execSQL("INSERT INTO " + CELL_TABLE +
                " (Lac , CellID, Net, Lat, Lng, Signal, Connection," +
                "Country, Operator, OperatorName)" +
                " VALUES(" + lac + "," + cellID + "," + netType + ","
                + latitude + "," + longitude + "," + signalInfo + ",\""
                + cellInfo + "\", \"" + simCountry + "\"," + simOperator + ",\""
                + simOperatorName + "\");");
    }

    /**
     * Inserts Location Details into Database
     */
    public void insertLocation(int lac, int cellID,
            int netType, double latitude, double longitude,
            int signalInfo, String cellInfo) {

        mDb.execSQL("INSERT INTO " + LOCATION_TABLE +
                " (Lac , CellID, Net, Lat, Lng, Signal, Connection)" +
                " VALUES(" + lac + "," + cellID + "," + netType + ","
                + latitude + "," + longitude + "," + signalInfo + ",\""
                + cellInfo + "\");");
    }

    /**
     * Inserts Signal Strength Details into Database
     */
    public void insertSignal(int lac, int cellID,
            int netType, double latitude, double longitude,
            int signalInfo, String cellInfo) {

        mDb.execSQL("INSERT INTO " + SIGNAL_TABLE +
                " (Lac , CellID, Net, Lat, Lng, Signal, Connection)" +
                " VALUES(" + lac + "," + cellID + "," + netType + ","
                + latitude + "," + longitude + "," + signalInfo + ",\""
                + cellInfo + "\");");
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
            onCreate(db);
        }

    }

}

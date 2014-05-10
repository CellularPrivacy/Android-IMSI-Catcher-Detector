package com.SecUpwN.AIMSICD.adapters;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.rilexecutor.HexDump;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class AIMSICDDbAdapter {

    private final String TAG = "AISMICD_DbAdaptor";

    private final DbHelper mDbHelper;
    private SQLiteDatabase mDb;
    private Context mContext;
    private static final int DATABASE_VERSION = 4;
    private static final String COLUMN_ID = "_id";
    private final String LOCATION_TABLE = "locationinfo";
    private final String CELL_TABLE = "cellinfo";
    private final String OPENCELLID_TABLE = "opencellid";
    private final String DEFAULT_MCC_TABLE = "defaultlocation";
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
     * OpenCellID Cell Information Database
     */
    private final String OPENCELLID_DATABASE_CREATE = "create table " +
            OPENCELLID_TABLE + " (" + COLUMN_ID +
            " integer primary key autoincrement, Lat VARCHAR, Lng VARCHAR, Mcc INTEGER, " +
            "Mnc INTEGER, Lac INTEGER, CellID INTEGER, AvgSigStr INTEGER, Samples INTEGER, " +
            "Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);";

    /**
     * Default MCC Location Database
     */
    private final String DEFAULT_MCC_DATABASE_CREATE = "create table " +
            DEFAULT_MCC_TABLE + " (" + COLUMN_ID +
            " integer primary key autoincrement, Country VARCHAR, Mcc INTEGER, "
            + "Lat VARCHAR, Lng VARCHAR);";

    /**
     * Inserts Cell Details into Database
     *
     * @return row id or -1 if error
     */
    public long insertCell( int lac, int cellID,
            int netType, double latitude, double longitude,
            int signalInfo, String cellInfo, String simCountry,
            String simOperator, String simOperatorName) {

        if (cellID != -1) {
            //Populate Content Values for Insert or Update
            ContentValues cellValues = new ContentValues();
            cellValues.put("Lac", HexDump.toHexString(lac));
            cellValues.put("CellID", HexDump.toHexString(cellID));
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
        return 0;
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

        if (latitude != 0.0 && longitude != 0.0) {
            //Populate Content Values for Insert or Update
            ContentValues locationValues = new ContentValues();
            locationValues.put("Lac", HexDump.toHexString(lac));
            locationValues.put("CellID", HexDump.toHexString(cellID));
            locationValues.put("Net", netType);
            locationValues.put("Lat", latitude);
            locationValues.put("Lng", longitude);
            locationValues.put("Signal", signalInfo);
            locationValues.put("Connection", cellInfo);

            if (locationExists(cellID)) {
                return mDb.update(LOCATION_TABLE, locationValues, "CellID=?",
                        new String[]{Integer.toString(cellID)});
            } else {
                return mDb.insert(LOCATION_TABLE, null, locationValues);
            }
        }

        return 0;
    }


    /**
     * Returns Cell Information database contents
     */
    public Cursor getCellData() {
        return mDb.query(CELL_TABLE, new String[] {"CellID", "Lac", "Net", "Lat", "Lng",
                        "Signal"},
                null,null,null,null, null);
    }

    /**
     * Returns Location Information database contents
     */
    public Cursor getLocationData() {
        return mDb.query(LOCATION_TABLE, new String[] {"CellID", "Lac", "Net", "Lat", "Lng",
                        "Signal"},
                null,null,null,null,null);
    }

    /**
     * Returns OpenCellID database contents
     */
    public Cursor getOpenCellIDData() {
        return mDb.query(OPENCELLID_TABLE, new String[] {"CellID", "Lac", "Mcc", "Mnc", "Lat", "Lng",
                        "AvgSigStr", "Samples"},
                null,null,null,null,null);
    }

    /**
     * Returns Default MCC Locations database contents
     */
    public Cursor getDefaultMccLocationData() {
        return mDb.query(DEFAULT_MCC_TABLE, new String[] {"Country", "Mcc", "Lat", "Lng"},
                null,null,null,null,null);
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

    public double[] getDefaultLocation(int mcc) {
        double[] loc = new double[2];

        Cursor cursor = mDb.rawQuery("SELECT Lat, Lng FROM " + DEFAULT_MCC_TABLE + " WHERE Mcc = " +
        mcc, null);

        if (cursor.moveToFirst()) {
            loc[0] = Double.parseDouble(cursor.getString(0));
            loc[1] = Double.parseDouble(cursor.getString(1));
        } else {
            loc[0] = 0.0;
            loc[1] = 0.0;
        }

        return loc;
    }

    /**
     * Populates the Default Mcc Location table using the CSV file found in the
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

            for (int i=1; i < csvMcc.size(); i++)
            {
                defaultMccValues.put("Country", csvMcc.get(i)[0]);
                defaultMccValues.put("Mcc", csvMcc.get(i)[1]);
                defaultMccValues.put("Lng", csvMcc.get(i)[2]);
                defaultMccValues.put("Lat", csvMcc.get(i)[3]);

                db.insert(DEFAULT_MCC_TABLE, null, defaultMccValues);
            }


        } catch (Exception e) {
            Log.e (TAG, "Error parsing OpenCellID data - " + e);
        }
    }

    /**
     * Exports the database tables to CSV files
     */
    public void exportDB () {
        try {
            export(LOCATION_TABLE);
            export(CELL_TABLE);
            export(OPENCELLID_TABLE);
            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.database_export_successful)
                    .setMessage("Database tables exported succesfully to:\n" + FOLDER);
            builder.create().show();
        } catch (Exception ioe) {
            Log.e (TAG, "exportDB() " + ioe.getMessage());
        }
    }

    /**
     * Exports the database tables to CSV files
     *
     * @param tableName String representing table name to export
     */
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
            database.execSQL(OPENCELLID_DATABASE_CREATE);
            database.execSQL(DEFAULT_MCC_DATABASE_CREATE);
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
            db.execSQL("DROP TABLE IF EXISTS " + DEFAULT_MCC_TABLE);

            onCreate(db);
        }
    }

}

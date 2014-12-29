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

    private final String TAG = "AISMICD_DbAdaptor";
    public static final String FOLDER = Environment.getExternalStorageDirectory() + "/AIMSICD/";
    private static final String COLUMN_ID = "_id";
    private final String LOCATION_TABLE = "locationinfo";
    private final String CELL_TABLE = "cellinfo";
    private final String OPENCELLID_TABLE = "opencellid";
    private final String DEFAULT_MCC_TABLE = "defaultlocation";
    private final String SILENT_SMS_TABLE = "silentsms";
    private final String CELL_SIGNAL_TABLE = "cellSignal";
    // E:V:A private final String DB_NAME = "myCellInfo";
    private final String DB_NAME = "aimsicd.db";  // old name: "myCellInfo"

    private final String[] mTables;
    private final DbHelper mDbHelper;
    private SQLiteDatabase mDb;
    private final Context mContext;

    public static final int DATABASE_VERSION = 8;

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
     * Inserts Cell Details into Database
     *
     * @return row id or -1 if error
     */
    public long insertCell(int lac, int cellID,
            int netType, double latitude, double longitude,
            int signalInfo, int mcc, int mnc, double accuracy,
            double speed, double direction, String networkType, long measurementTaken) {

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
                return mDb.update(CELL_TABLE, cellValues,
                        "CellID=?",
                        new String[]{Integer.toString(cellID)});
            } else {
                return mDb.insert(CELL_TABLE, null, cellValues);
            }
        }
        return 0;
    }

    /**
     * Inserts Cell Details into Database
     *
     * @return row id or -1 if error
     */
    public long insertCell(Cell cell) {

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
                return mDb.update(CELL_TABLE, cellValues,
                        "CellID=?",
                        new String[]{Integer.toString(cell.getCID())});
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
    long insertOpenCell(double latitude, double longitude,
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
            locationValues.put("Lac", lac);
            locationValues.put("CellID", cellID);
            locationValues.put("Net", netType);
            locationValues.put("Lat", latitude);
            locationValues.put("Lng", longitude);
            locationValues.put("Signal", signalInfo);
            locationValues.put("Connection", cellInfo);

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
     * @param cellId
     * @return
     */
    public int deleteCell(int cellId) {
        return mDb.delete(CELL_TABLE, "CellID = ?", new String[]{ String.valueOf(cellId) });
    }

    /**
     * Returns Silent Sms database contents
     */
    public Cursor getSilentSmsData() {
        return mDb.query(SILENT_SMS_TABLE, new String[]{"Address", "Display", "Class", "ServiceCtr",
                        "Message", "Timestamp"},
                null, null, null, null, COLUMN_ID + " DESC"
        );
    }

    /**
     * Returns Cell Information database contents
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
     * Returns Location Information database contents
     */
    public Cursor getLocationData() {
        return mDb.query(LOCATION_TABLE, new String[]{"CellID", "Lac", "Net", "Lat", "Lng",
                        "Signal"},
                null, null, null, null, null
        );
    }

    /**
     * Returns OpenCellID database contents
     */
    public Cursor getOpenCellIDData() {
        return mDb.query(OPENCELLID_TABLE, new String[]{"CellID", "Lac", "Mcc", "Mnc", "Lat", "Lng",
                        "AvgSigStr", "Samples"},
                null, null, null, null, null
        );
    }

    /**
     * Returns Default MCC Locations database contents
     */
    public Cursor getDefaultMccLocationData() {
        return mDb.query(DEFAULT_MCC_TABLE, new String[]{"Country", "Mcc", "Lat", "Lng"},
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
        cursor.close();

        return exists;
    }

    /**
     * Checks to see if Cell already exists in OpenCellID database
     */
    boolean openCellExists(int cellID) {
        Cursor cursor = mDb.rawQuery("SELECT * FROM " + OPENCELLID_TABLE + " WHERE CellID = " +
                cellID, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();

        return exists;
    }

    public boolean checkLAC(Cell cell) {
        Cursor cursor = mDb.query(CELL_TABLE, new String[]{"Lac"}, "CellID=" + cell.getCID(),
                null,null,null,null);

        while (cursor.moveToNext()) {
            if (cell.getLAC() != cursor.getInt(0)) {
                Log.i(TAG, "CHANGING LAC!!! - Current LAC: " + cell.getLAC() +
                        " Database LAC: " + cursor.getInt(0));
                cursor.close();
                return false;
            }
        }

        cursor.close();

        return true;
    }

    /**
     * Updates Cell records to indicate OpenCellID contribution has been made
     */
    public void ocidProcessed() {
        ContentValues ocidValues = new ContentValues();
        ocidValues.put("OCID_SUBMITTED", 1);

        mDb.update(CELL_TABLE, ocidValues, "OCID_SUBMITTED<>?",
                new String[]{"1"});
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

        cursor.close();

        return loc;
    }

    public void cleanseCellTable() {
        mDb.execSQL("DELETE FROM " + CELL_TABLE + " WHERE " + COLUMN_ID + " NOT IN (SELECT MAX(" + COLUMN_ID + ") FROM " + CELL_TABLE + " GROUP BY CellID)");
        mDb.execSQL("DELETE FROM " + CELL_TABLE + " WHERE CellID = " + Integer.MAX_VALUE + " OR CellID = -1");
    }

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
            result = file.createNewFile();
            if (!result) {
                return false;
            }
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            open();
            Cursor c = getOPCIDSubmitData();

            csvWrite.writeNext(
                    "mcc,mnc,lac,cellid,lon,lat,signal,measured_at,rating,speed,direction,act");
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
            Log.e(TAG, "Error creating OpenCellID Upload Data " + e);
            return false;
        } finally {
            AIMSICD.mProgressBar.setProgress(0);
        }
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

            for (int i = 1; i < csvMcc.size(); i++) {
                defaultMccValues.put("Country", csvMcc.get(i)[0]);
                defaultMccValues.put("Mcc", csvMcc.get(i)[1]);
                defaultMccValues.put("Lng", csvMcc.get(i)[2]);
                defaultMccValues.put("Lat", csvMcc.get(i)[3]);

                db.insert(DEFAULT_MCC_TABLE, null, defaultMccValues);
            }


        } catch (Exception e) {
            Log.e(TAG, "Error populating Default Mcc Data - " + e);
        }
    }

    /**
     * Parses the downloaded CSV from OpenCellID and adds Map Marker to identify known
     * Cell ID's
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
                        //Insert details into OpenCellID Database
                        insertOpenCell(Double.parseDouble(csvCellID.get(i)[0]),
                                Double.parseDouble(csvCellID.get(i)[1]),
                                Integer.parseInt(csvCellID.get(i)[2]),
                                Integer.parseInt(csvCellID.get(i)[3]),
                                Integer.parseInt(csvCellID.get(i)[4]),
                                Integer.parseInt(csvCellID.get(i)[5]),
                                Integer.parseInt(csvCellID.get(i)[6]),
                                Integer.parseInt(csvCellID.get(i)[7]));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing OpenCellID data - " + e.getMessage());
            return false;
        } finally {
            AIMSICD.mProgressBar.setProgress(0);
        }
    }

    /**
     * Imports CSV file export data into the database
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
                                case CELL_TABLE:
                                    insertCell(Integer.parseInt(records.get(i)[1]),
                                            Integer.parseInt(records.get(i)[2]),
                                            Integer.parseInt(records.get(i)[3]),
                                            Double.parseDouble(records.get(i)[4]),
                                            Double.parseDouble(records.get(i)[5]),
                                            Integer.parseInt(records.get(i)[6]),
                                            Integer.valueOf(records.get(i)[7]),
                                            Integer.valueOf(records.get(i)[8]),
                                            Double.valueOf(records.get(i)[9]),
                                            Double.valueOf(records.get(i)[10]),
                                            Double.valueOf(records.get(i)[11]),
                                            String.valueOf(records.get(i)[10]),
                                            Long.valueOf(records.get(i)[11]));
                                    break;
                                case LOCATION_TABLE:
                                    insertLocation(Integer.parseInt(records.get(i)[1]),
                                            Integer.parseInt(records.get(i)[2]),
                                            Integer.parseInt(records.get(i)[3]),
                                            Double.parseDouble(records.get(i)[4]),
                                            Double.parseDouble(records.get(i)[5]),
                                            Integer.parseInt(records.get(i)[6]),
                                            String.valueOf(records.get(i)[7]));
                                    break;
                                case OPENCELLID_TABLE:
                                    insertOpenCell(Double.parseDouble(records.get(i)[1]),
                                            Double.parseDouble(records.get(i)[2]),
                                            Integer.parseInt(records.get(i)[3]),
                                            Integer.parseInt(records.get(i)[4]),
                                            Integer.parseInt(records.get(i)[5]),
                                            Integer.parseInt(records.get(i)[6]),
                                            Integer.parseInt(records.get(i)[7]),
                                            Integer.parseInt(records.get(i)[8]));
                                    break;
                                case SILENT_SMS_TABLE:
                                    Bundle bundle = new Bundle();
                                    bundle.putString("address", String.valueOf(records.get(i)[1]));
                                    bundle.putString("display_address",
                                            String.valueOf(records.get(i)[2]));
                                    bundle.putString("message_class",
                                            String.valueOf(records.get(i)[3]));
                                    bundle.putString("service_centre",
                                            String.valueOf(records.get(i)[4]));
                                    bundle.putString("message", String.valueOf(records.get(i)[5]));
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
     * Backup the database tables to CSV files
     *
     * @return boolean indicating backup outcome
     */
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
    private void backup(String tableName) {
        Log.i(TAG, "Database Backup: " + DB_NAME);

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
            Log.e(TAG, "Error exporting table " + tableName + " " + e);
        } finally {
            AIMSICD.mProgressBar.setProgress(0);
        }

        Log.i(TAG, "exporting database complete");
    }

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
        Cursor c = mDb.rawQuery("SELECT COUNT(cellID) FROM " + CELL_SIGNAL_TABLE +" WHERE cellID="+cellID, new String[0]);
        c.moveToFirst();
        return c.getInt(0);
    }

    public int getAverageSignalStrength(int cellID) {
        Cursor c = mDb.rawQuery("SELECT AVG(signal) FROM " + CELL_SIGNAL_TABLE +" WHERE cellID="+cellID, new String[0]);
        c.moveToFirst();
        return c.getInt(0);
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

            /*
             * Cell Signal Measurements
             */
            database.execSQL("create table " + CELL_SIGNAL_TABLE + " (" + COLUMN_ID + " integer primary key autoincrement, cellID INTEGER, signal INTEGER, timestamp INTEGER);");
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
              */
            String LOC_DATABASE_CREATE = "create table " +
                    LOCATION_TABLE + " (" + COLUMN_ID +
                    " integer primary key autoincrement, Lac INTEGER, CellID INTEGER, " +
                    "Net VARCHAR, Lat VARCHAR, Lng VARCHAR, Signal INTEGER, Connection VARCHAR, " +
                    "Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);";
            database.execSQL(LOC_DATABASE_CREATE);

            /*
             * Cell Information Tracking Database
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

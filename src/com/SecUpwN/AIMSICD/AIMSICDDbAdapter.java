package com.SecUpwN.AIMSICD;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class AIMSICDDbAdapter {

    private final String TAG = "AISMICD_DbAdaptor";

    private DbHelper mDbHelper;
    private SQLiteDatabase mDb;
    private static final int DATABASE_VERSION = 1;
    public static final String COLUMN_ID = "_id";
    public String LOCATION_TABLE = "locationinfo";
    public String CELL_TABLE = "cellinfo";
    public String SIGNAL_TABLE = "signalinfo";
    public String DB_NAME = "myCellInfo";
    private final Context mContext;

    public AIMSICDDbAdapter(Context context) {
        mContext = context;
        mDbHelper = new DbHelper(mContext);
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
        return mDb.rawQuery("SELECT Net, Lat, Lng, Signal FROM "
                + SIGNAL_TABLE, null);
    }

    public void eraseLocationData() {
        mDb.delete(LOCATION_TABLE, null, null);
    }


    /**
     * Checks to see if Cell already exists in database
     */
    public boolean cellExists(int cellID) {
        Cursor cursor = mDb.rawQuery("SELECT * FROM " + CELL_TABLE + " WHERE CellID = " +
                cellID, null);

        return cursor.getCount()>0;
    }

    public void exportDB () {
        try {
            export(LOCATION_TABLE);
            export(CELL_TABLE);
            export(SIGNAL_TABLE);
        } catch (IOException ioe) {
            Log.e (TAG, "exportDB() " + ioe.getMessage());
        }
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
        Cursor c = mDb.rawQuery(sql, new String[0]);
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

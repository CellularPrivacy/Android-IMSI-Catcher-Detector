package com.SecUpwN.AIMSICD.smsdetection;
/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vJaf6 | TERMS:  http://git.io/vJMf5
 * -----------------------------------------------------------
 */

/*
*
@author Copyright Paul Kinsella paulkinsella29@yahoo.ie

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    */
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Paul Kinsella on 03/03/15.
 *
 * All databaes are created here
 */
public class SmsDetectionDbHelper extends SQLiteOpenHelper {
    private static final String LOGTAG = "TYPE0_DETECTION";

    private static final String DATABASE_NAME = "pk_sms_detection.db";
    private static final int DATABASE_VERSION = 1;

    //TABLE FOR SILENT SMS DATA
    public static final String SMS_DATA_TABLE_NAME = "sms_data";
    public static final String SMS_DATA_ID = "id";
    public static final String SMS_DATA_SENDER_NUMBER = "sender_number";
    public static final String SMS_DATA_SENDER_MSG = "sender_msg";
    public static final String SMS_DATA_TIMESTAMP = "sms_timestamp";
    public static final String SMS_DATA_SMS_TYPE = "sms_type";
    public static final String SMS_DATA_CURRENT_CID = "current_cid";
    public static final String SMS_DATA_CURRENT_LAC = "current_lac";
    public static final String SMS_DATA_CURRENT_NETTYPE = "current_net_type";
    public static final String SMS_DATA_CURRENT_ROAM_STATE = "current_roam_state";
    public static final String SMS_DATA_CURRENT_GPS_LAT = "current_gps_lat";
    public static final String SMS_DATA_CURRENT_GPS_LON = "current_gps_lon";

    public static final String SILENT_SMS_ID   = "_id";
    public static final String SILENT_SMS_STRINGS_TABLE   = "silentsmsstrings";
    public static final String SILENT_SMS_STRING_COLUMN   = "silent_sms_str";//IsTypeZero=True etc
    public static final String SILENT_SMS_TYPE_COLUMN   = "silent_sms_type";//type0 etc...


    String[] allSmsColumns = {//SILENT SMS DATA COLUMNS
            SmsDetectionDbHelper.SMS_DATA_ID,
            SmsDetectionDbHelper.SMS_DATA_SENDER_NUMBER,
            SmsDetectionDbHelper.SMS_DATA_SENDER_MSG,
            SmsDetectionDbHelper.SMS_DATA_TIMESTAMP,
            SmsDetectionDbHelper.SMS_DATA_SMS_TYPE,
            SmsDetectionDbHelper.SMS_DATA_CURRENT_CID,
            SmsDetectionDbHelper.SMS_DATA_CURRENT_LAC,
            SmsDetectionDbHelper.SMS_DATA_CURRENT_NETTYPE,
            SmsDetectionDbHelper.SMS_DATA_CURRENT_ROAM_STATE,
            SmsDetectionDbHelper.SMS_DATA_CURRENT_GPS_LAT,
            SmsDetectionDbHelper.SMS_DATA_CURRENT_GPS_LON
    };

    /**
     * Table:         SILENT_SMS_TABLE
     * What:          Unique logcat strings for detection of sms
     * Columns:       _id,silent_sms_str,silent_sms_type
     * Coder Paul Kinsella paulkinsella29@yahoo.ie
     */
    String SILENT_SMS_STRINGS_TABLE_CREATE = "CREATE TABLE " +
            SILENT_SMS_STRINGS_TABLE + " (" + SILENT_SMS_ID +
            " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "silent_sms_str TEXT, " +
            "silent_sms_type TEXT);";


    String CREATE_SMS_DATA_TB = String.format(
            "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s INTEGER, %s INTEGER,%s TEXT, %s TEXT,%s DOUBLE, %s DOUBLE )",
            SMS_DATA_TABLE_NAME,//TABLE NAME
            SMS_DATA_ID,
            SMS_DATA_SENDER_NUMBER,
            SMS_DATA_SENDER_MSG,
            SMS_DATA_TIMESTAMP,
            SMS_DATA_SMS_TYPE,//type 0 etc
            SMS_DATA_CURRENT_LAC,
            SMS_DATA_CURRENT_CID,
            SMS_DATA_CURRENT_NETTYPE,
            SMS_DATA_CURRENT_ROAM_STATE,
            SMS_DATA_CURRENT_GPS_LAT,
            SMS_DATA_CURRENT_GPS_LON);



    public SmsDetectionDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SILENT_SMS_STRINGS_TABLE_CREATE);
        db.execSQL(CREATE_SMS_DATA_TB);

        //Preparing strings for database here
        String SILENT_ONLY_TAGS[] = {
                "Received short message type 0, Don't display or store it. Send Ack#TYPE0",//Type0 Samsung
                "Received voice mail indicator clear SMS shouldStore=false#SILENTVOICE",//this msg is in a few phones
                "SMS TP-PID:0 data coding scheme: 24#FLASH",//Flash
                "isTypeZero=true#TYPE0",
                "incoming msg. Mti 0 ProtocolID 0 DCS 0x04 class -1#WAPPUSH"
        };
        //Inserting strings to DB here
        for (int x = 0; x < SILENT_ONLY_TAGS.length; x++) {
            ContentValues sms_detection_string = new ContentValues();
            sms_detection_string.put(SILENT_SMS_STRING_COLUMN, SILENT_ONLY_TAGS[x].split("#")[0]);
            sms_detection_string.put(SILENT_SMS_TYPE_COLUMN, SILENT_ONLY_TAGS[x].split("#")[1]);
            db.insert(SILENT_SMS_STRINGS_TABLE, null, sms_detection_string);
        }

        ContentValues values = new ContentValues();

        values.put(SmsDetectionDbHelper.SMS_DATA_SENDER_NUMBER, "1234567890");
        values.put(SmsDetectionDbHelper.SMS_DATA_SENDER_MSG, "First install test sms");
        values.put(SmsDetectionDbHelper.SMS_DATA_TIMESTAMP, "20150404113312");
        values.put(SmsDetectionDbHelper.SMS_DATA_SMS_TYPE, "TYPE0");
        values.put(SmsDetectionDbHelper.SMS_DATA_CURRENT_LAC, "1234");
        values.put(SmsDetectionDbHelper.SMS_DATA_CURRENT_CID, "4321");
        values.put(SmsDetectionDbHelper.SMS_DATA_CURRENT_NETTYPE, "GSM");
        values.put(SmsDetectionDbHelper.SMS_DATA_CURRENT_ROAM_STATE, "false");
        values.put(SmsDetectionDbHelper.SMS_DATA_CURRENT_GPS_LAT, 51.00000);
        values.put(SmsDetectionDbHelper.SMS_DATA_CURRENT_GPS_LON, -8.00000);

        db.insert(SmsDetectionDbHelper.SMS_DATA_TABLE_NAME,null,values);
        Log.i(LOGTAG, "onCreate >> Database has been created");
        
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+SILENT_SMS_STRINGS_TABLE_CREATE);
        db.execSQL("DROP TABLE IF EXISTS "+SMS_DATA_TABLE_NAME);
        onCreate(db);

        Log.i(LOGTAG, "onUpgrade >> Database has been created");
    }


}

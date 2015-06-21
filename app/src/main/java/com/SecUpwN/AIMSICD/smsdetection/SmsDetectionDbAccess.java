/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */

/* Coded by Paul Kinsella <paulkinsella29@yahoo.ie> */

package com.SecUpwN.AIMSICD.smsdetection;
    
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Paul Kinsella paulkinsella29@yahoo.ie on 03/03/15.
 *
 * This class allows users to access the database
 */
public class SmsDetectionDbAccess {
    SQLiteOpenHelper dbhelper;
    SQLiteDatabase dectection_db;
    private static final String LOGTAG = "SmsDetectionDbAccess";
    static Context mContext;
    public SmsDetectionDbAccess(Context context){
        mContext = context;
        dbhelper = new SmsDetectionDbHelper(context);
        dectection_db = dbhelper.getWritableDatabase();

    }

    public void open(){
        dectection_db = dbhelper.getWritableDatabase();
        Log.i(LOGTAG, "database opened");
    }

    public void close(){
        dbhelper.close();
        Log.i(LOGTAG, "database closed");
    }
    //====================================================================
    //      Insert new detection strings into database
    //====================================================================
    /*
        When inserting strings it has to be in the format
        i am a type 0 string  <-----your string can be found in locat
        TYPE0 SILENTVOICE FLASH <--- These have to be in CAPS
        ContentValues newconvalues = new ContentValues();
        newconvalues.put(SILENT_SMS_STRING_COLUMN, "your string goes here");
        newconvalues.put(SILENT_SMS_TYPE_COLUMN, "TYPE0");
        database.insert(SILENT_SMS_STRINGS_TABLE,null,newconvalues);

     */
    public boolean insertNewDetectionString(ContentValues newstring) {
        // First check that string not in DB

        String check4String = String.format("SELECT * FROM %s WHERE %s = \"%s\"",
                SmsDetectionDbHelper.SILENT_SMS_STRINGS_TABLE,
                SmsDetectionDbHelper.SILENT_SMS_STRING_COLUMN, newstring.get(SmsDetectionDbHelper.SILENT_SMS_STRING_COLUMN));
        Cursor stringCountCursor = dectection_db.rawQuery(check4String, null);

        int stringCount = stringCountCursor.getCount();

        stringCountCursor.close();

        if (stringCount > 0) {
            Log.i(LOGTAG, "Detection String already in Database");
        } else {

            try {
                dectection_db.insert(SmsDetectionDbHelper.SILENT_SMS_STRINGS_TABLE, null, newstring);
                Log.i(LOGTAG, "Detection String Added");
                return true;
            } catch (Exception ee) {
                Log.i(LOGTAG, "Detection String failed");
            }
        }
        return false;
    }

    public boolean deleteDetectionString(String deleteme) {

        try {
            dectection_db.delete(SmsDetectionDbHelper.SILENT_SMS_STRINGS_TABLE, SmsDetectionDbHelper.SILENT_SMS_STRING_COLUMN + "='" + deleteme + "'",null);
            return true;
        }catch (Exception ee){Log.i(LOGTAG, "Delete String failed");}

        return false;



    }

    public boolean deleteDetectedSms(long deleteme) {
        // First check that string not in DB
        try {
            dectection_db.delete(SmsDetectionDbHelper.SMS_DATA_TABLE_NAME, SmsDetectionDbHelper.SMS_DATA_ID + "=" + deleteme,null);
            return true;
        }catch (Exception ee){Log.i(LOGTAG, "Sms Deleted failed");}

        return false;
    }


    // ====================================================================
    //      Get all detection strings
    // ====================================================================
    public ArrayList<AdvanceUserItems> getDetectionStrings() {
        Cursor stringCountCursor = dectection_db.rawQuery("SELECT * FROM "+ SmsDetectionDbHelper.SILENT_SMS_STRINGS_TABLE, null);

        ArrayList<AdvanceUserItems> detection_strs = new ArrayList<>();
        System.out.println("DB LEN = " + stringCountCursor.getCount());
        if (stringCountCursor.getCount() > 0) {
            while (stringCountCursor.moveToNext()) {
                AdvanceUserItems setitems = new AdvanceUserItems();
                setitems.setDetection_string(stringCountCursor.getString(stringCountCursor.getColumnIndex(SmsDetectionDbHelper.SILENT_SMS_STRING_COLUMN)));
                setitems.setDetection_type(stringCountCursor.getString(stringCountCursor.getColumnIndex(SmsDetectionDbHelper.SILENT_SMS_TYPE_COLUMN)));
                detection_strs.add(setitems);

            }
        } else {
            AdvanceUserItems setitems = new AdvanceUserItems();
            setitems.setDetection_string("No data");
            setitems.setDetection_type("No data");
            detection_strs.add(setitems);
        }

        stringCountCursor.close();

        return  detection_strs;
    }
    public  Cursor getDetectionStringCursor() {
        return dectection_db.query(SmsDetectionDbHelper.SILENT_SMS_STRINGS_TABLE,
                new String[]{SmsDetectionDbHelper.SILENT_SMS_STRING_COLUMN, SmsDetectionDbHelper.SILENT_SMS_TYPE_COLUMN},
                null, null, null, null, null
        );
    }

    public CapturedSmsData storeCapturedSms(CapturedSmsData smsdata){

        ContentValues values = new ContentValues();

        values.put(SmsDetectionDbHelper.SMS_DATA_SENDER_NUMBER, smsdata.getSenderNumber());
        values.put(SmsDetectionDbHelper.SMS_DATA_SENDER_MSG, smsdata.getSenderMsg());
        values.put(SmsDetectionDbHelper.SMS_DATA_TIMESTAMP, smsdata.getSmsTimestamp());
        values.put(SmsDetectionDbHelper.SMS_DATA_SMS_TYPE, smsdata.getSmsType());
        values.put(SmsDetectionDbHelper.SMS_DATA_CURRENT_LAC, smsdata.getCurrent_lac());
        values.put(SmsDetectionDbHelper.SMS_DATA_CURRENT_CID, smsdata.getCurrent_cid());
        values.put(SmsDetectionDbHelper.SMS_DATA_CURRENT_NETTYPE, smsdata.getCurrent_nettype());
        values.put(SmsDetectionDbHelper.SMS_DATA_CURRENT_ROAM_STATE, smsdata.getCurrent_roam_status());
        values.put(SmsDetectionDbHelper.SMS_DATA_CURRENT_GPS_LAT, smsdata.getCurrent_gps_lat());
        values.put(SmsDetectionDbHelper.SMS_DATA_CURRENT_GPS_LON, smsdata.getCurrent_gps_lon());


        long insertid = dectection_db.insert(SmsDetectionDbHelper.SMS_DATA_TABLE_NAME,null,values);
        smsdata.setId(insertid);
        return  smsdata;
    }
    public Cursor returnDetectedSmsData(){
        Cursor getsmsdata_cursor = dectection_db.rawQuery("SELECT * FROM "+ SmsDetectionDbHelper.SMS_DATA_TABLE_NAME,null);
        return  getsmsdata_cursor;

    }
    
    public boolean isTimeStampInDB(String TS){
        String check4timestamp = String.format("SELECT * FROM %s WHERE %s = \"%s\"",
            SmsDetectionDbHelper.SMS_DATA_TABLE_NAME,
            SmsDetectionDbHelper.SMS_DATA_TIMESTAMP,TS);
        Cursor timestampcount = dectection_db.rawQuery(check4timestamp,null);
        if( timestampcount.getCount() > 0){
            return true;
        }else{
            return false;
        }
    }

}

package com.SecUpwN.AIMSICD.smsdetection;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.SecUpwN.AIMSICD.R;

import java.util.ArrayList;

public class AdvancedUserSmsActivity extends Activity {
    private ListView listViewAdv;
    private SmsDetectionDbAccess dbaccess;
    private ArrayList<CapturedSmsData> msgitems;
    private final String TAG = "AdvancedUserSmsActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_sms_user);

        dbaccess = new SmsDetectionDbAccess(getApplicationContext());

        listViewAdv = (ListView) findViewById(R.id.listView_Adv_Sms_Activity);
        msgitems = new ArrayList<>();
        dbaccess.open();

        try {
            Cursor smscur = dbaccess.returnDetectedSmsData();
            if (smscur.getCount() > 0) {
                while (smscur.moveToNext()) {
                    CapturedSmsData getdata = new CapturedSmsData();
                    getdata.setId(smscur.getLong(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_ID)));
                    getdata.setSmsTimestamp(smscur.getString(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_TIMESTAMP)));
                    getdata.setSmsType(smscur.getString(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_SMS_TYPE)));
                    getdata.setSenderNumber(smscur.getString(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_SENDER_NUMBER)));
                    getdata.setSenderMsg(smscur.getString(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_SENDER_MSG)));
                    getdata.setCurrent_lac(smscur.getInt(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_CURRENT_LAC)));
                    getdata.setCurrent_cid(smscur.getInt(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_CURRENT_CID)));
                    getdata.setCurrent_nettype(smscur.getString(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_CURRENT_NETTYPE)));
                    getdata.setCurrent_roam_status(smscur.getString(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_CURRENT_ROAM_STATE)));
                    getdata.setCurrent_gps_lat(smscur.getDouble(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_CURRENT_GPS_LAT)));
                    getdata.setCurrent_gps_lon(smscur.getDouble(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_CURRENT_GPS_LON)));
                    msgitems.add(getdata);
                }
            }

        } catch (Exception ee) {
            Log.e(TAG, "DB ERROR: " + ee.getMessage());
            ee.printStackTrace();
        }

        dbaccess.close();

        listViewAdv.setAdapter(new AdvanceUserBaseSmsAdapter(getApplicationContext(), msgitems));

        listViewAdv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> a, View v, int position, long id) {
                Object o = listViewAdv.getItemAtPosition(position);
                CapturedSmsData obj_itemDetails = (CapturedSmsData) o;

                dbaccess.open();
                if (dbaccess.deleteDetectedSms(obj_itemDetails.getId())) {
                    Toast.makeText(getApplicationContext(), "Deleted Sms Id = \n" + obj_itemDetails.getId(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to Delete", Toast.LENGTH_SHORT).show();
                }
                dbaccess.close();

                try {
                    loadDbString();
                } catch (Exception ee) {
                    Log.e(TAG, ee.getMessage());
                    ee.printStackTrace();
                }

                return false;
            }

        });

    }

    public void loadDbString() {
        ArrayList<CapturedSmsData> newmsglist = new ArrayList<>();

        dbaccess.open();
        try {
            Cursor smscur = dbaccess.returnDetectedSmsData();

            if (smscur.getCount() > 0) {
                while (smscur.moveToNext()) {
                    CapturedSmsData getdata = new CapturedSmsData();
                    getdata.setId(smscur.getLong(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_ID)));
                    getdata.setSmsTimestamp(smscur.getString(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_TIMESTAMP)));
                    getdata.setSmsType(smscur.getString(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_SMS_TYPE)));
                    getdata.setSenderNumber(smscur.getString(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_SENDER_NUMBER)));
                    getdata.setSenderMsg(smscur.getString(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_SENDER_MSG)));
                    getdata.setCurrent_lac(smscur.getInt(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_CURRENT_LAC)));
                    getdata.setCurrent_cid(smscur.getInt(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_CURRENT_CID)));
                    getdata.setCurrent_nettype(smscur.getString(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_CURRENT_NETTYPE)));
                    getdata.setCurrent_roam_status(smscur.getString(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_CURRENT_ROAM_STATE)));
                    getdata.setCurrent_gps_lat(smscur.getDouble(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_CURRENT_GPS_LAT)));
                    getdata.setCurrent_gps_lon(smscur.getDouble(smscur.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_CURRENT_GPS_LON)));
                    newmsglist.add(getdata);
                }
            }
            listViewAdv.setAdapter(new AdvanceUserBaseSmsAdapter(getApplicationContext(), newmsglist));
        } catch (Exception ee) {
            System.out.println("DB ERROR>>>>" + ee.toString());

        }

        dbaccess.close();


    }
}

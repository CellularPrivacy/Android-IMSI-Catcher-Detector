/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */

/* Coded by Paul Kinsella <paulkinsella29@yahoo.ie> */

package com.SecUpwN.AIMSICD.smsdetection;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.constants.DBTableColumnIds;

import java.util.ArrayList;

import io.freefair.android.util.logging.AndroidLogger;
import io.freefair.android.util.logging.Logger;

public class AdvancedUserSmsActivity extends AppCompatActivity {

    //TODO: @Inject
    private final Logger log = AndroidLogger.forClass(AdvancedUserSmsActivity.class);

    ListView listViewAdv;
    AIMSICDDbAdapter dbaccess;
    ArrayList<CapturedSmsData> msgitems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_sms_user);

        dbaccess = new AIMSICDDbAdapter(getApplicationContext());

        listViewAdv = (ListView)findViewById(R.id.listView_Adv_Sms_Activity);
        msgitems = new ArrayList<>();

        try {
            Cursor smscur = dbaccess.returnSmsData();
            if (smscur.getCount() > 0) {
                while (smscur.moveToNext()) {
                    CapturedSmsData getdata = new CapturedSmsData();
                    getdata.setId(smscur.getLong(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_ID)));
                    getdata.setSmsTimestamp(smscur.getString(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_TIMESTAMP)));
                    getdata.setSmsType(smscur.getString(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_SMS_TYPE)));
                    getdata.setSenderNumber(smscur.getString(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_SENDER_NUMBER)));
                    getdata.setSenderMsg(smscur.getString(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_SENDER_MSG)));
                    getdata.setCurrent_lac(smscur.getInt(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_LAC)));
                    getdata.setCurrent_cid(smscur.getInt(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_CID)));
                    getdata.setCurrent_nettype(smscur.getString(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_RAT)));
                    getdata.setCurrent_roam_status(smscur.getInt(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_ROAM_STATE)));
                    getdata.setCurrent_gps_lat(smscur.getDouble(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_GPS_LAT)));
                    getdata.setCurrent_gps_lon(smscur.getDouble(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_GPS_LON)));
                    msgitems.add(getdata);
                }
            }
            smscur.close();

        } catch (Exception ee) {
            log.error("DB ERROR", ee);
        }



        listViewAdv.setAdapter(new AdvanceUserBaseSmsAdapter(getApplicationContext(),msgitems));

        listViewAdv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> a, View v, int position, long id) {
                Object o = listViewAdv.getItemAtPosition(position);
                CapturedSmsData obj_itemDetails = (CapturedSmsData) o;

                if(dbaccess.deleteDetectedSms(obj_itemDetails.getId())){
                    Toast.makeText(getApplicationContext(),"Deleted Sms Id = \n"+obj_itemDetails.getId(),Toast.LENGTH_SHORT).show();
                }else {Toast.makeText(getApplicationContext(),"Failed to Delete",Toast.LENGTH_SHORT).show();}

                try{
                    loadDbString();
                }catch (Exception ee){}
                return false;
            }

        });

    }
    public void loadDbString(){
        ArrayList<CapturedSmsData> newmsglist = new ArrayList<>();

        try {
            Cursor smscur = dbaccess.returnSmsData();

            if (smscur.getCount() > 0) {
                while (smscur.moveToNext()) {
                    CapturedSmsData getdata = new CapturedSmsData();
                    getdata.setId(smscur.getLong(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_ID)));
                    getdata.setSmsTimestamp(smscur.getString(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_TIMESTAMP)));
                    getdata.setSmsType(smscur.getString(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_SMS_TYPE)));
                    getdata.setSenderNumber(smscur.getString(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_SENDER_NUMBER)));
                    getdata.setSenderMsg(smscur.getString(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_SENDER_MSG)));
                    getdata.setCurrent_lac(smscur.getInt(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_LAC)));
                    getdata.setCurrent_cid(smscur.getInt(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_CID)));
                    getdata.setCurrent_nettype(smscur.getString(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_RAT)));
                    getdata.setCurrent_roam_status(smscur.getInt(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_ROAM_STATE)));
                    getdata.setCurrent_gps_lat(smscur.getDouble(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_GPS_LAT)));
                    getdata.setCurrent_gps_lon(smscur.getDouble(smscur.getColumnIndex(DBTableColumnIds.SMS_DATA_GPS_LON)));
                    newmsglist.add(getdata);
                }
            }

            smscur.close();
            listViewAdv.setAdapter(new AdvanceUserBaseSmsAdapter(getApplicationContext(), newmsglist));
        } catch (Exception ee) {
            log.error("DB ERROR", ee);
        }


    }
}

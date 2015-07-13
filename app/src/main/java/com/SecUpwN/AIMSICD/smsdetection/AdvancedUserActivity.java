/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */

/* Coded by Paul Kinsella <paulkinsella29@yahoo.ie> */

package com.SecUpwN.AIMSICD.smsdetection;

import android.app.Activity;
import android.content.ContentValues;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.constants.DBTableColumnIds;

import java.util.ArrayList;

public class AdvancedUserActivity extends Activity {
    final static String TAG = "AdvancedUserActivity";
    ListView listViewAdv;
    AIMSICDDbAdapter dbaccess;
    Button btn_insert;
    EditText edit_adv_user_det;
    Spinner myspinner;
    ArrayList<AdvanceUserItems> msgitems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_user);

        btn_insert = (Button)findViewById(R.id.btn_insert);
        edit_adv_user_det = (EditText)findViewById(R.id.edit_adv_user_string);
        myspinner = (Spinner)findViewById(R.id.spinner);

        dbaccess = new AIMSICDDbAdapter(getApplicationContext());//SmsDetectionDbAccess(getApplicationContext());

        listViewAdv = (ListView)findViewById(R.id.listView_Adv_Activity);


        try {
            msgitems = dbaccess.getDetectionStrings();

        }catch (Exception ee){
            System.out.println("DB ERROR>>>>"+ee.toString());
            msgitems = new ArrayList<>();
            AdvanceUserItems itemss = new AdvanceUserItems();
            itemss.setDetection_string("NO DATA");
            itemss.setDetection_type("No TYPE");
            msgitems.add(itemss);

        }


        listViewAdv.setAdapter(new AdvanceUserBaseAdapter(getApplicationContext(),msgitems));

        listViewAdv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> a, View v, int position, long id) {
                Object o = listViewAdv.getItemAtPosition(position);
                AdvanceUserItems obj_itemDetails = (AdvanceUserItems) o;

                if(dbaccess.deleteDetectionString(obj_itemDetails.getDetection_string())){
                    Toast.makeText(getApplicationContext(),"Deleted String\n"+obj_itemDetails.getDetection_string(),Toast.LENGTH_SHORT).show();
                }else {Toast.makeText(getApplicationContext(),"Failed to Delete",Toast.LENGTH_SHORT).show();}

                try{
                    loadDbString();
                }catch (Exception ee){}
                return false;
            }
        });


        btn_insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (edit_adv_user_det.getText().toString().contains("\"")) {
                    Toast.makeText(getApplicationContext(), "String not added\n \" double quote will cause db error ", Toast.LENGTH_SHORT).show();
                } else {
                    ContentValues store_new_sms_string = new ContentValues();
                    store_new_sms_string.put(DBTableColumnIds.DETECTION_STRINGS_LOGCAT_STRING, edit_adv_user_det.getText().toString());//.replace("\"","")
                    store_new_sms_string.put(DBTableColumnIds.DETECTION_STRINGS_SMS_TYPE, myspinner.getSelectedItem().toString());//.replace("\"","")

                    if (dbaccess.insertNewDetectionString(store_new_sms_string)) {
                        Toast.makeText(getApplicationContext(), "String Added to DB", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "String Failed to add", Toast.LENGTH_SHORT).show();
                    }

                    try {
                        loadDbString();
                    } catch (Exception ee) {
                    }
                }
            }

        });
    }

    /* Reload ListView with new database values  */
    public void loadDbString(){
        ArrayList<AdvanceUserItems> newmsglist;
        try {
        /* There should be at least 1 detection string in db so not to cause an error */
            newmsglist = dbaccess.getDetectionStrings();
            listViewAdv.setAdapter(new AdvanceUserBaseAdapter(getApplicationContext(),newmsglist));
        }catch (Exception ee){
            Log.e(TAG,"<AdvanceUserItems>\n"+ee.toString() );
        }

    }
}

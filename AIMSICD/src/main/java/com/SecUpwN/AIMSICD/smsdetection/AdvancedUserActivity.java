/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */

/* Coded by Paul Kinsella <paulkinsella29@yahoo.ie> */

package com.SecUpwN.AIMSICD.smsdetection;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import io.freefair.android.util.logging.AndroidLogger;
import io.freefair.android.util.logging.Logger;

public class AdvancedUserActivity extends AppCompatActivity {

    //TODO: @Inject
    private final Logger log = AndroidLogger.forClass(AdvancedUserActivity.class);

    private ListView listViewAdv;
    private AIMSICDDbAdapter dbAccess;
    private Button insertButton;
    private EditText editAdvUserDet;
    private Spinner spinner;
    private ArrayList<AdvanceUserItems> msgItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_user);
        dbAccess = new AIMSICDDbAdapter(getApplicationContext());

        insertButton = (Button) findViewById(R.id.btn_insert);
        editAdvUserDet = (EditText) findViewById(R.id.edit_adv_user_string);
        spinner = (Spinner) findViewById(R.id.spinner);
        listViewAdv = (ListView) findViewById(R.id.listView_Adv_Activity);

        try {
            msgItems = dbAccess.getDetectionStrings();
        } catch (Exception ee) {
            log.error("Database error", ee);
            msgItems = new ArrayList<>();
            AdvanceUserItems advUserItems = new AdvanceUserItems();
            advUserItems.setDetection_string("NO DATA");
            advUserItems.setDetection_type("No TYPE");
            msgItems.add(advUserItems);
        }

        listViewAdv.setAdapter(new AdvanceUserBaseAdapter(getApplicationContext(), msgItems));

        listViewAdv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> a, View v, int position, long id) {
                Object o = listViewAdv.getItemAtPosition(position);
                AdvanceUserItems itemDetails = (AdvanceUserItems) o;

                String itemDetail = itemDetails.getDetection_string();

                if(dbAccess.deleteDetectionString(itemDetails.getDetection_string())) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.deleted) + ": " + itemDetail, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_to_delete)
                            + " " + itemDetail, Toast.LENGTH_SHORT).show();
                }

                try {
                    loadDbString();
                } catch (Exception ee){
                    log.debug("Error loading db string", ee);
                }
                return false;
            }
        });


        insertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (editAdvUserDet.getText().toString().contains("\"")) {
                    Toast.makeText(getApplicationContext(), R.string.double_quote_will_cause_db_error,
                            Toast.LENGTH_SHORT).show();
                } else {
                    ContentValues store_new_sms_string = new ContentValues();
                    store_new_sms_string.put(DBTableColumnIds.DETECTION_STRINGS_LOGCAT_STRING,
                            editAdvUserDet.getText().toString());

                    store_new_sms_string.put(DBTableColumnIds.DETECTION_STRINGS_SMS_TYPE,
                            spinner.getSelectedItem().toString());

                    if (dbAccess.insertNewDetectionString(store_new_sms_string)) {
                        Toast.makeText(getApplicationContext(), R.string.the_string_was_added_to_db,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.failed_to_add_the_string_to_db,
                                Toast.LENGTH_SHORT).show();
                    }

                    try {
                        loadDbString();
                    } catch (Exception ee) {
                        log.error(ee.getMessage(), ee);
                    }
                }
            }
        });
    }

    /**
     Reload ListView with new database values
     */
    public void loadDbString(){
        ArrayList<AdvanceUserItems> newmsglist;
        try {
        /* There should be at least 1 detection string in db so not to cause an error */
            newmsglist = dbAccess.getDetectionStrings();
            listViewAdv.setAdapter(new AdvanceUserBaseAdapter(getApplicationContext(), newmsglist));
        } catch (Exception ee) {
            log.error("<AdvanceUserItems>", ee);
        }
    }
}

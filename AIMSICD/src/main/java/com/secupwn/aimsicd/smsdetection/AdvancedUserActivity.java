/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */

/* Coded by Paul Kinsella <paulkinsella29@yahoo.ie> */

package com.secupwn.aimsicd.smsdetection;

import android.content.ContentValues;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.adapters.AIMSICDDbAdapter;
import com.secupwn.aimsicd.constants.DBTableColumnIds;

import java.util.ArrayList;
import java.util.List;

import io.freefair.android.injection.annotation.Inject;
import io.freefair.android.injection.annotation.InjectView;
import io.freefair.android.injection.annotation.XmlLayout;
import io.freefair.android.injection.app.InjectionAppCompatActivity;
import io.freefair.android.util.logging.Logger;

@XmlLayout(R.layout.activity_advanced_user)
public class AdvancedUserActivity extends InjectionAppCompatActivity {

    @Inject
    private Logger log;

    @InjectView(R.id.listView_Adv_Activity)
    private ListView listViewAdv;
    private AIMSICDDbAdapter dbAccess;

    @InjectView(R.id.btn_insert)
    private Button insertButton;

    @InjectView(R.id.edit_adv_user_string)
    private EditText editAdvUserDet;

    @InjectView(R.id.spinner)
    private Spinner spinner;
    private List<AdvanceUserItems> msgItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbAccess = new AIMSICDDbAdapter(getApplicationContext());

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

        listViewAdv.setOnItemLongClickListener((adapterView, v, position, id) -> {
            Object o = listViewAdv.getItemAtPosition(position);
            AdvanceUserItems itemDetails = (AdvanceUserItems) o;

            String itemDetail = itemDetails.getDetection_string();

            if (dbAccess.deleteDetectionString(itemDetails.getDetection_string())) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.deleted) + ": " + itemDetail, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.failed_to_delete)
                        + " " + itemDetail, Toast.LENGTH_SHORT).show();
            }

            try {
                loadDbString();
            } catch (Exception ee) {
                log.debug("Error loading db string", ee);
            }
            return false;
        });


        insertButton.setOnClickListener(view -> {

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
        });
    }

    /**
     * Reload ListView with new database values
     */
    public void loadDbString() {
        List<AdvanceUserItems> newmsglist;
        try {
        /* There should be at least 1 detection string in db so not to cause an error */
            newmsglist = dbAccess.getDetectionStrings();
            listViewAdv.setAdapter(new AdvanceUserBaseAdapter(getApplicationContext(), newmsglist));
        } catch (Exception ee) {
            log.error("<AdvanceUserItems>", ee);
        }
    }
}

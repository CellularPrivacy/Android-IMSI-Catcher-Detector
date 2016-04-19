/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */

/* Coded by Paul Kinsella <paulkinsella29@yahoo.ie> */

package com.secupwn.aimsicd.smsdetection;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.data.adapter.DetectionStringAdapter;
import com.secupwn.aimsicd.data.model.SmsDetectionString;

import io.freefair.android.injection.annotation.Inject;
import io.freefair.android.injection.annotation.InjectView;
import io.freefair.android.injection.annotation.XmlLayout;
import io.freefair.android.injection.app.InjectionAppCompatActivity;
import io.freefair.android.util.logging.Logger;
import io.realm.Realm;
import io.realm.RealmResults;

@XmlLayout(R.layout.activity_advanced_user)
public class AdvancedUserActivity extends InjectionAppCompatActivity {

    @Inject
    private Logger log;

    @InjectView(R.id.listView_Adv_Activity)
    private ListView listViewAdv;

    @InjectView(R.id.btn_insert)
    private Button insertButton;

    @InjectView(R.id.edit_adv_user_string)
    private EditText editAdvUserDet;

    @InjectView(R.id.spinner)
    private Spinner spinner;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();

        RealmResults<SmsDetectionString> msgItems = realm.allObjects(SmsDetectionString.class);

        listViewAdv.setAdapter(new DetectionStringAdapter(this, msgItems, true));

        listViewAdv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> a, View v, int position, long id) {
                final SmsDetectionString detectionString = (SmsDetectionString) listViewAdv.getItemAtPosition(position);

                String string = detectionString.getDetectionString();

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        detectionString.removeFromRealm();
                    }
                });

                Toast.makeText(getApplicationContext(),
                        getString(R.string.deleted) + ": " + string, Toast.LENGTH_SHORT).show();

                return true;
            }
        });


        insertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (editAdvUserDet.getText().toString().contains("\"")) {
                    Toast.makeText(AdvancedUserActivity.this, R.string.double_quote_will_cause_db_error,
                            Toast.LENGTH_SHORT).show();
                } else {

                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            SmsDetectionString detectionString = realm.createObject(SmsDetectionString.class);

                            detectionString.setDetectionString(editAdvUserDet.getText().toString());
                            detectionString.setSmsType(spinner.getSelectedItem().toString());

                            Toast.makeText(AdvancedUserActivity.this, R.string.the_string_was_added_to_db,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}

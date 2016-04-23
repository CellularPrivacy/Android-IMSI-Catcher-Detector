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
import android.widget.ListView;
import android.widget.Toast;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.data.adapter.SmsDataAdapter;
import com.secupwn.aimsicd.data.model.SmsData;
import com.secupwn.aimsicd.utils.RealmHelper;

import io.freefair.android.injection.annotation.Inject;
import io.freefair.android.injection.annotation.InjectView;
import io.freefair.android.injection.annotation.XmlLayout;
import io.freefair.android.injection.app.InjectionAppCompatActivity;
import io.freefair.android.util.logging.AndroidLogger;
import io.freefair.android.util.logging.Logger;
import io.realm.Realm;
import io.realm.RealmResults;

import static android.widget.Toast.LENGTH_SHORT;

@XmlLayout(R.layout.activity_advanced_sms_user)
public class AdvancedUserSmsActivity extends InjectionAppCompatActivity {

    @Inject
    private final Logger log = AndroidLogger.forClass(AdvancedUserSmsActivity.class);

    @InjectView(R.id.listView_Adv_Sms_Activity)
    ListView listViewAdv;

    RealmHelper dbaccess;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();

        dbaccess = new RealmHelper(getApplicationContext());
        RealmResults<SmsData> msgitems = realm.where(SmsData.class).findAllSorted("timestamp");

        listViewAdv.setAdapter(new SmsDataAdapter(getApplicationContext(), msgitems, true));

        listViewAdv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> a, View v, int position, long id) {
                final SmsData smsData = (SmsData) listViewAdv.getItemAtPosition(position);

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        smsData.removeFromRealm();
                    }
                });

                Toast.makeText(a.getContext(), "Deleted Sms", LENGTH_SHORT).show();

                return true;
            }

        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}

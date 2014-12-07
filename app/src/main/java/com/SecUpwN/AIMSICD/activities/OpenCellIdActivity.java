package com.SecUpwN.AIMSICD.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.SecUpwN.AIMSICD.BuildConfig;
import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.service.CellTracker;
import com.SecUpwN.AIMSICD.utils.Helpers;


public class OpenCellIdActivity extends Activity {
    Button btnOk;
    Button btnCancel;
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_cell_id);

        prefs = getSharedPreferences(
                AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
        btnOk = (Button) findViewById(R.id.btnOk);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Getting OCID API Key...");

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncTask<Void, Void, String>() {
                    @Override
                    protected void onPreExecute() {
                        pd.show();
                    }

                    @Override
                    protected String doInBackground(Void... voids) {
                        try {
                            return CellTracker.requestNewOCIDKey();
                        } catch (final Exception e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pd.dismiss();
                                    Helpers.msgShort(OpenCellIdActivity.this,
                                            getString(R.string.ocid_api_error)
                                                    + e.getClass().getName() + " - " + e.getMessage());
                                }
                            });
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        if (s == null) return;

                        final String OCID_KEY = getString(R.string.pref_ocid_key);
                        prefs.edit().putString(OCID_KEY, s).commit();
                        CellTracker.OCID_API_KEY = s;
                        Helpers.msgShort(OpenCellIdActivity.this, getString(R.string.ocid_api_success));

                        pd.dismiss();
                    }
                }.execute();
            }
        });
    }

}

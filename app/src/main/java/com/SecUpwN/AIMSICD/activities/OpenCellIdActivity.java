package com.SecUpwN.AIMSICD.activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.service.CellTracker;
import com.SecUpwN.AIMSICD.utils.Helpers;

/*
 * TODO: A couple of comments here would be good...
 */
public class OpenCellIdActivity extends BaseActivity {
    private SharedPreferences prefs;
    private final String TAG = "OpenCellIdActivity";
    private Handler handler;
    private ProgressDialog pd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_cell_id);

        prefs = getSharedPreferences(AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
    }

    public void onAcceptedClicked(View v) {
        pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.getting_ocid_api_key));

        OpenCellIdKeyDownloaderTask ocikd = new OpenCellIdKeyDownloaderTask();
        ocikd.execute();
        pd.show();
    }

    public void onCancelClicked(View v) {
        finish();
    }

    private class OpenCellIdKeyDownloaderTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                return CellTracker.requestNewOCIDKey();
            } catch (final Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        Helpers.msgLong(OpenCellIdActivity.this,
                                getString(R.string.ocid_api_error) + e.getClass().getName() +
                                        " - " + e.getMessage());
                    }

                });

                finish();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (s == null || s.isEmpty())
                return;

            String OCID_KEY = getString(R.string.pref_ocid_key);
            prefs.edit().putString(OCID_KEY, s).commit();
            CellTracker.OCID_API_KEY = s;
            Helpers.msgShort(OpenCellIdActivity.this, getString(R.string.ocid_api_success));

            pd.dismiss();
            finish();
        }
    }

}

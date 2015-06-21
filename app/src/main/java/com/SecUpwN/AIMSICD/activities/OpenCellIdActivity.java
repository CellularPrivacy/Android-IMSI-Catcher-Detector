/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.service.CellTracker;
import com.SecUpwN.AIMSICD.utils.Helpers;

/**
 * Popup asking if user wants to download new API key to access OpenCellId services.
 */
public class OpenCellIdActivity extends BaseActivity {
    private SharedPreferences prefs;
    private final String TAG = "OpenCellIdActivity";
    private ProgressDialog pd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_cell_id);

        prefs = getSharedPreferences(AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
    }

    public void onAcceptedClicked(View v) {
        pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.getting_ocid_key));
        pd.show();

        OpenCellIdKeyDownloaderTask ocikd = new OpenCellIdKeyDownloaderTask();
        ocikd.execute(); //starts background thread
    }

    public void onCancelClicked(View v) {
        finish();
    }

    /**
     * Background thread to send and parse response from OCID
     */
    private class OpenCellIdKeyDownloaderTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                return CellTracker.requestNewOCIDKey();
            } catch (final Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();

                /**
                 * In case response from OCID takes more time and user pressed back or anything else,
                 * application will crash due to 'UI modification from background thread, starting new
                 * runOnUIThread will prevent it.
                 */
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

            // if returned value is "Error: You can not register new account more than once per day."
            // don't save it as the API key -.-'
            if (isKeyValid(s) == true) {
                String opcidKey = getString(R.string.pref_ocid_key);
                prefs.edit().putString(opcidKey, s).commit();
                CellTracker.OCID_API_KEY = s;
                Helpers.msgShort(OpenCellIdActivity.this, getString(R.string.ocid_api_success));
            } else {
                Helpers.msgShort(OpenCellIdActivity.this, getString(R.string.invalid_key_try_later));
            }

            pd.dismiss();
            finish();
        }

        /**
         * This might be extended in the future.
         * Two keys I started started with `dev-usr`, not sure if that's a rule.
         */
        private boolean isKeyValid(String key) {
            return key.startsWith("dev-");
        }
    }
}

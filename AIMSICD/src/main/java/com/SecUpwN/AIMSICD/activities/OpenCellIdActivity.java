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
import android.view.View;

import com.SecUpwN.AIMSICD.AppAIMSICD;
import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.service.CellTracker;
import com.SecUpwN.AIMSICD.utils.Helpers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import io.freefair.android.util.logging.AndroidLogger;
import io.freefair.android.util.logging.Logger;

/**
 *  Description:    Popup toast messages asking if user wants to download new API key
 *                  to access OpenCellId services and data.
 *
 *  TODO:
 *              [ ] Add toast for every server response code/message
 *
 *  ChangeLog:
 *
 *      2015-07-19  E:V:A       Added new server response codes, removed old comments
 *
 */
public class OpenCellIdActivity extends BaseActivity {
    private SharedPreferences prefs;
    //TODO: @Inject
    private final Logger log = AndroidLogger.forClass(OpenCellIdActivity.class);
    private ProgressDialog pd;

    //TODO: @Inject
    private OkHttpClient okHttpClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_cell_id);

        prefs = getSharedPreferences(AimsicdService.SHARED_PREFERENCES_BASENAME, 0);

        //TODO: Use a dependency injection for this
        okHttpClient = ((AppAIMSICD)getApplication()).getOkHttpClient();
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
                return requestNewOCIDKey();
            } catch (final IOException e) {
                log.warn("Error getting new OCID-API", e);

                /**
                 * In case response from OCID takes more time and user pressed back or anything else,
                 * application will crash due to 'UI modification from background thread, starting new
                 * runOnUIThread will prevent it.
                 */
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        Helpers.msgLong(OpenCellIdActivity.this, getString(R.string.ocid_api_error) +
                                e.getClass().getName() + " - " + e.getMessage());
                        finish();
                    }
                });

                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (s == null || s.isEmpty())
                return;

            // Check key validity (is done on foreign server)
            if (isKeyValid(s)) {
                String opcidKey = getString(R.string.pref_ocid_key);
                prefs.edit().putString(opcidKey, s).commit();
                CellTracker.OCID_API_KEY = s;

                Helpers.msgShort(OpenCellIdActivity.this, getString(R.string.ocid_api_success));

            } else if(s.contains("Error: You can not register new account")){
                Helpers.msgLong(getApplicationContext(), getString(R.string.only_one_key_per_day));
            } else if(s.contains("Bad Request")){
                Helpers.msgShort(OpenCellIdActivity.this, "Bad Request 400, 403 or 500 error ");
            } else {
                Helpers.msgShort(OpenCellIdActivity.this, "Unknown error please view logcat");
            }

            pd.dismiss();
            finish();
        }

         // This might be extended in the future.
         // Newly obtained keys start with: "dev-usr", not sure if that's a rule.
        private boolean isKeyValid(String key) {
            return key.startsWith("dev-");
        }


        /**
         *
         * Description:     Get an API key for Open Cell ID. Do not call this from the UI/Main thread.
         *                  For the various server responses, pleas refer to the OpenCellID API wiki:
         *                  http://wiki.opencellid.org/wiki/API#Error_codes
         *                  TODO: And the github issue #303:
         *                  https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/303
         *
         *  TODO:   [ ] Add handlers for other HTTP request and OCID Server error codes:
         *
         *      OCID status codes http://wiki.opencellid.org/wiki/API#Error_codes
         *      1 	200 	Cell not found
         *      2 	401 	Invalid API key
         *      3 	400 	Invalid input data
         *      4 	403     Your API key must be white listed in order to run this operation
         *      5 	500 	Internal server error
         *      6 	503 	Too many requests. Try later again
         *      7 	429     Daily limit 1000 requests exceeded for your API key.
         *
         * @return null or newly generated key
         */
        public  String requestNewOCIDKey() throws IOException {
            Request request = new Request.Builder()
                    .get()
                    .url(getString(R.string.opencellid_api_get_key))
                    .build();

            Response response = okHttpClient.newCall(request).execute();

            int responseCode = response.code();

            String htmlResponse = response.body().string();

            // For debugging HTTP server response and codes
            log.debug("Response Html=" + htmlResponse + " Response Code=" + String.valueOf(responseCode));

            if (responseCode == 200) {
                log.debug("OCID Code 1: Cell Not found: " + htmlResponse);
                return htmlResponse;

            } else if (responseCode == 401) {
                log.debug("OCID Code 2: Invalid API Key! :" + htmlResponse);
                return htmlResponse;

            } else if(responseCode == 400){
                log.debug("OCID Code 3: Invalid input data: " + htmlResponse);
                return "Bad Request"; // For making a toast!

            } else if (responseCode == 403) {
                log.debug("OCID Code 4:  Your API key must be white listed: " + htmlResponse);
                return "Bad Request"; // For making a toast!

            } else if(responseCode == 500){
                log.debug("OCID Code 5: Remote internal server error: " + htmlResponse);
                return "Bad Request"; // For making a toast!

            } else if (responseCode == 503) {
                log.debug("OCID Code 6: Reached 24hr API key request limit: " + htmlResponse);
                return htmlResponse;

            } else if(responseCode == 429){
                log.debug("OCID Code 7: Exceeded daily request limit (1000) for your API key: " + htmlResponse);
                return htmlResponse;

            } else {
                log.debug("OCID Returned Unknown Response: " + responseCode);
                return null;
            }
        }
    }
}

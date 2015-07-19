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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Popup asking if user wants to download new API key to access OpenCellId services.
 */
public class OpenCellIdActivity extends BaseActivity {
    private SharedPreferences prefs;
    private final String TAG = "AIMSICD";
    private final String mTAG = "OpenCellIdActivity";
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
                return requestNewOCIDKey();
            } catch (final Exception e) {
                Log.e(TAG, mTAG + ": " + e.getMessage());
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
                        Helpers.msgLong(OpenCellIdActivity.this, getString(R.string.ocid_api_error) +
                                e.getClass().getName() + " - " + e.getMessage());
                        finish();
                    }
                });

                //finish(); TODO should this finish be here or in runOnUiThread or should it even be in here at all??
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (s == null || s.isEmpty())
                return;

            // If the returned value is "Error: You can not register new account more than once per day."
            // don't save it as the API key -.-'
            if (isKeyValid(s) == true) {
                String opcidKey = getString(R.string.pref_ocid_key);
                prefs.edit().putString(opcidKey, s).commit();
                CellTracker.OCID_API_KEY = s;

                Helpers.msgShort(OpenCellIdActivity.this, getString(R.string.ocid_api_success));
            }else if(s.contains("Error: You can not register new account")){
                Helpers.msgLong(getApplicationContext(), getString(R.string.only_one_key_per_day));

            }else if(s.contains("Bad Request")){
                Helpers.msgShort(OpenCellIdActivity.this, "Bad Request 400 or 403 error ");
            }
            else
            {
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
         *  TODO:   [ ] Add handlers for other HTTP request and OCID Server error codes
         *
         * banjaxbanjo I moved this here because it has no reason to be in CellTracker
         *
         * OCID status codes http://wiki.opencellid.org/wiki/API#Error_codes
         * 1 	200 	Cell not found
         * 2 	401 	Invalid API key
         * 3 	400 	Invalid input data
         * 4 	403     Your API key must be white listed in order to run this operation
         * 5 	500 	Internal server error
         * 6 	503 	Too many requests. Try later again
         * 7 	429     Daily limit 1000 requests exceeded for your API key.
         * @return null or newly generated key
         *
         */
        public  String requestNewOCIDKey() throws Exception {

            //TODO (remove OCIDResponse.java) I dont think we need a class just a parse 1 line of text.
            //String htmlResponse = EntityUtils.toString(response.getEntity(), "UTF-8");

            HttpGet httpRequest = new HttpGet(getString(R.string.opencellid_api_get_key));
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(httpRequest);

            int responseCode = response.getStatusLine().getStatusCode();

            String htmlResponse = EntityUtils.toString(response.getEntity(), "UTF-8");

            //here for debugging response and codes
            Log.d("OCID Response Message", "Response Html="+htmlResponse+" Response Code="+String.valueOf(responseCode));

            if (responseCode == 200) {
                Log.d(TAG, mTAG + ": OCID Server Repsonse: " + htmlResponse);
                return htmlResponse;

            } else if (responseCode == 503) {
                // Check for HTTP error code 503 which is returned when user is trying to request
                // a new API key within 24 hours of the last request. (See GH issue #267)
                // Make toast message:  "Only one new API key request per 24 hours. Please try again later."

                //Calling toasts from inside an AsyncTask ins't a good idea.
                //       Helpers.msgLong(getApplicationContext(), getApplicationContext().getString(R.string.only_one_key_per_day));

                Log.d(TAG, mTAG + ": OCID Reached 24hr API key request limit: " + htmlResponse);
                return htmlResponse;
            }else if(responseCode == 400 || responseCode == 403){
                return "Bad Request";
            }else {

                // TODO add code here or elsewhere to check for NO network exceptions...
                // See: https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/293

                // TODO: Remove commented out stuff if app works without these NULLs
                // See: https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/526
                // PR: 4a68d00
                //httpclient = null;
                //httpGet = null;
                //result = null;

                Log.d(TAG, mTAG + ": OCID Returned " + responseCode);
                //throw new Exception("OCID Returned " + status.getStatusCode() + " " + status.getReasonPhrase());
                return null;
            }
        }
    }
}

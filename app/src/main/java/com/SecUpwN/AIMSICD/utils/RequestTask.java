/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.BuildConfig;
import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.activities.MapViewerOsmDroid;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.constants.TinyDbKeys;
import com.SecUpwN.AIMSICD.service.CellTracker;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * Description:
 *
 *      This class is the request handler for Downloading and Uploading of BTS data and
 *      the backing up of the database. The download function currently requests a CVS file
 *      from OCID though an API query. The parsing of this CVS file is done in the
 *      "AIMSICDDbAdapter.java" adapter, which put the downloaded data into the
 *      "DBe_import" table. This should be a read-only table, in the sense that no new
 *      BTS or info should be added there. The indexing there can be very tricky when
 *      later displayed in "DbViewerFragment.java", as they are different.
 *
 * Criticism:
 *
 *      From the original look of it. It seem to first upload newly found BTSs to OCID,
 *      then it immediately attempts to download a new OCID data set, probably expecting
 *      to see the new BTS in that data. (If this description is correct?)
 *
 *      This is NOT correct behaviour as:
 *
 *   1) OCID data takes at least a few minutes before updating their DBs with the
 *      newly uploaded CSV data file.
 *   2) It doesn't make sense to re-download data that is already populated in the
 *      DBi_bts and and DBi_measure tables.
 *   3) This is very bad because if there are fake BTS found by AIMSICD, then we're
 *      uploading them and thus making users of AIMSICD believe these are good cells.
 *      Basically we'd be corrupting the OCID data.
 *
 *
 * Issues:
 *          [ ] There is no onPreExecute here...perhaps that's why the progress bar is not shown?
 *              see:  http://developer.android.com/reference/android/os/AsyncTask.html
 *
 * ChangeLog:
 *
 *      2015-01-21  E:V:A       Moved code blocks, added placeholder code, disabled upload
 *      2015-02-13  E:V:A       Added onPreExecute() and super keywords & Logs (to be removed when working)
 *      2015-03-01  kairenken   Fixed "DBE_UPLOAD_REQUEST" + button
 *      2015-03-02  kairenken   remove OCID_UPLOAD_PREF: Upload is manual, so this is not needed anymore.
 *      2015-03-03  E:V:A       Replaced dirty SharedPreferences code with TinyDB and Upload result Toast msg.
 *      2015-06-15  SecUpwN     Increased timeout of OCID data download to avoid further retrieval errors
 *
 *  To Fix:
 *
 *      [ ] Explain why BACKUP/RESTORE_DATABASE is in here?
 *      [ ] Think about what "lookup cell info" (CELL_LOOKUP) should do
 *      [ ] App is blocked while downloading.
 *
 */
public class RequestTask extends BaseAsyncTask<String, Integer, String> {

    //Calling from the menu more extensive(more difficult for sever),
    // we have to give more time for the server response
    public static final int REQUEST_TIMEOUT_MAPS = 80000;       // [ms] 80 s Calling from map
    public static final int REQUEST_TIMEOUT_MENU = 80000;       // [ms] 80 s Calling from menu

    public static final char DBE_DOWNLOAD_REQUEST = 1;          // OCID download request from "APPLICATION" drawer title
    public static final char DBE_DOWNLOAD_REQUEST_FROM_MAP = 2; // OCID download request from "Antenna Map Viewer"
    public static final char DBE_UPLOAD_REQUEST = 6;            // OCID upload request from "APPLICATION" drawer title
    public static final char BACKUP_DATABASE = 3;               // Backup DB to CSV and AIMSICD_dump.db
    public static final char RESTORE_DATABASE = 4;              // Restore DB from CSV files
    public static final char CELL_LOOKUP = 5;                   // TODO: "All Current Cell Details (ACD)"

    public static final String TAG = "RequestTask";

    private AIMSICDDbAdapter mDbAdapter;
    private Context mAppContext;
    private char mType;
    private int mTimeOut;

    public RequestTask(Context context, char type) {
        super((Activity)context);
        this.mType = type;
        this.mAppContext = context.getApplicationContext();
        this.mDbAdapter = new AIMSICDDbAdapter(mAppContext);
        this.mTimeOut = REQUEST_TIMEOUT_MAPS;
    }

    @Override
    protected String doInBackground(String... commandString) {

        // We need to create a separate case for UPLOADING to DBe (OCID, MLS etc)
        switch (mType) {
            // OCID upload request from "APPLICATION" drawer title
            case DBE_UPLOAD_REQUEST:
                try {
                    boolean prepared = mDbAdapter.prepareOpenCellUploadData();

                    Log.i(TAG, "OCID upload data prepared - " + String.valueOf(prepared));
                    if (prepared) {
                        File file = new File((mAppContext.getExternalFilesDir(null) + File.separator) + "OpenCellID/aimsicd-ocid-data.csv");
                        publishProgress(25, 100);

                        MultipartEntity mpEntity = new MultipartEntity();
                        FileInputStream fin = new FileInputStream(file);
                        String csv = Helpers.convertStreamToString(fin);

                        mpEntity.addPart("key", new StringBody(CellTracker.OCID_API_KEY));
                        mpEntity.addPart("datafile", new InputStreamBody(
                                new ByteArrayInputStream(csv.getBytes()), "text/csv", "aimsicd-ocid-data.csv"));

                        ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
                        publishProgress(50, 100);
                        mpEntity.writeTo(bAOS);
                        bAOS.flush();
                        ByteArrayEntity bArrEntity = new ByteArrayEntity(bAOS.toByteArray());
                        bAOS.close();
                        bArrEntity.setChunked(false);
                        bArrEntity.setContentEncoding(mpEntity.getContentEncoding());
                        bArrEntity.setContentType(mpEntity.getContentType());

                        HttpClient httpclient;
                        HttpPost httppost;
                        HttpResponse response;

                        httpclient = new DefaultHttpClient();
                        httppost = new HttpPost("http://www.opencellid.org/measure/uploadCsv");
                        publishProgress(60,100);
                        httppost.setEntity(bArrEntity);
                        response = httpclient.execute(httppost);
                        publishProgress(80,100);
                        if (response!= null) {
                            Log.i(TAG, "OCID Upload Response: "
                                    + response.getStatusLine().getStatusCode() + " - "
                                    + response.getStatusLine());
                            if (response.getStatusLine().getStatusCode() == org.apache.http.HttpStatus.SC_OK) {
                                mDbAdapter.ocidProcessed();
                            }
                            publishProgress(95, 100);
                        }
                        return "Successful";
                    } else {
                        Helpers.msgLong(mAppContext, mAppContext.getString(R.string.no_data_for_publishing));
                        return null;
                    }

                    // all caused by httpclient.execute(httppost);
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "Upload OpenCellID data Exception", e);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "Upload OpenCellID data Exception", e);
                } catch (ClientProtocolException e) {
                    Log.e(TAG, "Upload OpenCellID data Exception", e);
                } catch (IOException e) {
                    Log.e(TAG, "Upload OpenCellID data Exception", e);
                } catch (Exception e) {
                    Log.e(TAG, "Upload OpenCellID data Exception", e);
                }

                // DOWNLOADING...
            case DBE_DOWNLOAD_REQUEST:          // OCID download request from "APPLICATION" drawer title
                mTimeOut = REQUEST_TIMEOUT_MENU;
            case DBE_DOWNLOAD_REQUEST_FROM_MAP: // OCID download request from "Antenna Map Viewer"
                int count;
                try {
                    int total;
                    int progress = 0;
                    String dirName = getOCDBDownloadDirectoryPath(mAppContext);
                    File dir = new File(dirName);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, OCDB_File_Name);
                    Log.i(TAG, "DBE_DOWNLOAD_REQUEST write to: " + dirName + OCDB_File_Name);

                    URL url = new URL(commandString[0]);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(mTimeOut);
                    urlConnection.setReadTimeout(mTimeOut);   // [ms] 80 s
                    urlConnection.setDoInput(true);
                    urlConnection.connect();

                    if (urlConnection.getResponseCode() != 200) {
                        try {
                            String error = Helpers.convertStreamToString(urlConnection.getErrorStream());
                            Helpers.msgLong(mAppContext, mAppContext.getString(R.string.download_error) + " " + error);
                            Log.e(TAG, "Download OCID data error: " + error);
                        } catch (Exception e) {
                            Helpers.msgLong(mAppContext, mAppContext.getString(R.string.download_error) + " "
                                    + e.getClass().getName() + " - "
                                    + e.getMessage());
                            Log.e(TAG, "Download OCID exception: ", e);
                        }
                        return "Error";
                    } else {
                        // http://stackoverflow.com/questions/10439829/urlconnection-getcontentlength-returns-1
                        // This returns "-1" for streamed response (Chunked Transfer Encoding)
                        total = urlConnection.getContentLength();
                        if (total == -1 ) {
                            Log.d(TAG, "doInBackground DBE_DOWNLOAD_REQUEST total not returned!");
                            total = 1024; // Let's set it arbitrarily to something other than "-1"
                        } else {
                            Log.d(TAG, "doInBackground DBE_DOWNLOAD_REQUEST total: " + total);
                            publishProgress((int) (0.25 * total), total); // Let's show something!
                        }

                        FileOutputStream output = new FileOutputStream(file, false);
                        InputStream input = new BufferedInputStream(urlConnection.getInputStream());

                        byte[] data = new byte[1024];
                        while ((count = input.read(data)) > 0) {
                            // writing data to file
                            output.write(data, 0, count);
                            progress += count;
                            publishProgress(progress, total);
                        }
                        input.close();
                        // flushing output
                        output.flush();
                        output.close();
                    }
                    urlConnection.disconnect();
                    return "Successful";

                } catch (MalformedURLException e) {
                    Log.e(TAG, "Malformed URL", e);
                    return null;
                } catch (IOException e) {
                    Log.w(TAG, "Problem reading data from steam", e);
                    return null;
                }

            case BACKUP_DATABASE:
                if (mDbAdapter.backupDB()) {
                    return "Successful";
                }
                return null;

            case RESTORE_DATABASE:
                if (mDbAdapter.restoreDB()) {
                    return "Successful";
                }
                return null;
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        // Silence or Remove when working:
        Log.v(TAG, "onProgressUpdate values[0]: " + values[0] + " values[1]: " + values[1]);
        AIMSICD.mProgressBar.setProgress(values[0]);
        AIMSICD.mProgressBar.setMax(values[1]);
    }

    /**
     *  Description:    This is where we:
     *
     *                  1) Check the success for OCID data download
     *                  2) call the updateOpenCellID() to populate the DBe_import table
     *                  3) call the checkDBe() to cleanup bad cells from imported data
     *                  4) present a failure/success toast message
     *                  5) set a shared preference to indicate that data has been downloaded:
     *                      "ocid_downloaded true"
     *
     *  Issues:
     *                  [ ] checkDBe() is incomplete, due to missing RAT column in DBe_import
     *
     */
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        AIMSICD.mProgressBar.setProgress(0);
        TinyDB tinydb = TinyDB.getInstance();

        switch (mType) {
            case DBE_DOWNLOAD_REQUEST:
                // if `result` is null, it will evaluate to false, no need to check for null
                if ("Successful".equals(result)) {

                    if (mDbAdapter.populateDBeImport()) {
                        Helpers.msgShort(mAppContext, mAppContext.getString(R.string.opencellid_data_successfully_received));
                    }

                    mDbAdapter.checkDBe();

                    tinydb.putBoolean("ocid_downloaded", true);
                } else {
                    Helpers.msgLong(mAppContext, mAppContext.getString(R.string.error_retrieving_opencellid_data));
                }
                break;

            case DBE_DOWNLOAD_REQUEST_FROM_MAP:
                if ("Successful".equals(result)) {
                    if (mDbAdapter.populateDBeImport()) {
                        Intent intent = new Intent(MapViewerOsmDroid.updateOpenCellIDMarkers);
                        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(intent);
                        Helpers.msgShort(mAppContext, mAppContext.getString(R.string.opencellid_data_successfully_received_markers_updated));

                        mDbAdapter.checkDBe();
                        tinydb.putBoolean("ocid_downloaded", true);
                    }
                } else {
                    Helpers.msgLong(mAppContext, mAppContext.getString(R.string.error_retrieving_opencellid_data));
                }
                showHideMapProgressBar(false);
                TinyDB.getInstance().putBoolean(TinyDbKeys.FINISHED_LOAD_IN_MAP, true);
                break;

            case DBE_UPLOAD_REQUEST:
                if ("Successful".equals(result)) {
                    Helpers.msgShort(mAppContext, mAppContext.getString(R.string.uploaded_bts_data_successfully));
                } else {
                    Helpers.msgLong(mAppContext, mAppContext.getString(R.string.error_uploading_bts_data));
                }
                break;

            case RESTORE_DATABASE:
                if ("Successful".equals(result)) {
                    Helpers.msgShort(mAppContext, mAppContext.getString(R.string.restore_database_completed));
                    Activity lActivity = getActivity();

                    //Activity may be detached or destroyed
                    if(lActivity != null) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(lActivity);
                        builder.setTitle(R.string.restore_database_completed_title).setMessage(
                                lActivity.getString(R.string.restore_database_completed));
                        builder.create().show();
                    }
                } else {
                    Helpers.msgLong(mAppContext, mAppContext.getString(R.string.error_restoring_database));
                }
                break;

            case BACKUP_DATABASE:
                if ("Successful".equals(result)) {

                    // strings.xml: pref_last_db_backup_version
                    //tinydb.putInt(mContext.getString(R.string.pref_last_database_backup_version), AIMSICDDbAdapter.DATABASE_VERSION); //TODO
                    tinydb.putInt("pref_last_db_backup_version", AIMSICDDbAdapter.DATABASE_VERSION);
                    Activity lActivity = getActivity();

                    //Activity may be detached or destroyed
                    if(lActivity != null) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(lActivity);
                        builder.setTitle(R.string.database_export_successful).setMessage(
                                lActivity.getString(R.string.database_backup_successfully_saved_to) + "\n" + AIMSICDDbAdapter.FOLDER);
                        builder.create().show();
                    }
                } else {
                    Helpers.msgLong(mAppContext, mAppContext.getString(R.string.error_backing_up_data));
                }
        }
    }

    @Override
    protected void onActivityDetached() {
        if (mType == DBE_DOWNLOAD_REQUEST_FROM_MAP) {
            showHideMapProgressBar(false);
        }
    }

    @Override
    protected void onActivityAttached() {
        if (mType == DBE_DOWNLOAD_REQUEST_FROM_MAP) {
            showHideMapProgressBar(true);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (mType == DBE_DOWNLOAD_REQUEST_FROM_MAP) {
            showHideMapProgressBar(false);
        }
    }

    private void showHideMapProgressBar(boolean pFlag) {
        Activity lActivity = getActivity();
        if(BuildConfig.DEBUG && lActivity == null) {
            Log.v(TAG, "BaseTask showHideMapProgressBar() activity is null");
        }
        if (lActivity != null && lActivity instanceof MapViewerOsmDroid) {
            ((MapViewerOsmDroid) lActivity).setRefreshActionButtonState(pFlag);
        }
    }

    public static final String OCDB_File_Name = "opencellid.csv";

    /**
     * The folder path to OCDB download.
     * @param context
     * @return
     */
    public static String getOCDBDownloadDirectoryPath(Context context) {
        return (context.getExternalFilesDir(null) + File.separator) + "OpenCellID/";
    }
}

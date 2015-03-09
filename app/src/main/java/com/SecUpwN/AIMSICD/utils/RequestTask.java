package com.SecUpwN.AIMSICD.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.activities.MapViewerOsmDroid;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.service.CellTracker;
//import com.SecUpwN.AIMSICD.utils.Helpers;
import com.SecUpwN.AIMSICD.utils.TinyDB;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

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
 *
 *  To Fix:
 *
 *      [ ] Explain why BACKUP/RESTORE_DATABASE is in here?
 *      [ ] Think about what "lookup cell info" (CELL_LOOKUP) should do
 *      [ ] App is blocked while downloading.
 *
 */
public class RequestTask extends AsyncTask<String, Integer, String> {

    public static final char DBE_DOWNLOAD_REQUEST = 1;          // OCID download request from "APPLICATION" drawer title
    public static final char DBE_DOWNLOAD_REQUEST_FROM_MAP = 2; // OCID download request from "Antenna Map Viewer"
    public static final char DBE_UPLOAD_REQUEST = 6;            // OCID upload request from "APPLICATION" drawer title
    public static final char BACKUP_DATABASE = 3;               // Backup DB to CSV and AIMSICD_dump.db
    public static final char RESTORE_DATABASE = 4;              // Restore DB from CSV files
    public static final char CELL_LOOKUP = 5;                   // TODO: "All Current Cell Details (ACD)"

    public static final String TAG = "AIMSICD";
    public static final String mTAG = "RequestTask";

    private final AIMSICDDbAdapter mDbAdapter;
    private final Context mContext;
    private final char mType;

    public RequestTask(Context context, char type) {
        mType = type;
        mContext = context;
        mDbAdapter = new AIMSICDDbAdapter(mContext);
    }

    @Override
    protected String doInBackground(String... commandString) {

        // We need to create a separate case for UPLOADING to DBe (OCID, MLS etc)
        switch (mType) {

            // UPLOADING !!
            case DBE_UPLOAD_REQUEST:   // OCID upload request from "APPLICATION" drawer title
                try {
                        boolean prepared = mDbAdapter.prepareOpenCellUploadData();
                        Log.i(TAG, mTAG + ": OCID upload data prepared - " + String.valueOf(prepared));
                        if (prepared) {
                            File file = new File(Environment.getExternalStorageDirectory()
                                    + "/AIMSICD/OpenCellID/aimsicd-ocid-data.csv");
                            publishProgress(25,100);

                            MultipartEntity mpEntity = new MultipartEntity();
                            FileInputStream fin = new FileInputStream(file);
                            String csv = Helpers.convertStreamToString(fin);

                            mpEntity.addPart("key", new StringBody(CellTracker.OCID_API_KEY));
                            mpEntity.addPart("datafile", new InputStreamBody(
                                    new ByteArrayInputStream(csv.getBytes()), "text/csv", "aimsicd-ocid-data.csv"));

                            ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
                            publishProgress(50,100);
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
                                Log.i(TAG, mTAG + ": OCID Upload Response: "
                                        + response.getStatusLine().getStatusCode() + " - "
                                        + response.getStatusLine());
                                if (response.getStatusLine().getStatusCode() == org.apache.http.HttpStatus.SC_OK) {
                                    mDbAdapter.ocidProcessed();
                                }
                                publishProgress(95,100);
                            }
                            return "Successful";
                        } else {
                            Helpers.msgLong(mContext, "No data for publishing available");
                            return null;
                        }

                } catch (Exception e) {
                    Log.e(TAG, mTAG + ": Upload OpenCellID data Exception - " + e.getMessage());
                }

            // DOWNLOADING...
            case DBE_DOWNLOAD_REQUEST:          // OCID download request from "APPLICATION" drawer title
            case DBE_DOWNLOAD_REQUEST_FROM_MAP: // OCID download request from "Antenna Map Viewer"
                int count;
                try {
                    int total;
                    int progress = 0;

                    File dir = new File(Environment.getExternalStorageDirectory()+ "/AIMSICD/OpenCellID/");
                    if (!dir.exists()) { dir.mkdirs(); } // need a try{} catch{}
                    File file = new File(dir, "opencellid.csv");

                    URL url = new URL(commandString[0]);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(20000);// [ms] 20 s
                    urlConnection.setReadTimeout(20000);   // [ms] 20 s
                    urlConnection.setDoInput(true);
                    urlConnection.connect();

                    if (urlConnection.getResponseCode() != 200) {
                        try {
                            String error = Helpers.convertStreamToString(urlConnection.getErrorStream());
                            Helpers.msgLong(mContext, "Download error: " + error);
                            Log.e(TAG, mTAG + ": Download OCID data error: " + error);
                        } catch (Exception e) {
                            Helpers.msgLong(mContext, "Download error: "
                                    + e.getClass().getName() + " - "
                                    + e.getMessage());
                            Log.e(TAG, mTAG + ": Download OCID exception: " + e);
                        }
                        return "Error";
                    } else {
                        // http://stackoverflow.com/questions/10439829/urlconnection-getcontentlength-returns-1
                        // This returns "-1" for streamed response (Chunked Transfer Encoding)
                        total = urlConnection.getContentLength();
                        if (total == -1 ) {
                            Log.d(TAG, mTAG + ":doInBackground DBE_DOWNLOAD_REQUEST total not returned!");
                            total = 1024; // Let's set it arbitrarily to something other than "-1"
                        } else {
                            Log.d(TAG, mTAG + ":doInBackground DBE_DOWNLOAD_REQUEST total: " + total);
                            //publishProgress(progress, total);
                            publishProgress((int) (0.25*total), total); // Let's show something!
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
                        // flushing output
                        output.flush();
                        output.close();
                    }
                    urlConnection.disconnect();
                    return "Successful";

                } catch (MalformedURLException e) {
                    return null;
                } catch (IOException e) {
                    return null;
                }

            case BACKUP_DATABASE:
                mDbAdapter.open();
                if (mDbAdapter.backupDB()) {
                    mDbAdapter.close();
                    return "Successful";
                }
                mDbAdapter.close();
                return null;

            case RESTORE_DATABASE:
                mDbAdapter.open();
                if (mDbAdapter.restoreDB()) {
                    mDbAdapter.close();
                    return "Successful";
                }
                mDbAdapter.close();
                return null;
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG, mTAG + ":onPreExecute Started");
        //progress.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        // Silence or Remove when working:
        Log.v(TAG, mTAG + ":onProgressUpdate values[0]: " + values[0] +
                                           " values[1]: " + values[1]);
        //setProgressPercent(progress[0]); ??
        AIMSICD.mProgressBar.setProgress(values[0]);    // progress
        AIMSICD.mProgressBar.setMax(values[1]);         // total
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
        TinyDB tinydb = new TinyDB(mContext);

        switch (mType) {
            case DBE_DOWNLOAD_REQUEST:
                if (result != null && result.equals("Successful")) {
                    mDbAdapter.open();
                    if (mDbAdapter.updateOpenCellID()) {
                        Helpers.msgShort(mContext, "OpenCellID data successfully received");
                    }

                    mDbAdapter.checkDBe();
                    mDbAdapter.close();
                    tinydb.putBoolean("ocid_downloaded", true);
                } else {
                    Helpers.msgLong(mContext, "Error retrieving OpenCellID data.\nCheck your network!");
                }
                break;

            case DBE_DOWNLOAD_REQUEST_FROM_MAP:
                if (result != null && result.equals("Successful")) {
                    mDbAdapter.open();
                    if (mDbAdapter.updateOpenCellID()) {
                        Intent intent = new Intent(MapViewerOsmDroid.updateOpenCellIDMarkers);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        Helpers.msgShort(mContext, "OpenCellID data successfully received.\nMap Markers updated.");

                        mDbAdapter.checkDBe();
                        mDbAdapter.close();
                        tinydb.putBoolean("ocid_downloaded", true);
                    }
                } else {
                    Helpers.msgLong(mContext, "Error retrieving OpenCellID data.\nCheck your network!");
                }
                break;

            case DBE_UPLOAD_REQUEST:
                if (result != null && result.equals("Successful")) {
                    Helpers.msgShort(mContext, "Uploaded BTS data to OCID successfully");
                } else {
                    Helpers.msgLong(mContext, "Error in uploading BTS data to OCID servers!");
                }
                break;

            case RESTORE_DATABASE:
                if (result != null && result.equals("Successful")) {
                    Helpers.msgShort(mContext, "Restore database completed successfully");
                } else {
                    Helpers.msgLong(mContext, "Error restoring database");
                }
                break;

            case BACKUP_DATABASE:
                if (result != null && result.equals("Successful")) {

                    // strings.xml: pref_last_db_backup_version
                    //tinydb.putInt(mContext.getString(R.string.pref_last_database_backup_version), AIMSICDDbAdapter.DATABASE_VERSION); //TODO
                    tinydb.putInt("pref_last_db_backup_version", AIMSICDDbAdapter.DATABASE_VERSION);

                    final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.database_export_successful).setMessage(
                            "Database Backup successfully saved to:\n" + AIMSICDDbAdapter.FOLDER);
                    builder.create().show();
                } else {
                    Helpers.msgLong(mContext, "Error backing up database");
                }
                //break;
        }
    }
}
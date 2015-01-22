package com.SecUpwN.AIMSICD.utils;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.activities.MapViewerOsmDroid;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.service.CellTracker;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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

public class RequestTask extends AsyncTask<String, Integer, String> {

    public static final char DBE_DOWNLOAD_REQUEST = 1;          // OCID download request from "APPLICATION" drawer title
    public static final char DBE_DOWNLOAD_REQUEST_FROM_MAP = 2; // OCID download request from "Antenna Map Viewer"
    //public static final char DBE_UPLOAD_REQUEST = 3;            // TODO: OCID upload request from "APPLICATION" drawer title
    public static final char BACKUP_DATABASE = 3;
    public static final char RESTORE_DATABASE = 4;
    public static final char CELL_LOOKUP = 5;                   // TODO: What's this! ??


    private final AIMSICDDbAdapter mDbAdapter;
    private final Context mContext;
    private final char mType;

    public RequestTask(Context context, char type) {
        mType = type;
        mContext = context;
        mDbAdapter = new AIMSICDDbAdapter(mContext);
    }

    /**
     *  ???
     * TODO: It is unclear what this is attempting to do. Please add more comments.
     *
     * From the original look of it. It seem to first upload newly found BTSs to OCID,
     * then it immediately attempts to download a new OCID data set, probably expecting
     * to see the new BTS in that data. (If this description is correct?)
     *
     * This is NOT correct behaviour as:
     *   1) OCID data takes at least a few minutes before updating their DBs with the
     *      newly uploaded CSV data file.
     *   2) It doesn't make sense to re-download data that is already populated in the
     *      DBi_bts and and DBi_measure tables.
     *   3) This is very bad because if there are fake BTS found by AIMSICD, then we're
     *      uploading them and thus making users of AIMSICD believe these are good cells.
     *      Basically we'd be corrupting the OCID data.
     *
     * So is this function dependent?
     *
     * ChangeLog:
     *
     *      2015-01-21 E:V:A   Moved code blocks, added placeholder code, disabled upload
     *
     */

    @Override
    protected String doInBackground(String... commandString) {

        // We need to create a separate case for UPLOADING to DBe (OCID, MLS etc)
        switch (mType) {
/*
            // UPLOADING !!
            case DBE_UPLOAD_REQUEST:   // OCID upload request from "APPLICATION" drawer title
                try {
                    if (CellTracker.OCID_UPLOAD_PREF) {
                        boolean prepared = mDbAdapter.prepareOpenCellUploadData();
                        Log.i("AIMSICD", "OCID upload data prepared - " + String.valueOf(prepared));
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
                                Log.i("AIMSICD", "OCID Upload Response: "
                                        + response.getStatusLine().getStatusCode() + " - "
                                        + response.getStatusLine());
                                mDbAdapter.ocidProcessed();
                                publishProgress(95,100);
                            }

                        }
                    }
                } catch (Exception e) {
                    Log.i("AIMSICD", "Upload OpenCellID data - " + e.getMessage());
                }
*/
            // DOWNLOADING...
            case DBE_DOWNLOAD_REQUEST:          // OCID download request from "APPLICATION" drawer title
            case DBE_DOWNLOAD_REQUEST_FROM_MAP: // OCID download request from "Antenna Map Viewer"
                int count;
                try {
                    int total;
                    int progress = 0;

                    File dir = new File(Environment.getExternalStorageDirectory()+ "/AIMSICD/OpenCellID/");
                    if (!dir.exists()) { dir.mkdirs(); }
                    File file = new File(dir, "opencellid.csv");

                    URL url = new URL(commandString[0]);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(20000);// [ms] 20 s
                    urlConnection.setReadTimeout(20000); // [ms] 20 s
                    urlConnection.setDoInput(true);
                    urlConnection.connect();

                    if (urlConnection.getResponseCode() != 200) {
                        try {
                            String error = Helpers.convertStreamToString(urlConnection.getErrorStream());
                            Helpers.msgLong(mContext, "Download error: " + error);
                            Log.e("AIMSICD", "Download OCID data error: " + error);
                        } catch (Exception e) {
                            Helpers.msgLong(mContext, "Download error: "
                                    + e.getClass().getName() + " - "
                                    + e.getMessage());
                            Log.e("AIMSICD", "Download OCID - " + e);
                        }
                        return "Error";
                    } else {
                        total = urlConnection.getContentLength();
                        publishProgress(progress, total);
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

    protected void onProgressUpdate(Integer... values) {
        AIMSICD.mProgressBar.setMax(values[1]);
        AIMSICD.mProgressBar.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        AIMSICD.mProgressBar.setProgress(0);

        switch (mType) {
            case DBE_DOWNLOAD_REQUEST:
                if (result != null && result.equals("Successful")) {
                    mDbAdapter.open();
                    if (mDbAdapter.updateOpenCellID()) {
                        Helpers.msgShort(mContext, "OpenCellID data successfully received");
                    }
                    mDbAdapter.close();
                } else {
                    Helpers.msgShort(mContext, "Error retrieving OpenCellID data");
                }
                break;
            case DBE_DOWNLOAD_REQUEST_FROM_MAP:
                if (result != null && result.equals("Successful")) {
                    mDbAdapter.open();
                    if (mDbAdapter.updateOpenCellID()) {
                        Intent intent = new Intent(MapViewerOsmDroid.updateOpenCellIDMarkers);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        Helpers.msgShort(mContext, "OpenCellID data successfully received and "
                                + "Map Markers updated");
                        mDbAdapter.close();
                    }
                } else {
                    Helpers.msgShort(mContext, "Error retrieving OpenCellID data");
                }
                break;

            //case DBE_UPLOAD_REQUEST:
            //    // blah blah
            //    break;

            case RESTORE_DATABASE:
                if (result != null && result.equals("Successful")) {
                    Helpers.msgShort(mContext, "Restore database completed successfully");
                } else {
                    Helpers.msgShort(mContext, "Error restoring database");
                }
                break;

            case BACKUP_DATABASE:
                if (result != null && result.equals("Successful")) {
                    SharedPreferences prefs;
                    prefs = mContext.getSharedPreferences(
                            AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
                    SharedPreferences.Editor prefsEditor;
                    prefsEditor = prefs.edit();
                    prefsEditor.putInt(mContext.getString(R.string.pref_last_database_backup_version),
                            AIMSICDDbAdapter.DATABASE_VERSION);
                    prefsEditor.apply();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.database_export_successful)
                            .setMessage("Database Backup successfully saved to:\n"
                                    + AIMSICDDbAdapter.FOLDER);
                    builder.create().show();
                } else {
                    Helpers.msgShort(mContext, "Error backing up database");
                }
        }
    }
}
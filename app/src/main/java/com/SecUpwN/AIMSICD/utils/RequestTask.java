package com.SecUpwN.AIMSICD.utils;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.activities.MapViewer;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.service.AimsicdService;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

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
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class RequestTask extends AsyncTask<String, Integer, String> {

    public static final char OPEN_CELL_ID_REQUEST = 1;
    public static final char OPEN_CELL_ID_REQUEST_FROM_MAP = 2;
    public static final char BACKUP_DATABASE = 3;
    public static final char RESTORE_DATABASE = 4;

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
        switch (mType) {
            case OPEN_CELL_ID_REQUEST:
            case OPEN_CELL_ID_REQUEST_FROM_MAP:
                int count;
                try {
                    if (AimsicdService.OCID_UPLOAD_PREF) {
                        boolean prepared = mDbAdapter.prepareOpenCellUploadData();
                        Log.i("AIMSICD", "OCID prepared - " + String.valueOf(prepared));
                        if (prepared) {
                            File file = new File(Environment.getExternalStorageDirectory()
                                    + "/AIMSICD/OpenCellID/aimsicd-ocid-data.csv");

                            MultipartEntity mpEntity = new MultipartEntity();
                            FileInputStream fin = new FileInputStream(file);
                            String csv = Helpers.convertStreamToString(fin);
                            mpEntity.addPart("key", new StringBody(AimsicdService.OCID_API_KEY));
                            mpEntity.addPart("datafile", new InputStreamBody(new ByteArrayInputStream(csv.getBytes()), "text/csv", "aimsicd-ocid-data.csv"));

                            ByteArrayOutputStream mBAOS = new ByteArrayOutputStream();

                            mpEntity.writeTo(mBAOS);
                            mBAOS.flush();
                            ByteArrayEntity bArrEntity = new ByteArrayEntity(mBAOS.toByteArray());
                            mBAOS.close();
                            bArrEntity.setChunked(false);
                            bArrEntity.setContentEncoding(mpEntity.getContentEncoding());
                            bArrEntity.setContentType(mpEntity.getContentType());

                            HttpClient httpclient;
                            HttpPost httppost;
                            HttpResponse response = null;

                            httpclient = new DefaultHttpClient();
                            httppost = new HttpPost("http://www.opencellid.org/measure/uploadCsv");

                            httppost.setEntity(bArrEntity);
                            response = httpclient.execute(httppost);

                            if (response!= null) {
                                Log.i("AIMSICD", "OCID Upload Response: " + response.getStatusLine().getStatusCode() + " - " + response.getStatusLine());
                                HttpEntity resEntity = response.getEntity();
                                Log.i("AIMSICD", "OCID Response Content: " + EntityUtils.toString(resEntity));
                                resEntity.consumeContent();
                                mDbAdapter.ocidProcessed();
                            }

                        }
                    }
                } catch (Exception e) {
                    Log.i("AIMSICD", "Upload OpenCellID data - " + e.getMessage());
                }

                try {
                    File dir = new File(
                            Environment.getExternalStorageDirectory()
                                    + "/AIMSICD/OpenCellID/"
                    );
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, "opencellid.csv");

                    URL url = new URL(commandString[0]);
                    URLConnection connection = url.openConnection();
                    connection.connect();

                    // download the file
                    InputStream input = new BufferedInputStream(url.openStream(),
                            8192);

                    // Output stream
                    OutputStream output = new FileOutputStream(file);

                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        publishProgress((int) (total / 1024) * 2 );

                        // writing data to file
                        output.write(data, 0, count);
                    }

                    // flushing output
                    output.flush();

                    // closing streams
                    output.close();
                    input.close();

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
        AIMSICD.mProgressBar.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        AIMSICD.mProgressBar.setProgress(0);

        switch (mType) {
            case OPEN_CELL_ID_REQUEST:
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
            case OPEN_CELL_ID_REQUEST_FROM_MAP:
                if (result != null && result.equals("Successful")) {
                    mDbAdapter.open();
                    if (mDbAdapter.updateOpenCellID()) {
                        Intent intent = new Intent(MapViewer.updateOpenCellIDMarkers);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        Helpers.msgShort(mContext, "OpenCellID data successfully received and "
                                + "Map Markers updated");
                        mDbAdapter.close();
                    }
                } else {
                    Helpers.msgShort(mContext, "Error retrieving OpenCellID data");
                }
                break;
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
package com.SecUpwN.AIMSICD.utils;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RequestTask extends AsyncTask<String, String, String> {

    public static final int OPEN_CELL_ID_REQUEST = 1;
    public static final int RESTORE_DATABASE = 2;
    private final HttpClient Client = new DefaultHttpClient();
    private String Content;
    private String Error = null;
    private final AIMSICDDbAdapter mDbAdapter;
    private final Context mContext;
    private int mType;

    public RequestTask (Context context, int type) {
        mType = type;
        mContext = context;
        mDbAdapter = new AIMSICDDbAdapter(mContext);
    }

    @Override
    protected String doInBackground(String... uri) {

        switch (mType) {
            case OPEN_CELL_ID_REQUEST:
                try {
                    URL url = new URL(uri[0]);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoOutput(true);
                    urlConnection.connect();

                    File dir = new File(
                            Environment.getExternalStorageDirectory()
                                    + "/AIMSICD/OpenCellID/");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, "opencellid.csv");

                    FileOutputStream fileOutput = new FileOutputStream(file);
                    InputStream inputStream = urlConnection.getInputStream();
                    int totalSize = urlConnection.getContentLength();
                    AIMSICD.mProgressBar.setMax(totalSize);
                    int downloadedSize = 0;

                    byte[] buffer = new byte[1024];
                    int bufferLength = 0;

                    while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                        fileOutput.write(buffer, 0, bufferLength);
                        downloadedSize += bufferLength;

                        //Update progress bar
                        AIMSICD.mProgressBar.setProgress(downloadedSize);
                    }

                    fileOutput.close();

                } catch (MalformedURLException e) {
                    return null;
                } catch (IOException e) {
                    return null;
                }
                break;
            case RESTORE_DATABASE:
                mDbAdapter.open();
                if (mDbAdapter.importDB())
                    return "Successful";
                mDbAdapter.close();
                break;
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        switch (mType) {
            case OPEN_CELL_ID_REQUEST:
                AIMSICD.mProgressBar.setProgress(0);
                try {
                    mDbAdapter.open();
                    mDbAdapter.updateOpenCellID();
                    mDbAdapter.close();
                } catch (Exception e) {
                    Log.e("AIMSICD",
                            "RequestTask() write OpenCellID response - " + e);
                }
                break;
            case RESTORE_DATABASE:
                if (result != null) {
                    if (result.equals("Successful")){
                        Helpers.sendMsg(mContext, "Restore database completed successfully");
                    } else {
                        Helpers.sendMsg(mContext, "Error restoring database");
                    }
                } else {
                    Helpers.sendMsg(mContext, "Error restoring database");
                }
                break;
        }
    }
}
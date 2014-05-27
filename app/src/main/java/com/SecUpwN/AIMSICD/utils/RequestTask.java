package com.SecUpwN.AIMSICD.utils;

import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class RequestTask extends AsyncTask<String, String, String> {

    public static final int OPEN_CELL_ID_REQUEST = 1;

    private AIMSICDDbAdapter mDbAdapter;
    private Context mContext;
    private int mType;

    RequestTask (Context context, int type) {
        mContext = context;
        mType = type;
    }

    @Override
    protected String doInBackground(String... uri) {
        String responseString = null;
        switch (mType) {
            case OPEN_CELL_ID_REQUEST:
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response;

                try {
                    response = httpclient.execute(new HttpGet(uri[0]));
                    StatusLine statusLine = response.getStatusLine();
                    if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        response.getEntity().writeTo(out);
                        out.close();
                        responseString = out.toString();
                    } else {
                        //Closes the connection.
                        response.getEntity().getContent().close();
                        throw new IOException(statusLine.getReasonPhrase());
                    }
                } catch (IOException e) {
                    //TODO Handle problems..
                }
            break;
        }

        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        switch (mType) {
            case OPEN_CELL_ID_REQUEST:
                if (result != null) {
                    if (Helpers.isSdWritable()) {
                        try {
                            File dir = new File(
                                    Environment.getExternalStorageDirectory()
                                            + "/AIMSICD/OpenCellID/");
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }
                            String fileName = Environment.getExternalStorageDirectory()
                                    + "/AIMSICD/OpenCellID/opencellid.csv";
                            File file = new File(dir, "opencellid.csv");

                            FileOutputStream fOut = new FileOutputStream(file);
                            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                            myOutWriter.append(result);
                            myOutWriter.close();
                            fOut.close();
                            mDbAdapter = new AIMSICDDbAdapter(mContext);
                            mDbAdapter.open();
                            mDbAdapter.updateOpenCellID();
                        } catch (Exception e) {
                            Log.e("AIMSICD",
                                    "RequestTask() write OpenCellID response - " + e.getMessage());
                        }
                    }
                }
                break;
        }
    }
}
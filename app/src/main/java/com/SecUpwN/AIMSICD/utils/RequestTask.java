package com.SecUpwN.AIMSICD.utils;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class RequestTask extends AsyncTask<String, String, String> {

    public static final int OPEN_CELL_ID_REQUEST = 1;
    public static final int BACKUP_DATABASE = 2;
    public static final int RESTORE_DATABASE = 3;
    private final AIMSICDDbAdapter mDbAdapter;
    private final Context mContext;
    private final int mType;

    public RequestTask (Context context, int type) {
        mType = type;
        mContext = context;
        mDbAdapter = new AIMSICDDbAdapter(mContext);
    }

    @Override
    protected String doInBackground(String... urlString) {

        switch (mType) {
            case OPEN_CELL_ID_REQUEST:
                int count;
                try {
                    File dir = new File(
                            Environment.getExternalStorageDirectory()
                                    + "/AIMSICD/OpenCellID/");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, "opencellid.csv");

                    URL url = new URL(urlString[0]);
                    URLConnection conection = url.openConnection();
                    conection.connect();

                    // this will be useful so that you can show a typical 0-100%
                    // progress bar
                    int lengthOfFile = conection.getContentLength();
                    AIMSICD.mProgressBar.setMax(lengthOfFile);

                    // download the file
                    InputStream input = new BufferedInputStream(url.openStream(),
                            8192);

                    // Output stream
                    OutputStream output = new FileOutputStream(file);

                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        AIMSICD.mProgressBar.setProgress((int) ((total * 100) / lengthOfFile));

                        // writing data to file
                        output.write(data, 0, count);
                    }

                    // flushing output
                    output.flush();

                    // closing streams
                    output.close();
                    input.close();

                } catch (MalformedURLException e) {
                    return null;
                } catch (IOException e) {
                    return null;
                }
                break;
            case BACKUP_DATABASE:
                mDbAdapter.open();
                if (mDbAdapter.backupDB())
                    return "Successful";
                mDbAdapter.close();
                break;
            case RESTORE_DATABASE:
                mDbAdapter.open();
                if (mDbAdapter.restoreDB())
                    return "Successful";
                mDbAdapter.close();
                break;
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        AIMSICD.mProgressBar.setProgress(0);

        switch (mType) {
            case OPEN_CELL_ID_REQUEST:
                    mDbAdapter.open();
                    if (mDbAdapter.updateOpenCellID())
                        Helpers.sendMsg(mContext, "OpenCellID data successfully received");
                    mDbAdapter.close();
                break;
            case RESTORE_DATABASE:
                if (result != null && result.equals("Successful")) {
                    Helpers.sendMsg(mContext, "Restore database completed successfully");
                } else {
                    Helpers.sendMsg(mContext, "Error restoring database");
                }
                break;
        }
    }
}
/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

import com.secupwn.aimsicd.BuildConfig;
import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.constants.DrawerMenu;
import com.secupwn.aimsicd.constants.TinyDbKeys;
import com.secupwn.aimsicd.service.CellTracker;
import com.secupwn.aimsicd.ui.fragments.MapFragment;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import io.freefair.android.injection.annotation.Inject;
import io.freefair.android.injection.app.InjectionAppCompatActivity;
import io.freefair.android.util.logging.Logger;
import io.realm.Realm;
import lombok.Cleanup;

/**
 *
 * Description:
 *
 *      This class is the request handler for Downloading and Uploading of BTS data and
 *      the backing up of the database. The download function currently requests a CVS file
 *      from OCID though an API query. The parsing of this CVS file is done in the
 *      "AIMSICDDbAdapter.java" adapter, which put the downloaded data into the
 *      {@link com.secupwn.aimsicd.data.model.Import Import} realm. This should be a read-only table, in the sense that no new
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
    public static final char CELL_LOOKUP = 5;                   // TODO: "All Current Cell Details (ALL_CURRENT_CELL_DETAILS)"

    @Inject
    private Logger log;

    private RealmHelper mDbAdapter;
    private Context mAppContext;
    private char mType;
    private int mTimeOut;

    private AsyncTaskCompleteListener mListener;

    @Inject
    private OkHttpClient okHttpClient;

    /**
     *
     * @param context App context
     * @param type What type of request to be performed (download OCID, upload OCID, DB backup, etc.)
     * @param listener Allows the caller of RequestTask to implement success/fail callbacks
     */
    public RequestTask(InjectionAppCompatActivity context, char type, AsyncTaskCompleteListener listener) {
        super(context);
        this.mType = type;
        this.mAppContext = context.getApplicationContext();
        this.mDbAdapter = new RealmHelper(mAppContext);
        this.mTimeOut = REQUEST_TIMEOUT_MAPS;
        this.mListener = listener;
    }

    /**
     * @deprecated Use {@link #RequestTask(InjectionAppCompatActivity, char, AsyncTaskCompleteListener)}
     *             instead because of the listener callback interface.
     * @param context
     * @param type
     */
    @Deprecated
    public RequestTask(InjectionAppCompatActivity context, char type) {
        this(context, type, null);
        log.warn("RequestTask(InjectionAppCompatActivity, char) is deprecated in favour of using listener callbacks");
    }

    @Override
    protected String doInBackground(String... commandString) {

        // We need to create a separate case for UPLOADING to DBe (OCID, MLS etc)
        switch (mType) {
            // OCID upload request from "APPLICATION" drawer title
            case DBE_UPLOAD_REQUEST:
                try {
                    @Cleanup Realm realm = Realm.getDefaultInstance();
                    boolean prepared = mDbAdapter.prepareOpenCellUploadData(realm);

                    log.info("OCID upload data prepared - " + String.valueOf(prepared));
                    if (prepared) {
                        File file = new File((mAppContext.getExternalFilesDir(null) + File.separator) + "OpenCellID/aimsicd-ocid-data.csv");
                        publishProgress(25, 100);

                        RequestBody requestBody = new MultipartBuilder()
                                .type(MultipartBuilder.FORM)
                                .addFormDataPart("key", CellTracker.OCID_API_KEY)
                                .addFormDataPart("datafile", "aimsicd-ocid-data.csv", RequestBody.create(MediaType.parse("text/csv"), file))
                                .build();

                        Request request = new Request.Builder()
                                .url("http://www.opencellid.org/measure/uploadCsv")
                                .post(requestBody)
                                .build();

                        publishProgress(60, 100);

                        Response response = okHttpClient.newCall(request).execute();

                        publishProgress(80, 100);
                        if (response != null) {
                            log.info("OCID Upload Response: "
                                    + response.code() + " - "
                                    + response.message());
                            if (response.code() == 200) {
                                Realm.Transaction transaction = mDbAdapter.ocidProcessed();
                                realm.executeTransaction(transaction);
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
                    log.error("Upload OpenCellID data Exception", e);
                } catch (FileNotFoundException e) {
                    log.error("Upload OpenCellID data Exception", e);
                } catch (IOException e) {
                    log.error("Upload OpenCellID data Exception", e);
                } catch (Exception e) {
                    log.error("Upload OpenCellID data Exception", e);
                }

                // DOWNLOADING...
            case DBE_DOWNLOAD_REQUEST:          // OCID download request from "APPLICATION" drawer title
                mTimeOut = REQUEST_TIMEOUT_MENU;
            case DBE_DOWNLOAD_REQUEST_FROM_MAP: // OCID download request from "Antenna Map Viewer"
                int count;
                try {
                    long total;
                    int progress = 0;
                    String dirName = getOCDBDownloadDirectoryPath(mAppContext);
                    File dir = new File(dirName);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File file = new File(dir, OCDB_File_Name);
                    log.info("DBE_DOWNLOAD_REQUEST write to: " + dirName + OCDB_File_Name);

                    Request request = new Request.Builder()
                            .url(commandString[0])
                            .get()
                            .build();

                    Response response;
                    try {
                        // OCID's API can be slow. Give it up to a minute to do its job. Since this
                        // is a backgrounded task, it's ok to wait for a while.
                        okHttpClient.setReadTimeout(60, TimeUnit.SECONDS);
                        response = okHttpClient.newCall(request).execute();
                        okHttpClient.setReadTimeout(10, TimeUnit.SECONDS); // Restore back to default
                    } catch (SocketTimeoutException e) {
                        log.warn("Trying to talk to OCID timed out after 60 seconds. API is slammed? Throttled?");
                        return "Timeout";
                    }

                    if (response.code() != 200) {
                        try {
                            String error = response.body().string();
                            Helpers.msgLong(mAppContext, mAppContext.getString(R.string.download_error) + " " + error);
                            log.error("Download OCID data error: " + error);
                        } catch (Exception e) {
                            Helpers.msgLong(mAppContext, mAppContext.getString(R.string.download_error) + " "
                                    + e.getClass().getName() + " - "
                                    + e.getMessage());
                            log.error("Download OCID exception: ", e);
                        }
                        return "Error";
                    } else {
                        // This returns "-1" for streamed response (Chunked Transfer Encoding)
                        total = response.body().contentLength();
                        if (total == -1) {
                            log.debug("doInBackground DBE_DOWNLOAD_REQUEST total not returned!");
                            total = 1024; // Let's set it arbitrarily to something other than "-1"
                        } else {
                            log.debug("doInBackground DBE_DOWNLOAD_REQUEST total: " + total);
                            publishProgress((int) (0.25 * total), (int) total); // Let's show something!
                        }

                        FileOutputStream output = new FileOutputStream(file, false);
                        InputStream input = new BufferedInputStream(response.body().byteStream());

                        byte[] data = new byte[1024];
                        while ((count = input.read(data)) > 0) {
                            // writing data to file
                            output.write(data, 0, count);
                            progress += count;
                            publishProgress(progress, (int) total);
                        }
                        input.close();
                        // flushing output
                        output.flush();
                        output.close();
                    }
                    return "Successful";

                } catch (IOException e) {
                    log.warn("Problem reading data from steam", e);
                    return null;
                }
        }

        return null;
    }

    /**
     * This is where we:
     * <ol>
     * <li>Check the success for OCID data download</li>
     * <li>call the updateOpenCellID() to populate the {@link com.secupwn.aimsicd.data.model.Import Import} realm</li>
     * <li>call the {@link RealmHelper#checkDBe()} to cleanup bad cells from imported data</li>
     * <li>present a failure/success toast message</li>
     * <li>set a shared preference to indicate that data has been downloaded:
     * {@code ocid_downloaded true}</li>
     * </ol>
     */
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        TinyDB tinydb = TinyDB.getInstance();

        @Cleanup Realm realm = Realm.getDefaultInstance();

        switch (mType) {
            case DBE_DOWNLOAD_REQUEST:
                // if `result` is null, it will evaluate to false, no need to check for null
                if ("Successful".equals(result)) {

                    if (mDbAdapter.populateDBeImport(realm)) {
                        Helpers.msgShort(mAppContext, mAppContext.getString(R.string.opencellid_data_successfully_received));
                    }

                    realm.executeTransaction(mDbAdapter.checkDBe());
                    tinydb.putBoolean("ocid_downloaded", true);
                } else if ("Timeout".equals(result)) {
                    Helpers.msgLong(mAppContext, mAppContext.getString(R.string.download_timed_out));
                } else {
                    Helpers.msgLong(mAppContext, mAppContext.getString(R.string.error_retrieving_opencellid_data));
                }
                break;

            case DBE_DOWNLOAD_REQUEST_FROM_MAP:
                if ("Successful".equals(result)) {
                    if (mDbAdapter.populateDBeImport(realm)) {
                        Intent intent = new Intent(MapFragment.updateOpenCellIDMarkers);
                        LocalBroadcastManager.getInstance(mAppContext).sendBroadcast(intent);
                        Helpers.msgShort(mAppContext, mAppContext.getString(R.string.opencellid_data_successfully_received_markers_updated));

                        realm.executeTransaction(mDbAdapter.checkDBe());
                        tinydb.putBoolean("ocid_downloaded", true);
                    }
                } else if ("Timeout".equals(result)) {
                    Helpers.msgLong(mAppContext, mAppContext.getString(R.string.download_timed_out));
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
        }
        if (mListener != null) {
            if ("Successful".equals(result)) {
                mListener.onAsyncTaskSucceeded();
            } else {
                mListener.onAsyncTaskFailed(result);
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
        InjectionAppCompatActivity lActivity = getActivity();
        if (BuildConfig.DEBUG && lActivity == null) {
            log.verbose("BaseTask showHideMapProgressBar() activity is null");
        }

        if (lActivity != null) {
            Fragment myFragment = lActivity.getSupportFragmentManager().findFragmentByTag(String.valueOf(DrawerMenu.ID.MAIN.ALL_CURRENT_CELL_DETAILS));
            if (myFragment instanceof MapFragment) {
                ((MapFragment) myFragment).setRefreshActionButtonState(pFlag);
            }
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

    /**
     * The interface to be implemented by the caller of RequestTask so it can perform contextual
     * actions once the async task is completed.
     *
     * E.g. rechecking current cell in the newly updated database after OCID download.
     */
    public interface AsyncTaskCompleteListener {
        void onAsyncTaskSucceeded();
        void onAsyncTaskFailed(String result);
    }
}

/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.utils;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.secupwn.aimsicd.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import au.com.bytecode.opencsv.CSVReader;
import io.freefair.android.injection.annotation.Inject;
import io.freefair.android.injection.app.InjectionAppCompatActivity;
import io.freefair.android.util.logging.Logger;
import io.realm.Realm;
import lombok.Cleanup;

/**
 * Description:
 * <p/>
 * This class is the request handler for Importing data from OpenCellID's cell_towers.csv or
 * gzipped cell_towers.csv.gz.
 * Format of the file slightly differs from the OpenCellID's so it is converted on the
 * fly and filtered by MCC, MNC, location.
 * <p/>
 */
public class ImportTask extends BaseAsyncTask<String, Integer, String> {

    @Inject
    private Logger log;

    private RealmHelper mDbAdapter;
    private Context mAppContext;
    private final Uri importFile;
    private final int mobileCountryCode;
    private final int mobileNetworkCode;
    private final GeoLocation currentLocation;
    private final int locationRadius;

    private AsyncTaskCompleteListener mListener;

    public static final double EARTH_RADIUS = 6371.01;

    /**
     * @param context           App context
     * @param importFile        URI pointing to the file cell_towers.csv or cell_towers.csv.gz
     * @param mobileCountryCode MCC filter
     * @param mobileNetworkCode MNC filter
     * @param currentLocation   GPS location of cell
     * @param locationRadius    filtering radius
     * @param listener          Allows the caller of RequestTask to implement success/fail callbacks
     */
    public ImportTask(InjectionAppCompatActivity context,
                      Uri importFile,
                      int mobileCountryCode, int mobileNetworkCode,
                      GeoLocation currentLocation, int locationRadius,
                      AsyncTaskCompleteListener listener) {
        super(context);
        this.importFile = importFile;
        this.mobileCountryCode = mobileCountryCode;
        this.mobileNetworkCode = mobileNetworkCode;
        this.currentLocation = currentLocation;
        this.locationRadius = locationRadius;
        this.mAppContext = context.getApplicationContext();
        this.mDbAdapter = new RealmHelper(mAppContext);
        this.mListener = listener;
    }

    /**
     * Imports data from cell_towers.csv
     * <p/>
     * <blockquote>
     * opencellid.csv layout:
     * lat,lon,mcc,mnc,lac,cellid,averageSignalStrength,range,samples,changeable,radio,rnc,cid,psc,
     * tac,pci,sid,nid,bid
     * <p/>
     * example:
     * 52.201454,21.065345,260,2,58140,42042781,-59,1234,3,1,UMTS,641,34205,,,,
     * <p/>
     * cell_towers.csv layout:
     * radio,mcc,net,area,cell,unit,lon,lat,range,samples,changeable,created,updated,averageSignal
     * 0 radio
     * 1 mcc
     * 2 net (mnc)
     * 3 area (lac)
     * 4 cell (long)
     * 5 unit
     * 6 lon
     * 7 lat
     * 8 range
     * 9 samples
     * 10 changeable
     * 11 created
     * 12 updated
     * 13 averageSignal
     * <p/>
     * example:
     * UMTS,260,2,58140,42042781,,21.03006,52.207811,21,2,1,1379428153,1458591497,-92
     * </blockquote>
     */
    @Override
    protected String doInBackground(String... commandString) {

        try {
            @Cleanup Realm realm = Realm.getDefaultInstance();

            Long elapsedSeconds = System.currentTimeMillis() / 1000;

            // Prepare filtering values
            final String mccFilter = String.valueOf(mobileCountryCode);
            final String mncFilter = String.valueOf(mobileNetworkCode);

            long progress = 0;
            long failedRecords = 0;

            CSVReader csvReader = null;
            try {
                String next[];

                csvReader = new CSVReader(createFileReader());
                csvReader.readNext(); // skip header

                String[] opencellid_csv = new String[14];
                while ((next = csvReader.readNext()) != null) {
                    if (next.length < 14) {
                        log.warn("Not enough values in string: " + Arrays.toString(next));
                        ++failedRecords;
                        continue;
                    }
                    if (!next[1].equals(mccFilter) || !next[2].equals(mncFilter)) {
                        continue;
                    }
                    if (next[6].isEmpty() || next[7].isEmpty()) {
                        continue;
                    }
                    GeoLocation location = GeoLocation.fromDegrees(Double.parseDouble(next[7]),
                            Double.parseDouble(next[6]));
                    if (location.distanceTo(currentLocation, EARTH_RADIUS) > locationRadius) {
                        continue;
                    }

                    try {
                        // set non-existent range, avgSignal, etc to "0" so they
                        // will be possibly filtered by checkDBe
                        opencellid_csv[0] = next[7]; // lat
                        opencellid_csv[1] = next[6]; // lon
                        opencellid_csv[2] = next[1]; // mcc
                        opencellid_csv[3] = next[2]; // mnc
                        opencellid_csv[4] = next[3]; // lac
                        opencellid_csv[5] = next[4]; // cellid, long
                        opencellid_csv[6] = stringOrZero(next[13]); // averageSignalStrength
                        opencellid_csv[7] = stringOrZero(next[8]); // range
                        opencellid_csv[8] = stringOrZero(next[9]); // samples
                        opencellid_csv[9] = stringOrZero(next[10]); // changeable
                        opencellid_csv[10] = next[0]; // radio
                        opencellid_csv[11] = null; // rnc, not used
                        opencellid_csv[12] = null; // cid, not used
                        opencellid_csv[13] = null; // psc, not present

                        Date dateCreated = dateOrNow(next[11]);
                        Date dateUpdated = dateOrNow(next[12]);

                        mDbAdapter.addCSVRecord(realm, opencellid_csv, dateCreated, dateUpdated);
                        ++progress;

                    } catch (NumberFormatException e) {
                        log.warn("Problem parsing a record: " + Arrays.toString(opencellid_csv), e);
                        ++failedRecords;
                    }

                    if ((progress % 100) == 0) {
                        log.debug("Imported records for now: " + String.valueOf(progress));
                        // do not know progress because determining line count in gzipped
                        // multi-gigabyte file is slow
                        //publishProgress((int) progress, (int) totalRecords);
                    }
                    if ((progress % 1000) == 0) {
                        try {
                            Thread.sleep(1000); // wait 1 second to allow user to see progress bar.
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            } finally {
                if (csvReader != null) {
                    csvReader.close();
                }
            }
            elapsedSeconds = (System.currentTimeMillis() / 1000) - elapsedSeconds;
            log.debug("Importing took " + String.valueOf(elapsedSeconds) + " seconds");
            log.debug("Imported records: " + String.valueOf(progress));
            log.debug("Failed records: " + String.valueOf(failedRecords));

            return "Successful";

        } catch (IOException e) {
            log.warn("Problem reading data from CSV", e);
            return null;
        }
    }

    /**
     * Creates a new reader for optionally gziped file
     */
    @NonNull
    private Reader createFileReader() throws IOException {
        String type = mAppContext.getContentResolver().getType(importFile);
        boolean isGzip = type != null && type.equals("application/octet-stream") &&
                importFile.toString().endsWith(".gz");
        log.info("Importing " + (isGzip ? "gzipped file" : "plain-text file") + ": " + importFile);

        InputStream fileStream = mAppContext.getContentResolver().openInputStream(importFile);
        if (fileStream == null) {
            throw new IOException("File cannot be opened");
        }
        if (isGzip) {
            fileStream = new FixedGZIPInputStream(new GZIPInputStream(fileStream));
        }
        return new InputStreamReader(fileStream);
    }

    @NonNull
    private static Date dateOrNow(String timestamp) {
        return timestamp == null || timestamp.isEmpty() ? new Date() : new Date(Long.valueOf(timestamp) * 1000);
    }

    private static String stringOrZero(String s) {
        return s == null || s.isEmpty() ? "0" : s;
    }

    /**
     * This is where we:
     * <ol>
     * <li>Check the success for data import</li>
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

        // if `result` is null, it will evaluate to false, no need to check for null
        if ("Successful".equals(result)) {

            Helpers.msgShort(mAppContext, mAppContext.getString(R.string.celltowers_data_successfully_imported));

            realm.executeTransaction(mDbAdapter.checkDBe());
            tinydb.putBoolean("ocid_downloaded", true);
        } else {
            Helpers.msgLong(mAppContext, mAppContext.getString(R.string.error_importing_celltowers_data));
        }

        if (mListener != null) {
            if ("Successful".equals(result)) {
                mListener.onAsyncTaskSucceeded();
            } else {
                mListener.onAsyncTaskFailed(result);
            }
        }
    }

    /**
     * The interface to be implemented by the caller of ImportTask so it can perform contextual
     * actions once the async task is completed.
     * <p/>
     * E.g. rechecking current cell in the newly updated database after OCID imported.
     */
    public interface AsyncTaskCompleteListener {
        void onAsyncTaskSucceeded();

        void onAsyncTaskFailed(String result);
    }
}

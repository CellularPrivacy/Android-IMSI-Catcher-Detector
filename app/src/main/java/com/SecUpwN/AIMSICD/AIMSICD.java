/* Android IMSI Catcher Detector
 *      Copyright (C) 2014
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You may obtain a copy of the License at
 *      https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/LICENSE
 */

package com.SecUpwN.AIMSICD;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.SecUpwN.AIMSICD.service.AimsicdService;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

public class AIMSICD extends Activity {

    private final String TAG = "AIMSICD";

    private final Context mContext = this;
    private boolean mBound;
    private SharedPreferences prefs;
    private AIMSICDDbAdapter dbHelper;

    private AimsicdService mAimsicdService;

    //Back press to exit timer
    private long mLastPress = 0;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Bind to LocalService
        Intent intent = new Intent(this, AimsicdService.class);
        //Start Service before binding to keep it resident when activity is destroyed
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        //Create DB Instance
        dbHelper = new AIMSICDDbAdapter(mContext);

        prefs = mContext.getSharedPreferences(
                AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        final String KEY_KILL_SERVICE = mContext.getString(R.string.pref_killservice_key);
        boolean killService = prefs.getBoolean(KEY_KILL_SERVICE, false);
        if (killService) {
            Intent intent = new Intent(this, AimsicdService.class);
            stopService(intent);
        }
    }

    /**
     * Service Connection to bind the activity to the service
     */
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mAimsicdService = ((AimsicdService.AimscidBinder) service).getService();
            mBound = true;
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG, "Service Disconnected");
            mBound = false;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
        invalidateOptionsMenu();
    }

    private void updateUI() {
        TextView content;
        TableLayout tableLayout;
        TableRow tr;
        if (mBound) {
            int netID = mAimsicdService.getNetID(true);
            switch (mAimsicdService.getPhoneID())
            {
                case TelephonyManager.PHONE_TYPE_GSM: {
                    tableLayout = (TableLayout) findViewById(R.id.cdmaView);
                    tableLayout.setVisibility(View.INVISIBLE);
                    tr = (TableRow) findViewById(R.id.gsm_cellid);
                    tr.setVisibility(View.VISIBLE);
                    content = (TextView) findViewById(R.id.network_lac);
                    content.setText(mAimsicdService.getLAC(true));
                    content = (TextView) findViewById(R.id.network_cellid);
                    content.setText(mAimsicdService.getCellId());
                    break;
                }
                case TelephonyManager.PHONE_TYPE_CDMA:
                {
                    tableLayout = (TableLayout) findViewById(R.id.cdmaView);
                    tableLayout.setVisibility(View.VISIBLE);
                    tr = (TableRow) findViewById(R.id.gsm_cellid);
                    tr.setVisibility(View.INVISIBLE);
                    content = (TextView) findViewById(R.id.network_netid);
                    content.setText(mAimsicdService.getLAC(true));
                    content = (TextView) findViewById(R.id.network_sysid);
                    content.setText(mAimsicdService.getSID());
                    content = (TextView) findViewById(R.id.network_baseid);
                    content.setText(mAimsicdService.getCellId());
                    break;
                }
            }

            if (mAimsicdService.getNetID(true) == TelephonyManager.NETWORK_TYPE_LTE) {
                content = (TextView) findViewById(R.id.network_lte_timing_advance);
                content.setText(mAimsicdService.getLteTimingAdvance());
                tr = (TableRow) findViewById(R.id.lte_timing_advance);
                tr.setVisibility(View.VISIBLE);
            } else {
                tr = (TableRow) findViewById(R.id.lte_timing_advance);
                tr.setVisibility(View.GONE);
            }

            content = (TextView) findViewById(R.id.sim_country);
            content.setText(mAimsicdService.getSimCountry(false));
            content = (TextView) findViewById(R.id.sim_operator_id);
            content.setText(mAimsicdService.getSimOperator(false));
            content = (TextView) findViewById(R.id.sim_operator_name);
            content.setText(mAimsicdService.getSimOperatorName(false));
            content = (TextView) findViewById(R.id.sim_imsi);
            content.setText(mAimsicdService.getSimSubs(false));
            content = (TextView) findViewById(R.id.sim_serial);
            content.setText(mAimsicdService.getSimSerial(false));


            content = (TextView) findViewById(R.id.device_type);
            content.setText(mAimsicdService.getPhoneType(false));
            content = (TextView) findViewById(R.id.device_imei);
            content.setText(mAimsicdService.getIMEI(false));
            content = (TextView) findViewById(R.id.device_version);
            content.setText(mAimsicdService.getIMEIv(false));
            content = (TextView) findViewById(R.id.device_number);
            content.setText(mAimsicdService.getPhoneNumber(false));
            content = (TextView) findViewById(R.id.network_name);
            content.setText(mAimsicdService.getNetworkName(false));
            content = (TextView) findViewById(R.id.network_code);
            content.setText(mAimsicdService.getSmmcMcc(false));
            content = (TextView) findViewById(R.id.network_type);
            content.setText(mAimsicdService.getNetworkTypeName(netID, false));

            content = (TextView) findViewById(R.id.data_activity);
            content.setText(mAimsicdService.getActivityDesc(netID));
            content = (TextView) findViewById(R.id.data_status);
            content.setText(mAimsicdService.getStateDesc(netID));
            content = (TextView) findViewById(R.id.network_roaming);
            content.setText(mAimsicdService.isRoaming());

            Log.i(TAG, "**** AIMSICD ****");
            Log.i(TAG, "Device type   : " + mAimsicdService.getPhoneType(false));
            Log.i(TAG, "Device IMEI   : " + mAimsicdService.getIMEI(false));
            Log.i(TAG, "Device version: " + mAimsicdService.getIMEIv(false));
            Log.i(TAG, "Device num    : " + mAimsicdService.getPhoneNumber(false));
            Log.i(TAG, "Network type  : " + mAimsicdService.getNetworkTypeName(netID, false));
            Log.i(TAG, "Network CellID: " + mAimsicdService.getCellId());
            Log.i(TAG, "Network LAC   : " + mAimsicdService.getLAC(false));
            Log.i(TAG, "Network code  : " + mAimsicdService.getSmmcMcc(false));
            Log.i(TAG, "Network name  : " + mAimsicdService.getNetworkName(false));
            Log.i(TAG, "Roaming       : " + mAimsicdService.isRoaming());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mTrackCell = menu.findItem(R.id.track_cell);
        MenuItem mTrackSignal = menu.findItem(R.id.track_signal);
        MenuItem mTrackLocation = menu.findItem(R.id.track_location);
        MenuItem mTrackFemtocell = menu.findItem(R.id.track_femtocell);

        if (mAimsicdService.TrackingCell) {
            mTrackCell.setTitle(R.string.untrack_cell);
            mTrackCell.setIcon(R.drawable.track_cell);
        } else {
            mTrackCell.setTitle(R.string.track_cell);
            mTrackCell.setIcon(R.drawable.untrack_cell);
        }

        if (mAimsicdService.TrackingSignal) {
            mTrackSignal.setTitle(R.string.untrack_signal);
            mTrackSignal.setIcon(R.drawable.ic_action_network_cell);
        } else {
            mTrackSignal.setTitle(R.string.track_signal);
            mTrackSignal.setIcon(R.drawable.ic_action_network_cell_not_tracked);
        }

        if (mAimsicdService.TrackingLocation) {
            mTrackLocation.setTitle(R.string.untrack_location);
            mTrackLocation.setIcon(R.drawable.ic_action_location_found);
        } else {
            mTrackLocation.setTitle(R.string.track_location);
            mTrackLocation.setIcon(R.drawable.ic_action_location_off);
        }

        if (mAimsicdService.TrackingFemtocell) {
            mTrackFemtocell.setTitle(R.string.untrack_femtocell);
            mTrackFemtocell.setIcon(R.drawable.ic_action_network_cell);
        } else {
            mTrackFemtocell.setTitle(R.string.track_femtocell);
            mTrackFemtocell.setIcon(R.drawable.ic_action_network_cell_not_tracked);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.track_cell:
                trackcell();
                invalidateOptionsMenu();
                return true;
            case R.id.track_signal:
                tracksignal();
                invalidateOptionsMenu();
                return true;
            case R.id.track_location:
                tracklocation();
                invalidateOptionsMenu();
                return true;
            case R.id.track_femtocell:
                trackFemtocell();
                invalidateOptionsMenu();
                return true;
            case R.id.show_map:
                showmap();
                return true;
            case R.id.view_db:
                intent = new Intent(this, DbViewer.class);
                startActivity(intent);
                return true;
            case R.id.preferences:
                intent = new Intent(this, PrefActivity.class);
                startActivity(intent);
                return true;
            case R.id.export_database:
                dbHelper.exportDB();
                return true;
            case R.id.update_opencelldata:
                double[] loc = mAimsicdService.getLastLocation();
                getOpenCellData(loc[0], loc[1]);
                return true;
            case R.id.app_exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Exit application if back pressed twice
     */
    @Override
    public void onBackPressed() {
        Toast onBackPressedToast = Toast.makeText(this, R.string.press_once_again_to_exit, Toast.LENGTH_SHORT);
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastPress > 5000) {
            onBackPressedToast.show();
            mLastPress = currentTime;
        } else {
            onBackPressedToast.cancel();
            super.onBackPressed();
            finish();
        }
    }

    /**
     * Show the Map Viewer Activity
     */
    private void showmap() {
        Intent myIntent = new Intent(this, MapViewer.class);
        startActivity(myIntent);
    }

    /**
     * Signal Strength Tracking - Enable/Disable
     */
    private void tracksignal() {
        if (mAimsicdService.TrackingSignal) {
            mAimsicdService.setSignalTracking(false);
        } else {
            mAimsicdService.setSignalTracking(true);
        }
    }

    /**
     * Cell Information Tracking - Enable/Disable
     */
    private void trackcell() {
        if (mAimsicdService.TrackingCell) {
            mAimsicdService.setCellTracking(false);
        } else {
            mAimsicdService.setCellTracking(true);
        }
    }

    /**
     * Location Information Tracking - Enable/Disable
     */
    private void tracklocation() {
        if (mAimsicdService.TrackingLocation) {
            mAimsicdService.setLocationTracking(false);
        } else {
            mAimsicdService.setLocationTracking(true);
        }
    }

    /**
     * FemtoCell Detection (CDMA Phones ONLY) - Enable/Disable
     */
    private void trackFemtocell() {
        if (mAimsicdService.TrackingFemtocell) {
            mAimsicdService.stopTrackingFemto();
        } else {
            mAimsicdService.startTrackingFemto();
        }
    }

    /**
     * Requests Cell data from OpenCellID.org, calculating a 100 mile bounding radius
     * and requesting all Cell ID information in that area.
     *
     * @param lat Latitude of current location
     * @param lng Longitude of current location
     */
    private void getOpenCellData(double lat, double lng) {
        if (isNetAvailable(this)) {
            double earthRadius = 6371.01;

            //New GeoLocation object to find bounding Coordinates
            GeoLocation currentLoc = GeoLocation.fromDegrees(lat, lng);

            //Calculate the Bounding Coordinates in a 50 mile radius
            //0 = min 1 = max
            GeoLocation[] boundingCoords = currentLoc.boundingCoordinates(100, earthRadius);
            String boundParameter;

            //Request OpenCellID data for Bounding Coordinates
            boundParameter = String.valueOf(boundingCoords[0].getLatitudeInDegrees()) + ","
                    + String.valueOf(boundingCoords[0].getLongitudeInDegrees()) + ","
                    + String.valueOf(boundingCoords[1].getLatitudeInDegrees()) + ","
                    + String .valueOf(boundingCoords[1].getLongitudeInDegrees());

            String urlString = "http://www.opencellid.org/cell/getInArea?key=24c66165-9748-4384-ab7c-172e3f533056"
                    + "&BBOX=" + boundParameter
                    + "&format=csv";

            new RequestTask().execute(urlString);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.no_network_connection_title)
                    .setMessage(R.string.no_network_connection_message);
            builder.create().show();
        }
    }

    /**
     * Checks Network connectivity is available to download OpenCellID data
     *
     */
    private Boolean isNetAvailable(Context context)  {

        try{
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobileInfo =
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifiInfo.isConnected() || mobileInfo.isConnected()) {
                return true;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Parses the downloaded CSV from OpenCellID and adds Map Marker to identify known
     * Cell ID's
     *
     * @param fileName Name of file downloaded from OpenCellID
     */
    private void parseOpenCellID (String fileName) {

        File file = new File(fileName);
        try {
            CSVReader csvReader = new CSVReader(new FileReader(file));
            List<String[]> csvCellID = csvReader.readAll();


            for (int i=1; i<csvCellID.size(); i++)
            {
                //Insert details into OpenCellID Database
                long result =
                        dbHelper.insertOpenCell(Double.parseDouble(csvCellID.get(i)[0]),
                                Double.parseDouble(csvCellID.get(i)[1]),
                                Integer.parseInt(csvCellID.get(i)[2]), Integer.parseInt(csvCellID.get(i)[3]),
                                Integer.parseInt(csvCellID.get(i)[4]), Integer.parseInt(csvCellID.get(i)[5]),
                                Integer.parseInt(csvCellID.get(i)[6]), Integer.parseInt(csvCellID.get(i)[7]));
                if (result == -1)
                {
                    Log.e(TAG, "Error inserting OpenCellID database value");
                }
            }


        } catch (Exception e) {
            Log.e (TAG, "Error parsing OpenCellID data - " + e.getMessage());
        }

    }

    /**
     * Runs the request to download OpenCellID data in an AsyncTask
     * preventing the application from becoming unresponsive whilst
     * waiting for a response and download from the server
     *
     */
    private class RequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //Do anything with response..
            if (result != null) {
                if (Helpers.isSdWritable()) {
                    try {
                        File dir = new File(
                                Environment.getExternalStorageDirectory() + "/AIMSICD/OpenCellID/");
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        Time today = new Time(Time.getCurrentTimezone());
                        today.setToNow();
                        String fileName = Environment.getExternalStorageDirectory()
                                + "/AIMSICD/OpenCellID/opencellid.csv";
                        File file = new File(dir, "opencellid.csv");

                        FileOutputStream fOut = new FileOutputStream(file);
                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                        myOutWriter.append(result);
                        myOutWriter.close();
                        fOut.close();
                        Helpers.sendMsg(mContext, "OpenCellID data successfully received");
                        parseOpenCellID(fileName);
                    } catch (Exception e) {
                        Log.e (TAG, "Write OpenCellID response - " + e.getMessage());
                    }
                }
            }
        }
    }
}

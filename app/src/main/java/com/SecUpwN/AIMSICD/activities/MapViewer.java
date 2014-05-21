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

package com.SecUpwN.AIMSICD.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.utils.GeoLocation;
import com.SecUpwN.AIMSICD.utils.Helpers;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import au.com.bytecode.opencsv.CSVReader;

public class MapViewer extends FragmentActivity implements OnSharedPreferenceChangeListener {
    private final String TAG = "AIMSICD_MapViewer";

    private GoogleMap mMap;

    private AIMSICDDbAdapter mDbHelper;
    private Context mContext;
    private LatLng loc = null;
    private SharedPreferences prefs;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Starting MapViewer");
        super.onCreate(savedInstanceState);

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.error_google_play_services_message)
                    .setTitle(R.string.error_google_play_services_title);
            builder.create().show();
            finish();
        }

        setContentView(R.layout.map);
        setUpMapIfNeeded();

        mDbHelper = new AIMSICDDbAdapter(this);
        loadEntries();
        mContext = this;
        String mapTypePref = getResources().getString(R.string.pref_map_type_key);
        prefs = mContext.getSharedPreferences(
                AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
        if (prefs.contains(mapTypePref)) {
            int mapType = Integer.parseInt(prefs.getString(mapTypePref, "0"));
            switch (mapType) {
                case 0:
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    break;
                case 1:
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    break;
                case 2:
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    break;
                case 3:
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        prefs = this.getSharedPreferences(
                AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Initialises the Map and sets initial options
     *
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                // The Map is verified. It is now safe to manipulate the map.
                UiSettings uiSettings = mMap.getUiSettings();
                uiSettings.setZoomControlsEnabled(true);
                uiSettings.setCompassEnabled(true);
                uiSettings.setMyLocationButtonEnabled(true);
                uiSettings.setScrollGesturesEnabled(true);
                uiSettings.setZoomGesturesEnabled(true);
                uiSettings.setTiltGesturesEnabled(true);
                uiSettings.setRotateGesturesEnabled(true);
                mMap.setMyLocationEnabled(true);
            } else {
                Helpers.sendMsg(this, "Unable to create map!");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_viewer_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.map_preferences:
                Intent intent = new Intent(this, MapPrefActivity.class);
                startActivity(intent);
                return true;
            case R.id.get_opencellid:
            {
                Helpers.sendMsg(this, "Contacting OpenCellID.org for data...");
                Location mLocation = mMap.getMyLocation();
                if (mLocation != null) {
                    getOpenCellData(mLocation.getLatitude(), mLocation.getLongitude());
                } else if (loc != null) {
                    getOpenCellData(loc.latitude, loc.longitude);
                } else {
                    double[] lastKnown = getLastLocation();
                    if (lastKnown != null) {
                        getOpenCellData(lastKnown[0], lastKnown[1]);
                    } else {
                        Helpers.sendMsg(this, "Error finding your location!");
                    }
                }
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Loads Signal Strength Database details to plot on the map,
     * only entries which have a location (lon, lat) are used.
     *
     */
    private void loadEntries() {
        int SIGNAL_SIZE_RATIO = 15;
        double dlat;
        double dlng;
        int net;
        int signal;
        int color;
        int cellID;
        CircleOptions circleOptions;
        mDbHelper.open();
        Cursor c = mDbHelper.getCellData();
        if (c.moveToFirst()) {
            do {
                cellID = c.getInt(0);
                net = c.getInt(2);
                dlat = Double.parseDouble(c.getString(3));
                dlng = Double.parseDouble(c.getString(4));
                if (dlat == 0.0 && dlng == 0.0)
                    continue;
                signal = c.getInt(5);
                if (signal <= 0) {
                    signal = 20;
                }

                if ((dlat != 0.0) || (dlng != 0.0)) {
                    loc = new LatLng(dlat, dlng);
                    switch (net) {
                        case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                            color = 0xF0F8FF;
                            break;
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                            color = 0xA9A9A9;
                            break;
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                            color = 0x87CEFA;
                            break;
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                            color = 0x7CFC00;
                            break;
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                            color = 0xFF6347;
                            break;
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                            color = 0xFF00FF;
                            break;
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                            color = 0x238E6B;
                            break;
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                            color = 0x8A2BE2;
                            break;
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                            color = 0xFF69B4;
                            break;
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                            color = 0xFFFF00;
                            break;
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                            color = 0x7CFC00;
                            break;
                        default:
                            color = 0xF0F8FF;
                            break;
                    }

                    // Add Signal radius circle based on signal strength
                    circleOptions = new CircleOptions()
                            .center(loc)
                            .radius(signal * SIGNAL_SIZE_RATIO)
                            .fillColor(color)
                            .strokeColor(color)
                            .visible(true);

                    mMap.addCircle(circleOptions);

                    // Add map marker for CellID
                    mMap.addMarker(new MarkerOptions()
                            .position(loc)
                            .draggable(false)
                            .title("CellID - " + cellID));
                }

            } while (c.moveToNext());
            c.close();
        } else {
            Helpers.msgShort(this, "No tracked locations found to overlay on map.");
        }

        if (loc != null && (loc.latitude != 0.0 && loc.longitude != 0.0)) {
            CameraPosition POSITION =
                    new CameraPosition.Builder().target(loc)
                            .zoom(16)
                            .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(POSITION));
        } else {
            // Try and find last known location and zoom there
            double[] d = getLastLocation();
            if (d[0] != 0.0 && d[1] != 0.0) {
                loc = new LatLng(d[0], d[1]);
                CameraPosition POSITION =
                        new CameraPosition.Builder().target(loc)
                                .zoom(16)
                                .build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(POSITION));
            } else {
                //Use Mcc to move camera to an approximate location near Countries Capital
                TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
                int mcc = Integer.parseInt(tm.getNetworkOperator().substring(0, 3));
                d = mDbHelper.getDefaultLocation(mcc);
                loc = new LatLng(d[0], d[1]);
                CameraPosition POSITION =
                        new CameraPosition.Builder().target(loc)
                                .zoom(13)
                                .build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(POSITION));
            }
        }

        //Check if OpenCellID data exists and if so load this now
        c = mDbHelper.getOpenCellIDData();
        if (c.moveToFirst()) {
            do {
                cellID = c.getInt(0);
                dlat = Double.parseDouble(c.getString(4));
                dlng = Double.parseDouble(c.getString(5));
                loc = new LatLng (dlat, dlng);
                int lac = c.getInt(1);
                int mcc = c.getInt(2);
                int mnc = c.getInt(3);
                int samples = c.getInt(7);
                // Add map marker for CellID
                mMap.addMarker(new MarkerOptions()
                        .position(loc)
                        .draggable(false)
                        .title("CellID - " + cellID))
                        .setSnippet("LAC: " + lac
                        + "\nMCC: " + mcc
                        + "\nMNC: " + mnc
                        + "\nSamples: " + samples);

            } while (c.moveToNext());
        }
    }

    /**
     * Attempts to retrieve the last known location from the device
     *
     */
    private double[] getLastLocation() {
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        /* Loop over the array backwards, and if you get an accurate location, then break out the loop*/
        Location l = null;

        for (int i=providers.size()-1; i>=0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }

        double[] gps = new double[2];
        if (l != null) {
            gps[0] = l.getLatitude();
            gps[1] = l.getLongitude();
        }
        return gps;

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
            if (wifiInfo != null && mobileInfo != null) {
                return wifiInfo.isConnected() || mobileInfo.isConnected();
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
              loc =  new LatLng(Double.parseDouble(csvCellID.get(i)[0]), Double.parseDouble(csvCellID.get(i)[1]));

                // Add map marker for CellID
                mMap.addMarker(new MarkerOptions()
                        .position(loc)
                        .draggable(false)
                        .title("CellID - " + csvCellID.get(i)[5]))
                        .setSnippet("LAC: " + csvCellID.get(i)[4]
                                + "\nMCC: " + csvCellID.get(i)[2]
                                + "\nMNC: " + csvCellID.get(i)[3]
                                + "\nSamples: " + csvCellID.get(i)[7]);

                //Insert details into OpenCellID Database
                long result =
                mDbHelper.insertOpenCell(Double.parseDouble(csvCellID.get(i)[0]),
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

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        final String KEY_MAP_TYPE = getBaseContext().getString(R.string.pref_map_type_key);

        if (key.equals(KEY_MAP_TYPE)) {
            int item = Integer.parseInt(sharedPreferences.getString(key, "0"));
            switch (item) {
                case 0:
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    break;
                case 1:
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    break;
                case 2:
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    break;
                case 3:
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    break;
            }
        }
    }
}

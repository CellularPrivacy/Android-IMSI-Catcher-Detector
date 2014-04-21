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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.SecUpwN.AIMSICD.cmdprocessor.Helpers;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import au.com.bytecode.opencsv.CSVReader;

public class MapViewer extends FragmentActivity {
    private final String TAG = "AIMSICD_MapViewer";

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private AIMSICDDbAdapter mDbHelper;
    private Context mContext;
    private LatLng loc = null;

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
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mMap == null) {
            FragmentManager fmanager = getSupportFragmentManager();
            Fragment fragment = fmanager.findFragmentById(R.id.map);
            SupportMapFragment supportmapfragment = (SupportMapFragment) fragment;
            mMap = supportmapfragment.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                // The Map is verified. It is now safe to manipulate the map.
                mUiSettings = mMap.getUiSettings();
                mUiSettings.setZoomControlsEnabled(true);
                mUiSettings.setCompassEnabled(true);
                mUiSettings.setMyLocationButtonEnabled(true);
                mUiSettings.setScrollGesturesEnabled(true);
                mUiSettings.setZoomGesturesEnabled(true);
                mUiSettings.setTiltGesturesEnabled(true);
                mUiSettings.setRotateGesturesEnabled(true);
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
            case R.id.map_type:
                final AlertDialog.Builder menuAlert = new
                        AlertDialog.Builder(this);
                final String[] menuList = {
                        "Normal", "Hybrid", "Satellite", "Terrain"
                };
                menuAlert.setTitle("Map Type");
                menuAlert.setItems(menuList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
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
                });
                AlertDialog menuMapType = menuAlert.create();
                menuMapType.show();
                return true;
            case R.id.get_opencellid:
            {
                Location mLocation = mMap.getMyLocation();
                if (mLocation != null) {
                    getOpenCellData(mLocation.getLatitude(), mLocation.getLongitude());
                    Log.i(TAG, "Lat: " + mLocation.getLatitude()
                            + " Long: " + mLocation.getLongitude());
                } else if (loc != null) {
                    getOpenCellData(loc.latitude, loc.longitude);
                } else {
                    double[] lastKnown = getlocation();
                    if (lastKnown != null) {
                        getOpenCellData(lastKnown[0], lastKnown[1]);
                        Log.i(TAG, "Lat: " + lastKnown[0]
                                + " Long: " + lastKnown[1]);
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

    private void loadEntries() {
        int SIGNAL_SIZE_RATIO = 15;
        double dlat;
        double dlng;
        int net;
        int signal;
        int color;
        int cellID;
        mDbHelper.open();
        Cursor c = mDbHelper.getSignalData();
        if (c.moveToFirst()) {

            CircleOptions circleOptions;
            do {
                net = c.getInt(0);
                dlat = Double.parseDouble(c.getString(1));
                dlng = Double.parseDouble(c.getString(2));
                signal = c.getInt(3);
                if (signal > 0) {
                    signal = 20;
                }
                cellID = c.getInt(4);

                if ((dlat != 0.0) || (dlng != 0.0)) {
                    loc = new LatLng(dlat, dlng);
                    Log.i(TAG, "LatLng: " + loc.toString());
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
            if (loc != null) {
                final CameraPosition POSITION =
                        new CameraPosition.Builder().target(loc)
                                .zoom(13)
                                .bearing(320)
                                .tilt(30)
                                .build();
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(POSITION));
            }
        } else {
            Helpers.msgShort(this, "No tracked locations found to overlay on map.");

            // Try and find last known location and zoom there
            GetCurrentLocation();
        }
    }

    private void GetCurrentLocation() {

        double[] d = getlocation();

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(d[0], d[1]), 5));
    }

    public double[] getlocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        Location l = null;
        for (String provider : providers) {
            l = lm.getLastKnownLocation(provider);
            if (l != null)
                break;
        }
        double[] gps = new double[2];

        if (l != null) {
            gps[0] = l.getLatitude();
            gps[1] = l.getLongitude();
        }
        return gps;
    }

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
        }
    }

    public Boolean isNetAvailable(Context context)  {

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
                        .title("CellID - " + csvCellID.get(i)[5]));
            }


        } catch (Exception e) {
            Log.e (TAG, "Error parsing OpenCellID data - " + e.getMessage());
        }

    }

    class RequestTask extends AsyncTask<String, String, String> {

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
                if (Utils.isSdWritable()) {
                    try {
                        File dir = new File(
                                Environment.getExternalStorageDirectory() + "/AIMSICD/OpenCellID/");
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        Time today = new Time(Time.getCurrentTimezone());
                        today.setToNow();
                        String fileName = Environment.getExternalStorageDirectory() + "/AIMSICD/OpenCellID/"
                                + "cellid-" + today.format2445() + ".csv";
                        File file = new File(dir, "cellid-"
                                + today.format2445() + ".csv");

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

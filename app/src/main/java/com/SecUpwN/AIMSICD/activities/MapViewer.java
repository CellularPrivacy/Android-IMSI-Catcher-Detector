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

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.utils.Helpers;

import java.util.HashMap;
import java.util.Map;


public class MapViewer extends FragmentActivity implements OnSharedPreferenceChangeListener {
    private final String TAG = "AIMSICD_MapViewer";

    private GoogleMap mMap;

    private AIMSICDDbAdapter mDbHelper;
    private Context mContext;
    private LatLng loc = null;
    private SharedPreferences prefs;

    private AimsicdService mAimsicdService;
    private boolean mBound;

    private Map<Marker, MarkerData> mMarkerMap = new HashMap<>();

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
        mContext = this;
        mDbHelper = new AIMSICDDbAdapter(mContext);
        // Bind to LocalService
        Intent intent = new Intent(mContext, AimsicdService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        loadEntries();

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
        // Unbind from the service
        if (mBound) {
            mContext.unbindService(mConnection);
            mBound = false;
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
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG, "Service Disconnected");
            mBound = false;
        }
    };

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
                // Setting a custom info window adapter for the google map
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                    // Use default InfoWindow frame
                    @Override
                    public View getInfoWindow(Marker arg0) {
                        return null;
                    }

                    // Defines the contents of the InfoWindow
                    @Override
                    public View getInfoContents(Marker arg0) {

                        TextView tv;

                        // Getting view from the layout file info_window_layout
                        View v = getLayoutInflater().inflate(R.layout.marker_info_window, null);

                        if  (v != null) {
                            final MarkerData data = mMarkerMap.get(arg0);
                            if (data != null) {
                                if (data.openCellID) {
                                    TableRow tr = (TableRow) v.findViewById(R.id.open_cell_label);
                                    tr.setVisibility(View.VISIBLE);
                                }

                                tv = (TextView) v.findViewById(R.id.cell_id);
                                tv.setText(data.cellID);
                                tv = (TextView) v.findViewById(R.id.lat);
                                tv.setText("" + data.lat);
                                tv = (TextView) v.findViewById(R.id.lng);
                                tv.setText("" + data.lng);
                                tv = (TextView) v.findViewById(R.id.mcc);
                                tv.setText(data.mcc);
                                tv = (TextView) v.findViewById(R.id.mnc);
                                tv.setText(data.mnc);
                                tv = (TextView) v.findViewById(R.id.samples);
                                tv.setText(data.samples);
                            }
                        }

                        // Returning the view containing InfoWindow contents
                        return v;
                    }
                });
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
                    Helpers.getOpenCellData(mContext, mLocation.getLatitude(),
                            mLocation.getLongitude());
                } else if (loc != null) {
                    Helpers.getOpenCellData(mContext, loc.latitude, loc.longitude);
                } else {
                    Location lastKnown = mAimsicdService.lastKnownLocation();
                    if (lastKnown != null) {
                        Helpers.getOpenCellData(mContext, lastKnown.getLatitude(),
                                lastKnown.getLongitude());
                    } else {
                        Helpers.sendMsg(this, "Unable to determine your location!");
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
        int lac;
        CircleOptions circleOptions;
        mDbHelper.open();
        Cursor c = mDbHelper.getCellData();
        if (c.moveToFirst()) {
            do {
                cellID = c.getInt(0);
                lac = c.getInt(1);
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
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(loc)
                            .draggable(false)
                            .title("CellID - " + cellID));
                    mMarkerMap.put(marker, new MarkerData("" + cellID, "" + loc.latitude,
                            "" + loc.longitude, "" + lac, "", "", "",false));
                }

            } while (c.moveToNext());
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
            Location lastLoc = mAimsicdService.lastKnownLocation();
            if (lastLoc.getLatitude() != 0.0 && lastLoc.getLongitude() != 0.0) {
                loc = new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude());
                CameraPosition POSITION =
                        new CameraPosition.Builder().target(loc)
                                .zoom(16)
                                .build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(POSITION));
            } else {
                //Use Mcc to move camera to an approximate location near Countries Capital
                TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
                int mcc = Integer.parseInt(tm.getNetworkOperator().substring(0, 3));
                double[] d = mDbHelper.getDefaultLocation(mcc);
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
                lac = c.getInt(1);
                int mcc = c.getInt(2);
                int mnc = c.getInt(3);
                int samples = c.getInt(7);
                // Add map marker for CellID
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(loc)
                        .draggable(false)
                        .title("CellID - " + cellID));

                mMarkerMap.put(marker, new MarkerData("" + cellID, "" + loc.latitude,
                        "" + loc.longitude, "" + lac, "" + mcc, "" + mnc,
                        "" + samples, true));

            } while (c.moveToNext());
        }

        mDbHelper.close();
    }

    public class MarkerData {
        public String cellID;
        public String lat;
        public String lng;
        public String lac;
        public String mcc;
        public String mnc;
        public String samples;
        public boolean openCellID;

        MarkerData(String cell_id, String latitude, String longitude,
                String local_area_code, String mobile_country_code, String mobile_network_code,
                String samples_taken, boolean openCellID_Data) {
            cellID = cell_id;
            lat = latitude;
            lng = longitude;
            lac = local_area_code;
            mcc = mobile_country_code;
            mnc = mobile_network_code;
            samples = samples_taken;
            openCellID = openCellID_Data;
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

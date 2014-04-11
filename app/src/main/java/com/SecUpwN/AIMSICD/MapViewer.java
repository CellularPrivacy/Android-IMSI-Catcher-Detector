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
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.SecUpwN.AIMSICD.cmdprocessor.Helpers;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class MapViewer extends FragmentActivity {
    private final String TAG = "AIMSICD_MapViewer";

    private GoogleMap mMap;
    private AIMSICDDbAdapter mDbHelper;

    private GoogleMapOptions mMapOptions = new GoogleMapOptions();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Starting MapViewer ============");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        setUpMapIfNeeded();

        mMapOptions.mapType(GoogleMap.MAP_TYPE_HYBRID)
                .compassEnabled(true)
                .rotateGesturesEnabled(false)
                .tiltGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.setMyLocationEnabled(true);
        mDbHelper = new AIMSICDDbAdapter(this);
        loadEntries();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }



    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            FragmentManager fmanager = getSupportFragmentManager();
            Fragment fragment = fmanager.findFragmentById(R.id.map);
            SupportMapFragment supportmapfragment = (SupportMapFragment)fragment;
            mMap = supportmapfragment.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap == null) {
                // The Map is verified. It is now safe to manipulate the map.
                Helpers.sendMsg(this, "Unable to create map!");
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void loadEntries() {
        int SIGNAL_SIZE_RATIO = 15;
        double dlat;
        double dlng;
        int net;
        int signal;
        int color;
        mDbHelper.open();
        Cursor c = mDbHelper.getSignalData();
        if (c.moveToFirst()) {
            do {
                net = c.getInt(0);
                dlat = Double.parseDouble(c.getString(1));
                dlng = Double.parseDouble(c.getString(2));
                signal = c.getInt(3);
                if (signal == 0) {
                    signal = 20;
                }

                if ((dlat != 0.0) || (dlng != 0.0)) {
                    switch (net) {
                        case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                            color = R.color.map_unknown;
                            break;
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                            color = R.color.map_gprs;
                            break;
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                            color = R.color.map_edge;
                            break;
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                            color = R.color.map_umts;
                            break;
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                            color = R.color.map_hsdpa;
                            break;
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                            color = R.color.map_hsupa;
                            break;
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                            color = R.color.map_hspa;
                            break;
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                            color = R.color.map_cdma;
                            break;
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                            color = R.color.map_evdo0;
                            break;
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                            color = R.color.map_evdoA;
                            break;
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                            color = R.color.map_1xrtt;
                            break;
                        default:
                            color = R.color.map_default;
                            break;
                    }

                    CircleOptions circleOptions = new CircleOptions()
                            .center(new LatLng((int) (dlat * 1E6),
                                    (int) (dlng * 1E6)))
                            .radius(signal * SIGNAL_SIZE_RATIO)
                            .fillColor(color);

                    mMap.addCircle(circleOptions);
                }

            } while (c.moveToNext());
            c.close();

        } else {
            Helpers.msgShort(this, "No tracked locations found to overlay on map.");
        }
    }

}

 

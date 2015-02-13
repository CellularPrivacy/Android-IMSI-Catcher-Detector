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

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.map.CellTowerGridMarkerClusterer;
import com.SecUpwN.AIMSICD.map.CellTowerMarker;
import com.SecUpwN.AIMSICD.map.MarkerData;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.utils.Cell;
import com.SecUpwN.AIMSICD.utils.GeoLocation;
import com.SecUpwN.AIMSICD.utils.Helpers;
import com.SecUpwN.AIMSICD.utils.RequestTask;

import org.osmdroid.api.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.SimpleLocationOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *  Description:    TODO: add details
 *
 *  Variables:      TODO: add a list of variables that can be tuned (Max/MinZoom factors etc)
 *
 *  Current Issues:
 *
 *      [ ] Map is not immediately updated with the BTS info. It take a "long" time ( >10 seconds)
 *          before map is updated. Any way to shorten this?
 *      [ ] See: #272 #250 #228
 *      [ ] Some pins remain clustered even on the greatest zoom, this is probably
 *          due to over sized icons, or too low zoom level.
 *      [ ] pin icons are too big. We need to reduce pin dot diameter by ~50%
 *      [ ] Need a manual way to add GPS coordinates of current location (see code comments below)
 *      [ ]
 *
 *  Notes:
 *          a) Latest OSM version can use MaxZoomLevel of 21, please see:
 *              https://github.com/osmdroid/osmdroid/issues/49
 *              https://github.com/osmdroid/osmdroid/issues/81
 *              https://code.google.com/p/osmbonuspack/issues/detail?id=102
 *
 *  ChangeLog:
 *
 *      2015-01-22  E:V:A   Changed: setLocationUpdateMinTime:    60000 to 10000 ms
 *                                   setLocationUpdateMinDistance: 1000 to 100 meters
 *      2015-02-12  E:V:A   Added:   mMap.setMaxZoomLevel(19);
 *
 */

public class MapViewerOsmDroid extends BaseActivity implements OnSharedPreferenceChangeListener {

    private final String TAG = "AIMSICD_MapViewer";
    public static final String updateOpenCellIDMarkers = "update_opencell_markers";

    private MapView mMap;
    private AIMSICDDbAdapter mDbHelper;
    private Context mContext;
    private SharedPreferences prefs;
    private AimsicdService mAimsicdService;
    private boolean mBound;
    private boolean isViewingGUI;

    private GeoPoint loc = null;
    private final Map<Marker, MarkerData> mMarkerMap = new HashMap<>();

    private MyLocationNewOverlay mMyLocationOverlay;
    private CompassOverlay mCompassOverlay;
    private ScaleBarOverlay mScaleBarOverlay;
    private CellTowerGridMarkerClusterer mCellTowerGridMarkerClusterer;

    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            loadEntries();
        }

        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            loadEntries();
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Starting MapViewer");
        super.onCreate(savedInstanceState);
        mContext = this;

        setContentView(R.layout.map);
        setUpMapIfNeeded();

        mDbHelper = new AIMSICDDbAdapter(mContext);

        // Bind to LocalService
        Intent intent = new Intent(mContext, AimsicdService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION |
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        isViewingGUI = true;

        prefs = this.getSharedPreferences(
                AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
        prefs.registerOnSharedPreferenceChangeListener(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(updateOpenCellIDMarkers));

        if (!mBound) {
            // Bind to LocalService
            Intent intent = new Intent(mContext, AimsicdService.class);
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

        loadPreferences();
        loadEntries();

        if (mCompassOverlay != null) {
            mCompassOverlay.enableCompass();
        }

        if (mMyLocationOverlay != null) {
            mMyLocationOverlay.enableMyLocation();
        }
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

        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isViewingGUI = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        if (mCompassOverlay != null) {
            mCompassOverlay.disableCompass();
        }

        if (mMyLocationOverlay != null) {
            mMyLocationOverlay.disableMyLocation();
        }
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadOpenCellIDMarkers();
        }
    };

    /**
     * Service Connection to bind the activity to the service
     *
     * This seem to setup the connection and animates the map window movement to the
     * last known location.
     */
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mAimsicdService = ((AimsicdService.AimscidBinder) service).getService();
            mBound = true;

            // setup map
            GeoLocation lastKnown = mAimsicdService.lastKnownLocation();
            if (lastKnown != null) {
                mMap.getController().setZoom(16); // Initial Zoom level
                mMap.getController().animateTo(new GeoPoint(
                        lastKnown.getLatitudeInDegrees(),
                        lastKnown.getLongitudeInDegrees()));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG, "Service Disconnected");
            mBound = false;
        }
    };

    // Load the default map type from preferences
    private void loadPreferences() {
        String mapTypePref = getResources().getString(R.string.pref_map_type_key);
        prefs = mContext.getSharedPreferences(
                AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
        if (prefs.contains(mapTypePref)) {
            int mapType = Integer.parseInt(prefs.getString(mapTypePref, "0"));
            setupMapType(mapType);
        }
    }

    private void setupMapType(int mapType) {
        mMap.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        // There are two other map types (hybrid and satellite), but we don't use them
        // as they are redundant (hybrid) and broken (satellite).
        switch (mapType) {
            case 0:
                mMap.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE); //setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case 1:
                mMap.setTileSource(TileSourceFactory.CYCLEMAP); //.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            default:
                mMap.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE); //setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
        }
    }

    /**
     * Description:     Initialises the Map and sets initial options such as:
     *                      Zoom levels and controls
     *                      Compass
     *                      ScaleBar
     *                      Cluster Pin colors
     *                      Location update settings
     *
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            mMap = (MapView) findViewById(R.id.mapview);

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setBuiltInZoomControls(true);
                mMap.setMultiTouchControls(true);
                mMap.setMinZoomLevel(3);
                mMap.setMaxZoomLevel(19); // Latest OSM can go to 21!

                mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), mMap);

                mScaleBarOverlay = new ScaleBarOverlay(this);
                mScaleBarOverlay.setScaleBarOffset(getResources().getDisplayMetrics().widthPixels / 2, 10);
                mScaleBarOverlay.setCentred(true);

                // Sets cluster pin color
                mCellTowerGridMarkerClusterer = new CellTowerGridMarkerClusterer(MapViewerOsmDroid.this);
                mCellTowerGridMarkerClusterer.setIcon(((BitmapDrawable)mContext.getResources().
                        getDrawable(R.drawable.ic_map_pin_orange)).getBitmap());

                GpsMyLocationProvider imlp = new GpsMyLocationProvider(MapViewerOsmDroid.this.getBaseContext());
                imlp.setLocationUpdateMinDistance(100); // [m]  // Set the minimum distance for location updates
                imlp.setLocationUpdateMinTime(10000);   // [ms] // Set the minimum time interval for location updates
                mMyLocationOverlay = new MyLocationNewOverlay(MapViewerOsmDroid.this.getBaseContext(), imlp, mMap);
                mMyLocationOverlay.setDrawAccuracyEnabled(true);

                mMap.getOverlays().add(mCellTowerGridMarkerClusterer);
                mMap.getOverlays().add(mMyLocationOverlay);
                mMap.getOverlays().add(mCompassOverlay);
                mMap.getOverlays().add(mScaleBarOverlay);

            } else {
                Helpers.msgShort(this, "Unable to create map!");
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
            case R.id.get_opencellid: {
                if (mBound) {
                    GeoLocation lastKnown = mAimsicdService.lastKnownLocation();
                    if (lastKnown != null) {
                        Helpers.msgLong(mContext,
                            "Contacting opencellid.org for data...\nThis may take up to a minute.");
                        Cell cell;
                        cell = mAimsicdService.getCell();
                        cell.setLon(lastKnown.getLongitudeInDegrees());
                        cell.setLat(lastKnown.getLatitudeInDegrees());
                        Helpers.getOpenCellData(mContext, cell, RequestTask.DBE_DOWNLOAD_REQUEST_FROM_MAP);
                        return true;
                    }
                }

                if (loc != null) {
                    Helpers.msgLong(this,
                            "Contacting opencellid.org for data...\nThis may take up to a minute.");
                    Cell cell = new Cell();
                    cell.setLat(loc.getLatitude());
                    cell.setLon(loc.getLongitude());
                    Helpers.getOpenCellData(mContext, cell, RequestTask.DBE_DOWNLOAD_REQUEST_FROM_MAP);
                } else {
                    Helpers.msgLong(mContext,
                        "Unable to determine your last location.\nEnable Location Services and try again.");
                }
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     *  Description:    Loads Signal Strength Database details to plot on the map,
     *                  only entries which have a location (lon, lat) are used.
     *
     *
     */
    private void loadEntries() {

        new AsyncTask<Void,Void,GeoPoint>() {
            @Override
            protected GeoPoint doInBackground(Void... voids) {
                final int SIGNAL_SIZE_RATIO = 15;  // A scale factor to draw BTS Signal circles
                int signal;
                int color;

                mCellTowerGridMarkerClusterer.getItems().clear();
                loadOpenCellIDMarkers();

                LinkedList<CellTowerMarker> items = new LinkedList<>();

                mDbHelper.open();
                Cursor c = null;
                try {
                    // Grab cell data from CELL_TABLE (cellinfo) --> DBi_bts
                    c = mDbHelper.getCellData();
                }catch(IllegalStateException ix) {
                    Log.e(TAG, ix.getMessage(), ix);
                }
                if (c != null && c.moveToFirst()) {
                    do {
                        // The indexing here is that of the Cursor and not the DB table itself
                        final int cellID = c.getInt(0);  // CID
                        final int lac = c.getInt(1);     // LAC
                        final int net = c.getInt(2);     // RAT
                        final int mcc = c.getInt(6);     // MCC
                        final int mnc = c.getInt(7);     // MNC
                        final double dlat = Double.parseDouble(c.getString(3)); // Lat
                        final double dlng = Double.parseDouble(c.getString(4)); // Lon
                        if (dlat == 0.0 && dlng == 0.0) {
                            continue;
                        }
                        signal = c.getInt(5);  // signal
                        // In case of missing or negative signal, set a default fake signal,
                        // so that we can still draw signal circles.  ?
                        if (signal <= 0) {
                            signal = 20;
                        }

                        if ((dlat != 0.0) || (dlng != 0.0)) {
                            loc = new GeoPoint(dlat, dlng);

                            // TODO: write in text what these colors are. It's damn hard to guess!
                            // TODO: Remove if not used!! --E:V:A
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

                            CellTowerMarker ovm = new CellTowerMarker(mContext, mMap,
                                    "Cell ID: " + cellID,
                                    "",  loc,
                                    new MarkerData(
                                            "" + cellID,
                                            "" + loc.getLatitude(),
                                            "" + loc.getLongitude(),
                                            "" + lac,
                                            "" + mcc,
                                            "" + mnc,
                                            "", false)
                            );
                            // The pin of our current position
                            ovm.setIcon(getResources().getDrawable(R.drawable.ic_map_pin_blue));
                            items.add(ovm);


//                    // Add Signal radius circle based on signal strength
//                    circleOptions = new CircleOptions()
//                            .center(loc)
//                            .radius(signal * SIGNAL_SIZE_RATIO)
//                            .fillColor(color)
//                            .strokeColor(color)
//                            .visible(true);
//
//                    mMap.addCircle(circleOptions);
//
//                    // Add map marker for CellID
//                    Marker marker = mMap.addMarker(new MarkerOptions()
//                            .position(loc)
//                            .draggable(false)
//                            .title("CellID - " + cellID));
//                    mMarkerMap.put(marker, new MarkerData("" + cellID, "" + loc.latitude,
//                            "" + loc.longitude, "" + lac, "", "", "", false));
                        }

                    } while (c.moveToNext());
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Helpers.msgLong(MapViewerOsmDroid.this, "No tracked locations found to show on map.");
                        }
                    });
                }

                GeoPoint ret = new GeoPoint(0,0);
                if (mBound) {
                    try {
                        int mcc = mAimsicdService.getCell().getMCC();
                        double[] d = mDbHelper.getDefaultLocation(mcc);
                        ret = new GeoPoint(d[0], d[1]);
                    } catch (Exception e) {
                        Log.e("map", "Error getting default location!", e);
                    }
                }

                c.close();
                mDbHelper.close();

                // plot neighbouring cells
                while (mAimsicdService == null) try { Thread.sleep(100); } catch (Exception e) {}
                List<Cell> nc = mAimsicdService.getCellTracker().updateNeighbouringCells();
                for (Cell cell : nc) {
                    try {
                        loc = new GeoPoint(cell.getLat(), cell.getLon());
                        CellTowerMarker ovm = new CellTowerMarker(mContext,mMap,
                                "Cell ID: " + cell.getCID(),
                                "", loc,
                                new MarkerData(
                                            "" + cell.getCID(),
                                            "" + loc.getLatitude(),
                                            "" + loc.getLongitude(),
                                            "" + cell.getLAC(),
                                            "" + cell.getMCC(),
                                            "" + cell.getMNC(),
                                            "", false));

                        // The pin of other BTS
                        ovm.setIcon(getResources().getDrawable(R.drawable.ic_map_pin_orange));
                        items.add(ovm);
                    } catch (Exception e) {
                        Log.e("map", "Error plotting neighbouring cells", e);
                    }
                }

                mCellTowerGridMarkerClusterer.addAll(items);

                return ret;
            }

            /**
             *  TODO:  We need a manual way to add our own location in case:
             *          a) GPS is jammed or not working
             *          b) WiFi location is not used
             *          c) Default MCC is too far off
             *
             * @param defaultLoc
             */
            @Override
            protected void onPostExecute(GeoPoint defaultLoc) {
                if (loc != null && (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0)) {
                    mMap.getController().setZoom(16);
                    mMap.getController().animateTo(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
                } else {
                    if (mBound) {
                        // Try and find last known location and zoom there
                        GeoLocation lastLoc = mAimsicdService.lastKnownLocation();
                        if (lastLoc != null) {
                            loc = new GeoPoint(lastLoc.getLatitudeInDegrees(),
                                    lastLoc.getLongitudeInDegrees());

                            mMap.getController().setZoom(16);
                            mMap.getController().animateTo(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
                        } else {
                            //Use MCC to move camera to an approximate location near Countries Capital
                            loc = defaultLoc;

                            mMap.getController().setZoom(12);
                            mMap.getController().animateTo(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
                        }
                    }
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // TODO: Consider changing this function name to:  <something else>
    private void loadOpenCellIDMarkers() {
        // Check if OpenCellID data exists and if so load this now
        LinkedList<CellTowerMarker> items = new LinkedList<>();

        // DBe_import tower pins.
        Drawable cellTowerMarkerIcon = getResources().getDrawable(R.drawable.ic_map_pin_green);

        mDbHelper.open();
        Cursor c = mDbHelper.getOpenCellIDData();
        if (c.moveToFirst()) {
            do {
                // The indexing here is that of the Cursor and not the DB table itself:
                // CellID,Lac,Mcc,Mnc,Lat,Lng,AvgSigStr,Samples
                final int cellID = c.getInt(0);
                final int lac = c.getInt(1);
                final int mcc = c.getInt(2);
                final int mnc = c.getInt(3);
                final double dlat = Double.parseDouble(c.getString(4));
                final double dlng = Double.parseDouble(c.getString(5));
                final GeoPoint location = new GeoPoint(dlat, dlng);
                //
                final int samples = c.getInt(7);

                // Add map marker for CellID
                CellTowerMarker ovm = new CellTowerMarker(mContext, mMap,
                        "Cell ID: " + cellID,
                        "", location,
                        new MarkerData(
                                "" + cellID,
                                "" + location.getLatitude(),
                                "" + location.getLongitude(),
                                "" + lac,
                                "" + mcc,
                                "" + mnc,
                                "" + samples,
                                false));

                ovm.setIcon(cellTowerMarkerIcon);
                items.add(ovm);
            } while (c.moveToNext());
        }
        c.close();
        mDbHelper.close();

        mCellTowerGridMarkerClusterer.addAll(items);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final String KEY_MAP_TYPE = getBaseContext().getString(R.string.pref_map_type_key);
        if (key.equals(KEY_MAP_TYPE)) {
            int item = Integer.parseInt(sharedPreferences.getString(key, "0"));
            setupMapType(item);
        }
    }
}

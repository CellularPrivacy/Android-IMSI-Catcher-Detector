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
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
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
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    private CellTowerItemizedOverlay mOpenCellIdOverlay;

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

        setContentView(R.layout.map);
        setUpMapIfNeeded();

        mContext = this;
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
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadOpenCellIDMarkers();
        }
    };

    /**
     * Service Connection to bind the activity to the service
     */
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mAimsicdService = ((AimsicdService.AimscidBinder) service).getService();
            mBound = true;

            // set up map
            GeoLocation lastKnown = mAimsicdService.lastKnownLocation();
            if (lastKnown != null) {
                mMap.getController().setZoom(16);
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
        switch (mapType) {
            case 0:
                mMap.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE); //setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case 1:
                mMap.setTileSource(TileSourceFactory.MAPNIK); //.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case 2:
                mMap.setTileSource(TileSourceFactory.MAPQUESTAERIAL); //.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case 3:
                mMap.setTileSource(TileSourceFactory.CYCLEMAP); //.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
        }
    }

    /**
     * Initialises the Map and sets initial options
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mMap == null) {
            mMap = (MapView) findViewById(R.id.mapview);

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setBuiltInZoomControls(true);
                mMap.setMultiTouchControls(true);

                ResourceProxyImpl resProxyImp = new ResourceProxyImpl(MapViewerOsmDroid.this);
                GpsMyLocationProvider imlp = new GpsMyLocationProvider(
                        MapViewerOsmDroid.this.getBaseContext());
                imlp.setLocationUpdateMinDistance(1000);
                imlp.setLocationUpdateMinTime(60000);
                mMyLocationOverlay = new MyLocationNewOverlay(
                        MapViewerOsmDroid.this.getBaseContext(),
                        imlp,
                        mMap);
                mMyLocationOverlay.setDrawAccuracyEnabled(true);
                mMap.getOverlays().add(mMyLocationOverlay);

                mOpenCellIdOverlay = new CellTowerItemizedOverlay(MapViewerOsmDroid.this,
                        new LinkedList<CellTowerOverlayItem>());
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
                        Helpers.msgShort(this, "Contacting OpenCellID.org for data...");
                        Cell cell;
                        cell = mAimsicdService.getCell();
                        cell.setLon(lastKnown.getLongitudeInDegrees());
                        cell.setLat(lastKnown.getLatitudeInDegrees());
                        Helpers.getOpenCellData(mContext, cell,
                                RequestTask.OPEN_CELL_ID_REQUEST_FROM_MAP);
                        return true;
                    }
                }
                if (loc != null) {
                    Helpers.msgShort(this, "Contacting OpenCellID.org for data...");
                    Cell cell = new Cell();
                    cell.setLat(loc.getLatitude());
                    cell.setLon(loc.getLongitude());
                    Helpers.getOpenCellData(mContext, cell,
                            RequestTask.OPEN_CELL_ID_REQUEST_FROM_MAP);
                } else {
                    Helpers.msgShort(mContext,
                            "Unable to determine your last location. \nEnable Location Services and try again.");
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
     */
    private void loadEntries() {
        new AsyncTask<Void,Void,GeoPoint>() {
            @Override
            protected GeoPoint doInBackground(Void... voids) {
                final int SIGNAL_SIZE_RATIO = 15;
                int signal;
                int color;

                mOpenCellIdOverlay.removeAllItems();
                LinkedList<CellTowerOverlayItem> items = new LinkedList<>();

                mDbHelper.open();
                Cursor c = null;
                try {
                    c = mDbHelper.getCellData();
                }catch(IllegalStateException ix) {
                    Log.e(TAG, ix.getMessage(), ix);
                }
                if (c != null && c.moveToFirst()) {
                    do {
                        final int cellID = c.getInt(0);
                        final int lac = c.getInt(1);
                        final int net = c.getInt(2);
                        final int mcc = c.getInt(6);
                        final int mnc = c.getInt(7);
                        final double dlat = Double.parseDouble(c.getString(3));
                        final double dlng = Double.parseDouble(c.getString(4));
                        if (dlat == 0.0 && dlng == 0.0) {
                            continue;
                        }
                        signal = c.getInt(5);
                        if (signal <= 0) {
                            signal = 20;
                        }

                        if ((dlat != 0.0) || (dlng != 0.0)) {
                            loc = new GeoPoint(dlat, dlng);
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

                            CellTowerOverlayItem ovm = new CellTowerOverlayItem("Cell ID: " + cellID,
                                    "",
                                    loc,
                                    new MarkerData("" + cellID, "" + loc.getLatitude(),"" +
                                            loc.getLongitude(), "" + lac, "" + mcc, "" + mnc, "", false));

                            ovm.setMarker(getResources().getDrawable(R.drawable.ic_map_pin_blue));
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
                            Helpers.msgShort(MapViewerOsmDroid.this, "No tracked locations found to show on map.");
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
                while (mAimsicdService == null) try { Thread.sleep(100); } catch (Exception e) {};
                List<Cell> nc = mAimsicdService.getCellTracker().updateNeighbouringCells();
                for (Cell cell : nc) {
                    try {
                        loc = new GeoPoint(cell.getLat(), cell.getLon());
                        CellTowerOverlayItem ovm = new CellTowerOverlayItem("Cell ID: " + cell.getCID(),
                                "",
                                loc,
                                new MarkerData("" + cell.getCID(), "" + loc.getLatitude(),"" +
                                        loc.getLongitude(), "" + cell.getLAC(), "" + cell.getMCC(), "" + cell.getMNC(), "", false));

                        ovm.setMarker(getResources().getDrawable(R.drawable.ic_map_pin_orange));
                        items.add(ovm);
                    } catch (Exception e) {
                        Log.e("map", "Error plotting neighbouring cells", e);
                    }
                }

                mMap.getOverlays().remove(mOpenCellIdOverlay);
                mOpenCellIdOverlay.addItems(items);
                mMap.getOverlays().add(mOpenCellIdOverlay);

                return ret;
            }

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
                            //Use Mcc to move camera to an approximate location near Countries Capital
                            loc = defaultLoc;

                            mMap.getController().setZoom(13);
                            mMap.getController().animateTo(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
                        }
                    }
                }

                new Thread() {
                    @Override
                    public void run() {
                        loadOpenCellIDMarkers();
                    }
                }.start();
            }
        }.execute();
    }

    private void loadOpenCellIDMarkers() {
        //Check if OpenCellID data exists and if so load this now
        LinkedList<CellTowerOverlayItem> items = new LinkedList<>();

        mDbHelper.open();
        Cursor c = mDbHelper.getOpenCellIDData();
        if (c.moveToFirst()) {
            do {
                final double dlat = Double.parseDouble(c.getString(4));
                final double dlng = Double.parseDouble(c.getString(5));
                final int cellID = c.getInt(0);
                final int lac = c.getInt(1);
                final GeoPoint location = new GeoPoint(dlat, dlng);
                final int mcc = c.getInt(2);
                final int mnc = c.getInt(3);
                final int samples = c.getInt(7);
                // Add map marker for CellID


                CellTowerOverlayItem ovm = new CellTowerOverlayItem("Cell ID: " + cellID,
                        "",
                        location,
                        new MarkerData("" + cellID, "" + location.getLatitude(),"" +
                                location.getLongitude(), "" + lac, "" + mcc, "" + mnc, "" + samples, false));

                ovm.setMarker(getResources().getDrawable(R.drawable.ic_map_pin_green));
                items.add(ovm);
            } while (c.moveToNext());
        }
        c.close();
        mDbHelper.close();

        mMap.getOverlays().remove(mOpenCellIdOverlay);
        mOpenCellIdOverlay.addItems(items);
        mMap.getOverlays().add(mOpenCellIdOverlay);
    }

    public class MarkerData {

        public final String cellID;
        public final String lat;
        public final String lng;
        public final String lac;
        public final String mcc;
        public final String mnc;
        public final String samples;
        public final boolean openCellID;

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

    public class CellTowerOverlayItem extends OverlayItem {
        MarkerData mMarkerData;

        public CellTowerOverlayItem(String aTitle, String aSnippet, GeoPoint aGeoPoint, MarkerData data) {
            super(aTitle, aSnippet, aGeoPoint);
            mMarkerData = data;
        }

        public MarkerData getMarkerData() {
            return mMarkerData;
        }
    }

    public class CellTowerItemizedOverlay extends ItemizedIconOverlay<CellTowerOverlayItem> {
        protected Context mContext;

        public CellTowerItemizedOverlay(final Context context, final List<CellTowerOverlayItem> aList) {
            super(context, aList, new OnItemGestureListener<CellTowerOverlayItem>() {
                @Override public boolean onItemSingleTapUp(final int index, final CellTowerOverlayItem item) {
                    return false;
                }
                @Override public boolean onItemLongPress(final int index, final CellTowerOverlayItem item) {
                    return false;
                }
            } );

            mContext = context;
        }

        @Override
        protected boolean onSingleTapUpHelper(final int index, final CellTowerOverlayItem item, final MapView mapView) {
            // TODO - show as info window
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setTitle(item.getTitle());
            dialog.setView(getInfoContents(item.getMarkerData()));
            dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            dialog.show();
            return true;
        }

        // Defines the contents of the InfoWindow
        public View getInfoContents(MarkerData data) {

            TextView tv;

            // Getting view from the layout file info_window_layout
            View v = getLayoutInflater().inflate(R.layout.marker_info_window, null);

            if (v != null) {
                if (data != null) {
                    if (data.openCellID) {
                        TableRow tr = (TableRow) v.findViewById(R.id.open_cell_label);
                        tr.setVisibility(View.VISIBLE);
                    }

                    tv = (TextView) v.findViewById(R.id.cell_id);
                    tv.setText(data.cellID);
                    tv = (TextView) v.findViewById(R.id.lac);
                    tv.setText(data.lac);
                    tv = (TextView) v.findViewById(R.id.lat);
                    tv.setText(String.valueOf(data.lat));
                    tv = (TextView) v.findViewById(R.id.lng);
                    tv.setText(String.valueOf(data.lng));
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
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final String KEY_MAP_TYPE = getBaseContext().getString(R.string.pref_map_type_key);
        if (key.equals(KEY_MAP_TYPE)) {
            int item = Integer.parseInt(sharedPreferences.getString(key, "0"));
            setupMapType(item);
        }
    }
}

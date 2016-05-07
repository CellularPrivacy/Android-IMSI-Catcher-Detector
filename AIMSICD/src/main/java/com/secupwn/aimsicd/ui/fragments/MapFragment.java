/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.secupwn.aimsicd.AndroidIMSICatcherDetector;
import com.secupwn.aimsicd.BuildConfig;
import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.constants.TinyDbKeys;
import com.secupwn.aimsicd.data.model.BaseTransceiverStation;
import com.secupwn.aimsicd.data.model.GpsLocation;
import com.secupwn.aimsicd.data.model.Import;
import com.secupwn.aimsicd.data.model.Measure;
import com.secupwn.aimsicd.map.CellTowerGridMarkerClusterer;
import com.secupwn.aimsicd.map.CellTowerMarker;
import com.secupwn.aimsicd.map.MarkerData;
import com.secupwn.aimsicd.service.AimsicdService;
import com.secupwn.aimsicd.ui.activities.MapPrefActivity;
import com.secupwn.aimsicd.utils.Cell;
import com.secupwn.aimsicd.utils.GeoLocation;
import com.secupwn.aimsicd.utils.Helpers;
import com.secupwn.aimsicd.utils.RealmHelper;
import com.secupwn.aimsicd.utils.RequestTask;
import com.secupwn.aimsicd.utils.TinyDB;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.LinkedList;
import java.util.List;

import io.freefair.android.injection.annotation.Inject;
import io.freefair.android.injection.annotation.InjectView;
import io.freefair.android.injection.annotation.XmlLayout;
import io.freefair.android.injection.app.InjectionAppCompatActivity;
import io.freefair.android.injection.app.InjectionFragment;
import io.freefair.android.util.logging.Logger;
import io.realm.Realm;
import io.realm.RealmResults;
import lombok.Cleanup;

/**
 * Description:    TODO: add details
 * <p/>
 * Variables:      TODO: add a list of variables that can be tuned (Max/MinZoom factors etc)
 * <p/>
 * Current Issues:
 * <p/>
 * [x] Map is not immediately updated with the BTS info. It take a "long" time ( >10 seconds)
 * before map is updated. Any way to shorten this?
 * [ ] See: #272 #250 #228
 * [ ] Some pins remain clustered even on the greatest zoom, this is probably
 * due to over sized icons, or too low zoom level.
 * [x] pin icons are too big. We need to reduce pin dot diameter by ~50%
 * [ ] Need a manual way to add GPS coordinates of current location (see code comments below)
 * [ ]
 * <p/>
 * Notes:
 * a) Latest OSM version can use MaxZoomLevel of 21, please see:
 * https://github.com/osmdroid/osmdroid/issues/49
 * https://github.com/osmdroid/osmdroid/issues/81
 * https://code.google.com/p/osmbonuspack/issues/detail?id=102
 */
@XmlLayout(R.layout.activity_map_viewer)
public final class MapFragment extends InjectionFragment implements OnSharedPreferenceChangeListener {

    @Inject
    private Logger log;
    public static final String updateOpenCellIDMarkers = "update_open_cell_markers";

    @InjectView(R.id.mapview)
    private MapView mMap;
    private RealmHelper mDbHelper;
    private SharedPreferences prefs;
    private AimsicdService mAimsicdService;
    private boolean mBound;

    private GeoPoint loc = null;

    private MyLocationNewOverlay mMyLocationOverlay;
    private CompassOverlay mCompassOverlay;
    private CellTowerGridMarkerClusterer mCellTowerGridMarkerClusterer;
    private Menu mOptionsMenu;
    TelephonyManager tm;

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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        log.info("Starting MapViewer");

        setUpMapIfNeeded();

        mDbHelper = new RealmHelper(getActivity());
        tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);

        // Bind to LocalService
        Intent intent = new Intent(getActivity(), AimsicdService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION |
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        prefs = getActivity().getSharedPreferences(
                AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
        prefs.registerOnSharedPreferenceChangeListener(this);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(updateOpenCellIDMarkers));

        if (!mBound) {
            // Bind to LocalService
            Intent intent = new Intent(getActivity(), AimsicdService.class);
            getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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
    public void onDestroyView() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        if (mCompassOverlay != null) {
            mCompassOverlay.disableCompass();
        }

        if (mMyLocationOverlay != null) {
            mMyLocationOverlay.disableMyLocation();
        }

        prefs.unregisterOnSharedPreferenceChangeListener(this);
        // Unbind from the service
        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }

        TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadEntries();
            if (BuildConfig.DEBUG && mCellTowerGridMarkerClusterer != null && mCellTowerGridMarkerClusterer.getItems() != null) {
                log.verbose("mMessageReceiver CellTowerMarkers.invalidate() markers.size():" + mCellTowerGridMarkerClusterer.getItems().size());
            }

        }
    };

    /**
     * Service Connection to bind the activity to the service
     * <p/>
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
            log.error("Service Disconnected");
            mBound = false;
        }
    };

    // Load the default map type from preferences
    private void loadPreferences() {
        String mapTypePref = getResources().getString(R.string.pref_map_type_key);
        prefs = getActivity().getSharedPreferences(
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
                mMap.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                break;
            case 1:
                mMap.setTileSource(TileSourceFactory.CYCLEMAP);
                break;
            default:
                mMap.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
                break;
        }
    }

    /**
     * Description:     Initialises the Map and sets initial options such as:
     * Zoom levels and controls
     * Compass
     * ScaleBar
     * Cluster Pin colors
     * Location update settings
     */
    private void setUpMapIfNeeded() {

        // Check if we were successful in obtaining the map.
        mMap.setBuiltInZoomControls(true);
        mMap.setMultiTouchControls(true);
        mMap.setMinZoomLevel(3);
        mMap.setMaxZoomLevel(19); // Latest OSM can go to 21!
        mMap.getTileProvider().createTileCache();
        mCompassOverlay = new CompassOverlay(getActivity(), new InternalCompassOrientationProvider(getActivity()), mMap);

        ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(getActivity());
        mScaleBarOverlay.setScaleBarOffset(getResources().getDisplayMetrics().widthPixels / 2, 10);
        mScaleBarOverlay.setCentred(true);

        // Sets cluster pin color
        mCellTowerGridMarkerClusterer = new CellTowerGridMarkerClusterer(getActivity());
        BitmapDrawable mapPinDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_map_pin_orange);
        mCellTowerGridMarkerClusterer.setIcon(mapPinDrawable == null ? null : mapPinDrawable.getBitmap());

        GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(getActivity().getBaseContext());
        gpsMyLocationProvider.setLocationUpdateMinDistance(100); // [m]  // Set the minimum distance for location updates
        gpsMyLocationProvider.setLocationUpdateMinTime(10000);   // [ms] // Set the minimum time interval for location updates
        mMyLocationOverlay = new MyLocationNewOverlay(getActivity().getBaseContext(), gpsMyLocationProvider, mMap);
        mMyLocationOverlay.setDrawAccuracyEnabled(true);

        mMap.getOverlays().add(mCellTowerGridMarkerClusterer);
        mMap.getOverlays().add(mMyLocationOverlay);
        mMap.getOverlays().add(mCompassOverlay);
        mMap.getOverlays().add(mScaleBarOverlay);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.mOptionsMenu = menu;
        inflater.inflate(R.menu.fragment_map_menu, mOptionsMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.map_preferences:
                Intent intent = new Intent(getActivity(), MapPrefActivity.class);
                startActivity(intent);
                return true;
            case R.id.get_opencellid: {
                if (mBound) {
                    GeoLocation lastKnown = mAimsicdService.lastKnownLocation();
                    if (lastKnown != null) {
                        Helpers.msgLong(getActivity(),
                                getString(R.string.contacting_opencellid_for_data));
                        Cell cell;
                        cell = mAimsicdService.getCell();
                        cell.setLon(lastKnown.getLongitudeInDegrees());
                        cell.setLat(lastKnown.getLatitudeInDegrees());
                        setRefreshActionButtonState(true);
                        TinyDB.getInstance().putBoolean(TinyDbKeys.FINISHED_LOAD_IN_MAP, false);
                        Helpers.getOpenCellData((InjectionAppCompatActivity) getActivity(), cell, RequestTask.DBE_DOWNLOAD_REQUEST_FROM_MAP, mAimsicdService);
                        return true;
                    }
                }

                if (loc != null) {
                    Helpers.msgLong(getActivity(), getString(R.string.contacting_opencellid_for_data));
                    Cell cell = new Cell();
                    cell.setLat(loc.getLatitude());
                    cell.setLon(loc.getLongitude());
                    setRefreshActionButtonState(true);
                    TinyDB.getInstance().putBoolean(TinyDbKeys.FINISHED_LOAD_IN_MAP, false);
                    Helpers.getOpenCellData((InjectionAppCompatActivity) getActivity(), cell, RequestTask.DBE_DOWNLOAD_REQUEST_FROM_MAP, mAimsicdService);
                } else {
                    Helpers.msgLong(getActivity(),
                            getString(R.string.unable_to_determine_last_location));
                }
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Description:    Loads Signal Strength Database details to plot on the map,
     * only entries which have a location (lon, lat) are used.
     */
    private void loadEntries() {

        new AsyncTask<Void, Void, GeoPoint>() {
            @Override
            protected GeoPoint doInBackground(Void... voids) {
                //int signal;
                @Cleanup Realm realm = Realm.getDefaultInstance();

                mCellTowerGridMarkerClusterer.getItems().clear();

                loadOcidMarkersByNetwork();

                List<CellTowerMarker> items = new LinkedList<>();

                RealmResults<BaseTransceiverStation> baseStations = realm.where(BaseTransceiverStation.class).findAll();

                /*
                    This function is getting cells we logged from DBi_bts
                 */
                if (baseStations.size() > 0) {
                    for (BaseTransceiverStation baseStation : baseStations) {

                        if (isCancelled() || !isAdded()) {
                            return null;
                        }
                        // The indexing here is that of DB table
                        final int cellID = baseStation.getCellId();
                        final int lac = baseStation.getLocationAreaCode();
                        final int mcc = baseStation.getMobileCountryCode();
                        final int mnc = baseStation.getMobileNetworkCode();
                        final int psc = baseStation.getPrimaryScramblingCode();

                        Measure first = realm.where(Measure.class).equalTo("baseStation.cellId", baseStation.getCellId()).findFirst();
                        final String rat = first.getRadioAccessTechnology();
                        final double dLat = baseStation.getGpsLocation().getLatitude();
                        final double dLng = baseStation.getGpsLocation().getLongitude();

                        if (Double.doubleToRawLongBits(dLat) == 0
                                && Double.doubleToRawLongBits(dLng) == 0) {
                            continue;
                        }
                        // TODO this (signal) is not in DBi_bts
                        // signal = 1;
                        //c.getInt(c.getColumnIndex(DBTableColumnIds.DBE_IMPORT_AVG_SIGNAL));  // signal
                        // In case of missing or negative signal, set a default fake signal,
                        // so that we can still draw signal circles.  ?
                        //if (signal <= 0) {
                        //    signal = 20;
                        //}

                        if (Double.doubleToRawLongBits(dLat) != 0
                                || Double.doubleToRawLongBits(dLng) != 0) {
                            loc = new GeoPoint(dLat, dLng);

                            CellTowerMarker ovm = new CellTowerMarker(getActivity(), mMap,
                                    "Cell ID: " + cellID,
                                    "", loc,
                                    new MarkerData(
                                            getContext(),
                                            String.valueOf(cellID),
                                            String.valueOf(loc.getLatitude()),
                                            String.valueOf(loc.getLongitude()),
                                            String.valueOf(lac),
                                            String.valueOf(mcc),
                                            String.valueOf(mnc),
                                            String.valueOf(psc),
                                            rat,
                                            "", false)
                            );
                            // The pin of our current position
                            ovm.setIcon(getResources().getDrawable(R.drawable.ic_map_pin_blue));

                            items.add(ovm);
                        }
                    }
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Helpers.msgLong(getActivity(), getString(R.string.no_tracked_locations_found));
                        }
                    });
                }

                GeoPoint ret = new GeoPoint(0, 0);
                if (mBound) {
                    try {
                        int mcc = mAimsicdService.getCell().getMobileCountryCode();
                        GpsLocation d = mDbHelper.getDefaultLocation(realm, mcc);
                        ret = new GeoPoint(d.getLatitude(), d.getLongitude());
                    } catch (Exception e) {
                        log.error("Error getting default location!", e);
                    }
                }
                // plot neighboring cells
                while (mAimsicdService == null) {
                    try {
                        if (isCancelled() || !isAdded()) {
                            return null;
                        }
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        log.warn("thread interrupted", e);
                    }
                }
                List<Cell> nc = mAimsicdService.getCellTracker().updateNeighboringCells();
                for (Cell cell : nc) {
                    if (isCancelled() || !isAdded()) {
                        return null;
                    }
                    try {
                        loc = new GeoPoint(cell.getLat(), cell.getLon());
                        CellTowerMarker ovm = new CellTowerMarker(getActivity(), mMap,
                                getString(R.string.cell_id_label) + cell.getCellId(),
                                "", loc,
                                new MarkerData(
                                        getContext(),
                                        String.valueOf(cell.getCellId()),
                                        String.valueOf(loc.getLatitude()),
                                        String.valueOf(loc.getLongitude()),
                                        String.valueOf(cell.getLocationAreaCode()),
                                        String.valueOf(cell.getMobileCountryCode()),
                                        String.valueOf(cell.getMobileNetworkCode()),
                                        String.valueOf(cell.getPrimaryScramblingCode()),
                                        String.valueOf(cell.getRat()),
                                        "", false));

                        // The pin of other BTS
                        ovm.setIcon(getResources().getDrawable(R.drawable.ic_map_pin_orange));
                        items.add(ovm);
                    } catch (Exception e) {
                        log.error("Error plotting neighboring cells", e);
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
             * @param defaultLoc Default location to open map on
             */
            @Override
            protected void onPostExecute(GeoPoint defaultLoc) {
                if (loc != null && (Double.doubleToRawLongBits(loc.getLatitude()) != 0
                        && Double.doubleToRawLongBits(loc.getLongitude()) != 0)) {
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
                if (mCellTowerGridMarkerClusterer != null) {
                    if (BuildConfig.DEBUG && mCellTowerGridMarkerClusterer.getItems() != null) {
                        log.verbose("CellTowerMarkers.invalidate() markers.size():" + mCellTowerGridMarkerClusterer.getItems().size());
                    }
                    //Drawing markers of cell tower immediately as possible
                    mCellTowerGridMarkerClusterer.invalidate();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void loadOcidMarkersByNetwork() {
        // Check if OpenCellID data exists and if so load this now
        List<CellTowerMarker> items = new LinkedList<>();
        String networkOperator = tm.getNetworkOperator();
        int currentMmc = 0;
        int currentMnc = 0;
        if (networkOperator != null && networkOperator.length() > 3) {
            currentMmc = Integer.parseInt(networkOperator.substring(0, 3));
            currentMnc = Integer.parseInt(networkOperator.substring(3));
        }

        Drawable cellTowerMarkerIcon = getResources().getDrawable(R.drawable.ic_map_pin_green);

        @Cleanup Realm realm = Realm.getDefaultInstance();

        RealmResults<Import> importRealmResults = mDbHelper.returnOcidBtsByNetwork(realm, currentMmc, currentMnc).findAll();
        for (Import anImport : importRealmResults) {

            final int cellID = anImport.getCellId();
            final int lac = anImport.getLocationAreaCode();
            final int mcc = anImport.getMobileCountryCode();
            final int mnc = anImport.getMobileNetworkCode();
            final int psc = anImport.getPrimaryScramblingCode();
            final String rat = anImport.getRadioAccessTechnology();
            final double dLat = anImport.getGpsLocation().getLatitude();
            final double dLng = anImport.getGpsLocation().getLongitude();
            final GeoPoint location = new GeoPoint(dLat, dLng);
            //where is c.getString(6)AvgSigStr
            final int samples = anImport.getSamples();
            // Add map marker for CellID
            CellTowerMarker ovm = new CellTowerMarker(getActivity(), mMap,
                    "Cell ID: " + cellID,
                    "", location,
                    new MarkerData(
                            getContext(),
                            String.valueOf(cellID),
                            String.valueOf(location.getLatitude()),
                            String.valueOf(location.getLongitude()),
                            String.valueOf(lac),
                            String.valueOf(mcc),
                            String.valueOf(mnc),
                            String.valueOf(psc),
                            rat,
                            String.valueOf(samples),
                            false));

            ovm.setIcon(cellTowerMarkerIcon);
            items.add(ovm);
        }

        mCellTowerGridMarkerClusterer.addAll(items);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final String KEY_MAP_TYPE = getActivity().getBaseContext().getString(R.string.pref_map_type_key);
        if (key.equals(KEY_MAP_TYPE)) {
            int item = Integer.parseInt(sharedPreferences.getString(key, "0"));
            setupMapType(item);
        }
    }

    public void setRefreshActionButtonState(final boolean refreshing) {
        if (mOptionsMenu != null) {
            final MenuItem refreshItem = mOptionsMenu.findItem(R.id.get_opencellid);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AndroidIMSICatcherDetector) getActivity().getApplication()).attach((InjectionAppCompatActivity) getActivity());
        if (TinyDB.getInstance().getBoolean(TinyDbKeys.FINISHED_LOAD_IN_MAP)) {
            setRefreshActionButtonState(false);
        }
    }
}

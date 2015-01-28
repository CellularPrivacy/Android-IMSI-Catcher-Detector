package com.SecUpwN.AIMSICD.service;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.utils.Cell;
import com.SecUpwN.AIMSICD.utils.GeoLocation;

/**
 * Class to handle GPS location tracking
 */
public class LocationTracker {
    // how long with no movement detected, before we assume we are not moving
    public static final long MOVEMENT_THRESHOLD_MS = 20*1000;

    /**
     * Location listener stuff
     */
    private AimsicdService context;
    private SharedPreferences prefs;
    private static LocationManager lm;
    private LocationListener mLocationListener;
    private LocationListener extLocationListener;
    private long lastLocationTime = 0;
    private Location lastLocation;
    private static final long GPS_MIN_UPDATE_TIME = 10000;
    private static final float GPS_MIN_UPDATE_DISTANCE = 10;


    LocationTracker(AimsicdService service, LocationListener extLocationListener) {
        this.context = service;
        this.extLocationListener = extLocationListener;

        lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new MyLocationListener();
        prefs = context.getSharedPreferences(
                AimsicdService.SHARED_PREFERENCES_BASENAME, 0);
    }

    public void start() {
        lastKnownLocation();

        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_MIN_UPDATE_TIME,
                    GPS_MIN_UPDATE_DISTANCE, mLocationListener);
        } catch (IllegalArgumentException e) {
            // provider doesn't exist, so ignore
        }

        try {
            lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, GPS_MIN_UPDATE_TIME,
                    GPS_MIN_UPDATE_DISTANCE, mLocationListener);
        } catch (IllegalArgumentException e) {
            // provider doesn't exist, so ignore
        }

        try {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, GPS_MIN_UPDATE_TIME,
                    GPS_MIN_UPDATE_DISTANCE, mLocationListener);
        } catch (IllegalArgumentException e) {
            // provider doesn't exist, so ignore
        }
    }

    public void stop() {
        lm.removeUpdates(mLocationListener);
    }

    public boolean isGPSOn() {
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Check if we are moving, using last known GPS locations
     * @return
     */
    public boolean notMovedInAWhile() {
        // first-lock, assume no movement
        if (lastLocationTime <= 0) return true;

        // haven't received a GPS update in a while, assume no movement
        if (System.currentTimeMillis() - lastLocationTime > MOVEMENT_THRESHOLD_MS) return true;

        return false;
    }

    public GeoLocation lastKnownLocation() {
        GeoLocation loc = null;
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null && (location.getLatitude() != 0.0 && location.getLongitude() != 0.0)) {
            loc = GeoLocation.fromDegrees(location.getLatitude(), location.getLongitude());
        } else {
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null && (location.getLatitude() != 0.0
                    && location.getLongitude() != 0.0)) {
                loc = GeoLocation.fromDegrees(location.getLatitude(), location.getLongitude());
            } else {
                String coords = prefs.getString(context.getString(R.string.data_last_lat_lon), null);
                if (coords != null) {
                    String[] coord = coords.split(":");
                    loc = GeoLocation.fromDegrees(Double.valueOf(coord[0]), Double.valueOf(coord[1]));
                } else {
                    // get location from MCC
                    try {
                        Cell cell = context.getCell();
                        if (cell != null) {
                            Log.d("location", "Looking up MCC " + cell.getMCC());
                            AIMSICDDbAdapter mDbHelper = new AIMSICDDbAdapter(context);
                            mDbHelper.open();
                            double[] defLoc = mDbHelper.getDefaultLocation(cell.getMCC());
                            mDbHelper.close();
                            loc = GeoLocation.fromDegrees(defLoc[0], defLoc[1]);
                        }
                    } catch (Exception e) {
                        Log.e("location", "Unable to get location from MCC", e);
                    }
                }
            }
        }

        if (loc != null) Log.i("location", "Last known location " + loc.toString());
        return loc;
    }

    /**
     * Our location listener, so that we can update our internal status before passing on the
     * events to the caller
     */
    private class MyLocationListener implements LocationListener {

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onLocationChanged(Location loc) {
            //Log.d("location", "Got location " + loc);
            if (lastLocation != null &&
                    lastLocation.getLongitude() == loc.getLongitude() &&
                    lastLocation.getLatitude() == loc.getLatitude()) {
                // same location as before, so ignore
                return;
            }

            lastLocationTime = System.currentTimeMillis();
            extLocationListener.onLocationChanged(loc);
        }

        @Override
        public void onProviderDisabled(String provider) {
            extLocationListener.onProviderDisabled(provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            extLocationListener.onProviderEnabled(provider);
        }

        @Override
        public void onStatusChanged(String provider, int status,
                                    Bundle extras) {
            extLocationListener.onStatusChanged(provider, status, extras);
        }
    }
}

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

import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import com.SecUpwN.AIMSICD.cmdprocessor.Helpers;
import com.google.android.maps.*;

import java.util.List;

public class MapViewer extends MapActivity {
    private final String TAG = "AIMSICD_MapViewer";

    public MapView mapView;
    private AIMSICDDbAdapter mDbHelper;

    private MapController mapc;
    private final int SIGNAL_SIZE_RATIO = 15;

    class MapOverlay extends com.google.android.maps.Overlay {
        private GeoPoint gp1;
        private int color;
        private int radius;

        public MapOverlay(GeoPoint gp1, int radius, int color) {
            this.gp1 = gp1;
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            int pradius = (int) mapView.getProjection().metersToEquatorPixels(radius);

            Log.i(TAG, " ==> Draw pos: " + gp1.toString() + " color: " + color + " radius: " + radius + " pradius: " + pradius);
            Projection projection = mapView.getProjection();
            Paint paint = new Paint();
            Point point = new Point();
            projection.toPixels(gp1, point);
            paint.setColor(color);
            paint.setStrokeWidth(0);
            paint.setAlpha(20);
            canvas.drawCircle(point.x, point.y, pradius, paint);
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Starting MapViewer ============");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        mDbHelper = new AIMSICDDbAdapter(this);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setBuiltInZoomControls(true);
        mapView.displayZoomControls(true);
        loadentries();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        menu.add(0, 1, 1, "Erase DB");
        menu.add(0, 3, 3, "About");
        menu.add(0, 4, 4, "Go to Log");
        menu.setGroupCheckable(1, true, false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case 1:
                erasedb();
                return true;
            case 3:
                about();
                return true;
            case 4:
                quit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final void about() {
        WebView webview = new WebView(this);
        webview.loadUrl("http://secupwn.github.io/Android-IMSI-Catcher-Detector/");
        webview.canGoBack();
        setContentView(webview);
    }

    private void loadentries() {
        double dlat = 0.0;
        double dlng = 0.0;
        int net = 0;
        int signal = 0;
        int radius = 0;
        GeoPoint p = null;
        mapc = mapView.getController();
        int color = 0x000000;
        mDbHelper.open();
        Cursor c = mDbHelper.getSignalData();
        if (c.getCount()>0) {
            if (c.moveToFirst()) {
                List<Overlay> listOfOverlays = mapView.getOverlays();
                do {
                    net = c.getInt(0);
                    dlat = Double.parseDouble(c.getString(1));
                    dlng = Double.parseDouble(c.getString(2));
                    signal = c.getInt(3);
                    if (signal == 0) {
                        signal = 20;
                    }

                    if ((dlat != 0.0) || (dlng != 0.0)) {
                        p = new GeoPoint((int) (dlat * 1E6), (int) (dlng * 1E6));
                        radius = signal * SIGNAL_SIZE_RATIO;
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
                        Log.i(TAG, " ==> Point:" + p.toString() + " radius: " + radius + " color: " + color + " signal:" + signal);
                        listOfOverlays.add(new MapOverlay(p, radius, color));
                    }

                } while (c.moveToNext());
                c.close();
                mapc.setCenter(p);
            }
        } else {
            Helpers.msgShort(this, "No tracked locations found to overlay on map.");
        }
        mapc.setZoom(14);
        mapView.setSatellite(false);
        mapView.invalidate();
    }

    private final void quit() {
        this.finish();
    }

    private final void erasedb() {
        mDbHelper.eraseLocationData();
    }
}

 

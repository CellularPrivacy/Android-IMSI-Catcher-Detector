package com.SecUpwN.AIMSICD.map;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

/**
 * BTS pin item
 */
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

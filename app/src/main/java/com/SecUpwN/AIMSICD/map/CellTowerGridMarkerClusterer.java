/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.map;

import android.content.Context;

import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;

import java.util.List;

/**
 * Description:     Overlay class for OSMDroid map to display multiple BTS pins
 *                  as one numbered point, clustering multiple pins.
 */
public class CellTowerGridMarkerClusterer extends RadiusMarkerClusterer {
    protected Context mContext;

    public CellTowerGridMarkerClusterer(Context ctx) {
        super(ctx);
    }

    public void addAll(List<CellTowerMarker> markers) {
        for (CellTowerMarker marker : markers) {
            add(marker);
        }
    }
}

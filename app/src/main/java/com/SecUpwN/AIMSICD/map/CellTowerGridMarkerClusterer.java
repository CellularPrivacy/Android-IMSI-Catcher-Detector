package com.SecUpwN.AIMSICD.map;

import android.content.Context;
import org.osmdroid.bonuspack.clustering.GridMarkerClusterer;
import java.util.List;

/**
 * Overlay class for OSMDroid map to display BTS pins
 */
public class CellTowerGridMarkerClusterer extends GridMarkerClusterer {
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

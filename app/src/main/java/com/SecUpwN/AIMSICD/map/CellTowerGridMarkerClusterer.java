package com.SecUpwN.AIMSICD.map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;

import org.osmdroid.bonuspack.clustering.GridMarkerClusterer;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;

import java.util.LinkedList;
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

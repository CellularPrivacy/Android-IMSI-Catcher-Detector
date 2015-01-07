package com.SecUpwN.AIMSICD.map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;

import java.util.List;

/**
 * Overlay class for OSMDroid map to display BTS pins
 */
public class CellTowerItemizedOverlay extends ItemizedIconOverlay<CellTowerOverlayItem> {
    protected Context mContext;

    public CellTowerItemizedOverlay(final Context context, final List<CellTowerOverlayItem> aList) {
        super(context, aList, new OnItemGestureListener<CellTowerOverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(final int index, final CellTowerOverlayItem item) {
                return false;
            }

            @Override
            public boolean onItemLongPress(final int index, final CellTowerOverlayItem item) {
                return false;
            }
        });

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
        View v = LayoutInflater.from(mContext).inflate(R.layout.marker_info_window, null);

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
                tv.setText(data.getMCC());
                tv = (TextView) v.findViewById(R.id.mnc);
                tv.setText(data.getMNC());
                tv = (TextView) v.findViewById(R.id.pc);
                tv.setText(data.getPC());
                tv = (TextView) v.findViewById(R.id.samples);
                tv.setText(data.getSamples());
            }
        }

        // Returning the view containing InfoWindow contents
        return v;
    }
}

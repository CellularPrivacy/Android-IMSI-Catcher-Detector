package com.SecUpwN.AIMSICD.map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

/**
 * BTS pin item
 */
public class CellTowerMarker extends Marker {
    private Context mContext;
    private MarkerData mMarkerData;

    public CellTowerMarker(Context context, MapView mapView, String aTitle, String aSnippet, GeoPoint aGeoPoint, MarkerData data) {
        super(mapView);

        mContext = context;

        mTitle = aTitle;
        mSnippet = aSnippet;
        mPosition = aGeoPoint;

        mMarkerData = data;

        mOnMarkerClickListener = new OnCellTowerMarkerClickListener();
    }

    public MarkerData getMarkerData() {
        return mMarkerData;
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

    public class OnCellTowerMarkerClickListener implements OnMarkerClickListener {
        @Override
        public boolean onMarkerClick(Marker marker, MapView mapView) {
            CellTowerMarker cellTowerMarker = (CellTowerMarker) marker;
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setTitle(cellTowerMarker.getTitle());
            dialog.setView(getInfoContents(cellTowerMarker.getMarkerData()));
            dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            dialog.show();

            return true;
        }
    }
}

/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;

/**
 *  Description:     Contains the data and definitions of all the items of the XML layout
 *
 *  Dependencies:
 *                  DbViewerFragment.java: BuildTable()
 *                  DbeImportItemData.java
 *                  dbe_import_items.xml
 *
 *  Issues:
 *
 *  ChangeLog:
 *                  2015-08-02  E:V:A           Added rej_cause
 */
public class DbeImportCardInflater implements IAdapterViewInflater<DbeImportItemData> {

    @Override
    public View inflate(final BaseInflaterAdapter<DbeImportItemData> adapter,
                        final int pos, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.dbe_import_items, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final DbeImportItemData item = adapter.getTItem(pos);
        holder.updateDisplay(item);
        return convertView;
    }

    private class ViewHolder {

        private final View mRootView;

        private final TextView DB_SOURCE;
        private final TextView RAT;
        private final TextView MCC;
        private final TextView MNC;
        private final TextView LAC;
        private final TextView CID;
        private final TextView PSC;
        private final TextView GPS_LAT;
        private final TextView GPS_LON;
        private final TextView IS_GPS_EXACT;
        private final TextView AVG_RANGE;
        private final TextView AVG_SIGNAL;
        private final TextView SAMPLES;
        private final TextView TIME_FIRST;
        private final TextView TIME_LAST;
        private final TextView REJ_CAUSE;

        private final TextView mRecordId;

        public ViewHolder(View rootView) {
            mRootView = rootView;
            // TODO: explain these and try to adhere to a naming convention
            // These are the id names as used in the "dbe_import_items.xml" stylesheet
            DB_SOURCE =     (TextView) mRootView.findViewById(R.id.dbsource);
            RAT =           (TextView) mRootView.findViewById(R.id.RAT);
            MCC =           (TextView) mRootView.findViewById(R.id.nMCC);
            MNC =           (TextView) mRootView.findViewById(R.id.nMNC);
            LAC =           (TextView) mRootView.findViewById(R.id.nLAC);
            CID =           (TextView) mRootView.findViewById(R.id.nCID);
            PSC =           (TextView) mRootView.findViewById(R.id.nPSC);
            GPS_LAT =       (TextView) mRootView.findViewById(R.id.ngpsd_lat);
            GPS_LON =       (TextView) mRootView.findViewById(R.id.ngpsd_lon);
            IS_GPS_EXACT =  (TextView) mRootView.findViewById(R.id.is_exact);
            AVG_RANGE =     (TextView) mRootView.findViewById(R.id.navg_range);
            AVG_SIGNAL =    (TextView) mRootView.findViewById(R.id.navg_signal);
            SAMPLES =       (TextView) mRootView.findViewById(R.id.nSAMPLES);
            TIME_FIRST =    (TextView) mRootView.findViewById(R.id.nTIME_FIRST);
            TIME_LAST =     (TextView) mRootView.findViewById(R.id.nTIME_LAST);
            REJ_CAUSE =     (TextView) mRootView.findViewById(R.id.nREJ_CAUSE);

            mRecordId =     (TextView) mRootView.findViewById(R.id.record_id);
            rootView.setTag(this);
        }

        public void updateDisplay(DbeImportItemData item) {
            DB_SOURCE.setText(item.getDB_SOURCE());
            RAT.setText(item.getRAT());
            MCC.setText(item.getMCC());
            MNC.setText(item.getMNC());
            LAC.setText(item.getLAC());
            CID.setText(item.getCID());
            PSC.setText(item.getPSC());
            GPS_LAT.setText(item.getGPS_LAT());
            GPS_LON.setText(item.getGPS_LON());
            IS_GPS_EXACT.setText(item.getIS_GPS_EXACT());
            AVG_RANGE.setText(item.getAVG_RANGE());
            AVG_SIGNAL.setText(item.getAVG_SIGNAL());
            SAMPLES.setText(item.getSAMPLES());
            TIME_FIRST.setText(item.getTIME_FIRST());
            TIME_LAST.setText(item.getTIME_LAST());
            REJ_CAUSE.setText(item.getREJ_CAUSE());

            mRecordId.setText(item.getRecordId());
        }
    }
}

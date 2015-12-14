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
 * Description:     Contains the data and definitions of all the items of the XML layout
 *
 * Dependencies:
 *                  UniqueBtsItemData.java
 *                  unique_bts_data.xml
 *
 * TODO:
 *                  [ ] Order all the items according to appearance found in the DB table below
 *                  [ ] Add DB items: T3212, A5x and ST_id
 *                  [ ] If (exact) gpse_lat/lon doesn't exist in DBe_import, set Lat/Lon to "-"
 *
 *
 *
 * NOTE:
 *                  CREATE TABLE "DBi_bts"  (
 *                   "_id"        	INTEGER PRIMARY KEY AUTOINCREMENT,
 *                   "MCC"       	INTEGER NOT NULL,	--
 *                   "MNC"       	INTEGER NOT NULL,	--
 *                   "LAC"       	INTEGER NOT NULL,	--
 *                   "CID"       	INTEGER NOT NULL,	--
 *                   "PSC"       	INTEGER,		--
 *                   "T3212"     	INTEGER DEFAULT 0,	-- Fix java to allow null here
 *                   "A5x"       	INTEGER DEFAULT 0,	-- Fix java to allow null here
 *                   "ST_id"     	INTEGER DEFAULT 0,	-- Fix java to allow null here
 *                   "time_first"	INTEGER,		--
 *                   "time_last" 	INTEGER,		--
 *                   "gps_lat"       REAL NOT NULL,		--
 *                   "gps_lon"       REAL NOT NULL		--
 *                   );
 *
 *
 * ChangeLog:
 *                  2015-07-27  E:V:A           Added placeholders for missing items
 */
public class UniqueBtsCardInflater implements IAdapterViewInflater<UniqueBtsItemData> {

    @Override
    public View inflate(final BaseInflaterAdapter<UniqueBtsItemData> adapter,
                        final int pos, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.unique_bts_data, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final UniqueBtsItemData item = adapter.getTItem(pos);
        holder.updateDisplay(item);

        return convertView;
    }

    private class ViewHolder {

        private final View mRootView;

        // TODO: Order these and the rest, as in DB table (shown above)
        private final TextView LAC;
        private final TextView CID;
        private final TextView MCC;
        private final TextView MNC;
        private final TextView PSC;

        private final TextView T3212;
        private final TextView A5X;
        private final TextView ST_ID;

        private final TextView TIME_FIRST;
        private final TextView TIME_LAST;
        private final TextView LAT; // These are exact GPS coordinates if found in DBe_import, else "-"
        private final TextView LON; // These are exact GPS coordinates if found in DBe_import, else "-"

        private final TextView RecordId;


        // These are the names of the "@+id/xxxx" items in the XML layout file
        public ViewHolder(View rootView) {
            mRootView = rootView;

            LAC = (TextView) mRootView.findViewById(R.id.tv_uniquebts_lac);
            CID = (TextView) mRootView.findViewById(R.id.tv_uniquebts_cid);
            MCC = (TextView) mRootView.findViewById(R.id.tv_uniquebts_mcc);
            MNC = (TextView) mRootView.findViewById(R.id.tv_uniquebts_mnc);
            PSC = (TextView) mRootView.findViewById(R.id.tv_uniquebts_psc);
            T3212 =     (TextView) mRootView.findViewById(R.id.tv_uniquebts_t3212);
            A5X =       (TextView) mRootView.findViewById(R.id.tv_uniquebts_a5x);
            ST_ID =     (TextView) mRootView.findViewById(R.id.tv_uniquebts_st_id);
            TIME_FIRST = (TextView) mRootView.findViewById(R.id.tv_uniquebts_time_first);
            TIME_LAST = (TextView) mRootView.findViewById(R.id.tv_uniquebts_time_last);
            LAT =       (TextView) mRootView.findViewById(R.id.tv_uniquebts_lat);
            LON =       (TextView) mRootView.findViewById(R.id.tv_uniquebts_lon);

            RecordId = (TextView) mRootView.findViewById(R.id.record_id);
            rootView.setTag(this);
        }

        public void updateDisplay(UniqueBtsItemData item) {

            LAC.setText(item.getLac());
            CID.setText(item.getCid());

            MCC.setText(item.getMcc());
            MNC.setText(item.getMnc());
            PSC.setText(item.getPsc());

            // TODO: Get values from DB when available
            T3212.setText("n/a");   //T3212.setText(item.getT3212());
            A5X.setText("n/a");     //A5X.setText(item.getA5x());
            ST_ID.setText("n/a");   //ST_ID.setText(item.getStId());

            TIME_FIRST.setText(item.getTime_first());
            TIME_LAST.setText(item.getTime_last());

            LAT.setText(String.valueOf(item.getLat())); // DBe_import
            LON.setText(String.valueOf(item.getLon())); // DBe_import

            RecordId.setText(item.getRecordId());
        }
    }
}

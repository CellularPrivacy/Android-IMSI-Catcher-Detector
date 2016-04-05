/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.data.model.BTS;

import java.text.DateFormat;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Description:     Contains the data and definitions of all the items of the XML layout
 *
 *
 * TODO:
 *                  [ ] Add DB items: T3212, A5x and ST_id
 */
public class UniqueBtsAdapter extends RealmBaseAdapter<BTS> {

    public UniqueBtsAdapter(Context context, RealmResults<BTS> realmResults, boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.unique_bts_data, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final BTS item = getItem(position);
        holder.updateDisplay(item, position);

        return convertView;
    }

    private class ViewHolder {

        private final View mRootView;

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
        private final TextView LAT;
        private final TextView LON;

        private final TextView RecordId;


        // These are the names of the "@+id/xxxx" items in the XML layout file
        ViewHolder(View rootView) {
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

        public void updateDisplay(BTS item, int position) {

            LAC.setText(String.valueOf(item.getLocationAreaCode()));
            CID.setText(String.valueOf(item.getCellId()));

            MCC.setText(String.valueOf(item.getMobileCountryCode()));
            MNC.setText(String.valueOf(item.getMobileNetworkCode()));
            PSC.setText(String.valueOf(item.getPrimaryScramblingCode()));

            // TODO: Get values from DB when available

            T3212.setText(R.string.n_a);   //T3212.setText(item.getT3212());
            A5X.setText(R.string.n_a);     //A5X.setText(item.getA5x());
            ST_ID.setText(R.string.n_a);   //ST_ID.setText(item.getStId());


            DateFormat dateFormat = DateFormat.getDateTimeInstance();
            TIME_FIRST.setText(dateFormat.format(item.getTimeFirst()));
            TIME_LAST.setText(dateFormat.format(item.getTimeLast()));

            LAT.setText(String.valueOf(item.getLocationInfo().getLatitude()));
            LON.setText(String.valueOf(item.getLocationInfo().getLongitude()));

            RecordId.setText(String.valueOf(position));
        }
    }
}

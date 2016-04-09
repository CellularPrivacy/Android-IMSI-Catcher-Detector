/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.data.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.data.model.BaseTransceiverStation;

import java.text.DateFormat;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

import static java.lang.String.valueOf;

/**
 * Contains the data and definitions of all the items of the XML layout
 * <p/>
 * TODO: Add DB items: T3212, A5x and ST_id
 */
public class BaseStationAdapter extends RealmBaseAdapter<BaseTransceiverStation> {

    public BaseStationAdapter(Context context, RealmResults<BaseTransceiverStation> realmResults, boolean automaticUpdate) {
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

        final BaseTransceiverStation item = getItem(position);
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
            T3212 = (TextView) mRootView.findViewById(R.id.tv_uniquebts_t3212);
            A5X = (TextView) mRootView.findViewById(R.id.tv_uniquebts_a5x);
            ST_ID = (TextView) mRootView.findViewById(R.id.tv_uniquebts_st_id);
            TIME_FIRST = (TextView) mRootView.findViewById(R.id.tv_uniquebts_time_first);
            TIME_LAST = (TextView) mRootView.findViewById(R.id.tv_uniquebts_time_last);
            LAT = (TextView) mRootView.findViewById(R.id.tv_uniquebts_lat);
            LON = (TextView) mRootView.findViewById(R.id.tv_uniquebts_lon);

            RecordId = (TextView) mRootView.findViewById(R.id.record_id);
            rootView.setTag(this);
        }

        public void updateDisplay(BaseTransceiverStation baseStation, int position) {

            LAC.setText(valueOf(baseStation.getLocationAreaCode()));
            CID.setText(valueOf(baseStation.getCellId()));

            MCC.setText(valueOf(baseStation.getMobileCountryCode()));
            MNC.setText(valueOf(baseStation.getMobileNetworkCode()));
            PSC.setText(valueOf(baseStation.getPrimaryScramblingCode()));

            DateFormat dateFormat = DateFormat.getDateTimeInstance();
            TIME_FIRST.setText(dateFormat.format(baseStation.getTimeFirst()));
            TIME_LAST.setText(dateFormat.format(baseStation.getTimeLast()));

            LAT.setText(valueOf(baseStation.getGpsLocation().getLatitude()));
            LON.setText(valueOf(baseStation.getGpsLocation().getLongitude()));

            RecordId.setText(valueOf(position));
        }
    }
}

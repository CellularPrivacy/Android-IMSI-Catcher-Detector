package com.secupwn.aimsicd.data.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.data.model.Measure;

import java.text.DateFormat;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

import static java.lang.String.valueOf;

/**
 * Contains the data and definitions of all the items of the XML layout
 */
public class MeasureAdapter extends RealmBaseAdapter<Measure> {

    public MeasureAdapter(Context context, RealmResults<Measure> realmResults, boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.bts_measure_data, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Measure item = getItem(position);
        holder.updateDisplay(item, position);

        return convertView;
    }

    private class ViewHolder {

        private final View mRootView;

        private TextView bts_id;
        private TextView nc_list;
        private TextView time;
        private TextView gpsd_lat;
        private TextView gpsd_lon;
        private TextView gpsd_accu;
        private TextView gpse_lat;
        private TextView gpse_lon;
        private TextView bb_power;
        private TextView bb_rf_temp;
        private TextView tx_power;
        private TextView rx_signal;
        private TextView rx_stype;
        private TextView rat;
        private TextView BCCH;
        private TextView TMSI;
        private TextView TA;
        private TextView PD;
        private TextView BER;
        private TextView AvgEcNo;
        private TextView isSubmitted;
        private TextView isNeighbor;

        private TextView mRecordId;

        ViewHolder(View rootView) {
            mRootView = rootView;

            bts_id = (TextView) mRootView.findViewById(R.id.tv_bts_measure_bts_id);
            nc_list = (TextView) mRootView.findViewById(R.id.tv_bts_measure_nc_list);
            time = (TextView) mRootView.findViewById(R.id.tv_bts_measure_time);
            gpsd_lat = (TextView) mRootView.findViewById(R.id.tv_bts_measure_gpsd_lat);
            gpsd_lon = (TextView) mRootView.findViewById(R.id.tv_bts_measure_gpsd_lon);
            gpsd_accu = (TextView) mRootView.findViewById(R.id.tv_bts_measure_gpsd_accu);
            //gpse_lat = (TextView) mRootView.findViewById(R.id.tv_bts_measure_gpse_lat);
            //gpse_lon = (TextView) mRootView.findViewById(R.id.tv_bts_measure_gpse_lon);
            //bb_power = (TextView) mRootView.findViewById(R.id.tv_bts_measure_bb_power);
            //bb_rf_temp = (TextView) mRootView.findViewById(R.id.tv_bts_measure_bb_rf_temp);
            //tx_power = (TextView) mRootView.findViewById(R.id.tv_bts_measure_tx_power);
            rx_signal = (TextView) mRootView.findViewById(R.id.tv_bts_measure_rx_signal);
            //rx_stype = (TextView) mRootView.findViewById(R.id.tv_bts_measure_rx_stype);
            rat = (TextView) mRootView.findViewById(R.id.tv_bts_measure_rat);
            //BCCH = (TextView) mRootView.findViewById(R.id.tv_bts_measure_BCCH);
            //TMSI = (TextView) mRootView.findViewById(R.id.tv_bts_measure_TMSI);
            //TA = (TextView) mRootView.findViewById(R.id.tv_bts_measure_TA);
            //PD = (TextView) mRootView.findViewById(R.id.tv_bts_measure_PD);
            //BER = (TextView) mRootView.findViewById(R.id.tv_bts_measure_BER);
            //AvgEcNo = (TextView) mRootView.findViewById(R.id.tv_bts_measure_AvgEcNo);
            isSubmitted = (TextView) mRootView.findViewById(R.id.tv_bts_measure_isSubmitted);
            isNeighbor = (TextView) mRootView.findViewById(R.id.tv_bts_measure_isNeighbor);

            mRecordId = (TextView) mRootView.findViewById(R.id.record_id);      // EVA
            rootView.setTag(this);
        }

        public void updateDisplay(Measure item, int position) {

            bts_id.setText(valueOf(item.getBaseStation().getCellId()));
            time.setText(DateFormat.getDateTimeInstance().format(item.getTime()));
            gpsd_lat.setText(valueOf(item.getGpsLocation().getLatitude()));
            gpsd_lon.setText(valueOf(item.getGpsLocation().getLongitude()));
            gpsd_accu.setText(valueOf(item.getGpsLocation().getAccuracy()));
            rx_signal.setText(valueOf(item.getRxSignal()));
            rat.setText(item.getRadioAccessTechnology());
            isSubmitted.setText(valueOf(item.isSubmitted()));
            isNeighbor.setText(valueOf(item.isNeighbor()));

            mRecordId.setText(valueOf(position));
        }
    }
}

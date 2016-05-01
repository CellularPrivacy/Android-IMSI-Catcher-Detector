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
import com.secupwn.aimsicd.data.model.Measure;

import java.text.DateFormat;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

import static java.lang.String.valueOf;

/**
 *
 * Inflater class used in DB viewer (for Measured cell strength measurements)
 *
 * @author Tor Henning Ueland
 */
public class MeasuredCellStrengthAdapter extends RealmBaseAdapter<Measure> {

    public MeasuredCellStrengthAdapter(Context context, RealmResults<Measure> realmResults, boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.measured_signal_str, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Measure item = getItem(position);
        holder.updateDisplay(item);

        return convertView;
    }

    private class ViewHolder {

        private final View mRootView;

        private final TextView cid;
        private final TextView rss;
        private final TextView time;

        ViewHolder(View rootView) {
            mRootView = rootView;

            cid =   (TextView) mRootView.findViewById(R.id.tv_measure_cid);
            rss =   (TextView) mRootView.findViewById(R.id.tv_measure_rss);
            time =  (TextView) mRootView.findViewById(R.id.tv_measure_time);

            rootView.setTag(this);
        }

        public void updateDisplay(Measure item) {
            cid.setText(valueOf(item.getBaseStation().getCellId()));
            rss.setText(valueOf(item.getRxSignal()));
            time.setText(DateFormat.getDateTimeInstance().format(item.getTime()));
        }
    }
}

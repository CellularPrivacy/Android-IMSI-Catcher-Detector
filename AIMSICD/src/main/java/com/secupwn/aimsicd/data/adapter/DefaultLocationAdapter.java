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
import com.secupwn.aimsicd.data.model.DefaultLocation;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

import static java.lang.String.valueOf;

public class DefaultLocationAdapter extends RealmBaseAdapter<DefaultLocation> {

    public DefaultLocationAdapter(Context context, RealmResults<DefaultLocation> realmResults, boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.default_location_items, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final DefaultLocation defaultLocation = getItem(position);
        holder.updateDisplay(defaultLocation, position);

        return convertView;
    }

    private class ViewHolder {

        private final View mRootView;
        private final TextView mCountry;
        private final TextView mMcc;
        private final TextView mLat;
        private final TextView mLng;
        private final TextView mRecordId;

        ViewHolder(View rootView) {
            mRootView = rootView;

            mCountry =  (TextView) mRootView.findViewById(R.id.country);
            mMcc =      (TextView) mRootView.findViewById(R.id.mcc);
            mLat =      (TextView) mRootView.findViewById(R.id.lat);
            mLng =      (TextView) mRootView.findViewById(R.id.lng);
            mRecordId = (TextView) mRootView.findViewById(R.id.record_id);

            rootView.setTag(this);
        }

        public void updateDisplay(DefaultLocation defaultLocation, int position) {
            mCountry.setText(defaultLocation.getCountry());
            mMcc.setText(valueOf(defaultLocation.getMobileCountryCode()));
            mLat.setText(valueOf(defaultLocation.getGpsLocation().getLatitude()));
            mLng.setText(valueOf(defaultLocation.getGpsLocation().getLongitude()));
            mRecordId.setText(valueOf(position));
        }
    }
}

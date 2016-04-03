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
import com.secupwn.aimsicd.data.DefaultLocation;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

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

        final DefaultLocation item = getItem(position);
        holder.updateDisplay(item, position);

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

        public void updateDisplay(DefaultLocation item, int position) {
            mCountry.setText(item.getCountry());
            mMcc.setText(item.getMobileCountryCode());
            mLat.setText(String.valueOf(item.getLocationInfo().getLatitude()));
            mLng.setText(String.valueOf(item.getLocationInfo().getLongitude()));
            mRecordId.setText(position);
        }
    }
}

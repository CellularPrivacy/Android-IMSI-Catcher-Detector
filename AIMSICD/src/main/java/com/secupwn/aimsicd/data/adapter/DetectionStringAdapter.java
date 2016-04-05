package com.secupwn.aimsicd.data.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.data.model.SmsDetectionString;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

public class DetectionStringAdapter extends RealmBaseAdapter<SmsDetectionString> {

    public DetectionStringAdapter(Context context, RealmResults<SmsDetectionString> realmResults, boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.detection_strings_items, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SmsDetectionString smsDetectionString = getItem(position);
        holder.updateDisplay(smsDetectionString);

        return convertView;
    }

    private class ViewHolder {

        private final View mRootView;
        private final TextView d_string;
        private final TextView d_type;


        ViewHolder(View rootView) {
            mRootView = rootView;
            d_string = (TextView) mRootView.findViewById(R.id.tv_det_str_info);
            d_type = (TextView) mRootView.findViewById(R.id.tv_det_type_info);

            rootView.setTag(this);
        }

        public void updateDisplay(SmsDetectionString smsDetectionString) {
            d_string.setText(smsDetectionString.getDetectionString());
            d_type.setText(smsDetectionString.getSmsType());
        }
    }
}

/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */

/* Coded by Paul Kinsella <paulkinsella29@yahoo.ie> */

package com.SecUpwN.AIMSICD.smsdetection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.BaseInflaterAdapter;
import com.SecUpwN.AIMSICD.adapters.IAdapterViewInflater;


public class CapturedSmsCardInflater implements IAdapterViewInflater<CapturedSmsData> {

    @Override
    public View inflate(final BaseInflaterAdapter<CapturedSmsData> adapter,
                        final int pos, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.detection_sms_db_listview, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final CapturedSmsData item = adapter.getTItem(pos);
        holder.updateDisplay(item);

        return convertView;
    }

    private class ViewHolder {

        private final View mRootView;
        private final TextView smsd_timestamp,smsd_smstype,smsd_number,smsd_data,
                smsd_lac,smsd_cid,smsd_rat,smsd_roam,smsd_lat,smsd_lon;



        public ViewHolder(View rootView) {
            mRootView = rootView;
            //
            smsd_timestamp = (TextView) mRootView.findViewById(R.id.tv_smsdata_timestamp);
            smsd_smstype = (TextView) mRootView.findViewById(R.id.tv_smsdata_smstype);
            smsd_number = (TextView) mRootView.findViewById(R.id.tv_smsdata_number);
            smsd_data = (TextView) mRootView.findViewById(R.id.tv_smsdata_msg);
            smsd_lac = (TextView) mRootView.findViewById(R.id.tv_smsdata_lac);
            smsd_cid = (TextView) mRootView.findViewById(R.id.tv_smsdata_cid);
            smsd_rat = (TextView) mRootView.findViewById(R.id.tv_smsdata_nettype);
            smsd_roam = (TextView) mRootView.findViewById(R.id.tv_smsdata_roaming);
            smsd_lat = (TextView) mRootView.findViewById(R.id.tv_smsdata_lat);
            smsd_lon = (TextView) mRootView.findViewById(R.id.tv_smsdata_lon);


            rootView.setTag(this);
        }

        public void updateDisplay(CapturedSmsData item) {
            smsd_timestamp.setText(item.getSmsTimestamp());
            smsd_smstype.setText(item.getSmsType());
            smsd_number.setText(item.getSenderNumber());
            smsd_data.setText(item.getSenderMsg());
            smsd_lac.setText(SV(item.getCurrent_lac()));
            smsd_cid.setText(SV(item.getCurrent_cid()));
            smsd_rat.setText(item.getCurrent_nettype());
            String isRoaming = "false";
            if(item.getCurrent_roam_status() == 1){isRoaming = "true";}
            smsd_roam.setText(isRoaming);
            smsd_lat.setText(String.valueOf(item.getCurrent_gps_lat()));
            smsd_lon.setText(String.valueOf(item.getCurrent_gps_lon()));


        }
    }

    public String SV(int value){
        return String.valueOf(value);
    }
}

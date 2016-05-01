/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */

/* Coded by Paul Kinsella <paulkinsella29@yahoo.ie> */

package com.secupwn.aimsicd.data.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.data.model.SmsData;

import java.util.Date;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

import static java.lang.String.valueOf;


public class SmsDataAdapter extends RealmBaseAdapter<SmsData> {
    
    public SmsDataAdapter(Context context, RealmResults<SmsData> realmResults, boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.adv_user_sms_listview, parent, false);
            holder = new ViewHolder();
            holder.smsd_timestamp = (TextView) convertView.findViewById(R.id.tv_adv_smsdata_timestamp);
            holder.smsd_smstype = (TextView) convertView.findViewById(R.id.tv_adv_smsdata_smstype);
            holder.smsd_number = (TextView) convertView.findViewById(R.id.tv_adv_smsdata_number);
            holder.smsd_data = (TextView) convertView.findViewById(R.id.tv_adv_smsdata_msg);
            holder.smsd_lac = (TextView) convertView.findViewById(R.id.tv_adv_smsdata_lac);
            holder.smsd_cid = (TextView) convertView.findViewById(R.id.tv_adv_smsdata_cid);
            holder.smsd_rat = (TextView) convertView.findViewById(R.id.tv_adv_smsdata_nettype);
            holder.smsd_roam = (TextView) convertView.findViewById(R.id.tv_adv_smsdata_roaming);
            holder.smsd_lat = (TextView) convertView.findViewById(R.id.tv_adv_smsdata_lat);
            holder.smsd_lon = (TextView) convertView.findViewById(R.id.tv_adv_smsdata_lon);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Date timestamp = getItem(position).getTimestamp();
        
        holder.smsd_timestamp.setText(java.text.DateFormat.getDateTimeInstance().format(timestamp));
        holder.smsd_smstype.setText(getItem(position).getType());
        holder.smsd_number.setText(getItem(position).getSenderNumber());
        holder.smsd_data.setText(getItem(position).getMessage());
        holder.smsd_lac.setText(valueOf(getItem(position).getLocationAreaCode()));
        holder.smsd_cid.setText(valueOf(getItem(position).getCellId()));
        holder.smsd_rat.setText(getItem(position).getRadioAccessTechnology());
        String isRoaming = "false";
        if (getItem(position).isRoaming()) {
            isRoaming = "true";
        }
        holder.smsd_roam.setText(isRoaming);
        holder.smsd_lat.setText(valueOf(getItem(position).getGpsLocation().getLatitude()));
        holder.smsd_lon.setText(valueOf(getItem(position).getGpsLocation().getLongitude()));

        return convertView;
    }

    static class ViewHolder {

        TextView smsd_timestamp, smsd_smstype, smsd_number, smsd_data,
                smsd_lac, smsd_cid, smsd_rat, smsd_roam, smsd_lat, smsd_lon;
    }
}

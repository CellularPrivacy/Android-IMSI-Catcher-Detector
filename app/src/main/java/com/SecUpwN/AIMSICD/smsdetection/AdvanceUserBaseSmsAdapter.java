/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */

/* Coded by Paul Kinsella <paulkinsella29@yahoo.ie> */

package com.SecUpwN.AIMSICD.smsdetection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;

import java.util.ArrayList;


public class AdvanceUserBaseSmsAdapter extends BaseAdapter {
	private static ArrayList<CapturedSmsData> detectionItemDetails;

	private LayoutInflater l_Inflater;

	public AdvanceUserBaseSmsAdapter(Context context, ArrayList<CapturedSmsData> results) {
        detectionItemDetails = results;
		l_Inflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return detectionItemDetails.size();
	}

	public Object getItem(int position) {
		return detectionItemDetails.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = l_Inflater.inflate(R.layout.adv_user_sms_listview, parent, false);
			holder = new ViewHolder();
            holder.smsd_timestamp = (TextView)convertView.findViewById(R.id.tv_adv_smsdata_timestamp);
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

        holder.smsd_timestamp.setText(detectionItemDetails.get(position).getSmsTimestamp());
        holder.smsd_smstype.setText(detectionItemDetails.get(position).getSmsType());
        holder.smsd_number.setText(detectionItemDetails.get(position).getSenderNumber());
        holder.smsd_data.setText(detectionItemDetails.get(position).getSenderMsg());
        holder.smsd_lac.setText(SV(detectionItemDetails.get(position).getCurrent_lac()));
        holder.smsd_cid.setText(SV(detectionItemDetails.get(position).getCurrent_cid()));
        holder.smsd_rat.setText(detectionItemDetails.get(position).getCurrent_nettype());
		String isRoaming = "false";
		if(detectionItemDetails.get(position).getCurrent_roam_status() == 1){isRoaming = "true";}
        holder.smsd_roam.setText(isRoaming);
        holder.smsd_lat.setText(String.valueOf(detectionItemDetails.get(position).getCurrent_gps_lat()));
        holder.smsd_lon.setText(String.valueOf(detectionItemDetails.get(position).getCurrent_gps_lon()));

		return convertView;
	}

	static class ViewHolder {

        TextView smsd_timestamp,smsd_smstype,smsd_number,smsd_data,
                smsd_lac,smsd_cid,smsd_rat,smsd_roam,smsd_lat,smsd_lon;
	}

    public String SV(int value){
        return String.valueOf(value);
    }
}

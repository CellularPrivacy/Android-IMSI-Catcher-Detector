/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;

public class UniqueBtsCardInflater implements IAdapterViewInflater<UniqueBtsItemData> {

    @Override
    public View inflate(final BaseInflaterAdapter<UniqueBtsItemData> adapter,
                        final int pos, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.unique_bts_data, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final UniqueBtsItemData item = adapter.getTItem(pos);
        holder.updateDisplay(item);

        return convertView;
    }

    private class ViewHolder {

        private final View mRootView;

        private final TextView LAC;
        private final TextView CID;
        private final TextView MCC;
        private final TextView MNC;
        private final TextView PSC;
        private final TextView TIME_FIRST;
        private final TextView TIME_LAST;
        private final TextView LAT;
        private final TextView LON;


        public ViewHolder(View rootView) {
            mRootView = rootView;

            LAC =     (TextView) mRootView.findViewById(R.id.tv_uniquebts_lac);
            CID =     (TextView) mRootView.findViewById(R.id.tv_uniquebts_cid);
            MCC =          (TextView) mRootView.findViewById(R.id.tv_uniquebts_mcc);
            MNC =          (TextView) mRootView.findViewById(R.id.tv_uniquebts_mnc);
            PSC =    (TextView) mRootView.findViewById(R.id.tv_uniquebts_psc);
            TIME_FIRST =      (TextView) mRootView.findViewById(R.id.tv_uniquebts_time_first);
            TIME_LAST =     (TextView) mRootView.findViewById(R.id.tv_uniquebts_time_last);
            LAT =      (TextView) mRootView.findViewById(R.id.tv_uniquebts_lat);
            LON =     (TextView) mRootView.findViewById(R.id.tv_uniquebts_lon);


            rootView.setTag(this);
        }

        public void updateDisplay(UniqueBtsItemData item) {

            LAC.setText(item.getLac());
            CID.setText(item.getCid());

            MCC.setText(item.getMcc());
            MNC.setText(item.getMnc());
            PSC.setText(item.getPsc());

            TIME_FIRST.setText(item.getTime_first());
            TIME_LAST.setText(item.getTime_last());

            LAT.setText(String.valueOf(item.getLat()));
            LON.setText(String.valueOf(item.getLon()));

        }
    }
}

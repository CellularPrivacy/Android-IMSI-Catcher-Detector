package com.SecUpwN.AIMSICD.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;

/**
 *  Brief:   TODO: Please explain its use.
 *
 *  Description:
 *
 *      This class handle all the AMISICD DataBase ... TODO: Add info here !
 *
 *  Dependencies:   CardItemData.java
 *
 *  Issues:
 *
 *  ChangeLog:
 *
 *
 *  Notes:
 *
 */
public class EventLogCardInflater implements IAdapterViewInflater<CardItemData> {

    @Override
    public View inflate(final BaseInflaterAdapter<CardItemData> adapter,
                        final int pos, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.opencelid_items, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final CardItemData item = adapter.getTItem(pos);
        holder.updateDisplay(item);

        return convertView;
    }

    private class ViewHolder {

        private final View mRootView;
        private final TextView mtime;
        private final TextView mLAC;
        private final TextView mCID;
        private final TextView mPSC;
        private final TextView mgpsd_lat;
        private final TextView mgpsd_lon;
        private final TextView mgpsd_accur;
        private final TextView mDF_id;
        private final TextView mDF_description;

        public ViewHolder(View rootView) {
            mRootView = rootView;
            //
            mtime =         (TextView) mRootView.findViewById(R.id.time);
            mLAC =          (TextView) mRootView.findViewById(R.id.LAC);
            mCID =          (TextView) mRootView.findViewById(R.id.CID);
            mPSC =          (TextView) mRootView.findViewById(R.id.PSC);
            mgpsd_lat =     (TextView) mRootView.findViewById(R.id.gpsd_lat);
            mgpsd_lon =     (TextView) mRootView.findViewById(R.id.gpsd_lon);
            mgpsd_accur =   (TextView) mRootView.findViewById(R.id.gpsd_accur);
            mDF_id =        (TextView) mRootView.findViewById(R.id.DF_id);
            mDF_description =   (TextView) mRootView.findViewById(R.id.DF_description);

            rootView.setTag(this);
        }

        public void updateDisplay(CardItemData item) {
            mtime.setText(item.getCellID());
            mLAC.setText(item.getLac());
            mCID.setText(item.getMcc());
            mPSC.setText(item.getMnc());
            mgpsd_lat.setText(item.getLat());
            mgpsd_lon.setText(item.getLng());
            mgpsd_accur.setText(item.getAvgSigStr());
            mDF_id.setText(item.getSamples());
            mDF_description.setText(item.getRecordId());
        }
    }
}

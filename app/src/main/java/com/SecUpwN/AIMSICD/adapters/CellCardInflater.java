package com.SecUpwN.AIMSICD.adapters;

import com.SecUpwN.AIMSICD.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Brief:   TODO: Please explain its use.
 *
 * Description:
 *
 *      This class handle all the AMISICD DataBase ... TODO: Add info here !
 *
 *
 *
 *  Issues:     TODO:  !! === I'm not sure this is the right place... == !! --E:V:A
 *
 *  ChangeLog:
 *
 *
 *  Notes:
 *
 */
public class CellCardInflater implements IAdapterViewInflater<CardItemData> {

    @Override
    public View inflate(final BaseInflaterAdapter<CardItemData> adapter,
                        final int pos, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.cell_items, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final CardItemData item = adapter.getTItem(pos);
        holder.updateDisplay(item);

        return convertView;
    }

    private class ViewHolder {

        // OLD (in old DB tables)
        private final View mRootView;
        private final TextView mCellID;
        private final TextView mCellIDLabel;
        private final TextView mPsc;
        private final TextView mPscLabel;
        private final TextView mLac;
        private final TextView mLacLabel;
        private final TextView mMcc;
        private final TextView mMccLabel;
        private final TextView mMnc;
        private final TextView mMncLabel;
        private final TextView mNet;
        private final TextView mNetLabel;
        private final TextView mLat;
        private final TextView mLatLabel;
        private final TextView mLng;
        private final TextView mLngLabel;
        private final TextView mSignal;
        private final TextView mSignalLabel;
        private final TextView mRecordId;

        /*// NEW (in new DB tables)
        private final View mRootView;
        private final TextView mtime;
        private final TextView mLAC;
        private final TextView mCID;
        private final TextView mPSC;
        private final TextView mgpsd_lat;
        private final TextView mgpsd_lon;
        private final TextView mgpsd_accu;
        private final TextView mDF_id;
        private final TextView mDF_description;
        */

        // OLD (in old DB tables)
        public ViewHolder(View rootView) {

            // Initialize and hide TextViews as well as their labels
            mRootView = rootView;
            mCellID = (TextView) mRootView.findViewById(R.id.cellID);
            mCellID.setVisibility(View.GONE);
            mCellIDLabel = (TextView) mRootView.findViewById(R.id.CellIDLabel);
            mCellIDLabel.setVisibility(View.GONE);
            mPsc = (TextView) mRootView.findViewById(R.id.psc);
            mPsc.setVisibility(View.GONE);
            mPscLabel = (TextView) mRootView.findViewById(R.id.pscLabel);
            mPscLabel.setVisibility(View.GONE);
            mLac = (TextView) mRootView.findViewById(R.id.lac);
            mLac.setVisibility(View.GONE);
            mLacLabel = (TextView) mRootView.findViewById(R.id.lacLabel);
            mLacLabel.setVisibility(View.GONE);
            mMcc = (TextView) mRootView.findViewById(R.id.mcc);
            mMcc.setVisibility(View.GONE);
            mMccLabel = (TextView) mRootView.findViewById(R.id.mccLabel);
            mMccLabel.setVisibility(View.GONE);
            mMnc = (TextView) mRootView.findViewById(R.id.mnc);
            mMnc.setVisibility(View.GONE);
            mMncLabel = (TextView) mRootView.findViewById(R.id.mncLabel);
            mMncLabel.setVisibility(View.GONE);
            mNet = (TextView) mRootView.findViewById(R.id.net);
            mNet.setVisibility(View.GONE);
            mNetLabel = (TextView) mRootView.findViewById(R.id.netLabel);
            mNetLabel.setVisibility(View.GONE);
            mLat = (TextView) mRootView.findViewById(R.id.lat);
            mLat.setVisibility(View.GONE);
            mLatLabel = (TextView) mRootView.findViewById(R.id.latLabel);
            mLatLabel.setVisibility(View.GONE);
            mLng = (TextView) mRootView.findViewById(R.id.lng);
            mLng.setVisibility(View.GONE);
            mLngLabel = (TextView) mRootView.findViewById(R.id.lngLabel);
            mLngLabel.setVisibility(View.GONE);
            mSignal = (TextView) mRootView.findViewById(R.id.signal);
            mSignal.setVisibility(View.GONE);
            mSignalLabel = (TextView) mRootView.findViewById(R.id.signalLabel);
            mSignalLabel.setVisibility(View.GONE);
            mRecordId = (TextView) mRootView.findViewById(R.id.record_id);

            // NEW (in new DB tables)


            rootView.setTag(this);
        }

        /**
         * Method to update cell cards with information applicable to what is
         * being viewed from the database.  If the information is not relevant
         * (N/A) to the current pull from the database then the TextViews, label
         * and information, will stay invisible.
         *
         * @param item
         */
        public void updateDisplay(CardItemData item) {
            if (!item.getCellID().contains("N/A")) {
                mCellIDLabel.setVisibility(View.VISIBLE);
                mCellID.setVisibility(View.VISIBLE);
                mCellID.setText(item.getCellID());
            }

            if (!item.getPsc().contains("N/A")) {
                mPscLabel.setVisibility(View.VISIBLE);
                mPsc.setVisibility(View.VISIBLE);
                mPsc.setText(item.getPsc());
            }

            if (!item.getLac().contains("N/A")) {
                mLacLabel.setVisibility(View.VISIBLE);
                mLac.setVisibility(View.VISIBLE);
                mLac.setText(item.getLac());
            }

            if (!item.getNet().contains("N/A")) {
                mNetLabel.setVisibility(View.VISIBLE);
                mNet.setVisibility(View.VISIBLE);
                mNet.setText(item.getNet());
            }

            if (!item.getLat().contains("N/A")) {
                mLatLabel.setVisibility(View.VISIBLE);
                mLat.setVisibility(View.VISIBLE);
                mLat.setText(item.getLat());
            }

            if (!item.getLng().contains("N/A")) {
                mLngLabel.setVisibility(View.VISIBLE);
                mLng.setVisibility(View.VISIBLE);
                mLng.setText(item.getLng());
            }

            if (!item.getMcc().contains("N/A")) {
                mMccLabel.setVisibility(View.VISIBLE);
                mMcc.setVisibility(View.VISIBLE);
                mMcc.setText(item.getMcc());
            }

            if (!item.getMnc().contains("N/A")) {
                mMncLabel.setVisibility(View.VISIBLE);
                mMnc.setVisibility(View.VISIBLE);
                mMnc.setText(item.getMnc());
            }

            if (!item.getSignal().contains("N/A")) {
                mSignalLabel.setVisibility(View.VISIBLE);
                mSignal.setVisibility(View.VISIBLE);
                mSignal.setText(item.getSignal());
            }

            mRecordId.setText(item.getRecordId());
        }
    }
}

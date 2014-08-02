package com.SecUpwN.AIMSICD.adapters;

import com.SecUpwN.AIMSICD.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CellCardInflater implements IAdapterViewInflater<CardItemData> {

    @Override
    public View inflate(final BaseInflaterAdapter<CardItemData> adapter, final int pos,
            View convertView, ViewGroup parent) {
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

        private final View mRootView;
        private final TextView mCellID;
        private final TextView mLac;
        private final TextView mMcc;
        private final TextView mMnc;
        private final TextView mNet;
        private final TextView mLat;
        private final TextView mLng;
        private final TextView mSignal;

        private final TextView mRecordId;

        public ViewHolder(View rootView) {
            mRootView = rootView;
            mCellID = (TextView) mRootView.findViewById(R.id.cellID);
            mCellID.setVisibility(View.GONE);
            mLac = (TextView) mRootView.findViewById(R.id.lac);
            mLac.setVisibility(View.GONE);
            mMcc = (TextView) mRootView.findViewById(R.id.mcc);
            mMcc.setVisibility(View.GONE);
            mMnc = (TextView) mRootView.findViewById(R.id.mnc);
            mMnc.setVisibility(View.GONE);
            mNet = (TextView) mRootView.findViewById(R.id.net);
            mNet.setVisibility(View.GONE);
            mLat = (TextView) mRootView.findViewById(R.id.lat);
            mLat.setVisibility(View.GONE);
            mLng = (TextView) mRootView.findViewById(R.id.lng);
            mLng.setVisibility(View.GONE);
            mSignal = (TextView) mRootView.findViewById(R.id.signal);
            mSignal.setVisibility(View.GONE);
            mRecordId = (TextView) mRootView.findViewById(R.id.record_id);
            rootView.setTag(this);
        }

        public void updateDisplay(CardItemData item) {
            if (!item.getCellID().contains("N/A")) {
                mCellID.setVisibility(View.VISIBLE);
                mCellID.setText(item.getCellID());
            }

            if (!item.getLac().contains("N/A")) {
                mLac.setVisibility(View.VISIBLE);
                mLac.setText(item.getLac());
            }

            if (!item.getNet().contains("N/A")) {
                mNet.setVisibility(View.VISIBLE);
                mNet.setText(item.getNet());
            }

            if (!item.getLat().contains("N/A")) {
                mLat.setVisibility(View.VISIBLE);
                mLat.setText(item.getLat());
            }

            if (!item.getLng().contains("N/A")) {
                mLng.setVisibility(View.VISIBLE);
                mLng.setText(item.getLng());
            }

            if (!item.getMcc().contains("N/A")) {
                mMcc.setVisibility(View.VISIBLE);
                mMcc.setText(item.getMcc());
            }

            if (!item.getMnc().contains("N/A")) {
                mMnc.setVisibility(View.VISIBLE);
                mMnc.setText(item.getMnc());
            }

            if (!item.getSignal().contains("N/A")) {
                mSignal.setVisibility(View.VISIBLE);
                mSignal.setText(item.getSignal());
            }

            mRecordId.setText(item.getRecordId());
        }
    }
}
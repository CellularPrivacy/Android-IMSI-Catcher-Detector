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
        private final TextView mNet;
        private final TextView mLat;
        private final TextView mLng;
        private final TextView mSignal;
        private final TextView mRecordId;

        public ViewHolder(View rootView) {
            mRootView = rootView;
            mCellID = (TextView) mRootView.findViewById(R.id.cellID);
            mLac = (TextView) mRootView.findViewById(R.id.lac);
            mNet = (TextView) mRootView.findViewById(R.id.net);
            mLat = (TextView) mRootView.findViewById(R.id.lat);
            mLng = (TextView) mRootView.findViewById(R.id.lng);
            mSignal = (TextView) mRootView.findViewById(R.id.signal);
            mRecordId = (TextView) mRootView.findViewById(R.id.record_id);
            rootView.setTag(this);
        }

        public void updateDisplay(CardItemData item) {
            mCellID.setText(item.getCellID());
            mLac.setText(item.getLac());
            mNet.setText(item.getNet());
            mLat.setText(item.getLat());
            mLng.setText(item.getLng());
            mSignal.setText(item.getSignal());
            mRecordId.setText(item.getRecordId());
        }
    }
}
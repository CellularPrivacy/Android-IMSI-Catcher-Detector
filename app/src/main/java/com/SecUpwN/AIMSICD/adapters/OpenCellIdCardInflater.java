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

public class OpenCellIdCardInflater implements IAdapterViewInflater<CardItemData> {

    @Override
    public View inflate(final BaseInflaterAdapter<CardItemData> adapter, final int pos,
            View convertView, ViewGroup parent) {
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
        private final TextView mCellID;
        private final TextView mLac;
        private final TextView mMcc;
        private final TextView mMnc;
        private final TextView mLat;
        private final TextView mLng;
        private final TextView mAvgSigStr;
        private final TextView mSamples;
        private final TextView mRecordId;

        public ViewHolder(View rootView) {
            mRootView = rootView;
            mCellID =   (TextView) mRootView.findViewById(R.id.cellID);
            mLac =      (TextView) mRootView.findViewById(R.id.lac);
            mMcc =      (TextView) mRootView.findViewById(R.id.mcc);
            mMnc =      (TextView) mRootView.findViewById(R.id.mnc);
            mLat =      (TextView) mRootView.findViewById(R.id.lat);
            mLng =      (TextView) mRootView.findViewById(R.id.lng);
            mAvgSigStr = (TextView) mRootView.findViewById(R.id.avgSigStr);
            mSamples =  (TextView) mRootView.findViewById(R.id.samples);
            mRecordId = (TextView) mRootView.findViewById(R.id.record_id);
            rootView.setTag(this);
        }

        public void updateDisplay(CardItemData item) {
            mCellID.setText(item.getCellID());
            mLac.setText(item.getLac());
            mMcc.setText(item.getMcc());
            mMnc.setText(item.getMnc());
            mLat.setText(item.getLat());
            mLng.setText(item.getLng());
            mAvgSigStr.setText(item.getAvgSigStr());
            mSamples.setText(item.getSamples());
            mRecordId.setText(item.getRecordId());
        }
    }
}

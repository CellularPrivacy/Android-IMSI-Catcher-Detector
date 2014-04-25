package com.SecUpwN.AIMSICD.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.SecUpwN.AIMSICD.R;

public class OpenCellIdCardInflater implements IAdapterViewInflater<CardItemData>
{
    @Override
    public View inflate(final BaseInflaterAdapter<CardItemData> adapter, final int pos, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        if (convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.opencelid_items, parent, false);
            holder = new ViewHolder(convertView);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        final CardItemData item = adapter.getTItem(pos);
        holder.updateDisplay(item);

        return convertView;
    }

    private class ViewHolder
    {
        private View mRootView;
        private TextView mCellID;
        private TextView mLac;
        private TextView mMcc;
        private TextView mMnc;
        private TextView mLat;
        private TextView mLng;
        private TextView mAvgSigStr;
        private TextView mSamples;

        public ViewHolder(View rootView)
        {
            mRootView = rootView;
            mCellID = (TextView) mRootView.findViewById(R.id.cellID);
            mLac = (TextView) mRootView.findViewById(R.id.lac);
            mMcc = (TextView) mRootView.findViewById(R.id.mcc);
            mMnc = (TextView) mRootView.findViewById(R.id.mnc);
            mLat = (TextView) mRootView.findViewById(R.id.lat);
            mLng = (TextView) mRootView.findViewById(R.id.lng);
            mAvgSigStr = (TextView) mRootView.findViewById(R.id.avgSigStr);
            mSamples = (TextView) mRootView.findViewById(R.id.samples);
            rootView.setTag(this);
        }

        public void updateDisplay(CardItemData item)
        {
            mCellID.setText(item.getCellID());
            mLac.setText(item.getLac());
            mMcc.setText(item.getMcc());
            mMnc.setText(item.getMnc());
            mLat.setText(item.getLat());
            mLng.setText(item.getLng());
            mAvgSigStr.setText(item.getAvgSigStr());
            mSamples.setText(item.getSamples());
        }
    }
}

package com.SecUpwN.AIMSICD.adapters;

import com.SecUpwN.AIMSICD.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DefaultLocationCardInflater implements IAdapterViewInflater<CardItemData>
{
    @Override
    public View inflate(final BaseInflaterAdapter<CardItemData> adapter, final int pos, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        if (convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.default_location_items, parent, false);
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
        private final View mRootView;
        private final TextView mCountry;
        private final TextView mMcc;
        private final TextView mLat;
        private final TextView mLng;

        public ViewHolder(View rootView)
        {
            mRootView = rootView;
            mCountry = (TextView) mRootView.findViewById(R.id.country);
            mMcc = (TextView) mRootView.findViewById(R.id.mcc);
            mLat = (TextView) mRootView.findViewById(R.id.lat);
            mLng = (TextView) mRootView.findViewById(R.id.lng);
            rootView.setTag(this);
        }

        public void updateDisplay(CardItemData item)
        {
            mCountry.setText(item.getCountry());
            mMcc.setText(item.getMcc());
            mLat.setText(item.getLat());
            mLng.setText(item.getLng());
        }
    }
}

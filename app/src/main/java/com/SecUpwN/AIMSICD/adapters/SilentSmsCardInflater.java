package com.SecUpwN.AIMSICD.adapters;

import com.SecUpwN.AIMSICD.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SilentSmsCardInflater implements IAdapterViewInflater<SilentSmsCardData> {

    @Override
    public View inflate(final BaseInflaterAdapter<SilentSmsCardData> adapter, final int pos,
            View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.silent_sms_items, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final SilentSmsCardData item = adapter.getTItem(pos);
        holder.updateDisplay(item);

        return convertView;
    }

    private class ViewHolder {

        private final View mRootView;
        private final TextView mAddress;
        private final TextView mDisplayAddress;
        private final TextView mMessageClass;
        private final TextView mServiceCentre;
        private final TextView mMessageBody;
        private final TextView mTimestamp;

        public ViewHolder(View rootView) {
            mRootView = rootView;
            mAddress =          (TextView) mRootView.findViewById(R.id.address);
            mDisplayAddress =   (TextView) mRootView.findViewById(R.id.display_address);
            mMessageClass =     (TextView) mRootView.findViewById(R.id.message_class);
            mServiceCentre =    (TextView) mRootView.findViewById(R.id.service_centre);
            mMessageBody =      (TextView) mRootView.findViewById(R.id.message_body);
            mTimestamp =        (TextView) mRootView.findViewById(R.id.message_timestamp);
            rootView.setTag(this);
        }

        public void updateDisplay(SilentSmsCardData item) {
            mAddress.setText(       item.getAddress());
            mDisplayAddress.setText(item.getDisplayAddress());
            mMessageClass.setText(  item.getMessageClass());
            mServiceCentre.setText( item.getServiceCentre());
            mMessageBody.setText(   item.getMessage());
            mTimestamp.setText(     item.getTimestamp());
        }
    }
}

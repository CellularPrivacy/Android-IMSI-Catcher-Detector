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
/**
 *
 * Inflater class used in DB viewer (for Measured cell strength measurements)
 *
 * Template:    SilentSmsCardInflater.java
 * TODO:        Fix variable names!!
 *
 * @author Tor Henning Ueland
 */
public class MeasuredCellStrengthCardInflater implements IAdapterViewInflater<MeasuredCellStrengthCardData> {

    @Override
    public View inflate(final BaseInflaterAdapter<MeasuredCellStrengthCardData> adapter, final int pos,
            View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.silent_sms_items, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final MeasuredCellStrengthCardData item = adapter.getTItem(pos);
        holder.updateDisplay(item);

        return convertView;
    }

    private class ViewHolder {

        private final View mRootView;
        private final TextView mAddress;
        private final TextView mDisplayAddress;
        private final TextView mMessageClass;

        public ViewHolder(View rootView) {
            mRootView = rootView;
            mAddress =          (TextView) mRootView.findViewById(R.id.address);
            mDisplayAddress =   (TextView) mRootView.findViewById(R.id.display_address);
            mMessageClass =     (TextView) mRootView.findViewById(R.id.message_class);

            rootView.setTag(this);
        }

        public void updateDisplay(MeasuredCellStrengthCardData item) {
            mAddress.setText(       item.getCellID());
            mDisplayAddress.setText(item.getSignal());
            mMessageClass.setText(  item.getTimestamp());
        }
    }
}

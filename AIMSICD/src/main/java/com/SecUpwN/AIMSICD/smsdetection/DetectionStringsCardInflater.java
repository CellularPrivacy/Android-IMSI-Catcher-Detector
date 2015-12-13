/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */

/* Coded by Paul Kinsella <paulkinsella29@yahoo.ie> */

package com.SecUpwN.AIMSICD.smsdetection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.BaseInflaterAdapter;
import com.SecUpwN.AIMSICD.adapters.IAdapterViewInflater;

public class DetectionStringsCardInflater implements IAdapterViewInflater<DetectionStringsData> {

    @Override
    public View inflate(final BaseInflaterAdapter<DetectionStringsData> adapter,
                        final int pos, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.detection_strings_items, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final DetectionStringsData item = adapter.getTItem(pos);
        holder.updateDisplay(item);

        return convertView;
    }

    private class ViewHolder {

        private final View mRootView;
        private final TextView d_string;
        private final TextView d_type;


        public ViewHolder(View rootView) {
            mRootView = rootView;
            //
            d_string =         (TextView) mRootView.findViewById(R.id.tv_det_str_info);
            d_type =          (TextView) mRootView.findViewById(R.id.tv_det_type_info);

            rootView.setTag(this);
        }

        public void updateDisplay(DetectionStringsData item) {
            d_string.setText(      item.getDetectionString());
            d_type.setText(       item.getDetectionType());
        }
    }
}

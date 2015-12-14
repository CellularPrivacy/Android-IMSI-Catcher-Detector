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
 *  Brief:          TODO: Please explain its use.
 *
 *  Description:
 *
 *      This class handle the EventLog DB table ... TODO: Add info here !
 *
 *  Template:       OpenCellIdCardInflater.java
 *
 *  Dependencies:   EventLogItemData.java
 *                  eventlog_items.xml
 *
 *  Issues:
 *
 *  ChangeLog:
 *
 *
 *  Notes:
 *
 */
public class EventLogCardInflater implements IAdapterViewInflater<EventLogItemData> {

    @Override
    public View inflate(final BaseInflaterAdapter<EventLogItemData> adapter,
                        final int pos, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.eventlog_items, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final EventLogItemData item = adapter.getTItem(pos);
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
        private final TextView mgpsd_accu;
        private final TextView mDF_id;
        private final TextView mDF_desc;

        private final TextView mRecordId;
        private final TextView mExample;

        public ViewHolder(View rootView) {
            mRootView = rootView;

            mtime =         (TextView) mRootView.findViewById(R.id.time);
            mLAC =          (TextView) mRootView.findViewById(R.id.LAC);
            mCID =          (TextView) mRootView.findViewById(R.id.CID);
            mPSC =          (TextView) mRootView.findViewById(R.id.PSC);
            mgpsd_lat =     (TextView) mRootView.findViewById(R.id.gpsd_lat);
            mgpsd_lon =     (TextView) mRootView.findViewById(R.id.gpsd_lon);
            mgpsd_accu =    (TextView) mRootView.findViewById(R.id.gpsd_accu);
            mDF_id =        (TextView) mRootView.findViewById(R.id.DF_id);
            mDF_desc =      (TextView) mRootView.findViewById(R.id.DF_desc);

            mRecordId =     (TextView) mRootView.findViewById(R.id.record_id);
            mExample =      (TextView) mRootView.findViewById(R.id.example);

            rootView.setTag(this);
        }

        public void updateDisplay(EventLogItemData item) {
            mtime.setText(      item.getTimestamp());          // need fix ?
            mLAC.setText(       item.getLac());
            mCID.setText(       item.getCellID());
            mPSC.setText(       item.getPsc());
            mgpsd_lat.setText(  item.getLat());
            mgpsd_lon.setText(  item.getLng());
            mgpsd_accu.setText( item.getgpsd_accu());
            mDF_id.setText(     item.getDF_id());
            mDF_desc.setText(   item.getDF_desc());

            mRecordId.setText(  item.getRecordId());
            if(item.isFakeData()) {
                mExample.setText(mRootView.getContext().getString(R.string.example))  ;
                mExample.setVisibility(View.VISIBLE);
            } else {
                mExample.setVisibility(View.GONE);
            }
        }
    }
}

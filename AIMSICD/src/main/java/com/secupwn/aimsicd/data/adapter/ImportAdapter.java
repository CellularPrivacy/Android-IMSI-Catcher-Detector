/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.data.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.data.model.Import;

import java.text.DateFormat;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

import static java.lang.String.valueOf;

public class ImportAdapter extends RealmBaseAdapter<Import> {

    public ImportAdapter(Context context, RealmResults<Import> realmResults, boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.dbe_import_items, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Import anImport = getItem(position);
        holder.updateDisplay(anImport, position);
        return convertView;
    }

    private class ViewHolder {

        private final View mRootView;

        private final TextView DB_SOURCE;
        private final TextView RAT;
        private final TextView MCC;
        private final TextView MNC;
        private final TextView LAC;
        private final TextView CID;
        private final TextView PSC;
        private final TextView GPS_LAT;
        private final TextView GPS_LON;
        private final TextView IS_GPS_EXACT;
        private final TextView AVG_RANGE;
        private final TextView AVG_SIGNAL;
        private final TextView SAMPLES;
        private final TextView TIME_FIRST;
        private final TextView TIME_LAST;
        private final TextView REJ_CAUSE;

        private final TextView mRecordId;

        ViewHolder(View rootView) {
            mRootView = rootView;
            // TODO: explain these and try to adhere to a naming convention
            // These are the id names as used in the "dbe_import_items.xml" stylesheet
            DB_SOURCE =     (TextView) mRootView.findViewById(R.id.dbsource);
            RAT =           (TextView) mRootView.findViewById(R.id.RAT);
            MCC =           (TextView) mRootView.findViewById(R.id.nMCC);
            MNC =           (TextView) mRootView.findViewById(R.id.nMNC);
            LAC =           (TextView) mRootView.findViewById(R.id.nLAC);
            CID =           (TextView) mRootView.findViewById(R.id.nCID);
            PSC =           (TextView) mRootView.findViewById(R.id.nPSC);
            GPS_LAT =       (TextView) mRootView.findViewById(R.id.ngpsd_lat);
            GPS_LON =       (TextView) mRootView.findViewById(R.id.ngpsd_lon);
            IS_GPS_EXACT =  (TextView) mRootView.findViewById(R.id.is_exact);
            AVG_RANGE =     (TextView) mRootView.findViewById(R.id.navg_range);
            AVG_SIGNAL =    (TextView) mRootView.findViewById(R.id.navg_signal);
            SAMPLES =       (TextView) mRootView.findViewById(R.id.nSAMPLES);
            TIME_FIRST =    (TextView) mRootView.findViewById(R.id.nTIME_FIRST);
            TIME_LAST =     (TextView) mRootView.findViewById(R.id.nTIME_LAST);
            REJ_CAUSE =     (TextView) mRootView.findViewById(R.id.nREJ_CAUSE);

            mRecordId =     (TextView) mRootView.findViewById(R.id.record_id);
            rootView.setTag(this);
        }

        public void updateDisplay(Import anImport, int pos) {
            DB_SOURCE.setText(anImport.getDbSource());
            RAT.setText(anImport.getRadioAccessTechnology());
            MCC.setText(valueOf(anImport.getMobileCountryCode()));
            MNC.setText(valueOf(anImport.getMobileNetworkCode()));
            LAC.setText(valueOf(anImport.getLocationAreaCode()));
            CID.setText(valueOf(anImport.getCellId()));
            PSC.setText(valueOf(anImport.getPrimaryScramblingCode()));
            GPS_LAT.setText(valueOf(anImport.getGpsLocation().getLatitude()));
            GPS_LON.setText(valueOf(anImport.getGpsLocation().getLongitude()));
            IS_GPS_EXACT.setText(valueOf(anImport.isGpsExact()));
            AVG_RANGE.setText(valueOf(anImport.getAvgRange()));
            AVG_SIGNAL.setText(valueOf(anImport.getAvgSignal()));
            SAMPLES.setText(valueOf(anImport.getSamples()));

            DateFormat df = DateFormat.getDateTimeInstance();
            TIME_FIRST.setText(df.format(anImport.getTimeFirst()));
            TIME_LAST.setText(df.format(anImport.getTimeLast()));

            REJ_CAUSE.setText(valueOf(anImport.getRejCause()));

            mRecordId.setText(valueOf(pos));
        }
    }
}

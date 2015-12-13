package com.SecUpwN.AIMSICD.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;

/**
 * Description:     Contains the data and definitions of all the items of the XML layout
 *
 * Dependencies:
 *                  DbViewerFragment.java: BuildTable()
 *                  BtsMeasureItemData.java
 *                  bts_measure_data.xml
 *
 * TODO:
 *                  [ ] Order all the items according to appearance found in the DB table below
 *                      (Is this the right way to do it?)
 *                  [ ] Only show items currently supported
 *                  [ ] Add record id "n/XXX"
 *
 *
 * NOTE:
 *                  **  LAC and CID are not directly in this table, but referenced through
 *                      the foreign key "bts_id" to the DBi_bts table.
 *
 *
 *      CREATE TABLE "DBi_measure"  (
 *        "_id"           INTEGER PRIMARY KEY AUTOINCREMENT,
 *        "bts_id"       	INTEGER NOT NULL,	-- DBi_bts:_id
 *        "nc_list"      	TEXT,			    -- Neighboring Cells List (TODO: specify content)
 *        "time"         	INTEGER NOT NULL,	-- [s]
 *        "gpsd_lat"     	REAL,			    -- Device GPS (allow NULL)
 *        "gpsd_lon"     	REAL,			    -- Device GPS (allow NULL)
 *        "gpsd_accu"	INTEGER,		        -- Device GPS position accuracy [m]
 *        "gpse_lat"     	REAL,			    -- Exact GPS		(from where? DBi_import?)
 *        "gpse_lon"     	REAL,			    -- Exact GPS		(from where? DBi_import?)
 *        "bb_power"     	TEXT,			    -- [mW] or [mA]		(from BP power rail usage)
 *        "bb_rf_temp"   	TEXT,			    -- [C]			(from BP internal thermistor)
 *        "tx_power"     	TEXT,			    -- [dBm]		(from BP )
 *        "rx_signal"    	TEXT,			    -- [dBm] or ASU		(from AP/BP)
 *        "rx_stype"     	TEXT,			    -- Reveived Signal power Type [RSSI, ...] etc.
 *        "RAT"		TEXT NOT NULL,		        -- Radio Access Technology
 *        "BCCH"         	TEXT,			    -- Broadcast Channel		-- consider INTEGER
 *        "TMSI"         	TEXT,			    -- Temporary IMSI (hex)
 *        "TA"           	INTEGER DEFAULT 0,	-- Timing Advance (GSM, LTE)	-- allow NULL
 *        "PD"           	INTEGER DEFAULT 0,	-- Propagation Delay (LTE)	-- allow NULL
 *        "BER"          	INTEGER DEFAULT 0,	-- Bit Error Rate		-- allow NULL
 *        "AvgEcNo"      	TEXT,			    -- Average Ec/No		-- consider REAL
 *        "isSubmitted"  	INTEGER DEFAULT 0,	-- * Has been submitted to OCID/MLS etc?
 *        "isNeighbour"  	INTEGER DEFAULT 0,	-- * Is a neighboring BTS? [Is this what we want?]
 *        FOREIGN KEY("bts_id")			        --
 *        REFERENCES "DBi_bts"("_id")		    --
 *      );
 *
 *
 * ChangeLog:
 *                  2015-07-08  Marvin Arnold   Initial commit
 *                  2015-07-27  E:V:A           Added placeholders for missing items
 *
 */
public class BtsMeasureCardInflater implements IAdapterViewInflater<BtsMeasureItemData> {

    @Override
    public View inflate(final BaseInflaterAdapter<BtsMeasureItemData> adapter,
                        final int pos, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.bts_measure_data, parent, false);
            holder = new ViewHolder(convertView);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final BtsMeasureItemData item = adapter.getTItem(pos);
        holder.updateDisplay(item);

        return convertView;
    }

    private class ViewHolder {

        private final View mRootView;

        private TextView bts_id;
        private TextView nc_list;
        private TextView time;
        private TextView gpsd_lat;
        private TextView gpsd_lon;
        private TextView gpsd_accu;
        private TextView gpse_lat;
        private TextView gpse_lon;
        private TextView bb_power;
        private TextView bb_rf_temp;
        private TextView tx_power;
        private TextView rx_signal;
        private TextView rx_stype;
        private TextView rat;
        private TextView BCCH;
        private TextView TMSI;
        private TextView TA;
        private TextView PD;
        private TextView BER;
        private TextView AvgEcNo;
        private TextView isSubmitted;
        private TextView isNeighbour;

        private TextView mRecordId;     //EVA

        public ViewHolder(View rootView) {
            mRootView = rootView;

            bts_id = (TextView) mRootView.findViewById(R.id.tv_bts_measure_bts_id);
            nc_list = (TextView) mRootView.findViewById(R.id.tv_bts_measure_nc_list);
            time = (TextView) mRootView.findViewById(R.id.tv_bts_measure_time);
            gpsd_lat = (TextView) mRootView.findViewById(R.id.tv_bts_measure_gpsd_lat);
            gpsd_lon = (TextView) mRootView.findViewById(R.id.tv_bts_measure_gpsd_lon);
            gpsd_accu = (TextView) mRootView.findViewById(R.id.tv_bts_measure_gpsd_accu);
            //gpse_lat = (TextView) mRootView.findViewById(R.id.tv_bts_measure_gpse_lat);
            //gpse_lon = (TextView) mRootView.findViewById(R.id.tv_bts_measure_gpse_lon);
            //bb_power = (TextView) mRootView.findViewById(R.id.tv_bts_measure_bb_power);
            //bb_rf_temp = (TextView) mRootView.findViewById(R.id.tv_bts_measure_bb_rf_temp);
            //tx_power = (TextView) mRootView.findViewById(R.id.tv_bts_measure_tx_power);
            rx_signal = (TextView) mRootView.findViewById(R.id.tv_bts_measure_rx_signal);
            //rx_stype = (TextView) mRootView.findViewById(R.id.tv_bts_measure_rx_stype);
            rat = (TextView) mRootView.findViewById(R.id.tv_bts_measure_rat);
            //BCCH = (TextView) mRootView.findViewById(R.id.tv_bts_measure_BCCH);
            //TMSI = (TextView) mRootView.findViewById(R.id.tv_bts_measure_TMSI);
            //TA = (TextView) mRootView.findViewById(R.id.tv_bts_measure_TA);
            //PD = (TextView) mRootView.findViewById(R.id.tv_bts_measure_PD);
            //BER = (TextView) mRootView.findViewById(R.id.tv_bts_measure_BER);
            //AvgEcNo = (TextView) mRootView.findViewById(R.id.tv_bts_measure_AvgEcNo);
            isSubmitted = (TextView) mRootView.findViewById(R.id.tv_bts_measure_isSubmitted);
            isNeighbour = (TextView) mRootView.findViewById(R.id.tv_bts_measure_isNeighbour);

            mRecordId = (TextView) mRootView.findViewById(R.id.record_id);      // EVA
            rootView.setTag(this);
        }

        public void updateDisplay(BtsMeasureItemData item) {

           bts_id.setText(item.getBts_id());
            nc_list.setText(item.getNc_list());
            time.setText(item.getTime());
            gpsd_lat.setText(item.getGpsd_lat());
            gpsd_lon.setText(item.getGpsd_lon());
            gpsd_accu.setText(item.getGpsd_accu());
//            gpse_lat.setText(item.getGpse_lat());
//            gpse_lon.setText(item.getGpse_lon());
//            bb_power.setText(item.getBb_power());
//            bb_rf_temp.setText(item.getBb_rf_temp());
//            tx_power.setText(item.getTx_power());
            rx_signal.setText(item.getRx_signal());
//            rx_stype.setText(item.getRx_stype());
            rat.setText(item.getRat());
//            BCCH.setText(item.getBCCH());
//            TMSI.setText(item.getTMSI());
//            TA.setText(item.getTA());
//            PD.setText(item.getPD());
//            BER.setText(item.getBER());
//            AvgEcNo.setText(item.getAvgEcNo());
            isSubmitted.setText(item.getIsSubmitted());
            isNeighbour.setText(item.getIsNeighbour());

            mRecordId.setText(item.getRecordId());      // EVA
        }
    }
}

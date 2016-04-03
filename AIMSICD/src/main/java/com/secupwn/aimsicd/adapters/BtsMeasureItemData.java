package com.secupwn.aimsicd.adapters;

import lombok.Getter;
import lombok.Setter;

/**
 * Description:     Contains the data and definitions of all the items of the XML layout
 *
 * Dependencies:
 *                  DbViewerFragment.java: BuildTable()
 *                  BtsMeasureCardInflater.java
 *                  bts_measure_data.xml
 *
 * TODO:
 *                  [ ] Order all the items according to appearance found in the DB table below
 *
 * NOTE:
 *
 *      CREATE TABLE "DBi_measure"  (
 *        "_id"             INTEGER PRIMARY KEY AUTOINCREMENT,
 *        "bts_id"          INTEGER NOT NULL,   -- DBi_bts:_id
 *        "nc_list"         TEXT,               -- Neighboring Cells List (TODO: specify content)
 *        "time"            INTEGER NOT NULL,   -- [s]
 *        "gpsd_lat"        REAL,               -- Device GPS (allow NULL)
 *        "gpsd_lon"        REAL,               -- Device GPS (allow NULL)
 *        "gpsd_accu"       INTEGER,            -- Device GPS position accuracy [m]
 *        "gpse_lat"        REAL,               -- Exact GPS        (from where? DBi_import?)
 *        "gpse_lon"        REAL,               -- Exact GPS        (from where? DBi_import?)
 *        "bb_power"        TEXT,               -- [mW] or [mA]     (from BP power rail usage)
 *        "bb_rf_temp"      TEXT,               -- [C]              (from BP internal thermistor)
 *        "tx_power"        TEXT,               -- [dBm]            (from BP )
 *        "rx_signal"       TEXT,               -- [dBm] or ASU     (from AP/BP)
 *        "rx_stype"        TEXT,               -- Reveived Signal power Type [RSSI, ...] etc.
 *        "RAT"             TEXT NOT NULL,      -- Radio Access Technology
 *        "BCCH"            TEXT,               -- Broadcast Channel    -- consider INTEGER
 *        "TMSI"            TEXT,               -- Temporary IMSI (hex)
 *        "TA"              INTEGER DEFAULT 0,  -- Timing Advance (GSM, LTE)-- allow NULL
 *        "PD"              INTEGER DEFAULT 0,  -- Propagation Delay (LTE)  -- allow NULL
 *        "BER"             INTEGER DEFAULT 0,  -- Bit Error Rate           -- allow NULL
 *        "AvgEcNo"         TEXT,               -- Average Ec/No            -- consider REAL
 *        "isSubmitted"     INTEGER DEFAULT 0,  -- * Has been submitted to OCID/MLS etc?
 *        "isNeighbour"     INTEGER DEFAULT 0,  -- * Is a neighboring BTS? [Is this what we want?]
 *        FOREIGN KEY("bts_id")
 *        REFERENCES "DBi_bts"("_id")
 *      );
 */
@Getter
@Setter
public class BtsMeasureItemData  {

    private String bts_id;
    private String nc_list;
    private String time;
    private String gpsd_lat;
    private String gpsd_lon;
    private String gpsd_accu;
    private String gpse_lat;
    private String gpse_lon;
    private String bb_power;
    private String bb_rf_temp;
    private String tx_power;
    private String rx_signal;
    private String rx_stype;
    private String rat;
    private String BCCH;
    private String TMSI;
    private String TA;
    private String PD;
    private String BER;
    private String AvgEcNo;
    private String isSubmitted;
    private String isNeighbour;

    private String recordId;

    public BtsMeasureItemData(
            String _bts_id,
            String _nc_list,
            String _time,
            String _gpsd_lat,
            String _gpsd_lon,
            String _gpsd_accu,
//            String _gpse_lat,
//            String _gpse_lon,
//            String _bb_power,
//            String _bb_rf_temp,
//            String _tx_power,
            String _rx_signal,
//            String _rx_stype,
            String _rat,
//            String _BCCH,
//            String _TMSI,
//            String _TA,
//            String _PD,
//            String _BER,
//            String _AvgEcNo,
            String _isSubmitted,
            String _isNeighbour,
            String _mRecordId) {

        this.bts_id = _bts_id;
        this.nc_list = _nc_list;
        this.time = _time;
        this.gpsd_lat = _gpsd_lat;
        this.gpsd_lon = _gpsd_lon;
        this.gpsd_accu = _gpsd_accu;
//        this.gpse_lat = _gpse_lat;
//        this.gpse_lon = _gpse_lon;
//        this.bb_power = _bb_power;
//        this.bb_rf_temp = _bb_rf_temp;
//        this.tx_power = _tx_power;
        this.rx_signal = _rx_signal;
//        this.rx_stype = _rx_stype;
        this.rat = _rat;
//        this.BCCH = _BCCH;
//        this.TMSI = _TMSI;
//        this.TA = _TA;
//        this.PD = _PD;
//        this.BER = _BER;
//        this.AvgEcNo = _AvgEcNo;
        this.isSubmitted = _isSubmitted;
        this.isNeighbour = _isNeighbour;

        this.recordId = _mRecordId;
    }

    public BtsMeasureItemData(String... args) {
        this(
                args[0],    // bts_id
                args[1],    // nc_list
                args[2],    // time
                args[3],    // gpsd_lat
                args[4],    // gpsd_lon
                args[5],    // gpsd_accu
                args[6],    // rx_signal
                args[7],    // rat
                args[8],    // isSubmitted
                args[9],    // isNeighbour
                args[10]    // recordId        // EVA
                //,
        );
    }
}

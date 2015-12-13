package com.SecUpwN.AIMSICD.adapters;

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
public class BtsMeasureItemData  {
    public String getBts_id() {
        return bts_id;
    }

    public void setBts_id(String bts_id) {
        this.bts_id = bts_id;
    }

    public String getNc_list() {
        return nc_list;
    }

    public void setNc_list(String nc_list) {
        this.nc_list = nc_list;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getGpsd_lat() {
        return gpsd_lat;
    }

    public void setGpsd_lat(String gpsd_lat) {
        this.gpsd_lat = gpsd_lat;
    }

    public String getGpsd_lon() {
        return gpsd_lon;
    }

    public void setGpsd_lon(String gpsd_lon) {
        this.gpsd_lon = gpsd_lon;
    }

    public String getGpsd_accu() {
        return gpsd_accu;
    }

    public void setGpsd_accu(String gpsd_accu) {
        this.gpsd_accu = gpsd_accu;
    }

    public String getGpse_lat() {
        return gpse_lat;
    }

    public void setGpse_lat(String gpse_lat) {
        this.gpse_lat = gpse_lat;
    }

    public String getGpse_lon() {
        return gpse_lon;
    }

    public void setGpse_lon(String gpse_lon) {
        this.gpse_lon = gpse_lon;
    }

    public String getBb_power() {
        return bb_power;
    }

    public void setBb_power(String bb_power) {
        this.bb_power = bb_power;
    }

    public String getBb_rf_temp() {
        return bb_rf_temp;
    }

    public void setBb_rf_temp(String bb_rf_temp) {
        this.bb_rf_temp = bb_rf_temp;
    }

    public String getTx_power() {
        return tx_power;
    }

    public void setTx_power(String tx_power) {
        this.tx_power = tx_power;
    }

    public String getRx_signal() {
        return rx_signal;
    }

    public void setRx_signal(String rx_signal) {
        this.rx_signal = rx_signal;
    }

    public String getRx_stype() {
        return rx_stype;
    }

    public void setRx_stype(String rx_stype) {
        this.rx_stype = rx_stype;
    }

    public String getRat() {
        return rat;
    }

    public void setRat(String rat) {
        this.rat = rat;
    }

    public String getBCCH() {
        return BCCH;
    }

    public void setBCCH(String BCCH) {
        this.BCCH = BCCH;
    }

    public String getTMSI() {
        return TMSI;
    }

    public void setTMSI(String TMSI) {
        this.TMSI = TMSI;
    }

    public String getTA() {
        return TA;
    }

    public void setTA(String TA) {
        this.TA = TA;
    }

    public String getPD() {
        return PD;
    }

    public void setPD(String PD) {
        this.PD = PD;
    }

    public String getBER() {
        return BER;
    }

    public void setBER(String BER) {
        this.BER = BER;
    }

    public String getAvgEcNo() {
        return AvgEcNo;
    }

    public void setAvgEcNo(String avgEcNo) {
        AvgEcNo = avgEcNo;
    }

    public String getIsSubmitted() {
        return isSubmitted;
    }

    public void setIsSubmitted(String isSubmitted) {
        this.isSubmitted = isSubmitted;
    }

    public String getIsNeighbour() {
        return isNeighbour;
    }

    public void setIsNeighbour(String isNeighbour) {
        this.isNeighbour = isNeighbour;
    }

    public String getRecordId() {
        return mRecordId;
    }

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

    private String mRecordId;

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

        this.mRecordId = _mRecordId;
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
                args[10]    // mRecordId        // EVA
                //,
        );
    }


}
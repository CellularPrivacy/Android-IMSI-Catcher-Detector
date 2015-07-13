package com.SecUpwN.AIMSICD.adapters;

/**
 * Created by Marvin Arnold on 8/07/15.
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

    public BtsMeasureItemData(
            String _bts_id,
            String _nc_list,
            String _time,
            String _gpsd_lat,
            String _gpsd_lon,
            String _gpsd_accu,
//            String _gpse_lat,
//            String _gpse_lon,
            String _bb_power,
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
            String _isNeighbour) {

        this.bts_id = _bts_id;
        this.nc_list = _nc_list;
        this.time = _time;
        this.gpsd_lat = _gpsd_lat;
        this.gpsd_lon = _gpsd_lon;
        this.gpsd_accu = _gpsd_accu;
//        this.gpse_lat = _gpse_lat;
//        this.gpse_lon = _gpse_lon;
        this.bb_power = _bb_power;
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
    }

    public BtsMeasureItemData(String... args) {
        this(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6],
                args[7],
                args[8],
                args[9],
                args[10]
        );
    }


}
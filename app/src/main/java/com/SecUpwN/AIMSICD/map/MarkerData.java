package com.SecUpwN.AIMSICD.map;

/**
 * Class to hold data for displaying in BTS pin popup dialog
 */
public class MarkerData {

    public final String cellID;
    public final String lat;
    public final String lng;
    public final String lac;
    private final String mcc;
    private final String mnc;
    private final String samples;
    public final boolean openCellID;

    public MarkerData(String cell_id,
               String latitude,
               String longitude,
               String local_area_code,
               String mobile_country_code,
               String mobile_network_code,
               String samples_taken,
               boolean openCellID_Data) {
        cellID = cell_id;
        lat = latitude;
        lng = longitude;
        lac = local_area_code;
        mcc = mobile_country_code;
        mnc = mobile_network_code;
        samples = samples_taken;
        openCellID = openCellID_Data;
    }

    public String getMCC() {
        if (mcc == null) return "000";
        if (mcc.length() >= 3) return mcc;
        return ("000" + mcc).substring(mcc.length());
    }

    public String getMNC() {
        if (mnc == null) return "00";
        if (mnc.length() >= 2) return mnc;
        return ("00" + mnc).substring(mnc.length());
    }

    public String getPC() {
        return getMCC() + getMNC();
    }

    public String getSamples() {
        if (samples == null) return "0";
        return samples;
    }
}

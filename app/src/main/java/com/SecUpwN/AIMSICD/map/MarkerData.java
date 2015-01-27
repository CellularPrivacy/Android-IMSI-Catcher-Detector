package com.SecUpwN.AIMSICD.map;

/**
 * Class to hold data for displaying in BTS pin popup dialog ??
 *
 * TODO: Consider adding more details, similar as for the DB Viewer UI:
 * see: https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/234
 */
public class MarkerData {

    public final String cellID;     // change to "CID"...
    //private final String psc;     // PSC (UMTS)
    public final String lat;
    public final String lng;        // change to "lon"...
    public final String lac;
    private final String mcc;       // remove and use PC: MCC+MNC
    private final String mnc;       // remove and use PC: MCC+MNC
    //private final String pc;      // PC = MCC + MNC
    //private final String first;   // time_first
    //private final String last;    // time_last
    private final String samples;
    public final boolean openCellID; // ??

    public MarkerData(
               String cell_id,
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

    // (Mobile Network Operator) Provider Code
    public String getPC() {
        return getMCC() + getMNC();
    }

    public String getSamples() {
        if (samples == null) return "0";
        return samples;
    }
}

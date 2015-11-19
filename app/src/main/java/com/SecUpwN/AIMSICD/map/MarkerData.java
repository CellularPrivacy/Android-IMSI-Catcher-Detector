/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.map;

/**
 * Description:     Class to hold data for displaying in BTS pin popup dialog
 *
 * Issues:
 *                  [ ] clarify which GPS coordinates are used. Exact of from device?
 *                  [ ] adding more details, similar as for the DB Viewer UI:
 *                      see: https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/234
 *
 *
 */
public class MarkerData {

    public final String cellID;     // change to "CID"...
    //private final String psc;     // PSC (UMTS)
    public final String lat;        // gpsd_lat or gps_lat
    public final String lng;        // gpsd_lon or gps_lon    TODO: change to "lon"...
    public final String lac;        // LAC
    private final String mcc;       // remove and use PC: MCC+MNC
    private final String mnc;       // remove and use PC: MCC+MNC
    // todo: Add PSC and RAT
    //private final String psc;     // PSC
    //private final String rat;     // RAT

    private final String samples;   // samples
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
        if (mcc == null)
            return "000";
        if (mcc.length() >= 3)
            return mcc;

        return ("000" + mcc).substring(mcc.length());
    }

    public String getMNC() {
        if (mnc == null)
            return "00";
        if (mnc.length() >= 2)
            return mnc;
        return ("00" + mnc).substring(mnc.length());
    }

    // (Mobile Network Operator) Provider Code in form: MCC-MNC
    public String getPC() {
        return getMCC() +"-"+ getMNC();
    }

    public String getSamples() {
        if (samples == null || (!openCellID && samples.isEmpty()))
            return "0";
        return samples;
    }
}

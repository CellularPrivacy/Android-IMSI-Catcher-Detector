/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.adapters;

import lombok.Getter;

@Getter
public class DbeImportItemData {

    private final String DB_SOURCE;
    private final String RAT;
    private final String MCC;
    private final String MNC;
    private final String LAC;
    private final String CID;
    private final String PSC;
    private final String GPS_LAT;
    private final String GPS_LON;
    private final String IS_GPS_EXACT;
    private final String AVG_RANGE;
    private final String AVG_SIGNAL;
    private final String SAMPLES;
    private final String TIME_FIRST;
    private final String TIME_LAST;
    private final String REJ_CAUSE;

    //private boolean mIsFakeData;
    private final String recordId;

    public DbeImportItemData(
            String db_source,
             String rat,
             String mcc,
             String mnc,
             String lac,
             String cid,
             String psc,
             String gps_lat,
             String gps_lon,
             String is_gps_exact,
             String avg_range,
             String avg_signal,
             String samples,
             String time_first,
             String time_last,
             String rej_cause,
             String _mRecordId) {

            DB_SOURCE = db_source;
            RAT = rat;
            MCC = mcc;
            MNC = mnc;
            LAC = lac;
            CID = cid;
            PSC = psc;
            GPS_LAT = gps_lat;
            GPS_LON = gps_lon;
            IS_GPS_EXACT = is_gps_exact;
            AVG_RANGE = avg_range;
            AVG_SIGNAL = avg_signal;
            SAMPLES = samples;
            TIME_FIRST = time_first;
            TIME_LAST = time_last;
            REJ_CAUSE = rej_cause;

            recordId = _mRecordId;
    }

}

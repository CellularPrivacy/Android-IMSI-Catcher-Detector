/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.adapters;

import lombok.Getter;
import lombok.Setter;

/**
 *  Description:    Class to show data of table "EventLog" in "Database Viewer"
 *
 *  Dependencies:   EventLogCardInflater.java
 *                  Data representation in:     eventlog_items.xml (from Array in DdViewerFragment)
 *                  Data query in:              AIMSICDDbAdapter.java
 *                  Data representation in:     APP see menu "Database Viewer" > "EventLog"
 *
 *  Usage:          DdViewerFragment, AIMSICDDbAdapter.java
 *
 *  Issues:
 *          [ ] See AIMSICDDbAdapter.java line518
 *              - How to find the right values (timestamp, lat, lan, accu) for saving in the db
 *  -----------------------------------------------------------------------------------------
 *  Notes:
 *
 *   We often talk about "Network Type", when we actually refer to:
 *   "RAN" = Radio Access Network (cellular communaitcation only)
 *   "RAT" = Radio Access Technology (any wireless communication technology, like WiMax etc.)
 *
 *   As for this application, we shall use the terms:
 *   "Type" for the text values like ( UMTS/WCDMA, HSDPA, CDMA, LTE etc)  and
 *   "RAT" for the numerical equivalent (As obtained by AOS API?)
 *
 * ------------------------------------------------------------------------------------------
 */
@Getter
public class EventLogItemData {
    // OLD (in old DB tables)
    private final String timestamp;
    private final String cellId;
    private final String lac;
    private final String psc;
    private final String lat;
    private final String lon;
    private final String gpsd_accu;
    private final String dF_id;
    private final String dF_desc;

    private final String recordId;
    @Setter
    private boolean fakeData;

    public EventLogItemData(
                String time,
                String LAC,
                String CID,
                String PSC,
                String gpsd_lat,
                String gpsd_lon,
                String gpsd_accu,
                String DF_id,
                String DF_desc,

                String recordId) {
        this(
                time,
                LAC,
                CID,
                PSC,
                gpsd_lat,
                gpsd_lon,
                gpsd_accu,
                DF_id,
                DF_desc,

                recordId,
                false
        );
    }

    public EventLogItemData(String pTime,
                            String pLAC,
                            String pCID,
                            String pPSC,
                            String pGpsd_lat,
                            String pGpsd_lon,
                            String pGpsd_accu,
                            String pDF_id,
                            String pDF_desc,

                            String pRecordId,
                            boolean pIsFakeData) {
        timestamp = pTime;
        lac = pLAC;
        cellId = pCID;
        psc = pPSC;
        lat = pGpsd_lat;
        lon = pGpsd_lon;
        gpsd_accu = pGpsd_accu;
        dF_id = pDF_id;
        dF_desc = pDF_desc;

        recordId = pRecordId;
        fakeData = pIsFakeData;
    }
}

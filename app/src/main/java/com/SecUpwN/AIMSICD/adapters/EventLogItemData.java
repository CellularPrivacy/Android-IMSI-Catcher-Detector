/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.adapters;

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
 *
 *  ChangeLog:
 *
 *
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
public class EventLogItemData {
    // OLD (in old DB tables)
    private final String mTimestamp;
    private final String mCellID;
    private final String mLac;
    private final String mPsc;
    private final String mLat;
    private final String mLng;
    private final String mgpsd_accu;
    private final String mDF_id;
    private final String mDF_desc;

    private final String mRecordId;
    private boolean mIsFakeData;


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
        mTimestamp = pTime;
        mLac = pLAC;
        mCellID = pCID;
        mPsc = pPSC;
        mLat = pGpsd_lat;
        mLng = pGpsd_lon;
        mgpsd_accu = pGpsd_accu;
        mDF_id = pDF_id;
        mDF_desc = pDF_desc;

        mRecordId = pRecordId;
        mIsFakeData = pIsFakeData;
    }

    public String getTimestamp() {
        return mTimestamp;
    }

    public String getCellID() {
        return mCellID;
    }

    public String getLac() {
        return mLac;
    }

    public String getPsc() {
        return mPsc;
    }

    public String getLat() {
        return mLat;
    }

    public String getLng() {
        return mLng;
    }

    public String getgpsd_accu() {
        return mgpsd_accu;
    }

    public String getDF_id() {
        return mDF_id;
    }

    public String getDF_desc() {
        return mDF_desc;
    }


    public String getRecordId() {
        return mRecordId;
    }

    public boolean isFakeData() {
        return mIsFakeData;
    }

    public void setIsFakeData(boolean pIsFakeData) {
        mIsFakeData = pIsFakeData;
    }
}
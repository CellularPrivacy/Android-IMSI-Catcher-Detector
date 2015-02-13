package com.SecUpwN.AIMSICD.adapters;

import com.SecUpwN.AIMSICD.utils.Cell;
import com.SecUpwN.AIMSICD.utils.Device;
import java.text.SimpleDateFormat;

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




    public EventLogItemData(String time,
                            String LAC,
                            String CID,
                            String PSC,
                            String gpsd_lat,
                            String gpsd_lon,
                            String gpsd_accu,
                            String DF_id,
                            String DF_desc,
                            String recordId) {
        mTimestamp = time;
        mLac = LAC;
        mCellID = CID;
        mPsc = PSC;
        mLat = gpsd_lat;
        mLng = gpsd_lon;
        mgpsd_accu = gpsd_accu;
        mDF_id = DF_id;
        mDF_desc = DF_desc;
        mRecordId = recordId;
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

}
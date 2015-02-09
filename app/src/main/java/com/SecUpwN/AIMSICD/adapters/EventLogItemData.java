package com.SecUpwN.AIMSICD.adapters;

import com.SecUpwN.AIMSICD.utils.Cell;
import com.SecUpwN.AIMSICD.utils.Device;
import java.text.SimpleDateFormat;

/**
 *  Description:    Class to show data of table "EventLog" in "db-View" > "EventLog"
 *
 *  Dependencies:   EventLogCardInflater.java
 *                  Data representation on eventlog_items.xml (from Array in DdViewerFragment)
 *                  Data query on AIMSICDDbAdapter.java
 *                  Data representation on APP see menu "db-View" > "EventLog"
 *
 *  Usage: DdViewerFragment, AIMSICDDbAdapter.java
 *
 *  Issues: See AIMSICDDbAdapter.java line518 - How to find the right values (timestamp, lat, lan, accur) for saving in the db
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
    private final String mgpsd_accur;
    private final String mDF_id;
    private final String mDF_description;
    private final String mRecordId;




    public EventLogItemData(String time, String LAC, String CID, String PSC, String gpsd_lat,
                            String gpsd_lon, String gpsd_accur, String DF_id, String DF_description, String recordId) {
        mTimestamp = time;
        mLac = LAC;
        mCellID = CID;
        mPsc = PSC;
        mLat = gpsd_lat;
        mLng = gpsd_lon;
        mgpsd_accur = gpsd_accur;
        mDF_id = DF_id;
        mDF_description = DF_description;
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

    public String getgpsd_accur() {
        return mgpsd_accur;
    }

    public String getDF_id() {
        return mDF_id;
    }

    public String getmDF_description() {
        return mDF_description;
    }

    public String getRecordId() {
        return mRecordId;
    }



}
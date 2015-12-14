/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.adapters;

import com.SecUpwN.AIMSICD.utils.Cell;
import com.SecUpwN.AIMSICD.utils.Device;

/**
 *  Description:    TODO: A few comments please!
 *                  TODO: Where is this used exactly?
 *
 *  Dependencies:
 *
 *
 *  Usage:
 *
 *          Used to define methods in:
 *                  OpenCellIdCardInflater.java
 *                  EventLogCardInflater.java
 *
 *
 *  Issues:
 *
 *  ChangeLog:
 *
 *          2015-01-26  E:V:A   Added HEX string to CID.
 *
 *
 *  -----------------------------------------------------------------------------------------
 *  Notes:
 *
 *   We often talk about "Network Type", when we actually refer to:
 *   "RAN" = Radio Access Network (cellular communication only)
 *   "RAT" = Radio Access Technology (any wireless communication technology, like WiMax etc.)
 *
 *   As for this application, we shall use the terms:
 *   "Type" for the text values like ( UMTS/WCDMA, HSDPA, CDMA, LTE etc)  and
 *   "RAT" for the numerical equivalent (As obtained by AOS API?)
 *
 * ------------------------------------------------------------------------------------------
 */
public class CardItemData {
    // OLD (in old DB tables)
    private final String mCellID;
    private final String mLac;
    private final String mMcc;
    private final String mMnc;
    private final String mNet;
    private final String mSignal;
    private final String mAvgSigStr;
    private final String mSamples;
    private final String mLat;
    private final String mLng;
    private final String mCountry;
    private final String mPsc;
    private final String mTimestamp;
    private final String mRecordId;

    // NEW (in new DB tables)
/*
    private final String mtime;
    private final String mLAC;
    private final String mCID;
    private final String mPSC;
    private final String mgpsd_lat;
    private final String mgpsd_lon;
    private final String mgpsd_accu;
    private final String mDF_id;
    private final String mDF_description;
*/


    // OLD items in old DB table structure

    public CardItemData(Cell cell, String recordId) {

        if (cell.getCID() != Integer.MAX_VALUE && cell.getCID() != -1) {
            mCellID = "CID: " + cell.getCID() + "  (0x" + Integer.toHexString(cell.getCID()) +")";
        } else {
            mCellID = "N/A";
        }

        if (cell.getLAC() != Integer.MAX_VALUE && cell.getLAC() != -1) {
            mLac = "LAC: " + cell.getLAC();
        } else {
            mLac = "N/A";
        }

        if (cell.getMCC() != Integer.MAX_VALUE && cell.getMCC() != 0) {
            mMcc = "MCC: " + cell.getMCC();
        } else {
            mMcc = "N/A";
        }

        if (cell.getMNC() != Integer.MAX_VALUE && cell.getMNC() != 0) {
            mMnc = "MNC: " + cell.getMNC();
        } else {
            mMnc = "N/A";
        }

        if (cell.getNetType() != Integer.MAX_VALUE && cell.getNetType() != -1) {
            mNet = "Type: " + cell.getNetType() + " - " + Device.getNetworkTypeName(cell.getNetType());
        } else {
            mNet = "N/A";
        }

        if (cell.getPSC() != Integer.MAX_VALUE && cell.getPSC() != -1) {
            mPsc = "PSC: " + cell.getPSC();
        } else {
            mPsc = "N/A";
        }

        if (cell.getRssi() != Integer.MAX_VALUE && cell.getRssi() != -1) {
            mSignal = "RSSI: " + cell.getRssi();
        } else if (cell.getDBM() != Integer.MAX_VALUE && cell.getDBM() != -1) {
            mSignal = "dBm: " + cell.getDBM();
        } else {
            mSignal = "N/A";
        }
        // NEW (in new DB tables)


        // end New

        mLat = "N/A";
        mLng = "N/A";
        mAvgSigStr = "N/A";
        mSamples = "N/A";
        mCountry = "N/A";
        mTimestamp = "N/A";
        mRecordId = recordId;

        // NEW (in new DB tables)

        // end New

    }

    public CardItemData(String cellID, String lac, String mcc, String mnc, String lat, String lng,
            String avgSigStr, String samples, String recordId) {
        mCellID = cellID;
        mLac = lac;
        mMcc = mcc;
        mMnc = mnc;
        mNet = "Network Type: N/A";
        mLat = lat;
        mLng = lng;
        mSignal = "Signal: N/A";
        mAvgSigStr = avgSigStr;
        mSamples = samples;
        mPsc = "PSC: N/A";
        mCountry = "Country: N/A";
        mTimestamp = "Timestamp: N/A";
        mRecordId = recordId;
    }

    public CardItemData(String cellID, String psc, String mcc, String mnc, String signal,
            String recordId) {
        mCellID = cellID;
        mLac = "LAC: N/A";
        mMcc = mcc;
        mMnc = mnc;
        mLat = "Latitude: N/A";
        mLng = "Longitude: N/A";
        mNet = "Network Type: N/A";
        mAvgSigStr = "Avg Signal: N/A";
        mSamples = "Samples: N/A";
        mSignal = signal;
        mPsc = psc;
        mCountry = "Country: N/A";
        mTimestamp = "Timestamp: N/A";
        mRecordId = recordId;
    }

    public CardItemData(String cellID, String lac, String mcc, String mnc, String signal,
            String psc, String timestamp, String recordId) {
        mCellID = cellID;
        mLac = lac;
        mMcc = mcc;
        mMnc = mnc;
        mLat = "Latitude: N/A";
        mLng = "Longitude: N/A";
        mNet = "Network Type: N/A";
        mSignal = signal;
        mPsc = psc;
        mAvgSigStr = "Avg Signal: N/A";
        mSamples = "Samples: N/A";
        mTimestamp = timestamp;
        mCountry = "Country: N/A";
        mRecordId = recordId;
    }

    public CardItemData(int type, String cellID, String lac, String mcc, String mnc, String signal,
            String timestamp, String recordId) {
        mCellID = cellID;
        mLac = lac;
        mMcc = mcc;
        mMnc = mnc;
        mLat = "Latitude: N/A";
        mLng = "Longitude: N/A";
        mNet = "Network Type: N/A";
        mSignal = signal;
        mAvgSigStr = "Avg Signal: N/A";
        mSamples = "Samples: N/A";
        mTimestamp = timestamp;
        mPsc = "PSC: N/A";
        mCountry = "Country: N/A";
        mRecordId = recordId;
    }

    public CardItemData(String cellID, String lac, String net, String lat, String lng,
            String signal, String recordId) {
        mCellID = cellID;
        mLac = lac;
        mNet = net;
        mMcc = "MCC: N/A";
        mMnc = "MNC: N/A";
        mLat = lat;
        mLng = lng;
        mSignal = signal;
        mAvgSigStr = "Avg Signal: N/A";
        mSamples = "Samples: N/A";
        mPsc = "PSC: N/A";
        mCountry = "Country: N/A";
        mTimestamp = "Timestamp: N/A";
        mRecordId = recordId;
    }

    public CardItemData(String country, String mcc, String lat, String lng, String recordId) {
        mCellID = "CellID: N/A";
        mLac = "LAC: N/A";
        mCountry = country;
        mMcc = mcc;
        mMnc = "MNC: N/A";
        mNet = "Network Type: N/A";
        mSignal = "Signal: N/A";
        mLat = lat;
        mLng = lng;
        mAvgSigStr = "Avg Signal: N/A";
        mSamples = "Samples: N/A";
        mPsc = "PSC: N/A";
        mTimestamp = "Timestamp: N/A";
        mRecordId = recordId;
    }

    public String getCellID() {
        return mCellID;
    }

    public String getLac() {
        return mLac;
    }

    public String getMcc() {
        return mMcc;
    }

    public String getMnc() {
        return mMnc;
    }

    public String getNet() {
        return mNet;
    }

    public String getSignal() {
        return mSignal;
    }

    public String getAvgSigStr() {
        return mAvgSigStr;
    }

    public String getSamples() {
        return mSamples;
    }

    public String getLat() {
        return mLat;
    }

    public String getLng() {
        return mLng;
    }

    public String getCountry() {
        return mCountry;
    }

    public String getRecordId() {
        return mRecordId;
    }

    public String getPsc() {
        return mPsc;
    }

    public String getTimestamp() {
        return mTimestamp;
    }

    // NEW (in new DB tables)
    // EventLog

    //public String getAccu() {
    //    return mAccu;
    //}

}
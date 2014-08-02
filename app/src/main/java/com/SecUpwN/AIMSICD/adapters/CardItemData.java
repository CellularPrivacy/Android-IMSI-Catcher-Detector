package com.SecUpwN.AIMSICD.adapters;

public class CardItemData {

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
        mPsc = "Primary Scrambling Code: N/A";
        mCountry = "Country: N/A";
        mTimestamp = "Timestamp: N/A";
        mRecordId = recordId;
    }

    public CardItemData(String cellID, String lac, String mcc, String mnc, String signal,
            String recordId) {
        mCellID = cellID;
        mLac = lac;
        mMcc = mcc;
        mMnc = mnc;
        mLat = "Latitude: N/A";
        mLng = "Longitude: N/A";
        mNet = "Network Type: N/A";
        mAvgSigStr = "Avg Signal: N/A";
        mSamples = "Samples: N/A";
        mSignal = signal;
        mPsc = "Primary Scrambling Code: N/A";
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
        mPsc = "Primary Scrambling Code: N/A";
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
        mPsc = "Primary Scrambling Code: N/A";
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
        mPsc = "Primary Scrambling Code: N/A";
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
}
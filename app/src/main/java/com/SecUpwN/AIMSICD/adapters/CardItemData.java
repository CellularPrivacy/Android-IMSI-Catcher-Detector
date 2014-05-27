package com.SecUpwN.AIMSICD.adapters;

public class CardItemData
{
    private String mCellID;
    private String mLac;
    private String mMcc;
    private String mMnc;
    private String mNet;
    private String mSignal;
    private String mAvgSigStr;
    private String mSamples;
    private String mLat;
    private String mLng;
    private String mCountry;
    private String mRecordId;

    public CardItemData(String cellID, String lac, String mcc, String mnc, String lat, String lng,
            String avgSigStr, String samples, String recordId)
    {
        mCellID = cellID;
        mLac = lac;
        mMcc = mcc;
        mMnc = mnc;
        mLat = lat;
        mLng = lng;
        mAvgSigStr = avgSigStr;
        mSamples = samples;
        mRecordId = recordId;
    }

    public CardItemData(String cellID, String lac, String net, String lat, String lng, String signal,
            String recordId)
    {
        mCellID = cellID;
        mLac = lac;
        mNet = net;
        mLat = lat;
        mLng = lng;
        mSignal = signal;
        mRecordId = recordId;
    }

    public CardItemData(String country, String mcc, String lat, String lng, String recordId)
    {
        mCountry = country;
        mMcc = mcc;
        mLat = lat;
        mLng = lng;
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

    public String getRecordId() { return mRecordId; }
}
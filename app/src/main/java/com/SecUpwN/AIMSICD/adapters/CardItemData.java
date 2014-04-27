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


    public CardItemData(String cellID, String lac, String mcc, String mnc, String lat, String lng,
            String avgSigStr, String samples)
    {
        mCellID = cellID;
        mLac = lac;
        mMcc = mcc;
        mMnc = mnc;
        mLat = lat;
        mLng = lng;
        mAvgSigStr = avgSigStr;
        mSamples = samples;
    }

    public CardItemData(String cellID, String lac, String net, String lat, String lng, String signal)
    {
        mCellID = cellID;
        mLac = lac;
        mNet = net;
        mLat = lat;
        mLng = lng;
        mSignal = signal;
    }

    public CardItemData(String country, String mcc, String lat, String lng)
    {
        mCountry = country;
        mMcc = mcc;
        mLat = lat;
        mLng = lng;
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
}
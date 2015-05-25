package com.SecUpwN.AIMSICD.smsdetection;

/**
Created By Paul Kinsella paulkinsella29@yahoo.ie
 */
public class DetectionStringsData {

    private final String mDetection_string;
    private final String mDetection_type;
    private boolean mIsFakeData;




    public DetectionStringsData(String detection_string,
                                String detection_type) {
        this(detection_string,
                detection_type,
                false);
    }

    public DetectionStringsData(String pString,
                                String pType,
                                boolean pIsFakeData) {
        mDetection_string = pString;
        mDetection_type = pType;

        mIsFakeData = pIsFakeData;
    }

    public String getDetectionString() {
        return mDetection_string;
    }

    public String getDetectionType() {
        return mDetection_type;
    }

    public boolean isFakeData() {
        return mIsFakeData;
    }

    public void setIsFakeData(boolean pIsFakeData) {
        mIsFakeData = pIsFakeData;
    }
}
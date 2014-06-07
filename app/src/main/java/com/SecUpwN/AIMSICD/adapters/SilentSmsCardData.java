package com.SecUpwN.AIMSICD.adapters;

public class SilentSmsCardData {
    private String mAddress;
    private String mDisplayAddress;
    private String mMessageClass;
    private String mServiceCentre;
    private String mMessage;
    private int mTimestamp;

    public SilentSmsCardData(String address, String displayAddress, String messageClass,
            String serviceCentre, String messageBody, int timestamp)
    {
        mAddress = address;
        mDisplayAddress = displayAddress;
        mMessageClass = messageClass;
        mServiceCentre = serviceCentre;
        mMessage = messageBody;
        mTimestamp = timestamp;
    }

    public String getAddress() {
        return mAddress;
    }

    public String getDisplayAddress() {
        return mDisplayAddress;
    }

    public String getMessageClass() {
        return mMessageClass;
    }

    public String getServiceCentre() {
        return mServiceCentre;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getTimestamp() {
        return "Timestamp: " + mTimestamp;
    }

}

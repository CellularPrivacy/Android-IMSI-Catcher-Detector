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
        String address = "Address: ";
        if (mAddress.isEmpty())
            address += "Unavailable";
        else
            address += mAddress;

        return address;
    }

    public String getDisplayAddress() {
        String display = "Display Address: ";
        if (mDisplayAddress.isEmpty())
            display += "Unavailable";
        else
            display += mDisplayAddress;

        return display;
    }

    public String getMessageClass() {
        String messageClass = "Message Class: ";
        if (messageClass.isEmpty())
            messageClass += "Unavailable";
        else
            messageClass += mMessageClass;

        return messageClass;
    }

    public String getServiceCentre() {
        String serviceCentre = "Service Centre: ";
        if (mServiceCentre.isEmpty())
            serviceCentre += "Unavailable";
        else
            serviceCentre += mServiceCentre;

        return serviceCentre;
    }

    public String getMessage() {
        String message = "Message: ";
        if (message.isEmpty())
            message += "Unavailable";
        else
            message += mMessage;

        return message;
    }

    public String getTimestamp() {
        return "Timestamp: " + mTimestamp;
    }

}

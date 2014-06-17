package com.SecUpwN.AIMSICD.adapters;

import java.text.SimpleDateFormat;

public class FlashSmsCardData {
    private final String mAddress;
    private final String mDisplayAddress;
    private final String mMessageClass;
    private final String mServiceCentre;
    private final String mMessage;
    private final long mTimestamp;

    public FlashSmsCardData(String address, String displayAddress, String messageClass,
            String serviceCentre, String messageBody, long timestamp)
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
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        String dateString = formatter.format(mTimestamp);
        return "Timestamp: " + dateString;
    }

}

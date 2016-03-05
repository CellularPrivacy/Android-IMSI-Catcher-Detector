/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.adapters;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SilentSmsCardData {

    private final String mAddress;
    private final String mDisplayAddress;
    private final String mMessageClass;
    private final String mServiceCentre;
    private final String mMessage;
    private final long mTimestamp;
    private boolean mIsFakeData;

    public SilentSmsCardData(String address, String displayAddress, String messageClass,
            String serviceCentre, String messageBody, long timestamp) {
        mAddress = address;
        mDisplayAddress = displayAddress;
        mMessageClass = messageClass;
        mServiceCentre = serviceCentre;
        mMessage = messageBody;
        mTimestamp = timestamp;
    }

    public String getAddress() {
        String address = "Address: ";
        if (mAddress.isEmpty()) {
            return address + "Unavailable";
        } else {
            return address + mAddress;
        }
    }

    public String getDisplayAddress() {
        String display = "Display Address: ";
        if (mDisplayAddress.isEmpty()) {
            return display + "Unavailable";
        } else {
            return display + mDisplayAddress;
        }
    }

    public String getMessageClass() {
        String messageClass = "Message Class: ";
        if (messageClass.isEmpty()) {
            return messageClass + "Unavailable";
        } else {
            return messageClass + mMessageClass;
        }
    }

    public String getServiceCentre() {
        String serviceCentre = "Service Centre: ";
        if (mServiceCentre.isEmpty()) {
            return serviceCentre + "Unavailable";
        } else {
            return serviceCentre + mServiceCentre;
        }
    }

    public String getMessage() {
        String message = "Message: ";
        if (message.isEmpty()) {
            return message + "Unavailable";
        } else {
            return message + mMessage;
        }
    }

    public String getTimestamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault());
        String dateString = formatter.format(mTimestamp);
        return "Timestamp: " + dateString;
    }

    public boolean isFakeData() {
        return mIsFakeData;
    }

    public void setIsFakeData(boolean pIsFakeData) {
        mIsFakeData = pIsFakeData;
    }
}

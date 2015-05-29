package com.SecUpwN.AIMSICD.adapters;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 *
 * Data card class used in DB viewer (for Measured cell strength measurements)
 *
 * @author Tor Henning Ueland
 */
public class MeasuredCellStrengthCardData {

    private final long timestamp;
    private final int signal;
    private final int cellID;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public MeasuredCellStrengthCardData(int cellID, int signal, long timestamp) {
        this.cellID = cellID;
        this.signal = signal;
        this.timestamp = timestamp;
    }

    
    // Let's try to get it in HEX as well
    public String getCellID() {
        return "CID: "+cellID + "  (0x" + Integer.toHexString(cellID) +")";
    }

    //original
    // public String getCellID() {
    //    return "CID: "+cellID;
    //}

    // This is probably not dBm but ASU ?
    // Please see: http://wiki.opencellid.org/wiki/API#Filtering_of_data
    public String getSignal() {
        return "RSS: "+signal+" dBm";
    }

    public String getTimestamp() {
        return "Timestamp: "+formatter.format(timestamp);
    }
}

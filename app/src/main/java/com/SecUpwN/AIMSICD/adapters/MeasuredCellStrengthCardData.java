package com.SecUpwN.AIMSICD.adapters;

import java.text.SimpleDateFormat;
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
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public MeasuredCellStrengthCardData(int cellID, int signal, long timestamp) {
        this.cellID = cellID;
        this.signal = signal;
        this.timestamp = timestamp;
    }

    public String getCellID() {
        return "CID: "+cellID;
    }

    // This is probably not dBm but ASU ?
    public String getSignal() {
        return"RSS: "+signal+" dBm";
    }

    public String getTimestamp() {
        return "Timestamp: "+formatter.format(timestamp);
    }
}

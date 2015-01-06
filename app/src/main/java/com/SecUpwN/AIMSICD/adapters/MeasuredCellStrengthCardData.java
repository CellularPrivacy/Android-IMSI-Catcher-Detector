package com.SecUpwN.AIMSICD.adapters;

import java.text.SimpleDateFormat;

public class MeasuredCellStrengthCardData {

    private final long timestamp;
    private final int signal;
    private final int cellID;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

    public MeasuredCellStrengthCardData(int cellID, int signal, long timestamp) {
        this.cellID = cellID;
        this.signal = signal;
        this.timestamp = timestamp;
    }

    public String getCellID() {
        return "Cell ID: "+cellID;
    }

    public String getSignal() {
        return"Signal Strength: "+signal+"DB";
    }

    public String getTimestamp() {
        return "Recorded: "+formatter.format(timestamp);
    }
}

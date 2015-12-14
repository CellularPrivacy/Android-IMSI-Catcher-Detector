/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
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

    private final String timestamp;
    private final int signal;
    private final int cellID;

    public MeasuredCellStrengthCardData(int cellID, int signal, String timestamp) {
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
        return "Time: " +timestamp;
    }
}

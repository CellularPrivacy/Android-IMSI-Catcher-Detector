/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.constants;

/**
 * Description:     This class add Constants for examples as used to pre-populate
 *                  various tables used in the Database Viewer (DBV).
 *
 * Dependencies:    DbViewerFragment.java
 */
public class Examples {

    /**
     * Description:     Constants of examples for EventLogItemData
     * Dependencies:    EventLogItemData.java
     */
    public static class EVENT_LOG_DATA {
        public static final String LAC = "12345";
        public static final String CID = "543210";
        public static final String PSC = "111";
        public static final String GPSD_LAT = "54.6";
        public static final String GPSD_LON = "25.2";
        public static final String GPSD_ACCU = "100";
        public static final String DF_ID = "2";
    }

    /**
     * Description:     Constants of examples for SilentSmsCardData
     * Dependencies:    SilentSmsCardData.java
     */
    public static class SILENT_SMS_CARD_DATA {
        public static final String ADDRESS = "ADREZZ";
        public static final String DISPLAY = "DizzPlay";
    }


}

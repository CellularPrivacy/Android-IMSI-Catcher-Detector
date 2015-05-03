/* Android IMSI Catcher Detector
 *      Copyright (C) 2015
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You may obtain a copy of the License at
 *      https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/LICENSE
 */
package com.SecUpwN.AIMSICD.constants;

/**
 * Constants for examples
 */
public class Examples {

    /**
     * Constants of examples for EventLogItemData
     * <p>Relates to {@link com.SecUpwN.AIMSICD.adapters.EventLogItemData}<br />
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
     * Constants of examples for SilentSmsCardData
     * <p>Relates to {@link com.SecUpwN.AIMSICD.adapters.SilentSmsCardData}<br />
     */
    public static class SILENT_SMS_CARD_DATA {
        public static final String ADDRESS = "ADREZZ";
        public static final String DISPLAY = "DizzPlay";
    }


}

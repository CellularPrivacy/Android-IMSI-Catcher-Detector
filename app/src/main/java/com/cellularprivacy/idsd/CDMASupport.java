package com.cellularprivacy.isds;

/**
 * CDMA Support module for IMSI Catcher Detector.
 * 
 * CDMA uses a different way of tracking and connecting to base-stations (BTS)
 * that includes GPS info on the BTS and uses different parameters than GSM.
 * 
 * Reference: http://wiki.opencellid.org/wiki/API
 * 
 * CID to SID mapping for CDMA networks.
 */
public class CDMASupport {
    
    /**
     * CDMA base station parameters.
     */
    public static class CDMAParameters {
        public final int basestationId;
        public final int systemId;
        public final int networkId;
        public final double latitude;
        public final double longitude;
        public final int elevation;
        public final String carrierName;
        
        public CDMAParameters(int basestationId, int systemId, int networkId,
                              double latitude, double longitude, int elevation,
                              String carrierName) {
            this.basestationId = basestationId;
            this.systemId = systemId;
            this.networkId = networkId;
            this.latitude = latitude;
            this.longitude = longitude;
            this.elevation = elevation;
            this.carrierName = carrierName;
        }
        
        @Override
        public String toString() {
            return String.format("CDMA[BTS=%d SYS=%d NET=%d LAT=%.4f LON=%.4f ELEV=%dm Carrier=%s]",
                    basestationId, systemId, networkId, latitude, longitude, elevation, carrierName);
        }
    }
    
    /**
     * Parse CDMA BTS info from raw network data.
     * CDMA uses SID (System ID), NID (Network ID), and BTS ID.
     */
    public static CDMAParameters parseCDMAParameters(int sid, int nid, int bsId,
                                                      double lat, double lon, int elev,
                                                      String carrier) {
        return new CDMAParameters(bsId, sid, nid, lat, lon, elev, carrier);
    }
    
    /**
     * Known CDMA carrier SIDs.
     */
    private static final int[][] VERIZON_SIDS = {
        {0x0017}, {0x0023}
    };
    
    private static final int[][] SPREAD_SIDS = {
        {0x0032}, {0x0033}, {0x0034}, {0x0035}, {0x0036},
        {0x0037}, {0x0038}, {0x0039}, {0x0040}, {0x0041},
        {0x0042}, {0x0043}, {0x0044}, {0x0045}, {0x0046},
        {0x0047}, {0x0048}, {0x0049}, {0x0050}
    };
    
    /**
     * Check if a given SID belongs to a known CDMA carrier.
     */
    public static String getCarrierFromSID(int sid) {
        for (int[] sids : VERIZON_SIDS) {
            if (sid == sids[0]) return "Verizon Wireless";
        }
        for (int[] sids : SPREAD_SIDS) {
            if (sid == sids[0]) return "Sprint";
        }
        return "Unknown (SID=" + sid + ")";
    }
    
    /**
     * Convert CDMA parameters to OpenCellID-compatible format.
     */
    public static String toOpenCellIDFormat(CDMAParameters params) {
        long cellId = ((long) params.systemId << 32) |
                      ((long) params.networkId << 16) |
                      params.basestationId;
        return String.format("%.6f,%.6f,%d,%s",
                params.latitude, params.longitude, (int)(cellId & 0xFFFFFFFFL),
                params.carrierName);
    }
}

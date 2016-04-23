/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.utils;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.telephony.TelephonyManager;

import com.secupwn.aimsicd.R;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Cell implements Parcelable {

    public static final String INVALID_PSC = "invalid";

    // Cell Specific Variables
    /**
     * Current Cell ID
     * Cell Identification code
     */
    @Setter
    private int cellId;

    /**
     * Location Area Code
     */
    @Setter
    private int locationAreaCode;

    /**
     * Mobile Country Code
     */
    @Setter
    private int mobileCountryCode;

    /**
     * Mobile Network Code
     */
    @Setter
    private int mobileNetworkCode;

    /**
     * [dBm] RX signal "power"
     * Signal Strength Measurement (dBm)
     */
    @Setter
    private int dbm;

    /**
     * Primary Scrambling Code
     */
    private int primaryScramblingCode;

    /**
     * Relative Signal Strength Indicator [dBm, asu etc.]
     * Received Signal Strength Indicator (RSSI)
     */
    private int rssi;

    /**
     * Timing Advance [LTE,GSM]
     * LTE Timing Advance or Integer.MAX_VALUE if unavailable
     */
    @Setter
    private int timingAdvance;

    /**
     * Cell-ID for [CDMA]
     *
     * CDMA System ID
     *
     * @return System ID or Integer.MAX_VALUE if not supported
     */
    @Setter
    private int sid;

    /**
     * Timestamp of current cell information
     */
    @Setter
    private long timestamp;

    // Tracked Cell Specific Variables
    /**
     * Current Network Type
     */
    @Setter
    private int netType;
    /**
     * Current ground speed in metres/second
     */
    @Setter
    private double speed;
    /**
     * Location accuracy in metres or 0.0 if unavailable
     */
    @Setter
    private double accuracy;
    @Setter
    private double bearing;

    /**
     * Longitude Geolocation
     */
    @Setter
    private double lon;

    /**
     * Latitude
     */
    @Setter
    private double lat;

    {
        cellId = Integer.MAX_VALUE;
        locationAreaCode = Integer.MAX_VALUE;
        mobileCountryCode = Integer.MAX_VALUE;
        mobileNetworkCode = Integer.MAX_VALUE;
        dbm = Integer.MAX_VALUE;
        primaryScramblingCode = Integer.MAX_VALUE;
        rssi = Integer.MAX_VALUE;
        timingAdvance = Integer.MAX_VALUE;
        sid = Integer.MAX_VALUE;
        netType = Integer.MAX_VALUE;
        lon = 0.0;
        lat = 0.0;
        speed = 0.0;
        accuracy = 0.0;
        bearing = 0.0;
    }

    public Cell() {
    }

    public Cell(int CID, int locationAreaCode, int mobileCountryCode, int mobileNetworkCode, int dbm, long timestamp) {
        super();
        this.cellId = CID;
        this.locationAreaCode = locationAreaCode;
        this.mobileCountryCode = mobileCountryCode;
        this.mobileNetworkCode = mobileNetworkCode;
        this.dbm = dbm;
        this.rssi = Integer.MAX_VALUE;
        this.primaryScramblingCode = Integer.MAX_VALUE;
        this.timestamp = timestamp;
        this.timingAdvance = Integer.MAX_VALUE;
        this.sid = Integer.MAX_VALUE;
        this.netType = Integer.MAX_VALUE;
        this.lon = 0.0;
        this.lat = 0.0;
        this.speed = 0.0;
        this.accuracy = 0.0;
        this.bearing = 0.0;
    }

    public Cell(int CID, int locationAreaCode, int signal, int primaryScramblingCode, int netType, boolean dbm) {
        this.cellId = CID;
        this.locationAreaCode = locationAreaCode;
        this.mobileCountryCode = Integer.MAX_VALUE;
        this.mobileNetworkCode = Integer.MAX_VALUE;

        if (dbm) {
            this.dbm = signal;
        } else {
            this.rssi = signal;
        }
        this.primaryScramblingCode = primaryScramblingCode;

        this.netType = netType;
        this.timingAdvance = Integer.MAX_VALUE;
        this.sid = Integer.MAX_VALUE;
        this.lon = 0.0;
        this.lat = 0.0;
        this.speed = 0.0;
        this.accuracy = 0.0;
        this.bearing = 0.0;
        this.timestamp = SystemClock.currentThreadTimeMillis();
    }

    public Cell(int cellId, int locationAreaCode, int mobileCountryCode, int mobileNetworkCode, int dbm, double accuracy, double speed,
                double bearing, int netType, long timestamp) {
        this.cellId = cellId;
        this.locationAreaCode = locationAreaCode;
        this.mobileCountryCode = mobileCountryCode;
        this.mobileNetworkCode = mobileNetworkCode;
        this.dbm = dbm;
        this.rssi = Integer.MAX_VALUE;
        this.timingAdvance = Integer.MAX_VALUE;
        this.sid = Integer.MAX_VALUE;
        this.accuracy = accuracy;
        this.speed = speed;
        this.bearing = bearing;
        this.netType = netType;
        this.timestamp = timestamp;
    }

    /**
     * Set Primary Scrambling Code (PSC) of current Cell
     *
     * @param primaryScramblingCode Primary Scrambling Code
     */
    public void setPrimaryScramblingCode(int primaryScramblingCode) {
        if (primaryScramblingCode == -1) {
            this.primaryScramblingCode = Integer.MAX_VALUE;
        } else {
            this.primaryScramblingCode = primaryScramblingCode;
        }
    }

    /**
     * Radio Access Technology (RAT)
     *
     * Some places in the app refers to this as the Network Type.
     *
     * For our purposes, network types displayed to the user is referred to as RAT.
     *
     * @return Current cell's Radio Access Technology (e.g. UMTS, GSM) or null if not known
     */
    public String getRat() {
        return getRatFromInt(this.netType);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + cellId;
        result = prime * result + locationAreaCode;
        result = prime * result + mobileCountryCode;
        result = prime * result + mobileNetworkCode;
        if (primaryScramblingCode != -1) {
            result = prime * result + primaryScramblingCode;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (((Object) this).getClass() != obj.getClass()) {
            return false;
        }
        Cell other = (Cell) obj;
        if (this.primaryScramblingCode != Integer.MAX_VALUE) {
            return this.cellId == other.getCellId() && this.locationAreaCode == other.getLocationAreaCode() && this.mobileCountryCode == other
                    .getMobileCountryCode() && this.mobileNetworkCode == other.getMobileNetworkCode() && this.primaryScramblingCode == other.getPrimaryScramblingCode();
        } else {
            return this.cellId == other.getCellId() && this.locationAreaCode == other.getLocationAreaCode() && this.mobileCountryCode == other
                    .getMobileCountryCode() && this.mobileNetworkCode == other.getMobileNetworkCode();
        }
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("cid - ").append(cellId).append("\n");
        result.append("LAC - ").append(locationAreaCode).append("\n");
        result.append("MCC - ").append(mobileCountryCode).append("\n");
        result.append("MNC - ").append(mobileNetworkCode).append("\n");
        result.append("DBm - ").append(dbm).append("\n");
        result.append("PSC - ").append(validatePscValue(primaryScramblingCode)).append("\n");
        result.append("Type - ").append(netType).append("\n");
        result.append("Lon - ").append(lon).append("\n");
        result.append("Lat - ").append(lat).append("\n");

        return result.toString();
    }

    public boolean isValid() {
        return this.getCellId() != Integer.MAX_VALUE && this.getLocationAreaCode() != Integer.MAX_VALUE;
    }

    /**
     * Get a human-readable string of RAT/Network Type
     *
     * Frustratingly it looks like the app uses RAT & Network Type interchangably with both either
     * being an integer representation (TelephonyManager's constants) or a human-readable string.
     *
     * @param netType The integer representation of the network type, via TelephonyManager
     * @return Human-readable representation of network type (e.g. "EDGE", "LTE")
     */
    public static String getRatFromInt(int netType) {
        switch (netType) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "eHRPD";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "EVDO rev. 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "EVDO rev. A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "EVDO rev. B";
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPA+";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "iDen";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return "Unknown";
            default:
                return String.valueOf(netType);
        }
    }

    public static String validatePscValue(Context c, String psc) {
        return validatePscValue(c, Integer.parseInt(psc));
    }

    /**
     * Validate PSC is in bounds, return i18n'd "Unknown" if invalid
     *
     * @see #validatePscValue(int)
     *
     * @param c Used for getString translations
     * @param psc
     * @return PSC or "Unknown "if invalid
     */
    public static String validatePscValue(Context c, int psc) {
        String s = validatePscValue(psc);
        if (s.equals(INVALID_PSC)) {
            return c.getString(R.string.unknown);
        }
        return s;
    }

    public static String validatePscValue(String psc) {
        return validatePscValue(Integer.parseInt(psc));
    }

    /**
     * Validate PSC is in bounds
     *
     * Database import stores cell's PSC as "666" if its absent in OCID. This method will return
     * "invalid" instead.
     *
     * Use this method to translate/i18n a cell's missing PSC value.
     *
     * @param psc
     * @return PSC or "invalid" untranslated string if invalid
     */
    public static String validatePscValue(int psc) {
        if (psc < 0 || psc > 511) {
            return INVALID_PSC;
        }
        return String.valueOf(psc);
    }

    // Parcelling
    public Cell(Parcel in) {
        String[] data = new String[15];

        in.readStringArray(data);
        cellId = Integer.valueOf(data[0]);
        locationAreaCode = Integer.valueOf(data[1]);
        mobileCountryCode = Integer.valueOf(data[2]);
        mobileNetworkCode = Integer.valueOf(data[3]);
        dbm = Integer.valueOf(data[4]);
        primaryScramblingCode = Integer.valueOf(data[5]);
        rssi = Integer.valueOf(data[6]);
        timingAdvance = Integer.valueOf(data[7]);
        sid = Integer.valueOf(data[8]);
        netType = Integer.valueOf(data[9]);
        lon = Double.valueOf(data[10]);
        lat = Double.valueOf(data[11]);
        speed = Double.valueOf(data[12]);
        accuracy = Double.valueOf(data[13]);
        bearing = Double.valueOf(data[14]);
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                String.valueOf(this.cellId),
                String.valueOf(this.locationAreaCode),
                String.valueOf(this.mobileCountryCode),
                String.valueOf(this.mobileNetworkCode),
                String.valueOf(this.dbm),
                String.valueOf(this.primaryScramblingCode),
                String.valueOf(this.rssi),
                String.valueOf(this.timingAdvance),
                String.valueOf(this.sid),
                String.valueOf(this.netType),
                String.valueOf(this.lon),
                String.valueOf(this.lat),
                String.valueOf(this.speed),
                String.valueOf(this.accuracy),
                String.valueOf(this.bearing)});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Cell createFromParcel(Parcel in) {
            return new Cell(in);
        }

        public Cell[] newArray(int size) {
            return new Cell[size];
        }
    };
}

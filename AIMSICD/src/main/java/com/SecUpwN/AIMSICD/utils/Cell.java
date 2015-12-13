/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;


/**
 * Description:    TODO:  What've got here...
 */
public class Cell implements Parcelable {

    // Cell Specific Variables
    private int cid;                // Cell Identification code
    private int lac;                // Location Area Code
    private int mcc;                // Mobile Country Code
    private int mnc;                // Mobile Network Code
    private int dbm;                // [dBm] RX signal "power"
    private int psc;                // Primary Scrambling Code
    private int rssi;               // Relative Signal Strength Indicator [dBm, asu etc.]
    private int timingAdvance;      // Timing Advance [LTE,GSM]
    private int sid;                // Cell-ID for [CDMA]
    private long timestamp;         // time

    // Tracked Cell Specific Variables
    private int netType;
    private double speed;       //
    private double accuracy;    //
    private double bearing;     //
    private double lon;
    private double lat;

    {
        cid = Integer.MAX_VALUE;
        lac = Integer.MAX_VALUE;
        mcc = Integer.MAX_VALUE;
        mnc = Integer.MAX_VALUE;
        dbm = Integer.MAX_VALUE;
        psc = Integer.MAX_VALUE;
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

    public Cell(int cid, int lac, int mcc, int mnc, int dbm, long timestamp) {
        super();
        this.cid = cid;
        this.lac = lac;
        this.mcc = mcc;
        this.mnc = mnc;
        this.dbm = dbm;
        this.rssi = Integer.MAX_VALUE;
        this.psc = Integer.MAX_VALUE;
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

    public Cell(int cid, int lac, int signal, int psc, int netType, boolean dbm) {
        this.cid = cid;
        this.lac = lac;
        this.mcc = Integer.MAX_VALUE;
        this.mnc = Integer.MAX_VALUE;

        if (dbm) {
            this.dbm = signal;
        } else {
            this.rssi = signal;
        }
        this.psc = psc;

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

    public Cell(int cid, int lac, int mcc, int mnc, int dbm, double accuracy, double speed,
                double bearing, int netType, long timestamp) {
        this.cid = cid;
        this.lac = lac;
        this.mcc = mcc;
        this.mnc = mnc;
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
     * Current Cell ID
     *
     * @return int representing the Cell ID from GSM or CDMA devices
     */
    public int getCID() {
        return this.cid;
    }

    /**
     * Sets current Cell ID
     *
     * @param cid Cell ID
     */
    public void setCID(int cid) {
        this.cid = cid;
    }

    /**
     * Location Area Code (LAC) of current Cell
     *
     * @return int Current cells Location Area Code
     */
    public int getLAC() {
        return this.lac;
    }

    /**
     * Set Location Area Code (LAC) of current Cell
     *
     * @param lac Location Area Code
     */
    public void setLAC(int lac) {
        this.lac = lac;
    }

    /**
     * Mobile Country Code (Mcc) of current Cell
     *
     * @return int Current cells Mobile Country Code
     */
    public int getMCC() {
        return this.mcc;
    }

    /**
     * Set Mobile Country Code (Mcc) of current Cell
     *
     * @param mcc Mobile Country Code
     */
    public void setMCC(int mcc) {
        this.mcc = mcc;
    }

    /**
     * Mobile Network Code (Mnc) of current Cell
     *
     * @return int Current cells Mobile Network Code
     */
    public int getMNC() {
        return this.mnc;
    }

    /**
     * Set Mobile Network Code (Mnc) of current Cell
     *
     * @param mnc Mobile Network Code
     */
    public void setMNC(int mnc) {
        this.mnc = mnc;
    }

    /**
     * Primary Scrambling Code (PSC) of current Cell
     *
     * @return int Current cells Primary Scrambling Code
     */
    public int getPSC() {
        return this.psc;
    }

    /**
     * Set Primary Scrambling Code (PSC) of current Cell
     *
     * @param psc Primary Scrambling Code
     */
    public void setPSC(int psc) {
        if (psc == -1) {
            this.psc = Integer.MAX_VALUE;
        } else {
            this.psc = psc;
        }
    }

    /**
     * CDMA System ID
     *
     * @return System ID or Integer.MAX_VALUE if not supported
     */
    public int getSID() {
        return this.sid;
    }

    /**
     * Set CDMA System ID (SID) of current Cell
     *
     * @param sid CDMA System ID
     */
    public void setSID(int sid) {
        this.sid = sid;
    }

    /**
     * Signal Strength Measurement (dBm)
     *
     * @return Signal Strength Measurement (dBm)
     */
    public int getDBM() {
        return dbm;
    }

    /**
     * Set Signal Strength (dBm) of current Cell
     *
     * @param dbm Signal Strength (dBm)
     */
    public void setDBM(int dbm) {
        this.dbm = dbm;
    }

    /**
     * Longitude Geolocation of current Cell
     *
     * @return Longitude
     */
    public double getLon() {
        return this.lon;
    }

    /**
     * Set Longitude Geolocation of current Cell
     *
     * @param lon Longitude
     */
    public void setLon(double lon) {
        this.lon = lon;
    }

    /**
     * Latitude Geolocation of current Cell
     *
     * @return Latitude
     */
    public double getLat() {
        return this.lat;
    }

    /**
     * Set Latitude Geolocation of current Cell
     *
     * @param lat Latitude
     */
    public void setLat(double lat) {
        this.lat = lat;
    }

    /**
     * Ground speed in metres/second
     *
     * @return Ground speed or 0.0 if unavailable
     */
    public double getSpeed() {
        return this.speed;
    }

    /**
     * Set current ground speed in metres/second
     *
     * @param speed Ground Speed
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * Accuracy of location in metres
     *
     * @return Location accuracy in metres or 0.0 if unavailable
     */
    public double getAccuracy() {
        return this.accuracy;
    }

    /**
     * Set current location accuracy in metres
     *
     * @param accuracy Location accuracy
     */
    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * Set current Bearing
     *
     * @param bearing Current bearing
     */
    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    /**
     * LTE Timing Advance
     *
     * @return LTE Timing Advance or Integer.MAX_VALUE if unavailable
     */
    public int getTimingAdvance() {
        return this.timingAdvance;
    }

    /**
     * Set current LTE Timing Advance
     *
     * @param ta Current LTE Timing Advance
     */
    public void setTimingAdvance(int ta) {
        this.timingAdvance = ta;
    }

    /**
     * Network Type
     *
     * @return string representing device Network Type
     */
    public int getNetType() {
        return this.netType;
    }

    /**
     * Set current Network Type
     *
     * @param netType Current Network Type
     */
    public void setNetType(int netType) {
        this.netType = netType;
    }

    /**
     * Timestamp of current cell information
     *
     * @return Timestamp
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * Set current cell information timestamp
     *
     * @param timestamp Current cell information timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Received Signal Strength
     *
     * @return Received Signal Strength Indicator (RSSI)
     */
    public int getRssi() {
        return this.rssi;
    }


    /**
     * TODO: What is this, and where is it supposed to be used ???
     *
     * @return
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + cid;
        result = prime * result + lac;
        result = prime * result + mcc;
        result = prime * result + mnc;
        if (psc != -1)
            result = prime * result + psc;
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
        if (this.psc != Integer.MAX_VALUE) {
            return this.cid == other.getCID() && this.lac == other.getLAC() && this.mcc == other
                    .getMCC() && this.mnc == other.getMNC() && this.psc == other.getPSC();
        } else {
            return this.cid == other.getCID() && this.lac == other.getLAC() && this.mcc == other
                    .getMCC() && this.mnc == other.getMNC();
        }
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("CID - ").append(cid).append("\n");
        result.append("LAC - ").append(lac).append("\n");
        result.append("MCC - ").append(mcc).append("\n");
        result.append("MNC - ").append(mnc).append("\n");
        result.append("DBm - ").append(dbm).append("\n");
        if (psc != Integer.MAX_VALUE) {
            result.append("PSC - ").append(psc).append("\n");
        }
        result.append("Type - ").append(netType).append("\n");
        result.append("Lon - ").append(lon).append("\n");
        result.append("Lat - ").append(lat).append("\n");

        return result.toString();
    }

    public boolean isValid() {
        return this.getCID() != Integer.MAX_VALUE && this.getLAC() != Integer.MAX_VALUE;
    }

    // Parcelling
    public Cell(Parcel in) {
        String[] data = new String[15];

        in.readStringArray(data);
        cid = Integer.valueOf(data[0]);
        lac = Integer.valueOf(data[1]);
        mcc = Integer.valueOf(data[2]);
        mnc = Integer.valueOf(data[3]);
        dbm = Integer.valueOf(data[4]);
        psc = Integer.valueOf(data[5]);
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
                String.valueOf(this.cid),
                String.valueOf(this.lac),
                String.valueOf(this.mcc),
                String.valueOf(this.mnc),
                String.valueOf(this.dbm),
                String.valueOf(this.psc),
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
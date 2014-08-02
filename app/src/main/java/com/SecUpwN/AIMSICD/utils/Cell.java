package com.SecUpwN.AIMSICD.utils;

public class Cell {

    //Cell Specific Variables
    private int cid;
    private int lac;
    private int mcc;
    private int mnc;
    private int dbm;
    private int psc;
    private long timestamp;

    //Tracked Cell Specific Variables
    private int netType;
    private double speed;
    private double accuracy;
    private double bearing;

    public Cell(int cid, int lac, int mcc, int mnc, int dbm, long timestamp) {
        this.cid = cid;
        this.lac = lac;
        this.mcc = mcc;
        this.mnc = mnc;
        this.dbm = dbm;
        this.psc = -1;
        this.timestamp = timestamp;
    }

    public Cell(int cid, int lac, int mcc, int mnc, int dbm, int psc, long timestamp) {
        this.cid = cid;
        this.lac = lac;
        this.mcc = mcc;
        this.mnc = mnc;
        this.dbm = dbm;
        this.psc = psc;
        this.timestamp = timestamp;
    }

    public Cell(int cid, int lac, int mcc, int mnc, int dbm, double accuracy, double speed,
            double bearing, int netType, long timestamp) {
        this.cid = cid;
        this.lac = lac;
        this.mcc = mcc;
        this.mnc = mnc;
        this.dbm = dbm;
        this.accuracy = accuracy;
        this.speed = speed;
        this.bearing = bearing;
        this.netType = netType;
        this.timestamp = timestamp;
    }

    public void setCID(int cid) {
        this.cid = cid;
    }

    public void setLAC(int lac) {
        this.lac = lac;
    }

    public void setMCC(int mcc) {
        this.mcc = mcc;
    }

    public void setMNC(int mnc) {
        this.mnc = mnc;
    }

    public void setPSC(int psc) {
        this.psc = psc;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getCID() {
        return this.cid;
    }

    public int getLAC() {
        return this.lac;
    }

    public int getDBM() {
        return dbm;
    }

    public void setDBM(int dbm) {
        this.dbm = dbm;
    }

    public int getMCC() {
        return this.mcc;
    }

    public int getMNC() {
        return this.mnc;
    }

    public int getPSC() {
        return this.psc;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + cid;
        result = prime * result + lac;
        result = prime * result + mcc;
        result = prime * result + mnc;
        if (psc != -1) result = prime * result + psc;
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        Cell other = (Cell) obj;
        if (this.psc != -1) {
            return this.cid == other.getCID() && this.lac == other.getLAC() && this.mcc == other
                    .getMCC() && this.mnc == other.getMNC() && this.psc == other.getPSC();
        } else {
            return this.cid == other.getCID() && this.lac == other.getLAC() && this.mcc == other
                    .getMCC() && this.mnc == other.getMNC();
        }
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("Cell ID (CID): ").append(cid).append("\n");
        result.append("Location Code (LAC): ").append(lac).append("\n");
        result.append("Country Code (MCC): ").append(mcc).append("\n");
        result.append("Network Code (MNC): ").append(mnc).append("\n");
        result.append("Signal Strength (DBM): ").append(dbm).append("\n");
        if (psc != -1) {
            result.append("Primary Scrambling Code (PSC): ").append(psc).append("\n");
        }
        return result.toString();
    }
}
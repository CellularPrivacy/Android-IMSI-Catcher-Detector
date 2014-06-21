package com.SecUpwN.AIMSICD.utils;

public class Cell {

    private int cid;
    private int lac;
    private int mcc;
    private int mnc;
    private int dbm;
    private long timestamp;

    public Cell() {
    }

    public Cell(int cid, int lac, int mcc, int mnc, int dbm, long timestamp) {
        this.cid = cid;
        this.lac = lac;
        this.mcc = mcc;
        this.mnc = mnc;
        this.dbm = dbm;
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
        return this.cid == other.getCID() && this.lac == other.getLAC() && this.mcc == other
                .getMCC() && this.mnc == other.getMNC();
    }

    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("Cell ID (CID): ").append(cid).append("\n");
        result.append("Location Code (LAC): ").append(lac).append("\n");
        result.append("Country Code (MCC): ").append(mcc).append("\n");
        result.append("Network Code (MNC): ").append(mnc).append("\n");
        result.append("Signal Strength (DBM): ").append(dbm).append("\n");

        return result.toString();
    }
}
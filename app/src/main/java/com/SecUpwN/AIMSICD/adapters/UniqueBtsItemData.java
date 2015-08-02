/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.adapters;

/**
 * Description:     Contains the data and definitions of all the items of the XML layout
 *
 * Dependencies:
 *                  UniqueBtsCardInflater.java
 *                  unique_bts_data.xml
 *
 * TODO:
 *                  [ ] Add record id to show item number in DBV
 *                  [ ] Fix typo T3213 to be T3212
 *                  [ ] Order all the items according to appearance found in the DB table below
 *                  [ ] Add DB items: T3212, A5x and ST_id
 *                  [ ] If (exact) gpse_lat/lon doesn't exist in DBe_import, set Lat/Lon to "-"
 *
 *
 *
 * NOTE:
 *                  CREATE TABLE "DBi_bts"  (
 *                   "_id"        	INTEGER PRIMARY KEY AUTOINCREMENT,
 *                   "MCC"       	INTEGER NOT NULL,	--
 *                   "MNC"       	INTEGER NOT NULL,	--
 *                   "LAC"       	INTEGER NOT NULL,	--
 *                   "CID"       	INTEGER NOT NULL,	--
 *                   "PSC"       	INTEGER,		--
 *                   "T3212"     	INTEGER DEFAULT 0,	-- Fix java to allow null here
 *                   "A5x"       	INTEGER DEFAULT 0,	-- Fix java to allow null here
 *                   "ST_id"     	INTEGER DEFAULT 0,	-- Fix java to allow null here
 *                   "time_first"	INTEGER,		--
 *                   "time_last" 	INTEGER,		--
 *                   "gps_lat"       REAL NOT NULL,		--
 *                   "gps_lon"       REAL NOT NULL		--
 *                   );
 *
 *
 * ChangeLog:
 *                  2015-07-27  E:V:A           Added placeholders for missing items, Fixed T3212 typo
 */

public class UniqueBtsItemData {

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getLac() {
        return lac;
    }

    public void setLac(String lac) {
        this.lac = lac;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getMnc() {
        return mnc;
    }

    public void setMnc(String mnc) {
        this.mnc = mnc;
    }

    public String getTime_first() {
        return time_first;
    }

    public void setTime_first(String time_first) {
        this.time_first = time_first;
    }

    public String getTime_last() {
        return time_last;
    }

    public void setTime_last(String time_last) {
        this.time_last = time_last;
    }

    public String getPsc() {
        return psc;
    }

    public void setPsc(String psc) {
        this.psc = psc;
    }

    // START new ==============================================================
    public String getT3212() {
        return t3212;
    }

    public void setT3212(String t3212) {
        this.t3212 = t3212;
    }

    public String getA5x() {
        return a5x;
    }

    public void setA5x(String a5x) {
        this.a5x = a5x;
    }

    public String getStId() {
        return st_id;
    }

    public void setStId(String st_id) {
        this.st_id = st_id;
    }


    // END new ==============================================================

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }


    public String getRecordId() { return mRecordId; }

    String mcc;
    String mnc;
    String lac;
    String cid;
    String psc;
    String t3212;
    String a5x;
    String st_id;
    String time_first;
    String time_last;
    String lat;
    String lon;

    String mRecordId;

    public UniqueBtsItemData(
            String imcc,
            String imnc,
            String ilac,
            String icid,
            String ipsc,
            String itime_first,
            String itime_last,
            String ilat,
            String ilon,
            //String it3212,
            //String ia5x,
            //String ist_id,
            String iRecordId
            ) {

            mcc = imcc;
            mnc = imnc;
            lac = ilac;
            cid = icid;
            psc = ipsc;
            time_first = itime_first;
            time_last = itime_last;
            lat = ilat;
            lon = ilon;
            //t3212 = it3212;
            //a5x = ia5x;
            //st_id = ist_id;
            mRecordId = iRecordId;
    }

}
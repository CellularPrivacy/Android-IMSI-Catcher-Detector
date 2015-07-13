/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.adapters;

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

    public String getT3213() {
        return t3213;
    }

    public void setT3213(String t3213) {
        this.t3213 = t3213;
    }

    public String getA5x() {
        return a5x;
    }

    public void setA5x(String a5x) {
        this.a5x = a5x;
    }

    public String getSt_id() {
        return st_id;
    }

    public void setSt_id(String st_id) {
        this.st_id = st_id;
    }

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


    String mcc;
    String mnc;
    String lac;
    String cid;
    String psc;
    String t3213;
    String a5x;
    String st_id;
    String time_first;
    String time_last;
    String lat;
    String lon;


    public UniqueBtsItemData(
            String imcc,
            String imnc,
            String ilac,
            String icid,
            String ipsc,
            String itime_first,
            String itime_last,
            String ilat,
            String ilon
            //String it3213,
            //String ia5x,
            //String ist_id,

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
        //t3213 = it3213;
        //a5x = ia5x;
        //st_id = ist_id;

    }


}
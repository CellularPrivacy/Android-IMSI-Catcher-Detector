/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */

/* Coded by Paul Kinsella <paulkinsella29@yahoo.ie> */

package com.SecUpwN.AIMSICD.smsdetection;

public class CapturedSmsData {

    private long id;
    private String senderNumber;
    private String senderMsg;
    private String smsTimestamp;
    private String smsType;
    private int current_lac;
    private int current_cid;
    private String current_nettype;
    private int current_roam_status;
    private double current_gps_lat;
    private double current_gps_lon;

    public String getCurrent_nettype() {
        return current_nettype;
    }

    public void setCurrent_nettype(String current_nettype) {
        this.current_nettype = current_nettype;
    }

    public int getCurrent_roam_status() {
        return current_roam_status;
    }

    public void setCurrent_roam_status(int current_roam_status) {
        this.current_roam_status = current_roam_status;
    }


    public double getCurrent_gps_lat() {
        return current_gps_lat;
    }

    public void setCurrent_gps_lat(double current_gps_lat) {
        this.current_gps_lat = current_gps_lat;
    }

    public double getCurrent_gps_lon() {
        return current_gps_lon;
    }

    public void setCurrent_gps_lon(double current_gps_lon) {
        this.current_gps_lon = current_gps_lon;
    }


    public CapturedSmsData(){

    }

    public int getCurrent_lac() {
        return current_lac;
    }

    public void setCurrent_lac(int current_lac) {
        this.current_lac = current_lac;
    }

    public int getCurrent_cid() {
        return current_cid;
    }

    public void setCurrent_cid(int current_cid) {
        this.current_cid = current_cid;
    }

    public String getSenderNumber() {
        return senderNumber;
    }

    public void setSenderNumber(String senderNumber) {
        this.senderNumber = senderNumber;
    }

    public String getSenderMsg() {
        return senderMsg;
    }

    public void setSenderMsg(String senderMsg) {
        this.senderMsg = senderMsg;
    }

    public String getSmsTimestamp() {
        return smsTimestamp;
    }

    public void setSmsTimestamp(String smsTimestamp) {
        this.smsTimestamp = smsTimestamp;
    }

    public String getSmsType() {
        return smsType;
    }

    public void setSmsType(String smsType) {
        this.smsType = smsType;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}

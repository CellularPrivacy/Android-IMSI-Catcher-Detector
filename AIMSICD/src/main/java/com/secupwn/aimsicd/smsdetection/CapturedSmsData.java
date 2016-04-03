/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */

/* Coded by Paul Kinsella <paulkinsella29@yahoo.ie> */

package com.secupwn.aimsicd.smsdetection;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
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

}

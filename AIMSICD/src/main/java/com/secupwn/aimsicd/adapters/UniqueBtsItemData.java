/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.adapters;

import lombok.Getter;
import lombok.Setter;

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
 *
 *
 *
 * NOTE:
 *                  CREATE TABLE "DBi_bts"  (
 *                   "_id"          INTEGER PRIMARY KEY AUTOINCREMENT,
 *                   "MCC"          INTEGER NOT NULL,   --
 *                   "MNC"          INTEGER NOT NULL,   --
 *                   "LAC"          INTEGER NOT NULL,   --
 *                   "CID"          INTEGER NOT NULL,   --
 *                   "PSC"          INTEGER,            --
 *                   "T3212"        INTEGER DEFAULT 0,  -- Fix java to allow null here
 *                   "A5x"          INTEGER DEFAULT 0,  -- Fix java to allow null here
 *                   "ST_id"        INTEGER DEFAULT 0,  -- Fix java to allow null here
 *                   "time_first"   INTEGER,            --
 *                   "time_last"    INTEGER,            --
 *                   "gps_lat"       REAL NOT NULL,     --
 *                   "gps_lon"       REAL NOT NULL      --
 *                   );
 *
 *
 * ChangeLog:
 *                  2015-07-27  E:V:A           Added placeholders for missing items, Fixed T3212 typo
 */

@Getter
@Setter
public class UniqueBtsItemData {

    private String mcc;
    private String mnc;
    private String lac;
    private String cid;
    private String psc;
    private String t3212;
    private String a5x;
    private String st_id;
    private String time_first;
    private String time_last;
    private String lat;
    private String lon;

    private String recordId;

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
            recordId = iRecordId;
    }

}

package com.secupwn.aimsicd.data.model;

import java.util.Date;

import io.realm.RealmObject;
import lombok.Getter;
import lombok.Setter;

/**
 * DF_id   DF_desc
 * ---------------
 * 1       changing locationAreaCode
 * 2       cell no in OCID
 * 3       "Detected Type-0 SMS"
 * 4       "Detected MWI SMS"
 * 5       "Detected WAP PUSH SMS"
 * 6       "Detected WAP PUSH (2) SMS"
 * 7
 */
@Getter
@Setter
public class Event extends RealmObject {

    private Date timestamp;
    private String message;

    private GpsLocation gpsLocation;

    private int cellId;
    private int locationAreaCode;
    private int primaryScramblingCode;

    private int dfId;
    private String dfDescription;

    @Override
    public void removeFromRealm() {
        gpsLocation.removeFromRealm();
        super.removeFromRealm();
    }
}

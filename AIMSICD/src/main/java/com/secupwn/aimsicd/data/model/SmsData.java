package com.secupwn.aimsicd.data.model;

import java.util.Date;

import io.realm.RealmObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsData extends RealmObject {

    private Date timestamp;
    private String senderNumber;
    private String message;
    private String type;
    private GpsLocation gpsLocation;
    private boolean roaming;

    private int locationAreaCode;
    private int cellId;
    private String radioAccessTechnology;

    @Override
    public void removeFromRealm() {
        gpsLocation.removeFromRealm();
        super.removeFromRealm();
    }
}

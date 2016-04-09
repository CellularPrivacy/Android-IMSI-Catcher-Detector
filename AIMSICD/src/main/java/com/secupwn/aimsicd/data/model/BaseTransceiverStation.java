package com.secupwn.aimsicd.data.model;

import java.util.Date;

import io.realm.RealmObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseTransceiverStation extends RealmObject {

    private int mobileCountryCode;
    private int mobileNetworkCode;
    private int locationAreaCode;
    private int cellId;
    private int primaryScramblingCode;
    private Date timeFirst;
    private Date timeLast;
    private GpsLocation gpsLocation;

    @Override
    public void removeFromRealm() {
        gpsLocation.removeFromRealm();
        super.removeFromRealm();
    }
}

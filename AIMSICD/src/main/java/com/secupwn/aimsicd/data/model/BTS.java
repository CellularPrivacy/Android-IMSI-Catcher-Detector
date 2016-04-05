package com.secupwn.aimsicd.data.model;

import java.util.Date;

import io.realm.RealmObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BTS extends RealmObject {

    private int mobileCountryCode;
    private int mobileNetworkCode;
    private int locationAreaCode;
    private int cellId;
    private int primaryScramblingCode;
    private int t3212;
    private int a5x;
    private int stId;
    private Date timeFirst;
    private Date timeLast;
    private LocationInfo locationInfo;

    @Override
    public void removeFromRealm() {
        locationInfo.removeFromRealm();
        super.removeFromRealm();
    }
}

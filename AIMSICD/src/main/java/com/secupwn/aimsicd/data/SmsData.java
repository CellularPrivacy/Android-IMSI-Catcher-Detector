package com.secupwn.aimsicd.data;

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
    private LocationInfo locationInfo;
    private boolean roaming;

    private int currentLocationAreaCode;
    private int currentCid;
    private String currentRat;

    @Override
    public void removeFromRealm() {
        locationInfo.removeFromRealm();
        super.removeFromRealm();
    }
}

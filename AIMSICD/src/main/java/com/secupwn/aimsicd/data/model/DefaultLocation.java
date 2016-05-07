package com.secupwn.aimsicd.data.model;

import io.realm.RealmObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultLocation extends RealmObject {

    private int mobileCountryCode;
    private String country;
    private GpsLocation gpsLocation;
}

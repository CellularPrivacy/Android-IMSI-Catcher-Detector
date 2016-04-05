package com.secupwn.aimsicd.data.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultLocation extends RealmObject {

    @PrimaryKey
    private int mobileCountryCode;
    private String country;
    private GpsLocation gpsLocation;
}

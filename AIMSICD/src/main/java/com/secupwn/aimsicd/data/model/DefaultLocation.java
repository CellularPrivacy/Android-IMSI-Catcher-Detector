package com.secupwn.aimsicd.data.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultLocation extends RealmObject {

    private String country;

    @PrimaryKey
    private int mobileCountryCode;
    private LocationInfo locationInfo;
}

package com.secupwn.aimsicd.data.model;

import io.realm.RealmObject;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LocationInfo extends RealmObject {

    private double latitude;
    private double longitude;
    private double accuracy;
}

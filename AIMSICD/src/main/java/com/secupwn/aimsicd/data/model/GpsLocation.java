package com.secupwn.aimsicd.data.model;

import io.realm.RealmObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GpsLocation extends RealmObject {

    /**
     * The latitude in degrees
     */
    private double latitude;

    /**
     * The longitude in degrees
     */
    private double longitude;
    private double accuracy;
}

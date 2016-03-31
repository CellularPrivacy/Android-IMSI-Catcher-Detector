package com.secupwn.aimsicd.data;

import io.realm.RealmObject;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Location extends RealmObject {

    private double latitude;
    private double longitude;
    private double accuracy;
}

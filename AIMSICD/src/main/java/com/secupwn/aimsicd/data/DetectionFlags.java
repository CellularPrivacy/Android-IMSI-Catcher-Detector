package com.secupwn.aimsicd.data;

import io.realm.RealmObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetectionFlags extends RealmObject {

    private int code;
    private String name;
    private String description;
    private int p1;
    private int p2;
    private int p3;
    private double p1Fine;
    private double p2Fine;
    private double p3Fine;
    private String appText;
    private String funcUse;
    private int iStatus;
    private int cmId;
}

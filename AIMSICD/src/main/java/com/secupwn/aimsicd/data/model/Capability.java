package com.secupwn.aimsicd.data.model;

import io.realm.RealmObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Capability extends RealmObject {

    private int mobileCountryCode;
    private int mobileNetworkCode;
    private int locationAreaCode;
    private String opName;
    private String bandPlan;
    private String expand;
}

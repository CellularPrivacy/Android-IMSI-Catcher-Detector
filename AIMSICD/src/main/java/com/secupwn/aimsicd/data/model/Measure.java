package com.secupwn.aimsicd.data.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Required;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Measure extends RealmObject {

    private BTS bts;
    private String ncList;
    @Required
    private Date time;
    private LocationInfo gpsd;
    private LocationInfo gpse;
    private String bbPower;
    private String bbRfTemp;
    private String txPower;
    private int rxSignal;
    private String rxStype;
    @Required
    private String radioAccessTechnology;
    private String bcch;
    private String tmsi;
    private int ta;
    private int pd;
    private int ber;
    private String avgEcNo;
    private boolean submitted;
    private boolean neighbour;
}

package com.secupwn.aimsicd.data.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Required;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Measure extends RealmObject {

    private BaseTransceiverStation baseStation;
    private String ncList;
    @Required
    private Date time;
    private GpsLocation gpsd;
    private GpsLocation gpse;
    private String bbPower;
    private String bbRfTemp;
    private String txPower;
    private int rxSignal;
    private String rxStype;
    @Required
    private String radioAccessTechnology;
    private String bcch;
    private String tmsi;
    private int timingAdvance;
    private int pd;
    private int ber;
    private String avgEcNo;
    private boolean submitted;
    private boolean neighbour;

    @Override
    public void removeFromRealm() {
        getGpsd().removeFromRealm();
        getGpse().removeFromRealm();
        super.removeFromRealm();
    }
}

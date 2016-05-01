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
    @Required
    private Date time;
    private GpsLocation gpsLocation;
    private int rxSignal;
    @Required
    private String radioAccessTechnology;
    private int timingAdvance;
    private boolean submitted;
    private boolean neighbor;

    @Override
    public void removeFromRealm() {
        getGpsLocation().removeFromRealm();
        super.removeFromRealm();
    }
}

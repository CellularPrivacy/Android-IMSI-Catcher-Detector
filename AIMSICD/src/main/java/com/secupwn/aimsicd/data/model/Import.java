package com.secupwn.aimsicd.data.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Required;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Import extends RealmObject {

    @Required
    private String dbSource;
    private String radioAccessTechnology;
    private int mobileCountryCode;
    private int mobileNetworkCode;
    private int locationAreaCode;
    private int cellId;
    private int primaryScramblingCode;
    private GpsLocation gpsLocation;
    private boolean gpsExact;
    private int avgRange;
    private int avgSignal;
    private int samples;
    private Date timeFirst;
    private Date timeLast;
    private Integer rejCause;
}

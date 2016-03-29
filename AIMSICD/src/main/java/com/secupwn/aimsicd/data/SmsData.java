package com.secupwn.aimsicd.data;

import java.util.Date;

import io.realm.RealmObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsData extends RealmObject {

    private Date timestamp;
    private String number;
    private String message;
    private Location location;
}

package com.secupwn.aimsicd.data;

import io.realm.RealmObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsDetectionString extends RealmObject {

    private String detectionString;
    private String smsType;
}

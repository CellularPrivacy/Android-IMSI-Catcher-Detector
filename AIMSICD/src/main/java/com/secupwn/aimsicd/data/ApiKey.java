package com.secupwn.aimsicd.data;

import java.util.Date;

import io.realm.RealmObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiKey extends RealmObject {

    private String name;
    private String type;
    private String key;
    private Date timeAdd;
    private Date timeExp;
}

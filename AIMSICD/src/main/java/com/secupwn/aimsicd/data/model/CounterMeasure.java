package com.secupwn.aimsicd.data.model;

import io.realm.RealmObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CounterMeasure extends RealmObject {

    private String name;
    private String description;
    private int thResh;
    private int thFine;
}

package com.SecUpwN.AIMSICD.utils;

/**
 * Created by thu on 12/19/14.
 */
public class Status {
    public static Type currentStatus;
    public enum Type {
        ALARM,
        MEDIUM,
        NORMAL,
        IDLE,
    }

    public static void setCurrentStatus(Type t) {
        currentStatus = t;
    }

    public static Type getStatus() {
        if(currentStatus == null) {
            return Type.IDLE;
        }
        return currentStatus;
    }
}

package com.SecUpwN.AIMSICD.utils;

/**
 * Class that sets, holds and returns current system status
 *
 * @author Tor Henning Ueland
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

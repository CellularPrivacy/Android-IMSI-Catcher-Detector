package com.SecUpwN.AIMSICD.rilexecutor;

public class StringsResult {

    public final String result[];

    public final Throwable exception;

    public StringsResult(String r[], Throwable ex) {
        result = r;
        exception = ex;
    }

}

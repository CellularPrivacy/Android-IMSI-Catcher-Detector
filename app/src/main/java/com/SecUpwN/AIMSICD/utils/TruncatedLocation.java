package com.SecUpwN.AIMSICD.utils;

import android.location.Location;

/**
 * Created by Marvin Arnold on 1/07/15.
 */
public class TruncatedLocation extends Location {

    public TruncatedLocation(Location l) {
        super(l);
    }

    @Override
    public double getLatitude() {
        return truncateDouble(super.getLatitude(), 5);
    }

    @Override
    public double getLongitude() {
        return truncateDouble(super.getLongitude(), 5);
    }

    public static double truncateDouble(String d, int numDecimal) {
        return  truncateDouble(Double.parseDouble(d), numDecimal);
    }

    public static double truncateDouble(double d, int numDecimal) {
        String s = String.format("%." + Integer.toString(numDecimal) +"f", d);
        return Double.parseDouble(s);
    }
}

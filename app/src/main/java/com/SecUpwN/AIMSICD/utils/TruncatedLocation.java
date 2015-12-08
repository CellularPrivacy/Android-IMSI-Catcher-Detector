package com.SecUpwN.AIMSICD.utils;

import android.location.Location;

import io.freefair.android.util.logging.AndroidLogger;
import io.freefair.android.util.logging.Logger;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Created by Marvin Arnold on 1/07/15.
 */
public class TruncatedLocation extends Location {

    private static final Logger log = AndroidLogger.forClass(TruncatedLocation.class);

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
        double td = 0;
        NumberFormat format = NumberFormat.getInstance();

        // %.<string>f, d <-- this is wrong on so many ways
        String s = String.format("%." + Integer.toString(numDecimal) + "f", d);
        try {
            Number number = format.parse(s);
            td = number.doubleValue();
        } catch (ParseException e) {
            log.error("parsing exception", e);
        }
        return td;
    }
}

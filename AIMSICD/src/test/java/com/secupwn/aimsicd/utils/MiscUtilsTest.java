package com.secupwn.aimsicd.utils;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class MiscUtilsTest {

    @Test
    public void testParseLogcatTimeStamp() throws Exception {
        Date date = MiscUtils.parseLogcatTimeStamp("03-31 01:04:39.348 14308-14308/com.SecUpwN.AIMSICD I/SignalStrengthTracker: Ignored signal sample for");

        assertEquals(2, date.getMonth()); //Month is 0 based
        assertEquals(31, date.getDate());

        assertEquals(1, date.getHours());
        assertEquals(4, date.getMinutes());
        assertEquals(39, date.getSeconds());
    }
}

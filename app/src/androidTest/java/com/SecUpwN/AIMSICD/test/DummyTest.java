package com.SecUpwN.AIMSICD.test;

import android.test.ActivityInstrumentationTestCase2;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.R;

/**
 * Created by toby on 2014/12/07.
 */
public class DummyTest extends ActivityInstrumentationTestCase2<AIMSICD> {

    public DummyTest() {
        super(AIMSICD.class);
    }

    public void testSomething() {
        try { Thread.sleep(1000); } catch (Exception e) {}
        assertEquals(getActivity().getTitle(), getActivity().getString(R.string.app_name_short));
    }
}

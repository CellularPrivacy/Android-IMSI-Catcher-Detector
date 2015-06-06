/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.test;

import android.test.ActivityInstrumentationTestCase2;

import com.SecUpwN.AIMSICD.AIMSICD;

/**
 * Created by toby on 2014/12/07.
 */
public class DummyTest extends ActivityInstrumentationTestCase2<AIMSICD> {

    public DummyTest() {
        super(AIMSICD.class);
    }

    /**
     * Manual test to check if the progress bar is visible in the main activity
     */
    public void testProgressBar() {
        try {
            getActivity().showProgressbar(true, 100, 0);
            Thread.sleep(5000);
            getActivity().showProgressbar(false, 100, 30);
            Thread.sleep(1000);
            getActivity().showProgressbar(false, 100, 50);
            Thread.sleep(1000);
            getActivity().showProgressbar(false, 100, 80);
            Thread.sleep(1000);
            getActivity().hideProgressbar();
        } catch (InterruptedException e) {
        }

        assertTrue(true);
    }
}

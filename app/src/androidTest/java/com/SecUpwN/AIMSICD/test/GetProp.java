/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.test;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.SecUpwN.AIMSICD.activities.DebugLogs;

import java.io.IOException;

/**
 * DebugLogs test
 */
public class GetProp extends ActivityInstrumentationTestCase2<DebugLogs> {

    public GetProp() {
        super(DebugLogs.class);
    }

    public void testGetPropsReturnsValue() throws IOException {
        DebugLogs activity = (DebugLogs) getActivity();
        try { Thread.sleep(1000); } catch (Exception e) {}
        String props = activity.getProp();
        Log.d("getprop", props);
        assertTrue(props.trim().length() > 0);
    }
}
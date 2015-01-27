package com.SecUpwN.AIMSICD.test;

import android.test.ActivityInstrumentationTestCase2;
import android.test.AndroidTestCase;
import android.widget.Toast;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.activities.DebugLogs;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.utils.Cell;

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
        assertTrue(props.trim().length() > 0);
    }
}
/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.enums;

import android.content.Context;

import com.SecUpwN.AIMSICD.R;

import java.util.ArrayList;
import java.util.Arrays;

public enum StatesDbViewer {

    UNIQUE_BTS_DATA(R.string.unique_bts_data),
    BTS_MEASUREMENTS(R.string.bts_measurements),
    IMPORTED_OCID_DATA(R.string.imported_ocid_data),
    DEFAULT_MCC_LOCATIONS(R.string.default_mmc_locations),
    SILENT_SMS(R.string.silent_sms),
    MEASURED_SIGNAL_STRENGTHS(R.string.measured_signal_strengths),
    EVENT_LOG(R.string.eventlog),
    DETECTION_STRINGS(R.string.detection_strings);
    //TODO DetectionFlags
    // DETECTION_FLAGS(R.string.detection_flags)

    private final int mStatementValue;

    StatesDbViewer(int pStatementValue) {
        mStatementValue = pStatementValue;
    }

    public int getStatementValue() {
        return mStatementValue;
    }


    public static ArrayList<StatesDbViewer> getStates() {
        return new ArrayList<>(Arrays.asList(values()));
    }

    public static StatesDbViewer getValueByOrdinal(int pOrdinal) {
        StatesDbViewer lResult = null;
        for (StatesDbViewer item : values()) {
            if (item.ordinal() == pOrdinal) {
                lResult = item;
                break;
            }
        }
        return lResult;
    }

    public String getDisplayName(Context pContext) {
        if (pContext == null) {
            return null;
        }
        return pContext.getString(getStatementValue());
    }

}

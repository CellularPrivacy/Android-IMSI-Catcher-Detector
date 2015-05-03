/* Android IMSI Catcher Detector
 *      Copyright (C) 2015
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You may obtain a copy of the License at
 *      https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/LICENSE
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
    EVENT_LOG(R.string.eventlog);
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

/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */

/* Coded by Paul Kinsella <paulkinsella29@yahoo.ie> */

package com.secupwn.aimsicd.smsdetection;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetectionStringsData {

    private final String detectionString;
    private final String detectionType;
    private boolean fakeData;


    public DetectionStringsData(String detectionString, String detectionType) {
        this(detectionString, detectionType, false);
    }

    public DetectionStringsData(String pString, String pType, boolean pIsFakeData) {
        detectionString = pString;
        detectionType = pType;
        fakeData = pIsFakeData;
    }
}

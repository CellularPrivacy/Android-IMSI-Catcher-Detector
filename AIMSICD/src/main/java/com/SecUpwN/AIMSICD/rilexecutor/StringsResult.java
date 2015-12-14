/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.rilexecutor;

public class StringsResult {

    public final String result[];

    public final Throwable exception;

    public StringsResult(String r[], Throwable ex) {
        result = r;
        exception = ex;
    }

}

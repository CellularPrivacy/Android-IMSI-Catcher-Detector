/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.rilexecutor;

public class RawResult {

    public final byte result[];

    public final Throwable exception;

    public RawResult(byte r[], Throwable ex) {
        result = r;
        exception = ex;
    }

}

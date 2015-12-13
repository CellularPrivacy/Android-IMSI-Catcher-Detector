/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.utils.atcmd;

import java.io.IOException;


/**
 *  Description:    ...
 *
 *  Issues:     [ ]
 *
 *              [ ]
 *
 *
 *  Notes:
 *
 *
 *  ChangeLog:
 *
 */
public class TtyPrivFile extends TtyStream {

    protected Process mReadProc;
    protected Process mWriteProc;

    public TtyPrivFile(String ttyPath) throws IOException {
        // TODO robustify su detection?
        this(
            new ProcessBuilder("su", "-c", "\\exec cat <" + ttyPath).start(),
            new ProcessBuilder("su", "-c", "\\exec cat >" + ttyPath).start()
        );
    }

    private TtyPrivFile(Process read, Process write) {
        super(read.getInputStream(), write.getOutputStream());
        mReadProc = read;
        mWriteProc = write;

        log.debug("mReadProc=" + mReadProc + ", mWriteProc=" + mWriteProc);
    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            // Have to do this to get readproc to exit.
            // I guess it gets blocked waiting for input, so let's give it some.
            mOutputStream.write("ATE0\r".getBytes("ASCII"));// disable local Echo
            mOutputStream.flush();
        } catch (IOException e) {
            log.error("moutputstream didnt close", e);
        }
        mReadProc.destroy();
        mWriteProc.destroy();
    }
}

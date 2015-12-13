/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.utils.atcmd;

import android.os.Message;

import java.io.File;
import java.io.IOException;

import io.freefair.android.util.logging.AndroidLogger;
import io.freefair.android.util.logging.Logger;

/**
 *  Description:    ...
 *
 *  Issues:     [ ] This probably won't work well with two clients! I don't know what happens
 *                  if your RIL currently uses the same AT interface.
 *              [ ] TODO track down SIGPIPE (apparently in "cat /dev/smd7") on uncaught exception?
 *                  The stack barf in logcat is bugging me, but I spent some time trying
 *                  to figure it out and can't.
 *
 *  Notes:
 *              QCom: /dev/smd7, possibly other SMD devices. On 2 devices I've checked,
 *              smd7 is owned by bluetooth:bluetooth, so that could be something to sniff for if
 *              it's not always smd7.
 *
 *              More common is that modem AT CoP is on:  /dev/smd0
 *              while the Bluetooth modem (which also us AT CoP is on: /dev/smd7
 *
 *  ChangeLog:
 *
 */
public abstract class AtCommandTerminal {

    protected static Logger log = AndroidLogger.forClass(AtCommandTerminal.class);

    // message may be null if the response is not needed
    public abstract void send(String s, Message message);

    public abstract void dispose();

    /**
     * @return
     * @throws UnsupportedOperationException if no instance can be made
     */
    public static AtCommandTerminal factory() throws UnsupportedOperationException {
        AtCommandTerminal term = null;

        File smdFile = new File("/dev/smd7");
        if (smdFile.exists()) {
            try {
                term = new TtyPrivFile(smdFile.getAbsolutePath());
            } catch (IOException e) {
                log.error("IOException in constructor", e);
                // fall through
            }
        }

        if (term == null) {
            throw new UnsupportedOperationException("unable to find AT command terminal");
        }

        return term;
    }

}

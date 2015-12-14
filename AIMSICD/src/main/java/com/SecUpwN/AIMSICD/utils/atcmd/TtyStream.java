/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.utils.atcmd;

import android.os.Message;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

/*package*/
class TtyStream extends AtCommandTerminal {

    protected InputStream mInputStream;
    protected OutputStream mOutputStream;

    private boolean mThreadRun = true;
    private Thread mIoThread;

    protected BlockingQueue<Pair<byte[], Message>> mWriteQ;

    /*package*/ TtyStream(InputStream in, OutputStream out) {
        mInputStream = in;
        mOutputStream = out;

        mIoThread = new Thread(new IoRunnable(), "AtCommandTerminalIO");
        mIoThread.start();

        mWriteQ = new LinkedBlockingQueue<>();

        // return result codes, return verbose codes, no local echo
        this.send("ATQ0V1E0", null);
    }

    private class IoRunnable implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(mInputStream, "ASCII"));
                while (mThreadRun) {
                    // wait for something to write
                    byte[] bytesOut;
                    Message resultMessage;
                    try {
                        Pair<byte[], Message> p = mWriteQ.take();
                        bytesOut = p.first;
                        resultMessage = p.second;
                    } catch (InterruptedException e) {
                        continue; // restart loop
                    }

                    try {
                        mOutputStream.write(bytesOut);
                        mOutputStream.write('\r');
                        mOutputStream.flush();
                    } catch (IOException e) {
                        log.error("Output IOException", e);
                        if (resultMessage != null) {
                            resultMessage.obj = e;
                            resultMessage.sendToTarget();
                        }
                        return; // kill thread
                    }

                    /**
                     * ETSI TS 127 007 gives this example:
                     * <CR><LF>+CMD2: 3,0,15,"GSM"<CR><LF>
                     * <CR><LF>+CMD2: (0-3),(0,1),(0-12,15),("GSM","IRA")<CR><LF>
                     * <CR><LF>OK<CR><LF>
                     *
                     * I see embedded <CR><LF> sequences to line-break within responses.
                     * We can fake it using the BufferedReader, ignoring blank lines.
                     */

                    // TODO error case (on Qcom at least): this thread gets hung waiting for a
                    // response if garbage commands are issued (e.g., "ls", which I was using to
                    // make sure shell commands were removed.)  It seems local echo will not echo
                    // that back, but it will echo back valid commands right away (even slow ones
                    // like AT+COPS=?).  So maybe enable echo and see if we get a quick echo back.
                    // If not, assume the command was not recognized and bail out of the read loop
                    // below.
                    // We could also have a timeout where we hope/assume a response isn't coming,
                    // and move on to process the next command.

                    // dispatch response lines until done
                    String line;
                    List<String> lines = new ArrayList<>();
                    do {
                        try {
                            line = in.readLine();
                            if (line == null) throw new IOException("reader closed");
                        } catch (IOException e) {
                            log.error("Input IOException", e);
                            if (resultMessage != null) {
                                resultMessage.obj = e;
                                resultMessage.sendToTarget();
                            }
                            return; // kill thread
                        }

                        if (line.length() != 0) lines.add(line);
                    	// ignore empty lines
                    } while (!(line.equals("OK") || line.equals("ERROR") || line.startsWith("+CME ERROR")));

                    // XXX this logging could have sensitive info
                    //log.debug("IO< " + lines);

                    if (resultMessage != null) {
                        resultMessage.obj = lines;
                        resultMessage.sendToTarget();
                    } else {
                        log.debug("Data came in with no handler");
                    }
                }
            } catch (UnsupportedEncodingException e) {
                // ASCII should work
                throw new RuntimeException(e);
            } finally {
                dispose();
            }
        }
    }

    @Override
    public void send(String s, Message resultMessage) {
        try {
            // XXX this logging could have sensitive info
            //log.debug("IO> " + s);
            mWriteQ.add(Pair.create(s.getBytes("ASCII"), resultMessage));
        } catch (UnsupportedEncodingException e) {
            // we assume that if a String is being used for convenience, it must be ASCII
            throw new RuntimeException(e);
        }
    }

    @Override
    public void dispose() {
        mThreadRun = false;
    }
}

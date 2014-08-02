/**   Copyright (C) 2013  Louis Teboul (a.k.a Androguide)
 *
 *    admin@pimpmyrom.org  || louisteboul@gmail.com
 *    http://pimpmyrom.org || http://androguide.fr
 *    71 quai Clémenceau, 69300 Caluire-et-Cuire, FRANCE.
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License along
 *      with this program; if not, write to the Free Software Foundation, Inc.,
 *      51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 **/

package com.SecUpwN.AIMSICD.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.lang.System.nanoTime;

public class ChildProcess {

    private String TAG = getClass().getSimpleName();

    private static final int PIPE_SIZE = 1024;

    private class ChildReader extends Thread {

        final InputStream mStream;

        final StringBuffer mBuffer;

        ChildReader(InputStream is, StringBuffer buf) {
            mStream = is;
            mBuffer = buf;
        }

        public void run() {
            byte[] buf = new byte[PIPE_SIZE];
            try {
                int len;
                while ((len = mStream.read(buf)) != -1) {
                    String s = new String(buf, 0, len);
                    mBuffer.append(s);
                }
            } catch (IOException e) {
                // Ignore
            }
            try {
                mStream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    private class ChildWriter extends Thread {

        final OutputStream mStream;

        final String mBuffer;

        ChildWriter(OutputStream os, String buf) {
            mStream = os;
            mBuffer = buf;
        }

        public void run() {
            int off = 0;
            byte[] buf = mBuffer.getBytes();
            try {
                while (off < buf.length) {
                    int len = Math.min(PIPE_SIZE, buf.length - off);
                    mStream.write(buf, off, len);
                    off += len;
                }
            } catch (IOException e) {
                // Ignore
            }
            try {
                mStream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    private final long mStartTime;
    private Process mChildProc;
    private ChildWriter mChildStdinWriter;
    private ChildReader mChildStdoutReader;
    private ChildReader mChildStderrReader;
    private StringBuffer mChildStdout;
    private StringBuffer mChildStderr;
    private int mExitValue;
    private long mEndTime;

    public ChildProcess(String[] cmdarray, String childStdin) {
        mStartTime = nanoTime();
        try {
            mChildProc = Runtime.getRuntime().exec(cmdarray);
            if (childStdin != null) {
                mChildStdinWriter = new ChildWriter(mChildProc.getOutputStream(), childStdin);
                mChildStdinWriter.start();
            }
            mChildStdout = new StringBuffer();
            mChildStdoutReader = new ChildReader(mChildProc.getInputStream(), mChildStdout);
            mChildStdoutReader.start();
            mChildStderr = new StringBuffer();
            mChildStderrReader = new ChildReader(mChildProc.getErrorStream(), mChildStderr);
            mChildStderrReader.start();
        } catch (IOException e) {
            // XXX: log
        }
    }

    public boolean isFinished() {
        boolean finished = true;
        if (mChildProc != null) {
            try {
                mChildProc.exitValue();
            } catch (IllegalStateException e) {
                finished = false;
            }
        }
        return finished;
    }

    public int waitFinished() {
        while (mChildProc != null) {
            try {
                mExitValue = mChildProc.waitFor();
                mEndTime = nanoTime();
                mChildProc = null;
                mChildStderrReader.join();
                mChildStderrReader = null;
                mChildStdoutReader.join();
                mChildStdoutReader = null;
                if (mChildStdinWriter != null) {
                    mChildStdinWriter.join();
                    mChildStdinWriter = null;
                }
            } catch (InterruptedException e) {
                // Ignore
            }
        }
        return mExitValue;
    }

    public CommandResult getResult() {
        if (!isFinished()) {
            throw new IllegalThreadStateException("Child process running");
        }
        return new CommandResult(
                mStartTime,
                mExitValue,
                mChildStdout.toString(),
                mChildStderr.toString(),
                mEndTime);
    }
}
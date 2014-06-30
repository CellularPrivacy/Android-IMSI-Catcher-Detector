package com.SecUpwN.AIMSICD.utils;

import android.os.SystemClock;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class Shell {

    private final String TAG = "AIMSICD_Shell";

    private static Process mShellProcess = null;
    private static DataOutputStream mShellStdIn = null;
    private static DataInputStream mShellStdOut = null;
    private static DataInputStream mShellStdErr = null;
    private static boolean mShellOpened = false;
    private static String mSerialDevice;

    public Shell() {
        openRootShell(true);
    }

    public Shell(String serialDevice) {
        openRootShell(true);
        setSerialDevice(serialDevice);
    }

    public List<String> GetStdOut() {
        if (mShellStdOut == null) {
            return null;
        }
        try {
            if (mShellStdOut.available() <= 0) {
                return null;
            }
        } catch (IOException ioe) {
            return null;
        }

        byte[] dataOut = null;
        int dataLength = 0;
        try {
            while (mShellStdOut.available() > 0) {
                byte[] data = new byte[1024];
                int readCount = mShellStdOut.read(data);
                if (readCount == -1) {
                    break;
                }
                // Realloc
                {
                    int currentSize = 0;
                    if (dataOut != null) {
                        currentSize = dataOut.length;
                    }
                    byte[] baNewDataOut = new byte[currentSize + readCount];
                    if (dataOut != null) {
                        System.arraycopy(dataOut, 0, baNewDataOut, 0, currentSize);
                    }
                    System.arraycopy(data, 0, baNewDataOut, currentSize, readCount);
                    dataOut = baNewDataOut;
                    dataLength += readCount;
                }
            }
        } catch (IOException ioe) {
            Log.i(TAG, "IOException, msg = " + ioe.getMessage());
        }
        Log.i(TAG, "Standard output read " + dataLength + " bytes");

        return Helpers.ByteArrayToStringList(dataOut, dataLength);
    }

    public List<String> GetStdErr() {
        if (mShellStdErr == null) {
            return null;
        }
        try {
            if (mShellStdErr.available() <= 0) {
                return null;
            }
        } catch (IOException ioe) {
            return null;
        }

        byte[] dataOut = null;
        int dataLength = 0;
        try {
            while (mShellStdErr.available() > 0) {
                byte[] data = new byte[1024];
                int readCount = mShellStdErr.read(data);
                if (readCount == -1) {
                    break;
                }
                // Realloc
                {
                    int currentSize = 0;
                    if (dataOut != null) {
                        currentSize = dataOut.length;
                    }
                    byte[] newDataOut = new byte[currentSize + readCount];
                    if (dataOut != null) {
                        System.arraycopy(dataOut, 0, newDataOut, 0, currentSize);
                    }
                    System.arraycopy(data, 0, newDataOut, currentSize, readCount);
                    dataOut = newDataOut;
                    dataLength += readCount;
                }
            }
        } catch (IOException ioe) {
            Log.i(TAG, "IOException, - " + ioe.getMessage());
        }
        Log.i(TAG, "Standard error read " + dataLength + " bytes");

        return Helpers.ByteArrayToStringList(dataOut, dataLength);
    }

    public boolean openRootShell(boolean reOpen) {
        Log.i(TAG, "Open root shell, reopen = " + reOpen);

        if (mShellOpened && !reOpen) {
            Log.i(TAG, "Shell already opened");
            return true;
        } else if (mShellOpened) {
            Log.i(TAG, "Close current shell");
            closeShell();
        }

        try {
            Log.i(TAG, "Starting rootshell");
            mShellProcess = Runtime.getRuntime().exec("su");
        } catch (IOException ioe) {
            Log.i(TAG, "Error starting shell - " + ioe.getMessage());
            mShellProcess = null;
            mShellStdIn = null;
            mShellStdOut = null;
            mShellStdErr = null;
            mShellOpened = false;
            return false;
        }

        Log.i(TAG, "Fetching shell stdin, stdout and stderr");
        mShellStdIn = new DataOutputStream(mShellProcess.getOutputStream());
        mShellStdOut = new DataInputStream(mShellProcess.getInputStream());
        mShellStdErr = new DataInputStream(mShellProcess.getErrorStream());
        try {
            Log.i(TAG, "Performing shell banner and query id, timeout = 20 secs");
            mShellStdIn.writeBytes("echo \"Welcome to AIMSICD's root Shell\"\n");
            mShellStdIn.writeBytes("id\n");
            mShellStdIn.flush();

            boolean result = false;
            for (int waitCount = 0; waitCount < 200; waitCount++) {
                List<String> stdOut = GetStdOut();
                if (stdOut != null) {
                    result = true;
                }
                List<String> stdErr = GetStdErr();
                if (stdErr != null) {
                    result = true;
                }
                if (result) {
                    if (stdOut != null) {
                        for (String line : stdOut) {
                            Log.i(TAG, "stdout: " + line);
                        }
                    }
                    if (stdErr != null) {
                        for (String line : stdErr) {
                            Log.i(TAG, "stderr: " + line);
                        }
                    }
                    break;
                }

                SystemClock.sleep(100);
                Log.i(TAG, ((waitCount + 1) * 100) + " ms waited, still no result");
            }

            if (!result) {
                Log.i(TAG, "Root shell timeout");
                closeShell();
                return false;
            }
        } catch (IOException ioe) {
            Log.i(TAG, "OpenRootShell - " + ioe.getMessage());
            closeShell();
            return false;
        }

        clearStdOutAndErr();
        mShellOpened = true;

        return true;
    }

    public void closeShell() {
        if (mShellStdIn != null) {
            Log.i(TAG, "Closing shell standard input");
            try {
                mShellStdIn.writeBytes("exit\n");
                mShellStdIn.flush();
                mShellStdIn.close();
            } catch (IOException ioe) {
                Log.i(TAG, "Close shell StdIn - " + ioe.getMessage());
            }
            mShellStdIn = null;
        }
        clearStdOutAndErr();
        if (mShellStdOut != null) {
            Log.i(TAG, "Closing shell standard output");
            try {
                mShellStdOut.close();
            } catch (IOException ioe) {
                Log.i(TAG, "Close shell StdOut - " + ioe.getMessage());
            }
            mShellStdOut = null;
        }
        if (mShellStdErr != null) {
            Log.i(TAG, "Closing shell standard error");
            try {
                mShellStdErr.close();
            } catch (IOException ioe) {
                Log.i(TAG, "Close shell StdErr - " + ioe.getMessage());
            }
            mShellStdErr = null;
        }
        if (mShellProcess != null) {
            try {
                Log.i(TAG, "Waiting for shell exit");
                mShellProcess.waitFor();
            } catch (InterruptedException ie) {
                Log.i(TAG, "Shell exit failed - " + ie.getMessage());
            }
            Log.i(TAG, "Closing shell");
            mShellProcess.destroy();
            mShellProcess = null;
        }

        mShellOpened = false;
        Log.i(TAG, "Shell closed");
    }

    private void clearStdOutAndErr() {
        if (mShellStdOut != null) {
            Log.i(TAG, "Flushing standard output ...");
            try {
                while (mShellStdOut.available() > 0) {
                    if (mShellStdOut.read() == -1) {
                        break;
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error flushing StdOut - " + e);
            }
        }
        if (mShellStdErr != null) {
            Log.i(TAG, "Flushing standard error ...");
            try {
                while (mShellStdErr.available() > 0) {
                    if (mShellStdErr.read() == -1) {
                        break;
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error flushing StdErr - " + e);
            }
        }
    }

    public boolean sendCommand(String command, float maxWaitSeconds) {
        Log.i(TAG, "Sending shell \"" + command + "\", wait " + maxWaitSeconds + " seconds");

        if (!mShellOpened) {
            return false;
        }
        if (mShellStdIn == null) {
            return false;
        }
        if (mShellStdOut == null) {
            return false;
        }
        if (mShellStdErr == null) {
            return false;
        }

        clearStdOutAndErr();
        try {
            int oldCount;
            try {
                oldCount = mShellStdOut.available() + mShellStdErr.available();
            } catch (IOException ioe) {
                oldCount = 0;
            }
            mShellStdIn.writeBytes(command + "\n");
            mShellStdIn.flush();
            for (int waitCount = 0; waitCount <= Math.round(maxWaitSeconds * 10); waitCount++) {
                int currCount;
                try {
                    currCount = mShellStdOut.available() + mShellStdErr.available();
                } catch (IOException ioe) {
                    currCount = 0;
                }
                Log.i(TAG, "Waiting for command return, idx = " + waitCount + ", old = " + oldCount
                        + ", curr = " + currCount);
                if (currCount != oldCount) {
                    Log.i(TAG, "Command returned");
                    break;
                }
                SystemClock.sleep(100);
            }
        } catch (IOException ioe) {
            Log.i(TAG, "Send shell failed - " + ioe.getMessage());
            return false;
        }

        List<String> stdOut = GetStdOut();
        if (stdOut != null) {
            for (String aStdOut : stdOut) {
                Log.i(TAG, aStdOut);
            }
        }
        List<String> stdErr = GetStdErr();
        if (stdErr != null) {
            for (String aStdErr : stdErr) {
                Log.i(TAG, aStdErr);
            }
        }

        return true;
    }

    public boolean sendCommandPreserveOut(String command, float maxWaitSeconds) {
        Log.i(TAG, "Sending shell \"" + command + "\", wait " + maxWaitSeconds + " seconds");

        if (!mShellOpened) {
            return false;
        }
        if (mShellStdIn == null) {
            return false;
        }
        if (mShellStdOut == null) {
            return false;
        }
        if (mShellStdErr == null) {
            return false;
        }

        clearStdOutAndErr();
        try {
            int oldCount;
            boolean returned = false;
            try {
                oldCount = mShellStdOut.available() + mShellStdErr.available();
            } catch (IOException ioe) {
                oldCount = 0;
            }
            mShellStdIn.writeBytes(command + "\n");
            mShellStdIn.flush();
            for (int waitCount = 0; waitCount <= Math.round(maxWaitSeconds * 10); waitCount++) {
                int currCount;
                try {
                    currCount = mShellStdOut.available() + mShellStdErr.available();
                } catch (IOException ioe) {
                    currCount = 0;
                }
                Log.i(TAG, "Waiting for command return, idx = " + waitCount + ", old = " + oldCount
                        + ", curr = " + currCount);
                if (currCount != oldCount) {
                    Log.i(TAG, "Command returned");
                    returned = true;
                    break;
                }
                SystemClock.sleep(100);
            }

        } catch (IOException ioe) {
            Log.i(TAG, "Send shell failed - " + ioe.getMessage());
            return false;
        }

        return true;
    }

    public boolean executeAt(String command) {
        return sendCommandPreserveOut("echo -e " + command + "\r > " + mSerialDevice, 5.0f);
    }

    public int getLastReturnValue() {
        clearStdOutAndErr();
        /* Lets print the last commands exit value to StdOut */
        if (!sendCommandPreserveOut("echo $?", 1.0f)) {
            return -65536;
        }
        List<String> stdOut = GetStdOut();
        if (stdOut != null) {
            int retValue = -65536;
            for (String aStdOut : stdOut) {
                try {
                    retValue = Integer.parseInt(aStdOut.trim());
                } catch (NumberFormatException nfe) {
                    continue;
                }
            }
            return retValue;
        } else {
            return -65536;
        }
    }


    public void setSerialDevice(String serialDevice) {
        mSerialDevice = serialDevice;
        sendCommandPreserveOut("cat " + mSerialDevice + " \u0026\r", 5.0f);
    }

    public void close() {
        mShellProcess.destroy();
        mShellProcess = null;
        Log.i(TAG, "Root shell destroyed");
    }

}
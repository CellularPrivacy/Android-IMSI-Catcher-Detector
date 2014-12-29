/*
 * Copyright (C) 2014 Alexey Illarionov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.SecUpwN.AIMSICD.rilexecutor;

import com.SecUpwN.AIMSICD.BuildConfig;

import android.annotation.SuppressLint;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Message;
import android.os.Parcel;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;


public class SamsungMulticlientRilExecutor implements OemRilExecutor {

    public static final String MULTICLIENT_SOCKET = "Multiclient";
    public static final String SOCKET_NAME_RIL = "rild";

    private static final int RIL_REQUEST_OEM_RAW = 59;
    private static final int RIL_REQUEST_OEM_STRINGS = 60;
    public static final int RIL_CLIENT_ERR_SUCCESS = 0;
    public static final int RIL_CLIENT_ERR_AGAIN = 1;
    public static final int RIL_CLIENT_ERR_INIT = 2;
    public static final int RIL_CLIENT_ERR_INVAL = 3;
    public static final int RIL_CLIENT_ERR_CONNECT = 4;
    public static final int RIL_CLIENT_ERR_IO = 5;
    public static final int RIL_CLIENT_ERR_RESPONSE = 6;
    public static final int RIL_CLIENT_ERR_UNKNOWN = 7;
    private static final int RESPONSE_SOLICITED = 0;
    private static final int RESPONSE_UNSOLICITED = 1;
    private static final int ID_REQUEST_AT_COMMAND = 5;
    private static final int ID_RESPONSE_AT_COMMAND = 104;

    // E:V:A:  2014-12-19  We need debugging on, until further notice.
    //  WARNING: Could fill your logcat if running app long!
    // private static final boolean DBG = BuildConfig.DEBUG;
    private static final boolean DBG = false;

    // TODO: EVA: We should add AIMSICD to this TAG
    private static final String TAG = SamsungMulticlientRilExecutor.class.getSimpleName();
    private volatile LocalSocketThread mThread;

    public SamsungMulticlientRilExecutor() {
    }

    @Override
    public DetectResult detect() {
        String gsmVerRilImpl = "";

        try {
            Class clazz;
            clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class, String.class);
            gsmVerRilImpl = (String) method.invoke(null, "gsm.version.ril-impl", "");
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }

        // E:V:A comment out for debugging purposes on other non-Samsung RILS
        // and moved gsm.version.. to catch...
        // WARNING may have bad consequences...
        //if (!gsmVerRilImpl.matches("Samsung\\s+RIL\\(IPC\\).*")) {
        //    return DetectResult.Unavailable("gsm.version.ril-impl = " + gsmVerRilImpl);
        //}

        LocalSocket s = new LocalSocket();
        try {
            s.connect(new LocalSocketAddress(MULTICLIENT_SOCKET));
        } catch (IOException e) {
            return DetectResult.Unavailable(
                    "Multiclient socket is not available\n" + "gsm.version.ril-impl = " + gsmVerRilImpl);
        } finally {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return DetectResult.AVAILABLE;
    }

    @Override
    public synchronized void start() {
        if (mThread != null) {
            Log.e(TAG, "OEM raw request executor thread is running");
            return;
        }
        mThread = new LocalSocketThread(MULTICLIENT_SOCKET);
        mThread.start();
    }

    @Override
    public synchronized void stop() {
        if (mThread == null) {
            Log.e(TAG, "OEM raw request executor thread is not running");
            return;
        }
        mThread.cancel();
        mThread = null;
    }

    @Override
    public synchronized void invokeOemRilRequestRaw(byte[] data, Message response) {
        if (mThread == null) {
            Log.e(TAG, "OEM raw request executor thread is not running");
            return;
        }
        try {
            mThread.invokeOemRilRequestRaw(data, response);
        } catch (IOException ioe) {
            Log.e(TAG, "invokeOemRilRequestRaw() error", ioe);
        }
    }

    @Override
    public synchronized void invokeOemRilRequestStrings(String[] strings, Message response) {
        if (mThread == null) {
            Log.e(TAG, "OEM raw request executor thread is not running");
            return;
        }
        try {
            mThread.invokeOemRilRequestStrings(strings, response);
        } catch (IOException ioe) {
            Log.e(TAG, "invokeOemRilRequestStrings() error", ioe);
        }
    }

    public class LocalSocketThread extends Thread {

        private static final int MAX_MESSAGES = 30;
        private final LocalSocketAddress mSocketPath;
        private final AtomicBoolean mCancelRequested = new AtomicBoolean();
        private LocalSocket mSocket;
        private volatile InputStream mInputStream;
        private volatile OutputStream mOutputStream;
        private final Random mTokenGen = new Random();
        private final Map<Integer, Message> mMessages;

        @SuppressLint("UseSparseArrays")
        public LocalSocketThread(String socketPath) {
            mSocketPath = new LocalSocketAddress(socketPath);
            mInputStream = null;
            mOutputStream = null;
            mMessages = new HashMap<>();
        }

        public void cancel() {
            if (DBG) {
                Log.v(TAG, "SamsungMulticlientRil cancel()");
            }
            synchronized (this) {
                mCancelRequested.set(true);
                disconnect();
                notifyAll();
            }
        }

        public synchronized void invokeOemRilRequestRaw(byte[] data, Message response)
                throws IOException {
            int token;
            if (mMessages.size() > MAX_MESSAGES) {
                Log.e(TAG, "message queue is full");
                return;
            }

            if (mOutputStream == null) {
                Log.e(TAG, "Local write() error: not connected");
                return;
            }

            do {
                token = mTokenGen.nextInt();
            } while (mMessages.containsKey(token));

            byte req[] = marshallRequest(token, data);

            if (DBG) {
                Log.v(TAG,
                        String.format("invokeOemRilRequestRaw() token: 0x%X, header: %s, req: %s ",
                                token, HexDump.toHexString(getHeader(req)),
                                HexDump.toHexString(req))
                );
            }

            mOutputStream.write(getHeader(req));
            mOutputStream.write(req);
            mMessages.put(token, response);
        }

        public synchronized void invokeOemRilRequestStrings(String strings[], Message response)
                throws IOException {
            int token;
            if (mMessages.size() > MAX_MESSAGES) {
                Log.e(TAG, "message queue is full");
                return;
            }

            if (mOutputStream == null) {
                Log.e(TAG, "Local write() error: not connected");
                return;
            }

            do {
                token = mTokenGen.nextInt();
            } while (mMessages.containsKey(token));

            byte[] req = marshallRequest(token, strings);

            if (DBG) {
                Log.v(TAG, String.format(
                        "invokeOemRilRequestStrings() token: 0x%X, header: %s, req: %s ",
                        token, HexDump.toHexString(getHeader(req)), HexDump.toHexString(req)));
            }

            mOutputStream.write(getHeader(req));
            mOutputStream.write(req);
            mMessages.put(token, response);
        }

        private byte[] getHeader(byte data[]) {
            int len = data.length;
            return new byte[]{
                    (byte) ((len >> 24) & 0xff),
                    (byte) ((len >> 16) & 0xff),
                    (byte) ((len >> 8) & 0xff),
                    (byte) (len & 0xff)
            };
        }

        private byte[] marshallRequest(int token, byte data[]) {
            Parcel p = Parcel.obtain();
            p.writeInt(RIL_REQUEST_OEM_RAW);
            p.writeInt(token);
            p.writeByteArray(data);
            byte[] res = p.marshall();
            p.recycle();
            return res;
        }

        private byte[] marshallRequest(int token, String strings[]) {
            Parcel p = Parcel.obtain();
            p.writeInt(RIL_REQUEST_OEM_STRINGS);
            p.writeInt(token);
            p.writeStringArray(strings);
            byte[] res = p.marshall();
            p.recycle();
            return res;
        }

        public synchronized void disconnect() {

            if (DBG) {
                Log.v(TAG, "Local disconnect()");
            }

            if (mSocket == null) {
                return;
            }

            try {
                mSocket.shutdownInput();
            } catch (IOException e) {
                Log.e(TAG, "Local shutdownInput() of mSocket failed", e);
            }

            try {
                mSocket.shutdownOutput();
            } catch (IOException e) {
                Log.e(TAG, "Local shutdownOutput() of mSocket failed", e);
            }

            try {
                mInputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Local close() of mInputStream failed", e);
            }

            try {
                mOutputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Local close() of mOutputStream failed", e);
            }

            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Local close() of mSocket failed", e);
            }

            mSocket = null;
            mInputStream = null;
            mOutputStream = null;
        }

        @Override
        public void run() {
            int rcvd;
            int endpos = 0;
            final byte buf[] = new byte[4096];

            Log.i(TAG, "BEGIN LocalSocketThread-Socket");
            setName("MultiClientThread");

            mSocket = new LocalSocket();
            try {
                mSocket.connect(mSocketPath);
                mInputStream = mSocket.getInputStream();
                mOutputStream = mSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Connect error", e);
                return;
            }

            while (!mCancelRequested.get()) {
                try {
                    rcvd = mInputStream.read(buf, endpos, buf.length - endpos);
                    if (rcvd < 0) {
                        if (DBG) {
                            Log.v(TAG, "EOF reached");
                        }
                        break;
                    }
                    endpos += rcvd;
                    if (endpos < 4) {
                        continue;
                    }

                    int msgLen = (buf[0] << 24) | (buf[1] << 16) | (buf[2] << 8) | (buf[3] & 0xff);
                    if (msgLen + 4 > buf.length) {
                        Log.e(TAG, "message to big. Length: " + msgLen);
                        endpos = 0;
                        continue;
                    }
                    if (endpos >= msgLen + 4) {
                        processRxPacket(buf, 4, msgLen);
                        int secondPktPos = msgLen + 4;
                        if (secondPktPos != endpos) {
                            System.arraycopy(buf, secondPktPos, buf, 0, endpos - secondPktPos);
                        }
                        endpos -= msgLen + 4;
                    }

                    if (endpos == buf.length) {
                        endpos = 0;
                    }
                } catch (IOException e) {
                    disconnect();
                }
            }
            disconnect();
        }

        private synchronized void processRxPacket(byte data[], int pos, int length) {
            int responseType;
            Parcel p;

            if (DBG) {

                Log.v(TAG,
                        "received " + length + " bytes: " + HexDump.toHexString(data, pos, length));
            }

            p = Parcel.obtain();
            try {
                p.unmarshall(data, pos, length);
                p.setDataPosition(0);

                responseType = p.readInt();
                switch (responseType) {
                    case RESPONSE_UNSOLICITED:
                        Log.v(TAG, "Unsolicited response ");
                        break;
                    case RESPONSE_SOLICITED:
                        processSolicited(p);
                        break;
                    default:
                        Log.v(TAG, "Invalid response type: " + responseType);
                        break;
                }
            } finally {
                p.recycle();
            }
        }

        private int processSolicited(Parcel p) {
            Integer token = null;
            byte responseData[] = null;
            String stringsResponseData[] = null;
            Exception errorEx = null;

            try {
                token = p.readInt();
                int err = p.readInt();

                if (DBG) {
                    Log.v(TAG, String.format("processSolicited() token: 0x%X err: %d", token, err));
                }

                if (err != RIL_CLIENT_ERR_SUCCESS) {
                    throw new RemoteException("remote error " + err);
                }

                responseData = p.createByteArray();
                stringsResponseData = p.createStringArray();

            } catch (Exception ex) {
                errorEx = ex;
            }

            if (token == null) {
                Log.e(TAG, "token is null", errorEx);
            } else {
                synchronized (this) {
                    Message m = mMessages.remove(token);

                    if (m != null) {
                        switch (m.what) {
                            case ID_REQUEST_AT_COMMAND:
                            case ID_RESPONSE_AT_COMMAND:
                            case RIL_REQUEST_OEM_STRINGS:
                                m.obj = new StringsResult(stringsResponseData, errorEx);
                                m.sendToTarget();
                                break;
                            default:
                                m.obj = new RawResult(responseData, errorEx);
                                m.sendToTarget();
                        }
                    } else {
                        Log.i(TAG, "Message with token " + token + " not found");
                    }
                }
            }
            return RIL_CLIENT_ERR_SUCCESS;
        }
    }

    public static class RemoteException extends Exception {

        public RemoteException() {
        }

        public RemoteException(String detailMessage) {
            super(detailMessage);
        }
    }

}

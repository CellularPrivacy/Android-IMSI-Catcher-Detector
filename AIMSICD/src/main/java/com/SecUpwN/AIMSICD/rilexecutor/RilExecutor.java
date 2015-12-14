/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.rilexecutor;

import android.content.Context;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import io.freefair.android.util.logging.AndroidLogger;
import io.freefair.android.util.logging.Logger;

import com.SecUpwN.AIMSICD.utils.Helpers;
import com.SecUpwN.AIMSICD.utils.OemCommands;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

/*
 * Class to handle Ril and Samsung MultiRil implementation. Used by the Aimsicd Service.
 */
public class RilExecutor {

    private final Logger log = AndroidLogger.forClass(RilExecutor.class);
    
    public boolean mMultiRilCompatible;

    /*
     * Samsung MultiRil Implementation
     */

    // E:V:A  2014-12-18
    // This function as implemented here , only works on Samsung Galaxy S2 GT-I9100
    // and possibly on the S3 GT-I9300 and the P7100. It currently depend on:
    //  1. baseband is an Intel XMM modem
    //  2. gsm.version.ril-impl = "Samsung RIL (IPC) v2.0"
    // However, it should be possible to extend this to the latest devices
    // and those also using Qualcomm basebands
    //
    // Q: How does it work?
    // A: It uses the internal BP ServiceMode function on the BP and forwards the info
    // to the AP ServiceMode wrapper App. that is known by various other names, such as:
    //
    // FactoryTest, FactoryTest_FB, serviceModeApp_FB, ServiceModeApp_RIL,
    // SecFactoryPhoneTest, Factory_RIL, ServiceMode etc etc.
    //
    // The protocol to make these forwarded messages are through IPC messages via
    // the RIL QMI interface on /dev/socket/rild | ril-debug ...or something like that.

    private static final int ID_REQUEST_START_SERVICE_MODE_COMMAND = 1;
    private static final int ID_REQUEST_FINISH_SERVICE_MODE_COMMAND = 2;
    private static final int ID_REQUEST_PRESS_A_KEY = 3;
    private static final int ID_REQUEST_REFRESH = 4;
    private static final int ID_RESPONSE = 101;
    private static final int ID_RESPONSE_FINISH_SERVICE_MODE_COMMAND = 102;
    private static final int ID_RESPONSE_PRESS_A_KEY = 103;
    private static final int REQUEST_TIMEOUT = 10000; // ms

    private final ConditionVariable mRequestCondvar = new ConditionVariable();
    private final Object mLastResponseLock = new Object();
    private volatile List<String> mLastResponse;
    private DetectResult mRilExecutorDetectResult;
    private OemCommands mOemCommands;
    private OemRilExecutor mRequestExecutor;
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    public RilExecutor(Context context) {
        mOemCommands = OemCommands.getInstance(context);
        mRequestExecutor = new SamsungMulticlientRilExecutor();
        mRilExecutorDetectResult = mRequestExecutor.detect();
        if (!mRilExecutorDetectResult.available) {
            mMultiRilCompatible = false;
            log.error("Samsung Multiclient RIL not available: " + mRilExecutorDetectResult.error);
            mRequestExecutor = null;
        } else {
            mRequestExecutor.start();
            mMultiRilCompatible = true;
            // Samsung MultiRil Initialization
            mHandlerThread = new HandlerThread("ServiceModeSeqHandler");
            mHandlerThread.start();

            Looper l = mHandlerThread.getLooper();
            if (l != null) {
                mHandler = new Handler(l, new MyHandler());
            }
        }

    }

    public void stop() {
        //Samsung MultiRil Cleanup
        if (mRequestExecutor != null) {
            mRequestExecutor.stop();
            mRequestExecutor = null;
            mHandler = null;
            mHandlerThread.quit();
            mHandlerThread = null;
        }
    }


    /**
     * Check the status of the Rill Executor
     *
     * @return DetectResult providing access status of the Ril Executor
     */
    public DetectResult getRilExecutorStatus() {
        return mRilExecutorDetectResult;
    }


    /**
     * Service Mode Command Helper to call with Timeout value
     *
     * @return executeServiceModeCommand adding REQUEST_TIMEOUT
     */
    private List<String> executeServiceModeCommand(int type, int subtype,
                                                   java.util.Collection<KeyStep> keySeqence) {
        return executeServiceModeCommand(type, subtype, keySeqence, REQUEST_TIMEOUT);
    }

    /**
     * Service Mode Command Helper to call with Timeout value
     *
     * @return executeServiceModeCommand adding REQUEST_TIMEOUT
     */
    private synchronized List<String> executeServiceModeCommand(int type, int subtype,
                                                                java.util.Collection<KeyStep> keySeqence, int timeout) {
        if (mRequestExecutor == null) {
            return Collections.emptyList();
        }

        mRequestCondvar.close();
        mHandler.obtainMessage(ID_REQUEST_START_SERVICE_MODE_COMMAND,
                type,
                subtype,
                keySeqence).sendToTarget();
        if (!mRequestCondvar.block(timeout)) {
            log.error("request timeout");
            return Collections.emptyList();
        } else {
            synchronized (mLastResponseLock) {
                return mLastResponse;
            }
        }
    }

    /**
     * Executes and receives the Ciphering Information request using
     * the Rill Executor
     *
     * @return String list response from Rill Executor
     */
    public List<String> getCipheringInfo() {
        return executeServiceModeCommand(
                OemCommands.OEM_SM_TYPE_TEST_MANUAL,
                OemCommands.OEM_SM_TYPE_SUB_CIPHERING_PROTECTION_ENTER,
                null
        );
    }

    /**
     * Executes and receives the Neighbouring Cell request using
     * the Rill Executor
     *
     * @return String list response from Rill Executor
     */
    public List<String> getNeighbours() {
        KeyStep getNeighboursKeySeq[] = new KeyStep[]{
                new KeyStep('\0', false),
                new KeyStep('1', false), // [1] DEBUG SCREEN
                new KeyStep('4', true), // [4] NEIGHBOUR CELL
        };

        return executeServiceModeCommand(
                OemCommands.OEM_SM_TYPE_TEST_MANUAL,
                OemCommands.OEM_SM_TYPE_SUB_ENTER,
                Arrays.asList(getNeighboursKeySeq)
        );

    }

    private static class KeyStep {

        public final char keychar;
        public final boolean captureResponse;
        public KeyStep(char keychar, boolean captureResponse) {
            this.keychar = keychar;
            this.captureResponse = captureResponse;
        }

        public static final KeyStep KEY_START_SERVICE_MODE = new KeyStep('\0', true);
    }

    private class MyHandler implements Handler.Callback {

        private int mCurrentType;
        private int mCurrentSubtype;
        private Queue<KeyStep> mKeySequence;

        @Override
        public boolean handleMessage(Message msg) {
            byte[] requestData;
            Message responseMsg;
            KeyStep lastKeyStep;

            switch (msg.what) {
                case ID_REQUEST_START_SERVICE_MODE_COMMAND:
                    mCurrentType = msg.arg1;
                    mCurrentSubtype = msg.arg2;
                    mKeySequence = new ArrayDeque<>(3);
                    if (msg.obj != null) {
                        mKeySequence.addAll((java.util.Collection<KeyStep>) msg.obj);
                    } else {
                        mKeySequence.add(KeyStep.KEY_START_SERVICE_MODE);
                    }
                    synchronized (mLastResponseLock) {
                        mLastResponse = new ArrayList<>();
                    }
                    requestData = mOemCommands.getEnterServiceModeData(
                            mCurrentType, mCurrentSubtype, OemCommands.OEM_SM_ACTION);
                    responseMsg = mHandler.obtainMessage(ID_RESPONSE);
                    mRequestExecutor.invokeOemRilRequestRaw(requestData, responseMsg);
                    break;
                case ID_REQUEST_FINISH_SERVICE_MODE_COMMAND:
                    requestData = mOemCommands.getEndServiceModeData(mCurrentType);
                    responseMsg = mHandler.obtainMessage(ID_RESPONSE_FINISH_SERVICE_MODE_COMMAND);
                    mRequestExecutor.invokeOemRilRequestRaw(requestData, responseMsg);
                    break;
                case ID_REQUEST_PRESS_A_KEY:
                    requestData = mOemCommands.getPressKeyData(msg.arg1, OemCommands.OEM_SM_ACTION);
                    responseMsg = mHandler.obtainMessage(ID_RESPONSE_PRESS_A_KEY);
                    mRequestExecutor.invokeOemRilRequestRaw(requestData, responseMsg);
                    break;
                case ID_REQUEST_REFRESH:
                    requestData = mOemCommands.getPressKeyData('\0', OemCommands.OEM_SM_QUERY);
                    responseMsg = mHandler.obtainMessage(ID_RESPONSE);
                    mRequestExecutor.invokeOemRilRequestRaw(requestData, responseMsg);
                    break;
                case ID_RESPONSE:
                    lastKeyStep = mKeySequence.poll();
                    try {
                        RawResult result = (RawResult) msg.obj;
                        if (result == null) {
                            log.error("result is null");
                            break;
                        }
                        if (result.exception != null) {
                            log.error("", result.exception);
                            break;
                        }
                        if (result.result == null) {
                            log.verbose("No need to refresh");
                            break;
                        }
                        if (lastKeyStep.captureResponse) {
                            synchronized (mLastResponseLock) {
                                mLastResponse
                                        .addAll(Helpers.unpackByteListOfStrings(result.result));
                            }
                        }
                    } finally {
                        if (mKeySequence.isEmpty()) {
                            mHandler.obtainMessage(ID_REQUEST_FINISH_SERVICE_MODE_COMMAND)
                                    .sendToTarget();
                        } else {
                            mHandler.obtainMessage(ID_REQUEST_PRESS_A_KEY,
                                    mKeySequence.element().keychar, 0).sendToTarget();
                        }
                    }
                    break;
                case ID_RESPONSE_PRESS_A_KEY:
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(ID_REQUEST_REFRESH), 10);
                    break;
                case ID_RESPONSE_FINISH_SERVICE_MODE_COMMAND:
                    mRequestCondvar.open();
                    break;

            }
            return true;
        }
    }
}
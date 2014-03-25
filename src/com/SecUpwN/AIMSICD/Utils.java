package com.SecUpwN.AIMSICD;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;
import com.SecUpwN.AIMSICD.cmdprocessor.CMDProcessor;
import com.stericson.RootTools.RootTools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class Utils {
    private static String TAG = "AIMSICD_Utils";

    public static void CheckUtils(Context ctx) {
        final String mChmod;
        final Context mInstance = ctx;

        // Check chmod utils
        if (RootTools.checkUtil("chmod"))
            mChmod = "chmod";
        else {
            if (RootTools.checkUtil("busybox") && RootTools.hasUtil("chmod", "busybox"))
                mChmod = "busybox chmod";
            else if (RootTools.checkUtil("toolbox") && RootTools.hasUtil("chmod", "toolbox"))
                mChmod = "toolbox chmod";
            else
                mChmod = "";
        }

    }

    // Copy assets to local
    private static boolean CopyAssetsToLocal(Context ctx, String mSourceName, String mDstName) {
        String mBasePath = GetBasePath(ctx);
        if (mBasePath.equals(""))
            return false;
        mDstName = mBasePath + "/" + mDstName;

        InputStream mAssetInput;
        OutputStream mAssetOutput;
        String outFileName = mDstName;
        try {
            File mFileOutput = new File(mDstName);
            if (mFileOutput.exists())
                mFileOutput.delete();

            mAssetOutput = new FileOutputStream(mFileOutput);
            mAssetInput = ctx.getAssets().open(mSourceName);
            byte[] tBuffer = new byte[4096];  /* 4K page size */
            int nLength;
            while ((nLength = mAssetInput.read(tBuffer)) > 0)
                mAssetOutput.write(tBuffer, 0, nLength);
            mAssetOutput.flush();
            mAssetInput.close();
            mAssetOutput.close();
        } catch (Exception e) {
            Log.e(TAG, "CopyAssetsToLocal() failed, msg = " + e.getMessage());
            return false;
        }

        return true;
    }

    // Get application data path
    private static String GetBasePath(Context ctx) {
        Context mContext = ctx.getApplicationContext();
        String mBasePath = "";
        if (mContext != null) {
            // No try catch the cont != null will prevent a possible NPE here
            if (mContext.getFilesDir().exists())
                mBasePath = mContext.getFilesDir().getAbsolutePath();
            else if (!mContext.getFilesDir().mkdirs())
                mBasePath = "";
        } else {
            mBasePath = "";
        }

        return mBasePath;
    }

    private static boolean InstallBinary(String mBinaryPath, String mBinaryName, String mChmod) {
        boolean mOperationSucceeded;

        mOperationSucceeded = CMDProcessor.runSuCommand("mount -o rw,remount /system").success();
        mOperationSucceeded &= CMDProcessor.runSuCommand("cp " + mBinaryPath + mBinaryName + " /system/xbin/" + mBinaryName).success();
        mOperationSucceeded &= CMDProcessor.runSuCommand(mChmod + " 755 /system/xbin/" + mBinaryName).success();
        mOperationSucceeded &= CMDProcessor.runSuCommand("sync").success();
        mOperationSucceeded &= CMDProcessor.runSuCommand("mount -o ro,remount /system").success();

        return mOperationSucceeded;
    }

    private static String ByteToString(byte[] mByteArray) {
        if (mByteArray == null)
            return null;
        try {
            String mResult = new String(mByteArray, "ASCII");
            mResult = String.copyValueOf(mResult.toCharArray(), 0, mByteArray.length);
            return mResult;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private static String[] ByteArrayToStringArray(byte[] mByteArray, int mDataLength) {
        if (mByteArray == null)
            return null;
        if (mDataLength <= 0)
            return null;
        if (mDataLength > mByteArray.length)
            return null;

        // Replace all invisible chars to '.'
        for (int i = 0; i < mDataLength; i++) {
            if ((mByteArray[i] == 0x0D) || (mByteArray[i] == 0x0A)) {
                mByteArray[i] = 0;
                continue;
            }
            if (mByteArray[i] < 0x20)
                mByteArray[i] = 0x2E;
            if (mByteArray[i] > 0x7E)
                mByteArray[i] = 0x2E;
        }

        // Split and convert to string
        List<String> mListString = new ArrayList<String>();
        for (int i = 0; i < mDataLength; i++) {
            if (mByteArray[i] == 0)
                continue;
            int nBlockLength = -1;
            for (int j = i + 1; j < mDataLength; j++) {
                if (mByteArray[j] == 0) {
                    nBlockLength = j - i;
                    break;
                }
            }
            if (nBlockLength == -1)
                nBlockLength = mDataLength - i;
            byte[] mBlockData = new byte[nBlockLength];
            System.arraycopy(mByteArray, i, mBlockData, 0, nBlockLength);
            mListString.add(ByteToString(mBlockData));
            i += nBlockLength;
        }

        if (mListString.size() <= 0)
            return null;
        String[] mResult = new String[mListString.size()];
        mListString.toArray(mResult);
        return mResult;
    }
}

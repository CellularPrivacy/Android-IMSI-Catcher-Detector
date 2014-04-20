/* Android IMSI Catcher Detector
 *      Copyright (C) 2014
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You may obtain a copy of the License at
 *      https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/LICENSE
 */

package com.SecUpwN.AIMSICD;

import com.SecUpwN.AIMSICD.cmdprocessor.CMDProcessor;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    private static String TAG = "AIMSICD_Utils";

    // Copy assets to local
    private static boolean CopyAssetsToLocal(Context ctx, String mSourceName,
            String mDstName) {
        String mBasePath = GetBasePath(ctx);
        if (mBasePath.equals("")) {
            return false;
        }
        mDstName = mBasePath + "/" + mDstName;

        InputStream mAssetInput;
        OutputStream mAssetOutput;
        try {
            File mFileOutput = new File(mDstName);
            if (mFileOutput.exists()) {
                mFileOutput.delete();
            }

            mAssetOutput = new FileOutputStream(mFileOutput);
            mAssetInput = ctx.getAssets().open(mSourceName);
            byte[] tBuffer = new byte[4096]; /* 4K page size */
            int nLength;
            while ((nLength = mAssetInput.read(tBuffer)) > 0) {
                mAssetOutput.write(tBuffer, 0, nLength);
            }
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
    public static String GetBasePath(Context ctx) {
        Context mContext = ctx.getApplicationContext();
        String mBasePath = "";
        if (mContext != null) {
            // No try catch the cont != null will prevent a possible NPE here
            if (mContext.getFilesDir().exists()) {
                mBasePath = mContext.getFilesDir().getAbsolutePath();
            } else if (!mContext.getFilesDir().mkdirs()) {
                mBasePath = "";
            }
        } else {
            mBasePath = "";
        }

        return mBasePath;
    }

    private static boolean InstallBinary(String mBinaryPath,
            String mBinaryName, String mChmod) {
        boolean mOperationSucceeded;

        mOperationSucceeded = CMDProcessor.runSuCommand(
                "mount -o rw,remount /system").success();
        mOperationSucceeded &= CMDProcessor.runSuCommand(
                "cp " + mBinaryPath + mBinaryName + " /system/xbin/"
                        + mBinaryName
        ).success();
        mOperationSucceeded &= CMDProcessor.runSuCommand(
                mChmod + " 755 /system/xbin/" + mBinaryName).success();
        mOperationSucceeded &= CMDProcessor.runSuCommand("sync").success();
        mOperationSucceeded &= CMDProcessor.runSuCommand(
                "mount -o ro,remount /system").success();

        return mOperationSucceeded;
    }

    private static String ByteToString(byte[] mByteArray) {
        if (mByteArray == null) {
            return null;
        }
        try {
            String mResult = new String(mByteArray, "ASCII");
            mResult = String.copyValueOf(mResult.toCharArray(), 0,
                    mByteArray.length);
            return mResult;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private static String[] ByteArrayToStringArray(byte[] mByteArray,
            int mDataLength) {
        if (mByteArray == null) {
            return null;
        }
        if (mDataLength <= 0) {
            return null;
        }
        if (mDataLength > mByteArray.length) {
            return null;
        }

        // Replace all invisible chars to '.'
        for (int i = 0; i < mDataLength; i++) {
            if ((mByteArray[i] == 0x0D) || (mByteArray[i] == 0x0A)) {
                mByteArray[i] = 0;
                continue;
            }
            if (mByteArray[i] < 0x20) {
                mByteArray[i] = 0x2E;
            }
            if (mByteArray[i] > 0x7E) {
                mByteArray[i] = 0x2E;
            }
        }

        // Split and convert to string
        List<String> mListString = new ArrayList<String>();
        for (int i = 0; i < mDataLength; i++) {
            if (mByteArray[i] == 0) {
                continue;
            }
            int nBlockLength = -1;
            for (int j = i + 1; j < mDataLength; j++) {
                if (mByteArray[j] == 0) {
                    nBlockLength = j - i;
                    break;
                }
            }
            if (nBlockLength == -1) {
                nBlockLength = mDataLength - i;
            }
            byte[] mBlockData = new byte[nBlockLength];
            System.arraycopy(mByteArray, i, mBlockData, 0, nBlockLength);
            mListString.add(ByteToString(mBlockData));
            i += nBlockLength;
        }

        if (mListString.size() <= 0) {
            return null;
        }
        String[] mResult = new String[mListString.size()];
        mListString.toArray(mResult);
        return mResult;
    }

    public static boolean isSdWritable() {

        boolean mExternalStorageAvailable = false;
        try {
            String state = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(state)) {
                // We can read and write the media
                mExternalStorageAvailable = true;
                Log.i(TAG, "External storage card is readable.");
            } else {
                mExternalStorageAvailable = false;
            }
        } catch (Exception ex) {
            Log.e(TAG, "isSdWritable - " + ex.getMessage());
        }
        return mExternalStorageAvailable;
    }

}

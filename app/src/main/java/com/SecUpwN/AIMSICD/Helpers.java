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

package com.SecUpwN.AIMSICD;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Helpers {

    private static final String TAG = "AIMSICD_Helpers";
    private static final int CHARS_PER_LINE = 34;

    /**
     * Long toast message
     *
     * @param context Application Context
     * @param msg     Message to send
     */
    public static void msgLong(Context context, String msg) {
        if (context != null && msg != null) {
            Toast.makeText(context, msg.trim(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Short toast message
     *
     * @param context Application Context
     * @param msg     Message to send
     */
    public static void msgShort(Context context, String msg) {
        if (context != null && msg != null) {
            Toast.makeText(context, msg.trim(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Long toast message
     *
     * @param context Application Context
     * @param msg     Message to send
     */
    public static void sendMsg(Context context, String msg) {
        if (context != null && msg != null) {
            msgLong(context, msg);
        }
    }

    /**
     * Return a timestamp
     *
     * @param context Application Context
     */
    @SuppressWarnings("UnnecessaryFullyQualifiedName")
    public static String getTimestamp(Context context) {
        String timestamp = "unknown";
        Date now = new Date();
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        java.text.DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        timestamp = dateFormat.format(now) + ' ' + timeFormat.format(now);
        return timestamp;
    }

    /**
     * Checks Network connectivity is available to download OpenCellID data
     *
     */
    public static Boolean isNetAvailable(Context context)  {

        try{
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobileInfo =
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifiInfo.isConnected() || mobileInfo.isConnected()) {
                return true;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
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

    public static List<String> unpackListOfStrings(byte aob[]) {

        if (aob.length == 0) {
            Log.v(TAG, "Length = 0");
            return Collections.emptyList();
        }

        int lines = aob.length / CHARS_PER_LINE;

        String[] display = new String[lines];
        for (int i = 0; i < lines; i++) {
            int offset, byteCount;
            offset = i * CHARS_PER_LINE + 2;
            byteCount = 0;

            if (offset + byteCount >= aob.length) {
                Log.e(TAG, "Unexpected EOF");
                break;
            }

            while (aob[offset + byteCount] != 0 && (byteCount < CHARS_PER_LINE)) {
                byteCount += 1;
                if (offset + byteCount >= aob.length) {
                    Log.e(TAG, "Unexpected EOF");
                    break;
                }
            }
            display[i] = new String(aob, offset, byteCount).trim();
        }

        int newLength = display.length;
        while (newLength > 0 && TextUtils.isEmpty(display[newLength - 1])) newLength -= 1;

        return Arrays.asList(Arrays.copyOf(display, newLength));
    }
}


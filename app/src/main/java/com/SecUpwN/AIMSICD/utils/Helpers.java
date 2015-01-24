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

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.service.CellTracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * Description:     This class contain many various functions to:
 *
 *                  - present Toast messages
 *                  - getTimestamp
 *                  - Check network connectivity
 *                  - Download CSV file with BTS data via HTTP API from OCID servers
 *                  - Convert ByteToString
 *                  - unpackListOfStrings
 *                  - Check if SD is writeable
 *                  - get System properties
 *                  - Check for SU and BusyBox
 *
 * Dependencies:    TODO: Write a few words about where the content of this is used.
 *
 * Issues:          AS complaints that several of these methods are not used...
 *
 * ChangeLog:
 *
 */
 public class Helpers {

    private static final String TAG = "AIMSICD_Helpers";

    private static final int CHARS_PER_LINE = 34;

    /**
     * Long toast message
     * (Predefined in AOS to 3500 ms = 3.5 sec)
     *
     * @param context Application Context
     * @param msg     Message to send
     */
    public static void msgLong(final Context context, final String msg) {
        if (context != null && msg != null) {
            new Handler(context.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, msg.trim(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Short toast message
     * (Predefined in AOS to 2000 ms = 2 sec)
     *
     * @param context Application Context
     * @param msg     Message to send
     */
    public static void msgShort(final Context context, final String msg) {
        if (context != null && msg != null) {
            new Handler(context.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, msg.trim(), Toast.LENGTH_SHORT).show();
                }
            });
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
        String timestamp;
        Date now = new Date();
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        java.text.DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        timestamp = dateFormat.format(now) + ' ' + timeFormat.format(now);
        return timestamp;
    }

    /**
     * Checks if Network connectivity is available to download OpenCellID data
     */
    public static Boolean isNetAvailable(Context context) {

        try {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobileInfo =
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifiInfo != null && mobileInfo != null) {
                return wifiInfo.isConnected() || mobileInfo.isConnected();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

   /**
    * Requests Cell data from OpenCellID.org (OCID).
    *
    * @param cell Current Cell Information
    *
    * The free OCID API has a download limit of 1000 BTSs for each download.
    * Thus we need to carefully select the area we choose to download and make sure it is
    * centered on the current GPS location. (It is also possible to query the number of
    * cells in a particular bounding box (bbox), and use that.)
    *
    * The bbox is described in the OCID API here:
    *   http://wiki.opencellid.org/wiki/API#Getting_the_list_of_cells_in_a_specified_area
    *
    * In an urban area, we could try to limit ourselves to an area of ~10 Km.
    * The (GSM) Timing Advance is limiting us to 35 Km.
    *
    * The OCID API payload:
    *
    *     required:   <apiKey>&BBOX=<latmin>,<lonmin>,<latmax>,<lonmax>
    *     optional:   &mcc=<mcc>&mnc=<mnc>&lac=<lac>&radio=<radio>
    *                 &limit=<limit>&offset=<offset>&format=<format>
    *
    *  Our API query is using:  (Lat1,Lon1, Lat2,Lon2, mcc,mnc,lac)
    *
    *  Issues:
    *
    *      [ ] A too restrictive payload leads to many missing BTS in area, but a too liberal
    *          payload would return many less relevant ones and would cause us to reach the
    *          OCID API 1000 BTS download limit much faster. The solution would be to make the
    *          BBOX smaller, but that in turn, would result in the loss of some more distant,
    *          but still available towers. Possibly making them appears as RED, even when they
    *          are neither fake nor IMSI-catchers. However, a more realistic BTS picture is
    *          more useful, especially when sharing that info across different devices using
    *          on different RAT and MNO.

    *
    *  ChangeLog:
    *
    *      2015-01-22   E:V:A   Removed some restrictive payload items, leaving MCC.
    *      2015-01-23   E:V:A   Tightened the BBOX from 10 to 5 Km, because of above.
    *      2015-01-24   E:V:A   Re-imposed the MNC constraint.
    *                           TODO: Inform user that BTSs from other networks are not shown.
    *
    */
    public static void getOpenCellData(Context context, Cell cell, char type) {
        if (Helpers.isNetAvailable(context)) {
            if (!CellTracker.OCID_API_KEY.equals("NA")) {
                double earthRadius = 6371.01; // [Km]

                if (cell.getLat() != 0.0 && cell.getLon() != 0.0) {
                    //New GeoLocation object to find bounding Coordinates
                    GeoLocation currentLoc = GeoLocation.fromDegrees(cell.getLat(), cell.getLon());

                    //Calculate the Bounding Box Coordinates in a 5 Km radius //0 = min 1 = max
                    GeoLocation[] boundingCoords = currentLoc.boundingCoordinates(5, earthRadius);
                    String boundParameter;

                    //Request OpenCellID data for Bounding Coordinates (0 = min, 1 = max)
                    boundParameter = String.valueOf(boundingCoords[0].getLatitudeInDegrees()) + ","
                                   + String.valueOf(boundingCoords[0].getLongitudeInDegrees()) + ","
                                   + String.valueOf(boundingCoords[1].getLatitudeInDegrees()) + ","
                                   + String.valueOf(boundingCoords[1].getLongitudeInDegrees());

                    StringBuilder sb = new StringBuilder();
                    sb.append("http://www.opencellid.org/cell/getInArea?key=")
                            .append(CellTracker.OCID_API_KEY).append("&BBOX=")
                            .append(boundParameter);

                    if (cell.getMCC() != Integer.MAX_VALUE) {
                        sb.append("&mcc=").append(cell.getMCC());
                    }
                    if (cell.getMNC() != Integer.MAX_VALUE) {
                        sb.append("&mnc=").append(cell.getMNC());
                    }
                    //if (cell.getLAC() != Integer.MAX_VALUE) {
                    //    sb.append("&lac=").append(cell.getLAC());
                    //}

                    sb.append("&format=csv");
                    new RequestTask(context, type).execute(sb.toString());
                }
            } else {
                Helpers.sendMsg(context, "No OpenCellID API Key detected! \nPlease enter your key in settings first.");
            }
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.no_network_connection_title)
                    .setMessage(R.string.no_network_connection_message);
            builder.create().show();
        }
    }

    public static String ByteToString(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        try {
            String result = new String(byteArray, "ASCII");
            result = String.copyValueOf(result.toCharArray(), 0, byteArray.length);
            return result;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * Converts a byte array into a String array
     *
     * @param byteArray  byte array to convert
     * @param dataLength length of byte array
     * @return String array copy of passed byte array
     */
    public static List<String> ByteArrayToStringList(byte[] byteArray, int dataLength) {
        if (byteArray == null) {
            return null;
        }
        if (dataLength <= 0) {
            return null;
        }
        if (dataLength > byteArray.length) {
            return null;
        }

        // Replace all invisible chars with '.'
        for (int i = 0; i < dataLength; i++) {
            if ((byteArray[i] == 0x0D) || (byteArray[i] == 0x0A)) {
                byteArray[i] = 0;
                continue;
            }
            if (byteArray[i] < 0x20) {
                byteArray[i] = 0x2E;
            }
            if (byteArray[i] > 0x7E) {
                byteArray[i] = 0x2E;
            }
        }

        // Split and convert to string
        List<String> result = new ArrayList<>();
        for (int i = 0; i < dataLength; i++) {
            if (byteArray[i] == 0) {
                continue;
            }
            int blockLength = -1;
            for (int j = i + 1; j < dataLength; j++) {
                if (byteArray[j] == 0) {
                    blockLength = j - i;
                    break;
                }
            }
            if (blockLength == -1) {
                blockLength = dataLength - i;
            }
            byte[] mBlockData = new byte[blockLength];
            System.arraycopy(byteArray, i, mBlockData, 0, blockLength);
            result.add(ByteToString(mBlockData));
            i += blockLength;
        }
        if (result.size() <= 0) {
            return null;
        }
        return result;
    }

    /**
     * Checks if the external media (SD Card) is writable
     *
     * @return boolean True if Writable
     */
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

    /**
     * Return a String List representing response from invokeOemRilRequestRaw
     *
     * @param aob byte array response from invokeOemRilRequestRaw
     */
    public static List<String> unpackListOfStrings(byte aob[]) {

        if (aob.length == 0) {
            // WARNING: This one is very chatty!
            Log.v(TAG, "invokeOemRilRequestRaw: string list response Length = 0");
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
        while (newLength > 0 && TextUtils.isEmpty(display[newLength - 1])) {
            newLength -= 1;
        }
        return Arrays.asList(Arrays.copyOf(display, newLength));
    }

    /**
     * Return a String List representing response from invokeOemRilRequestRaw
     *
     * @param aob Byte array response from invokeOemRilRequestRaw
     */
    public static List<String> unpackByteListOfStrings(byte aob[]) {

        if (aob.length == 0) {
            // WARNING: This one is very chatty!
            Log.v(TAG, "invokeOemRilRequestRaw: byte-list response Length = 0");
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
        while (newLength > 0 && TextUtils.isEmpty(display[newLength - 1])) {
            newLength -= 1;
        }
        return Arrays.asList(Arrays.copyOf(display, newLength));
    }

    public static String getProp(String prop) {
        return CMDProcessor.runSuCommand("getprop " + prop).getStdout();
    }

    public static String getSystemProp(Context context, String prop, String def) {
        String result = null;
        try {
            result = SystemPropertiesReflection.get(context, prop);
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Failed to get system property: " + prop);
        }
        return result == null ? def : result;
    }

    /**
     * Checks device for SuperUser permission
     *
     * @return If SU was granted or denied
     */
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    public static boolean checkSu() {
        if (!new File("/system/bin/su").exists()
                && !new File("/system/xbin/su").exists()) {
            Log.e(TAG, "su binary does not exist!!!");
            return false; // tell caller to bail...
        }
        try {
            if (CMDProcessor.runSuCommand("ls /data/app-private").success()) {
                Log.i(TAG, " SU exists and we have permission");
                return true;
            } else {
                Log.i(TAG, " SU exists but we don't have permission");
                return false;
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointer throw while looking for su binary", e);
            return false;
        }
    }

    /**
     * Checks to see if Busybox is installed in "/system/"
     *
     * @return If busybox exists
     */
    public static boolean checkBusybox() {
        if (!new File("/system/bin/busybox").exists()
                && !new File("/system/xbin/busybox").exists()) {
            Log.e(TAG, "Busybox not in xbin or bin!");
            return false;
        }
        try {
            if (!CMDProcessor.runSuCommand("busybox mount").success()) {
                Log.e(TAG, "Busybox is there but it is borked! ");
                return false;
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "NullpointerException thrown while testing busybox", e);
            return false;
        }
        return true;
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

}


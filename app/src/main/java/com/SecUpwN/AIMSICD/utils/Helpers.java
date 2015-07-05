/**
 *     Copyright (C) 2013  Louis Teboul    <louisteboul@gmail.com>
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
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 **/

package com.SecUpwN.AIMSICD.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.activities.MapViewerOsmDroid;
import com.SecUpwN.AIMSICD.service.CellTracker;
import com.SecUpwN.AIMSICD.utils.Toaster;

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
 *                  - Check if SD is writable
 *                  - get System properties
 *                  - Check for SU and BusyBox
 *
 * Dependencies:    This is used all over the place, the non-trivial ones are in:
 *
 *                      AIMSICD.java
 *                      AtCommandFragment.java
 *                      Device.java
 *                      MapViewerOsmDroid.java
 *                      RequestTask.java
 *                      RilExecutor.java
 *
 *
 *                  get/setprop:    SystemPropertiesReflection.java
 *
 *
 * Issues:          AS complaints that several of these methods are not used...
 *
 * ChangeLog:       2015-05-08   SecUpwN   Added Toast Extender for longer toasts
 *
 */
 public class Helpers {

    private static final String TAG = "AIMSICD";
    private static final String mTAG = "Helpers";

    private static final int CHARS_PER_LINE = 34;

   /**
    * Description:      Long toast message
    *
    * Notes:
    *
    *       This is only a proxy method to the Toaster class.
    *       It also takes care of using the Toaster's Singleton.
    *
    * @param context Application Context
    * @param msg     Message to send
    */
    public static void msgLong(Context context, String msg) {
        if (context != null && msg != null) {
            Toaster.getInstance().msgLong(context, msg);
        }
    }
   /**
    * Description:      Short toast message
    *
    * Notes:
    *
    *       This is only a proxy method to the Toaster class.
    *       It also takes care of using the Toaster's Singleton.
    *
    * @param context Application Context
    * @param msg     Message to send
    */
    public static void msgShort(Context context, String msg) {
        if (context != null && msg != null) {
            Toaster.getInstance().msgShort(context, msg);
        }
    }
   /**
    * Description:      Long toast message
    *
    * Notes:
    *
    *       This is only a proxy method to the Toaster class.
    *       It also takes care of using the Toaster's Singleton.
    *
    * @param context Application Context
    * @param msg     Message to send
    */
    public static void sendMsg(Context context, String msg) {
        if (context != null && msg != null) {
            Toaster.getInstance().msgLong(context, msg);
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
     * Requires:        android:name="android.permission.ACCESS_NETWORK_STATE"
     */
    public static Boolean isNetAvailable(Context context) {

        try {
            ConnectivityManager cM = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo =   cM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobileInfo = cM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifiInfo != null && mobileInfo != null) {
                return wifiInfo.isConnected() || mobileInfo.isConnected();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

   /**
    * Description:      Requests Cell data from OpenCellID.org (OCID).
    *
    * Notes:
    *
    *       The free OCID API has a download limit of 1000 BTSs for each download.
    *       Thus we need to carefully select the area we choose to download and make sure it is
    *       centered on the current GPS location. (It is also possible to query the number of
    *       cells in a particular bounding box (bbox), and use that.)
    *
    *       The bbox is described in the OCID API here:
    *       http://wiki.opencellid.org/wiki/API#Getting_the_list_of_cells_in_a_specified_area
    *
    *       In an urban area, we could try to limit ourselves to an area radius of ~2 Km.
    *       The (GSM) Timing Advance is limiting us to 35 Km.
    *
    *       The OCID API payload:
    *
    *       required:   key=<apiKey>&BBOX=<latmin>,<lonmin>,<latmax>,<lonmax>
    *       optional:   &mcc=<mcc>&mnc=<mnc>&lac=<lac>&radio=<radio>
    *                   &limit=<limit>&offset=<offset>&format=<format>
    *
    *       Our API query is using:  (Lat1,Lon1, Lat2,Lon2, mcc,mnc,lac)
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
    *      [ ] We need a smarter way to handle the downloading of the BTS data. The OCID API
    *          allows for finding how many cells are contained in a query. We can the use this
    *          info to loop the max query size to get all those cells. The Query format is:
    *
    *          GET:        http://<WebServiceURL>/cell/getInAreaSize
    *
    *          The OCID API payload:
    *
    *          required:     key=<apiKey>&BBOX=<latmin>,<lonmin>,<latmax>,<lonmax>
    *          optional:     &mcc=<mcc>&mnc=<mnc>&lac=<lac>&radio=<radio>&format=<format>
    *
    *          result:       JSON:
    *                              {
    *                                count: 123
    *                              }
    *
    *      [x]  Q:  How is the BBOX actually calculated from the "radius"?
    *           A:  It's calculated as an inscribed circle to a square of 2*R on each side.
    *               See ./utils/GeoLocation.java
    *
    *  Dependencies:    GeoLocation.java
    *
    *  Used:
    *
    *  ChangeLog:
    *
    *      2015-01-22   E:V:A   Removed some restrictive payload items, leaving MCC.
    *      2015-01-23   E:V:A   Tightened the BBOX from 10 to 5 Km, because of above.
    *      2015-01-24   E:V:A   Re-imposed the MNC constraint.
    *      2015-02-13   E:V:A   Tightened the BBOX from 5 to 2 Km, and added it to Log.
    *
    *
    * @param cell Current Cell Information
    *
    */
    public static void getOpenCellData(Context context, Cell cell, char type) {
        if (Helpers.isNetAvailable(context)) {
            if (!CellTracker.OCID_API_KEY.equals("NA")) {
                double earthRadius = 6371.01; // [Km]
                int radius = 2; // Use a 2 Km radius with center at GPS location.

                if (cell.getLat() != 0.0 && cell.getLon() != 0.0) {
                    //New GeoLocation object to find bounding Coordinates
                    GeoLocation currentLoc = GeoLocation.fromDegrees(cell.getLat(), cell.getLon());

                    //Calculate the Bounding Box Coordinates using an N Km "radius" //0=min, 1=max
                    GeoLocation[] boundingCoords = currentLoc.boundingCoordinates(radius, earthRadius);
                    String boundParameter;

                    //Request OpenCellID data for Bounding Coordinates (0 = min, 1 = max)
                    boundParameter = String.valueOf(boundingCoords[0].getLatitudeInDegrees()) + ","
                                   + String.valueOf(boundingCoords[0].getLongitudeInDegrees()) + ","
                                   + String.valueOf(boundingCoords[1].getLatitudeInDegrees()) + ","
                                   + String.valueOf(boundingCoords[1].getLongitudeInDegrees());

                    Log.i(TAG, mTAG + ":OCID BBOX is set to: " + boundParameter
                                + "  with radius " + radius + " Km.");

                    StringBuilder sb = new StringBuilder();
                    sb.append("http://www.opencellid.org/cell/getInArea?key=")
                            .append(CellTracker.OCID_API_KEY).append("&BBOX=")
                            .append(boundParameter);

                    Log.i(TAG, mTAG + ":OCID MCC is set to: " + cell.getMCC());
                    if (cell.getMCC() != Integer.MAX_VALUE) {
                        sb.append("&mcc=").append(cell.getMCC());
                    }
                    Log.i(TAG, mTAG + ":OCID MNC is set to: " + cell.getMNC());
                    if (cell.getMNC() != Integer.MAX_VALUE) {
                        sb.append("&mnc=").append(cell.getMNC());
                    }
                    //Log.i(TAG, mTAG + ":OCID LAC is set to: " + cell.getLAC());
                    // We need DBe_import filtering, if we wanna keep these lines commented out...
                    //if (cell.getLAC() != Integer.MAX_VALUE) {
                    //    sb.append("&lac=").append(cell.getLAC());
                    //}

                    sb.append("&format=csv");
                    new RequestTask(context, type).execute(sb.toString());
                }
            } else {
                if(context instanceof MapViewerOsmDroid) {
                    ((MapViewerOsmDroid)context).setRefreshActionButtonState(false);
                }
                Helpers.sendMsg(context, context.getString(R.string.no_opencellid_key_detected));
            }
        } else {
            if(context instanceof MapViewerOsmDroid) {
                ((MapViewerOsmDroid)context).setRefreshActionButtonState(false);
            }
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

    // IT'S NEVER USED
//    /**
//     * Checks if the external media (SD Card) is writable
//     *
//     * @return boolean True if Writable
//     */
//    public static boolean isSdWritable() {
//
//        boolean mExternalStorageAvailable = false;
//        try {
//            String state = Environment.getExternalStorageState();
//
//            if (Environment.MEDIA_MOUNTED.equals(state)) {
//                // We can read and write the media
//                mExternalStorageAvailable = true;
//                Log.i(TAG, mTAG + ": External storage card is readable.");
//            } else {
//                mExternalStorageAvailable = false;
//            }
//        } catch (Exception ex) {
//            Log.e(TAG, mTAG + ":isSdWritable - " + ex.getMessage());
//        }
//        return mExternalStorageAvailable;
//    }

    /**
     * Return a String List representing response from invokeOemRilRequestRaw
     *
     * @param aob byte array response from invokeOemRilRequestRaw
     */
    public static List<String> unpackListOfStrings(byte aob[]) {

        if (aob.length == 0) {
            // WARNING: This one is very chatty!
            //Log.v(TAG, "invokeOemRilRequestRaw: string list response Length = 0");
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
            //Log.v(TAG, "invokeOemRilRequestRaw: byte-list response Length = 0");
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

    public static String getSystemProp(Context context, String prop, String def) {
        String result = null;
        try {
            result = SystemPropertiesReflection.get(context, prop);
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, mTAG + ": Failed to get system property: " + prop);
        }
        return result == null ? def : result;
    }

    // Use this when invoking from SU shell (not recommended!)
    public static String setProp(String prop, String value) {
        // We might wanna return the success of this. From mksh it is given by "$?" (0=ok, 1=fail)
        return CMDProcessor.runSuCommand("setprop " + prop + " " + value).getStdout();
    }

    /**
     * Use this to setprop using reflection of native android.os.SystemProperties class
     *      IllegalArgumentException if the key exceeds 32 characters
     *      IllegalArgumentException if the value exceeds 92 characters
     *
     * Generally speaking this cannot be done unless the app is running under system UID:
     *
     * " When you reflect the android.os.SystemProperties and make the call then you will
     *   make the request as the UID of the application and it will be rejected as the
     *   properties service has an ACL of allowed UIDs to write to particular key domains,
     *   see: /system/core/init/property_service.c "
     */
    /*  Something wrong here...
    public static String setSystemProp(Context context, String prop, String value, String def) {
        String result = null;
        try {
            result = SystemPropertiesReflection.set(context, prop, value);
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, mTAG + ": Failed to Set system property: " + prop);
        }
        return result == null ? def : result;
    }
    */


    /**
     * Checks device for SuperUser permission
     *
     * @return If SU was granted or denied
     */
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    public static boolean checkSu() {
        if (!new File("/system/bin/su").exists()
                && !new File("/system/xbin/su").exists()) {
            Log.e(TAG, mTAG + ": su binary does not exist!!!");
            return false; // tell caller to bail...
        }
        try {
            if (CMDProcessor.runSuCommand("ls /data/app-private").success()) {
                Log.i(TAG, mTAG + ": SU exists and we have permission");
                return true;
            } else {
                Log.i(TAG, mTAG + ": SU exists but we don't have permission");
                return false;
            }
        } catch (NullPointerException e) {
            Log.e(TAG, mTAG + ": NPE while looking for su binary: ", e);
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
            Log.e(TAG, mTAG + ": Busybox not in xbin or bin!");
            return false;
        }
        try {
            if (!CMDProcessor.runSuCommand("busybox mount").success()) {
                Log.e(TAG, mTAG + ": Busybox is there but is broken!");
                return false;
            }
        } catch (NullPointerException e) {
            Log.e(TAG, mTAG + ": NPE while testing Busybox: ", e);
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

    /**
     * Very cool method. Completely erases the entire database.
     * Apply on medical prescription.
     * Also asks the user, whether he wants to erase its database ...
     *
     * @param pContext Context of Activity
     */
    public static void askAndDeleteDb(final Context pContext) {
        AlertDialog lAlertDialog = new AlertDialog.Builder(pContext)
                .setNegativeButton(R.string.open_cell_id_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing
                    }
                })
                .setPositiveButton(R.string.open_cell_id_button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //FIXME need to remove hardcoded string into constants
                        pContext.deleteDatabase("aimsicd.db");
                    }
                })
                .setMessage(pContext.getString(R.string.clear_database_question))
                .setTitle(R.string.clear_database)
                .setCancelable(false)
                .setIcon(R.drawable.ic_action_delete_database).create();
        lAlertDialog.show();
    }
}
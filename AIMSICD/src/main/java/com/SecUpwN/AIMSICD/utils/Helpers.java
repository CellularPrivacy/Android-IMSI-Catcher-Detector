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
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import io.freefair.android.util.logging.AndroidLogger;
import io.freefair.android.util.logging.Logger;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.activities.MapViewerOsmDroid;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.service.CellTracker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
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
 * ChangeLog:
 *              2015-05-08   SecUpwN   Added Toast Extender for longer toasts
 *
 */
 public class Helpers {

    private static final Logger log = AndroidLogger.forClass(Helpers.class);
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
            log.error(e.getMessage(), e);
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
            if (!"NA".equals(CellTracker.OCID_API_KEY)) {
                double earthRadius = 6371.01; // [Km]
                int radius = 2; // Use a 2 Km radius with center at GPS location.

                if (Double.doubleToRawLongBits(cell.getLat()) != 0 &&
                        Double.doubleToRawLongBits(cell.getLon()) != 0) {
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

                    log.info("OCID BBOX is set to: " + boundParameter + "  with radius " + radius + " Km.");

                    StringBuilder sb = new StringBuilder();
                    sb.append("http://www.opencellid.org/cell/getInArea?key=")
                            .append(CellTracker.OCID_API_KEY).append("&BBOX=")
                            .append(boundParameter);

                    log.info("OCID MCC is set to: " + cell.getMCC());
                    if (cell.getMCC() != Integer.MAX_VALUE) {
                        sb.append("&mcc=").append(cell.getMCC());
                    }
                    log.info("OCID MNC is set to: " + cell.getMNC());
                    if (cell.getMNC() != Integer.MAX_VALUE) {
                        sb.append("&mnc=").append(cell.getMNC());
                    }

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

    /**
     * Return a String List representing response from invokeOemRilRequestRaw
     *
     * @param aob Byte array response from invokeOemRilRequestRaw
     */
    public static List<String> unpackByteListOfStrings(byte aob[]) {

        if (aob.length == 0) {
            // WARNING: This one is very chatty!
            log.verbose("invokeOemRilRequestRaw: byte-list response Length = 0");
            return Collections.emptyList();
        }
        int lines = aob.length / CHARS_PER_LINE;
        String[] display = new String[lines];

        for (int i = 0; i < lines; i++) {
            int offset, byteCount;
            offset = i * CHARS_PER_LINE + 2;
            byteCount = 0;

            if (offset + byteCount >= aob.length) {
                log.error("Unexpected EOF");
                break;
            }
            while (aob[offset + byteCount] != 0 && (byteCount < CHARS_PER_LINE)) {
                byteCount += 1;
                if (offset + byteCount >= aob.length) {
                    log.error("Unexpected EOF");
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
            log.error("Failed to get system property: " + prop, iae);
        }
        return result == null ? def : result;
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
     * Description:     Deletes the entire database by removing internal SQLite DB file
     *

     *
     * Dependencies:    Used in AIMSICD.java
     *
     * Notes:           See Android developer info: http://tinyurl.com/psz8vmt
     *
     *              WARNING!
     *              This deletes the entire database, thus any subsequent DB access will FC app.
     *              Therefore we need to either restart app or run AIMSICDDbAdapter, to rebuild DB.
     *              See: #581
     *
     *              In addition, since SQLite is kept in memory during lifetime of App, and
     *              is using Journaling, we have to restart app in order to clear old data
     *              already in memory.
     *
     * @param pContext Context of Activity
     */
    public static void askAndDeleteDb(final Context pContext) {
        AlertDialog lAlertDialog = new AlertDialog.Builder(pContext)
                .setNegativeButton(R.string.open_cell_id_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton(R.string.open_cell_id_button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Probably put in try/catch in case file removal fails...
                        pContext.stopService(new Intent(pContext, AimsicdService.class));
                        pContext.deleteDatabase("aimsicd.db");
                        new AIMSICDDbAdapter(pContext);
                        pContext.startService(new Intent(pContext, AimsicdService.class));
                        msgLong(pContext,pContext.getString(R.string.delete_database_msg_success));
                    }
                })
                .setMessage(pContext.getString(R.string.clear_database_question))
                .setTitle(R.string.clear_database)
                .setCancelable(false)
                .setIcon(R.drawable.ic_action_delete_database).create();
        lAlertDialog.show();
    }
}
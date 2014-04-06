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

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class AIMSICD extends Activity {

    private final String TAG = "AIMSICD";

    private Device mDevice;
    private final Context mContext = this;
    private Menu mMenu;

    //Back press to exit timer
    private long mLastPress = 0;

    //Notification ID
    private int mID = 1;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mDevice = new Device(mContext, this);

        TextView outputView = (TextView) findViewById(R.id.view);
        outputView.setHorizontalFadingEdgeEnabled(false);

        outputView.setText("Information:\n\n");

        if (mDevice.getPhoneID() == TelephonyManager.PHONE_TYPE_GSM) {
            outputView.append("SIM country:    " + mDevice.getSimCountry(false) + "\n");
            outputView.append("SIM Op ID:      " + mDevice.getSimOperator(false) + "\n");
            outputView.append("SIM Op Name:    " + mDevice.getSimOperatorName(false) + "\n");
            outputView.append("SIM IMSI:       " + mDevice.getSimSubs(false) + "\n");
            outputView.append("SIM serial:     " + mDevice.getSimSerial(false) + "\n\n");
        }

        int netID = mDevice.getNetID(true);
        outputView.append("Device type:    " + mDevice.getPhoneType(false) + "\n");
        outputView.append("Device IMEI:    " + mDevice.getIMEI(false) + "\n");
        outputView.append("Device version: " + mDevice.getIMEIv(false) + "\n");
        outputView.append("Device num:     " + mDevice.getPhoneNumber(false) + "\n\n");
        outputView.append("Network name:   " + mDevice.getNetworkName(false) + "\n");
        outputView.append("Network code:   " + mDevice.getSmmcMcc(false) + "\n");
        outputView.append("Network type:   " + mDevice.getNetworkTypeName() + "\n");
        outputView.append("Network LAC:    " + mDevice.getLAC(false) + "\n");
        outputView.append("Network CellID: " + mDevice.getCellId(false) + "\n\n");

        outputView.append("Data activity:  " + mDevice.getActivityDesc(netID) + "\n");
        outputView.append("Data status:    " + mDevice.getStateDesc(netID) + "\n");

        outputView.append("--------------------------------\n");
        outputView.append("[LAC,CID]|DAct|DStat|Net|Sig|Lat|Lng\n");
        Log.i(TAG, "**** AIMSICD ****");
        Log.i(TAG, "Device type   : " + mDevice.getPhoneType(false));
        Log.i(TAG, "Device IMEI   : " + mDevice.getIMEI(false));
        Log.i(TAG, "Device version: " + mDevice.getIMEIv(false));
        Log.i(TAG, "Device num    : " + mDevice.getPhoneNumber(false));
        Log.i(TAG, "Network type  : " + mDevice.getNetworkTypeName());
        Log.i(TAG, "Network CellID: " + mDevice.getCellId(false));
        Log.i(TAG, "Network LAC   : " + mDevice.getLAC(false));
        Log.i(TAG, "Network code  : " + mDevice.getSmmcMcc(false));
        Log.i(TAG, "Network name  : " + mDevice.getNetworkName(false));

        setNotification();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mTrackCell = menu.findItem(R.id.track_cell);
        MenuItem mTrackSignal = menu.findItem(R.id.track_signal);
        MenuItem mTrackLocation = menu.findItem(R.id.track_location);

        if (mDevice.isTrackingCell()) {
            mTrackCell.setTitle(R.string.track_cell);
            mTrackCell.setIcon(R.drawable.track_cell);
        } else {
            mTrackCell.setTitle(R.string.untrack_cell);
            mTrackCell.setIcon(R.drawable.untrack_cell);
        }

        if (mDevice.isTrackingSignal()) {
            mTrackSignal.setTitle(R.string.track_signal);
            mTrackSignal.setIcon(R.drawable.ic_action_network_cell);
        } else {
            mTrackSignal.setTitle(R.string.untrack_signal);
            mTrackSignal.setIcon(R.drawable.ic_action_network_cell_not_tracked);
        }
        if (mDevice.isTrackingLocation()) {
            mTrackLocation.setTitle(R.string.track_location);
            mTrackLocation.setIcon(R.drawable.ic_action_location_found);
        } else {
            mTrackLocation.setTitle(R.string.untrack_location);
            mTrackLocation.setIcon(R.drawable.ic_action_location_off);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.track_cell:
                mDevice.trackcell();
                if (Build.VERSION.SDK_INT > 11) {
                    onPrepareOptionsMenu(mMenu);
                }
                return true;
            case R.id.track_signal:
                mDevice.tracksignal();
                if (Build.VERSION.SDK_INT > 11) {
                    onPrepareOptionsMenu(mMenu);
                }
                return true;
            case R.id.track_location:
                mDevice.tracklocation();
                if (Build.VERSION.SDK_INT > 11) {
                    onPrepareOptionsMenu(mMenu);
                }
                return true;
            case R.id.show_map:
                showmap();
                return true;
            case R.id.export_database:
                mDevice.getDbHelper().exportDB();
                return true;
            case R.id.at_injector:
                Intent intent = new Intent(this, ATRilHook.class);
                startActivity(intent);
                return true;
            case R.id.app_exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Exit application if back pressed twice
     */
    @Override
    public void onBackPressed() {
        Toast onBackPressedToast = Toast.makeText(this, R.string.press_once_again_to_exit, Toast.LENGTH_SHORT);
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastPress > 5000) {
            onBackPressedToast.show();
            mLastPress = currentTime;
        } else {
            onBackPressedToast.cancel();
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public void onDestroy() {
        cancelNotification();
        super.onDestroy();
    }

    /**
     * Set or modify the Notification
     */
    private void setNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.iconbn)
                        .setContentTitle(mContext.getResources().getString(R.string.app_name))
                        .setContentText("Phone Type " + mDevice.getPhoneType(false))
                        .setOngoing(true)
                        .setAutoCancel(false);

        Intent notificationIntent = new Intent(this, AIMSICD.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 0);

        mBuilder.setContentIntent(contentIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mID, mBuilder.build());
    }

    private void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(mID);
        }
    }

    /**
     * Show the Map Viewer Activity
     */
    private final void showmap() {
        Intent myIntent = new Intent(this, MapViewer.class);
        startActivity(myIntent);
    }

    /**
     * Returns the device instance
     */
    public Device getDevice() {
        return mDevice;
    }

    /**
     * Receives a response from Location Services Settings if the user agreed to enable them.
     * If activity returns that the user made a valid selection then enable Location Tracking
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == mDevice.START_LOCATION_SERVICES) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                // Location Services were enabled attempt to enable Location Tracking
                mDevice.tracklocation();
            }
        }
    }

}

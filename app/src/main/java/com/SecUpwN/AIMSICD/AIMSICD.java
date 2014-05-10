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


import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.activities.MapViewer;
import com.SecUpwN.AIMSICD.activities.PrefActivity;
import com.SecUpwN.AIMSICD.fragments.AboutFragment;
import com.SecUpwN.AIMSICD.fragments.CellInfoFragment;
import com.SecUpwN.AIMSICD.fragments.DbViewerFragment;
import com.SecUpwN.AIMSICD.fragments.DeviceFragment;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.utils.GeoLocation;
import com.SecUpwN.AIMSICD.utils.Helpers;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

public class AIMSICD extends FragmentActivity {

    private final String TAG = "AIMSICD";

    private MyFragmentPagerAdapter mMyFragmentPagerAdapter;
    private ViewPager mViewPager;

    private final Context mContext = this;
    private boolean mBound;
    private SharedPreferences prefs;
    private Editor prefsEditor;
    private AIMSICDDbAdapter dbHelper;
    private String mDisclaimerAccepted;

    private AimsicdService mAimsicdService;

    //Back press to exit timer
    private long mLastPress = 0;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.top);

        // Bind to LocalService
        Intent intent = new Intent(this, AimsicdService.class);
        //Start Service before binding to keep it resident when activity is destroyed
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        mMyFragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager)findViewById(R.id.viewPager);
        mViewPager.setAdapter(mMyFragmentPagerAdapter);

        prefs = mContext.getSharedPreferences(
                AimsicdService.SHARED_PREFERENCES_BASENAME, 0);

        mDisclaimerAccepted = getResources().getString(R.string.disclaimer_accepted);

        if (!prefs.getBoolean(mDisclaimerAccepted, false)) {
            final AlertDialog.Builder disclaimer = new AlertDialog.Builder(this)
                    .setTitle(R.string.disclaimer_title)
                    .setMessage(R.string.disclaimer)
                    .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            prefsEditor = prefs.edit();
                            prefsEditor.putBoolean(mDisclaimerAccepted, true);
                            prefsEditor.commit();
                        }
                    })
                    .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            prefsEditor = prefs.edit();
                            prefsEditor.putBoolean(mDisclaimerAccepted, false);
                            prefsEditor.commit();
                            Uri packageUri = Uri.parse("package:com.SecUpwN.AIMSICD");
                            Intent uninstallIntent =
                                    new Intent(Intent.ACTION_DELETE, packageUri);
                            startActivity(uninstallIntent);
                            finish();
                            mAimsicdService.onDestroy();
                        }
                    });

            AlertDialog disclaimerAlert = disclaimer.create();
            disclaimerAlert.show();
        }

        //Create DB Instance
        dbHelper = new AIMSICDDbAdapter(mContext);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        final String KEY_KILL_SERVICE = mContext.getString(R.string.pref_persistservice_key);
        boolean persistService = prefs.getBoolean(KEY_KILL_SERVICE, true);
        if (!persistService) {
            Intent intent = new Intent(this, AimsicdService.class);
            stopService(intent);
        }
    }

    /**
     * Service Connection to bind the activity to the service
     */
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mAimsicdService = ((AimsicdService.AimscidBinder) service).getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG, "Service Disconnected");
            mBound = false;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mTrackCell = menu.findItem(R.id.track_cell);
        MenuItem mTrackLocation = menu.findItem(R.id.track_location);
        MenuItem mTrackFemtocell = menu.findItem(R.id.track_femtocell);

        if (mBound && mAimsicdService.isTrackingCell()) {
            mTrackCell.setTitle(R.string.untrack_cell);
            mTrackCell.setIcon(R.drawable.track_cell);
        } else {
            mTrackCell.setTitle(R.string.track_cell);
            mTrackCell.setIcon(R.drawable.untrack_cell);
        }

        if (mBound && mAimsicdService.isTrackingLocation()) {
            mTrackLocation.setTitle(R.string.untrack_location);
            mTrackLocation.setIcon(R.drawable.ic_action_location_found);
        } else {
            mTrackLocation.setTitle(R.string.track_location);
            mTrackLocation.setIcon(R.drawable.ic_action_location_off);
        }

        if (mBound && mAimsicdService.getPhoneID() == TelephonyManager.PHONE_TYPE_CDMA) {
            if (mBound && mAimsicdService.isTrackingFemtocell()) {
                mTrackFemtocell.setTitle(R.string.untrack_femtocell);
                mTrackFemtocell.setIcon(R.drawable.ic_action_network_cell);
            } else {
                mTrackFemtocell.setTitle(R.string.track_femtocell);
                mTrackFemtocell.setIcon(R.drawable.ic_action_network_cell_not_tracked);
            }
        } else {
            mTrackFemtocell.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.track_cell:
                trackcell();
                invalidateOptionsMenu();
                return true;
            case R.id.track_location:
                tracklocation();
                invalidateOptionsMenu();
                return true;
            case R.id.track_femtocell:
                trackFemtocell();
                invalidateOptionsMenu();
                return true;
            case R.id.show_map:
                showmap();
                return true;
            case R.id.preferences:
                intent = new Intent(this, PrefActivity.class);
                startActivity(intent);
                return true;
            case R.id.export_database:
                dbHelper.exportDB();
                return true;
            case R.id.update_opencelldata:
                double[] loc = mAimsicdService.getLastLocation();
                if (loc[0] != 0.0 && loc[1] != 0.0) {
                    getOpenCellData(loc[0], loc[1]);
                } else {
                    Helpers.sendMsg(mContext,
                            "Unable to determine your last location, enable Location Services (GPS) and try again.");
                }
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

    /**
     * Show the Map Viewer Activity
     */
    private void showmap() {
        Intent myIntent = new Intent(this, MapViewer.class);
        startActivity(myIntent);
    }

    /**
     * Cell Information Tracking - Enable/Disable
     */
    private void trackcell() {
        if (mAimsicdService.isTrackingCell()) {
            mAimsicdService.setCellTracking(false);
        } else {
            mAimsicdService.setCellTracking(true);
        }
    }

    /**
     * Location Information Tracking - Enable/Disable
     */
    private void tracklocation() {
        if (mAimsicdService.isTrackingLocation()) {
            mAimsicdService.setLocationTracking(false);
        } else {
            mAimsicdService.setLocationTracking(true);
        }
    }

    /**
     * FemtoCell Detection (CDMA Phones ONLY) - Enable/Disable
     */
    private void trackFemtocell() {
        if (mAimsicdService.isTrackingFemtocell()) {
            mAimsicdService.stopTrackingFemto();
        } else {
            mAimsicdService.startTrackingFemto();
        }
    }

    /**
     * Requests Cell data from OpenCellID.org, calculating a 100 mile bounding radius
     * and requesting all Cell ID information in that area.
     *
     * @param lat Latitude of current location
     * @param lng Longitude of current location
     */
    private void getOpenCellData(double lat, double lng) {
        if (isNetAvailable(this)) {
            double earthRadius = 6371.01;

            //New GeoLocation object to find bounding Coordinates
            GeoLocation currentLoc = GeoLocation.fromDegrees(lat, lng);

            //Calculate the Bounding Coordinates in a 50 mile radius
            //0 = min 1 = max
            GeoLocation[] boundingCoords = currentLoc.boundingCoordinates(100, earthRadius);
            String boundParameter;

            //Request OpenCellID data for Bounding Coordinates
            boundParameter = String.valueOf(boundingCoords[0].getLatitudeInDegrees()) + ","
                    + String.valueOf(boundingCoords[0].getLongitudeInDegrees()) + ","
                    + String.valueOf(boundingCoords[1].getLatitudeInDegrees()) + ","
                    + String .valueOf(boundingCoords[1].getLongitudeInDegrees());

            String urlString = "http://www.opencellid.org/cell/getInArea?key=24c66165-9748-4384-ab7c-172e3f533056"
                    + "&BBOX=" + boundParameter
                    + "&format=csv";

            new RequestTask().execute(urlString);
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.no_network_connection_title)
                    .setMessage(R.string.no_network_connection_message);
            builder.create().show();
        }
    }

    /**
     * Checks Network connectivity is available to download OpenCellID data
     *
     */
    private Boolean isNetAvailable(Context context)  {

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

    /**
     * Parses the downloaded CSV from OpenCellID and adds Map Marker to identify known
     * Cell ID's
     *
     * @param fileName Name of file downloaded from OpenCellID
     */
    private void parseOpenCellID (String fileName) {

        File file = new File(fileName);
        try {
            CSVReader csvReader = new CSVReader(new FileReader(file));
            List<String[]> csvCellID = csvReader.readAll();


            for (int i=1; i<csvCellID.size(); i++)
            {
                //Insert details into OpenCellID Database
                long result =
                        dbHelper.insertOpenCell(Double.parseDouble(csvCellID.get(i)[0]),
                                Double.parseDouble(csvCellID.get(i)[1]),
                                Integer.parseInt(csvCellID.get(i)[2]), Integer.parseInt(csvCellID.get(i)[3]),
                                Integer.parseInt(csvCellID.get(i)[4]), Integer.parseInt(csvCellID.get(i)[5]),
                                Integer.parseInt(csvCellID.get(i)[6]), Integer.parseInt(csvCellID.get(i)[7]));
                if (result == -1)
                {
                    Log.e(TAG, "Error inserting OpenCellID database value");
                }
            }


        } catch (Exception e) {
            Log.e (TAG, "Error parsing OpenCellID data - " + e.getMessage());
        }

    }

    /**
     * Runs the request to download OpenCellID data in an AsyncTask
     * preventing the application from becoming unresponsive whilst
     * waiting for a response and download from the server
     *
     */
    private class RequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //Do anything with response..
            if (result != null) {
                if (Helpers.isSdWritable()) {
                    try {
                        File dir = new File(
                                Environment.getExternalStorageDirectory() + "/AIMSICD/OpenCellID/");
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        Time today = new Time(Time.getCurrentTimezone());
                        today.setToNow();
                        String fileName = Environment.getExternalStorageDirectory()
                                + "/AIMSICD/OpenCellID/opencellid.csv";
                        File file = new File(dir, "opencellid.csv");

                        FileOutputStream fOut = new FileOutputStream(file);
                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                        myOutWriter.append(result);
                        myOutWriter.close();
                        fOut.close();
                        Helpers.sendMsg(mContext, "OpenCellID data successfully received");
                        parseOpenCellID(fileName);
                    } catch (Exception e) {
                        Log.e (TAG, "Write OpenCellID response - " + e.getMessage());
                    }
                }
            }
        }
    }


    class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;
        private List<String> titles;

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<Fragment>();
            titles = new ArrayList<String>();
            fragments.add(new DeviceFragment());
            titles.add(getString(R.string.device_info));
            fragments.add(new CellInfoFragment());
            titles.add(getString(R.string.cell_info_title));
            fragments.add(new DbViewerFragment());
            titles.add(getString(R.string.db_viewer));
            fragments.add(new AboutFragment());
            titles.add(getString(R.string.about_aimsicd));
        }
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }



            @Override
        public int getCount() {
            return fragments.size();
        }
    }
}

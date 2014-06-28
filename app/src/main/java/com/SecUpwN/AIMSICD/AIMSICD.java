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

import com.SecUpwN.AIMSICD.activities.MapViewer;
import com.SecUpwN.AIMSICD.activities.PrefActivity;
import com.SecUpwN.AIMSICD.fragments.AboutFragment;
import com.SecUpwN.AIMSICD.fragments.AtCommandFragment;
import com.SecUpwN.AIMSICD.fragments.CellInfoFragment;
import com.SecUpwN.AIMSICD.fragments.DbViewerFragment;
import com.SecUpwN.AIMSICD.fragments.DeviceFragment;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.utils.Helpers;
import com.SecUpwN.AIMSICD.utils.RequestTask;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AIMSICD extends Activity {

    private final String TAG = "AIMSICD";

    private final Context mContext = this;
    private boolean mBound;
    private SharedPreferences prefs;
    private Editor prefsEditor;
    private String mDisclaimerAccepted;
    private AimsicdService mAimsicdService;
    private String[] mNavigationTitles;

    private DrawerLayout mDrawerLayout;
    private ActionBar mActionBar;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    public static ProgressBar mProgressBar;

    //Back press to exit timer
    private long mLastPress = 0;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Bind to LocalService
        Intent intent = new Intent(this, AimsicdService.class);
        //Start Service before binding to keep it resident when activity is destroyed
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mActionBar = getActionBar();

        mNavigationTitles = getResources().getStringArray(R.array.navigation_array);
        mTitle = mDrawerTitle = getTitle();

        //Declare Drawer Icons
        int[] mIcons = new int[]{
                R.drawable.ic_action_phone,
                R.drawable.cell_tower,
                R.drawable.ic_action_computer,
                R.drawable.ic_action_storage,
                R.drawable.ic_action_map,
                R.drawable.ic_action_about,
        };



        List<HashMap<String, String>> navigationItems = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            HashMap<String, String> hm = new HashMap<>();
            hm.put("title", mNavigationTitles[i]);
            hm.put("icon", Integer.toString(mIcons[i]));
            navigationItems.add(hm);
        }

        String[] from = {"title", "icon"};
        int[] to = {R.id.navigation_item, R.id.icon};

        // Set the adapter for the list view
        mDrawerList.setAdapter(new SimpleAdapter(mContext,
                navigationItems, R.layout.drawer_layout, from, to));
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                mActionBar.setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mActionBar.setTitle(mDrawerTitle);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);

        //Display the Device Fragment as the Default View
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, new DeviceFragment())
                .commit();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

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

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        final String PERSIST_SERVICE = mContext.getString(R.string.pref_persistservice_key);
        boolean persistService = prefs.getBoolean(PERSIST_SERVICE, false);
        if (!persistService) {
            Intent intent = new Intent(mContext, AimsicdService.class);
            stopService(intent);
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        // Create a new fragment
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new DeviceFragment();
                break;
            case 1:
                fragment = new CellInfoFragment();
                break;
            case 2:
                fragment = new AtCommandFragment();
                break;
            case 3:
                fragment = new DbViewerFragment();
                break;
            case 5:
                fragment = new AboutFragment();
                break;
            default:
                fragment = new DeviceFragment();
        }

        if (position == 4) {
            showmap();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mNavigationTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        mActionBar.setTitle(mTitle);
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
            mAimsicdService.setNotification();
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
        if (!mBound) {
            // Bind to LocalService
            Intent intent = new Intent(this, AimsicdService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
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
        MenuItem mTrackFemtocell = menu.findItem(R.id.track_femtocell);

        if (mBound) {
            if (mAimsicdService.isTrackingCell()) {
                if (mTrackCell != null) {
                    mTrackCell.setTitle(R.string.untrack_cell);
                    mTrackCell.setIcon(R.drawable.track_cell);
                }
            } else {
                if (mTrackCell != null) {
                    mTrackCell.setTitle(R.string.track_cell);
                    mTrackCell.setIcon(R.drawable.untrack_cell);
                }
            }

            if (mAimsicdService.mDevice.getPhoneID() == TelephonyManager.PHONE_TYPE_CDMA) {
                if (mAimsicdService.isTrackingFemtocell()) {
                    if (mTrackFemtocell != null) {
                        mTrackFemtocell.setTitle(R.string.untrack_femtocell);
                        mTrackFemtocell.setIcon(R.drawable.ic_action_network_cell);
                    }
                } else {
                    if (mTrackFemtocell != null) {
                        mTrackFemtocell.setTitle(R.string.track_femtocell);
                        mTrackFemtocell.setIcon(R.drawable.ic_action_network_cell_not_tracked);
                    }
                }
            } else {
                if (mTrackFemtocell != null) {
                    mTrackFemtocell.setVisible(false);
                }
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle item selection
        Intent intent;
        switch (item.getItemId()) {
            case R.id.track_cell:
                trackcell();
                invalidateOptionsMenu();
                return true;
            case R.id.track_femtocell:
                trackFemtocell();
                invalidateOptionsMenu();
                return true;
            case R.id.preferences:
                intent = new Intent(this, PrefActivity.class);
                startActivity(intent);
                return true;
            case R.id.backup_database:
                new RequestTask(mContext, RequestTask.BACKUP_DATABASE).execute();
                return true;
            case R.id.restore_database:
                new RequestTask(mContext, RequestTask.RESTORE_DATABASE).execute();
                return true;
            case R.id.update_opencelldata:
                Location loc = mAimsicdService.lastKnownLocation();
                if (loc != null && loc.hasAccuracy()) {
                    Helpers.sendMsg(mContext, "Contacting OpenCellID.org for data...");
                    Helpers.getOpenCellData(mContext, loc.getLatitude(), loc.getLongitude(),
                            RequestTask.OPEN_CELL_ID_REQUEST);
                } else {
                    Helpers.sendMsg(mContext,
                            "Unable to determine your last location. \nEnable Location Services and try again.");
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
        Toast onBackPressedToast = Toast
                .makeText(this, R.string.press_once_again_to_exit, Toast.LENGTH_SHORT);
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
     * FemtoCell Detection (CDMA Phones ONLY) - Enable/Disable
     */
    private void trackFemtocell() {
        if (mAimsicdService.isTrackingFemtocell()) {
            mAimsicdService.stopTrackingFemto();
        } else {
            mAimsicdService.startTrackingFemto();
        }
    }
}

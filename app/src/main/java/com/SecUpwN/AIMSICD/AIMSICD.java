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

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.SecUpwN.AIMSICD.activities.BaseActivity;
import com.SecUpwN.AIMSICD.activities.DebugLogs;
import com.SecUpwN.AIMSICD.activities.MapViewerOsmDroid;
import com.SecUpwN.AIMSICD.activities.PrefActivity;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.adapters.DrawerMenuAdapter;
import com.SecUpwN.AIMSICD.drawer.DrawerMenuActivityConfiguration;
import com.SecUpwN.AIMSICD.drawer.DrawerMenuItem;
import com.SecUpwN.AIMSICD.drawer.DrawerMenuSection;
import com.SecUpwN.AIMSICD.drawer.NavDrawerItem;
import com.SecUpwN.AIMSICD.fragments.AboutFragment;
import com.SecUpwN.AIMSICD.fragments.AtCommandFragment;
import com.SecUpwN.AIMSICD.fragments.DetailsContainerFragment;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.service.CellTracker;
import com.SecUpwN.AIMSICD.service.SignalStrengthTracker;
import com.SecUpwN.AIMSICD.utils.AsyncResponse;
import com.SecUpwN.AIMSICD.utils.Cell;
import com.SecUpwN.AIMSICD.utils.GeoLocation;
import com.SecUpwN.AIMSICD.utils.Helpers;
import com.SecUpwN.AIMSICD.utils.Icon;
import com.SecUpwN.AIMSICD.utils.LocationServices;
import com.SecUpwN.AIMSICD.utils.RequestTask;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Description:     TODO: Please add some comments about this class
 *
 * Dependencies:    TODO: Write a few words about where the content of this is used.
 *
 * Issues:
 *
 * ChangeLog:
 *
 */
public class AIMSICD extends BaseActivity implements AsyncResponse {

    private final String TAG = "AIMSICD";

    private final Context mContext = this;
    private boolean mBound;
    private SharedPreferences prefs;
    private Editor prefsEditor;
    private String mDisclaimerAccepted;
    private AimsicdService mAimsicdService;

    private DrawerLayout mDrawerLayout;
    private ActionBar mActionBar;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    public static ProgressBar mProgressBar;

    //Back press to exit timer
    private long mLastPress = 0;

    private DrawerMenuActivityConfiguration mNavConf ;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        mNavConf = getNavDrawerConfiguration();

        setContentView(mNavConf.getMainLayout());

        mDrawerLayout = (DrawerLayout) findViewById(mNavConf.getDrawerLayoutId());
        mDrawerList = (ListView) findViewById(mNavConf.getLeftDrawerId());
        mActionBar = getActionBar();
        mTitle = mDrawerTitle = getTitle();

        mDrawerList.setAdapter(mNavConf.getBaseAdapter());

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
                invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mActionBar.setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        prefs = mContext.getSharedPreferences( AimsicdService.SHARED_PREFERENCES_BASENAME, 0);

        mDisclaimerAccepted = getResources().getString(R.string.disclaimer_accepted);

        if (!prefs.getBoolean(mDisclaimerAccepted, false)) {
            final AlertDialog.Builder disclaimer = new AlertDialog.Builder(this)
                    .setTitle(R.string.disclaimer_title)
                    .setMessage(R.string.disclaimer)
                    .setPositiveButton(R.string.text_agree, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            prefsEditor = prefs.edit();
                            prefsEditor.putBoolean(mDisclaimerAccepted, true);
                            prefsEditor.apply();
                            startService();
                        }
                    })
                    .setNegativeButton(R.string.text_disagree, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            prefsEditor = prefs.edit();
                            prefsEditor.putBoolean(mDisclaimerAccepted, false);
                            prefsEditor.apply();
                            Uri packageUri = Uri.parse("package:com.SecUpwN.AIMSICD");
                            Intent uninstallIntent =
                                    new Intent(Intent.ACTION_DELETE, packageUri);
                            startActivity(uninstallIntent);
                            finish();
                            if (mAimsicdService != null) mAimsicdService.onDestroy();
                        }
                    });

            AlertDialog disclaimerAlert = disclaimer.create();
            disclaimerAlert.show();
        } else {
            startService();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        final String iconType = prefs.getString(mContext.getString(R.string.pref_ui_icons_key), "SENSE").toUpperCase();
        mActionBar.setIcon(Icon.getIcon(Icon.Type.valueOf(iconType)));
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
            stopService(new Intent(mContext, AimsicdService.class));
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mDrawerLayout.closeDrawer(mDrawerList);
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    void selectItem(int position) {
        NavDrawerItem selectedItem = mNavConf.getNavItems().get(position);

        /**
         * This is a work-around for Issue 42601
         * https://code.google.com/p/android/issues/detail?id=42601
         *
         * The method getChildFragmentManager() does not clear up
         * when the Fragment is detached.
         */
        DetailsContainerFragment mDetailsFrag = new DetailsContainerFragment();

        // Create a new fragment
        switch (selectedItem.getId()) {
            case 101:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, mDetailsFrag).commit();
                mDetailsFrag.setCurrentPage(0);
                break;
            case 102:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, mDetailsFrag).commit();
                mDetailsFrag.setCurrentPage(1);
                break;
            case 103:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, new AtCommandFragment()).commit();
                break;
            case 104:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, mDetailsFrag).commit();
                mDetailsFrag.setCurrentPage(2);
                break;
            case 303:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, new AboutFragment()).commit();
                break;
            case 306:
                // Request uploading here?
                new RequestTask(mContext, com.SecUpwN.AIMSICD.utils.RequestTask.DBE_UPLOAD_REQUEST).execute(""); // no string needed for csv based upload
                break;
        }

        if (selectedItem.getId() == 901) {
            monitorcell();
        } else if (selectedItem.getId() == 902) {
            trackcell();
        } else if (selectedItem.getId() == 903) {
            trackFemtocell();
        } else if (selectedItem.getId() == 105) {
            showmap();
        } else if (selectedItem.getId() == 202) {
            Intent intent = new Intent(this, PrefActivity.class);
            startActivity(intent);
        } else if (selectedItem.getId() == 203) {
            new RequestTask(mContext, RequestTask.BACKUP_DATABASE).execute();
        } else if (selectedItem.getId() == 204) {
            if (CellTracker.LAST_DB_BACKUP_VERSION < AIMSICDDbAdapter.DATABASE_VERSION) {
                Helpers.msgLong(mContext, "Unable to restore backup from previous database version"
                        + " due to structural changes!");
            } else {
                new RequestTask(mContext, RequestTask.RESTORE_DATABASE).execute();
            }
        } else if (selectedItem.getId() == 301) {
            if (CellTracker.OCID_API_KEY != null && !CellTracker.OCID_API_KEY.equals("NA")) {
                GeoLocation loc = mAimsicdService.lastKnownLocation();
                if (loc != null) {
                    Helpers.msgLong(mContext, "Contacting opencellid.org for data...\n"
                            + "This may take up to a minute.");
                    Cell cell = new Cell();
                    cell.setLon(loc.getLongitudeInDegrees());
                    cell.setLat(loc.getLatitudeInDegrees());
                    Helpers.getOpenCellData(mContext, cell, RequestTask.DBE_DOWNLOAD_REQUEST);
                } else {
                    Helpers.msgShort(mContext, "Waiting for location...");

                    // TODO: Is this implemented?? --E:V:A (2015-01-22)
                    //Attempt to find location through CID
                    //CID Location Async Output Delegate Interface Implementation
                    LocationServices.LocationAsync locationAsync
                            = new LocationServices.LocationAsync();
                    locationAsync.delegate = this;
                    locationAsync.execute(
                            mAimsicdService.getCell().getCID(),
                            mAimsicdService.getCell().getLAC(),
                            mAimsicdService.getCell().getMNC(),
                            mAimsicdService.getCell().getMCC());
                }
            } else {
                Helpers.sendMsg(mContext,
                      "No OpenCellID API Key detected! \nPlease enter your key in settings first.");
            }
        } else if (selectedItem.getId() == 302) {
            if (CellTracker.OCID_API_KEY != null && !CellTracker.OCID_API_KEY.equals("NA")) {
                Cell.CellLookUpAsync cellLookUpAsync = new Cell.CellLookUpAsync();
                cellLookUpAsync.delegate = this;
                StringBuilder sb = new StringBuilder();
                sb.append("http://www.opencellid.org/cell/get?key=").append(CellTracker.OCID_API_KEY);

                if (mAimsicdService.getCell().getMCC() != Integer.MAX_VALUE) {
                    sb.append("&mcc=").append(mAimsicdService.getCell().getMCC());
                }

                if (mAimsicdService.getCell().getMNC() != Integer.MAX_VALUE) {
                    sb.append("&mnc=").append(mAimsicdService.getCell().getMNC());
                }

                if (mAimsicdService.getCell().getLAC() != Integer.MAX_VALUE) {
                    sb.append("&lac=").append(mAimsicdService.getCell().getLAC());
                }

                if (mAimsicdService.getCell().getCID() != Integer.MAX_VALUE) {
                    sb.append("&cellid=").append(mAimsicdService.getCell().getCID());
                }

                sb.append("&format=xml");
                cellLookUpAsync.execute(sb.toString());
            } else {
                Helpers.sendMsg(mContext,
                        "No OpenCellID API Key detected! \nPlease enter your key in settings first.");
            }
        } else if (selectedItem.getId() == 305) {
            Intent i = new Intent(this, DebugLogs.class);
            startActivity(i);
        } else if (selectedItem.getId() == 304) {
            finish();
        }

        mDrawerList.setItemChecked(position, true);

        if ( selectedItem.updateActionBarTitle()) {
            setTitle(selectedItem.getLabel());
        }

        if ( this.mDrawerLayout.isDrawerOpen(this.mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    @Override
    public void processFinish(float[] location) {
        Log.i(TAG, "processFinish - location[0]=" + location[0] + " location[1]=" + location[1]);
        if (location[0] != 0.0f && location[1] != 0.0f) {
            Helpers.msgLong(mContext, "Contacting opencellid.org for data...\n"
                    + "This may take up to a minute.");
            Helpers.getOpenCellData(mContext, mAimsicdService.getCell(), RequestTask.DBE_DOWNLOAD_REQUEST);
        } else {
            Helpers.msgLong(mContext,
                    "Unable to determine your last location.\nEnable Location Services and try again.");
        }
    }

    @Override
    public void processFinish(List<Cell> cells) {
        if (cells != null) {
            if (!cells.isEmpty()) {
                for (Cell cell : cells) {
                    Log.i(TAG, "processFinish - Cell =" + cell.toString());
                    if (cell.isValid()) {
                        mAimsicdService.setCell(cell);
                        Intent intent = new Intent(AimsicdService.UPDATE_DISPLAY);
                        intent.putExtra("update", true);
                        mContext.sendBroadcast(intent);
                    }
                }
            }
        }
    }


    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        mActionBar.setTitle(mTitle);
    }

    public DrawerMenuActivityConfiguration getNavDrawerConfiguration() {

        List<NavDrawerItem> menu = new ArrayList<>();

        menu.add(DrawerMenuSection.create(900, "Tracking"));
        // TODO: Clarify names and usage...
        menu.add(DrawerMenuItem.create(901, getString(R.string.monitor_cell), "untrack_cell", false, this));    // Toggle "AIMSICD Monitoring"
        menu.add(DrawerMenuItem.create(902, getString(R.string.track_cell), "untrack_cell", false, this));      // Toggle "Track Cell Details"
        if (CellTracker.PHONE_TYPE == TelephonyManager.PHONE_TYPE_CDMA) {
            menu.add(DrawerMenuItem.create(903, getString(R.string.track_femtocell), "ic_action_network_cell", false, this)); // Track FemtoCell
        }
        menu.add(DrawerMenuSection.create(100, "Main"));
        menu.add(DrawerMenuItem.create(101, getString(R.string.device_info), "ic_action_phone", true, this));           // Phone/SIM Details
        menu.add(DrawerMenuItem.create(102, getString(R.string.cell_info_title), "cell_tower", true, this));            // Cell Information (Neighboring cells etc)
        menu.add(DrawerMenuItem.create(103, getString(R.string.at_command_title), "ic_action_computer", true, this));   // AT Command Interface
        menu.add(DrawerMenuItem.create(104, getString(R.string.db_viewer), "ic_action_storage", true, this));           // Database Viewer
        menu.add(DrawerMenuItem.create(105, getString(R.string.map_view), "ic_action_map", false, this));               // Antenna Map Viewer

        menu.add(DrawerMenuSection.create(200, "Settings"));
        menu.add(DrawerMenuItem.create(202, getString(R.string.preferences), "ic_action_settings", false, this));            // Preferences
        menu.add(DrawerMenuItem.create(203, getString(R.string.backup_database), "ic_action_import_export", false, this));   // Backup Database
        menu.add(DrawerMenuItem.create(204, getString(R.string.restore_database), "ic_action_import_export", false, this));  // Restore Database
        // TODO:  menu.add(DrawerMenuItem.create(205?, getString(R.string.reset_database), "ic_action_clear", false, this)); // "Reset/Clear DataBase"

        menu.add(DrawerMenuSection.create(300, "Application"));
        menu.add(DrawerMenuItem.create(301, getString(R.string.get_opencellid), "stat_sys_download_anim0", false, this));   // "Download Local BTS data"
        // TODO: Upload DBi_measure + DBi_bts to OCID or MLS
        menu.add(DrawerMenuItem.create(306, getString(R.string.upload_bts), "stat_sys_upload_anim0", false, this));      // "Upload Local BTS data"
        menu.add(DrawerMenuItem.create(302, getString(R.string.cell_lookup), "stat_sys_download_anim0", false, this));  // Lookup "All Current Cell Details (ACD)"
        menu.add(DrawerMenuItem.create(303, getString(R.string.about_aimsicd), "ic_action_about", true, this));         // About
        menu.add(DrawerMenuItem.create(305, getString(R.string.send_logs), "ic_action_computer", false, this));         // Debugging
        menu.add(DrawerMenuItem.create(304, getString(R.string.quit), "ic_action_remove", false, this));                // Quit

        DrawerMenuActivityConfiguration navDrawerActivityConfiguration = new DrawerMenuActivityConfiguration();
        navDrawerActivityConfiguration.setMainLayout(R.layout.main);
        navDrawerActivityConfiguration.setDrawerLayoutId(R.id.drawer_layout);
        navDrawerActivityConfiguration.setLeftDrawerId(R.id.left_drawer);
        navDrawerActivityConfiguration.setNavItems(menu);
        navDrawerActivityConfiguration.setDrawerOpenDesc(R.string.drawer_open);
        navDrawerActivityConfiguration.setDrawerCloseDesc(R.string.drawer_close);
        navDrawerActivityConfiguration.setBaseAdapter( new DrawerMenuAdapter(this, R.layout.drawer_item, menu ));
        return navDrawerActivityConfiguration;
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

            //If tracking cell details check location services are still enabled
            if (mAimsicdService.isTrackingCell()) {
                mAimsicdService.checkLocationServices();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG, "Service Disconnected");
            mBound = false;
        }
    };

    private void startService() {
        // don't start service if disclaimer is not accepted
        if (!prefs.getBoolean(mDisclaimerAccepted, false)) return;

        if (!mBound) {
            // Bind to LocalService
            Intent intent = new Intent(AIMSICD.this, AimsicdService.class);
            //Start Service before binding to keep it resident when activity is destroyed
            startService(intent);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

            //Display the Device Fragment as the Default View
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new DetailsContainerFragment())
                    .commit();
        }
    }

    /**
     * Triggered when GUI is opened
     */
    @Override
    public void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        startService();
    }


    /**
     * Triggered when GUI is closed/put to background
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if ( mNavConf.getActionMenuItemsToHideWhenDrawerOpen() != null ) {
            boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
            for( int iItem : mNavConf.getActionMenuItemsToHideWhenDrawerOpen()) {
                menu.findItem(iItem).setVisible(!drawerOpen);
            }
        }

        NavDrawerItem femtoTrackingItem = null;

        List<NavDrawerItem> menuItems = mNavConf.getNavItems();
        NavDrawerItem cellMonitoringItem = menuItems.get(1);
        NavDrawerItem cellTrackingItem = menuItems.get(2);
        if (CellTracker.PHONE_TYPE == TelephonyManager.PHONE_TYPE_CDMA) {
            femtoTrackingItem = menuItems.get(3);
        }

        if (mBound) {
            if (cellMonitoringItem != null) {
                if (mAimsicdService.isMonitoringCell()) {
                    cellMonitoringItem.setLabel(getString(R.string.unmonitor_cell));
                    cellMonitoringItem.setIcon(R.drawable.track_cell);
                } else {
                    cellMonitoringItem.setLabel(getString(R.string.monitor_cell));
                    cellMonitoringItem.setIcon(R.drawable.untrack_cell);
                }
                mNavConf.getBaseAdapter().notifyDataSetChanged();
            }
            if (cellTrackingItem != null) {
                if (mAimsicdService.isTrackingCell()) {
                    cellTrackingItem.setLabel(getString(R.string.untrack_cell));
                    cellTrackingItem.setIcon(R.drawable.track_cell);
                } else {
                    cellTrackingItem.setLabel(getString(R.string.track_cell));
                    cellTrackingItem.setIcon(R.drawable.untrack_cell);
                }
                mNavConf.getBaseAdapter().notifyDataSetChanged();
            }

            if (femtoTrackingItem != null) {
                if (mAimsicdService.isTrackingFemtocell()) {
                    femtoTrackingItem.setLabel(getString(R.string.untrack_femtocell));
                    femtoTrackingItem.setIcon(R.drawable.ic_action_network_cell);
                } else {
                    femtoTrackingItem.setLabel(getString(R.string.track_femtocell));
                    femtoTrackingItem.setIcon(R.drawable.ic_action_network_cell_not_tracked);
                }
                mNavConf.getBaseAdapter().notifyDataSetChanged();
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Exit application if Back button is pressed twice
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
        Intent myIntent = new Intent(this, MapViewerOsmDroid.class);
        startActivity(myIntent);
    }

    /**
     * Cell Information Tracking - Enable/Disable
     *
     * TODO: Clarify usage and what functions we would like this to provide.
     *  - Are we toggling GPS location tracking?
     *  - Are we logging measurement data into DBi?
     *  - Are we locking phone to 2/3/4G operation?
     *
     */
    private void trackcell() {
        if (mAimsicdService.isTrackingCell()) {
            mAimsicdService.setCellTracking(false);
        } else {
            mAimsicdService.setCellTracking(true);
        }
    }

    /**
     * Cell Information Monitoring - Enable/Disable
     *
     * TODO: Clarify usage and what functions we would like this to provide.
     * - Are we temporarily disabling AIMSICD monitoring? (IF yes, why not just Quit?)
     * - Are we ignoring Detection alarms?
     * - Are we logging something?
     *
     */
    // TODO: Wrong Spelling, should be "monitorcell"
    private void monitorcell() {
        if (mAimsicdService.isMonitoringCell()) {
            mAimsicdService.setCellMonitoring(false);
        } else {
            mAimsicdService.setCellMonitoring(true);
        }
    }

    /**
     * FemtoCell Detection (CDMA Phones ONLY) - Enable/Disable
     */
    private void trackFemtocell() {
        if (mAimsicdService.isTrackingFemtocell()) {
            mAimsicdService.setTrackingFemtocell(false);
        } else {
            mAimsicdService.setTrackingFemtocell(true);
        }
    }

}
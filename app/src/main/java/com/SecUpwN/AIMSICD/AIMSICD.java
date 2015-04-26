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
import com.SecUpwN.AIMSICD.constants.DrawerMenu;
import com.SecUpwN.AIMSICD.drawer.DrawerMenuActivityConfiguration;
import com.SecUpwN.AIMSICD.drawer.DrawerMenuItem;
import com.SecUpwN.AIMSICD.drawer.DrawerMenuSection;
import com.SecUpwN.AIMSICD.drawer.NavDrawerItem;
import com.SecUpwN.AIMSICD.fragments.AboutFragment;
import com.SecUpwN.AIMSICD.fragments.AtCommandFragment;
import com.SecUpwN.AIMSICD.fragments.DetailsContainerFragment;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.service.CellTracker;
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
        String title = selectedItem.getLabel();

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
            case DrawerMenu.ID.MAIN.PHONE_SIM_DETAILS:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, mDetailsFrag).commit();
                mDetailsFrag.setCurrentPage(0);
                // exception: title here does not match nav drawer label
                title = getString(R.string.app_name_short);
                break;
            case DrawerMenu.ID.MAIN.CURRENT_TREAT_LEVEL:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, mDetailsFrag).commit();
                mDetailsFrag.setCurrentPage(1);
                // exception: title here does not match nav drawer label
                title = getString(R.string.app_name_short);
                break;
            case DrawerMenu.ID.MAIN.AT_COMMAND_INTERFACE:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, new AtCommandFragment()).commit();
                break;
            case DrawerMenu.ID.MAIN.DB_VIEWER:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, mDetailsFrag).commit();
                mDetailsFrag.setCurrentPage(2);
                // exception: title here does not match nav drawer label
                title = getString(R.string.app_name_short);
                break;
            case DrawerMenu.ID.APPLICATION.ABOUT:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, new AboutFragment()).commit();
                break;
            case DrawerMenu.ID.APPLICATION.UPLOAD_LOCAL_BST_DATA:
                // Request uploading here?
                new RequestTask(mContext, com.SecUpwN.AIMSICD.utils.RequestTask.DBE_UPLOAD_REQUEST).execute(""); // no string needed for csv based upload
                break;
        }

        if (selectedItem.getId() == DrawerMenu.ID.TRACKING.TOGGLE_AIMSICD_MONITORING) {
            monitorcell();
        } else if (selectedItem.getId() == DrawerMenu.ID.TRACKING.TOGGLE_2G_ONLY_NETWORK) {
            trackcell();
        } else if (selectedItem.getId() == DrawerMenu.ID.TRACKING.TRACK_FEMTOCELL) {
            trackFemtocell();
        } else if (selectedItem.getId() == DrawerMenu.ID.MAIN.ANTENNA_MAP_VIEW) {
            showmap();
        } else if (selectedItem.getId() == DrawerMenu.ID.SETTINGS.PREFERENCES) {
            Intent intent = new Intent(this, PrefActivity.class);
            startActivity(intent);
        } else if (selectedItem.getId() == DrawerMenu.ID.SETTINGS.BACKUP_DB) {
            new RequestTask(mContext, RequestTask.BACKUP_DATABASE).execute();
        } else if (selectedItem.getId() == DrawerMenu.ID.SETTINGS.RESTORE_DB) {
            if (CellTracker.LAST_DB_BACKUP_VERSION < AIMSICDDbAdapter.DATABASE_VERSION) {
                Helpers.msgLong(mContext, getString(R.string.unable_to_restore_backup_from_previous_database_version));
            } else {
                new RequestTask(mContext, RequestTask.RESTORE_DATABASE).execute();
            }
        } else if (selectedItem.getId() == DrawerMenu.ID.APPLICATION.DOWNLOAD_LOCAL_BST_DATA) {
            if (CellTracker.OCID_API_KEY != null && !CellTracker.OCID_API_KEY.equals("NA")) {
                GeoLocation loc = mAimsicdService.lastKnownLocation();
                if (loc != null) {
                    Helpers.msgLong(mContext, mContext.getString(R.string.contacting_opencellid_for_data)
                            + getString(R.string.this_might_take_a_minute));
                    Cell cell = new Cell();
                    cell.setLon(loc.getLongitudeInDegrees());
                    cell.setLat(loc.getLatitudeInDegrees());
                    Helpers.getOpenCellData(mContext, cell, RequestTask.DBE_DOWNLOAD_REQUEST);
                } else {
                    Helpers.msgShort(mContext, getString(R.string.waiting_for_location));

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
                Helpers.sendMsg(mContext, mContext.getString(R.string.no_opencellid_key_detected));
            }
        } else if (selectedItem.getId() == DrawerMenu.ID.MAIN.ACD) {
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
                Helpers.sendMsg(mContext, mContext.getString(R.string.no_opencellid_key_detected));
            }
        } else if (selectedItem.getId() == DrawerMenu.ID.APPLICATION.SEND_DEBUGGING_LOG) {
            Intent i = new Intent(this, DebugLogs.class);
            startActivity(i);
        } else if (selectedItem.getId() == DrawerMenu.ID.APPLICATION.QUIT) {
            finish();
        }

        mDrawerList.setItemChecked(position, true);

        if ( selectedItem.updateActionBarTitle()) {
            setTitle(title);
        }

        if ( this.mDrawerLayout.isDrawerOpen(this.mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    @Override
    public void processFinish(float[] location) {
        Log.i(TAG, "processFinish - location[0]=" + location[0] + " location[1]=" + location[1]);
        if (location[0] != 0.0f && location[1] != 0.0f) {
            Helpers.msgLong(mContext, mContext.getString(R.string.contacting_opencellid_for_data));
            Helpers.getOpenCellData(mContext, mAimsicdService.getCell(), RequestTask.DBE_DOWNLOAD_REQUEST);
        } else {
            Helpers.msgLong(mContext, mContext.getString(R.string.unable_to_determine_last_location));
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

        //Section Main
        menu.add(DrawerMenuSection.create(DrawerMenu.ID.SECTION_MAIN, getString(R.string.main))); // Changed 100 --> DrawerMenu.ID.SECTION_MAIN
        // Changed 102 --> DrawerMenu.ID.MAIN.CURRENT_TREAT_LEVEL
        menu.add(DrawerMenuItem.create(DrawerMenu.ID.MAIN.CURRENT_TREAT_LEVEL, getString(R.string.cell_info_title), R.drawable.cell_tower, true));            // Cell Information (Neighboring cells etc)
        // Changed 101 --> DrawerMenu.ID.MAIN.PHONE_SIM_DETAILS
        menu.add(DrawerMenuItem.create(DrawerMenu.ID.MAIN.PHONE_SIM_DETAILS, getString(R.string.device_info), R.drawable.ic_action_phone, true));           // Phone/SIM Details
        // Changed 302 --> DrawerMenu.ID.MAIN.ACD
        menu.add(DrawerMenuItem.create(DrawerMenu.ID.MAIN.ACD, getString(R.string.cell_lookup), R.drawable.stat_sys_download_anim0, false));  // Lookup "All Current Cell Details (ACD)"
        // Changed 104 --> DrawerMenu.ID.MAIN.DB_VIEWER
        menu.add(DrawerMenuItem.create(DrawerMenu.ID.MAIN.DB_VIEWER, getString(R.string.db_viewer), R.drawable.ic_action_storage, true));           // Database Viewer
        // Changed 105 --> DrawerMenu.ID.MAIN.DB_VIEWER
        menu.add(DrawerMenuItem.create(DrawerMenu.ID.MAIN.ANTENNA_MAP_VIEW, getString(R.string.map_view), R.drawable.ic_action_map, false));               // Antenna Map Viewer
        // Changed 103 --> DrawerMenu.ID.MAIN.DB_VIEWER
        menu.add(DrawerMenuItem.create(DrawerMenu.ID.MAIN.AT_COMMAND_INTERFACE, getString(R.string.at_command_title), R.drawable.ic_action_computer, true));   // AT Command Interface

        //Section Tracking
        menu.add(DrawerMenuSection.create(DrawerMenu.ID.SECTION_TRACKING, getString(R.string.tracking)));         // Changed 900 --> DrawerMenu.ID.SECTION_TRACKING
        // Changed 901 --> DrawerMenu.ID.TRACKING.TOGGLE_AIMSICD_MONITORING
        menu.add(DrawerMenuItem.create(DrawerMenu.ID.TRACKING.TOGGLE_AIMSICD_MONITORING, getString(R.string.toggle_aimsicd_monitoring), R.drawable.untrack_cell, false));    // Toggle "AIMSICD Monitoring"
        // Changed 902 --> DrawerMenu.ID.TRACKING.TOGGLE_2G_ONLY_NETWORK
        menu.add(DrawerMenuItem.create(DrawerMenu.ID.TRACKING.TOGGLE_2G_ONLY_NETWORK, getString(R.string.toggle_2g_only_network_lock), R.drawable.untrack_cell, false));      // Toggle "Track Cell Details"
        if (CellTracker.PHONE_TYPE == TelephonyManager.PHONE_TYPE_CDMA) {
            // Changed 903 --> DrawerMenu.ID.TRACKING.TRACK_FEMTOCELL
            menu.add(DrawerMenuItem.create(DrawerMenu.ID.TRACKING.TRACK_FEMTOCELL, getString(R.string.track_femtocell), R.drawable.ic_action_network_cell, false, false)); // Track FemtoCell
        }

        //Section Settings
        menu.add(DrawerMenuSection.create(DrawerMenu.ID.SECTION_SETTINGS, getString(R.string.settings))); // Changed 200 --> DrawerMenu.ID.SECTION_SETTINGS
        // Changed 202 --> DrawerMenu.ID.SETTINGS.PREFERENCES
        menu.add(DrawerMenuItem.create(DrawerMenu.ID.SETTINGS.PREFERENCES, getString(R.string.preferences), R.drawable.ic_action_settings, false));            // Preferences
        // Changed 203 --> DrawerMenu.ID.SETTINGS.PREFERENCES
        menu.add(DrawerMenuItem.create(DrawerMenu.ID.SETTINGS.BACKUP_DB, getString(R.string.backup_database), R.drawable.ic_action_import_export, false));   // Backup Database
        // Changed 204 --> DrawerMenu.ID.SETTINGS.RESTORE_DB
        menu.add(DrawerMenuItem.create(DrawerMenu.ID.SETTINGS.RESTORE_DB, getString(R.string.restore_database), R.drawable.ic_action_import_export, false));  // Restore Database

        //Section Application
        menu.add(DrawerMenuSection.create(DrawerMenu.ID.SECTION_APPLICATION, getString(R.string.application))); // Changed 300 --> DrawerMenu.ID.SECTION_APPLICATION
        // Changed 301 --> DrawerMenu.ID.APPLICATION.DOWNLOAD_LOCAL_BST_DATA
        menu.add(DrawerMenuItem.create(DrawerMenu.ID.APPLICATION.DOWNLOAD_LOCAL_BST_DATA, getString(R.string.get_opencellid), R.drawable.stat_sys_download_anim0, false, false));   // "Download Local BTS data"
        // Changed 306 --> DrawerMenu.ID.APPLICATION.UPLOAD_LOCAL_BST_DATA
        menu.add(DrawerMenuItem.create(DrawerMenu.ID.APPLICATION.UPLOAD_LOCAL_BST_DATA, getString(R.string.upload_bts), R.drawable.stat_sys_upload_anim0, false, false));      // "Upload Local BTS data"
        // Changed 303 --> DrawerMenu.ID.APPLICATION.ABOUT
        menu.add(DrawerMenuItem.create(DrawerMenu.ID.APPLICATION.ABOUT, getString(R.string.about_aimsicd), R.drawable.ic_action_about, true));         // About
        // Changed 305 --> DrawerMenu.ID.APPLICATION.SEND_DEBUGGING_LOG
        menu.add(DrawerMenuItem.create(DrawerMenu.ID.APPLICATION.SEND_DEBUGGING_LOG, getString(R.string.send_logs), R.drawable.ic_action_computer, false, false));         // Debugging
        // Changed 304 --> DrawerMenu.ID.APPLICATION.QUIT
        menu.add(DrawerMenuItem.create(DrawerMenu.ID.APPLICATION.QUIT, getString(R.string.quit), R.drawable.ic_action_remove, false, false));                // Quit

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
        NavDrawerItem cellMonitoringItem = null;
        NavDrawerItem cellTrackingItem = null;

        List<NavDrawerItem> menuItems = mNavConf.getNavItems();
        for(NavDrawerItem lItem:menuItems) {
            if(lItem.getId() == DrawerMenu.ID.TRACKING.TOGGLE_AIMSICD_MONITORING) {
                cellMonitoringItem = lItem;
            } else if(lItem.getId() == DrawerMenu.ID.TRACKING.TOGGLE_2G_ONLY_NETWORK) {
                cellTrackingItem = lItem;
            } else if(lItem.getId() == DrawerMenu.ID.TRACKING.TRACK_FEMTOCELL) {
                femtoTrackingItem = lItem;
            }
        }

        if (mBound) {
            if (cellMonitoringItem != null) {
                if (mAimsicdService.isMonitoringCell()) {
                    cellMonitoringItem.setmIconId(R.drawable.track_cell);
                } else {
                    cellMonitoringItem.setmIconId(R.drawable.untrack_cell);
                }
                mNavConf.getBaseAdapter().notifyDataSetChanged();
            }
            if (cellTrackingItem != null) {
                if (mAimsicdService.isTrackingCell()) {
                    cellTrackingItem.setmIconId(R.drawable.track_cell);
                } else {
                    cellTrackingItem.setmIconId(R.drawable.untrack_cell);
                }
                mNavConf.getBaseAdapter().notifyDataSetChanged();
            }

            if (femtoTrackingItem != null) {
                if (mAimsicdService.isTrackingFemtocell()) {
                    femtoTrackingItem.setmIconId(R.drawable.ic_action_network_cell);
                } else {
                    femtoTrackingItem.setmIconId(R.drawable.ic_action_network_cell_not_tracked);
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

    public void showProgressbar(final boolean indeterminate, final int max, final int progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setIndeterminate(indeterminate);
                if (max > 0) mProgressBar.setMax(max);
                if (max > 0 && progress >= 0) mProgressBar.setProgress(progress);
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hideProgressbar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setMax(0);
                mProgressBar.setProgress(0);
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }
}

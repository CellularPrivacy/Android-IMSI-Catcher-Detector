/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
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
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.SecUpwN.AIMSICD.activities.AboutActivity;
import com.SecUpwN.AIMSICD.activities.BaseActivity;
import com.SecUpwN.AIMSICD.activities.DebugLogs;
import com.SecUpwN.AIMSICD.fragments.MapFragment;
import com.SecUpwN.AIMSICD.activities.SettingsActivity;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.constants.DrawerMenu;
import com.SecUpwN.AIMSICD.drawer.DrawerMenuActivityConfiguration;
import com.SecUpwN.AIMSICD.drawer.NavDrawerItem;
import com.SecUpwN.AIMSICD.fragments.AtCommandFragment;
import com.SecUpwN.AIMSICD.fragments.CellInfoFragment;
import com.SecUpwN.AIMSICD.fragments.DbViewerFragment;
import com.SecUpwN.AIMSICD.fragments.DeviceFragment;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.service.CellTracker;
import com.SecUpwN.AIMSICD.utils.AsyncResponse;
import com.SecUpwN.AIMSICD.utils.Cell;
import com.SecUpwN.AIMSICD.utils.GeoLocation;
import com.SecUpwN.AIMSICD.utils.Helpers;
import com.SecUpwN.AIMSICD.utils.Icon;
import com.SecUpwN.AIMSICD.utils.LocationServices;
import com.SecUpwN.AIMSICD.utils.RequestTask;
import com.SecUpwN.AIMSICD.utils.StackOverflowXmlParser;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import io.freefair.android.injection.annotation.Inject;

public class AIMSICD extends BaseActivity implements AsyncResponse {

    private boolean mBound;
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    private Editor prefsEditor;
    private String mDisclaimerAccepted;
    private AimsicdService mAimsicdService;

    private DrawerLayout mDrawerLayout;
    private ActionBar mActionBar;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private DeviceFragment deviceFragment;
    private CellInfoFragment cellInfoFragment;
    private AtCommandFragment atCommandFragment;
    private DbViewerFragment dbViewerFragment;
    private MapFragment mapFragment;

    private long mLastPress = 0;    // Back press to exit timer

    private DrawerMenuActivityConfiguration mNavConf;

    @Inject
    OkHttpClient okHttpClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceFragment = new DeviceFragment();
        cellInfoFragment = new CellInfoFragment();
        atCommandFragment = new AtCommandFragment();
        dbViewerFragment = new DbViewerFragment();
        mapFragment = new MapFragment();

        mNavConf = new DrawerMenuActivityConfiguration.Builder(this).build();

        mDrawerLayout = (DrawerLayout) findViewById(mNavConf.getDrawerLayoutId());
        mDrawerList = (ListView) findViewById(mNavConf.getLeftDrawerId());
        mActionBar = getSupportActionBar();
        mTitle =  getTitle();
        mDrawerTitle = getTitle();

        mDrawerList.setAdapter(mNavConf.getBaseAdapter());

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                mActionBar.setTitle(mTitle);
                invalidateOptionsMenu();
            }

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

        prefs = getSharedPreferences(AimsicdService.SHARED_PREFERENCES_BASENAME, 0);

                /* Pref listener to enable sms detection on pref change   */
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals(getString(R.string.adv_user_root_pref_key))) {
                    SmsDetection();
                }

            }
        };
        prefs.registerOnSharedPreferenceChangeListener(prefListener);

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
                            if (mAimsicdService != null) {
                                mAimsicdService.onDestroy();
                            }
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
        final String iconType = prefs.getString(getString(R.string.pref_ui_icons_key), "SENSE").toUpperCase();
        mActionBar.setIcon(Icon.getIcon(Icon.Type.valueOf(iconType), ((AppAIMSICD) getApplication()).getStatus()));
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

        final String PERSIST_SERVICE = getString(R.string.pref_persistservice_key);
        boolean persistService = prefs.getBoolean(PERSIST_SERVICE, false);
        if (!persistService) {
            stopService(new Intent(this, AimsicdService.class));
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mDrawerLayout.closeDrawer(mDrawerList);
            selectDrawerItem(position);
        }
    }




    void selectDrawerItem(int position) {
        NavDrawerItem selectedItem = mNavConf.getNavItems().get(position);
        String title = selectedItem.getLabel();

        switch (selectedItem.getId()) {
            case DrawerMenu.ID.MAIN.PHONE_SIM_DETAILS:
                openFragment(deviceFragment);
                title = getString(R.string.app_name_short);
                break;
            case DrawerMenu.ID.MAIN.CURRENT_TREAT_LEVEL:
                openFragment(cellInfoFragment);
                title = getString(R.string.app_name_short);
                break;
            case DrawerMenu.ID.MAIN.AT_COMMAND_INTERFACE:
                openFragment(atCommandFragment);
                title = getString(R.string.app_name_short);
                break;
            case DrawerMenu.ID.MAIN.DB_VIEWER:
                openFragment(dbViewerFragment);
                title = getString(R.string.app_name_short);
                break;
            case DrawerMenu.ID.APPLICATION.UPLOAD_LOCAL_BTS_DATA:
                // Request uploading here?
                new RequestTask(this, com.SecUpwN.AIMSICD.utils.RequestTask.DBE_UPLOAD_REQUEST).execute("");
                // no string needed for csv based upload
                break;
            case DrawerMenu.ID.MAIN.ANTENNA_MAP_VIEW:
                openFragment(mapFragment);
                title = getString(R.string.app_name_short);
                break;
            case DrawerMenu.ID.SETTINGS.BACKUP_DB:
                new RequestTask(this, RequestTask.BACKUP_DATABASE).execute();
                break;
        }

        if (selectedItem.getId() == DrawerMenu.ID.SETTINGS.RESTORE_DB) {
            if (CellTracker.LAST_DB_BACKUP_VERSION < AIMSICDDbAdapter.DATABASE_VERSION) {
                Helpers.msgLong(this, getString(R.string.unable_to_restore_backup_from_previous_database_version));
            } else {
                new RequestTask(this, RequestTask.RESTORE_DATABASE).execute();
            }
        } else if (selectedItem.getId() == DrawerMenu.ID.SETTINGS.RESET_DB) {
            // WARNING! This deletes the entire database, thus any subsequent DB access will FC app.
            //          Therefore we need to either restart app or run AIMSICDDbAdapter, to rebuild DB.
            //          See: https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/581 and Helpers.java
            Helpers.askAndDeleteDb(this);


        } else if (selectedItem.getId() == DrawerMenu.ID.APPLICATION.DOWNLOAD_LOCAL_BTS_DATA) {
            downloadBtsDataIfApiKeyAvailable();
        } else if (selectedItem.getId() == DrawerMenu.ID.MAIN.ALL_CURRENT_CELL_DETAILS) {
            if (CellTracker.OCID_API_KEY != null && !CellTracker.OCID_API_KEY.equals("NA")) {

                //TODO: Use Retrofit for that
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

                Request request = new Request.Builder()
                        .url(sb.toString())
                        .get()
                        .build();

                okHttpClient.newCall(request)
                        .enqueue(new Callback() {
                            @Override
                            public void onFailure(Request request, IOException e) {

                            }

                            @Override
                            public void onResponse(Response response) throws IOException {
                                try {
                                    List<Cell> cellList = new StackOverflowXmlParser().parse(response.body().byteStream());
                                    AIMSICD.this.processFinish(cellList);
                                } catch (XmlPullParserException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            } else {
                Helpers.sendMsg(this, getString(R.string.no_opencellid_key_detected));
            }
        } else if (selectedItem.getId() == DrawerMenu.ID.APPLICATION.QUIT) {
            try {
                if (mAimsicdService.isSmsTracking()) {
                    mAimsicdService.stopSmsTracking();
                }
            } catch (Exception ee) {
                log.warn("Exception in smstracking module: " + ee.getMessage());
            }

            if (mAimsicdService != null) {
                mAimsicdService.onDestroy();
            }
            //Close database on Exit
            log.info("Closing db from DrawerMenu.ID.APPLICATION.QUIT");
            new AIMSICDDbAdapter(getApplicationContext()).close();
            finish();
        }

        mDrawerList.setItemChecked(position, true);

        if (selectedItem.updateActionBarTitle()) {
            setTitle(title);
        }

        if (this.mDrawerLayout.isDrawerOpen(this.mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.content_frame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void downloadBtsDataIfApiKeyAvailable() {
        if (CellTracker.OCID_API_KEY != null && !CellTracker.OCID_API_KEY.equals("NA")) {

            Cell cell = new Cell();
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String networkOperator = tm.getNetworkOperator();

            if (networkOperator != null) {
                int mcc = Integer.parseInt(networkOperator.substring(0, 3));
                cell.setMCC(Integer.parseInt(networkOperator.substring(0, 3)));
                int mnc = Integer.parseInt(networkOperator.substring(3));
                cell.setMNC(Integer.parseInt(networkOperator.substring(3, 5)));
                log.debug("CELL:: mcc=" + mcc + " mnc=" + mnc);
            }


            GeoLocation loc = mAimsicdService.lastKnownLocation();
            if (loc != null) {
                Helpers.msgLong(this, getString(R.string.contacting_opencellid_for_data));

                cell.setLon(loc.getLongitudeInDegrees());
                cell.setLat(loc.getLatitudeInDegrees());
                Helpers.getOpenCellData(this, cell, RequestTask.DBE_DOWNLOAD_REQUEST);
            } else {
                Helpers.msgShort(this, getString(R.string.waiting_for_location));

                // This uses the LocationServices to get CID/LAC/MNC/MCC to be used
                // for grabbing the BTS data from OCID, via their API.
                // CID Location Async Output Delegate Interface Implementation
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
            Helpers.sendMsg(this, getString(R.string.no_opencellid_key_detected));
        }
    }

    @Override
    public void processFinish(float[] location) {
        log.info("processFinish - location[0]=" + location[0] + " location[1]=" + location[1]);


        if (Float.floatToRawIntBits(location[0]) == 0
                && Float.floatToRawIntBits(location[1]) != 0) {
            Helpers.msgLong(this, getString(R.string.contacting_opencellid_for_data));
            Helpers.getOpenCellData(this, mAimsicdService.getCell(), RequestTask.DBE_DOWNLOAD_REQUEST);
        } else {
            Helpers.msgLong(this, getString(R.string.unable_to_determine_last_location));
        }
    }

    @Override
    public void processFinish(List<Cell> cells) {
        if (cells != null) {
            if (!cells.isEmpty()) {
                for (Cell cell : cells) {
                    log.info("processFinish - Cell =" + cell.toString());
                    if (cell.isValid()) {
                        mAimsicdService.setCell(cell);
                        Intent intent = new Intent(AimsicdService.UPDATE_DISPLAY);
                        intent.putExtra("update", true);
                        sendBroadcast(intent);
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

    /**
     * Service Connection to bind the activity to the service
     */
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mAimsicdService = ((AimsicdService.AimscidBinder) service).getService();
            mBound = true;

            // Check if tracking cell details check location services are still enabled
            if (mAimsicdService.isTrackingCell()) {
                mAimsicdService.checkLocationServices();
            }

            if (!mAimsicdService.isSmsTracking() && prefs.getBoolean(getString(R.string.adv_user_root_pref_key), false)) {
                    /*Auto Start sms detection here if:
                    *    isSmsTracking = false <---- not running
                    *    root sms enabled = true
                    *
                    * */
                SmsDetection();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            log.warn("Service disconnected");
            mBound = false;
        }
    };

    private void startService() {
        // don't start service if disclaimer is not accepted
        if (!prefs.getBoolean(mDisclaimerAccepted, false)) {
            return;
        }

        if (!mBound) {
            // Bind to LocalService
            Intent intent = new Intent(AIMSICD.this, AimsicdService.class);
            //Start Service before binding to keep it resident when activity is destroyed
            startService(intent);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

            openFragment(deviceFragment);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem toggleAttackDetectionMenuItem = menu.findItem(R.id.toggle_attack_detection);
        toggleAttackDetectionMenuItem.setChecked(true);
        MenuItem toggleCellTrackingMenuItem = menu.findItem(R.id.toggle_cell_tracking);
        toggleCellTrackingMenuItem.setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.toggle_attack_detection:
                item.setChecked(!item.isChecked());
                monitorCell();
                break;
            case R.id.toggle_cell_tracking:
                item.setChecked(!item.isChecked());
                trackCell();
                break;
            case R.id.settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent );
                break;
            case R.id.about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                break;
            case R.id.debugging:
                Intent i = new Intent(this, DebugLogs.class);
                startActivity(i);
                break;
        }

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
            try {
                if (mAimsicdService.isSmsTracking()) {
                    mAimsicdService.stopSmsTracking();
                }
            } catch (Exception ee) {
                log.error("Error: Stopping SMS detection : " + ee.getMessage());
            }
            // Close database on Exit
            log.info("Closing db from onBackPressed()");
            new AIMSICDDbAdapter(getApplicationContext()).close();
            finish();
        }
    }


    private void SmsDetection() {
        boolean root_sms = prefs.getBoolean(getString(R.string.adv_user_root_pref_key), false); // default is false

        if (root_sms && !mAimsicdService.isSmsTracking()) {
            mAimsicdService.startSmsTracking();
            Helpers.msgShort(this, "SMS Detection Started");
            log.info("SMS Detection Thread Started");
        } else if (!root_sms && mAimsicdService.isSmsTracking()) {
            mAimsicdService.stopSmsTracking();
            Helpers.msgShort(this, "Sms Detection Stopped");
            log.info("SMS Detection Thread Stopped");
        }
    }

    /**
     * Cell Information Tracking - Enable/Disable
     */
    private void trackCell() {
        mAimsicdService.setCellTracking(mAimsicdService.isTrackingCell());
    }

    /**
     * Cell Information Monitoring - Enable/Disable
     */
    private void monitorCell() {
        mAimsicdService.setCellMonitoring(!mAimsicdService.isMonitoringCell());
    }

    /**
     * FemtoCell Detection (CDMA Phones ONLY) - Enable/Disable
     */
    private void trackFemtocell() {
        mAimsicdService.setTrackingFemtocell(!mAimsicdService.isTrackingFemtocell());
    }

    public void onStop() {
        super.onStop();
        ((AppAIMSICD) getApplication()).detach(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AppAIMSICD) getApplication()).attach(this);
    }
}

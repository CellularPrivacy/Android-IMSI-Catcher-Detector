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
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.activities.MapViewer;
import com.SecUpwN.AIMSICD.activities.PrefActivity;
import com.SecUpwN.AIMSICD.fragments.AboutFragment;
import com.SecUpwN.AIMSICD.fragments.AtCommandFragment;
import com.SecUpwN.AIMSICD.fragments.CellInfoFragment;
import com.SecUpwN.AIMSICD.fragments.DbViewerFragment;
import com.SecUpwN.AIMSICD.fragments.DeviceFragment;
import com.SecUpwN.AIMSICD.fragments.SilentSmsFragment;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.utils.Helpers;
import com.SecUpwN.AIMSICD.utils.RequestTask;

import java.util.ArrayList;
import java.util.List;

public class AIMSICD extends FragmentActivity {

    private final String TAG = "AIMSICD";

    private final Context mContext = this;
    private boolean mBound;
    private SharedPreferences prefs;
    private Editor prefsEditor;
    private AIMSICDDbAdapter dbHelper;
    private String mDisclaimerAccepted;

    private AimsicdService mAimsicdService;

    private FragmentManager fm;
    private List<Fragment> mFragmentList;
    private List<String> titles;

    private FragmentStatePagerAdapter adapterViewPager;
    public static ProgressBar mProgressBar;

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
        fm = getSupportFragmentManager();
        adapterViewPager = new MyPagerAdapter(fm);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(adapterViewPager);
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

        //Create DB Instance
        dbHelper = new AIMSICDDbAdapter(mContext);

        //Register receiver for Silent SMS Interception Notification
        mContext.registerReceiver(mMessageReceiver, new IntentFilter(AimsicdService.SILENT_SMS));
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

        mContext.unregisterReceiver(mMessageReceiver);
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

            if (mAimsicdService.getPhoneID() == TelephonyManager.PHONE_TYPE_CDMA) {
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
            case R.id.show_map:
                showmap();
                return true;
            case R.id.preferences:
                intent = new Intent(this, PrefActivity.class);
                startActivity(intent);
                return true;
            case R.id.backup_database:
                dbHelper.exportDB();
                return true;
            case R.id.restore_database:
                new RequestTask(mContext, RequestTask.RESTORE_DATABASE).execute();
                return true;
            case R.id.update_opencelldata:
                Location loc = mAimsicdService.lastKnownLocation();
                if (loc != null) {
                    Helpers.getOpenCellData(mContext, loc.getLatitude(), loc.getLongitude());
                } else {
                    Helpers.sendMsg(mContext,
                            "Unable to determine your last location, enable Location Services and try again.");
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
     * FemtoCell Detection (CDMA Phones ONLY) - Enable/Disable
     */
    private void trackFemtocell() {
        if (mAimsicdService.isTrackingFemtocell()) {
            mAimsicdService.stopTrackingFemto();
        } else {
            mAimsicdService.startTrackingFemto();
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Bundle smsCardBundle = new Bundle();
                smsCardBundle.putString("address", bundle.getString("address"));
                smsCardBundle.putString("display_address", bundle.getString("display_address"));
                smsCardBundle.putString("message_class", bundle.getString("class"));
                smsCardBundle.putString("service_centre", bundle.getString("service_centre"));
                smsCardBundle.putString("message", bundle.getString("message"));
                smsCardBundle.putInt("timestamp", bundle.getInt("timestamp"));
                mAimsicdService.setSilentSmsStatus(true);
                dbHelper.open();
                dbHelper.insertSilentSms(smsCardBundle);
                dbHelper.close();
                Fragment fragment = new SilentSmsFragment();
                titles.add(getString(R.string.sms_title));
                mFragmentList.add(fragment);
                adapterViewPager.notifyDataSetChanged();
                adapterViewPager.getItem(adapterViewPager.getCount()-1);
            }
        }
    };

    class MyPagerAdapter extends FragmentStatePagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentList = new ArrayList<>();
            titles = new ArrayList<>();
            mFragmentList.add(new DeviceFragment());
            titles.add(getString(R.string.device_info));
            mFragmentList.add(new CellInfoFragment());
            titles.add(getString(R.string.cell_info_title));
            mFragmentList.add(new AtCommandFragment());
            titles.add(getString(R.string.at_command_title));
            mFragmentList.add(new DbViewerFragment());
            titles.add(getString(R.string.db_viewer));
            mFragmentList.add(new AboutFragment());
            titles.add(getString(R.string.about_aimsicd));
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

    }
}

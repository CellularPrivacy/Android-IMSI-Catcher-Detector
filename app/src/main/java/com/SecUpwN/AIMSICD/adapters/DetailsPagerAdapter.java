package com.SecUpwN.AIMSICD.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.SecUpwN.AIMSICD.fragments.CellInfoFragment;
import com.SecUpwN.AIMSICD.fragments.DbViewerFragment;
import com.SecUpwN.AIMSICD.fragments.DeviceFragment;

/**
 * Adapter to allow swiping between various detail fragments
 */
public class DetailsPagerAdapter extends FragmentPagerAdapter {

    public DetailsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return new DeviceFragment();
            case 1: return new CellInfoFragment();
            case 2: return new DbViewerFragment();
        }

        return new DeviceFragment();
    }

    @Override
    public long getItemId(int position) {
        // map position to position in AIMSICD.getNavDrawerConfiguration
        switch (position) {
            case 0: return 4;
            case 1: return 5;
            case 2: return 7;
        }

        return -1;
    }

    @Override
    public int getCount() {
        return 3;
    }
}

package com.SecUpwN.AIMSICD.activities;

import com.SecUpwN.AIMSICD.fragments.MapPrefFragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;


public class MapPrefActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapPrefFragment settingsFragment = new MapPrefFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(android.R.id.content, settingsFragment);
        fragmentTransaction.commit();
    }

}
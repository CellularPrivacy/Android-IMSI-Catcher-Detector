package com.SecUpwN.AIMSICD.activities;

import com.SecUpwN.AIMSICD.fragments.PrefFragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;


public class PrefActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadFragment();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadFragment();
    }

    private void loadFragment() {
        PrefFragment settingsFragment = new PrefFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(android.R.id.content, settingsFragment);
        fragmentTransaction.commit();
    }
}

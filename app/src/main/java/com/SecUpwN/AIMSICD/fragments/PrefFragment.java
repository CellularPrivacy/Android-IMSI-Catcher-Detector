package com.SecUpwN.AIMSICD.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.SecUpwN.AIMSICD.R;

public class PrefFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}

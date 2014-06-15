package com.SecUpwN.AIMSICD.fragments;

import com.SecUpwN.AIMSICD.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class MapPrefFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.map_preferences);
    }

}

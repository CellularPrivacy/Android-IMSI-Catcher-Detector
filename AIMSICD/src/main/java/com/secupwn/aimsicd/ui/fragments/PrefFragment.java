/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.secupwn.aimsicd.R;

public class PrefFragment extends PreferenceFragment {


    CheckBoxPreference gpsPref;
    LocationManager locationManager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        final String gps_key = getResources().getString(R.string.pref_enable_gps_key);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        gpsPref = (CheckBoxPreference) findPreference(gps_key);
        gpsPref.setDefaultValue(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        gpsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent gpsSettings = new Intent(
                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(gpsSettings, 1);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    gpsPref.setChecked(true);
                    return true;
                }

                gpsPref.setChecked(false);
                return false;

            }

        });


        gpsPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Log.e("pref", "changed to " + newValue);
                    preference.setDefaultValue(newValue);
                    return true;
                }

                preference.setDefaultValue(newValue);
                return false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                gpsPref.setChecked(true);
                gpsPref.setDefaultValue(true);
            } else {
                gpsPref.setDefaultValue(false);
                gpsPref.setChecked(false);
            }
        }
    }
}

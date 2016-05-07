/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.ui.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.secupwn.aimsicd.ui.fragments.MapPrefFragment;

public class MapPrefActivity extends BaseActivity {

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

/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.ui.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.adapters.DetailsPagerAdapter;

import io.freefair.android.injection.annotation.InjectView;
import io.freefair.android.injection.annotation.XmlLayout;
import io.freefair.android.injection.app.InjectionFragment;

/**
 * This fragment will host child fragments to display device details, cell info, etc.
 */
@XmlLayout(R.layout.fragment_details_container)
public class DetailsContainerFragment extends InjectionFragment {

    @InjectView(R.id.details_pager)
    private ViewPager vp;

    @InjectView(R.id.details_pager_tab_strip)
    private PagerTabStrip tabStrip;

    DetailsPagerAdapter adapter;
    int initialPage = -1;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new DetailsPagerAdapter(getChildFragmentManager(), getActivity());

        tabStrip.setBackgroundColor(Color.BLACK);

        vp.setAdapter(adapter);

        if (initialPage >= 0) {
            vp.setCurrentItem(initialPage);
        }
   }

    public void setCurrentPage(int page) {
        if (adapter == null) {
            initialPage = page;
            return;
        }

        if (page >= 0 && page < adapter.getCount()) {
            vp.setCurrentItem(page);
        }
    }
}

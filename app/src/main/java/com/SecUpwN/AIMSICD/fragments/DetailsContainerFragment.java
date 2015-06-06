/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.DetailsPagerAdapter;

/**
 * This fragment will host child fragments to display device details, cell info, etc.
 */
public class DetailsContainerFragment extends Fragment {
    ViewPager vp;
    DetailsPagerAdapter adapter;
    int initialPage = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details_container, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new DetailsPagerAdapter(getChildFragmentManager(), getActivity());

        PagerTabStrip tabStrip = (PagerTabStrip) view.findViewById(R.id.details_pager_tab_strip);
        tabStrip.setBackgroundColor(Color.BLACK);

        vp = (ViewPager) view.findViewById(R.id.details_pager);
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

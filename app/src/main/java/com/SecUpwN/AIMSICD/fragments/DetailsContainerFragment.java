package com.SecUpwN.AIMSICD.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.SecUpwN.AIMSICD.AIMSICD;
import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.DetailsPagerAdapter;
import com.SecUpwN.AIMSICD.drawer.DrawerMenuActivityConfiguration;
import com.SecUpwN.AIMSICD.drawer.NavDrawerItem;

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

        final DrawerMenuActivityConfiguration mNavConf = ((AIMSICD) getActivity()).getNavDrawerConfiguration();
        adapter = new DetailsPagerAdapter(getChildFragmentManager());

        vp = (ViewPager) view.findViewById(R.id.details_pager);
        vp.setAdapter(adapter);
        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int navId = (int) adapter.getItemId(position);
                NavDrawerItem selectedItem = mNavConf.getNavItems().get(navId);
                Log.d("nav", "selected Item " + selectedItem.getLabel());
                getActivity().getActionBar().setTitle(selectedItem.getLabel());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

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

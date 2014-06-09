package com.SecUpwN.AIMSICD.fragments;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.adapters.BaseInflaterAdapter;
import com.SecUpwN.AIMSICD.adapters.SilentSmsCardData;
import com.SecUpwN.AIMSICD.adapters.SilentSmsCardInflater;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class SilentSmsFragment extends Fragment {

    private AIMSICDDbAdapter mDbHelper;

    public SilentSmsFragment() {}

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        mDbHelper = new AIMSICDDbAdapter(getActivity().getBaseContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sms_fragment,
                container, false);

        if (view != null) {
            ListView lv = (ListView) view.findViewById(R.id.list_view);
            mDbHelper.open();
            Cursor smsData = mDbHelper.getSilentSmsData();
            BaseInflaterAdapter<SilentSmsCardData> adapter
                    = new BaseInflaterAdapter<>(
                    new SilentSmsCardInflater());
            while (smsData.moveToNext()) {
                SilentSmsCardData data = new SilentSmsCardData(smsData.getString(0), smsData.getString(1),
                        smsData.getString(2), smsData.getString(3), smsData.getString(4),
                        smsData.getInt(5));
                adapter.addItem(data, false);
            }
            mDbHelper.close();
            lv.setAdapter(adapter);
            lv.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

}

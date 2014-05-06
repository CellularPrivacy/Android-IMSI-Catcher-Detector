package com.SecUpwN.AIMSICD;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends Fragment {

    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.about_fragment, container, false);
        String version;

        PackageManager manager = mContext.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException nnfe) {
            //Woops something went wrong??
            version = "";
        }

        TextView versionNumber = (TextView) v.findViewById(R.id.aimsicd_version);
        versionNumber.setText(version);

        return v;
    }

    public AboutFragment (Context context) {
        mContext = context;
    }


}

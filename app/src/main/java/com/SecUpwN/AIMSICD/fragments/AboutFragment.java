package com.SecUpwN.AIMSICD.fragments;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.activities.CreditsRollActivity;
import com.SecUpwN.AIMSICD.utils.MiscUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
            PackageInfo info = manager != null ? manager
                    .getPackageInfo(mContext.getPackageName(), 0) : null;
            version = info != null ? info.versionName : "";
        } catch (PackageManager.NameNotFoundException nnfe) {
            //Woops something went wrong??
            version = "";
        }

        TextView versionNumber;
        if (v != null) {
            versionNumber = (TextView) v.findViewById(R.id.aimsicd_version);
            versionNumber.setText(getString(R.string.app_version) + version);

            View tv;

            //links
            tv = v.findViewById(R.id.img_about_github);
            setLink(tv, R.string.aimsicd_github_link);

            tv = v.findViewById(R.id.img_about_wiki);
            setLink(tv, R.string.wiki_link);

            tv = v.findViewById(R.id.img_about_poc);
            setLink(tv, R.string.poc_link);

            tv = v.findViewById(R.id.img_about_disclaim);
            setLink(tv, R.string.disclaimer_link);

            tv = v.findViewById(R.id.img_about_releases);
            setLink(tv, R.string.aimsicd_release_link);

            tv = v.findViewById(R.id.img_about_changelog);
            setLink(tv, R.string.aimsicd_changelog_link);

            tv = v.findViewById(R.id.img_about_gnu);
            setLink(tv, R.string.aimsicd_license_link);

            tv = v.findViewById(R.id.img_about_credits);
            buttonAction(tv);

            // Detailed pop ups
            tv = v.findViewById(R.id.imgView_idle);
            setPopUp(tv,0);

            tv = v.findViewById(R.id.imgView_normal);
            setPopUp(tv,1);

            tv = v.findViewById(R.id.imgView_medium);
            setPopUp(tv,2);

            tv = v.findViewById(R.id.imgView_high);
            setPopUp(tv,3);

            tv = v.findViewById(R.id.imgView_danger);
            setPopUp(tv,4);

            tv= v.findViewById(R.id.imgView_run);
            setPopUp(tv,5);
        }
        return v;
    }
    private void setPopUp(View view1, final int mode) {
        view1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MiscUtils.startPopUpInfo(mContext, mode);
            }
        });
    }
    private void setLink(View view2, final int link) {
        view2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getResources().getString(link)));
                startActivity(intent);
            }
        });
    }

    private void buttonAction(View view3) {
        view3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(mContext, CreditsRollActivity.class);
                    startActivity(i);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getBaseContext();
    }

    public AboutFragment() {
    }


}

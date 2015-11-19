/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.BuildConfig;
import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.activities.CreditsRollActivity;
import com.SecUpwN.AIMSICD.utils.MiscUtils;

public class AboutFragment extends Fragment {

    private static final String TAG = "Aboutfragment";
    private Context mContext;
    private Button btncredits;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.about_fragment, container, false);
        String version;

        btncredits = (Button) v.findViewById(R.id.aimsicd_credits_link);

        PackageManager manager = mContext.getPackageManager();
        try {
            PackageInfo info = manager != null ? manager
                    .getPackageInfo(mContext.getPackageName(), 0) : null;
            version = (info != null ? info.versionName : "");
        } catch (PackageManager.NameNotFoundException e) {
            //Woops something went wrong?? // so what do we do then?
            // YES!! LOG THE EXCEPTION ~agilob
            Log.e(TAG, "Something went wrong", e);
            version = "";
        }

        String BUILDOZER_BUILDNUMBER = BuildConfig.BUILDOZER_BUILDNUMBER;
        if (BUILDOZER_BUILDNUMBER == null) {
            BUILDOZER_BUILDNUMBER = "NA"; // avoid null buildnumber
        }

        TextView versionNumber;
        TextView BuildozerView;
        if (v != null) {
            versionNumber = (TextView) v.findViewById(R.id.aimsicd_version);
            versionNumber.setText(getString(R.string.app_version) + version);

            if(!"NA".equals(BUILDOZER_BUILDNUMBER)) {
                BuildozerView = (TextView) v.findViewById(R.id.buildozer_buildnumber);
                BuildozerView.setText(getString(R.string.buildozer_buildnumber) + BUILDOZER_BUILDNUMBER);
                BuildozerView.setVisibility(View.VISIBLE);
            }

            //GitHub WIKI Link
            View tv = v.findViewById(R.id.aimsicd_wiki_link);
            setLink(tv, R.string.aimsicd_wiki_link);

            //GitHub Website Link
            tv = v.findViewById(R.id.aimsicd_github_link);
            setLink(tv, R.string.aimsicd_github_link);

            //Disclaimer Link
            tv = v.findViewById(R.id.aimsicd_disclaimer_link);
            setLink(tv, R.string.disclaimer_link);

            //GitHub Contribution Link
            tv = v.findViewById(R.id.aimsicd_contribute_link);
            setLink(tv, R.string.aimsicd_contribute_link);

            //WIP Release Link
            tv = v.findViewById(R.id.aimsicd_release_link);
            setLink(tv, R.string.aimsicd_release_link);

            //Changelog Link
            tv = v.findViewById(R.id.aimsicd_changelog_link);
            setLink(tv, R.string.aimsicd_changelog_link);

            //License Link
            tv = v.findViewById(R.id.aimsicd_license_link);
            setLink(tv, R.string.aimsicd_license_link);

            View imgView_idle = v.findViewById(R.id.imgView_idle);
            imgView_idle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MiscUtils.startPopUpInfo(mContext, 0);
                }
            });

            View imgView_normal = v.findViewById(R.id.imgView_normal);
            imgView_normal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MiscUtils.startPopUpInfo(mContext, 1);
                }
            });

            View imgView_medium = v.findViewById(R.id.imgView_medium);
            imgView_medium.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MiscUtils.startPopUpInfo(mContext, 2);
                }
            });

            View imgView_high = v.findViewById(R.id.imgView_high);
            imgView_high.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MiscUtils.startPopUpInfo(mContext, 3);
                }
            });

            View imgView_danger = v.findViewById(R.id.imgView_danger);
            imgView_danger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MiscUtils.startPopUpInfo(mContext, 4);
                }
            });

            View imgView_run = v.findViewById(R.id.imgView_run);
            imgView_run.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MiscUtils.startPopUpInfo(mContext, 5);
                }
            });
        }
        return v;
    }

    private void setLink(View b, final int link) {
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getResources().getString(link)));
                startActivity(intent);
            }
        });

        btncredits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, CreditsRollActivity.class);
                startActivity(i);
            }
        });

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getBaseContext();
    }
}
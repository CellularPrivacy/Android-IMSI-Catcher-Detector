package com.SecUpwN.AIMSICD.fragments;

import com.SecUpwN.AIMSICD.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
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

            //Status icons link
            View tv = v.findViewById(R.id.aimsicd_status_icons);
            setLink(tv, R.string.status_icons_link);

            //GitHub WIKI Link
            tv = v.findViewById(R.id.aimsicd_wiki_link);
            setLink(tv, R.string.wiki_link);

            //Proof of Concept Link
            tv = v.findViewById(R.id.aimsicd_poc_link);
            setLink(tv, R.string.poc_link);

            //Disclaimer Link
            tv = v.findViewById(R.id.aimsicd_disclaimer_link);
            setLink(tv, R.string.disclaimer_link);

            //GitHub Contribution Link
            tv = v.findViewById(R.id.aimsicd_contribute_link);
            setLink(tv, R.string.aimsicd_github_link);

            //XDA Development Thread Link
//            tv = (TextView) v.findViewById(R.id.aimsicd_visit_xda_link);
//            tv.setMovementMethod(LinkMovementMethod.getInstance());
//            tv.setText(Html.fromHtml(getResources().getString(R.string.aimsicd_xda_link)));

            //WIP Release Link
            tv = v.findViewById(R.id.aimsicd_release_link);
            setLink(tv, R.string.aimsicd_release_link);

            //Changelog Link
            tv = v.findViewById(R.id.aimsicd_changelog_link);
            setLink(tv, R.string.aimsicd_changelog_link);

            //License Link
            tv = v.findViewById(R.id.aimsicd_license_link);
            setLink(tv, R.string.aimsicd_license_link);

            //Credits Link
            tv = v.findViewById(R.id.aimsicd_credits_link);
            setLink(tv, R.string.aimsicd_credits_link);

            //Donations Link
            tv = v.findViewById(R.id.aimsicd_donations_link);
            setLink(tv, R.string.aimsicd_donations_link);
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
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getBaseContext();
    }

    public AboutFragment() {
    }


}

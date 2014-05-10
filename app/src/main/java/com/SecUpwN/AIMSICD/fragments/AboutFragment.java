package com.SecUpwN.AIMSICD.fragments;

import com.SecUpwN.AIMSICD.R;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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

        RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.aboutView);
        int layouts = layout.getChildCount();

        //Proof of Concept Link
        TextView tv = (TextView) v.findViewById(R.id.aimsicd_poc_link);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(Html.fromHtml(getResources().getString(R.string.poc_link)));

        //Disclaimer Link
        tv = (TextView) v.findViewById(R.id.aimsicd_disclaimer_link);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(Html.fromHtml(getResources().getString(R.string.disclaimer_link)));

        //Github Contribution Link
        tv = (TextView) v.findViewById(R.id.aimsicd_contribute_link);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(Html.fromHtml(getResources().getString(R.string.aimsicd_github_link)));

        //XDA Development Thread Link
        tv = (TextView) v.findViewById(R.id.aimsicd_visit_xda_link);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(Html.fromHtml(getResources().getString(R.string.aimsicd_xda_link)));

        //WIP Release Link
        tv = (TextView) v.findViewById(R.id.aimsicd_release_link);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(Html.fromHtml(getResources().getString(R.string.aimsicd_release_link)));

        //Changelog Link
        tv = (TextView) v.findViewById(R.id.aimsicd_changelog_link);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(Html.fromHtml(getResources().getString(R.string.aimsicd_changelog_link)));

        //License Link
        tv = (TextView) v.findViewById(R.id.aimsicd_license_link);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(Html.fromHtml(getResources().getString(R.string.aimsicd_license_link)));

        //License Link
        tv = (TextView) v.findViewById(R.id.aimsicd_credits_link);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(Html.fromHtml(getResources().getString(R.string.aimsicd_credits_link)));

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getBaseContext();
    }

    public AboutFragment () {

    }


}

/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.secupwn.aimsicd.BuildConfig;
import com.secupwn.aimsicd.R;

import io.freefair.android.injection.annotation.InjectView;
import io.freefair.android.injection.annotation.XmlLayout;
import io.freefair.android.injection.app.InjectionAppCompatActivity;


@XmlLayout(R.layout.activity_about)
public class AboutActivity extends InjectionAppCompatActivity {

    @InjectView(R.id.aimsicd_credits_link)
    private Button btncredits;

    @InjectView(R.id.aimsicd_version)
    private TextView versionNumber;

    @InjectView(R.id.build_number)
    private TextView buildNumberTextView;

    @InjectView(R.id.git_sha)
    private TextView gitShaTextView;


    @InjectView(R.id.textViewIdle)
    private TextView idleTextView;
    @InjectView(R.id.textViewOk)
    private TextView okTextView;
    @InjectView(R.id.textViewMedium)
    private TextView mediumTextView;
    @InjectView(R.id.textViewHigh)
    private TextView highTextView;
    @InjectView(R.id.textViewDanger)
    private TextView dangerTextView;
    @InjectView(R.id.textViewSkull)
    private TextView skullTextView;
    private boolean closeAfterFinish = true;
    private Context mContext = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        versionNumber.setText(getString(R.string.app_version, BuildConfig.VERSION_NAME));

        String buildNumber = BuildConfig.BUILD_NUMBER;
        //noinspection ConstantConditions
        if (buildNumber == null) {
            buildNumber = getString(R.string.n_a);
        }
        buildNumberTextView.setText(getString(R.string.buildnumber, buildNumber));

        gitShaTextView.setText(getString(R.string.git_sha, BuildConfig.GIT_SHA));

        //GitHub WIKI Link
        View tv = findViewById(R.id.aimsicd_wiki_link);
        setLink(tv, R.string.aimsicd_wiki_link);

        //GitHub Website Link
        tv = findViewById(R.id.aimsicd_github_link);
        setLink(tv, R.string.aimsicd_github_link);

        //Disclaimer Link
        tv = findViewById(R.id.aimsicd_disclaimer_link);
        setLink(tv, R.string.disclaimer_link);

        //GitHub Contribution Link
        tv = findViewById(R.id.aimsicd_contribute_link);
        setLink(tv, R.string.aimsicd_contribute_link);

        //WIP Release Link
        tv = findViewById(R.id.aimsicd_release_link);
        setLink(tv, R.string.aimsicd_release_link);

        //Changelog Link
        tv = findViewById(R.id.aimsicd_changelog_link);
        setLink(tv, R.string.aimsicd_changelog_link);

        //License Link
        tv = findViewById(R.id.aimsicd_license_link);
        setLink(tv, R.string.aimsicd_license_link);

        idleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInfoDialog(Status.IDLE);
            }
        });

        okTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInfoDialog(Status.OK);
            }
        });

        mediumTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInfoDialog(Status.MEDIUM);
            }
        });

        highTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInfoDialog(Status.HIGH);
            }
        });

        dangerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInfoDialog(Status.DANGER);
            }
        });

        skullTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInfoDialog(Status.SKULL);
            }
        });

        btncredits.setOnClickListener(new View.OnClickListener() {
            AlertDialog mCreditsDialog;
            @Override
            public void onClick(View view) {
                final TextView mCreditsTitle = new TextView(getApplicationContext());
                final TextView mCreditsText = new TextView(mContext);
                final ScrollView mScrollView = new ScrollView(getApplicationContext());
                mCreditsText.setText(R.string.about_credits_content);
                mCreditsText.setTextSize(18);
                mCreditsText.setTypeface(Typeface.DEFAULT_BOLD);
                mCreditsTitle.setText(R.string.about_credits);
                mCreditsTitle.setTypeface(Typeface.DEFAULT_BOLD);
                mCreditsTitle.setTextSize(20);
                mCreditsTitle.setGravity(Gravity.CENTER_HORIZONTAL);
                mScrollView.addView(mCreditsText);
                Animation scrollTo = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.credits_dialog_scroll);
                mCreditsText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        closeAfterFinish = false;
                        mCreditsText.clearAnimation();
                    }
                });
                scrollTo.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (closeAfterFinish) {
                            mCreditsDialog.cancel();
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mCreditsText.setGravity(Gravity.CENTER_HORIZONTAL);
                mCreditsText.setMovementMethod(LinkMovementMethod.getInstance());
                scrollTo.setDuration(countLines(getResources().getString(R.string.about_credits_content)) * 500);
                mCreditsText.startAnimation(scrollTo);
                closeAfterFinish = true;
                mCreditsDialog = new AlertDialog.Builder(AboutActivity.this)
                        .setCustomTitle(mCreditsTitle)
                        .setPositiveButton(android.R.string.ok, null)
                        .setView(mScrollView)
                        .show();


            }
        });
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

    private void showInfoDialog(Status status) {
        new AlertDialog.Builder(this)
                .setIcon(status.getIcon())
                .setTitle(getString(R.string.status) + "\t" + getString(status.getName()))
                .setMessage(status.getDescription())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    public enum Status {
        IDLE(
                R.drawable.sense_idle,
                R.string.status_idle,
                R.string.status_idle_detail_info
        ),
        OK(
                R.drawable.sense_ok,
                R.string.status_ok,
                R.string.status_ok_detail_info
        ),
        MEDIUM(
                R.drawable.sense_medium,
                R.string.status_medium,
                R.string.status_medium_detail_info
        ),
        HIGH(
                R.drawable.sense_high,
                R.string.status_high,
                R.string.status_high_detail_info
        ),
        DANGER(
                R.drawable.sense_danger,
                R.string.status_danger,
                R.string.status_danger_detail_info
        ),
        SKULL(
                R.drawable.sense_skull,
                R.string.status_skull,
                R.string.status_skull_detail_info
        );

        @DrawableRes
        private int icon;

        @StringRes
        private int name;

        @StringRes
        private int description;

        Status(@DrawableRes int icon,
               @StringRes int name,
               @StringRes int description) {
            this.icon = icon;
            this.name = name;
            this.description = description;
        }

        @DrawableRes
        public int getIcon() {
            return icon;
        }

        @StringRes
        public int getName() {
            return name;
        }

        @StringRes
        public int getDescription() {
            return description;
        }
    }
    private  int countLines(String mString) {
        String[] lines = mString.split("\r\n|\r|\n");
        return  lines.length;
    }


}

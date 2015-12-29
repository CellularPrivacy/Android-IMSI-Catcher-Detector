/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.BuildConfig;
import com.SecUpwN.AIMSICD.R;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        versionNumber.setText(getString(R.string.app_version, BuildConfig.VERSION_NAME));
        buildNumberTextView.setText(getString(R.string.buildnumber, BuildConfig.BUILD_NUMBER));
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
                Intent i = new Intent(AboutActivity.this, CreditsRollActivity.class);
                startActivity(i);
            }
        });

    }

    private void showInfoDialog(Status status) {
        new AlertDialog.Builder(this)
                .setIcon(status.getIcon())
                .setTitle(getString(R.string.status) + "\t" + getString(status.getName()))
                .setMessage(status.getDescription())
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public enum Status {
        IDLE(
                R.drawable.sense_idle,
                R.string.status_idle,
                R.string.detail_info_idle
        ),
        OK(
                R.drawable.sense_ok,
                R.string.status_ok,
                R.string.detail_info_ok
        ),
        MEDIUM(
                R.drawable.sense_medium,
                R.string.status_medium,
                R.string.detail_info_medium
        ),
        HIGH(
                R.drawable.sense_high,
                R.string.status_high,
                R.string.detail_info_high
        ),
        DANGER(
                R.drawable.sense_danger,
                R.string.status_danger,
                R.string.detail_info_danger
        ),
        SKULL(
                R.drawable.sense_skull,
                R.string.status_skull,
                R.string.detail_info_skull
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
}
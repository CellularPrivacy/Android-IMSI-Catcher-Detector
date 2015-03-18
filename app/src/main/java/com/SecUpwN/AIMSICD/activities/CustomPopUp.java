/*
    Copyright 2015 Created by Paul Kinsella <paulkinsella29@yahoo.ie>

        Basic Pop Up Screen with Icon - Header - data

    To create a new dialog add this to the case block

            case 6://your case value for your custom popup
                 createPopUp(
                        "YOUR TITLE",
                        "YOUR HEADER",
                        "YOUR DATA"
                        ,getResources().getDrawable(R.drawable.sense_idle));//set your icon

                to call from any activity use:

                MiscUtils.startPopUpInfo(YOUR APP CONTEXT,0);

                The int value is your custom dialog value eg case 10:



 */


package com.SecUpwN.AIMSICD.activities;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;

public class CustomPopUp extends Activity {
    TextView about_tv_status,about_tv_data;
    ImageView about_icon_holder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_pop_up);

        about_icon_holder = (ImageView)findViewById(R.id.about_icon_holder);
        about_tv_status = (TextView)findViewById(R.id.about_tv_status);
        about_tv_data = (TextView)findViewById(R.id.about_tv_data);
        int mode = 0; //default
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mode = extras.getInt("display_mode");
        }



        switch (mode){
            case 0:
                createPopUp(
                        null,
                        "Status\tIdle",
                        getResources().getString(R.string.detail_info_idle)
                        ,getResources().getDrawable(R.drawable.sense_idle));
                    break;

            case 1:
                createPopUp(
                        null,
                        "Status\tNormal",
                        getResources().getString(R.string.detail_info_nomral)
                        ,getResources().getDrawable(R.drawable.sense_ok));
                break;
            case 2:
                createPopUp(
                        null,
                        "Status\tMedium",
                        getResources().getString(R.string.detail_info_medium)
                        ,getResources().getDrawable(R.drawable.sense_medium));
                break;
            case 3:
                createPopUp(
                        null,
                        "Status\tHigh",
                        getResources().getString(R.string.detail_info_high)
                        ,getResources().getDrawable(R.drawable.sense_high));
                break;
            case 4:
                createPopUp(
                        null,
                        "Status\tDanger",
                        getResources().getString(R.string.detail_info_danger)
                        ,getResources().getDrawable(R.drawable.sense_danger));
                break;
            case 5:
                createPopUp(
                        null,
                        "Status\tRun",
                        getResources().getString(R.string.detail_info_run)
                        ,getResources().getDrawable(R.drawable.sense_skull));
                break;

        }
    }

    public void createPopUp(String title,String header,String data,Drawable icon){
        if(title != null){
            setTitle(title);
        }

        if(header != null){
            about_tv_status.setText(header);
        }
        if(data != null){
            about_tv_data.setText(data);
        }
        if(icon != null){
            about_icon_holder.setImageDrawable(icon);
        }





    }

}

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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;

public class CustomPopUp extends Activity {
    TextView tv_popup_title,about_tv_status,about_tv_data;
    ImageView about_icon_holder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.about_pop_up);

        about_icon_holder = (ImageView)findViewById(R.id.about_icon_holder);
        about_tv_status = (TextView)findViewById(R.id.about_tv_status);
        about_tv_data = (TextView)findViewById(R.id.about_tv_data);
        tv_popup_title = (TextView)findViewById(R.id.tv_popup_title);

        int mode = 0; //default
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mode = extras.getInt("display_mode");
        }

        setFinishOnTouchOutside(true);
        switch (mode){
            case 0:
                createPopUp(
                        null,
                        getString(R.string.status) + "\t" + getString(R.string.idle),
                        getResources().getString(R.string.detail_info_idle),
                        getResources().getDrawable(R.drawable.sense_idle));
                break;

            case 1:
                createPopUp(
                        null,
                        getString(R.string.status) + "\t" + getString(R.string.normal),
                        getResources().getString(R.string.detail_info_nomral),
                        getResources().getDrawable(R.drawable.sense_ok));
                break;
            case 2:
                createPopUp(
                        null,
                        getString(R.string.status) + "\t" + getString(R.string.medium),
                        getResources().getString(R.string.detail_info_medium),
                        getResources().getDrawable(R.drawable.sense_medium));
                break;
            case 3:
                createPopUp(
                        null,
                        getString(R.string.status) + "\t" + getString(R.string.high),
                        getResources().getString(R.string.detail_info_high),
                        getResources().getDrawable(R.drawable.sense_high));
                break;
            case 4:
                createPopUp(
                        null,
                        getString(R.string.status) + "\t" + getString(R.string.danger),
                        getResources().getString(R.string.detail_info_danger),
                        getResources().getDrawable(R.drawable.sense_danger));
                break;
            case 5:
                createPopUp(
                        null,
                        getString(R.string.status) + "\t" + getString(R.string.run),
                        getResources().getString(R.string.detail_info_run)
                        ,getResources().getDrawable(R.drawable.sense_skull));
                break;

        }
    }

    public void createPopUp(String title,String header,String data,Drawable icon){
        if(title != null){
            tv_popup_title.setText(title);
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

    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                finish();
        }
        return true;
    }
}

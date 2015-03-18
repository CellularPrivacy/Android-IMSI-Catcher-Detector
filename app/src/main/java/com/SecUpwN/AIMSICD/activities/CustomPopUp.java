/*
    Copyright 2015 Created by Paul Kinsella <paulkinsella29@yahoo.ie>

        Basic Pop Up Screen with Icon - Header - data

    To create a new dialog add this to the case block

            case 6://your case value for your custom popup
                setTitle("My Title");
                about_icon_holder.setImageResource(R.drawable.white_skull);
                about_tv_status.setText("Your Header");
                about_tv_data.setText("This is a test");
                break;

                to call from any activity use:

                MiscUtils.startPopUpInfo(YOUR APP CONTEXT,0);

                The int value is your custom dialog value eg case 10:



 */


package com.SecUpwN.AIMSICD.activities;

import android.app.Activity;
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
                about_icon_holder.setImageResource(R.drawable.sense_idle);
                about_tv_status.setText("Status\tIdle");
                about_tv_data.setText(R.string.detail_info_idle);
                break;
            case 1:
                about_icon_holder.setImageResource(R.drawable.sense_ok);
                about_tv_status.setText("Status\tNormal");
                about_tv_data.setText(R.string.detail_info_nomral);
                break;
            case 2:
                about_icon_holder.setImageResource(R.drawable.sense_medium);
                about_tv_status.setText("Status\tMedium");
                about_tv_data.setText(R.string.detail_info_medium);
                break;
            case 3:
                about_icon_holder.setImageResource(R.drawable.sense_high);
                about_tv_status.setText("Status\tHigh");
                about_tv_data.setText(R.string.detail_info_high);
                break;
            case 4:
                about_icon_holder.setImageResource(R.drawable.sense_danger);
                about_tv_status.setText("Status\tDanger");
                about_tv_data.setText(R.string.detail_info_danger);
                break;
            case 5:
                about_icon_holder.setImageResource(R.drawable.sense_skull);
                about_tv_status.setText("Status\tRun");
                about_tv_data.setText(R.string.detail_info_run);
                break;

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_about_pop_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

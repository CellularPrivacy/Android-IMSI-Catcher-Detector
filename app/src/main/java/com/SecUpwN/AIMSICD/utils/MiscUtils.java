package com.SecUpwN.AIMSICD.utils;

import android.content.Context;
import android.content.Intent;

import com.SecUpwN.AIMSICD.activities.CustomPopUp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Paul Kinsella on 04/03/15.
 * paulkinsella29@yahoo.ie
 */

public class MiscUtils {

    public static String setAssetsString(Context context){
        BufferedReader reader = null;
        StringBuilder buildassets = new StringBuilder();
        try{
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open("CREDITS")));
            String rline = reader.readLine().replace("'","\\'").replace("\\n","");

            while (rline != null ){
                buildassets.append(rline).append("\n");
                rline = reader.readLine().replace("'","\\'").replace("\\n","");
            }
        } catch (Exception ee){
            ee.printStackTrace();
        }finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (Exception ee){
                    ee.printStackTrace();
                }
            }
        }

        return buildassets.toString();
    }

    public static void startPopUpInfo(Context context,int mode){
        Intent i = new Intent(context, CustomPopUp.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("display_mode",mode);
        context.startActivity(i);
    }

    public static String getCurrentTimeStamp(){

        Date now = new Date();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
        return timestamp;
    }
}

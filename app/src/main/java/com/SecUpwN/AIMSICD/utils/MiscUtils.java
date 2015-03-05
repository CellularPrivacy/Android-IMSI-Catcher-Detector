package com.SecUpwN.AIMSICD.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStreamReader;

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
        }catch (Exception ee){
            System.out.println("Out>>> " + ee);
        }finally {
            if(reader != null){
                try {
                    reader.close();
                }catch (Exception ee){
                    System.out.println("Out>>> " + ee);
                }
            }
        }



        return buildassets.toString();
    }
}

/*
 * Copyright 2014 KC Ochibili
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.SecUpwN.AIMSICD.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


/**
 *  Description:    This class simplifies calls to SharedPreferences in a line of code.
 *                  It can also do more like: saving a list of Strings or Integers and Saving images.
 *                  All in 1 line of code!
 *
 *  Example usage:
 *
 *  ChangeLog:      2015-03-02  E:V:A   Added from repo (see links below)
 *
 *  See:
 *    http://stackoverflow.com/questions/5734721/android-shared-preferences
 *    https://github.com/kcochibili/TinyDB--Android-Shared-Preferences-Turbo/
 *
 *  Usage:
 *
 *  1)
 *      import com.SecUpwN.AIMSICD.utils.TinyDB;
 *      ...
 *      TinyDB tinydb = TinyDB.getInstance(); //Possibly mContext if already declared in AppAIMSICD via method init(AppContext).
 *
 *      tinydb.putInt("clickCount", 2);
 *      tinydb.putFloat("xPoint", 3.6f);
 *      tinydb.putLong("userCount", 39832L);
 *      tinydb.putString("userName", "john");
 *      tinydb.putBoolean("isUserMale", true);
 *      tinydb.putList("MyUsers", mUsersArray);
 *      tinydb.putImagePNG("DropBox/WorkImages", "MeAtlunch.png", lunchBitmap);
 *
 *  2)
 *      import com.SecUpwN.AIMSICD.utils.TinyDB;
 *      ...
 *      TinyDB.getInstance().putInt("clickCount", 2);
 *      TinyDB.getInstance().putFloat("xPoint", 3.6f);
 *
 *      and etc.
 *
 *
 */
public class TinyDB {
    Context mContext;
    SharedPreferences preferences;
    String DEFAULT_APP_IMAGEDATA_DIRECTORY;
    File mFolder = null;
    public static String lastImagePath = "";

    private TinyDB() {}

    public void init(Context appContext) {
        mContext = appContext;
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public Bitmap getImage(String path) {
        Bitmap theGottenBitmap = null;
        try {
            theGottenBitmap = BitmapFactory.decodeFile(path);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return theGottenBitmap;
    }

    /**
     * Returns the String path of the last image that was saved with this Object
     *
     */
    public String getSavedImagePath() {
        return lastImagePath;
    }

    /**
     * Returns the String path of the last image that was saved with this
     * tnydbobj
     *
     * @param theFolder     - the folder path dir you want to save it to e.g "DropBox/WorkImages"
     * @param theImageName  - the name you want to assign to the image file e.g "MeAtlunch.png"
     *
     */
    public String putImagePNG(String theFolder, String theImageName,
                              Bitmap theBitmap) {
        this.DEFAULT_APP_IMAGEDATA_DIRECTORY = theFolder;
        String mFullPath = setupFolderPath(theImageName);
        saveBitmapPNG(mFullPath, theBitmap);
        lastImagePath = mFullPath;
        return mFullPath;
    }

    public Boolean putImagePNGwithfullPath(String fullPath, Bitmap theBitmap){
        return saveBitmapPNG(fullPath, theBitmap);
    }

    private String setupFolderPath(String imageName) {
        File sdcard_path = new File(mContext.getExternalFilesDir(null) + File.separator);
        mFolder = new File(sdcard_path, DEFAULT_APP_IMAGEDATA_DIRECTORY);
        if (!mFolder.exists()) {
            if (!mFolder.mkdirs()) {
                Log.e("AIMSICD", "TinyDB:setupFolderPath() - Default Save Path Creation Error");
                // Toast("Default Save Path Creation Error");
            }
        }
        String savePath = mFolder.getPath() + '/' + imageName;
        return savePath;
    }

    private boolean saveBitmapPNG(String strFileName, Bitmap bitmap) {
        if (strFileName == null || bitmap == null)
            return false;
        boolean bSuccess1 = false;
        boolean bSuccess2;
        boolean bSuccess3;
        File saveFile = new File(strFileName);

        if (saveFile.exists()) {
            if (!saveFile.delete())
                return false;
        }

        try {
            bSuccess1 = saveFile.createNewFile();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        OutputStream out = null;
        try {
            out = new FileOutputStream(saveFile);
            bSuccess2 = bitmap.compress(CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
            bSuccess2 = false;
        }
        try {
            if (out != null) {
                out.flush();
                out.close();
                bSuccess3 = true;
            } else
                bSuccess3 = false;

        } catch (IOException e) {
            e.printStackTrace();
            bSuccess3 = false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        return (bSuccess1 && bSuccess2 && bSuccess3);
    }

    public int getInt(String key) {
        return preferences.getInt(key, 0);
    }

    public long getLong(String key) {
        return preferences.getLong(key, 0l);
    }

    public String getString(String key) {
        return preferences.getString(key, "");
    }

    public double getDouble(String key) {
        String number = getString(key);
        try {
            double value = Double.parseDouble(number);
            return value;
        }
        catch(NumberFormatException e)
        {
            return 0;
        }
    }

    public void putInt(String key, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void putLong(String key, long value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public void putDouble(String key, double value) {
        putString(key, String.valueOf(value));
    }

    public void putString(String key, String value) {

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void putList(String key, ArrayList<String> marray) {

        SharedPreferences.Editor editor = preferences.edit();
        String[] mystringlist = marray.toArray(new String[marray.size()]);
        // the comma like character used below is not a comma it is the SINGLE
        // LOW-9 QUOTATION MARK unicode 201A and unicode 2017 they are used for
        // separating the items in the list
        editor.putString(key, TextUtils.join("‚‗‚", mystringlist));
        editor.apply();
    }

    public ArrayList<String> getList(String key) {
        // the comma like character used below is not a comma it is the SINGLE
        // LOW-9 QUOTATION MARK unicode 201A and unicode 2017 they are used for
        // separating the items in the list
        String[] mylist = TextUtils
                .split(preferences.getString(key, ""), "‚‗‚");
        return new ArrayList<>(Arrays.asList(mylist));
    }

    public void putListInt(String key, ArrayList<Integer> marray) {
        SharedPreferences.Editor editor = preferences.edit();
        Integer[] mystringlist = marray.toArray(new Integer[marray.size()]);
        // the comma like character used below is not a comma it is the SINGLE
        // LOW-9 QUOTATION MARK unicode 201A and unicode 2017 they are used for
        // separating the items in the list
        editor.putString(key, TextUtils.join("‚‗‚", mystringlist));
        editor.apply();
    }

    public ArrayList<Integer> getListInt(String key) {
        // the comma like character used below is not a comma it is the SINGLE
        // LOW-9 QUOTATION MARK unicode 201A and unicode 2017 they are used for
        // separating the items in the list
        String[] mylist = TextUtils
                .split(preferences.getString(key, ""), "‚‗‚");
        ArrayList<String> gottenlist = new ArrayList<>(
                Arrays.asList(mylist));
        ArrayList<Integer> gottenlist2 = new ArrayList<>();
        for (int i = 0; i < gottenlist.size(); i++) {
            gottenlist2.add(Integer.parseInt(gottenlist.get(i)));
        }

        return gottenlist2;
    }

    public void putListDouble(String key, ArrayList<Double> marray) {
        SharedPreferences.Editor editor = preferences.edit();
        Double[] mystringlist = marray.toArray(new Double[marray.size()]);
        // the comma like character used below is not a comma it is the SINGLE
        // LOW-9 QUOTATION MARK unicode 201A and unicode 2017 they are used for
        // separating the items in the list
        editor.putString(key, TextUtils.join("â€šâ€—â€š", mystringlist));
        editor.apply();
    }

    public ArrayList<Double> getListDouble(String key) {
        // the comma like character used below is not a comma it is the SINGLE
        // LOW-9 QUOTATION MARK unicode 201A and unicode 2017 they are used for
        // separating the items in the list
        String[] mylist = TextUtils
                .split(preferences.getString(key, ""), "â€šâ€—â€š");
        ArrayList<String> gottenlist = new ArrayList<>(
                Arrays.asList(mylist));
        ArrayList<Double> gottenlist2 = new ArrayList<>();
        for (int i = 0; i < gottenlist.size(); i++) {
            gottenlist2.add(Double.parseDouble(gottenlist.get(i)));
        }

        return gottenlist2;
    }

    public void putListBoolean(String key, ArrayList<Boolean> marray){
        ArrayList<String> origList = new ArrayList<>();
        for(Boolean b : marray){
            if(b){
                origList.add("true");
            }else{
                origList.add("false");
            }
        }
        putList(key, origList);
    }

    public ArrayList<Boolean> getListBoolean(String key) {
        ArrayList<String> origList = getList(key);
        ArrayList<Boolean> mBools = new ArrayList<>();
        for(String b : origList){
            if(b.equals("true")){
                mBools.add(true);
            }else{
                mBools.add(false);
            }
        }
        return mBools;
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    public void putFloat(String key, float value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public float getFloat(String key) {
        return preferences.getFloat(key, 0f);
    }

    public void remove(String key) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.apply();
    }

    public Boolean deleteImage(String path){
        File tobedeletedImage = new File(path);
        Boolean isDeleted = tobedeletedImage.delete();
        return isDeleted;
    }

    public void clear() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    public Map<String, ?> getAll() {
        return preferences.getAll();
    }

    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    private static class InstanceHolder {
        private static final TinyDB INSTANCE = new TinyDB();
    }

    public static TinyDB getInstance() {
        return InstanceHolder.INSTANCE;
    }

}


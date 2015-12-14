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
import android.preference.PreferenceManager;

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
*/
public class TinyDB {

    private Context mContext;
    private SharedPreferences preferences;

    //prevent direct initialisation
    private TinyDB() {
    }

    public void init(Context appContext) {
        mContext = appContext;
        preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public String getString(String key) {
        return preferences.getString(key, "");
    }

    public void putInt(String key, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    public void remove(String key) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.apply();
    }

    public void clear() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    public Map<String, ?> getAll() {
        return preferences.getAll();
    }

    private static class InstanceHolder {
        private static final TinyDB INSTANCE = new TinyDB();
    }

    public static TinyDB getInstance() {
        return InstanceHolder.INSTANCE;
    }
}


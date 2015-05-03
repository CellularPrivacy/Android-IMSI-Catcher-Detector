/* Android IMSI Catcher Detector
 *      Copyright (C) 2015
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You may obtain a copy of the License at
 *      https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/LICENSE
 */
package com.SecUpwN.AIMSICD.utils;


import android.app.Activity;
import android.os.AsyncTask;

import com.SecUpwN.AIMSICD.AppAIMSICD;

import java.lang.ref.WeakReference;

public abstract class BaseAsyncTask<TParams, TProgress, TResult> extends AsyncTask<TParams, TProgress, TResult> {
    protected AppAIMSICD mApp;
    protected WeakReference<Activity> mWeakReferenceActivity;

    public BaseAsyncTask(Activity activity) {
        mWeakReferenceActivity = new WeakReference<>(activity);
        mApp = (AppAIMSICD) activity.getApplication();
    }

    public void setActivity(Activity activity) {
        if (activity == null) {
            mWeakReferenceActivity.clear();
            onActivityDetached();
        } else {
            onActivityAttached();
            mWeakReferenceActivity = new WeakReference<>(activity);
        }
    }

    protected void onActivityAttached() {
    }

    protected void onActivityDetached() {
    }

    @Override
    protected void onPreExecute() {
        mApp.addTask(mWeakReferenceActivity.get(), this);
    }

    @Override
    protected void onPostExecute(TResult result) {
        mApp.removeTask(this);
    }

    @Override
    protected void onCancelled() {
        mApp.removeTask(this);
    }

    protected Activity getActivity() {
        return mWeakReferenceActivity.get();
    }

}

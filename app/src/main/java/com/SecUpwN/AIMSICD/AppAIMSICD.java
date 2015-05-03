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
package com.SecUpwN.AIMSICD;


import android.app.Activity;
import android.app.Application;
import android.util.Log;
import android.util.SparseArray;

import com.SecUpwN.AIMSICD.constants.TinyDbKeys;
import com.SecUpwN.AIMSICD.utils.BaseAsyncTask;
import com.SecUpwN.AIMSICD.utils.TinyDB;

import java.util.ArrayList;
import java.util.List;

public class AppAIMSICD extends Application {
    final static String TAG = "AppAIMSICD";

    /**
     * Maps between an activity class name and the list of currently running
     * AsyncTasks that were spawned while it was active.
     */
    private SparseArray<List<BaseAsyncTask<?, ?, ?>>> mActivityTaskMap;

    public AppAIMSICD() {
        mActivityTaskMap = new SparseArray<>();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TinyDB.getInstance().init(getApplicationContext());
        TinyDB.getInstance().putBoolean(TinyDbKeys.FINISHED_LOAD_IN_MAP, true);
    }

    public void removeTask(BaseAsyncTask<?, ?, ?> pTask) {
        int key;
        for (int i = 0; i < mActivityTaskMap.size(); i++) {
            key = mActivityTaskMap.keyAt(i);
            List<BaseAsyncTask<?, ?, ?>> tasks = mActivityTaskMap.get(key);
            for (BaseAsyncTask<?, ?, ?> lTask : tasks) {
                if (lTask.equals(pTask)) {
                    tasks.remove(lTask);
                    if (BuildConfig.DEBUG) {
                        Log.v(TAG, "BaseTask removed:" + pTask.toString());
                    }
                    break;
                }
            }
            if (tasks.size() == 0) {
                mActivityTaskMap.remove(key);
                return;
            }
        }
    }

    public void addTask(Activity activity, BaseAsyncTask<?, ?, ?> pTask) {
        if (activity == null) {
            return;
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "BaseTask addTask activity:" + activity.getClass().getCanonicalName());
        }
        int key = activity.getClass().getCanonicalName().hashCode();
        List<BaseAsyncTask<?, ?, ?>> tasks = mActivityTaskMap.get(key);
        if (tasks == null) {
            tasks = new ArrayList<>();
            mActivityTaskMap.put(key, tasks);
        }
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "BaseTask added:" + pTask.toString());
        }
        tasks.add(pTask);
    }

    public void detach(Activity activity) {
        if (activity == null) {
            return;
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "BaseTask detach:" + activity.getClass().getCanonicalName());
        }

        List<BaseAsyncTask<?, ?, ?>> tasks = mActivityTaskMap.get(activity.getClass().getCanonicalName().hashCode());
        if (tasks != null) {
            for (BaseAsyncTask<?, ?, ?> task : tasks) {
                task.setActivity(null);
            }
        }
    }

    public void attach(Activity activity) {
        if (activity == null) {
            return;
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "BaseTask attach:" + activity.getClass().getCanonicalName());
        }
        List<BaseAsyncTask<?, ?, ?>> tasks = mActivityTaskMap.get(activity.getClass().getCanonicalName().hashCode());
        if (tasks != null) {
            for (BaseAsyncTask<?, ?, ?> task : tasks) {
                task.setActivity(activity);
            }
        }
    }
}


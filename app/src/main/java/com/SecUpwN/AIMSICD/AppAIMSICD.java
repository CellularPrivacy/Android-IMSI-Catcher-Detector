/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD;


import android.app.Activity;
import android.app.Application;
import android.util.Log;
import android.util.SparseArray;

import com.SecUpwN.AIMSICD.constants.TinyDbKeys;
import com.SecUpwN.AIMSICD.utils.BaseAsyncTask;
import com.SecUpwN.AIMSICD.utils.TinyDB;
// DO NOT REMOVE BELOW COMMENTED-OUT CODE BEFORE ASKING!
//import com.squareup.leakcanary.LeakCanary;

import java.util.ArrayList;
import java.util.List;

public class AppAIMSICD extends Application {
    final static String TAG = "AIMSICD";
    final static String mTAG = "AppAIMSICD";

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
        // DO NOT REMOVE BELOW COMMENTED-OUT CODE BEFORE ASKING!
        //LeakCanary.install(this);
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
                        Log.v(TAG, mTAG + ": BaseTask removed:" + pTask.toString());
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
            Log.d(TAG, mTAG + ": BaseTask addTask activity:" + activity.getClass().getCanonicalName());
        }
        int key = activity.getClass().getCanonicalName().hashCode();
        List<BaseAsyncTask<?, ?, ?>> tasks = mActivityTaskMap.get(key);
        if (tasks == null) {
            tasks = new ArrayList<>();
            mActivityTaskMap.put(key, tasks);
        }
        if (BuildConfig.DEBUG) {
            Log.v(TAG, mTAG + ": BaseTask added:" + pTask.toString());
        }
        tasks.add(pTask);
    }

    public void detach(Activity activity) {
        if (activity == null) {
            return;
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, mTAG + ": BaseTask detach:" + activity.getClass().getCanonicalName());
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
            Log.d(TAG, mTAG + ": BaseTask attach:" + activity.getClass().getCanonicalName());
        }
        List<BaseAsyncTask<?, ?, ?>> tasks = mActivityTaskMap.get(activity.getClass().getCanonicalName().hashCode());
        if (tasks != null) {
            for (BaseAsyncTask<?, ?, ?> task : tasks) {
                task.setActivity(activity);
            }
        }
    }
}
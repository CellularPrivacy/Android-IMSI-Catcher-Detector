/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD;


import android.app.Activity;
import android.app.Application;
import android.util.SparseArray;

import com.SecUpwN.AIMSICD.constants.TinyDbKeys;
import com.SecUpwN.AIMSICD.utils.BaseAsyncTask;
import com.SecUpwN.AIMSICD.utils.TinyDB;
import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.List;

import io.freefair.android.util.logging.AndroidLogger;
import io.freefair.android.util.logging.Logger;

// DO NOT REMOVE BELOW COMMENTED-OUT CODE BEFORE ASKING!
//import com.squareup.leakcanary.LeakCanary;

public class AppAIMSICD extends Application {

    private final Logger log = AndroidLogger.forClass(AppAIMSICD.class);

    private OkHttpClient okHttpClient;

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

        okHttpClient = new OkHttpClient();
    }

    public void removeTask(BaseAsyncTask<?, ?, ?> pTask) {
        int key;
        for (int i = 0; i < mActivityTaskMap.size(); i++) {
            key = mActivityTaskMap.keyAt(i);
            List<BaseAsyncTask<?, ?, ?>> tasks = mActivityTaskMap.get(key);
            for (BaseAsyncTask<?, ?, ?> lTask : tasks) {
                if (lTask.equals(pTask)) {
                    tasks.remove(lTask);
                    log.verbose("BaseTask removed:" + pTask.toString());

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

        log.debug("BaseTask addTask activity:" + activity.getClass().getCanonicalName());

        int key = activity.getClass().getCanonicalName().hashCode();
        List<BaseAsyncTask<?, ?, ?>> tasks = mActivityTaskMap.get(key);
        if (tasks == null) {
            tasks = new ArrayList<>();
            mActivityTaskMap.put(key, tasks);
        }
        log.verbose("BaseTask added:" + pTask.toString());
        tasks.add(pTask);
    }

    public void detach(Activity activity) {
        if (activity == null) {
            return;
        }

        log.debug("BaseTask detach:" + activity.getClass().getCanonicalName());

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
        log.debug("BaseTask attach:" + activity.getClass().getCanonicalName());

        List<BaseAsyncTask<?, ?, ?>> tasks = mActivityTaskMap.get(activity.getClass().getCanonicalName().hashCode());
        if (tasks != null) {
            for (BaseAsyncTask<?, ?, ?> task : tasks) {
                task.setActivity(activity);
            }
        }
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }
}
/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd;

import android.app.Activity;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;

import com.secupwn.aimsicd.constants.TinyDbKeys;
import com.secupwn.aimsicd.data.DefaultDataTransaction;
import com.secupwn.aimsicd.enums.Status;
import com.secupwn.aimsicd.utils.BaseAsyncTask;
import com.secupwn.aimsicd.utils.TinyDB;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.freefair.android.injection.annotation.Inject;
import io.freefair.android.injection.app.InjectionAppCompatActivity;
import io.freefair.android.injection.app.InjectionApplication;
import io.freefair.android.injection.modules.AndroidLoggerModule;
import io.freefair.android.injection.modules.OkHttpModule;
import io.freefair.android.util.logging.Logger;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class AndroidIMSICatcherDetector extends InjectionApplication {

    private static WeakReference<AndroidIMSICatcherDetector> instance;

    public static AndroidIMSICatcherDetector getInstance() {
        return instance.get();
    }

    private Status currentStatus;
    @Inject
    private Logger log;

    /**
     * Maps between an activity class name and the list of currently running
     * AsyncTasks that were spawned while it was active.
     */
    private SparseArray<List<BaseAsyncTask<?, ?, ?>>> mActivityTaskMap;

    public AndroidIMSICatcherDetector() {
        mActivityTaskMap = new SparseArray<>();
    }

    @Override
    public void onCreate() {
        instance = new WeakReference<>(this);
        addModule(new AndroidLoggerModule());
        addModule(OkHttpModule.withCache(this));
        super.onCreate();

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm.setDefaultConfiguration(realmConfiguration);
        final Realm realm = Realm.getDefaultInstance();

        realm.executeTransactionAsync(
                new DefaultDataTransaction(),
                new Realm.Transaction.OnSuccess() {
                    @Override
                    public void onSuccess() {
                        log.debug("Loading default data successful");
                        realm.close();
                    }
                },
                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        log.error("Error loading default data", error);
                        realm.close();
                    }
                }
        );

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

    public void attach(InjectionAppCompatActivity activity) {
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

    /**
     * Changes the current status, this will also trigger a local broadcast event
     * if the new status is different from the previous one
     */
    public void setCurrentStatus(Status status, boolean vibrate, int minVibrateLevel) {
        if (status == null) {
            status = Status.IDLE;
        }
        if (status != currentStatus) {
            Intent intent = new Intent("StatusChange");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            if (vibrate && status.ordinal() >= minVibrateLevel) {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(100);
            }
        }
        currentStatus = status;
    }

    /**
     * Returns the current status
     */
    public Status getStatus() {
        if (currentStatus == null) {
            return Status.IDLE;
        }
        return currentStatus;
    }

}

/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
 
/*  This was introduced by Aussie in the Pull Request Commit: 
 * https://github.com/xLaMbChOpSx/Android-IMSI-Catcher-Detector/commit/6d8719ab356a3ecbd0b526a9ded0cabb17ab2021
 * 
 * Where he writes: 
 * ""
 * Advanced Cell Fragment added to display the Neighboring Cell information in two ways: Firstly
 * through telephony manager methods which does not work on Samsung Devices, a fallback is available
 * through the methods developed by Alexey and will display if these are successful.
 * Ciphering Indicator also uses Alexey's methods and will display on Samsung devices.
 * ""
 */

package com.secupwn.aimsicd.ui.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.adapters.BaseInflaterAdapter;
import com.secupwn.aimsicd.adapters.CardItemData;
import com.secupwn.aimsicd.adapters.CellCardInflater;
import com.secupwn.aimsicd.rilexecutor.RilExecutor;
import com.secupwn.aimsicd.service.AimsicdService;
import com.secupwn.aimsicd.service.CellTracker;
import com.secupwn.aimsicd.utils.Cell;
import com.secupwn.aimsicd.utils.Helpers;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.freefair.android.injection.annotation.InjectView;
import io.freefair.android.injection.annotation.XmlLayout;
import io.freefair.android.injection.app.InjectionFragment;

/**
 *  Description:    This class updates the CellInfo fragment. This is also known as
 *                  the Neighboring Cells info, which is using the MultiRilClient to
 *                  show neighboring cells on the older Samsung Galaxy S2/3 series.
 *                  It's refresh rate is controlled in the settings by:
 *
 *                  arrays.xml:
 *                      pref_refresh_entries    (the names)
 *                      pref_refresh_values     (the values in seconds)
 *
 *
 *  Dependencies:   Seem that this is intimately connected to: CellTracker.java service...
 *
 *
 *  TODO:   1)  Use an IF check, in order not to run the MultiRilClient on non supported devices
 *              as this will cause excessive logcat spam.
 *  TODO:   2) Might wanna make the refresh rate lower/higher depending on support
 *
 */
@XmlLayout(R.layout.fragment_cell_info)
public class CellInfoFragment extends InjectionFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final int STOCK_REQUEST = 1;
    public static final int SAMSUNG_MULTIRIL_REQUEST = 2;

    private AimsicdService mAimsicdService;
    private RilExecutor rilExecutor;
    private boolean mBound;
    private Context mContext;
    private final Handler timerHandler = new Handler();

    private List<Cell> neighboringCells;

    @InjectView(R.id.list_view)
    private ListView lv;

    @InjectView(R.id.neighboring_cells)
    private TextView mNeighboringCells;

    @InjectView(R.id.neighboring_number)
    private TextView mNeighboringTotal;

    @InjectView(R.id.neighboring_total)
    private TableRow mNeighboringTotalView;

    @InjectView(R.id.ciphering_indicator_title)
    private TextView mCipheringIndicatorLabel;

    @InjectView(R.id.ciphering_indicator)
    private TextView mCipheringIndicator;

    @InjectView(R.id.swipeRefreshLayout)
    private SwipeRefreshLayout swipeRefreshLayout;


    private final Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            updateUI();
            timerHandler.postDelayed(this, CellTracker.REFRESH_RATE);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mBound) {
            // Bind to LocalService
            Intent intent = new Intent(mContext, AimsicdService.class);
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

        // Refresh display if preference (pref_refresh_values) is not set to manual (0)
        // For automatic it is "1" and defined in:
        //    CellTracker.java :: onSharedPreferenceChanged()
        if (CellTracker.REFRESH_RATE != 0) {
            timerHandler.postDelayed(timerRunnable, 0);
            Helpers.msgShort(mContext, mContext.getString(R.string.refreshing_every) + " " +
                    TimeUnit.MILLISECONDS.toSeconds(CellTracker.REFRESH_RATE) + " " + mContext.getString(R.string.seconds));
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = getActivity().getBaseContext();
        // Bind to LocalService
        Intent intent = new Intent(mContext, AimsicdService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mBound) {
            mContext.unbindService(mConnection);
            mBound = false;
        }
        timerHandler.removeCallbacks(timerRunnable);
    }

    /**
     * Service Connection to bind the activity to the service
     */
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mAimsicdService = ((AimsicdService.AimscidBinder) service).getService();
            rilExecutor = mAimsicdService.getRilExecutor();
            mBound = true;
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onRefresh() {
        updateUI();
        swipeRefreshLayout.setRefreshing(false);
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            updateUI();
        }
    }

    private void updateUI() {
        if (mBound && rilExecutor.mMultiRilCompatible) {
                new CellAsyncTask().execute(SAMSUNG_MULTIRIL_REQUEST);
            } else {
            new CellAsyncTask().execute(STOCK_REQUEST);
        }
    }

    void updateCipheringIndicator() {
        final List<String> list = rilExecutor.getCipheringInfo();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (list != null) {
                    mCipheringIndicatorLabel.setVisibility(View.VISIBLE);
                    mCipheringIndicator.setVisibility(View.VISIBLE);
                    mCipheringIndicator.setText(TextUtils.join("\n", list));
                }
            }
        });
    }

    boolean getStockNeighboringCells() {
        if (mBound) {
            neighboringCells = mAimsicdService.getCellTracker().updateNeighboringCells();
            return neighboringCells.size() > 0;
        }

        return false;
    }

    void updateStockNeighboringCells() {
        mNeighboringTotal.setText(String.valueOf(neighboringCells.size()));
        if (neighboringCells.size() != 0) {

            BaseInflaterAdapter<CardItemData> adapter
                    = new BaseInflaterAdapter<>(new CellCardInflater());
            int i = 1;
            int total = neighboringCells.size();
            for (Cell cell : neighboringCells) {
                CardItemData data = new CardItemData(cell, i++ + " / " + total);
                adapter.addItem(data, false);
            }
            lv.setAdapter(adapter);
            mNeighboringCells.setVisibility(View.GONE);
            mNeighboringTotalView.setVisibility(View.VISIBLE);
        }
    }

    void updateNeighboringCells() {
        final List<String> list = rilExecutor.getNeighbors();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (list != null) {
                    mNeighboringCells.setText(TextUtils.join("\n", list));
                    mNeighboringCells.setVisibility(View.VISIBLE);
                    mNeighboringTotalView.setVisibility(View.GONE);
                }
            }
        });
    }

    void getSamSungMultiRil() {
        if (mBound && rilExecutor.mMultiRilCompatible) {
            new CellAsyncTask().execute(SAMSUNG_MULTIRIL_REQUEST);
        }
    }

    private class CellAsyncTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... type) {
            switch (type[0]) {
                case STOCK_REQUEST:
                    return getStockNeighboringCells();
                case SAMSUNG_MULTIRIL_REQUEST:
                    if (mBound) {
                        updateNeighboringCells();
                        updateCipheringIndicator();
                    }
                    break;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                updateStockNeighboringCells();
            } else {
                getSamSungMultiRil();
            }
        }
    }
}

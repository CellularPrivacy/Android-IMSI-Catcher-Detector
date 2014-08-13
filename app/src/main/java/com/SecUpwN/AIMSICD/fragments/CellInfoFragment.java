package com.SecUpwN.AIMSICD.fragments;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.BaseInflaterAdapter;
import com.SecUpwN.AIMSICD.adapters.CardItemData;
import com.SecUpwN.AIMSICD.adapters.CellCardInflater;
import com.SecUpwN.AIMSICD.rilexecutor.DetectResult;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.utils.Cell;
import com.SecUpwN.AIMSICD.utils.Helpers;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CellInfoFragment extends Fragment {

    private AimsicdService mAimsicdService;
    private boolean mBound;
    private Context mContext;
    private Activity mActivity;
    private final Handler timerHandler = new Handler();

    //Layout items
    private ListView lv;
    private TextView mNeighbouringCells;
    private TextView mNeighbouringTotal;
    private TableRow mNeighbouringTotalView;
    private TextView mCipheringIndicatorLabel;
    private TextView mCipheringIndicator;

    private final Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            updateUI();
            timerHandler.postDelayed(this, AimsicdService.REFRESH_RATE);
        }
    };

    public CellInfoFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getBaseContext();
        mActivity = activity;
        // Bind to LocalService
        Intent intent = new Intent(mContext, AimsicdService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

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

        //Refresh display if preference is not set to manual
        if (AimsicdService.REFRESH_RATE != 0) {
            timerHandler.postDelayed(timerRunnable, 0);
            Helpers.msgShort(mContext, "Refreshing every "
                    + TimeUnit.MILLISECONDS.toSeconds(AimsicdService.REFRESH_RATE) + " seconds");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.cell_fragment,
                container, false);
        if (view != null) {
            lv = (ListView) view.findViewById(R.id.list_view);
            mNeighbouringCells = (TextView) view.findViewById(R.id.neighbouring_cells);
            mNeighbouringTotal = (TextView) view.findViewById(R.id.neighbouring_number);
            mNeighbouringTotalView = (TableRow) view.findViewById(R.id.neighbouring_total);
            mCipheringIndicatorLabel = (TextView) view.findViewById(R.id.ciphering_indicator_title);
            mCipheringIndicator = (TextView) view.findViewById(R.id.ciphering_indicator);

            Button refresh = (Button) view.findViewById(R.id.button_refresh);
            refresh.setOnClickListener(new btnClick());
        }
        return view;
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
            mBound = true;
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private class btnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Helpers.msgShort(mContext, "Refreshing now...");
            updateUI();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            updateUI();
        }
    }

    private void updateUI() {
        if (mBound) {
            List<Cell> neighboringCells = mAimsicdService.updateNeighbouringCells();
            mNeighbouringTotal
                    .setText(String.valueOf(neighboringCells.size()));
            if (neighboringCells.size() != 0) {

                BaseInflaterAdapter<CardItemData> adapter
                        = new BaseInflaterAdapter<>(
                        new CellCardInflater());
                int i = 1;
                int total = neighboringCells.size();
                for (Cell cell : neighboringCells) {
                    CardItemData data = new CardItemData(cell, i++ + " / " + total);
                    adapter.addItem(data, false);
                }
                lv.setAdapter(adapter);
                mNeighbouringCells.setVisibility(View.GONE);
                mNeighbouringTotalView.setVisibility(View.VISIBLE);
            } else {
                //Try SamSung MultiRil Implementation
                DetectResult rilStatus = mAimsicdService.getRilExecutorStatus();
                if (rilStatus.available) {
                    new RequestOemInfoTask().execute();
                }
            }
        }
    }

    void updateCipheringIndicator() {
        final List<String> list = mAimsicdService.getCipheringInfo();
        mActivity.runOnUiThread(new Runnable() {
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

    void updateNeighbouringCells() {
        final List<String> list = mAimsicdService.getNeighbours();
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (list != null) {
                    mNeighbouringCells.setText(TextUtils.join("\n", list));
                    mNeighbouringCells.setVisibility(View.VISIBLE);
                    mNeighbouringTotalView.setVisibility(View.GONE);
                }
            }
        });
    }

    private class RequestOemInfoTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... string) {
            if (!mBound) {
                return null;
            }
            updateNeighbouringCells();
            updateCipheringIndicator();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}

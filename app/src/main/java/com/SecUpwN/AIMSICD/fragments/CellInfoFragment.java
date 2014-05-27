package com.SecUpwN.AIMSICD.fragments;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.rilexecutor.DetectResult;
import com.SecUpwN.AIMSICD.service.AimsicdService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class CellInfoFragment extends Fragment {
    private AimsicdService mAimsicdService;

    private TextView mNeighbouringCells;
    private TextView mNeighbouringTotal;
    private TextView mNeighbouringTotalLabel;
    private TextView mCipheringIndicatorLabel;
    private TextView mCipheringIndicator;

    private boolean mBound;
    private Context mContext;

    public CellInfoFragment () {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.cell_fragment,
                container, false);
        if (view != null) {
            mNeighbouringCells = (TextView) view.findViewById(R.id.neighbouring_cells);

            mNeighbouringTotal = (TextView) view.findViewById(R.id.neighbouring_number);
            mNeighbouringTotalLabel = (TextView) view.findViewById(R.id.neighbouring_number_label);
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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getBaseContext();
        // Bind to LocalService
        Intent intent = new Intent(mContext, AimsicdService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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
            mAimsicdService.updateNeighbouringCells();
            Map neighborMapUMTS = mAimsicdService.getUMTSNeighbouringCells();
            Map neighborMapGSM = mAimsicdService.getGSMNeighbouringCells();

            mNeighbouringTotal
                    .setText(String.valueOf(mAimsicdService.getNeighbouringCellSize()) + "/"
                            + String.valueOf(neighborMapUMTS.size() + neighborMapGSM.size()));

            StringBuilder sb = new StringBuilder();
            int i = 1;
            if (!neighborMapUMTS.isEmpty())
                for (Object key : neighborMapUMTS.keySet()) {
                    sb.append(i)
                            .append(") PSC: ")
                            .append(key)
                            .append(" RSCP: ")
                            .append(neighborMapUMTS.get(key)) //TS25.133 section 9.1.1.3
                            .append(" dBm");
                    if (i < neighborMapUMTS.size() + neighborMapGSM.size())
                        sb.append("\n");
                    i++;
                }
            if (!neighborMapGSM.isEmpty())
                for (Object key : neighborMapGSM.keySet()) {
                    sb.append(i)
                            .append(") LAC-CID: ")
                            .append(key)
                            .append(" RSSI: ")
                            .append(neighborMapGSM.get(key)) //TS27.007 section 8.5
                            .append(" dBm");
                    if (i < neighborMapUMTS.size() + neighborMapGSM.size())
                        sb.append("\n");
                    i++;
                }
            mNeighbouringCells.setText(sb);

            //Try SamSung MultiRil Implementation
            if (neighborMapGSM.isEmpty() && neighborMapUMTS.isEmpty()) {
                    DetectResult rilStatus = mAimsicdService.getRilExecutorStatus();
                    if (rilStatus.available) {
                        //new RequestOemInfoTask().execute();
                        new RequestOemInfoTask().execute();
                    }
            } else {
                mNeighbouringTotal.setVisibility(View.VISIBLE);
                mNeighbouringTotalLabel.setVisibility(View.VISIBLE);
            }
        }
    }

    void updateCipheringIndicator() {
        final List<String> list = mAimsicdService.getCipheringInfo();
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

    void updateNeighbouringCells() {
        final List<String> list = mAimsicdService.getNeighbours();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (list != null) {
                    mNeighbouringCells.setText(TextUtils.join("\n", list));
                    mNeighbouringTotal.setVisibility(View.GONE);
                    mNeighbouringTotalLabel.setVisibility(View.GONE);
                }
            }
        });
    }

    private class RequestOemInfoTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... string) {
            if (!mBound) return null;
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

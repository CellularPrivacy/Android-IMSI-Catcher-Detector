package com.SecUpwN.AIMSICD;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CellInfoFragment extends Fragment {
    private AimsicdService mAimsicdService;
    private View mView;
    private TextView mNeighbouringCells;
    private TextView mNeighbouringTotal;
    private TextView mNeighbouringTotalLabel;
    private TextView mCipheringIndicatorLabel;
    private TextView mCipheringIndicator;

    private boolean mBound;
    private Context mContext;

    private Map<Integer,Integer> mNeighborMapUMTS = new HashMap<Integer,Integer>();
    private Map<String,Integer> mNeighborMapGSM = new HashMap<String,Integer>();

    public CellInfoFragment (Context context) {
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mView= inflater.inflate(R.layout.cell_fragment,
                container, false);
        mNeighbouringCells = (TextView) mView.findViewById(R.id.neighbouring_cells);
        mNeighbouringTotal = (TextView) mView.findViewById(R.id.neighbouring_number);
        mNeighbouringTotalLabel = (TextView) mView.findViewById(R.id.neighbouring_number_label);
        mCipheringIndicatorLabel = (TextView) mView.findViewById(R.id.ciphering_indicator_title);
        mCipheringIndicator = (TextView) mView.findViewById(R.id.ciphering_indicator);
        Button refresh = (Button) mView.findViewById(R.id.button_refresh);
        refresh.setOnClickListener(new btnClick());

        return mView;
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
        // Bind to LocalService
        Intent intent = new Intent(mContext, AimsicdService.class);
        //Start Service before binding to keep it resident when activity is destroyed
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
            mNeighborMapUMTS = mAimsicdService.getUMTSNeighbouringCells();
            mNeighborMapGSM = mAimsicdService.getGSMNeighbouringCells();

            mNeighbouringTotal
                    .setText(String.valueOf(mAimsicdService.getNeighbouringCellSize()) + "/"
                            + String.valueOf(mNeighborMapUMTS.size() + mNeighborMapGSM.size()));

            StringBuilder sb = new StringBuilder();
            int i = 1;
            if (!mNeighborMapUMTS.isEmpty())
                for (Object key : mNeighborMapUMTS.keySet()) {
                    sb.append(i)
                            .append(") PSC: ")
                            .append(key)
                            .append(" RSCP: ")
                            .append(mNeighborMapUMTS.get(key)) //TS25.133 section 9.1.1.3
                            .append(" dBm");
                    if (i < mNeighborMapUMTS.size() + mNeighborMapGSM.size())
                        sb.append("\n");
                    i++;
                }
            if (!mNeighborMapGSM.isEmpty())
                for (Object key : mNeighborMapGSM.keySet()) {
                    sb.append(i)
                            .append(") LAC-CID: ")
                            .append(key)
                            .append(" RSSI: ")
                            .append(mNeighborMapGSM.get(key)) //TS27.007 section 8.5
                            .append(" dBm");
                    if (i < mNeighborMapUMTS.size() + mNeighborMapGSM.size())
                        sb.append("\n");
                    i++;
                }
            mNeighbouringCells.setText(sb);

            //Try SamSung MultiRil Implementation
            if (mNeighborMapGSM.isEmpty() && mNeighborMapUMTS.isEmpty()) {
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

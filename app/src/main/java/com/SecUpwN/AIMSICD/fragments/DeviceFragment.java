package com.SecUpwN.AIMSICD.fragments;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.utils.Helpers;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class DeviceFragment extends Fragment {

    private final String TAG = "AIMSICD";

    private AimsicdService mAimsicdService;
    private View mView;
    private boolean mBound;

    private Context mContext;

    Handler timerHandler = new Handler();

    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            updateUI();
            timerHandler.postDelayed(this, AimsicdService.REFRESH_RATE);
        }
    };

    public DeviceFragment() {}

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getBaseContext();
        // Bind to LocalService
        Intent intent = new Intent(mContext, AimsicdService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        //Refresh display if preference is not set to manual
        if (AimsicdService.REFRESH_RATE != 0) {
            timerHandler.postDelayed(timerRunnable, 0);
            Helpers.sendMsg(mContext, "Refreshing every "
                    + TimeUnit.MILLISECONDS.toSeconds(AimsicdService.REFRESH_RATE) + " seconds");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mView= inflater.inflate(R.layout.device,
                container, false);
        updateUI();
        return mView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            updateUI();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
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

    @Override
    public void onPause() {
        super.onPause();
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
            Log.e(TAG, "Service Disconnected");
            mBound = false;
        }
    };

    private void updateUI() {
        TextView content;
        TableLayout tableLayout;
        TableRow tr;
        if (mBound) {
            tableLayout = (TableLayout) mView.findViewById(R.id.mainView);
            switch (mAimsicdService.getPhoneID()) {
                case TelephonyManager.PHONE_TYPE_GSM: {
                    content = (TextView) mView.findViewById(R.id.network_lac);
                    content.setText(String.valueOf(mAimsicdService.getLAC(true)));
                    tr = (TableRow) mView.findViewById(R.id.gsm_cellid);
                    tr.setVisibility(View.VISIBLE);
                    content = (TextView) mView.findViewById(R.id.network_cellid);
                    content.setText(String.valueOf(mAimsicdService.getCellId()));
                    break;
                }
                case TelephonyManager.PHONE_TYPE_CDMA: {
                    int layouts = tableLayout.getChildCount();
                    for(int i = 0; i < layouts;  i++){
                        TableRow row = (TableRow) tableLayout.getChildAt(i);
                        if (row != null) {
                            if (row.getTag().equals("cdma")) {
                                row.setVisibility(View.VISIBLE);
                            } else if( row.getTag().equals("gsm_network")) {
                                row.setVisibility(View.GONE);
                            }
                        }
                    }
                    content = (TextView) mView.findViewById(R.id.network_netid);
                    content.setText(String.valueOf(mAimsicdService.getLAC(true)));
                    content = (TextView) mView.findViewById(R.id.network_sysid);
                    content.setText(String.valueOf(mAimsicdService.getSID()));
                    content = (TextView) mView.findViewById(R.id.network_baseid);
                    content.setText(String.valueOf(mAimsicdService.getCellId()));
                    double[] location = mAimsicdService.getLastLocation();
                    content = (TextView) mView.findViewById(R.id.network_cmda_lat);
                    content.setText(String.valueOf(location[0]));
                    content = (TextView) mView.findViewById(R.id.network_cmda_long);
                    content.setText(String.valueOf(location[1]));
                    break;
                }
            }

            if (mAimsicdService.getNetID(true) == TelephonyManager.NETWORK_TYPE_LTE) {
                tr = (TableRow) mView.findViewById(R.id.lte_timing_advance);
                tr.setVisibility(View.VISIBLE);
                content = (TextView) mView.findViewById(R.id.network_lte_timing_advance);
                content.setText(String.valueOf(mAimsicdService.getLteTimingAdvance()));
            } else {
                tr = (TableRow) mView.findViewById(R.id.lte_timing_advance);
                tr.setVisibility(View.GONE);
            }

            content = (TextView) mView.findViewById(R.id.sim_country);
            content.setText(mAimsicdService.getSimCountry(false));
            content = (TextView) mView.findViewById(R.id.sim_operator_id);
            content.setText(mAimsicdService.getSimOperator(false));
            content = (TextView) mView.findViewById(R.id.sim_operator_name);
            content.setText(mAimsicdService.getSimOperatorName(false));
            content = (TextView) mView.findViewById(R.id.sim_imsi);
            content.setText(mAimsicdService.getSimSubs(false));
            content = (TextView) mView.findViewById(R.id.sim_serial);
            content.setText(mAimsicdService.getSimSerial(false));

            content = (TextView) mView.findViewById(R.id.device_type);
            content.setText(mAimsicdService.getPhoneType(false));
            content = (TextView) mView.findViewById(R.id.device_imei);
            content.setText(mAimsicdService.getIMEI(false));
            content = (TextView) mView.findViewById(R.id.device_version);
            content.setText(mAimsicdService.getIMEIv(false));
            content = (TextView) mView.findViewById(R.id.device_number);
            content.setText(mAimsicdService.getPhoneNumber(false));
            content = (TextView) mView.findViewById(R.id.network_name);
            content.setText(mAimsicdService.getNetworkName(false));
            content = (TextView) mView.findViewById(R.id.network_code);
            content.setText(mAimsicdService.getSmmcMcc(false));
            content = (TextView) mView.findViewById(R.id.network_type);
            content.setText(mAimsicdService.getNetworkTypeName());

            content = (TextView) mView.findViewById(R.id.data_activity);
            content.setText(mAimsicdService.getActivityDesc());
            content = (TextView) mView.findViewById(R.id.data_status);
            content.setText(mAimsicdService.getStateDesc());
            content = (TextView) mView.findViewById(R.id.network_roaming);
            content.setText(mAimsicdService.isRoaming());
        }
    }
}

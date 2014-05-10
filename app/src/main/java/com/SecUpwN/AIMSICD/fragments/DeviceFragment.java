package com.SecUpwN.AIMSICD.fragments;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.service.AimsicdService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class DeviceFragment extends Fragment {

    private final String TAG = "AIMSICD";

    private AimsicdService mAimsicdService;
    private View mView;
    private boolean mBound;

    private Context mContext;

    public DeviceFragment() {}

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        mContext = getActivity().getBaseContext();
        // Bind to LocalService
        Intent intent = new Intent(mContext, AimsicdService.class);
        //Start Service before binding to keep it resident when activity is destroyed
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mView= inflater.inflate(R.layout.main,
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
    public void onStart() {
        super.onStart();
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
                    content.setText(mAimsicdService.getLAC(true));
                    content = (TextView) mView.findViewById(R.id.network_cellid);
                    content.setText(mAimsicdService.getCellId());
                    break;
                }
                case TelephonyManager.PHONE_TYPE_CDMA: {
                    int layouts = tableLayout.getChildCount();
                    for(int i = 0; i < layouts;  i++){
                        TableRow row = (TableRow) tableLayout.getChildAt(i);
                        if (row.getTag().equals("cdma")) {
                            row.setVisibility(View.GONE);
                        } else if( row.getTag().equals("gsm_network")) {
                            row.setVisibility(View.VISIBLE);
                        }
                    }
                    content = (TextView) mView.findViewById(R.id.network_netid);
                    content.setText(mAimsicdService.getLAC(true));
                    content = (TextView) mView.findViewById(R.id.network_sysid);
                    content.setText(mAimsicdService.getSID());
                    content = (TextView) mView.findViewById(R.id.network_baseid);
                    content.setText(mAimsicdService.getCellId());
                    break;
                }
            }

            if (mAimsicdService.getNetID(true) == TelephonyManager.NETWORK_TYPE_LTE) {
                content = (TextView) mView.findViewById(R.id.network_lte_timing_advance);
                content.setText(mAimsicdService.getLteTimingAdvance());
                tr = (TableRow) mView.findViewById(R.id.lte_timing_advance);
                tr.setVisibility(View.VISIBLE);
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

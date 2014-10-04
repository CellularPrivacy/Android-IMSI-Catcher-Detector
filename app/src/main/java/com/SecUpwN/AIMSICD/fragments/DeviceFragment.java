package com.SecUpwN.AIMSICD.fragments;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.utils.Device;
import com.SecUpwN.AIMSICD.utils.Helpers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

public class DeviceFragment extends Fragment {

    private final String TAG = "AIMSICD";

    private View mView;

    private AimsicdService mAimsicdService;
    private boolean mBound;
    private Context mContext;

    public DeviceFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getBaseContext();
        // Bind to LocalService
        Intent intent = new Intent(mContext, AimsicdService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.device,
                container, false);
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mBound) {
            // Bind to LocalService
            Intent intent = new Intent(mContext, AimsicdService.class);
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

        mContext.registerReceiver(mMessageReceiver, new IntentFilter(AimsicdService.UPDATE_DISPLAY));
        updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        mContext.unregisterReceiver(mMessageReceiver);
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

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.getBoolean("update")) {
                Helpers.msgShort(mContext, "Refreshing display");
                updateUI();
            }
        }
    };

    private void updateUI() {
        TextView content;
        TableRow tr;
        if (mBound) {
            mAimsicdService.getCellTracker().refreshDevice();
            Device mDevice = mAimsicdService.getCellTracker().getDevice();
            switch (mDevice.getPhoneID()) {
                case TelephonyManager.PHONE_TYPE_GSM: {
                    content = (TextView) mView.findViewById(R.id.network_lac);
                    content.setText(String.valueOf(mAimsicdService.getCell().getLAC()));
                    tr = (TableRow) mView.findViewById(R.id.gsm_cellid);
                    tr.setVisibility(View.VISIBLE);
                    content = (TextView) mView.findViewById(R.id.network_cellid);
                    content.setText(String.valueOf(mAimsicdService.getCell().getCID()));
                    break;
                }
                case TelephonyManager.PHONE_TYPE_CDMA: {
                    tr = (TableRow) mView.findViewById(R.id.cdma_netid);
                    tr.setVisibility(View.VISIBLE);
                    content = (TextView) mView.findViewById(R.id.network_netid);
                    content.setText(String.valueOf(mAimsicdService.getCell().getLAC()));
                    tr = (TableRow) mView.findViewById(R.id.cdma_sysid);
                    tr.setVisibility(View.VISIBLE);
                    content = (TextView) mView.findViewById(R.id.network_sysid);
                    content.setText(String.valueOf(mAimsicdService.getCell().getSID()));
                    tr = (TableRow) mView.findViewById(R.id.cdma_baseid);
                    tr.setVisibility(View.VISIBLE);
                    content = (TextView) mView.findViewById(R.id.network_baseid);
                    content.setText(String.valueOf(mAimsicdService.getCell().getCID()));
                    break;
                }
            }

            if (mAimsicdService.getCell().getTimingAdvance() != Integer.MAX_VALUE) {
                tr = (TableRow) mView.findViewById(R.id.lte_timing_advance);
                tr.setVisibility(View.VISIBLE);
                content = (TextView) mView.findViewById(R.id.network_lte_timing_advance);
                content.setText(String.valueOf(mAimsicdService.getCell().getTimingAdvance()));
            } else {
                tr = (TableRow) mView.findViewById(R.id.lte_timing_advance);
                tr.setVisibility(View.GONE);
            }

            if (mAimsicdService.getCell().getPSC() != Integer.MAX_VALUE) {
                content = (TextView) mView.findViewById(R.id.network_psc);
                content.setText(String.valueOf(mAimsicdService.getCell().getPSC()));
                tr = (TableRow) mView.findViewById(R.id.primary_scrambling_code);
                tr.setVisibility(View.VISIBLE);
            }

            content = (TextView) mView.findViewById(R.id.sim_country);
            content.setText(mDevice.getSimCountry());
            content = (TextView) mView.findViewById(R.id.sim_operator_id);
            content.setText(mDevice.getSimOperator());
            content = (TextView) mView.findViewById(R.id.sim_operator_name);
            content.setText(mDevice.getSimOperatorName());
            content = (TextView) mView.findViewById(R.id.sim_imsi);
            content.setText(mDevice.getSimSubs());
            content = (TextView) mView.findViewById(R.id.sim_serial);
            content.setText(mDevice.getSimSerial());

            content = (TextView) mView.findViewById(R.id.device_type);
            content.setText(mDevice.getPhoneType());
            content = (TextView) mView.findViewById(R.id.device_imei);
            content.setText(mDevice.getIMEI());
            content = (TextView) mView.findViewById(R.id.device_version);
            content.setText(mDevice.getIMEIv());
            content = (TextView) mView.findViewById(R.id.network_name);
            content.setText(mDevice.getNetworkName());
            content = (TextView) mView.findViewById(R.id.network_code);
            content.setText(mDevice.getMncMcc());
            content = (TextView) mView.findViewById(R.id.network_type);
            content.setText(mDevice.getNetworkTypeName());

            content = (TextView) mView.findViewById(R.id.data_activity);
            content.setText(mDevice.getDataActivity());
            content = (TextView) mView.findViewById(R.id.data_status);
            content.setText(mDevice.getDataState());
            content = (TextView) mView.findViewById(R.id.network_roaming);
            content.setText(mDevice.isRoaming());
        }
    }
}

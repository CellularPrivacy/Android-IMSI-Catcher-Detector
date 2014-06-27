package com.SecUpwN.AIMSICD.fragments;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.service.AimsicdService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
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

public class DeviceFragment extends Fragment {

    private final String TAG = "AIMSICD";

    private View mView;

    private AimsicdService mAimsicdService;
    private boolean mBound;
    private Context mContext;

    public DeviceFragment() {
    }

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
            switch (mAimsicdService.mDevice.getPhoneID()) {
                case TelephonyManager.PHONE_TYPE_GSM: {
                    content = (TextView) mView.findViewById(R.id.network_lac);
                    content.setText(String.valueOf(mAimsicdService.mDevice.getLac()));
                    tr = (TableRow) mView.findViewById(R.id.gsm_cellid);
                    tr.setVisibility(View.VISIBLE);
                    content = (TextView) mView.findViewById(R.id.network_cellid);
                    content.setText(String.valueOf(mAimsicdService.mDevice.getCellId()));
                    break;
                }
                case TelephonyManager.PHONE_TYPE_CDMA: {
                    int layouts = tableLayout.getChildCount();
                    for (int i = 0; i < layouts; i++) {
                        TableRow row = (TableRow) tableLayout.getChildAt(i);
                        if (row != null) {
                            if (row.getTag().equals("cdma")) {
                                row.setVisibility(View.VISIBLE);
                            } else if (row.getTag().equals("gsm_network")) {
                                row.setVisibility(View.GONE);
                            }
                        }
                    }
                    content = (TextView) mView.findViewById(R.id.network_netid);
                    content.setText(String.valueOf(mAimsicdService.mDevice.getLac()));
                    content = (TextView) mView.findViewById(R.id.network_sysid);
                    content.setText(String.valueOf(mAimsicdService.mDevice.getSID()));
                    content = (TextView) mView.findViewById(R.id.network_baseid);
                    content.setText(String.valueOf(mAimsicdService.mDevice.getCellId()));
                    Location location = mAimsicdService.mDevice.getLastLocation();
                    content = (TextView) mView.findViewById(R.id.network_cmda_lat);
                    content.setText(String.valueOf(location.getLatitude()));
                    content = (TextView) mView.findViewById(R.id.network_cmda_long);
                    content.setText(String.valueOf(location.getLongitude()));
                    break;
                }
            }

            if (mAimsicdService.mDevice.getNetID() == TelephonyManager.NETWORK_TYPE_LTE) {
                tr = (TableRow) mView.findViewById(R.id.lte_timing_advance);
                tr.setVisibility(View.VISIBLE);
                content = (TextView) mView.findViewById(R.id.network_lte_timing_advance);
                content.setText(String.valueOf(mAimsicdService.mDevice.getLteTimingAdvance()));
            } else {
                tr = (TableRow) mView.findViewById(R.id.lte_timing_advance);
                tr.setVisibility(View.GONE);
            }

            content = (TextView) mView.findViewById(R.id.sim_country);
            content.setText(mAimsicdService.mDevice.getSimCountry());
            content = (TextView) mView.findViewById(R.id.sim_operator_id);
            content.setText(mAimsicdService.mDevice.getSimOperator());
            content = (TextView) mView.findViewById(R.id.sim_operator_name);
            content.setText(mAimsicdService.mDevice.getSimOperatorName());
            content = (TextView) mView.findViewById(R.id.sim_imsi);
            content.setText(mAimsicdService.mDevice.getSimSubs());
            content = (TextView) mView.findViewById(R.id.sim_serial);
            content.setText(mAimsicdService.mDevice.getSimSerial());

            content = (TextView) mView.findViewById(R.id.device_type);
            content.setText(mAimsicdService.mDevice.getPhoneType());
            content = (TextView) mView.findViewById(R.id.device_imei);
            content.setText(mAimsicdService.mDevice.getIMEI());
            content = (TextView) mView.findViewById(R.id.device_version);
            content.setText(mAimsicdService.mDevice.getIMEIv());
            content = (TextView) mView.findViewById(R.id.network_name);
            content.setText(mAimsicdService.mDevice.getNetworkName());
            content = (TextView) mView.findViewById(R.id.network_code);
            content.setText(mAimsicdService.mDevice.getSmmcMcc());
            content = (TextView) mView.findViewById(R.id.network_type);
            content.setText(mAimsicdService.mDevice.getNetworkTypeName());

            content = (TextView) mView.findViewById(R.id.data_activity);
            content.setText(mAimsicdService.mDevice.getDataActivity());
            content = (TextView) mView.findViewById(R.id.data_status);
            content.setText(mAimsicdService.mDevice.getDataState());
            content = (TextView) mView.findViewById(R.id.network_roaming);
            content.setText(mAimsicdService.mDevice.isRoaming());
        }
    }
}

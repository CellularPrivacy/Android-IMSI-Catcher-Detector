/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TableRow;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.utils.Device;
import com.SecUpwN.AIMSICD.utils.Helpers;
import com.SecUpwN.AIMSICD.widget.HighlightTextView;
import com.kaichunlin.transition.animation.AnimationManager;

import io.freefair.android.injection.annotation.Inject;
import io.freefair.android.injection.annotation.XmlLayout;
import io.freefair.android.injection.app.InjectionFragment;
import io.freefair.android.util.logging.Logger;

@XmlLayout(R.layout.fragment_device)
public class DeviceFragment extends InjectionFragment {

    @Inject
    private Logger log;

    private AimsicdService mAimsicdService;
    private boolean mBound;
    private Context mContext;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = getActivity().getBaseContext();
        // Bind to LocalService
        Intent intent = new Intent(mContext, AimsicdService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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
            log.error("Service Disconnected");
            mBound = false;
        }
    };

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.getBoolean("update")) {
                Helpers.msgShort(mContext, context.getString(R.string.refreshing_display));
                updateUI();
            }
        }
    };

    private void updateUI() {
        HighlightTextView content;
        TableRow tr;
        if (mBound) {
            final AnimationManager ani=new AnimationManager();

            mAimsicdService.getCellTracker().refreshDevice();
            Device mDevice = mAimsicdService.getCellTracker().getDevice();
            switch (mDevice.getPhoneID()) {

                case TelephonyManager.PHONE_TYPE_NONE:  // Maybe bad!
                case TelephonyManager.PHONE_TYPE_SIP:   // Maybe bad!
                case TelephonyManager.PHONE_TYPE_GSM: {
                    content = (HighlightTextView)  getView().findViewById(R.id.network_lac);
                    content.updateText(String.valueOf(mAimsicdService.getCell().getLAC()), ani);
                    tr = (TableRow) getView().findViewById(R.id.gsm_cellid);
                    tr.setVisibility(View.VISIBLE);
                    content = (HighlightTextView)  getView().findViewById(R.id.network_cellid);
                    content.updateText(String.valueOf(mAimsicdService.getCell().getCID()), ani);
                    break;
                }

                case TelephonyManager.PHONE_TYPE_CDMA: {
                    tr = (TableRow) getView().findViewById(R.id.cdma_netid);
                    tr.setVisibility(View.VISIBLE);
                    content = (HighlightTextView)  getView().findViewById(R.id.network_netid);
                    content.updateText(String.valueOf(mAimsicdService.getCell().getLAC()), ani);
                    tr = (TableRow) getView().findViewById(R.id.cdma_sysid);
                    tr.setVisibility(View.VISIBLE);
                    content = (HighlightTextView)  getView().findViewById(R.id.network_sysid);
                    content.updateText(String.valueOf(mAimsicdService.getCell().getSID()), ani);
                    tr = (TableRow) getView().findViewById(R.id.cdma_baseid);
                    tr.setVisibility(View.VISIBLE);
                    content = (HighlightTextView)  getView().findViewById(R.id.network_baseid);
                    content.updateText(String.valueOf(mAimsicdService.getCell().getCID()), ani);
                    break;
                }
            }

            if (mAimsicdService.getCell().getTimingAdvance() != Integer.MAX_VALUE) {
                tr = (TableRow) getView().findViewById(R.id.lte_timing_advance);
                tr.setVisibility(View.VISIBLE);
                content = (HighlightTextView)  getView().findViewById(R.id.network_lte_timing_advance);
                content.updateText(String.valueOf(mAimsicdService.getCell().getTimingAdvance()), ani);
            } else {
                tr = (TableRow) getView().findViewById(R.id.lte_timing_advance);
                tr.setVisibility(View.GONE);
            }

            if (mAimsicdService.getCell().getPSC() != Integer.MAX_VALUE) {
                content = (HighlightTextView)  getView().findViewById(R.id.network_psc);
                content.updateText(String.valueOf(mAimsicdService.getCell().getPSC()), ani);
                tr = (TableRow) getView().findViewById(R.id.primary_scrambling_code);
                tr.setVisibility(View.VISIBLE);
            }

            String notAvailable = getString(R.string.n_a);

            content = (HighlightTextView)  getView().findViewById(R.id.sim_country);
            content.updateText(mDevice.getSimCountry().orElse(notAvailable), ani);
            content = (HighlightTextView)  getView().findViewById(R.id.sim_operator_id);
            content.updateText(mDevice.getSimOperator().orElse(notAvailable), ani);
            content = (HighlightTextView) getView().findViewById(R.id.sim_operator_name);
            content.updateText(mDevice.getSimOperatorName().orElse(notAvailable), ani);
            content = (HighlightTextView)  getView().findViewById(R.id.sim_imsi);
            content.updateText(mDevice.getSimSubs().orElse(notAvailable), ani);
            content = (HighlightTextView)  getView().findViewById(R.id.sim_serial);
            content.updateText(mDevice.getSimSerial().orElse(notAvailable), ani);

            content = (HighlightTextView)  getView().findViewById(R.id.device_type);
            content.updateText(mDevice.getPhoneType(), ani);
            content = (HighlightTextView)  getView().findViewById(R.id.device_imei);
            content.updateText(mDevice.getIMEI(), ani);
            content = (HighlightTextView)  getView().findViewById(R.id.device_version);
            content.updateText(mDevice.getIMEIv(), ani);
            content = (HighlightTextView)  getView().findViewById(R.id.network_name);
            content.updateText(mDevice.getNetworkName(), ani);
            content = (HighlightTextView)  getView().findViewById(R.id.network_code);
            content.updateText(mDevice.getMncMcc(), ani);
            content = (HighlightTextView)  getView().findViewById(R.id.network_type);
            content.updateText(mDevice.getNetworkTypeName(), ani);

            content = (HighlightTextView)  getView().findViewById(R.id.data_activity);
            content.updateText(mDevice.getDataActivity(), ani);
            content = (HighlightTextView)  getView().findViewById(R.id.data_status);
            content.updateText(mDevice.getDataState(), ani);
            content = (HighlightTextView)  getView().findViewById(R.id.network_roaming);
            content.updateText(mDevice.isRoaming(), ani);

            ani.startAnimation(5000);
        }
    }
}

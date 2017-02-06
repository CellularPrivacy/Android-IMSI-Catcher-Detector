/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.ui.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TableRow;

import com.kaichunlin.transition.animation.AnimationManager;
import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.service.AimsicdService;
import com.secupwn.aimsicd.service.CellTracker;
import com.secupwn.aimsicd.ui.widget.HighlightTextView;
import com.secupwn.aimsicd.utils.Cell;
import com.secupwn.aimsicd.utils.Device;
import com.secupwn.aimsicd.utils.Helpers;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.freefair.android.injection.annotation.Inject;
import io.freefair.android.injection.annotation.InjectView;
import io.freefair.android.injection.annotation.XmlLayout;
import io.freefair.android.injection.app.InjectionFragment;
import io.freefair.android.util.logging.Logger;

@XmlLayout(R.layout.fragment_device)
public class DeviceFragment extends InjectionFragment implements SwipeRefreshLayout.OnRefreshListener {

    @Inject
    private Logger log;

    @Inject
    OkHttpClient okHttpClient;

    @InjectView(R.id.swipeRefresLayout)
    private SwipeRefreshLayout swipeRefreshLayout;

    private AimsicdService mAimsicdService;
    private boolean mBound;
    private Context mContext;

    @InjectView(R.id.network_lac)
    private HighlightTextView locationAreaCodeView;

    @InjectView(R.id.gsm_cellid)
    private TableRow gsmCellIdTableRow;

    @InjectView(R.id.network_cellid)
    private HighlightTextView cellIdView;

    @InjectView(R.id.cdma_netid)
    private TableRow cdmaNetworkIdRow;
    @InjectView(R.id.cdma_sysid)
    private TableRow cdmaSystemIdRow;
    @InjectView(R.id.cdma_baseid)
    private TableRow cdmaBaseIdRow;

    @InjectView(R.id.network_netid)
    private HighlightTextView networkIdView;
    @InjectView(R.id.network_sysid)
    private HighlightTextView systemIdView;
    @InjectView(R.id.network_baseid)
    private HighlightTextView baseIdView;

    @InjectView(R.id.lte_timing_advance)
    private TableRow lteTimingAdvanceRow;

    @InjectView(R.id.network_lte_timing_advance)
    private HighlightTextView lteTimingAdvanceView;

    @InjectView(R.id.primary_scrambling_code)
    private TableRow primaryScramblingCodeRow;

    @InjectView(R.id.network_psc)
    private HighlightTextView primaryScramblingCodeView;

    @InjectView(R.id.sim_country)
    private HighlightTextView simCountryView;
    @InjectView(R.id.sim_operator_id)
    private HighlightTextView simOperatorIdView;
    @InjectView(R.id.sim_operator_name)
    private HighlightTextView simOperatorNameView;
    @InjectView(R.id.sim_imsi)
    private HighlightTextView simImsiView;
    @InjectView(R.id.sim_serial)
    private HighlightTextView simSerialView;
    @InjectView(R.id.device_type)
    private HighlightTextView deviceTypeView;
    @InjectView(R.id.device_imei)
    private HighlightTextView deviceImeiView;
    @InjectView(R.id.device_version)
    private HighlightTextView deviceImeiVersionView;
    @InjectView(R.id.network_name)
    private HighlightTextView networkNameView;
    @InjectView(R.id.network_code)
    private HighlightTextView networkCodeView;
    @InjectView(R.id.network_type)
    private HighlightTextView networkTypeView;
    @InjectView(R.id.data_activity)
    private HighlightTextView dataActivityTypeView;
    @InjectView(R.id.data_status)
    private HighlightTextView dataStatusView;
    @InjectView(R.id.network_roaming)
    private HighlightTextView networkRoamingView;

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
    public void onPause() {
        super.onPause();
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

    private void updateUI() {
        if (mBound) {
            final AnimationManager ani = new AnimationManager();

            mAimsicdService.getCellTracker().refreshDevice();
            Device mDevice = mAimsicdService.getCellTracker().getDevice();
            switch (mDevice.getPhoneId()) {

                case TelephonyManager.PHONE_TYPE_NONE:  // Maybe bad!
                case TelephonyManager.PHONE_TYPE_SIP:   // Maybe bad!
                case TelephonyManager.PHONE_TYPE_GSM: {
                    locationAreaCodeView.updateText(String.valueOf(mAimsicdService.getCell().getLocationAreaCode()), ani);
                    gsmCellIdTableRow.setVisibility(View.VISIBLE);
                    cellIdView.updateText(String.valueOf(mAimsicdService.getCell().getCellId()), ani);
                    break;
                }

                case TelephonyManager.PHONE_TYPE_CDMA: {
                    cdmaNetworkIdRow.setVisibility(View.VISIBLE);
                    networkIdView.updateText(String.valueOf(mAimsicdService.getCell().getLocationAreaCode()), ani);

                    cdmaSystemIdRow.setVisibility(View.VISIBLE);
                    systemIdView.updateText(String.valueOf(mAimsicdService.getCell().getSid()), ani);

                    cdmaBaseIdRow.setVisibility(View.VISIBLE);
                    baseIdView.updateText(String.valueOf(mAimsicdService.getCell().getCellId()), ani);
                    break;
                }
                default:
                    log.error("unknown phone type: " + mDevice.getPhoneId());
            }

            if (mAimsicdService.getCell().getTimingAdvance() != Integer.MAX_VALUE) {
                lteTimingAdvanceRow.setVisibility(View.VISIBLE);
                lteTimingAdvanceView.updateText(String.valueOf(mAimsicdService.getCell().getTimingAdvance()), ani);
            } else {
                lteTimingAdvanceRow.setVisibility(View.GONE);
            }

            if (mAimsicdService.getCell().getPrimaryScramblingCode() != Integer.MAX_VALUE) {
                primaryScramblingCodeView.updateText(String.valueOf(mAimsicdService.getCell().getPrimaryScramblingCode()), ani);
                primaryScramblingCodeRow.setVisibility(View.VISIBLE);
            }

            String notAvailable = getString(R.string.n_a);

            simCountryView.updateText(mDevice.getSimCountry().orElse(notAvailable), ani);
            simOperatorIdView.updateText(mDevice.getSimOperator().orElse(notAvailable), ani);
            simOperatorNameView.updateText(mDevice.getSimOperatorName().orElse(notAvailable), ani);
            simImsiView.updateText(mDevice.getSimSubs().orElse(notAvailable), ani);
            simSerialView.updateText(mDevice.getSimSerial().orElse(notAvailable), ani);

            deviceTypeView.updateText(mDevice.getPhoneType(), ani);
            deviceImeiView.updateText(mDevice.getIMEI(), ani);
            deviceImeiVersionView.updateText(mDevice.getIMEIv(), ani);
            networkNameView.updateText(mDevice.getNetworkName(), ani);
            networkCodeView.updateText(mDevice.getMncMcc(), ani);
            networkTypeView.updateText(mDevice.getNetworkTypeName(), ani);

            dataActivityTypeView.updateText(mDevice.getDataActivityType(), ani);
            dataStatusView.updateText(mDevice.getDataState(), ani);
            networkRoamingView.updateText(String.valueOf(mDevice.isRoaming()), ani);

            ani.startAnimation(5000);
        }
    }

    @Override
    public void onRefresh() {
        if (CellTracker.OCID_API_KEY != null && !CellTracker.OCID_API_KEY.equals("NA")) {
            Request request = createOpenCellIdApiCall();
            okHttpClient.newCall(request).enqueue(getOpenCellIdResponseCallback());
        } else {
            Handler refresh = new Handler(Looper.getMainLooper());
            refresh.post(new Runnable() {
                public void run() {
                    Helpers.sendMsg(getActivity(), getString(R.string.no_opencellid_key_detected));
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }

    @NonNull
    private Callback getOpenCellIdResponseCallback() {
        return new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Handler refresh = new Handler(Looper.getMainLooper());
                refresh.post(new Runnable() {
                    public void run() {
                        refreshFailed();
                    }
                });
            }

            @Override
            public void onResponse(final Response response) throws IOException {
                Handler refresh = new Handler(Looper.getMainLooper());
                refresh.post(new Runnable() {
                    public void run() {
                        Cell cell = responseToCell(response);
                        processFinish(cell);
                    }
                });
            }
        };
    }

    //TODO: Use Retrofit for this
    private Request createOpenCellIdApiCall() {
        StringBuilder sb = new StringBuilder();
        sb.append("http://www.opencellid.org/cell/get?key=").append(CellTracker.OCID_API_KEY);

        if (mAimsicdService.getCell().getMobileCountryCode() != Integer.MAX_VALUE) {
            sb.append("&mcc=").append(mAimsicdService.getCell().getMobileCountryCode());
        }
        if (mAimsicdService.getCell().getMobileNetworkCode() != Integer.MAX_VALUE) {
            sb.append("&mnc=").append(mAimsicdService.getCell().getMobileNetworkCode());
        }
        if (mAimsicdService.getCell().getLocationAreaCode() != Integer.MAX_VALUE) {
            sb.append("&lac=").append(mAimsicdService.getCell().getLocationAreaCode());
        }
        if (mAimsicdService.getCell().getCellId() != Integer.MAX_VALUE) {
            sb.append("&cellid=").append(mAimsicdService.getCell().getCellId());
        }
        sb.append("&format=json");
        return new Request.Builder()
                .url(sb.toString())
                .get()
                .build();
    }

    private Cell responseToCell(Response response) {
        try {
            JSONObject jsonCell = new JSONObject(response.body().string());
            Cell cell = new Cell();
            cell.setLat(jsonCell.getDouble("lat"));
            cell.setLon(jsonCell.getDouble("lon"));
            cell.setMobileCountryCode(jsonCell.getInt("mcc"));
            cell.setMobileNetworkCode(jsonCell.getInt("mnc"));
            cell.setCellId(jsonCell.getInt("cellid"));
            cell.setLocationAreaCode(jsonCell.getInt("lac"));
            return cell;
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void processFinish(Cell cell) {
        if (cell != null) {
            log.info("processFinish - Cell =" + cell.toString());
            if (cell.isValid()) {
                mAimsicdService.setCell(cell);
                Helpers.msgShort(mContext, getActivity().getString(R.string.refreshed_cell_id_info));  // TODO re-translating other languages
                updateUI();
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void refreshFailed() {
        Helpers.msgShort(mContext, "Failed to refresh CellId. Check network connection.");
        swipeRefreshLayout.setRefreshing(false);
    }
}

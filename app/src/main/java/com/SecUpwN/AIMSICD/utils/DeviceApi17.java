package com.SecUpwN.AIMSICD.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.List;

/**
 * Created by toby on 10/10/14.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DeviceApi17 {
    public static final String TAG = "DeviceApi18";

    public static void loadCellInfo(TelephonyManager tm, Cell mCell) {
        try {
            List<CellInfo> cellInfoList = tm.getAllCellInfo();
            if (cellInfoList != null) {
                for (final CellInfo info : cellInfoList) {
                    mCell = new Cell();
                    //Network type
                    mCell.setNetType(tm.getNetworkType());
                    if (info instanceof CellInfoGsm) {
                        final CellSignalStrengthGsm gsm = ((CellInfoGsm) info)
                                .getCellSignalStrength();
                        final CellIdentityGsm identityGsm = ((CellInfoGsm) info)
                                .getCellIdentity();
                        //Signal Strength
                        mCell.setDBM(gsm.getDbm());
                        //Cell Identity
                        mCell.setCID(identityGsm.getCid());
                        mCell.setMCC(identityGsm.getMcc());
                        mCell.setMNC(identityGsm.getMnc());
                        mCell.setLAC(identityGsm.getLac());
                    } else if (info instanceof CellInfoCdma) {
                        final CellSignalStrengthCdma cdma = ((CellInfoCdma) info)
                                .getCellSignalStrength();
                        final CellIdentityCdma identityCdma = ((CellInfoCdma) info)
                                .getCellIdentity();
                        //Signal Strength
                        mCell.setDBM(cdma.getDbm());
                        //Cell Identity
                        mCell.setCID(identityCdma.getBasestationId());
                        mCell.setMNC(identityCdma.getSystemId());
                        mCell.setLAC(identityCdma.getNetworkId());
                        mCell.setSID(identityCdma.getSystemId());
                    } else if (info instanceof CellInfoLte) {
                        final CellSignalStrengthLte lte = ((CellInfoLte) info)
                                .getCellSignalStrength();
                        final CellIdentityLte identityLte = ((CellInfoLte) info)
                                .getCellIdentity();
                        //Signal Strength
                        mCell.setDBM(lte.getDbm());
                        mCell.setTimingAdvance(lte.getTimingAdvance());
                        //Cell Identity
                        mCell.setMCC(identityLte.getMcc());
                        mCell.setMNC(identityLte.getMnc());
                        mCell.setCID(identityLte.getCi());
                    } else if  (info instanceof CellInfoWcdma) {
                        final CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) info)
                                .getCellSignalStrength();
                        final CellIdentityWcdma identityWcdma = ((CellInfoWcdma) info)
                                .getCellIdentity();
                        //Signal Strength
                        mCell.setDBM(wcdma.getDbm());
                        //Cell Identity
                        mCell.setLAC(identityWcdma.getLac());
                        mCell.setMCC(identityWcdma.getMcc());
                        mCell.setMNC(identityWcdma.getMnc());
                        mCell.setCID(identityWcdma.getCid());
                        mCell.setPSC(identityWcdma.getPsc());
                    } else {
                        Log.i(TAG, "Unknown type of cell signal!" + "ClassName: " +
                                info.getClass().getSimpleName() + " ToString: " +
                                info.toString());
                    }
                    if (mCell.isValid())
                        break;
                }
            }
        } catch (NullPointerException npe) {
            Log.e(TAG, "Unable to obtain cell signal information", npe);
        }

    }

    public static void startListening(TelephonyManager tm, PhoneStateListener listener) {
        tm.listen(listener,
                PhoneStateListener.LISTEN_CELL_INFO |
                PhoneStateListener.LISTEN_CELL_LOCATION |
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
                PhoneStateListener.LISTEN_SERVICE_STATE |
                PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
        );
    }
}

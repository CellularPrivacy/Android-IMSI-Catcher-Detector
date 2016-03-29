/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.utils;

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

import java.util.List;

import io.freefair.android.util.logging.AndroidLogger;
import io.freefair.android.util.logging.Logger;

/**
 * This class is taking in consideration newly available network info items
 * that are only available in the AOS API 18 and above. In this case we're 
 * concerned with Wcdma Cell info (CellInfoWcdma)
 *
 * See: http://developer.android.com/reference/android/os/Build.VERSION_CODES.html
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DeviceApi18 {

    public static final Logger log = AndroidLogger.forClass(DeviceApi18.class);

    public static void loadCellInfo(TelephonyManager tm, Device pDevice) {
        int lCurrentApiVersion = Build.VERSION.SDK_INT;
        try {
            if (pDevice.mCell == null) {
                pDevice.mCell = new Cell();
            }
            List<CellInfo> cellInfoList = tm.getAllCellInfo();
            if (cellInfoList != null) {
                for (final CellInfo info : cellInfoList) {

                    //Network Type
                    pDevice.mCell.setNetType(tm.getNetworkType());

                    if (info instanceof CellInfoGsm) {
                        final CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
                        final CellIdentityGsm identityGsm = ((CellInfoGsm) info).getCellIdentity();
                        // Signal Strength
                        pDevice.mCell.setDbm(gsm.getDbm()); // [dBm]
                        // Cell Identity
                        pDevice.mCell.setCid(identityGsm.getCid());
                        pDevice.mCell.setMcc(identityGsm.getMcc());
                        pDevice.mCell.setMnc(identityGsm.getMnc());
                        pDevice.mCell.setLac(identityGsm.getLac());

                    } else if (info instanceof CellInfoCdma) {
                        final CellSignalStrengthCdma cdma = ((CellInfoCdma) info).getCellSignalStrength();
                        final CellIdentityCdma identityCdma = ((CellInfoCdma) info).getCellIdentity();
                        // Signal Strength
                        pDevice.mCell.setDbm(cdma.getDbm());
                        // Cell Identity
                        pDevice.mCell.setCid(identityCdma.getBasestationId());
                        pDevice.mCell.setMnc(identityCdma.getSystemId());
                        pDevice.mCell.setLac(identityCdma.getNetworkId());
                        pDevice.mCell.setSid(identityCdma.getSystemId());

                    } else if (info instanceof CellInfoLte) {
                        final CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
                        final CellIdentityLte identityLte = ((CellInfoLte) info).getCellIdentity();
                        // Signal Strength
                        pDevice.mCell.setDbm(lte.getDbm());
                        pDevice.mCell.setTimingAdvance(lte.getTimingAdvance());
                        // Cell Identity
                        pDevice.mCell.setMcc(identityLte.getMcc());
                        pDevice.mCell.setMnc(identityLte.getMnc());
                        pDevice.mCell.setCid(identityLte.getCi());

                    } else if  (lCurrentApiVersion >= Build.VERSION_CODES.JELLY_BEAN_MR2 && info instanceof CellInfoWcdma) {
                        final CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) info).getCellSignalStrength();
                        final CellIdentityWcdma identityWcdma = ((CellInfoWcdma) info).getCellIdentity();
                        // Signal Strength
                        pDevice.mCell.setDbm(wcdma.getDbm());
                        // Cell Identity
                        pDevice.mCell.setLac(identityWcdma.getLac());
                        pDevice.mCell.setMcc(identityWcdma.getMcc());
                        pDevice.mCell.setMnc(identityWcdma.getMnc());
                        pDevice.mCell.setCid(identityWcdma.getCid());
                        pDevice.mCell.setPsc(identityWcdma.getPsc());

                    } else {
                        log.info("Unknown type of cell signal!"
                                + "\n ClassName: " + info.getClass().getSimpleName()
                                + "\n ToString: " + info.toString());
                    }
                    if (pDevice.mCell.isValid()) {
                        break;
                    }
                }
            }
        } catch (NullPointerException npe) {
            log.error("loadCellInfo: Unable to obtain cell signal information: ", npe);
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

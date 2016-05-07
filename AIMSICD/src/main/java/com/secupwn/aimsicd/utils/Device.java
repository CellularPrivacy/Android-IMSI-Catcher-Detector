/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.utils;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import io.freefair.android.util.function.Optional;
import io.freefair.android.util.function.Supplier;
import io.freefair.android.util.logging.AndroidLogger;
import io.freefair.android.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Device {

    private static final Logger log = AndroidLogger.forClass(Device.class);

    public Cell cell;
    /**
     * integer representation of Phone Type
     */
    private int phoneId = -1;
    @Setter
    private String cellInfo;
    @Setter
    private String dataState;
    @Setter
    private String dataStateShort;
    /**
     * Network Operator Name
     */
    @Setter
    private String networkName;
    private String mncMcc;
    private Optional<String> simCountry;
    private String phoneType;
    /**
     * Device IMEI
     */
    private String iMEI;
    /**
     * Device IMEI Version
     */
    private String iMEIv;
    private Optional<String> simOperator;
    private Optional<String> simOperatorName;
    private Optional<String> simSerial;
    private Optional<String> simSubs;
    @Setter
    private String dataActivityType;
    @Setter
    private String dataActivityTypeShort;
    private boolean roaming;

    /**
     * Cell object representing last known location
     */
    @Setter
    private Location lastLocation;

    /**
     * Refreshes all device specific details
     */
    public void refreshDeviceInfo(TelephonyManager tm, Context context) {

        //Phone type and associated details
        iMEI = tm.getDeviceId();
        iMEIv = tm.getDeviceSoftwareVersion();
        phoneId = tm.getPhoneType();
        roaming = tm.isNetworkRoaming();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            DeviceApi18.loadCellInfo(tm, this);
        }

        if (cell == null) {
            cell = new Cell();
        }

        switch (phoneId) {

            case TelephonyManager.PHONE_TYPE_NONE:
            case TelephonyManager.PHONE_TYPE_SIP:
            case TelephonyManager.PHONE_TYPE_GSM:
                phoneType = "GSM";
                mncMcc = tm.getNetworkOperator();
                if (mncMcc != null && mncMcc.length() >= 5) {
                    try {
                        if (cell.getMobileCountryCode() == Integer.MAX_VALUE) {
                            cell.setMobileCountryCode(Integer.parseInt(tm.getNetworkOperator().substring(0, 3)));
                        }
                        if (cell.getMobileNetworkCode() == Integer.MAX_VALUE) {
                            cell.setMobileNetworkCode(Integer.parseInt(tm.getNetworkOperator().substring(3, 5)));
                        }
                    } catch (Exception e) {
                        log.info("MncMcc parse exception: ", e);
                    }
                }
                networkName = tm.getNetworkOperatorName();
                if (!cell.isValid()) {
                    GsmCellLocation gsmCellLocation = (GsmCellLocation) tm.getCellLocation();
                    if (gsmCellLocation != null) {
                        cell.setCellId(gsmCellLocation.getCid());
                        cell.setLocationAreaCode(gsmCellLocation.getLac());
                        cell.setPrimaryScramblingCode(gsmCellLocation.getPsc());
                    }
                }
                break;

            case TelephonyManager.PHONE_TYPE_CDMA:
                phoneType = "CDMA";
                if (!cell.isValid()) {
                    CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) tm.getCellLocation();
                    if (cdmaCellLocation != null) {
                        cell.setCellId(cdmaCellLocation.getBaseStationId());
                        cell.setLocationAreaCode(cdmaCellLocation.getNetworkId());
                        cell.setSid(cdmaCellLocation.getSystemId()); // one of these must be a bug !!
                        // See: http://stackoverflow.com/questions/8088046/android-how-to-identify-carrier-on-cdma-network
                        // and: https://github.com/klinker41/android-smsmms/issues/26
                        cell.setMobileNetworkCode(cdmaCellLocation.getSystemId()); // todo: check! (Also CellTracker.java)

                        //Retrieve MCC through System Property
                        String homeOperator = Helpers.getSystemProp(context,
                                "ro.cdma.home.operator.numeric", "UNKNOWN");
                        if (!homeOperator.contains("UNKNOWN")) {
                            try {
                                if (cell.getMobileCountryCode() == Integer.MAX_VALUE) {
                                    cell.setMobileCountryCode(Integer.valueOf(homeOperator.substring(0, 3)));
                                }
                                if (cell.getMobileNetworkCode() == Integer.MAX_VALUE) {
                                    cell.setMobileNetworkCode(Integer.valueOf(homeOperator.substring(3, 5)));
                                }
                            } catch (Exception e) {
                                log.info("HomeOperator parse exception - " + e.getMessage(), e);
                            }
                        }
                    }
                }
                break;
        }

        // SIM Information
        simCountry = getSimCountry(tm);
        // Get the operator code of the active SIM (MCC + MNC)
        simOperator = getSimOperator(tm);
        simOperatorName = getSimOperatorName(tm);
        simSerial = getSimSerial(tm);
        simSubs = getSimSubs(tm);

        dataActivityType = getDataActivityType(tm);
        dataState = getDataState(tm);
    }

    private Optional<String> getSimInformation(Supplier<String> simInfoSupplier) {
        try {
            return Optional.ofNullable(simInfoSupplier.get());
        } catch (Exception e) {
            // SIM methods can cause Exceptions on some devices
            log.error("Failed to get SIM-Information", e);
        }
        return Optional.empty();
    }

    /**
     * SIM Country
     *
     * @return string of SIM Country data
     */
    Optional<String> getSimCountry(final TelephonyManager tm) {
        return getSimInformation(new Supplier<String>() {
            @Nullable
            @Override
            public String get() {
                return tm.getSimCountryIso();
            }
        });
    }

    /**
     * SIM Operator
     *
     * @return string of SIM Operator data
     */
    public Optional<String> getSimOperator(final TelephonyManager tm) {
        return getSimInformation(new Supplier<String>() {
            @Nullable
            @Override
            public String get() {
                return tm.getSimOperator();
            }
        });
    }

    /**
     * SIM Operator Name
     *
     * @return string of SIM Operator Name
     */
    Optional<String> getSimOperatorName(final TelephonyManager tm) {
        return getSimInformation(new Supplier<String>() {
            @Nullable
            @Override
            public String get() {
                return tm.getSimOperatorName();
            }
        });
    }

    /**
     * SIM Subscriber ID
     *
     * @return string of SIM Subscriber ID data
     */
    Optional<String> getSimSubs(final TelephonyManager tm) {
        return getSimInformation(new Supplier<String>() {
            @Nullable
            @Override
            public String get() {
                return tm.getSubscriberId();
            }
        });
    }

    /**
     * SIM Serial Number
     *
     * @return string of SIM Serial Number data
     */
    Optional<String> getSimSerial(final TelephonyManager tm) {
        return getSimInformation(new Supplier<String>() {
            @Nullable
            @Override
            public String get() {
                return tm.getSimSerialNumber();
            }
        });
    }

    /**
     * Network Type
     *
     * @return string representing device Network Type
     */
    public String getNetworkTypeName() {
        if (cell == null) {
            return "Unknown";
        }

        return cell.getRat();
    }

    String getDataActivityType(TelephonyManager tm) {
        int direction = tm.getDataActivity();
        dataActivityTypeShort = "un";
        dataActivityType = "undef";

        switch (direction) {
            case TelephonyManager.DATA_ACTIVITY_NONE:
                dataActivityTypeShort = "No";
                dataActivityType = "None";
                break;
            case TelephonyManager.DATA_ACTIVITY_IN:
                dataActivityTypeShort = "In";
                dataActivityType = "In";
                break;
            case TelephonyManager.DATA_ACTIVITY_OUT:
                dataActivityTypeShort = "Ou";
                dataActivityType = "Out";
                break;
            case TelephonyManager.DATA_ACTIVITY_INOUT:
                dataActivityTypeShort = "IO";
                dataActivityType = "In-Out";
                break;
            case TelephonyManager.DATA_ACTIVITY_DORMANT:
                dataActivityTypeShort = "Do";
                dataActivityType = "Dormant";
                break;
        }

        return dataActivityType;
    }

    String getDataState(TelephonyManager tm) {
        int state = tm.getDataState();
        dataState = "undef";
        dataStateShort = "un";
        switch (state) {
            case TelephonyManager.DATA_DISCONNECTED:
                dataState = "Disconnected";
                dataStateShort = "Di";
                break;
            case TelephonyManager.DATA_CONNECTING:
                dataState = "Connecting";
                dataStateShort = "Ct";
                break;
            case TelephonyManager.DATA_CONNECTED:
                dataState = "Connected";
                dataStateShort = "Cd";
                break;
            case TelephonyManager.DATA_SUSPENDED:
                dataState = "Suspended";
                dataStateShort = "Su";
                break;
        }

        return dataState;
    }

    public void setSignalDbm(int signalDbm) {
        cell.setDbm(signalDbm);
    }

    public int getSignalDBm() {
        return cell.getDbm();
    }

    /**
     * Update Network Type
     */
    public void setNetID(TelephonyManager tm) {
        cell.setNetType(tm.getNetworkType());
    }
}

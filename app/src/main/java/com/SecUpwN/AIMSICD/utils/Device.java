package com.SecUpwN.AIMSICD.utils;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class Device {

    private final String TAG = "AIMSICD";

    /*
     * Device Declarations
     */
    public Cell mCell;

    private int mPhoneID = -1;

    private String mNetType = "";
    private String mCellInfo = "";
    private String mDataState = "";
    private String mDataStateShort = "";
    private String mNetName = "";
    private String mMncmcc = "";
    private String mSimCountry = "";
    private String mPhoneType = "";
    private String mIMEI = "";
    private String mIMEIV = "";
    private String mSimOperator = "";
    private String mSimOperatorName = "";
    private String mSimSerial = "";
    private String mSimSubs = "";
    private String mDataActivityType = "";
    private String mDataActivityTypeShort = "";
    private boolean mRoaming;

    private Location mLastLocation;

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /**
     * Refreshes all device specific details
     */
    public void refreshDeviceInfo(TelephonyManager tm, Context context) {

        //Phone type and associated details
        mIMEI = tm.getDeviceId();
        mIMEIV = tm.getDeviceSoftwareVersion();
        mPhoneID = tm.getPhoneType();
        mRoaming = tm.isNetworkRoaming();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mNetType = getNetworkTypeName();
            DeviceApi17.loadCellInfo(tm, mCell);
        }

        if (mCell == null)
            mCell = new Cell();

        switch (mPhoneID) {

            case TelephonyManager.PHONE_TYPE_NONE:  // Maybe bad!
            case TelephonyManager.PHONE_TYPE_SIP:   // Maybe bad!
            case TelephonyManager.PHONE_TYPE_GSM:
                mPhoneType = "GSM";
                mMncmcc = tm.getNetworkOperator();
                if (mMncmcc != null && mMncmcc.length() >= 5 ) {
                    try {
                        if (mCell.getMCC() == Integer.MAX_VALUE)
                            mCell.setMCC(Integer.parseInt(tm.getNetworkOperator().substring(0, 3)));
                        if (mCell.getMNC() == Integer.MAX_VALUE)
                            mCell.setMNC(Integer.parseInt(tm.getNetworkOperator().substring(3, 5)));
                    } catch (Exception e) {
                        Log.i(TAG, "MncMcc parse exception - " + e.getMessage());
                    }
                }
                mNetName = tm.getNetworkOperatorName();
                if (!mCell.isValid()) {
                    GsmCellLocation gsmCellLocation = (GsmCellLocation) tm.getCellLocation();
                    if (gsmCellLocation != null) {
                        mCell.setCID(gsmCellLocation.getCid());
                        mCell.setLAC(gsmCellLocation.getLac());
                        mCell.setPSC(gsmCellLocation.getPsc());
                    }
                }
                break;

            case TelephonyManager.PHONE_TYPE_CDMA:
                mPhoneType = "CDMA";
                if (!mCell.isValid()) {
                    CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) tm.getCellLocation();
                    if (cdmaCellLocation != null) {
                        mCell.setCID(cdmaCellLocation.getBaseStationId());
                        mCell.setLAC(cdmaCellLocation.getNetworkId());
                        mCell.setSID(cdmaCellLocation.getSystemId()); // one of these must be a bug !!
                        mCell.setMNC(cdmaCellLocation.getSystemId()); // todo: check! (Also CellTracker.java)

                        //Retrieve MCC through System Property
                        String homeOperator = Helpers.getSystemProp(context,
                                "ro.cdma.home.operator.numeric", "UNKNOWN");
                        if (!homeOperator.contains("UNKNOWN")) {
                            try {
                                if (mCell.getMCC() == Integer.MAX_VALUE)
                                    mCell.setMCC(Integer.valueOf(homeOperator.substring(0, 3)));
                                if (mCell.getMNC() == Integer.MAX_VALUE)
                                    mCell.setMNC(Integer.valueOf(homeOperator.substring(3, 5)));
                            } catch (Exception e) {
                                Log.i(TAG, "homeOperator parse exception - " + e.getMessage());
                            }
                        }
                    }
                }
                break;
        }

        // SIM Information
        int simState = tm.getSimState();
        switch (simState) {

            case (TelephonyManager.SIM_STATE_ABSENT): break;
            case (TelephonyManager.SIM_STATE_NETWORK_LOCKED): break;
            case (TelephonyManager.SIM_STATE_PIN_REQUIRED): break;
            case (TelephonyManager.SIM_STATE_PUK_REQUIRED): break;
            case (TelephonyManager.SIM_STATE_UNKNOWN): break;
            case (TelephonyManager.SIM_STATE_READY): {

                // Get the SIM country ISO code
                mSimCountry = getSimCountry(tm);

                // Get the operator code of the active SIM (MCC + MNC)
                mSimOperator = getSimOperator(tm);

                // Get the name of the SIM operator
                mSimOperatorName = getSimOperatorName(tm);

                // Get the SIM’s serial number
                mSimSerial = getSimSerial(tm);

                mSimSubs = getSimSubs(tm);
            }
        }

        mDataActivityType = getDataActivity(tm);
        mDataState = getDataState(tm);
    }

    public String getCellInfo() {
        return mCellInfo;
    }

    public void setCellInfo(String cellInfo) {
        mCellInfo = cellInfo;
    }

    /**
     * Phone Type ID
     *
     * @return integer representation of Phone Type
     */
    public int getPhoneID() {
        return mPhoneID;
    }

    /**
     * SIM Country
     *
     * @return string of SIM Country data
     */
    String getSimCountry(TelephonyManager tm) {
        try {
            if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                mSimCountry = (tm.getSimCountryIso() != null) ? tm.getSimCountryIso() : "N/A";
            } else {
                mSimCountry = "N/A";
            }
        } catch (Exception e) {
            // SIM methods can cause Exceptions on some devices
            mSimCountry = "N/A";
            Log.e(TAG, "getSimCountry " + e);
        }

        if (mSimCountry.isEmpty()) {
            mSimCountry = "N/A";
        }

        return mSimCountry;
    }

    /**
     * SIM Country data
     */
    public String getSimCountry() {
        return mSimCountry;
    }

    /**
     * SIM Operator
     *
     * @return string of SIM Operator data
     */
    public String getSimOperator(TelephonyManager tm) {
        try {
            if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                mSimOperator = (tm.getSimOperator() != null) ? tm.getSimOperator() : "N/A";
            } else {
                mSimOperator = "N/A";
            }
        } catch (Exception e) {
            // SIM methods can cause Exceptions on some devices
            mSimOperator = "N/A";
            Log.e(TAG, "getSimOperator " + e);
        }

        if (mSimOperator.isEmpty()) {
            mSimOperator = "N/A";
        }

        return mSimOperator;
    }

    public String getSimOperator() {
        return mSimOperator;
    }

    /**
     * SIM Operator Name
     *
     * @return string of SIM Operator Name
     */
    String getSimOperatorName(TelephonyManager tm) {
        try {
            if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                mSimOperatorName = (tm.getSimOperatorName() != null) ? tm.getSimOperatorName()
                        : "N/A";
            } else {
                mSimOperatorName = "N/A";
            }
        } catch (Exception e) {
            //SIM methods can cause Exceptions on some devices
            mSimOperatorName = "N/A";
        }

        if (mSimOperatorName.isEmpty()) {
            mSimOperatorName = "N/A";
        }

        return mSimOperatorName;
    }

    public String getSimOperatorName() {
        return mSimOperatorName;
    }

    /**
     * SIM Subscriber ID
     *
     * @return string of SIM Subscriber ID data
     */
    String getSimSubs(TelephonyManager tm) {
        try {
            if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                mSimSubs = (tm.getSubscriberId() != null) ? tm.getSubscriberId() : "N/A";
            } else {
                mSimSubs = "N/A";
            }
        } catch (Exception e) {
            //Some devices don't like this method
            mSimSubs = "N/A";
            Log.e(TAG, "getSimSubs " + e);
        }

        if (mSimSubs.isEmpty()) {
            mSimSubs = "N/A";
        }

        return mSimSubs;
    }

    public String getSimSubs() {
        return mSimSubs;
    }

    /**
     * SIM Serial Number
     *
     * @return string of SIM Serial Number data
     */
    String getSimSerial(TelephonyManager tm) {
        try {
            if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                mSimSerial = (tm.getSimSerialNumber() != null) ? tm.getSimSerialNumber()
                        : "N/A";
            } else {
                mSimSerial = "N/A";
            }
        } catch (Exception e) {
            // SIM methods can cause Exceptions on some devices
            mSimSerial = "N/A";
            Log.e(TAG, "getSimSerial " + e);
        }

        if (mSimSerial.isEmpty()) {
            mSimSerial = "N/A";
        }

        return mSimSerial;
    }

    public String getSimSerial() {
        return mSimSerial;
    }

    public String getPhoneType() {
        return mPhoneType;
    }

    /**
     * IMEI
     *
     * @return string representing device IMEI
     */
    public String getIMEI() {
        return mIMEI;
    }

    /**
     * IMEI Version / Device Software Version
     *
     * @return string representing device IMEI Version
     */
    public String getIMEIv() {
        return mIMEIV;
    }

    /**
     * Sets Network Operator Name
     *
     */
    public void setNetworkName(String networkName) {
        mNetName = networkName;
    }

    public String getNetworkName() {
        return mNetName;
    }

    /**
     * Network Operator
     *
     * @return string representing the Network Operator
     */
    public String getMncMcc() {
        return mMncmcc;
    }

    /**
     * Network Type
     *
     * @return string representing device Network Type
     */
    public String getNetworkTypeName() {
        if (mCell == null) return "Unknown";

        switch (mCell.getNetType()) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                mNetType = "1xRTT";
                break;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                mNetType = "CDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                mNetType = "EDGE";
                break;
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                mNetType = "eHRPD";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                mNetType = "EVDO rev. 0";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                mNetType = "EVDO rev. A";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                mNetType = "EVDO rev. B";
                break;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                mNetType = "GPRS";
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                mNetType = "HSDPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                mNetType = "HSPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                mNetType = "HSPA+";
                break;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                mNetType = "HSUPA";
                break;
            case TelephonyManager.NETWORK_TYPE_IDEN:
                mNetType = "iDen";
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                mNetType = "LTE";
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                mNetType = "UMTS";
                break;
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                mNetType = "Unknown";
                break;
        }

        return mNetType;
    }

    /**
     * Network Type
     *
     * @return string representing device Network Type
     */
    public static String getNetworkTypeName(int netType) {
        String networkType = "Unknown";
        switch (netType) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                networkType = "1xRTT";
                break;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                networkType = "CDMA";
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                networkType = "EDGE";
                break;
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                networkType = "eHRPD";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                networkType = "EVDO_0";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                networkType = "EVDO_A";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                networkType = "EVDO_B";
                break;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                networkType = "GPRS";
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                networkType = "HSDPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                networkType = "HSPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                networkType = "HSPA+";
                break;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                networkType = "HSUPA";
                break;
            case TelephonyManager.NETWORK_TYPE_IDEN:
                networkType = "iDEN";
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                networkType = "LTE";
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                networkType = "UMTS";
                break;
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                networkType = "Unknown";
                break;
        }

        return networkType;
    }

    String getDataActivity(TelephonyManager tm) {
        int direction = tm.getDataActivity();
        mDataActivityTypeShort = "un";
        mDataActivityType = "undef";
        switch (direction) {
            case TelephonyManager.DATA_ACTIVITY_NONE:
                mDataActivityTypeShort = "No";
                mDataActivityType = "None";
                break;
            case TelephonyManager.DATA_ACTIVITY_IN:
                mDataActivityTypeShort = "In";
                mDataActivityType = "In";
                break;
            case TelephonyManager.DATA_ACTIVITY_OUT:
                mDataActivityTypeShort = "Ou";
                mDataActivityType = "Out";
                break;
            case TelephonyManager.DATA_ACTIVITY_INOUT:
                mDataActivityTypeShort = "IO";
                mDataActivityType = "In-Out";
                break;
            case TelephonyManager.DATA_ACTIVITY_DORMANT:
                mDataActivityTypeShort = "Do";
                mDataActivityType = "Dormant";
                break;
        }

        return mDataActivityType;
    }

    public String getDataActivity() {
        return mDataActivityType;
    }

    String getDataState(TelephonyManager tm) {
        int state = tm.getDataState();
        mDataState = "undef";
        mDataStateShort = "un";
        switch (state) {
            case TelephonyManager.DATA_DISCONNECTED:
                mDataState = "Disconnected";
                mDataStateShort = "Di";
                break;
            case TelephonyManager.DATA_CONNECTING:
                mDataState = "Connecting";
                mDataStateShort = "Ct";
                break;
            case TelephonyManager.DATA_CONNECTED:
                mDataState = "Connected";
                mDataStateShort = "Cd";
                break;
            case TelephonyManager.DATA_SUSPENDED:
                mDataState = "Suspended";
                mDataStateShort = "Su";
                break;
        }

        return mDataState;
    }

    public String getDataState() {
        return mDataState;
    }

    public String getDataActivityTypeShort() {
        return mDataActivityTypeShort;
    }

    public void setDataActivityTypeShort(String dataActivityTypeShort) {
        mDataActivityTypeShort = dataActivityTypeShort;
    }

    public String getDataStateShort() {
        return mDataStateShort;
    }

    public void setDataStateShort(String dataStateShort) {
        mDataStateShort = dataStateShort;
    }

    public void setDataActivityType(String dataActivityType) {
        mDataActivityType = dataActivityType;
    }

    public void setDataState(String dataState) {
        mDataState = dataState;
    }

    public void setSignalDbm(int signalDbm) {
        mCell.setDBM(signalDbm);
    }
    public int getSignalDBm() { return mCell.getDBM();}

    /**
     * Update Network Type
     */
    public void setNetID(TelephonyManager tm) {
        mCell.setNetType(tm.getNetworkType());
    }

    /**
     * Mobile Roaming
     *
     * @return string representing Roaming status (True/False)
     */
    public String isRoaming() {
        return String.valueOf(mRoaming);
    }

    public void setLastLocation(Location location) {
        mLastLocation = location;
    }

    /**
     * Attempts to retrieve the Last Known Location from the device
     *
     * @return Cell object representing last known location
     */
    public Location getLastLocation() {
        return mLastLocation;
    }


    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new
     *                            one
     */
    public boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        // E:V:A  2014-12-19  This may be a bad assumption, unless we also include
        // movement detection (accelerometer)... Remove comment if ok.
        if (isSignificantlyNewer) {
            return true;
            // If the last location is more than two minutes old, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}

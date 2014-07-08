package com.SecUpwN.AIMSICD.utils;

import android.location.Location;
import android.location.LocationManager;
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
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Device {

    public final String TAG = "AIMSICD";

    /*
     * Device Declarations
     */
    private int mPhoneID = -1;
    private int mMcc = -1;
    private int mMnc = -1;
    private int mSignalInfo = -1;
    private int mNetID = -1;
    private int mLac = -1;
    private int mCellID = -1;
    private int mSID = -1;
    private int mPSC = -1;
    private int mTimingAdvance = -1;
    private double mLongitude = 0.0;
    private double mLatitude = 0.0;
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

    private final List<Cell> mNeighboringCells = new ArrayList<>();
    private Location mLastLocation;

    private static final int TWO_MINUTES = 1000 * 60 * 2;


    /**
     * Refreshes all device specific details
     */
    public void refreshDeviceInfo(TelephonyManager tm) {
        //Phone type and associated details
        mIMEI = tm.getDeviceId();
        mIMEIV = tm.getDeviceSoftwareVersion();
        mPhoneID = tm.getPhoneType();
        mRoaming = tm.isNetworkRoaming();
        //Network type
        mNetID = tm.getNetworkType();
        mNetType = getNetworkTypeName();

        switch (mPhoneID) {
            case TelephonyManager.PHONE_TYPE_GSM:
                mPhoneType = "GSM";
                mMncmcc = tm.getNetworkOperator();
                if (mMncmcc != null) {
                    mMcc = Integer.parseInt(tm.getNetworkOperator().substring(0, 3));
                    mMnc = Integer.parseInt(tm.getNetworkOperator().substring(3));
                }
                mNetName = tm.getNetworkOperatorName();
                GsmCellLocation gsmCellLocation = (GsmCellLocation) tm.getCellLocation();
                if (gsmCellLocation != null) {
                    mCellID = (gsmCellLocation.getCid() == 0x7FFFFFFF) ?
                            gsmCellLocation.getCid() & 0xffff : gsmCellLocation.getCid();
                    mLac = (gsmCellLocation.getLac() == 0x7FFFFFFF ) ?
                            gsmCellLocation.getLac() & 0xffff : gsmCellLocation.getLac();
                    mPSC = gsmCellLocation.getPsc();
                }

                break;
            case TelephonyManager.PHONE_TYPE_CDMA:
                mPhoneType = "CDMA";
                CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) tm.getCellLocation();
                if (cdmaCellLocation != null) {
                    mCellID = cdmaCellLocation.getBaseStationId();
                    mLac = cdmaCellLocation.getNetworkId();
                    mSID = cdmaCellLocation.getSystemId();
                }
                break;
        }

        //SDK 17 allows access to signal strength outside of the listener and also
        //provide access to the LTE timing advance data
        if (Build.VERSION.SDK_INT > 16) {
            try {
                List<CellInfo> cellInfoList = tm.getAllCellInfo();
                if (cellInfoList != null) {
                    for (final CellInfo info : cellInfoList) {
                        if (info instanceof CellInfoGsm) {
                            final CellSignalStrengthGsm gsm = ((CellInfoGsm) info)
                                    .getCellSignalStrength();
                            final CellIdentityGsm identityGsm = ((CellInfoGsm) info)
                                    .getCellIdentity();
                            //Signal Strength
                            mSignalInfo = gsm.getDbm();
                            //Cell Identity
                            mCellID = identityGsm.getCid();
                            mMcc = identityGsm.getMcc();
                            mMnc = identityGsm.getMnc();
                            mLac = identityGsm.getLac();
                        } else if (info instanceof CellInfoCdma) {
                            final CellSignalStrengthCdma cdma = ((CellInfoCdma) info)
                                    .getCellSignalStrength();
                            final CellIdentityCdma identityCdma = ((CellInfoCdma) info)
                                    .getCellIdentity();
                            //Signal Strength
                            mSignalInfo = cdma.getDbm();
                            //Cell Identity
                            mCellID = identityCdma.getBasestationId();
                            mLac = identityCdma.getNetworkId();
                            mSID = identityCdma.getSystemId();
                        } else if (info instanceof CellInfoLte) {
                            final CellSignalStrengthLte lte = ((CellInfoLte) info)
                                    .getCellSignalStrength();
                            final CellIdentityLte identityLte = ((CellInfoLte) info)
                                    .getCellIdentity();
                            //Signal Strength
                            mSignalInfo = lte.getDbm();
                            mTimingAdvance = lte.getTimingAdvance();
                            //Cell Identity
                            mMcc = identityLte.getMcc();
                            mMnc = identityLte.getMnc();
                            mCellID = identityLte.getCi();
                        } else if (info instanceof CellInfoWcdma) {
                            final CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) info)
                                    .getCellSignalStrength();
                            final CellIdentityWcdma identityWcdma = ((CellInfoWcdma) info)
                                    .getCellIdentity();
                            //Signal Strength
                            mSignalInfo = wcdma.getDbm();
                            //Cell Identity
                            mLac = identityWcdma.getLac();
                            mMcc = identityWcdma.getMcc();
                            mMnc = identityWcdma.getMnc();
                            mCellID = identityWcdma.getCid();
                            mPSC = identityWcdma.getPsc();
                        } else {
                            Log.i(TAG, "Unknown type of cell signal!" + "ClassName: " +
                                    info.getClass().getSimpleName() + " ToString: " +
                                    info.toString());
                        }
                    }
                }
            } catch (NullPointerException npe) {
                Log.e(TAG, "Unable to obtain cell signal information", npe);
            }
        }

        //SIM Information
        mSimCountry = getSimCountry(tm);
        mSimOperator = getSimOperator(tm);
        mSimOperatorName = getSimOperatorName(tm);
        mSimSerial = getSimSerial(tm);
        mSimSubs = getSimSubs(tm);

        mDataActivityType = getDataActivity(tm);
        mDataState = getDataState(tm);

    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public String getCellInfo() {
        return mCellInfo;
    }

    public void setCellInfo(String cellInfo) {
        mCellInfo = cellInfo;
    }

    public List<Cell> getNeighboringCells() {
        return mNeighboringCells;
    }

    public int getPSC() {
        return mPSC;
    }

    /**
     * LTE Timing Advance
     *
     * @return Timing Advance figure or -1 if not available
     */
    public int getLteTimingAdvance() {
        return mTimingAdvance;
    }

    /**
     * Mobile Country Code MCC
     */
    public int getMCC() {
        return mMcc;
    }

    /**
     * Mobile Network Code MCC
     */
    public int getMnc() {
        return mMnc;
    }

    /**
     * CDMA System ID
     *
     * @return System ID or -1 if not supported
     */
    public int getSID() {
        return mSID;
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
    public String getSimCountry(TelephonyManager tm) {
        try {
            if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                mSimCountry = (tm.getSimCountryIso() != null) ? tm.getSimCountryIso() : "N/A";
            } else {
                mSimCountry = "N/A";
            }
        } catch (Exception e) {
            //SIM methods can cause Exceptions on some devices
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
            //SIM methods can cause Exceptions on some devices
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
    public String getSimOperatorName(TelephonyManager tm) {
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
    public String getSimSubs(TelephonyManager tm) {
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
    public String getSimSerial(TelephonyManager tm) {
        try {
            if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                mSimSerial = (tm.getSimSerialNumber() != null) ? tm.getSimSerialNumber()
                        : "N/A";
            } else {
                mSimSerial = "N/A";
            }
        } catch (Exception e) {
            //SIM methods can cause Exceptions on some devices
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
     * Network Operator Name
     *
     * @return string representing device Network Operator Name
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
    public String getSmmcMcc() {
        return mMncmcc;
    }

    /**
     * Network Type
     *
     * @return string representing device Network Type
     */
    public String getNetworkTypeName() {
        switch (getNetID()) {
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

    public void setSignalInfo(int signalInfo) {
        mSignalInfo = signalInfo;
    }

    public int getSignalInfo() {
        return mSignalInfo;
    }

    /**
     * Network Type
     *
     * @return integer representing device Network Type
     */
    public int getNetID() {
        return mNetID;
    }

    /**
     * Update Network Type
     */
    public void setNetID(TelephonyManager tm) {
        mNetID = tm.getNetworkType();
    }

    /**
     * Mobile Roaming
     *
     * @return string representing Roaming status (True/False)
     */
    public String isRoaming() {
        return String.valueOf(mRoaming);
    }

    public int getLac() {
        return mLac;
    }

    public void setLAC(int lac) {
        mLac = lac;
    }

    /**
     * Cell ID for either GSM or CDMA devices, returns string representation
     * but also updates the integer member as well
     *
     * @return int representing the Cell ID from GSM or CDMA devices
     */
    public int getCellId() {
        return mCellID;
    }

    public void setCellID(int cellID) {
        mCellID = cellID;
    }

    public void setLastLocation(Location location) {
        mLastLocation = location;
    }

    /**
     * Attempts to retrieve the Last Known Location from the device
     *
     * @return Location object representing last known location
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
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
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

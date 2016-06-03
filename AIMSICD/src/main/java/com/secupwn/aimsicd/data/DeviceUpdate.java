package com.secupwn.aimsicd.data;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.service.AimsicdService;
import com.secupwn.aimsicd.utils.Device;

/**
 * Created by Kai on 2016/6/3.
 */

public class DeviceUpdate {
    private static DeviceUpdate lastUpdate;

    public static DeviceUpdate getLastUpdate() {
        return lastUpdate;
    }

    public static DeviceUpdate getUpdate(AimsicdService mAimsicdService) {
        mAimsicdService.getCellTracker().refreshDevice();
        String notAvailable = mAimsicdService.getApplicationContext().getString(R.string.n_a);
        lastUpdate = new DeviceUpdate();
        Device mDevice = mAimsicdService.getCellTracker().getDevice();
        lastUpdate.phoneId = mDevice.getPhoneId();
        lastUpdate.locationAreaCode = String.valueOf(mAimsicdService.getCell().getLocationAreaCode());
        lastUpdate.cellId = String.valueOf(mAimsicdService.getCell().getCellId());
        lastUpdate.systemId = String.valueOf(mAimsicdService.getCell().getSid());
        lastUpdate.lteTimingAdvanceInt = mAimsicdService.getCell().getTimingAdvance();
        lastUpdate.lteTimingAdvance = String.valueOf(lastUpdate.lteTimingAdvanceInt);
        lastUpdate.primaryScramblingCodeInt = mAimsicdService.getCell().getPrimaryScramblingCode();
        lastUpdate.primaryScramblingCode = String.valueOf(lastUpdate.primaryScramblingCodeInt);
        lastUpdate.simCountry = mDevice.getSimCountry().orElse(notAvailable);
        lastUpdate.simOperatorId = mDevice.getSimOperator().orElse(notAvailable);
        lastUpdate.simOperatorName = mDevice.getSimOperatorName().orElse(notAvailable);
        lastUpdate.simImsi = mDevice.getSimSubs().orElse(notAvailable);
        lastUpdate.simSerial = mDevice.getSimSerial().orElse(notAvailable);
        lastUpdate.deviceType = mDevice.getPhoneType();
        lastUpdate.deviceImei = mDevice.getIMEI();
        lastUpdate.deviceImeiVersion = mDevice.getIMEIv();
        lastUpdate.networkName = mDevice.getNetworkName();
        lastUpdate.networkCode = mDevice.getMncMcc();
        lastUpdate.networkType = mDevice.getNetworkTypeName();
        lastUpdate.dataActivityType = mDevice.getDataActivityType();
        lastUpdate.dataStatus = mDevice.getDataState();
        lastUpdate.networkRoaming = String.valueOf(mDevice.isRoaming());
        return getLastUpdate();
    }

    private DeviceUpdate() {
    }

    private String locationAreaCode;
    private String cellId;
    private String systemId;
    private String lteTimingAdvance;
    private int lteTimingAdvanceInt;
    private String primaryScramblingCode;
    private int primaryScramblingCodeInt;
    private String simCountry;
    private String simOperatorId;
    private String simOperatorName;
    private String simImsi;
    private String simSerial;
    private String deviceType;
    private String deviceImei;
    private String deviceImeiVersion;
    private String networkName;
    private String networkCode;
    private String networkType;
    private String dataActivityType;
    private String dataStatus;
    private String networkRoaming;
    private int phoneId;

    public String getLocationAreaCode() {
        return locationAreaCode;
    }

    public String getCellId() {
        return cellId;
    }

    public String getSystemId() {
        return systemId;
    }

    public String getLteTimingAdvance() {
        return lteTimingAdvance;
    }

    public int getLteTimingAdvanceInt() {
        return lteTimingAdvanceInt;
    }

    public String getPrimaryScramblingCode() {
        return primaryScramblingCode;
    }

    public int getPrimaryScramblingCodeInt() {
        return primaryScramblingCodeInt;
    }

    public String getSimCountry() {
        return simCountry;
    }

    public String getSimOperatorId() {
        return simOperatorId;
    }

    public String getSimOperatorName() {
        return simOperatorName;
    }

    public String getSimImsi() {
        return simImsi;
    }

    public String getSimSerial() {
        return simSerial;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceImei() {
        return deviceImei;
    }

    public String getDeviceImeiVersion() {
        return deviceImeiVersion;
    }

    public String getNetworkName() {
        return networkName;
    }

    public String getNetworkCode() {
        return networkCode;
    }

    public String getNetworkType() {
        return networkType;
    }

    public String getDataActivityType() {
        return dataActivityType;
    }

    public String getDataStatus() {
        return dataStatus;
    }

    public String getNetworkRoaming() {
        return networkRoaming;
    }

    public int getPhoneId() {
        return phoneId;
    }
}

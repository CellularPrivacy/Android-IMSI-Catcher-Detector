/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.utils;

import io.freefair.android.util.logging.AndroidLogger;
import io.freefair.android.util.logging.Logger;

import com.SecUpwN.AIMSICD.R;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import android.content.Context;

/*
 * These are the OEM commands used by the MulticlientRil ServiceMode menu scraper,
 * and possibly elsewhere. OEM commands (as the name suggests) are device dependent
 * and these particular ones are for Samsung Galaxy S2, AFAIK.
 * -- E:V:A
 */
public class OemCommands {

    private static final Logger log = AndroidLogger.forClass(OemCommands.class);

    public static final char OEM_SERVM_FUNCTAG = 1;
    public static final char OEM_SM_ACTION = 0;
    public static final char OEM_SM_QUERY = 1;
    public static final char OEM_SM_DUMMY = 0;
    public static final char OEM_SM_ENTER_MODE_MESSAGE = 1;
    public static final char OEM_SM_END_MODE_MESSAGE = 2;
    public static final char OEM_SM_PROCESS_KEY_MESSAGE = 3;
    public static final char OEM_SM_GET_DISPLAY_DATA_MESSAGE = 4;

    public static final char OEM_SM_TYPE_TEST_MANUAL = 1;
    public static final char OEM_SM_TYPE_TEST_AUTO = 2;
    public static final char OEM_SM_TYPE_NAM_EDIT = 3;
    public static final char OEM_SM_TYPE_MONITOR = 4;
    public static final char OEM_SM_TYPE_PHONE_TEST = 5;

    public static final char OEM_SM_TYPE_SUB_ENTER = 0;
    public static final char OEM_SM_TYPE_SUB_SW_VERSION_ENTER = 1;
    public static final char OEM_SM_TYPE_SUB_FTA_SW_VERSION_ENTER = 2;
    public static final char OEM_SM_TYPE_SUB_FTA_HW_VERSION_ENTER = 3;
    public static final char OEM_SM_TYPE_SUB_ALL_VERSION_ENTER = 4;
    public static final char OEM_SM_TYPE_SUB_BATTERY_INFO_ENTER = 5;
    public static final char OEM_SM_TYPE_SUB_CIPHERING_PROTECTION_ENTER = 6;
    public static final char OEM_SM_TYPE_SUB_INTEGRITY_PROTECTION_ENTER = 7;
    public static final char OEM_SM_TYPE_SUB_IMEI_READ_ENTER = 8;
    public static final char OEM_SM_TYPE_SUB_BLUETOOTH_TEST_ENTER = 9;
    public static final char OEM_SM_TYPE_SUB_VIBRATOR_TEST_ENTER = 10;
    public static final char OEM_SM_TYPE_SUB_MELODY_TEST_ENTER = 11;
    public static final char OEM_SM_TYPE_SUB_MP3_TEST_ENTER = 12;
    public static final char OEM_SM_TYPE_SUB_FACTORY_RESET_ENTER = 13;
    public static final char OEM_SM_TYPE_SUB_FACTORY_PRECONFIG_ENTER = 14;
    public static final char OEM_SM_TYPE_SUB_TFS4_EXPLORE_ENTER = 15;
    // E:V:A  What is 16? // SecUpwN: Does not seem to be defined. Remove this line?
    public static final char OEM_SM_TYPE_SUB_RSC_FILE_VERSION_ENTER = 17;
    public static final char OEM_SM_TYPE_SUB_USB_DRIVER_ENTER = 18;
    public static final char OEM_SM_TYPE_SUB_USB_UART_DIAG_CONTROL_ENTER = 19;
    public static final char OEM_SM_TYPE_SUB_RRC_VERSION_ENTER = 20;
    public static final char OEM_SM_TYPE_SUB_GPSONE_SS_TEST_ENTER = 21;
    public static final char OEM_SM_TYPE_SUB_BAND_SEL_ENTER = 22;
    public static final char OEM_SM_TYPE_SUB_GCF_TESTMODE_ENTER = 23;
    public static final char OEM_SM_TYPE_SUB_GSM_FACTORY_AUDIO_LB_ENTER = 24;
    public static final char OEM_SM_TYPE_SUB_FACTORY_VF_TEST_ENTER = 25;
    public static final char OEM_SM_TYPE_SUB_TOTAL_CALL_TIME_INFO_ENTER = 26;
    public static final char OEM_SM_TYPE_SUB_SELLOUT_SMS_ENABLE_ENTER = 27;
    public static final char OEM_SM_TYPE_SUB_SELLOUT_SMS_DISABLE_ENTER = 28;
    public static final char OEM_SM_TYPE_SUB_SELLOUT_SMS_TEST_MODE_ON = 29;
    public static final char OEM_SM_TYPE_SUB_SELLOUT_SMS_PRODUCT_MODE_ON = 30;
    public static final char OEM_SM_TYPE_SUB_GET_SELLOUT_SMS_INFO_ENTER = 31;
    public static final char OEM_SM_TYPE_SUB_TST_AUTO_ANSWER_ENTER = 32;
    public static final char OEM_SM_TYPE_SUB_TST_NV_RESET_ENTER = 33;

    public static final char OEM_SM_TYPE_SUB_TST_FTA_SW_VERSION_ENTER = 4098;
    public static final char OEM_SM_TYPE_SUB_TST_FTA_HW_VERSION_ENTER = 4099;

    private int mApiVersion;

    private OemCommands(int apiVersion) {
        mApiVersion = apiVersion;
    }

    public static OemCommands getInstance(Context context) {
        int apiVersion = context.getResources().getInteger(R.integer.config_api_version);
        return new OemCommands(apiVersion);
	}

    public byte[] getEnterServiceModeData(int modeType, int subType, int query) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(OEM_SERVM_FUNCTAG);
            dos.writeByte(OEM_SM_ENTER_MODE_MESSAGE);
            if (mApiVersion == 1) {
                dos.writeShort(7);
			} else if (mApiVersion == 2) {
                dos.writeShort(8);
                dos.writeByte(4);
			} else {
                throw new IllegalArgumentException("Invalid API version " + mApiVersion);
			}
            dos.writeByte(modeType);
            dos.writeByte(subType);
            dos.writeByte(query);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("", e);
        }
        return null;
    }

    public byte[] getEndServiceModeData(int modeType) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(OEM_SERVM_FUNCTAG);
            dos.writeByte(OEM_SM_END_MODE_MESSAGE);
            if (mApiVersion == 1) {
                dos.writeShort(5);
			} else if (mApiVersion == 2) {
                dos.writeShort(6);
                dos.writeByte(4);
			} else {
                throw new IllegalArgumentException("Invalid API version " + mApiVersion);
			}
            dos.writeByte(modeType);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("", e);
        }
        return null;
    }

    public byte[] getPressKeyData(int keycode, int query) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(OEM_SERVM_FUNCTAG);
            dos.writeByte(OEM_SM_PROCESS_KEY_MESSAGE);
            if (mApiVersion == 1) {
                dos.writeShort(6);
			} else if (mApiVersion == 2) {
                dos.writeShort(7);
                dos.writeByte(4);
			} else {
                throw new IllegalArgumentException("Invalid API version " + mApiVersion);
			}
            dos.writeByte(keycode);
            dos.writeByte(query);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("", e);
        }
        return null;
    }

}

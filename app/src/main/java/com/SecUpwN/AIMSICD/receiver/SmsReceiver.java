/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.SecUpwN.AIMSICD.service.CellTracker;

import java.util.ArrayList;
import java.util.List;


/**
 *  Description:    The SMS receiver class that handles the SMS PDU data
 *
 *  Dependencies:   AndroidManifest.xml (receiver)
 *
 *  Permissions:    To read and intercept various SMS/MMS/WAP push messages, we need
 *                  at least 3 different Android permissions:
 *
 *                  android.permission.READ_SMS
 *                  android.permission.RECEIVE_MMS
 *                  android.permission.RECEIVE_SMS
 *                  android.permission.RECEIVE_WAP_PUSH
 *
 *                  To read silent SMS from radio logcat, we also need:
 *
 *                  android.permission.READ_LOGS
 *
 *  Notes:      1)  3GPP TS 23.040 9.2.3.9 specifies that Type Zero messages are indicated
 *                  by TP_PID field set to value 0x40 = 64 = 01000000
 *              2)  For others see: http://web.tiscali.it/richard/tlc/articoli/sms3.htm
 *              3) http://www.etsi.org/deliver/etsi_ts/123000_123099/123040/12.02.00_60/ts_123040v120200p.pdf
 *              4)  Here:
 *                          TP-MTI = Message Type Indicator    -
 *                          TP-PID = Protocol IDentifier       -
 *                          TP-MMS = More Messages to Send     -
 *
 *   Issues:
 *              [ ] TODO: Add silent MMS check ? -- Is this correctly understood?
 *              [ ] TODO: Add silent WAP PUSH check
 *              [ ] TODO: https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/178
 *              [ ] Possible blocking issue due to AndroidManifest.xml permissions
 *              [ ]  Status Report Indication (TP-SRI) -- requested ?
 *              [ ]  Validity period (TP-VP) -- too short ?
 *              [ ]  The service centre time stamp (TP-SCTS) -- wrong date?
 *
 *      Log:
 *      --------- beginning of /dev/log/main
 *      I/AIMSICD_SmsReceiver(28544): Pdu data: firstByte = 8 mti = 0 TP_PID = 128
 *      I/AIMSICD_SmsReceiver(28544): Type 0 Message received, Sender: +38631XXXXXX Message: t
 *      I/CellTracker(28544): neighbouringCellInfo Size - 1
 *      I/CellTracker(28544): neighbouringCellInfo - CID:-1 LAC:-1 RSSI:-85 PSC:183
 *
 *
 *   ChangeLog:     2015-02-10  banjaxbanjo     - added code to dump full SMS PDU to logcat
 *                  2015-02-11  E:V:A           - changed from "||" to "&& mti==0"
 *
 */
public class SmsReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        try {
            final Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                final List<SmsMessage> messages = new ArrayList<>();
                StringBuilder sb = new StringBuilder();
                String full_pdu_string = "";
                for (Object pdu : pdus) {
                    byte smsPdu[] = (byte[]) pdu;

                    // Dump the full SMS in PDU format (HEX string) to logcat
                    try {
                        for(int xx = 0; xx < smsPdu.length; xx++) {
                            String test = Integer.toHexString(smsPdu[xx] & 0xff);
                            if (test.length() <= 1){ test = "0"+test; }
                            sb.append(test);
                        }
                        full_pdu_string = sb.toString();
                    } catch (Exception err) {
                        Log.e("SmsReceiver", "Exception PDU smsReceiver" + err);
                    }

                    // We may also need to consider catching WAP PUSH SMS messages
                    // as they can also be hidden from user. -- E:V:A
                    int firstByte = smsPdu[0] & 0xff;
                    int mti = firstByte & 0x3;  //   3 = 0000 0011      (bits 0-1)
                    //int mms = firstByte & 0x4;  //   4 = 0000 0100    (bit 3)
                    //int sri = firstByte & 0x10; //  16 = 0001 0000    (bit 5)
                    int pID = smsPdu[1] & 0xc0; // 192 = 1100 0000
                    Log.i("AIMSICD_SmsReceiver", "PDU Data: firstByte: " + firstByte +
                            " TP-MTI: " + mti + " TP-PID: " + pID);
                    // Need checking! --EVA
                    if (pID == 0x40 && mti == 0) {
                        messages.add(SmsMessage.createFromPdu((byte[]) pdu));
                    }
                }

                if (messages.size() > 0) {
                    for (SmsMessage sms : messages) {
                        Intent smsIntent = new Intent(CellTracker.SILENT_SMS);
                        Bundle smsData = new Bundle();
                        smsData.putString("address",         sms.getOriginatingAddress());
                        smsData.putString("display_address", sms.getDisplayOriginatingAddress());
                        smsData.putString("class",           sms.getMessageClass().name());
                        smsData.putString("service_centre",  sms.getServiceCenterAddress());
                        smsData.putString("message",         sms.getMessageBody());
                        smsIntent.putExtras(smsData);
                        context.sendBroadcast(smsIntent);
                        Log.i("AIMSICD_SmsReceiver", "Type-0 SMS received! Sender: "
                                + sms.getOriginatingAddress() + " Message: "
                                + sms.getMessageBody());
                    }
                }
            }
        } catch (NullPointerException npe) {
            Log.e("SmsReceiver", "Exception smsReceiver" + npe);
        }
    }

}

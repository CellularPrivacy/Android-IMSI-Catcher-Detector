package com.SecUpwN.AIMSICD.receiver;

import com.SecUpwN.AIMSICD.service.AimsicdService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        try {
                final Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[])bundle.get("pdus");
                    final SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                    }
                    if (messages.length > -1) {
                        for (SmsMessage sms : messages) {
                            if (sms.getMessageClass().equals(SmsMessage.MessageClass.CLASS_0)) {
                                Intent smsIntent = new Intent(AimsicdService.FLASH_SMS);
                                Bundle smsData = new Bundle();
                                smsData.putString("address", sms.getOriginatingAddress());
                                smsData.putString("display_address",
                                        sms.getDisplayOriginatingAddress());
                                smsData.putString("class", sms.getMessageClass().name());
                                smsData.putString("service_centre", sms.getServiceCenterAddress());
                                smsData.putString("message", sms.getMessageBody());
                                smsIntent.putExtras(smsData);
                                context.sendBroadcast(smsIntent);
                                Log.i("AIMSICD_SmsReceiver", "Class 0 Message received, Sender: "
                                        + sms.getOriginatingAddress() + " Message: "
                                        + sms.getMessageBody());
                            }
                        }
                    }
                }

        } catch (NullPointerException npe) {
            Log.e("SmsReceiver", "Exception smsReceiver" + npe);
        }
    }


}

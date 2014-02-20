package com.jofrepalau.rawphone;

/*=========================================================
 Demo App Code by Ublox, modified copy and paste from:
 http://www.u-blox.com/images/downloads/Product_Docs/AndroidRIL_Source_Code_ApplicationNote_%283G.G2-CS-11003%29.pdf

 =========================================================== */

import java.lang.reflect.Method;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.*;

import com.android.internal.telephony.Phone;

public class ATRilHook extends Activity {

	private static final String LOG_TAG = "RILOemHookTestApp";
	private RadioButton mRadioButtonAPI1 = null;
	private RadioGroup mRadioGroupAPI = null;
	private Object mPhone = null;
	private AsyncResult RilRequestRaw = null;
	private AsyncResult RilRequestString = null;
	private EditText mRespText = null;
    private static Context sContext = null;

	private static final int EVENT_RIL_OEM_HOOK_CMDRAW_COMPLETE = 1300;
	private static final int EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE = 1400;
	private static final int EVENT_UNSOL_RIL_OEM_HOOK_RAW = 500;
	private static final int EVENT_UNSOL_RIL_OEM_HOOK_STR = 600;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
        sContext = this;
		setContentView(R.layout.riloemhook_layout);
		mRadioButtonAPI1 = (RadioButton) findViewById(R.id.radio_api1);
		mRadioGroupAPI = (RadioGroup) findViewById(R.id.radio_group_api);
		// Initially turn on first button.
		mRadioButtonAPI1.toggle();
		// Get our main phone object.

		makeDefaultPhones();
		getDefaultPhone();
		// mPhone = PhoneFactory.getDefaultPhone();
		// Register for OEM raw notification.
		// mPhone.mCM.setOnUnsolOemHookRaw(mHandler,EVENT_UNSOL_RIL_OEM_HOOK_RAW, null);
		// Capture text edit key press
		mRespText = (EditText) findViewById(R.id.edit_cmdstr);
		mRespText.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					Toast.makeText(ATRilHook.this, mRespText.getText(), Toast.LENGTH_SHORT).show();
					return true;
				}
				return false;
			}
		});
		Button btn = (Button) findViewById(R.id.run);

		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				execRIL(v);
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
		log("onPause()");
		// Unregister for OEM raw notification.
		// mPhone.mCM.unSetOnUnsolOemHookRaw(mHandler);
	}

	@Override
	public void onResume() {
		super.onResume();
		log("onResume()");
		// Register for OEM raw notification.
		// mPhone.mCM.setOnUnsolOemHookRaw(mHandler, EVENT_UNSOL_RIL_OEM_HOOK_RAW, null);
	}

	public void execRIL(View view) {
		// Get the checked button
		int idButtonChecked = mRadioGroupAPI.getCheckedRadioButtonId();

		// Get the response field
		mRespText = (EditText) findViewById(R.id.edit_response);
		byte[] oemhook = null;
		switch (idButtonChecked) {
		case R.id.radio_api1:
			oemhook = new byte[1];
			oemhook[0] = (byte) 0xAA;
			break;
		case R.id.radio_api2:
			oemhook = new byte[2];
			oemhook[0] = (byte) 0xBB;
			oemhook[1] = (byte) 0x55;
			break;
		case R.id.radio_api3:
			// Send OEM notification (just echo the data bytes)
			oemhook = new byte[7];
			oemhook[0] = (byte) 0xCC;
			oemhook[1] = (byte) 0x12;
			oemhook[2] = (byte) 0x34;
			oemhook[3] = (byte) 0x56;
			oemhook[4] = (byte) 0x78;
			oemhook[5] = (byte) 0x9A;
			oemhook[6] = (byte) 0xBC;
			break;
		case R.id.radio_api4:
			// Send OEM command string
			break;
		default:
			log("unknown button selected");
			break;
		}

		if (idButtonChecked != R.id.radio_api4) {
			Message msg = mHandler.obtainMessage(EVENT_RIL_OEM_HOOK_CMDRAW_COMPLETE);
			getOemRilRequestRaw(oemhook, msg);
			mRespText.setText("");
		} else {
			// Copy string from EditText and add carriage return
			String[] oemhookstring = { ((EditText) findViewById(R.id.edit_cmdstr)).getText().toString() + '\r' };

			// Create message
			Message msg = mHandler.obtainMessage(EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE);
			// Send request
			getOemRilRequestStrings(oemhookstring, msg);
			mRespText = (EditText) findViewById(R.id.edit_response);
			mRespText.setText("---Wait response---");
		}
	}

	private void logRilOemHookResponse(AsyncResult ar) {
		log("received oem hook response");
		String mResponse = "";

		if (ar.exception != null) {
			log("Exception:" + ar.exception);
			mResponse += "Exception:" + ar.exception + "\n\n";
		}

		if (ar.result != null) {
			byte[] oemResponse = (byte[]) ar.result;
			int size = oemResponse.length;
			log("oemResponse length=[" + Integer.toString(size) + "]");
			mResponse += "oemResponse length=[" + Integer.toString(size) + "]" + "\n";

			if (size > 0) {
				for (int i = 0; i < size; i++) {
					byte myByte = oemResponse[i];
					int myInt = myByte & 0xFF;
					log("oemResponse[" + Integer.toString(i) + "]=[0x" + Integer.toString(myInt, 16) + "]");
					mResponse += "oemResponse[" + Integer.toString(i) + "]=[0x" + Integer.toString(myInt, 16) + "]"
							+ "\n";
				}
			}
		} else {
			log("received NULL oem hook response");
			mResponse += "received NULL oem hook response";
		}

		// Display message box
		AlertDialog.Builder builder = new AlertDialog.Builder(sContext);
		builder.setMessage(mResponse);
		builder.setPositiveButton("OK", null);
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void logRilOemHookResponseString(AsyncResult ar) {
		log("received oem hook string response");
		String mException = "";
		// CmdRespText = (EditText) findViewById(R.id.edit_response);

		if (ar.exception != null) {
			log("Exception:" + ar.exception);
			mException += "Exception:" + ar.exception + "\n\n";
		}

		if (ar.result != null) {
			String[] oemStrResponse = (String[]) ar.result;
			int mStringSize = oemStrResponse.length;
			log("oemResponseString[0] [" + oemStrResponse[0] + "]");
			mRespText.setText("" + oemStrResponse[0]);
		} else {
			log("received NULL oem hook response");
			mRespText.setText("No response or error received");
		}
	}

	private void log(String msg) {
		Log.d(LOG_TAG, "[RIL_HOOK_OEM_TESTAPP] " + msg);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			AsyncResult ar;
			switch (msg.what) {
			case EVENT_RIL_OEM_HOOK_CMDRAW_COMPLETE:
				log("EVENT_RIL_OEM_HOOK_CMDRAW_COMPLETE");
				ar = (AsyncResult) msg.obj;
				logRilOemHookResponse(ar);
				break;
			case EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE:
				log("EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE");
				ar = (AsyncResult) msg.obj;
				logRilOemHookResponseString(ar);
				break;
			case EVENT_UNSOL_RIL_OEM_HOOK_RAW:
				break;
			case EVENT_UNSOL_RIL_OEM_HOOK_STR:
				break;
			}
		}
	};

	public static void makeDefaultPhones() throws IllegalArgumentException {

		try {
			ClassLoader cl = sContext.getClassLoader();
			@SuppressWarnings("rawtypes")
			Class PhoneFactory = cl.loadClass("com.android.internal.telephony.PhoneFactory");

			// Parameters Types
			@SuppressWarnings("rawtypes")
			Class[] paramTypes = new Class[1];
			paramTypes[0] = Context.class;

			Method get = PhoneFactory.getMethod("makeDefaultPhones", paramTypes);

			// Parameters
			Object[] params = new Object[1];
			params[0] = sContext;

			get.invoke(null, params);

		} catch (IllegalArgumentException iAE) {
			throw iAE;
		} catch (Exception e) {
			Log.e(LOG_TAG, "makeDefaultPhones", e);
		}

	}

	public static void makeDefaultPhone(Context context) throws IllegalArgumentException {

		try {
            sContext = context;
			ClassLoader cl = sContext.getClassLoader();
			@SuppressWarnings("rawtypes")
			Class PhoneFactory = cl.loadClass("com.android.internal.telephony.PhoneFactory");

			// Parameters Types
			@SuppressWarnings("rawtypes")
			Class[] paramTypes = new Class[1];
			paramTypes[0] = Context.class;

			Method get = PhoneFactory.getMethod("makeDefaultPhone", paramTypes);

			// Parameters
			Object[] params = new Object[1];
			params[0] = sContext;

			get.invoke(null, params);

		} catch (IllegalArgumentException iAE) {
			throw iAE;
		} catch (Exception e) {
			Log.e(LOG_TAG, "makeDefaultPhone", e);
		}

	}

	/*
	 * This function returns the type of the phone, depending on the network mode.
	 * 
	 * @param network mode
	 * 
	 * @return Phone Type
	 */
	public static Integer getPhoneType(Context context, int networkMode) throws IllegalArgumentException {

		Integer ret = -1;

		try {
            sContext = context;
			ClassLoader cl = sContext.getClassLoader();
			@SuppressWarnings("rawtypes")
			Class PhoneFactory = cl.loadClass("com.android.internal.telephony.PhoneFactory");

			// Parameters Types
			@SuppressWarnings("rawtypes")
			Class[] paramTypes = new Class[1];
			paramTypes[0] = Integer.class;

			Method get = PhoneFactory.getMethod("getPhoneType", paramTypes);

			// Parameters
			Object[] params = new Object[1];
			params[0] = new Integer(networkMode);

			ret = (Integer) get.invoke(PhoneFactory, params);

		} catch (IllegalArgumentException iAE) {
			throw iAE;
		} catch (Exception e) {
			ret = -1;
		}

		return ret;

	}

	public static Object getDefaultPhone() throws IllegalArgumentException {

		Object ret = null;

		try {
			ClassLoader cl = sContext.getClassLoader();
			@SuppressWarnings("rawtypes")
			Class PhoneFactory = cl.loadClass("com.android.internal.telephony.PhoneFactory");

			Method get = PhoneFactory.getMethod("getDefaultPhone", (Class[]) null);
			ret = get.invoke(null, (Object[]) null);

		} catch (IllegalArgumentException iAE) {
			throw iAE;
		} catch (Exception e) {
			Log.e(LOG_TAG, "getDefaultPhone", e);
		}

		return ret;

	}

	public static Phone getCdmaPhone() throws IllegalArgumentException {

		Phone ret = null;

		try {
			ClassLoader cl = sContext.getClassLoader();
			@SuppressWarnings("rawtypes")
			Class PhoneFactory = cl.loadClass("com.android.internal.telephony.PhoneFactory");

			Method get = PhoneFactory.getMethod("getCdmaPhone", (Class[]) null);
			ret = (Phone) get.invoke(null, (Object[]) null);

		} catch (IllegalArgumentException iAE) {
			throw iAE;
		} catch (Exception e) {
			//
		}

		return ret;

	}

	public void getOemRilRequestRaw(byte[] oemhook, Message msg)
			throws IllegalArgumentException {

		try {
			ClassLoader cl = sContext.getClassLoader();
			@SuppressWarnings("rawtypes")
			Class PhoneFactory = cl.loadClass("com.android.internal.telephony.PhoneFactory");

			// Parameters Types
			@SuppressWarnings("rawtypes")
			Class[] paramTypes = new Class[1];
			paramTypes[0] = byte[].class;
			paramTypes[1] = Message.class;

			Method get = PhoneFactory.getMethod("invokeOemRilRequestRaw", paramTypes);

			// Parameters
			Object[] params = new Object[1];
			params[0] = oemhook;
			params[1] = msg;

            // Process the response
            logRilOemHookResponse((AsyncResult) get.invoke(PhoneFactory, params));

		} catch (IllegalArgumentException iAE) {
			throw iAE;
		} catch (Exception e) {
			//
		}

	}

	public void getOemRilRequestStrings(String[] oemhook, Message msg)
			throws IllegalArgumentException {

		try {
			ClassLoader cl = sContext.getClassLoader();
			@SuppressWarnings("rawtypes")
			Class PhoneFactory = cl.loadClass("com.android.internal.telephony.PhoneFactory");

			// Parameters Types
			@SuppressWarnings("rawtypes")
			Class[] paramTypes = new Class[1];
			paramTypes[0] = String[].class;
			paramTypes[1] = Message.class;

			Method get = PhoneFactory.getMethod("invokeOemRilRequestStrings", paramTypes);

			// Parameters
			Object[] params = new Object[1];
			params[0] = oemhook;
			params[1] = msg;

            // Process the response
            logRilOemHookResponseString((AsyncResult) get.invoke(PhoneFactory, params));

		} catch (IllegalArgumentException iAE) {
			throw iAE;
		} catch (Exception e) {
			//
		}

	}
}

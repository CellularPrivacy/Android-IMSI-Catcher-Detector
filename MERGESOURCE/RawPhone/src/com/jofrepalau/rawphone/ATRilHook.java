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
import com.android.internal.telephony.PhoneFactory;

public class ATRilHook extends Activity {

	private static final String LOG_TAG = "RILOemHookTestApp";
	private RadioButton mRadioButtonAPI1 = null;
	private RadioGroup mRadioGroupAPI = null;
	private EditText mRespText = null;
    private ListView mListView;
    private String[] mDisplay;
	private Phone mPhone = null;
    private OemCommands mOemCommands;
	private static Context sContext = null;

    private int mCurrentSvcMode = OemCommands.OEM_SM_TYPE_TEST_MANUAL;
    private int mCurrentModeType = OemCommands.OEM_SM_TYPE_SUB_ENTER;

	private static final int EVENT_RIL_OEM_HOOK_CMDRAW_COMPLETE = 1300;
	private static final int EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE = 1400;
	private static final int EVENT_UNSOL_RIL_OEM_HOOK_RAW = 500;
	private static final int EVENT_UNSOL_RIL_OEM_HOOK_STR = 600;
    private static final int ID_SERVICE_MODE_REFRESH = 1001;
    private static final int ID_SERVICE_MODE_REQUEST = 1008;
    private static final int ID_SERVICE_MODE_END = 1009;

    private static final int DIALOG_INPUT = 0;
    private static final int CHARS_PER_LINE = 34;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		sContext = this;
		setContentView(R.layout.riloemhook_layout);
        mListView = (ListView)findViewById(R.id.displayList);
		mRadioButtonAPI1 = (RadioButton) findViewById(R.id.radio_api1);
		mRadioGroupAPI = (RadioGroup) findViewById(R.id.radio_group_api);
		// Initially turn on first button.
		mRadioButtonAPI1.toggle();
		// Get our main phone object.

		mPhone = PhoneFactory.getDefaultPhone();
        mOemCommands = OemCommands.getInstance(sContext);

		/*
		 * makeDefaultPhones(); getDefaultPhone();
		 */

		mRespText = (EditText) findViewById(R.id.edit_cmdstr);
		mRespText.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					Toast.makeText(sContext, mRespText.getText(), Toast.LENGTH_SHORT).show();
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
	}

	@Override
	public void onResume() {
		super.onResume();
		log("onResume()");
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
         case R.id.radio_api5:
             // Ciphering Indicator
             mCurrentSvcMode = OemCommands.OEM_SM_ENTER_MODE_MESSAGE;
             mCurrentModeType = OemCommands.OEM_SM_TYPE_SUB_CIPHERING_PROTECTION_ENTER;
             byte[] data = mOemCommands.getEnterServiceModeData(mCurrentSvcMode, mCurrentModeType, OemCommands.OEM_SM_ACTION);
             sendRequest(data, ID_SERVICE_MODE_REQUEST);
             break;
		default:
			log("unknown button selected");
			break;
		}


        if (idButtonChecked == R.id.radio_api5) {
            byte[] data = mOemCommands.getEnterServiceModeData(mCurrentSvcMode, mCurrentModeType, OemCommands.OEM_SM_ACTION);
            sendRequest(data, ID_SERVICE_MODE_REQUEST);
        } else if (idButtonChecked != R.id.radio_api4) {
			Message msg = mHandler.obtainMessage(EVENT_RIL_OEM_HOOK_CMDRAW_COMPLETE);
			mPhone.invokeOemRilRequestRaw(oemhook, msg);
			mRespText.setText("");
		} else {
			// Copy string from EditText and add carriage return
			String[] oemhookstring = { ((EditText) findViewById(R.id.edit_cmdstr)).getText().toString() + '\r' };

			// Create message
			Message msg = mHandler.obtainMessage(EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE);
			// Send request
			mPhone.invokeOemRilRequestStrings(oemhookstring, msg);
			mRespText = (EditText) findViewById(R.id.edit_response);
			mRespText.setText("---Wait response---");
		}

		/*
		 * if (idButtonChecked != R.id.radio_api4) { Message msg =
		 * mHandler.obtainMessage(EVENT_RIL_OEM_HOOK_CMDRAW_COMPLETE); getOemRilRequestRaw(oemhook, msg);
		 * mRespText.setText(""); } else { // Copy string from EditText and add carriage return String[] oemhookstring =
		 * { ((EditText) findViewById(R.id.edit_cmdstr)).getText().toString() + '\r' };
		 * 
		 * // Create message Message msg = mHandler.obtainMessage(EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE); // Send request
		 * getOemRilRequestStrings(oemhookstring, msg); mRespText = (EditText) findViewById(R.id.edit_response);
		 * mRespText.setText("---Wait response---"); }
		 */
	}

	private void logRilOemHookResponse(AsyncResult ar) {
		log("received oem hook response");
		String str = new String("");

		if (ar.exception != null) {
			log("Exception:" + ar.exception);
			str += "Exception:" + ar.exception + "\n\n";
		}

		if (ar.result != null) {
			byte[] oemResponse = (byte[]) ar.result;
			int size = oemResponse.length;
			log("oemResponse length=[" + Integer.toString(size) + "]");
			str += "oemResponse length=[" + Integer.toString(size) + "]" + "\n";

			if (size > 0) {
				for (int i = 0; i < size; i++) {
					byte myByte = oemResponse[i];
					int myInt = (int) (myByte & 0xFF);
					log("oemResponse[" + Integer.toString(i) + "]=[0x" + Integer.toString(myInt, 16) + "]");
					str += "oemResponse[" + Integer.toString(i) + "]=[0x" + Integer.toString(myInt, 16) + "]" + "\n";
				}
			}
		} else {
			log("received NULL oem hook response");
			str += "received NULL oem hook response";
		}

		// Display message box
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(str);
		builder.setPositiveButton("OK", null);
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void logRilOemHookResponseString(AsyncResult ar) {
		log("received oem hook string response");
		String str = new String("");
		mRespText = (EditText) findViewById(R.id.edit_response);

		if (ar.exception != null) {
			log("Exception:" + ar.exception);
			str += "Exception:" + ar.exception + "\n\n";
		}

		if (ar.result != null) {
			String[] oemStrResponse = (String[]) ar.result;
			int sizeStr = oemStrResponse.length;
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
            AsyncResult result;
			switch (msg.what) {
            case ID_SERVICE_MODE_REFRESH:
                Log.v(LOG_TAG, "Tick");
                byte[] data = null;
                switch(mCurrentSvcMode) {
                    case OemCommands.OEM_SM_ENTER_MODE_MESSAGE:
                        data = mOemCommands.getEnterServiceModeData(0, 0, OemCommands.OEM_SM_QUERY);
                        break;
                    case OemCommands.OEM_SM_PROCESS_KEY_MESSAGE:
                        data = mOemCommands.getPressKeyData('\0', OemCommands.OEM_SM_QUERY);
                        break;
                    default:
                        Log.e(LOG_TAG, "Unknown mode: " + mCurrentSvcMode);
                        break;
                }

                if (data != null) {
                    sendRequest(data, ID_SERVICE_MODE_REQUEST);
                }
                break;
            case ID_SERVICE_MODE_REQUEST:
                result = (AsyncResult)msg.obj;
                if (result.exception != null) {
                    Log.e(LOG_TAG, "", result.exception);
                    return;
                }
                if (result.result == null) {
                    Log.v(LOG_TAG, "No need to refresh.");
                    return;
                }
                byte[] aob = (byte[])result.result;

                if (aob.length == 0) {
                    Log.v(LOG_TAG, "Length = 0");
                    return;
                }

                int lines = aob.length / CHARS_PER_LINE;

                if (mDisplay == null || mDisplay.length != lines) {
                    Log.v(LOG_TAG, "New array = " + lines);
                    mDisplay = new String[lines];
                }

                for (int i = 0; i < lines; i++) {
                    StringBuilder strb = new StringBuilder(CHARS_PER_LINE);
                    for (int j = 2; i < CHARS_PER_LINE; j++) {
                        int pos = i * CHARS_PER_LINE + j;
                        if (pos >= aob.length) {
                            Log.e(LOG_TAG, "Unexpected EOF");
                            break;
                        }
                        if (aob[pos] == 0) {
                            break;
                        }
                        strb.append((char)aob[pos]);
                    }
                    mDisplay[i] = strb.toString();
                }

                mListView.setAdapter(new ArrayAdapter<String>(
                        ATRilHook.this, R.layout.list_item, mDisplay));

                if (mDisplay[0].contains("End service mode")) {
                    finish();
                } else if (((mDisplay[0].contains("[")) && (mDisplay[0].contains("]")))
                        || ((mDisplay[1].contains("[")) && (mDisplay[1].contains("]")))) {
                    // This is a menu, don't refresh
                } else if ((mDisplay[0].length() != 0) && (mDisplay[1].length() == 0)
                        && (mDisplay[0].charAt(1) > 48) && (mDisplay[0].charAt(1) < 58)) {
                    // Only numerical display, refresh
                    mHandler.sendEmptyMessageDelayed(ID_SERVICE_MODE_REFRESH, 200);
                } else {
                    // Periodical refresh
                    mHandler.sendEmptyMessageDelayed(ID_SERVICE_MODE_REFRESH, 1500);
                }
                break;
            case ID_SERVICE_MODE_END:
                Log.v(LOG_TAG, "Service Mode End");
                break;
            case EVENT_RIL_OEM_HOOK_CMDRAW_COMPLETE:
                log("EVENT_RIL_OEM_HOOK_CMDRAW_COMPLETE");
                result = (AsyncResult)msg.obj;
                logRilOemHookResponse(result);
                break;
            case EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE:
                log("EVENT_RIL_OEM_HOOK_CMDSTR_COMPLETE");
                result = (AsyncResult) msg.obj;
                logRilOemHookResponseString(result);
                break;
            }
		}

	};

    private void sendRequest(byte[] data, int id) {
        Message msg = mHandler.obtainMessage(id);
        mPhone.invokeOemRilRequestRaw(data, msg);
    }

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

			Toast toast = Toast.makeText(sContext, "makeDefaultPhones Completed!", Toast.LENGTH_SHORT);
			toast.show();

		} catch (IllegalArgumentException iAE) {
			throw iAE;
		} catch (Exception e) {
			Log.e(LOG_TAG, "makeDefaultPhones", e);
		}

	}

	public static void makeDefaultPhone() throws IllegalArgumentException {

		try {
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

			Toast toast = Toast.makeText(sContext, "getDefaultPhone Completed!", Toast.LENGTH_SHORT);
			toast.show();

		} catch (IllegalArgumentException iAE) {
			throw iAE;
		} catch (Exception e) {
			Log.e(LOG_TAG, "getDefaultPhone", e);
		}

		return ret;

	}

	public void getOemRilRequestRaw(byte[] oemhook, Message msg) throws IllegalArgumentException {

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
			get.invoke(PhoneFactory, params);
			Toast toast = Toast.makeText(sContext, "invoke OemRilRequestRaw Completed!", Toast.LENGTH_SHORT);
			toast.show();

		} catch (IllegalArgumentException iAE) {
			throw iAE;
		} catch (Exception e) {
			//
		}

	}

	public void getOemRilRequestStrings(String[] oemhook, Message msg) throws IllegalArgumentException {

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
			get.invoke(PhoneFactory, params);
			Toast toast = Toast.makeText(sContext, "invoke OemRilRequestStrings Completed!", Toast.LENGTH_SHORT);
			toast.show();
		} catch (IllegalArgumentException iAE) {
			throw iAE;
		} catch (Exception e) {
			//
		}

	}
}

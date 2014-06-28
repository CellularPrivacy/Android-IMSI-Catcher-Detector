package com.SecUpwN.AIMSICD.fragments;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.utils.Helpers;
import com.SecUpwN.AIMSICD.utils.Shell;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AtCommandFragment extends Fragment {

    //Return value constants
    private final int SERIAL_INIT_OK = 200;
    private final int SERIAL_INIT_ERROR = 201;
    private final int ROOT_UNAVAILABLE = 202;
    private final int BUSYBOX_UNAVAILABLE = 203;

    private final int EXECUTE_AT = 300;
    private final int EXECUTE_COMMAND = 301;

    //System items
    private Context mContext;
    private Shell mShell = null;
    private String mSerialDevice;
    private final List<String> mSerialDevices = new ArrayList<>();

    private List<String> mOutput;
    private List<String> mError;

    //Layout items
    private View mView;
    private RelativeLayout mAtCommandLayout;
    private TextView mAtCommandError;
    private TextView mSerialDeviceDisplay;
    private Button mAtCommandExecute;
    private TextView mAtResponse;
    private EditText mAtCommand;
    private Spinner mSerialDeviceSpinner;
    private TextView mSerialDeviceSpinnerLabel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.at_command_fragment, container, false);
        if (mView != null) {
            mAtCommandLayout = (RelativeLayout) mView.findViewById(R.id.atcommandView);
            mAtCommandError = (TextView) mView.findViewById(R.id.at_command_error);
            mAtCommandExecute = (Button) mView.findViewById(R.id.execute);
            mSerialDeviceDisplay = (TextView) mView.findViewById(R.id.serial_device);
            mAtResponse = (TextView) mView.findViewById(R.id.response);
            mAtCommand = (EditText) mView.findViewById(R.id.at_command);
            mAtCommandExecute.setOnClickListener(new btnClick());
            mSerialDeviceSpinner = (Spinner) mView.findViewById(R.id.serial_device_spinner);
            mSerialDeviceSpinner.setOnItemSelectedListener(new spinnerListener());
            mSerialDeviceSpinnerLabel = (TextView) mView.findViewById(R.id.serial_device_spinner_title);
        }

        return mView;
    }

    private class spinnerListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                int position, long id) {
            mSerialDevice = String.valueOf(mSerialDeviceSpinner.getSelectedItem());
            mSerialDeviceDisplay.setText(mSerialDevice);
            mShell.setSerialDevice(mSerialDevice);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getBaseContext();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mShell != null) {
            mShell.close();
            mShell = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        int serialDevice = initSerialDevice();

        switch (serialDevice) {
            case SERIAL_INIT_OK:
                mAtCommandLayout.setVisibility(View.VISIBLE);
                break;
            case ROOT_UNAVAILABLE:
                String rootUnavailable = "Unable to acquire ROOT access on your device. \n\n" +
                        "AT Command Injection requires ROOT Terminal access. \n\n" +
                        "Please check your device is ROOTED and try again";
                mAtCommandError.setText(rootUnavailable);
                break;
            case BUSYBOX_UNAVAILABLE:
                String busyboxUnavailable = "Unable to detect Busybox on your device. \n\n" +
                        "AT Command Injection requires Busybox components to function correctly. \n\n"
                        + "Please check your device has Busybox installed and try again";
                mAtCommandError.setText(busyboxUnavailable);
                break;
            case SERIAL_INIT_ERROR:
                String error =
                        "An unknown error has occurred trying to acquire the Serial Device.\n\n"
                                + "Please check your logcat for any errors and post them to Github "
                                + "or XDA, links to both of these locations can be found within the "
                                + "About section of the application.";
                mAtCommandError.setText(error);
                break;
            default:
                String unknownError =
                        "An unknown error has occurred trying to initialise the AT Command Injector.\n\n"
                                + "Please check your logcat for any errors and post them to Github or "
                                + "XDA, links to both of these locations can be found within the "
                                + "About section of the application.";
                mAtCommandError.setText(unknownError);
                break;
        }

    }

    private class btnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (mAtCommand.getText() != null) {
                String command = mAtCommand.getText().toString();
                if (command.indexOf("AT") == 0 || command.indexOf("at") == 0) {
                    Log.i("AIMSICD", "AT Command Detected");
                    new MyAsync().execute(EXECUTE_AT);
                } else {
                    Log.i("AIMSICD", "Terminal Command Detected");
                    new MyAsync().execute(EXECUTE_COMMAND);
                }
            }
        }
    }

    private int initSerialDevice() {

        //Check for root access
        boolean root = Helpers.checkSu();
        if (!root) {
            return ROOT_UNAVAILABLE;
        }

        //Check busybox is installed
        boolean busybox = Helpers.checkBusybox();
        if (!busybox) {
            return BUSYBOX_UNAVAILABLE;
        }

        if (mShell == null) {
            mShell = new Shell();
        }

        //Draw Ril Serial Device details from the System Property
        String rilDevice = Helpers.getSystemProp(mContext, "rild.libargs", "UNKNOWN");
        mSerialDevice = (rilDevice.equals("UNKNOWN") ? rilDevice : rilDevice.substring(3));

        if (!mSerialDevice.equals("UNKNOWN")) {
            mSerialDevices.add(mSerialDevice);
        }

        boolean result = mShell.sendCommandPreserveOut("ls /dev/radio | grep atci*", 5.0f);
        if (result) {
            mOutput = new ArrayList<>();
            mOutput = mShell.GetStdOut();
            mError = new ArrayList<>();
            mError = mShell.GetStdErr();
        }
        if (mOutput != null) {
            for (String device : mOutput) {
                mSerialDevices.add("/dev/radio/" + device.trim());
            }
        }
        if (mError != null) {
            for (String error : mError) {
                mAtResponse.append(error + "\n");
            }
        }

        //Now try xgold modem config
        File xgold = new File("/system/etc/ril_xgold_radio.cfg");
        if (xgold.exists() && xgold.isFile()) {
            result = mShell.sendCommandPreserveOut("cat /system/etc/ril_xgold_radio.cfg | "
                    + "grep -E \"atport*|dataport*\"", 5.0f);
            if (result) {
                mOutput = new ArrayList<>();
                mOutput = mShell.GetStdOut();
                mError = new ArrayList<>();
                mError = mShell.GetStdErr();
            }
        }

        if (mOutput != null) {
            for (String device : mOutput) {
                if (device.contains("/dev/")) {
                    int place = device.indexOf("=") + 1;
                    mSerialDevices.add(device.substring(place, device.length()-1));
                }
            }
        }
        if (mError != null) {
            for (String error : mError) {
                mAtResponse.append(error + "\n");
            }
        }

        if (!mSerialDevices.isEmpty()) {
            mSerialDevice = mSerialDevices.get(0);
            mShell.setSerialDevice(mSerialDevices.get(0));
            String[] entries = new String[mSerialDevices.size()];
            entries = mSerialDevices.toArray(entries);
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(mContext,
                    android.R.layout.simple_spinner_item, entries);
            mSerialDeviceSpinner.setAdapter(spinnerAdapter);
            mSerialDeviceSpinner.setVisibility(View.VISIBLE);
            mSerialDeviceSpinnerLabel.setVisibility(View.VISIBLE);
        }

        mAtResponse.setVisibility(View.VISIBLE);

        return SERIAL_INIT_OK;
    }

    private boolean executeAT() {
        boolean result = false;
        if (mAtCommand.getText() != null) {
            result = mShell.executeAt(mAtCommand.getText().toString());
            if (result) {
                mOutput = new ArrayList<>();
                mOutput = mShell.GetStdOut();
                mError = new ArrayList<>();
                mError = mShell.GetStdErr();
            }
        }
        return result;
    }

    private boolean executeCommand() {
        boolean result = false;
        if (mAtCommand.getText() != null) {
            result = mShell.sendCommandPreserveOut(mAtCommand.getText().toString(), 5.0f);
            if (result) {
                mOutput = new ArrayList<>();
                mOutput = mShell.GetStdOut();
                mError = new ArrayList<>();
                mError = mShell.GetStdErr();
            }
        }
        return result;
    }

    private void updateDisplay() {
        String displayOutput = "";
        if (mOutput == null && mError == null) {
            displayOutput = "Command Timeout/No Response\n";
        } else {
            if (mOutput != null) {
                for (String output : mOutput) {
                    displayOutput += output + "\n";
                }
            } else {
                for (String error : mError) {
                    displayOutput += error + "\n";
                }
            }
        }

        mAtResponse.append((displayOutput.isEmpty())
                ? "Command Timeout/No Response\n" : displayOutput + "\n" );
    }

    private class MyAsync extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer ... params) {
            switch (params[0]) {
                case EXECUTE_AT:
                    return executeAT();
                case EXECUTE_COMMAND:
                    return executeCommand();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            updateDisplay();
        }
    }
}

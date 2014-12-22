package com.SecUpwN.AIMSICD.fragments;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.utils.Helpers;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AtCommandFragment extends Fragment {

    //Return value constants
    private final int SERIAL_INIT_OK = 100;
    private final int SERIAL_INIT_ERROR = 101;
    private final int ROOT_UNAVAILABLE = 102;
    private final int BUSYBOX_UNAVAILABLE = 103;

    private final int EXECUTE_AT = 200;
    private final int EXECUTE_COMMAND = 201;

    private final int SET_DEVICE = 300;

    private Context mContext;
    private String mSerialDevice;
    private int mTimeout;
    private final List<String> mSerialDevices = new ArrayList<>();

    private RelativeLayout mAtCommandLayout;
    private TextView mAtCommandError;
    private TextView mSerialDeviceDisplay;

    private TextView mAtResponse;
    private EditText mAtCommand;
    private Spinner mSerialDeviceSpinner;
    private TextView mSerialDeviceSpinnerLabel;

    private Shell shell;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.at_command_fragment, container, false);
        if (view != null) {
            mAtCommandLayout = (RelativeLayout) view.findViewById(R.id.atcommandView);
            mAtCommandError = (TextView) view.findViewById(R.id.at_command_error);
            Button atCommandExecute = (Button) view.findViewById(R.id.execute);
            mSerialDeviceDisplay = (TextView) view.findViewById(R.id.serial_device);
            mAtResponse = (TextView) view.findViewById(R.id.response);
            mAtCommand = (EditText) view.findViewById(R.id.at_command);
            atCommandExecute.setOnClickListener(new btnClick());
            mSerialDeviceSpinner = (Spinner) view.findViewById(R.id.serial_device_spinner);
            mSerialDeviceSpinner.setOnItemSelectedListener(new spinnerListener());
            mSerialDeviceSpinnerLabel = (TextView) view
                    .findViewById(R.id.serial_device_spinner_title);
            Spinner timoutSpinner = (Spinner) view.findViewById(R.id.timeout_spinner);
            timoutSpinner.setOnItemSelectedListener(new timoutSpinnerListener());
            timoutSpinner.setSelection(1);
            mTimeout = 5000;
        }

        return view;
    }

    private class timoutSpinnerListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                int position, long id) {
            switch (position) {
                case 0: //2 seconds
                    mTimeout = 2000;
                    break;
                case 1: //5 seconds
                    mTimeout = 5000;
                    break;
                case 2: //8 seconds
                    mTimeout = 8000;
                    break;
                case 3: //10 seconds
                    mTimeout = 10000;
                    break;
                case 4: //15 seconds
                    mTimeout = 15000;
                    break;
                default:
                    mTimeout = 5000;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {

        }
    }

    private class spinnerListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                int position, long id) {
            mSerialDevice = String.valueOf(mSerialDeviceSpinner.getSelectedItem());
            mSerialDeviceDisplay.setText(mSerialDevice);
            setSerialDevice();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {

        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getBaseContext();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (shell != null) {
            try {
                shell.close();
            } catch (Exception e) {
                Log.e("AIMSICD", "Closing shell: " + e);
            }
            Log.i("AIMSICD", "AT Shell Closed");
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

                // E:V:A This need to be fixed, AT commands can often be lowercase,
                // and it's possible that some OEM AT's are case sensitive.
                // Let's try to not mix terminal shell access with AT commands.
                // May still be useful for debugging PID and GID/UID etc.

                //if (command.toUpperCase().indexOf("AT") == 0) {
                    Log.i("AIMSICD", "AT Command Detected: " + command );
                    executeAT();
                //} else {
                //    Log.i("AIMSICD", "Terminal Command Detected");
                //    executeCommand();
                //}
            }
        }
    }

    private int initSerialDevice() {

        // Check for root access
        boolean root = RootTools.isAccessGiven();
        if (!root) {
            return ROOT_UNAVAILABLE;
        }

        // Check if Busybox is installed
        boolean busybox = RootTools.isBusyboxAvailable();
        if (!busybox) {
            return BUSYBOX_UNAVAILABLE;
        }

        try {
            shell = RootTools.getShell(true);

            mAtResponse.setText("*** Setting Up... Ignore any errors. ***\n");

            mSerialDevices.clear();
            // Use RIL Serial Device details from the System Property
            String rilDevice = Helpers.getSystemProp(mContext, "rild.libargs", "UNKNOWN");
            mSerialDevice = (rilDevice.equals("UNKNOWN") ? rilDevice : rilDevice.substring(3));

            if (!mSerialDevice.equals("UNKNOWN")) {
                mSerialDevices.add(mSerialDevice);
            }

            //==================================================================
            // WARNING:  Scraping commands can be masked by aliases in: mkshrc
            //           and even hardcoded in the sh binary or elsewhere.
            //           To get unaliased versions, use: "\\<command>"
            //==================================================================

            // MTK: Check for ATCI devices and add found location to the serial device list
            // XMM: Unknown
            // QC: /dev/smd[0-7]
            CommandCapture cmd = new CommandCapture(0, "\\ls /dev/radio | grep atci*") {

                @Override
                public void commandOutput(int id, String line) {
                    if (id == 0) {
                        if (!line.trim().equals("") &&
                                !mSerialDevices.contains("/dev/radio/" + line.trim())) {
                            mSerialDevices.add("/dev/radio/" + line.trim());
                            mAtResponse.append("Found: " + line.trim());
                        }
                    }
                }
            };

            shell.add(cmd);
            commandWait(shell, cmd);

            // Now try XMM/XGOLD modem config
            File xgold = new File("/system/etc/ril_xgold_radio.cfg");
            if (xgold.exists() && xgold.isFile()) {
                cmd = new CommandCapture(1, "cat /system/etc/ril_xgold_radio.cfg | "
                        + "grep -E \"atport*|dataport*\"") {

                    @Override
                    public void commandOutput(int id, String line) {
                        if (id == 0) {
                            if (!line.trim().equals("") && line.contains("/dev/")) {
                                int place = line.indexOf("=") + 1;
                                mSerialDevices.add(line.substring(place, line.length() - 1));
                                mAtResponse.append(line.substring(place, line.length() - 1));
                            }
                        }
                    }
                };

                shell.add(cmd);
                commandWait(shell, cmd);

            }

        } catch (Exception e) {
            Log.e("AIMSICD", "initSerialDevice " + e);
        }


        if (!mSerialDevices.isEmpty()) {
            mSerialDevice = mSerialDevices.get(0);
            setSerialDevice();
            String[] entries = new String[mSerialDevices.size()];
            entries = mSerialDevices.toArray(entries);
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(mContext,
                    android.R.layout.simple_spinner_item, entries);
            mSerialDeviceSpinner.setAdapter(spinnerAdapter);
            mSerialDeviceSpinner.setVisibility(View.VISIBLE);
            mSerialDeviceSpinnerLabel.setVisibility(View.VISIBLE);
        }

        mAtResponse.append("*** Setup Complete ***\n");
        mAtResponse.setVisibility(View.VISIBLE);

        return SERIAL_INIT_OK;
    }

    private void setSerialDevice() {
        CommandCapture cmd = new CommandCapture(SET_DEVICE, "cat " + mSerialDevice + " \u0026\n");
        try {
            shell.add(cmd);
            commandWait(shell, cmd);
            Log.i("AIMSICD", "setSerialDevice finished on " + mSerialDevice);
        } catch (Exception e) {
            Log.e("AIMSICD", "setSerialDevice " + e);
        }
    }

    private void executeAT() {
        if (mAtCommand.getText() != null) {
            try {
                // E:V:A  It seem that MTK devices doesn't need "\r" but QC devices do.
                // We need a device-type check here, perhaps: gsm.version.ril-impl.
                CommandCapture cmd = new CommandCapture(EXECUTE_AT,
                        "echo -e " + mAtCommand.getText().toString() +"\r >" + mSerialDevice + "\n") {

                    @Override
                    public void commandOutput(int id, String line) {
                        if (id == EXECUTE_AT) {
                            if (!line.trim().equals("")) {
                                mAtResponse.append(line + "\n");
                            }
                        }
                    }
                };
                Log.i("AIMSICD", "Trying to executeAT: " + cmd);
                shell.add(cmd);
                commandWait(shell, cmd);
            } catch (Exception e) {
                Log.e("AIMSICD", "Failed to executeAT: " + e);
            }
        }

    }

    private void executeCommand() {
        if (mAtCommand.getText() != null) {
            try {
                CommandCapture cmd = new CommandCapture(EXECUTE_COMMAND,
                        mAtCommand.getText().toString() + "\n") {

                    @Override
                    public void commandOutput(int id, String line) {
                        if (id == EXECUTE_COMMAND) {
                            if (!line.trim().equals("")) {
                                mAtResponse.append(line + "\n");
                            }
                        }
                    }

                };

                Log.i("AIMSICD", "Trying to executeCommand: " + cmd);
                shell.add(cmd);
                commandWait(shell, cmd);
            } catch (Exception e) {
                Log.e("AIMSICD", "Failed to executeCommand: " + e);
            }
        }
    }

    /**
     * This below method is part of the RootTools Project: http://code.google.com/p/roottools/
     * Copyright (c) 2012 Stephen Erickson, Chris Ravenscroft, Dominik Schuermann, Adam Shanks
     *
     * Slightly modified commandWait method as found in RootToolsInternalMethods.java to utilise
     * the user selected timeout value.
     *
     */
    private void commandWait(Shell shell, Command cmd) throws Exception {
        while (!cmd.isFinished()) {
            synchronized (cmd) {
                try {
                    if (!cmd.isFinished()) {
                        cmd.wait(mTimeout);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!cmd.isExecuting() && !cmd.isFinished()) {
                if (!shell.isExecuting && !shell.isReading) {
                    Log.e("AIMSICD", "Waiting for a command to be executed in a shell that is not executing and not reading! \n\n Command: " + cmd.getCommand());
                    Exception e = new Exception();
                    e.setStackTrace(Thread.currentThread().getStackTrace());
                    e.printStackTrace();
                } else if (shell.isExecuting && !shell.isReading) {
                    Log.e("AIMSICD", "Waiting for a command to be executed in a shell that is executing but not reading! \n\n Command: " + cmd.getCommand());
                    Exception e = new Exception();
                    e.setStackTrace(Thread.currentThread().getStackTrace());
                    e.printStackTrace();
                } else {
                    Log.e("AIMSICD", "Waiting for a command to be executed in a shell that is not reading! \n\n Command: " + cmd.getCommand());
                    Exception e = new Exception();
                    e.setStackTrace(Thread.currentThread().getStackTrace());
                    e.printStackTrace();
                }
            }
        }
    }
}
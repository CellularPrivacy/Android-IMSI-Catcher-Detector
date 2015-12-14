/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
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

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.utils.Helpers;
import com.SecUpwN.AIMSICD.utils.atcmd.AtCommandTerminal;
import com.SecUpwN.AIMSICD.utils.atcmd.TtyPrivFile;
import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.freefair.android.util.logging.AndroidLogger;
import io.freefair.android.util.logging.Logger;


/**
 *  Description:    This is the AT Command Interface or AT Command Processor (ATCoP) that
 *                  allow the user to communicate directly to the baseband processor (BP),
 *                  via old-school AT commands. This can be very useful for debugging radio
 *                  related network problems on those devices that are using this interface.
 *                  The most common baseband hardware that allow for this are those made by
 *                  Qualcomm (MSM) or Mediatek (MTK). Intel XMM based devices have been found
 *                  very difficult or impossible to use this, especailly on Samsung devices.
 *
 *  Requirements:   1) You need to have supported hardware that already has an AT serial device
 *                  enumerated in the Android /dev tree. Some common ones are:
 *                      Qualcomm:   /dev/smd[0,7]
 *                      MTK:        /dev/radio/atci[0-9]
 *                      XMM:        TBA
 *
 *                  2) You need to be rooted as this interface is using a persistent root shell.
 *
 *  Issues:
 *              [ ] Need to increase time for long AT commands like "AT+COPS=?" (~30 sec)
 *              [ ] Need a "no" timeout to watch output for while, or let's make it 10 minutes.
 *                  Perhaps with a manual stop?
 *
 *  ChangeLog:
 *              2015-02-11  E:V:A       Testing to add back some old working code
 *              2015-02-16  scintill    Made some code changes and added /utils/atcmd/...
 *              2015-02-11  E:V:A       Added 10 minutes timeout
 *
 *
 */
public class AtCommandFragment extends Fragment {

    private final Logger log = AndroidLogger.forClass(AtCommandFragment.class);

    //Return value constants
    private static final int SERIAL_INIT_OK = 100;
    private static final int SERIAL_INIT_ERROR = 101;
    private static final int ROOT_UNAVAILABLE = 102;
    private static final int BUSYBOX_UNAVAILABLE = 103;
    private static final List<String> mSerialDevices = new ArrayList<>();

    private Context mContext;
    private String mSerialDevice;
    private int mTimeout;

    private RelativeLayout mAtCommandLayout;
    private TextView mAtCommandError;
    private TextView mSerialDeviceDisplay;

    private TextView mAtResponse;
    private EditText mAtCommand;
    private Spinner mSerialDeviceSpinner;
    private TextView mSerialDeviceSpinnerLabel;

    private AtCommandTerminal mCommandTerminal;

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
            mSerialDeviceSpinnerLabel = (TextView) view.findViewById(R.id.serial_device_spinner_title);
            Spinner timeoutSpinner = (Spinner) view.findViewById(R.id.timeout_spinner);
            timeoutSpinner.setOnItemSelectedListener(new timeoutSpinnerListener());
            timeoutSpinner.setSelection(1);
            mTimeout = 5000;
        }

        return view;
    }

    private class timeoutSpinnerListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                int position, long id) {
            switch (position) {
                // Don't forget to also change the arrays.xml
                case 0: //2 seconds
                    mTimeout = 2000;
                    break;
                case 1: //5 seconds
                    mTimeout = 5000;
                    break;
                case 2: //10 seconds
                    mTimeout = 10000;
                    break;
                case 3: //20 seconds
                    mTimeout = 20000;
                    break;
                case 4: //30 seconds
                    mTimeout = 30000;
                    break;
                case 5: // No timeout, when watching output...
                    mTimeout = 600000; // Well, ok, 10 min
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
        if (mCommandTerminal != null) {
            mCommandTerminal.dispose();
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
                mAtCommandError.setText(mContext.getString(R.string.unable_to_acquire_root_access));
                break;
            case BUSYBOX_UNAVAILABLE:
                mAtCommandError.setText(mContext.getString(R.string.unable_to_detect_busybox));
                break;
            case SERIAL_INIT_ERROR:
                mAtCommandError.setText(mContext.getString(R.string.unknown_error_trying_to_acquire_serial_device));
                break;
            default:
                mAtCommandError.setText(mContext.getString(R.string.unknown_error_initialising_at_command_injector));
                break;
        }

    }

    private class btnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (mAtCommand.getText() != null) {
                String command = mAtCommand.getText().toString();
                log.info("AT Command Detected: " + command);
                executeAT();
            }
        }
    }

    /**
     *  Description:    This is looking for possible serial devices that may be used for ATCoP.
     *
     *  Issues:         This is generally not working since it is very HW and SW dependent.
     *
     *
     * @return
     */
    private int initSerialDevice() {

        /**
         * NOTE:
         *
         *      Because of how RootShell is being used the handler has to be disabled.
         *
         *      With the handler disabled absolutely NO UI work can be done in the callback methods
         *      since they will be called on a separate thread.
         *
         *      To work around this, either:
         *
         *      a) Execute all Shell commands in a thread, such as AsyncTask
         *          OR
         *      b) Stop using commandWait (which is a no no...you should never sleep on the
         *         main UI thread) and implement the callback commandFinished/commandTerminated
         *         to determine when to continue on.
         *
         */
        RootShell.handlerEnabled = false;

        // Check for root access
        boolean root = RootShell.isAccessGiven();
        if (!root) {
            return ROOT_UNAVAILABLE;
        }

        // Check if Busybox is installed
        boolean busybox = RootShell.isBusyboxAvailable();
        if (!busybox) {
            return BUSYBOX_UNAVAILABLE;
        }

        try {
            mAtResponse.setText(R.string.at_command_response_looking);
            mSerialDevices.clear();

            // THIS IS A BAD IDEA       TODO: Consider removing
            // Use RIL Serial Device details from the System Property
            try {
                String rilDevice = Helpers.getSystemProp(mContext, "rild.libargs", "UNKNOWN");
                mSerialDevice = ("UNKNOWN".equals(rilDevice) ? rilDevice : rilDevice.substring(3));

                if (!"UNKNOWN".equals(mSerialDevice)) {
                    mSerialDevices.add(mSerialDevice);
                }
            } catch (StringIndexOutOfBoundsException e) {
                log.warn(e.getMessage());
                // ignore, move on
            }

            //==================================================================
            // WARNING:  Scraping commands can be masked by aliases in: mkshrc
            //           and even hardcoded in the sh binary or elsewhere.
            //           To get unaliased versions, use: "\\<command>"
            //==================================================================

            for (File file : new File("/dev").listFiles()) {
                String name = file.getName();
                boolean add = false;
                // QC: /dev/smd[0-7]
                if (name.matches("^smd.$")) {
                    add = true;
                } else if ("radio".equals(name)) {
                    // MTK: /dev/radio/*atci*
                    for (File subfile : file.listFiles()) {
                        String subname = subfile.getName();
                        if (subname.contains("atci")) {
                            add = true;
                            file = subfile;
                        }
                    }
                }

                if (add) {
                    mSerialDevices.add(file.getAbsolutePath());
                    mAtResponse.append(getString(R.string.at_command_response_found) + file.getAbsolutePath() + "\n");
                }
            }

            // Now try XMM/XGOLD modem config
            File xgold = new File("/system/etc/ril_xgold_radio.cfg");
            if (xgold.exists() && xgold.isFile()) {
                Command cmd = new Command(1, "\\cat /system/etc/ril_xgold_radio.cfg | "
                                            + "\\grep -E \"atport*|dataport*\"") {

                    @Override
                    public void commandOutput(int id, String line) {
                        if (id == 0) {
                            if (!line.trim().isEmpty() && line.contains("/dev/")) {
                                int place = line.indexOf("=") + 1;
                                mSerialDevices.add(line.substring(place, line.length() - 1));
                                mAtResponse.append(getString(R.string.at_command_response_found)+line.substring(place, line.length() - 1) + "\n");
                            }
                        }
                        super.commandOutput(id, line);
                    }
                };

                Shell shell = RootShell.getShell(true);
                shell.add(cmd);
                commandWait(shell, cmd);
            }

        } catch (Exception e) {
            log.error("InitSerialDevice ", e);
        }

        if (!mSerialDevices.isEmpty()) {
            String[] entries = new String[mSerialDevices.size()];
            entries = mSerialDevices.toArray(entries);
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(mContext,
                    android.R.layout.simple_spinner_item, entries);
            mSerialDeviceSpinner.setAdapter(spinnerAdapter);
            mSerialDeviceSpinner.setVisibility(View.VISIBLE);
            mSerialDeviceSpinnerLabel.setVisibility(View.VISIBLE);
        }

        mAtResponse.append(getString(R.string.at_command_response_setup_complete));
        mAtResponse.setVisibility(View.VISIBLE);

        return SERIAL_INIT_OK;
    }

    private void setSerialDevice() {
        if (mCommandTerminal != null) {
            mCommandTerminal.dispose();
            mCommandTerminal = null;
        }
    }

    private AtCommandTerminal getSerialDevice() {
        if (mCommandTerminal == null) {
            try {
                mCommandTerminal = new TtyPrivFile(mSerialDevice);
                return mCommandTerminal;
            } catch (IOException e) {
                mAtResponse.append(e.toString());
            }
        } else {
            return mCommandTerminal;
        }
        return null;
    }

    private void executeAT() {
        // It seem that MTK devices doesn't need "\r" but QC devices do.
        // We need a device-type check here, perhaps: gsm.version.ril-impl.
        Editable cmd = mAtCommand.getText();
        if (cmd != null && cmd.length() != 0) {
            log.debug("ExecuteAT: attempting to send: " + cmd.toString());

            if (getSerialDevice() != null) {
                mCommandTerminal.send(cmd.toString(), new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message message) {
                        if (message.obj instanceof List) {
                            List<String> lines = ((List<String>) message.obj);
                            StringBuffer response = new StringBuffer();
                            for (String line : lines) {
                                response.append(line);
                                response.append('\n');
                            }
                            if (response.length() != 0) {
                                mAtResponse.append(response);
                            }

                        } else if (message.obj instanceof IOException) {
                            mAtResponse.append("IOException: " + ((IOException) message.obj).getMessage() + "\n");
                        }
                    }
                }.obtainMessage());
            }
        }

    }

    /**
     * This below method is part of the RootTools Project: https://github.com/Stericson/RootTools
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
                    log.error(e.getMessage());
                }
            }
            if (!cmd.isExecuting() && !cmd.isFinished()) {
                Exception e = new Exception();

                if (!shell.isExecuting && !shell.isReading) {
                    log.warn("Waiting for a command to be executed in a shell that is not executing and not reading! \n\n Command: " + cmd.getCommand());
                    e.setStackTrace(Thread.currentThread().getStackTrace());
                    log.error(e.getMessage(), e);

                } else if (shell.isExecuting && !shell.isReading) {
                    log.error("Waiting for a command to be executed in a shell that is executing but not reading! \n\n Command: " + cmd.getCommand());
                    e.setStackTrace(Thread.currentThread().getStackTrace());
                    log.error(e.getMessage(), e);

                } else {
                    log.error("Waiting for a command to be executed in a shell that is not reading! \n\n Command: " + cmd.getCommand());
                    e.setStackTrace(Thread.currentThread().getStackTrace());
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
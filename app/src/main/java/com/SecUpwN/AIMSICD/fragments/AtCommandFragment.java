package com.SecUpwN.AIMSICD.fragments;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.utils.CMDProcessor;
import com.SecUpwN.AIMSICD.utils.CommandResult;
import com.SecUpwN.AIMSICD.utils.Helpers;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class AtCommandFragment extends Fragment {

    //Return value constants
    private final int RIL_INIT_OK = 200;
    private final int RIL_INIT_ERROR = 201;
    private final int ROOT_UNAVAILABLE = 202;
    private final int BUSYBOX_UNAVAILABLE = 203;

    //System items
    private AimsicdService mAimsicdService;
    private boolean mBound;
    private Context mContext;
    private Activity mActivity;

    //Layout items
    private View mView;
    private RelativeLayout mAtCommandLayout;
    private TextView mAtCommandError;
    private TextView mRilDeviceDisplay;
    private Button mAtCommandExecute;
    private TextView mAtResponse;
    private EditText mAtCommand;
    private String mRilDevice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.at_command_fragment, container, false);
        if (mView != null) {
            mAtCommandLayout = (RelativeLayout) mView.findViewById(R.id.atcommandView);
            mAtCommandError = (TextView) mView.findViewById(R.id.at_command_error);
            mAtCommandExecute = (Button) mView.findViewById(R.id.execute);
            mRilDeviceDisplay = (TextView) mView.findViewById(R.id.ril_device);
            mAtResponse = (TextView) mView.findViewById(R.id.response);
            mAtCommand = (EditText) mView.findViewById(R.id.at_command);
            mAtCommandExecute.setOnClickListener(new btnClick());
        }

        return mView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getBaseContext();
        mActivity = activity;
        // Bind to LocalService
        Intent intent = new Intent(mContext, AimsicdService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mBound) {
            mContext.unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mBound) {
            // Bind to LocalService
            Intent intent = new Intent(mContext, AimsicdService.class);
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

        int rilInit = initialiseRil();

        switch (rilInit) {
            case RIL_INIT_OK:
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
            case RIL_INIT_ERROR:
                String error =
                        "An unknown error has occurred trying to acquire the Ril Serial Device.\n\n"
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

    /**
     * Service Connection to bind the activity to the service
     */
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mAimsicdService = ((AimsicdService.AimscidBinder) service).getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private class btnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            executeAT();
        }
    }

    private int initialiseRil() {
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

        //Draw Ril Serial Device details from the System Property
        String rilDevice = Helpers.getSystemProp(mContext, "rild.libargs", "UNKNOWN");
        mRilDevice = (rilDevice.equals("UNKNOWN") ? rilDevice : rilDevice.substring(3));
        mRilDeviceDisplay.setText(mRilDevice);

        if (mRilDevice.equals("UNKNOWN"))
            return RIL_INIT_ERROR;

        return RIL_INIT_OK;
    }

    private void executeAT() {
        if (mAtCommand.getText() != null) {
            mAtResponse.setText("");
            String cmdSetup = "cat " + mRilDevice + " &";
            new ExecuteAtCommand().execute(cmdSetup);
            String atCommand = mAtCommand.getText().toString();
            new ExecuteAtCommand().execute("echo -e " + atCommand +"\r > " + mRilDevice);
        }
    }

    private class ExecuteAtCommand extends AsyncTask<String, Integer, CommandResult> {

        protected CommandResult doInBackground(String... atCommand) {
            return CMDProcessor.runSuCommand(atCommand[0]);
        }

        protected void onPostExecute(CommandResult result) {
            if (result != null) {
                Log.i("AIMSICD_ATCommand", "Stdout: " + result.getStdout() +
                        " StdErr: " + result.getStderr() + " Exit Value: " + result.getExitValue());
                if (!result.getStdout().isEmpty())
                    mAtResponse.append(result.getStdout());
                else if (!result.getStderr().isEmpty())
                    mAtResponse.append(result.getStderr());
                else
                    mAtResponse.append("No response or error detected...");
            }
        }
    }

}

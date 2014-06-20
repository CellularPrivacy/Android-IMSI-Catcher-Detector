package com.SecUpwN.AIMSICD.fragments;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.service.AimsicdService;
import com.SecUpwN.AIMSICD.utils.Helpers;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class AtCommandFragment extends Fragment {

    private AimsicdService mAimsicdService;
    private boolean mBound;
    private Context mContext;
    private Activity mActivity;
    private View mView;
    private Button mAtCommandExecute;
    private TextView mAtResponse;
    private EditText mAtCommand;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.at_command_fragment, container, false);
        if (mView != null) {
            mAtCommandExecute = (Button) mView.findViewById(R.id.execute);
        }
        mAtCommandExecute.setOnClickListener(new btnClick());
        mAtResponse = (TextView) mView.findViewById(R.id.response);
        mAtCommand = (EditText) mView.findViewById(R.id.at_command);

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

    private void executeAT() {
/*        if (mBound && !mView.findViewById(R.id.at_command).toString().isEmpty()) {
            //Try SamSung MultiRil Implementation
            DetectResult rilStatus = mAimsicdService.getRilExecutorStatus();
            if (rilStatus.available) {
                new RequestOemInfoTask().execute();
            }
        }*/
        Helpers.sendMsg(mContext, "Coming soon...");
    }

    private void requestResponse() {
        try {
            String[] atCommand = new String[]{mAtCommand.getText().toString()};
            final List<String> list = mAimsicdService.executeAtCommand(atCommand);
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (list != null) {
                        mAtResponse.setText(TextUtils.join("\n", list));
                    }
                }
            });
        }catch (Exception e) {
            Log.e("AIMSICD", "requestResponse " + e);
        }
    }


    private class RequestOemInfoTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... string) {
            if (!mBound) return null;
            requestResponse();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}

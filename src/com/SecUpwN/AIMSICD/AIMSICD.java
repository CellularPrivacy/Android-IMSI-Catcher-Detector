package com.SecUpwN.AIMSICD;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;
import android.os.Build;

public class AIMSICD extends Activity {

    private final String TAG = "AIMSICD";

    private Device mDevice;

    private TextView outputView;

    private WebView webview;

    private boolean isAbout;
    private final Context mContext = this;
    private Menu mMenu;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mDevice = new Device(mContext);

        outputView = (TextView) findViewById(R.id.view);
        outputView.setHorizontalFadingEdgeEnabled(false);

        outputView.setText("Information:\n\n");

        if (mDevice.getPhoneID() == TelephonyManager.PHONE_TYPE_GSM) {
            outputView.append("SIM country:    " + mDevice.getSimCountry(false) + "\n");
            outputView.append("SIM Op ID:      " + mDevice.getSimOperator(false) + "\n");
            outputView.append("SIM Op Name:    " + mDevice.getSimOperatorName(false) + "\n");
            outputView.append("SIM IMSI:       " + mDevice.getSimSubs(false) + "\n");
            outputView.append("SIM serial:     " + mDevice.getSimSerial(false) + "\n\n");
        }

        int netID = mDevice.getNetID(true);
        outputView.append("Device type:    " + mDevice.getPhoneType(false) + "\n");
        outputView.append("Device IMEI:    " + mDevice.getIMEI(false) + "\n");
        outputView.append("Device version: " + mDevice.getIMEIv(false) + "\n");
        outputView.append("Device num:     " + mDevice.getPhoneNumber(false) + "\n\n");
        outputView.append("Network name:   " + mDevice.getNetworkName(false) + "\n");
        outputView.append("Network code:   " + mDevice.getSmmcMcc(false) + "\n");
        outputView.append("Network type:   " + mDevice.getNetworkTypeName() + "\n");
        outputView.append("Network LAC:    " + mDevice.getsLAC(false) + "\n");
        outputView.append("Network CellID: " + mDevice.getsCellId(false) + "\n\n");

        outputView.append("Data activity:  " + mDevice.getActivityDesc(netID) + "\n");
        outputView.append("Data status:    " + mDevice.getStateDesc(netID) + "\n");

        outputView.append("--------------------------------\n");
        outputView.append("[LAC,CID]|DAct|DStat|Net|Sig|Lat|Lng\n");
        Log.i(TAG, "**** AIMSICD ****");
        Log.i(TAG, "Device type   : " + mDevice.getPhoneType(false));
        Log.i(TAG, "Device imei   : " + mDevice.getIMEI(false));
        Log.i(TAG, "Device version: " + mDevice.getIMEIv(false));
        Log.i(TAG, "Device num    : " + mDevice.getPhoneNumber(false));
        Log.i(TAG, "Network type  : " + mDevice.getNetworkTypeName());
        Log.i(TAG, "Network CellID: " + mDevice.getsCellId(false));
        Log.i(TAG, "Network LAC   : " + mDevice.getsLAC(false));
        Log.i(TAG, "Network code  : " + mDevice.getSmmcMcc(false));
        Log.i(TAG, "Network name  : " + mDevice.getNetworkName(false));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mTrackCell = menu.findItem(R.id.track_cell);
        MenuItem mTrackSignal = menu.findItem(R.id.track_signal);
        MenuItem mTrackLocation = menu.findItem(R.id.track_location);

        if (mDevice.isTrackingCell()) {
            mTrackCell.setTitle(getResources().getString(R.string.track_cell));
            mTrackCell.setIcon(getResources().getDrawable(R.drawable.track_cell));
        } else {
            mTrackCell.setTitle(getResources().getString(R.string.untrack_cell));
            mTrackCell.setIcon(getResources().getDrawable(R.drawable.untrack_cell));
        }

        if (mDevice.isTrackingSignal()) {
            mTrackSignal.setTitle(getResources().getString(R.string.track_signal));
            mTrackSignal.setIcon(getResources().getDrawable(R.drawable.ic_action_network_cell));
        } else {
            mTrackSignal.setTitle(getResources().getString(R.string.untrack_signal));
            mTrackSignal.setIcon(getResources().getDrawable(R.drawable.ic_action_network_cell_not_tracked));
        }
        if (mDevice.isTrackingLocation()) {
            mTrackLocation.setTitle(getResources().getString(R.string.track_location));
            mTrackLocation.setIcon(getResources().getDrawable(R.drawable.ic_action_location_found));
        } else {
            mTrackLocation.setTitle(getResources().getString(R.string.untrack_location));
            mTrackLocation.setIcon(getResources().getDrawable(R.drawable.ic_action_location_off));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.track_cell:
                mDevice.trackcell();
                if (Build.VERSION.SDK_INT > 11)
                    onPrepareOptionsMenu(mMenu);
                return true;
            case R.id.track_signal:
                mDevice.tracksignal();
                if (Build.VERSION.SDK_INT > 11)
                    onPrepareOptionsMenu(mMenu);
                return true;
            case R.id.track_location:
                mDevice.tracklocation(mContext);
                if (Build.VERSION.SDK_INT > 11)
                    onPrepareOptionsMenu(mMenu);
                return true;
            case R.id.show_map:
                showmap();
                return true;
            case R.id.export_database:
                mDevice.exportDB();
                return true;
            case R.id.at_injector:
                Intent intent = new Intent(this, ATRilHook.class);
                startActivity(intent);
                return true;
            case R.id.app_exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return false;
    }

    protected final void about() {
        if (isAbout) {
            Log.i(TAG, "Call bring outputview (LOG) to front");
            webview.bringChildToFront(outputView);
            outputView.bringToFront();
            isAbout = false;

        } else {
            if (webview != null) {
                webview.bringToFront();
            } else {
                webview = new WebView(this);
                webview.loadUrl("http://secupwn.github.io/Android-IMSI-Catcher-Detector/");
                setContentView(webview);
            }
            isAbout = true;
        }
    }

    protected final void showmap() {
        Intent myIntent = new Intent(this, MapViewer.class);
        startActivity(myIntent);
    }

    public Device getDevice() {
        return mDevice;
    }

}

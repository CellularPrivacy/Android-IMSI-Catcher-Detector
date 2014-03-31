package com.SecUpwN.AIMSICD;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

public class AIMSICD extends Activity {

    private final String TAG = "AIMSICD";

    private TextView outputView;

    private WebView webview;

    private boolean isAbout;
    private final Context mContext = this;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Initialise device details
        Device.InitDevice(mContext);

        // Check required utilities are available
        Utils.CheckUtils(mContext);

        outputView = (TextView) findViewById(R.id.view);
        outputView.setHorizontalFadingEdgeEnabled(false);

        outputView.setText("Information:\n\n");

        if (Device.getPhoneID() == TelephonyManager.PHONE_TYPE_GSM) {
            outputView.append("SIM country:    " + Device.getSimCountry(false) + "\n");
            outputView.append("SIM Op ID:      " + Device.getSimOperator(false) + "\n");
            outputView.append("SIM Op Name:    " + Device.getSimOperatorName(false) + "\n");
            outputView.append("SIM IMSI:       " + Device.getSimSubs(false) + "\n");
            outputView.append("SIM serial:     " + Device.getSimSerial(false) + "\n\n");
        }

        int netID = Device.getNetID(true);
        outputView.append("Device type:    " + Device.getPhoneType(false) + "\n");
        outputView.append("Device IMEI:    " + Device.getIMEI(false) + "\n");
        outputView.append("Device version: " + Device.getIMEIv(false) + "\n");
        outputView.append("Device num:     " + Device.getPhoneNumber(false) + "\n\n");
        outputView.append("Network name:   " + Device.getNetworkName(false) + "\n");
        outputView.append("Network code:   " + Device.getSmmcMcc(false) + "\n");
        outputView.append("Network type:   " + Device.getNetworkTypeName() + "\n");
        outputView.append("Network LAC:    " + Device.getsLAC(false) + "\n");
        outputView.append("Network CellID: " + Device.getsCellId(false) + "\n\n");

        outputView.append("Data activity:  " + Device.getActivityDesc(netID) + "\n");
        outputView.append("Data status:    " + Device.getStateDesc(netID) + "\n");

        outputView.append("--------------------------------\n");
        outputView.append("[LAC,CID]|DAct|DStat|Net|Sig|Lat|Lng\n");
        Log.i(TAG, "**** AIMSICD ****");
        Log.i(TAG, "Device type   : " + Device.getPhoneType(false));
        Log.i(TAG, "Device imei   : " + Device.getIMEI(false));
        Log.i(TAG, "Device version: " + Device.getIMEIv(false));
        Log.i(TAG, "Device num    : " + Device.getPhoneNumber(false));
        Log.i(TAG, "Network type  : " + Device.getNetworkTypeName());
        Log.i(TAG, "Network CellID: " + Device.getsCellId(false));
        Log.i(TAG, "Network LAC   : " + Device.getsLAC(false));
        Log.i(TAG, "Network code  : " + Device.getSmmcMcc(false));
        Log.i(TAG, "Network name  : " + Device.getNetworkName(false));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (Device.isTrackingCell()) {
            menu.add(1, 0, 0, "Untrack Cell");
        } else {
            menu.add(1, 0, 0, "Track Cell");
        }

        if (Device.isTrackingSignal()) {
            menu.add(1, 1, 0, "Untrack Signal");
        } else {
            menu.add(1, 1, 0, "Track Signal");
        }
        if (Device.isTrackingLocation()) {
            menu.add(1, 2, 0, "Untrack Location");
        } else {
            menu.add(1, 2, 0, "Track Location");
        }
        menu.add(0, 4, 4, "Show Map");

        menu.add(0, 6, 6, "Quit");
        menu.add(0, 7, 7, "Export Database Tables");
        menu.add(0, 8, 8, "AT OEM RIL Hook Test");
        menu.setGroupCheckable(1, true, false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case 0:
                Device.trackcell();
                return true;
            case 1:
                Device.tracksignal();
                return true;
            case 2:
                Device.tracklocation(mContext);
                return true;
            case 4:
                showmap();
                return true;
            case 5:
                about();
                return true;
            case 6:
                finish();
                return true;
            case 7:
                Device.exportDB();
                return true;
            case 8:
                Intent intent = new Intent(this, ATRilHook.class);
                startActivity(intent);
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

}

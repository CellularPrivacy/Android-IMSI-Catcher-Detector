package com.jofrepalau.rawphone;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;
import android.view.Menu;
import com.jofrepalau.rawphone.cmdprocessor.CMDProcessor;
import com.jofrepalau.rawphone.cmdprocessor.Helpers;
import com.stericson.RootTools.RootTools;

public class rawphone extends Activity
{
    private TextView outputView;

    private WebView webview;

    private boolean isAbout;
    private final Context mContext = this;
	 
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Initialise device details
        Device.InitDevice(mContext);

        boolean isRootProvided = RootTools.isAccessGiven();
        boolean isBusyboxInstalled = Helpers.checkBusybox();

        // Check required utilities are available
        Utils.CheckUtils(mContext);

/*        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int icon = R.drawable.iconbn;
        CharSequence text = "Starting RawPhone...";
        CharSequence contentTitle = "RawPhone";
        CharSequence contentText = "Bring RawPhone to the foreground.";
        long when = System.currentTimeMillis();
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, this.getIntent(), 0);
        Notification notification = new Notification(icon,text,when);
        notification.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);
        notificationManager.notify(9999, notification);*/

        outputView = (TextView) findViewById(R.id.view);  
        outputView.setHorizontalFadingEdgeEnabled(false);

        outputView.setText("Information:\n\n");
                
        if (Device.getPhonetype() == TelephonyManager.PHONE_TYPE_GSM)
        {
            outputView.append("SIM country:    "+ Device.getSimCountry() +"\n") ;
            outputView.append("SIM Op ID:      "+ Device.getSimOperator() +"\n") ;
            outputView.append("SIM Op Name:    "+ Device.getSimOperatorName() +"\n") ;
            outputView.append("SIM IMSI:       "+ Device.getSimSubs() +"\n");
            outputView.append("SIM serial:     "+ Device.getSimSerial() +"\n\n");
        }

        outputView.append("Device type:    "+ Device.getSPhonetype() +"\n") ;
        outputView.append("Device IMEI:    "+ Device.getIMEI() +"\n") ;
        outputView.append("Device version: "+ Device.getIMEIv() +"\n") ;
        outputView.append("Device num:     "+ Device.getPhoneNumber() +"\n\n") ;
        outputView.append("Network name:   "+ Device.getsNetworkName() +"\n") ;
        outputView.append("Network code:   "+ Device.getSmmcMcc() +"\n") ;
        outputView.append("Network type:   "+ Device.getsNetworkType() +"\n") ;
        outputView.append("Network LAC:    "+ Device.getsLAC() +"\n") ;
        outputView.append("Network CellID: "+ Device.getsCellId() +"\n\n") ;

        outputView.append("Data activity:  "+ Device.getsDataActivity() +"\n") ;
        outputView.append("Data status:    "+ Device.getsDataState() +"\n") ;

        outputView.append("--------------------------------\n");
        outputView.append("[LAC,CID]|DAct|DStat|Net|Sig|Lat|Lng\n");
        Log.i("rawphone","Device type   : "+ Device.getSPhonetype());
        Log.i("rawphone","Device imei   : "+ Device.getIMEI());
        Log.i("rawphone","Device version: "+ Device.getIMEIv());
        Log.i("rawphone","Device num    : "+ Device.getPhoneNumber());
        Log.i("rawphone","Network type  : "+ Device.getsNetworkType());
        Log.i("rawphone","Network CellID: "+ Device.getsCellId());
        Log.i("rawphone","Network LAC   : "+ Device.getsLAC());
        Log.i("rawphone","Network code  : "+ Device.getSmmcMcc());
        Log.i("rawphone","Network name  : "+ Device.getsNetworkName());
    }

    @Override 
      public boolean onPrepareOptionsMenu(Menu menu) { 
        menu.clear();
        if(Device.isTrackingCell()){
        	menu.add(1, 0, 0, "Untrack Cell");
        }else{
        	menu.add(1, 0, 0, "Track Cell");
        }
        
        if(Device.isTrackingSignal()){
        	menu.add(1, 1, 0, "Untrack Signal");
        }else{
        	menu.add(1, 1, 0, "Track Signal");
        }
        if(Device.isTrackingLocation()){
        	menu.add(1, 2, 0, "Remove Loc.");
        }else{
        	menu.add(1, 2, 0, "Add Location");
        }   
        menu.add(0, 4, 4, "Show Map"); 

    	menu.add(0, 6, 6, "Quit"); 
        menu.add(0, 7, 7, "Dump Session KML");        
        menu.add(0, 8, 8, "Dump Session CSV");        
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
        	Device.dumpinfokml(mContext);
        	return true;
        case 8:
        	Device.dumpinfocsv(mContext);
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
    	//Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://m.mobiclip.net"));
    	//startActivity(browserIntent);
    	if(isAbout){
    		Log.i("Rawphone", "Call bring outputview (LOG) to front");
    		 webview.bringChildToFront(outputView);
    		outputView.bringToFront();
    		isAbout=false;
    		
    	}else{
    		if(webview != null){
    			webview.bringToFront();
    		}else{
    			webview = new WebView(this);
           	 	webview.loadUrl("http://rawphone.dyndns.biz/rawphone.php");
           	 	setContentView(webview);    			
    		}
       	 	isAbout=true;
    	} 
    }

    protected final void showmap() {
     	Intent myIntent = new Intent(this, MapViewer.class);
    	startActivity(myIntent);
    }

 }

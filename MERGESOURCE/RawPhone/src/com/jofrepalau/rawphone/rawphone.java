package com.jofrepalau.rawphone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Overlay;
import com.jofrepalau.rawphone.MapViewer.MapOverlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;
import android.view.Menu; 

public class rawphone extends Activity {
	private TextView outputView;
	private TelephonyManager tm;
	private PhoneStateListener signalListenerstrength;
	private PhoneStateListener signalListenerlocation;
	private int phonetype;
	private boolean isTrackingCell;
	private boolean isTrackingSignal;
	private boolean isTrackingLocation;
	private boolean isAbout;
    private LocationManager lm;
    private LocationListener locationListener;
    private String snettype;
    private String cellinfo;
    private int signalinfo;
    private int nettype;
    private NotificationManager notificationManager;
    private String kml;
    private String csv;
    private int lac;
    private int cellid;
    private double slng; 
    private double slat;
    private ArrayList<String> alPosition;
    private WebView webview;
    private final String DB_NAME = "myCellInfo";
	private final String TABLE_NAME = "locationinfo";
	public SQLiteDatabase myDB;
	 
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);  
        
        String imei, imeiv, phonenum, sCellId, sLAC, snetname, smmcmcc, sphonetype, simcountry, simoperator, simoperatorname, simserial, simsubs, sdataactivity;
        int dataactivity;
    	sCellId = "undef";
        sLAC = "undef";
        smmcmcc = "undef";
        snetname = "undef";
        snettype = "undef"; 
        sphonetype = "undef";
        isTrackingCell=false;
        isTrackingLocation=false;
        isTrackingSignal= false; 
        isAbout=false;
        cellinfo="[0,0]|nn|nn|";
        signalinfo=0;
        lac = 0;
        cellid = 0;
        slng = 0.0;
        slat = 0.0; 
        alPosition  = new ArrayList<String>();
        myDB=null;
        try {        	
        	myDB =  this.openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        	myDB.execSQL("CREATE TABLE IF NOT EXISTS " +
        			TABLE_NAME + " (_id INTEGER primary key autoincrement, Lac INTEGER, CellID INTEGER, Net VARCHAR, Lat VARCHAR, Lng VARCHAR, Signal INTEGER, Connection VARCHAR, Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);");        	
        	
        } catch (SQLiteException se ) {
        	Log.e(getClass().getSimpleName(), "Could not create or Open the database:" +se);
        	myDB=null;
        }
        
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int icon = R.drawable.iconbn;
        CharSequence text = "Starting RawPhone...";
        CharSequence contentTitle = "RawPhone";
        CharSequence contentText = "Bring RawPhone to the foreground.";
        long when = System.currentTimeMillis();
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, this.getIntent(), 0);
        Notification notification = new Notification(icon,text,when);
        notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
        notificationManager.notify(9999, notification);
        
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE) ;
        outputView = (TextView) findViewById(R.id.view);  
        outputView.setHorizontalFadingEdgeEnabled(false);

        outputView.setText("Information:\n\n");
        imei = tm.getDeviceId();
        imeiv = tm.getDeviceSoftwareVersion();
        phonenum = tm.getLine1Number();
        nettype = tm.getNetworkType();
         
        switch (nettype){
        case TelephonyManager.NETWORK_TYPE_UNKNOWN: snettype = "Unknown"; break;
        case TelephonyManager.NETWORK_TYPE_GPRS:    snettype = "GPRS"; break; 
        case TelephonyManager.NETWORK_TYPE_EDGE:    snettype = "EDGE"; break;
        case TelephonyManager.NETWORK_TYPE_UMTS:    snettype = "UMTS"; break;
        case TelephonyManager.NETWORK_TYPE_HSDPA:   snettype = "HSPA"; break;
        case TelephonyManager.NETWORK_TYPE_HSUPA:   snettype = "HDSPA"; break;
        case TelephonyManager.NETWORK_TYPE_HSPA:    snettype = "HUSPA"; break;
        case TelephonyManager.NETWORK_TYPE_CDMA:    snettype = "CDMA"; break;
        case TelephonyManager.NETWORK_TYPE_EVDO_0:  snettype = "EVDO_0"; break;
        case TelephonyManager.NETWORK_TYPE_EVDO_A:  snettype = "EVDO_A"; break;
        case TelephonyManager.NETWORK_TYPE_1xRTT:   snettype = "1xRTT"; break;
        default:                                    snettype = "Unknown"; break;
        }  
                
        phonetype = tm.getPhoneType();                
        switch (phonetype){
        case TelephonyManager.PHONE_TYPE_GSM: 
        	sphonetype = "GSM"; 
        	smmcmcc = tm.getNetworkOperator();
        	snetname = tm.getNetworkOperatorName();
        	GsmCellLocation gsmCellLocation =  (GsmCellLocation) tm.getCellLocation() ;
            if (gsmCellLocation != null) {
            	sCellId =  "" + gsmCellLocation.getCid() ;
                sLAC = "" + gsmCellLocation.getLac() ;
            }
            simcountry = tm.getSimCountryIso();
            simoperator = tm.getSimOperator();
         	simoperatorname = tm.getSimOperatorName();
            simserial =	tm.getSimSerialNumber();
            simsubs = tm.getSubscriberId();
            outputView.append("SIM country:    "+simcountry+"\n") ;
            outputView.append("SIM Op ID:      "+simoperator+"\n") ;
            outputView.append("SIM Op Name:    "+simoperatorname+"\n") ;
            outputView.append("SIM IMSI:       "+simsubs+"\n");
            outputView.append("SIM serial:     "+simserial+"\n\n");
            break;
        case TelephonyManager.PHONE_TYPE_CDMA:
        	sphonetype = "CDMA"; 
        	break; 
        }
        outputView.append("Device type:    "+sphonetype+"\n") ;
        outputView.append("Device IMEI:    "+imei+"\n") ;
        outputView.append("Device version: "+imeiv+"\n") ;
        outputView.append("Device num:     "+phonenum+"\n\n") ;       
        outputView.append("Network name:   "+snetname +"\n") ;
        outputView.append("Network code:   "+smmcmcc +"\n") ;
        outputView.append("Network type:   "+snettype +"\n") ;
        outputView.append("Network LAC:    "+sLAC +"\n") ;
        outputView.append("Network CellID: "+sCellId +"\n\n") ;

        dataactivity = tm.getDataActivity();
        sdataactivity="undef";
        switch (dataactivity){
        case TelephonyManager.DATA_ACTIVITY_NONE:    sdataactivity = "None"; break;
        case TelephonyManager.DATA_ACTIVITY_IN:      sdataactivity = "In"; break;
        case TelephonyManager.DATA_ACTIVITY_OUT:     sdataactivity = "Out"; break;
        case TelephonyManager.DATA_ACTIVITY_INOUT:   sdataactivity = "In-Out"; break;
        case TelephonyManager.DATA_ACTIVITY_DORMANT: sdataactivity = "Dormant"; break;
        }
        outputView.append("Data activity:  "+sdataactivity +"\n") ;
        
        dataactivity = tm.getDataState();
        sdataactivity="undef";
        switch (dataactivity){
        case TelephonyManager.DATA_DISCONNECTED:     sdataactivity = "Disconnected"; break;
        case TelephonyManager.DATA_CONNECTING:       sdataactivity = "Connecting"; break;
        case TelephonyManager.DATA_CONNECTED:        sdataactivity = "Connected"; break;
        case TelephonyManager.DATA_SUSPENDED:        sdataactivity = "Suspended"; break;
        }
        outputView.append("Data status:    "+sdataactivity +"\n") ;
        
        signalListenerlocation=new PhoneStateListener() {
            public void onCellLocationChanged(CellLocation location) {
                nettype = tm.getNetworkType();
                switch (nettype){
                case TelephonyManager.NETWORK_TYPE_UNKNOWN: snettype = "Unknown"; break;
                case TelephonyManager.NETWORK_TYPE_GPRS:    snettype = "GPRS"; break; 
                case TelephonyManager.NETWORK_TYPE_EDGE:    snettype = "EDGE"; break;
                case TelephonyManager.NETWORK_TYPE_UMTS:    snettype = "UMTS"; break;
                case TelephonyManager.NETWORK_TYPE_HSDPA:   snettype = "HDSPA"; break;
                case TelephonyManager.NETWORK_TYPE_HSUPA:   snettype = "HSUPA"; break;
                case TelephonyManager.NETWORK_TYPE_HSPA:    snettype = "HSPA"; break;
                case TelephonyManager.NETWORK_TYPE_CDMA:    snettype = "CDMA"; break;
                case TelephonyManager.NETWORK_TYPE_EVDO_0:  snettype = "EVDO_0"; break;
                case TelephonyManager.NETWORK_TYPE_EVDO_A:  snettype = "EVDO_A"; break;
                case TelephonyManager.NETWORK_TYPE_1xRTT:   snettype = "1xRTT"; break;
                default:                                    snettype = "Unknown"; break;
                }
                int dataactivity = tm.getDataActivity();
                String sdataactivity="un";
                switch (dataactivity){
                case TelephonyManager.DATA_ACTIVITY_NONE:    sdataactivity = "No"; break;
                case TelephonyManager.DATA_ACTIVITY_IN:      sdataactivity = "In"; break;
                case TelephonyManager.DATA_ACTIVITY_OUT:     sdataactivity = "Ou"; break;
                case TelephonyManager.DATA_ACTIVITY_INOUT:   sdataactivity = "IO"; break;
                case TelephonyManager.DATA_ACTIVITY_DORMANT: sdataactivity = "Do"; break;
                }
                
                int datastate = tm.getDataState();
                String sdatastate="un";
                switch (datastate){
                case TelephonyManager.DATA_DISCONNECTED:     sdatastate = "Di"; break;
                case TelephonyManager.DATA_CONNECTING:       sdatastate = "Ct"; break;
                case TelephonyManager.DATA_CONNECTED:        sdatastate = "Cd"; break;
                case TelephonyManager.DATA_SUSPENDED:        sdatastate = "Su"; break;
                }
         
                switch (phonetype){
                case TelephonyManager.PHONE_TYPE_GSM: 
                	GsmCellLocation gsmCellLocation =  (GsmCellLocation) location ;
                	if (gsmCellLocation != null) 
               		{
               			cellinfo=gsmCellLocation.toString()+sdataactivity+"|"+sdatastate+"|"+snettype+"|";
                		lac = gsmCellLocation.getLac();
                		cellid = gsmCellLocation.getCid();
                   		outputView.append(cellinfo+signalinfo+"["+slng+"|"+slat +"\n");
                   	    kmlpoints(lac, cellid, cellinfo, slng, slat);
               		};
               		break;
                case TelephonyManager.PHONE_TYPE_CDMA:
                	CdmaCellLocation cdmaCellLocation =  (CdmaCellLocation) location ;
                	if (cdmaCellLocation != null) 
               		{
               			cellinfo=cdmaCellLocation.toString().toString()+sdataactivity+"|"+sdatastate+"|"+snettype+"|";
                		lac = cdmaCellLocation.getNetworkId();
                		cellid = cdmaCellLocation.getBaseStationId();
                		outputView.append(cellinfo+signalinfo+"["+slng+"|"+slat +"\n");
                		kmlpoints(lac, cellid, cellinfo, slng, slat);
               		};                	
                }
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
                java.util.Date date = new java.util.Date();
                String datetime = dateFormat.format(date);
                insertrow(lac,cellid,nettype,slat,slng, signalinfo,cellinfo);
                
                csv += lac+","+cellid+","+snettype+","+slat+","+slng+","+signalinfo+","+datetime+"\n";
                	
//                List<NeighboringCellInfo> neighboringCells = tm.getNeighboringCellInfo(); 
//                if((neighboringCells.size() > 0)&&(neighboringCells.get(0).getCid()>0))
//                {
//                	outputView.append("Neighboring Cells:\n");
//                	for(int i = 0; i < neighboringCells.size(); i++)
//                	{
//                		outputView.append("Cell" +i+": CellID: "+neighboringCells.get(i).getCid()+" LAC: "+neighboringCells.get(i).getLac()+"\n");
//                	};
//                };
            };
        };
        signalListenerstrength=new PhoneStateListener() {
             public void onSignalStrengthsChanged(SignalStrength signalStrength) {
             	switch (phonetype){
             	case TelephonyManager.PHONE_TYPE_GSM:
             		signalinfo= signalStrength.getGsmSignalStrength();
             		break;
             	case TelephonyManager.PHONE_TYPE_CDMA:
             		signalinfo=signalStrength.getCdmaDbm();
             		break; 
             	default:
             		signalinfo=0;
             	}         
             	outputView.append(cellinfo+signalinfo+"["+slng+"|"+slat +"\n");
             	kmlpoints(lac, cellid, cellinfo, slng, slat);
             	insertrow(lac,cellid,nettype,slat,slng, signalinfo,cellinfo);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
                java.util.Date date = new java.util.Date();
                String datetime = dateFormat.format(date);
             	csv += lac+","+cellid+","+snettype+","+slat+","+slng+","+signalinfo+","+datetime+"\n";
             }
         };            
     	
        kmlheader();
        csv="LAC,CellID,NetType,LAT,LNG,Strength\n";
        outputView.append("--------------------------------\n");
        outputView.append("[LAC,CID]|DAct|DStat|Net|Sig|Lat|Lng\n");
        Log.i("rawphone","Device type   : "+ sphonetype);
        Log.i("rawphone","Device imei   : "+imei);
        Log.i("rawphone","Device version: "+imeiv);
        Log.i("rawphone","Device num    : "+phonenum);
        Log.i("rawphone","Network type  : "+snettype);
        Log.i("rawphone","Network CellID: "+sCellId);  
        Log.i("rawphone","Network LAC   : "+sLAC);  
        Log.i("rawphone","Network code  : "+smmcmcc);
        Log.i("rawphone","Network name  : "+snetname);     
    }
    @Override 
      public boolean onPrepareOptionsMenu(Menu menu) { 
        menu.clear();
        if(isTrackingCell){
        	menu.add(1, 0, 0, "Untrack Cell");
        }else{
        	menu.add(1, 0, 0, "Track Cell");
        }
        
        if(isTrackingSignal){
        	menu.add(1, 1, 0, "Untrack Signal");
        }else{
        	menu.add(1, 1, 0, "Track Signal");
        }
        if(isTrackingLocation){
        	menu.add(1, 2, 0, "Remove Loc.");
        }else{
        	menu.add(1, 2, 0, "Add Location");
        }   
        menu.add(0, 4, 4, "Show Map"); 
    	
        if(isAbout){
        	//menu.add(0, 5, 5, "Log");
        }else{
        	//menu.add(0, 5, 5, "About");
        }
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
        	trackcell();
            return true;
        case 1:
        	tracksignal();
            return true;
        case 2:
        	tracklocation();
        	return true;
        case 4:
        	showmap();
        	return true;
        case 5:
        	about(); 
        	return true;
        case 6:
        	this.quit(); 
        	return true;
        case 7:
        	dumpinfokml();
        	return true;
        case 8:
        	dumpinfocsv();
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
    private final void tracksignal() {
    	if(isTrackingSignal)
    	{
    		tm.listen(signalListenerstrength, PhoneStateListener.LISTEN_NONE);	
    		isTrackingSignal=false;
    		signalinfo=0;
    	}else{
    		tm.listen(signalListenerstrength, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);	
    		isTrackingSignal=true;
    	}
    }
    private final void trackcell() {
    	if(isTrackingCell)
    	{
    		tm.listen(signalListenerlocation, PhoneStateListener.LISTEN_NONE);	
    		isTrackingCell=false;
            cellinfo="[0,0]|nn|nn|";
    	}else{
    		tm.listen(signalListenerlocation, PhoneStateListener.LISTEN_CELL_LOCATION);	
    		isTrackingCell=true;
    	}
    }
    private final void about() {
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
    
    private final void tracklocation() {
    	if(isTrackingLocation){
    		lm.removeUpdates(locationListener);
    		isTrackingLocation=false;  
            slng = 0.0;
            slat = 0.0; 
    	}else{
    		if(lm != null){
    			Log.i("rawphone", "LocationManager already existed");
    	        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
    	        isTrackingLocation=true;
    	    }else{
    	        Log.i("rawphone", "LocationManager did not existed");
    	  		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);    
    	  		if(lm != null){
    	  			if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){  	
    	           		Log.i("rawphone", "LocationManager created");
    	           		locationListener = new MyLocationListener();
    	           		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
    	           		isTrackingLocation=true;
    	  			}else{
    	  				// GPS No es permet
    	  				Log.i("rawphone", "GPS not allowed");
    	     	    	AlertDialog.Builder msg = new AlertDialog.Builder(this);
    	     	    	msg.setMessage("GPS is not enabled!. You won«t be able to use GPS data until you enable it");
    	     	    	AlertDialog alert = msg.create();
    	     	        alert.setTitle("Error:");
    	     	    	alert.show();
    	     	    	lm=null;
    	  			}
    	        } 
    		 }
    	}
    }

    private class MyLocationListener implements LocationListener 
    {
        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                slng = loc.getLongitude();
                slat = loc.getLatitude(); 
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, 
            Bundle extras) {
            // TODO Auto-generated method stub
        }
    }        
       
    private final void dumpinfokml() {
	    try {
	    	String state = Environment.getExternalStorageState();
	    	if (Environment.MEDIA_MOUNTED.equals(state)) {
	    	      // We can read and write the media
	    	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	    	      // We can only read the media
		    	  AlertDialog.Builder msg = new AlertDialog.Builder(this);
		    	  msg.setMessage("Sorry, your SD card is mounted as ready only. We can«t copy the log if we can« write there");
		    	  AlertDialog alert = msg.create();
		          alert.setTitle("Error:");
		    	  alert.show();	    	      
	    	      return;
	    	} else {
	    	      // Something else is wrong. It may be one of many other states, but all we need
	    	      //  to know is we can neither read nor write
		    	  AlertDialog.Builder msg = new AlertDialog.Builder(this);
		    	  msg.setMessage("Sorry, I could not find an SD card where to copy the log");
		    	  AlertDialog alert = msg.create();
		          alert.setTitle("Error:");
		    	  alert.show();	    	      
	    	      return;	    	
	     	}
    	    File rootcasirectory = new File(Environment.getExternalStorageDirectory() + "/rawphone/");
    	    // have the object build the directory structure, if needed.
    	    rootcasirectory.mkdirs();
    	    // create a File object for the output file
    	    
    	    // Make a copy of current content
    	    String buff = kml;
    	    buff = kmlLAC(buff);
    	    buff += "</Folder>\n</kml>\n";
    	    
    	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
            java.util.Date date = new java.util.Date();
            String datetime = dateFormat.format(date);
    	    
    	    File file = new File(rootcasirectory, "rawphone-"+datetime+".kml");
    	    try {
    	        OutputStream os = new FileOutputStream(file);
 
    	        os.write(buff.getBytes());
    	    } catch (IOException e) {
    	        // Unable to create file, likely because external storage is
    	        // not currently mounted.
    	    	System.out.println("ExternalStorage: Error writing " + file + e.getMessage());
    	    }
    	    
	    	AlertDialog.Builder msg = new AlertDialog.Builder(this);
	    	msg.setMessage("KML log copied in " + file);
	    	AlertDialog alert = msg.create();
	        alert.setTitle("Log:");
	    	alert.show();
	      } catch (Exception e) {
	    	  AlertDialog.Builder msg = new AlertDialog.Builder(this);
	    	  msg.setMessage("Something unexpected happened: " + e.getMessage());
	    	  AlertDialog alert = msg.create();
	    	  alert.setTitle("Error!");
	    	  alert.setIcon(R.drawable.icon);
	    	  alert.show();
	          e.printStackTrace();
	      }
    }
    private final void kmlheader() {  	
    	kml = "<kml xmlns=\"http://earth.google.com/kml/2.0\">\n";
    	kml += "<Folder>\n";
    	kml += "<name>RawPhone</name>\n";
    	kml += "<description><![CDATA[RawPhone LAC and CellID logs]]></description>\n";
    }
    private final void kmlpoints(int lac, int cellid, String info, double lng, double lat) {
    	if((slng != 0.0)||(slat != 0.0)){
    		//String timestamp = new java.text.SimpleDateFormat("yyyy-MM-ddTHH:mm:ss").format(new java.util.Date (epoch*1000));
    		alPosition.add(slng+","+slat);
    		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        java.util.Date date = new java.util.Date();
	        String datetime = dateFormat.format(date);	
	    	kml += "<Placemark>\n";
	    	kml += "<TimeStamp><when>"+datetime+"</when></TimeStamp>\n";
	    	kml += "<name>"+lac+" "+cellid+"</name>\n";
	    	kml += "<description><![CDATA["+info+"]]></description>\n";
	    	kml += "<Style><IconStyle><color>ffffbebe</color><scale>0.5</scale></IconStyle></Style><Point><coordinates>"+lng+","+lat+",0</coordinates></Point>\n";
	    	kml += "</Placemark>\n";
    	}
    }
    private final String kmlLAC(String content) {
//    	Array aLAC (LAC)
//      0      1       2     3    4    5    6    7
//      Array aInfo (LAC, CellID, Signal, Net, Con, Sta, Lng, Lat)
//      String[] aPosition;
//    	int i =0;
//    	for (int lac : aLAC) {
//    		for ( array tupla : aInfo)
//    		{
//    			i=0;
//    			if(lac == tupla[0])
//    			{
//    				lng = tupla[6];
//    				lat = tupla[7];
//    				aPosition[i] = lng + "," + lat + ",0";
//    				i++;
//    			}
//    		}
//    		sort(aPosition);
//    		poligon(lac, aPosition);
//    	}
    	return poligon(lac, alPosition, content);
    }
    String poligon (int lac, ArrayList<String> alPosition, String content)
    {
    	//Collections.sort(alPosition);
    	if(alPosition.size()>0)
    	{
	    	String[] aPosition = new String[alPosition.size()];
	    	alPosition.toArray(aPosition);
	    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        java.util.Date date = new java.util.Date();
	        String datetime = dateFormat.format(date);
	
	        content += "<Placemark>\n";
	        content += "  <TimeStamp><when>"+datetime+"</when></TimeStamp>\n";
	        content += "  <name>LAC "+lac+"</name>\n";
	        content += "  <description><![CDATA[LAC "+lac+"]]></description>\n";
	        content += "  <Style><PolyStyle><color>7f9e9eff</color></PolyStyle></Style><MultiGeometry><Polygon><outerBoundaryIs><LinearRing><coordinates>";
	        String initpos = aPosition[0];
	        for (String pos : aPosition)
	        {
	        	content += " "+pos;
	        }
	        content += " " +initpos+"\n</coordinates></LinearRing></outerBoundaryIs></Polygon></MultiGeometry>\n";
	        content += "</Placemark>\n";
    	}
    	return content;
    }

    
    private final void dumpinfocsv() {
	    try {
	    	String state = Environment.getExternalStorageState();
	    	if (Environment.MEDIA_MOUNTED.equals(state)) {
	    	      // We can read and write the media
	    	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	    	      // We can only read the media
		    	  AlertDialog.Builder msg = new AlertDialog.Builder(this);
		    	  msg.setMessage("Sorry, your SD card is mounted as ready only. We can«t copy the log if we can« write there");
		    	  AlertDialog alert = msg.create();
		          alert.setTitle("Error:");
		    	  alert.show();	    	      
	    	      return;
	    	} else {
	    	      // Something else is wrong. It may be one of many other states, but all we need
	    	      //  to know is we can neither read nor write
		    	  AlertDialog.Builder msg = new AlertDialog.Builder(this);
		    	  msg.setMessage("Sorry, I could not find an SD card where to copy the log");
		    	  AlertDialog alert = msg.create();
		          alert.setTitle("Error:");
		    	  alert.show();	    	      
	    	      return;	    	
	     	}
    	    File rootcasirectory = new File(Environment.getExternalStorageDirectory() + "/rawphone/");
    	    // have the object build the directory structure, if needed.
    	    rootcasirectory.mkdirs();
    	    // create a File object for the output file
    	    
    	    // Make a copy of current content
    	    String buff = csv;
    	    
    	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
            java.util.Date date = new java.util.Date();
            String datetime = dateFormat.format(date);
    	    
    	    File file = new File(rootcasirectory, "rawphone-"+datetime+".csv");
    	    try {
    	        OutputStream os = new FileOutputStream(file);
 
    	        os.write(buff.getBytes());
    	    } catch (IOException e) {
    	        // Unable to create file, likely because external storage is
    	        // not currently mounted.
    	    	System.out.println("ExternalStorage: Error writing " + file + e.getMessage());
    	    }
    	    
	    	AlertDialog.Builder msg = new AlertDialog.Builder(this);
	    	msg.setMessage("CSV log copied in " + file);
	    	AlertDialog alert = msg.create();
	        alert.setTitle("Log:");
	    	alert.show();
	      } catch (Exception e) {
	    	  AlertDialog.Builder msg = new AlertDialog.Builder(this);
	    	  msg.setMessage("Something unexpected happened: " + e.getMessage());
	    	  AlertDialog alert = msg.create();
	    	  alert.setTitle("Error!");
	    	  alert.setIcon(R.drawable.icon);
	    	  alert.show();
	          e.printStackTrace();
	      }
    }    
    
    
    
    private final void showmap() {
     	Intent myIntent = new Intent(this, MapViewer.class);
    	startActivity(myIntent);
    }
    
    private final void insertrow(int lac, int cellid, int nettype, double dlat, double dlng, int signalinfo, String cellinfo) {
    // _id INTEGER primary key autoincrement, Lac INTEGER, CellID INTEGER, Net VARCHAR, Lat VARCHAR, Lng VARCHAR, Signal INTEGER, Connection VARCHAR, Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);"
    	Log.i("rawphone", " ==> Exec: insertrow ("+lac+","+cellid+","+nettype+","+dlat+","+dlng+","+signalinfo+","+cellinfo+")");
	    try{
    		if((dlng != 0.0)||(dlat != 0.0)){
		    	if(myDB != null){
		    	    Cursor mCursor = myDB.query(true, TABLE_NAME, new String[] {"_id", "Signal" }, "CellID =" + cellid, null, null, null, "Signal", null);
		    	    if (mCursor.moveToFirst()) {   
		        		do{
		        			int iSignal = mCursor.getInt(1);
		        			if (iSignal <= signalinfo){
		        				Log.i("rawphone", " ==> Removing "+mCursor.getInt(0)+" adding cell "+cellid+" with signal "+signalinfo);
		        				myDB.delete(TABLE_NAME, "_id ="+ mCursor.getInt(0), null);
		        				//mCursor.deactivate();
		        				myDB.execSQL("INSERT INTO " +TABLE_NAME + 
		        		        		" (Lac , CellID, Net, Lat, Lng, Signal, Connection)" +
		        		        		" VALUES("+lac+","+cellid+","+nettype+","+dlat+","+dlng+","+signalinfo+",\""+cellinfo+"\");");
		        				//mCursor.deactivate();
		        			}else{
		        				Log.i("rawphone", " ==> Keep entry "+mCursor.getInt(0)+" with signal "+mCursor.getInt(1));
		        			}
		        			
		        		} while (mCursor.moveToNext());
		        	}else{
		        		Log.i("rawphone", " ==> Adding VALUES("+lac+","+cellid+","+nettype+","+dlat+","+dlng+","+signalinfo+",\""+cellinfo+"\");");
	    				myDB.execSQL("INSERT INTO " +TABLE_NAME + 
	    		        		" (Lac , CellID, Net, Lat, Lng, Signal, Connection)" +
	    		        		" VALUES("+lac+","+cellid+","+nettype+","+dlat+","+dlng+","+signalinfo+",\""+cellinfo+"\");");
		        		
		        	}
		        		
		    	    mCursor.close();
		    	}else{
		    		Log.e("rawphone", " ==>  Database not initialized!");
		    	}
	    	}
	    } catch (Exception e) {
	        // Something wierd happended
	    	System.out.println("Strange Error caught: " + e.getMessage());
	    }
    }    
    private final void quit() {
    	notificationManager.cancelAll();
    	if(lm != null){
    		lm.removeUpdates(locationListener);
    	}
    	if(myDB != null){
    		myDB.close();
    	}
        this.finish();
    }
 }
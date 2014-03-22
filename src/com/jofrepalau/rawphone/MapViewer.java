package com.jofrepalau.rawphone;

import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ImageView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;


public class MapViewer extends MapActivity {
	public MapView mapv;
	private MapController mapc;
	private SQLiteDatabase myDB;
    private final String DB_NAME = "myCellInfo";
	private final String TABLE_NAME = "locationinfo";
	private final int SIGNAL_SIZE_RATIO = 15;
	
	class MapOverlay extends com.google.android.maps.Overlay
    {
		private GeoPoint gp1;
		private int color;
		private int radius;
		
		public MapOverlay(GeoPoint gp1, int radius, int color) {
			this.gp1 = gp1;
			this.color = color;
			this.radius = radius;
		}
		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) 
        {                           
			int pradius=(int) mapv.getProjection().metersToEquatorPixels(radius);
	        
			Log.i("MapViewer", " ==> Draw pos: " +gp1.toString() + " color: " + color + " radius: " + radius + " pradius: " + pradius );
            Projection projection = mapView.getProjection();
    		Paint paint = new Paint();
    		Point point = new Point();
    		projection.toPixels(gp1, point);
    		paint.setColor(color);
    		paint.setStrokeWidth(0);
    		paint.setAlpha(20);
    		canvas.drawCircle(point.x, point.y, pradius, paint);    
        }
    } 

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	Log.i("rawphone", "Starting MapViewer ============");   	
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        
        ImageView imagev = new ImageView(this);
        mapv = (MapView) findViewById(R.id.mapView);  
        mapv.setBuiltInZoomControls(true);
        mapv.displayZoomControls(true);
        
        myDB=null;
        try {        	
        	myDB =  this.openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        	loadentries();
        } catch (SQLiteException se ) {
        	Log.e(getClass().getSimpleName(), "Could not Open the database:" +se);
        	myDB=null;
        }
    }
 
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    @Override 
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.clear();
    	menu.add(0, 1, 1, "Erase DB");
    	menu.add(0, 3, 3, "About");
    	menu.add(0, 4, 4, "Go to Log"); 
    	menu.setGroupCheckable(1, true, false);
  	return super.onCreateOptionsMenu(menu); 
    }
    
    @Override 
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case 1:
        	erasedb();
            return true;
        case 3:
        	about();
        	return true;
        case 4:
        	quit(); 
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    private final void about() {
    	//Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://m.mobiclip.net"));
    	//startActivity(browserIntent);
    	WebView webview = new WebView(this);
    	webview.loadUrl("http://rawphone.dyndns.biz/rawphone.php");
        webview.canGoBack();
        setContentView(webview);    			
    }
    private void loadentries(){
        double dlat = 0.0;
        double dlng = 0.0;
        String datainfo = "undef";
        int  net    = 0;
        int signal  = 0;
        int lac     = 0;
        int cellid  = 0;
        int radius  = 0;
        GeoPoint p  = null;
        mapc= mapv.getController();
        int color  = 0x000000;
        // 	_id INTEGER primary key autoincrement, Lac INTEGER, CellID INTEGER, Net VARCHAR, Lat VARCHAR, Lng VARCHAR, Signal INTEGER, Connection VARCHAR, Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);"
        Cursor c = myDB.query(true, TABLE_NAME, new String[] {"Net", "Lat", "Lng", "Signal","Connection" }, null, null, null, null, "Timestamp", null);  
     	
     	if (c.moveToFirst()) {
	        List<Overlay> listOfOverlays = mapv.getOverlays();	   
    		do{
    			net = c.getInt(0);
    			dlat = Double.parseDouble(c.getString(1));
    			dlng = Double.parseDouble(c.getString(2));
    			signal = c.getInt(3);
    			datainfo = c.getString(4);
    			if(signal == 0){ signal = 20;}
    			
    			if((dlat != 0.0)||(dlng != 0.0)){
    				p = new GeoPoint((int) (dlat * 1E6), (int) (dlng * 1E6));
    				radius=signal * SIGNAL_SIZE_RATIO;
    		        switch (net) {
    		        case TelephonyManager.NETWORK_TYPE_UNKNOWN: color = 0xF0F8FF; break;
    		        case TelephonyManager.NETWORK_TYPE_GPRS:    color = 0xA9A9A9; break; 
    		        case TelephonyManager.NETWORK_TYPE_EDGE:    color = 0x87CEFA; break;
    		        case TelephonyManager.NETWORK_TYPE_UMTS:    color = 0x7CFC00; break;
    		        case TelephonyManager.NETWORK_TYPE_HSDPA:   color = 0xFF6347; break;
    		        case TelephonyManager.NETWORK_TYPE_HSUPA:   color = 0xFF00FF; break;
    		        case TelephonyManager.NETWORK_TYPE_HSPA:    color = 0x238E6B; break;
    		        case TelephonyManager.NETWORK_TYPE_CDMA:    color = 0x8A2BE2; break;
    		        case TelephonyManager.NETWORK_TYPE_EVDO_0:  color = 0xFF69B4; break;
    		        case TelephonyManager.NETWORK_TYPE_EVDO_A:  color = 0xFFFF00; break;
    		        case TelephonyManager.NETWORK_TYPE_1xRTT:   color = 0x7CFC00; break;
    		        default:                                    color =  0xF0F8FF; break;
    		        }
    		        Log.i("MapViewer", " ==> Poin:" + p.toString() + " radius: " + radius + " color: " + color + " signal:" + signal);
    				listOfOverlays.add(new MapOverlay(p, radius, color));
    			}
    			// mapv.invalidate();
    			
    		} while (c.moveToNext());
    		c.close();
    		mapc.setCenter(p);
    	}
    	mapc.setZoom(14);
        mapv.setSatellite(false);
    	mapv.invalidate();
    }
    
    private final void quit() {
    	this.finish();
    }
    private final void erasedb() {
    	myDB.delete(TABLE_NAME, null, null);
    	
    }
}

 

package com.SecUpwN.AIMSICD.activities;
/**
 * Created by Paul Kinsella paulkinsella29@yahoo.ie on 25/03/15.
 * Copyright Paul Kinsella
 *
 *      This is a listener class for catching cell change rather than using a timer
 *      which will not detect IMSIC's if the timer is longer than 5 seconds,
 *      every time the cell changes this will also check for new neighboring cells.
 *      use this as a base class to build on for cell change etc.
 *
 *
 *          to start this from another activity use:
 *
 *          NeighboringCellMonitor  NC_MONITOR = new NeighboringCellMonitor(context);
 *          NC_MONITOR.startMonitoring();
 *
 *          This Solves this issue #346 but whoever is coded the Cell Tracker needs
 *          to impliment his code in this class to update DB and Neighboring cells
 *          activity.
 *
 *          TODO: listener is idle useless the cell tracker coder updates this class
 *
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import java.util.List;

public class NeighboringCellMonitor extends PhoneStateListener {

    private static final String TAG = "NeighboringCellMonitor";
    private Context mContext = null;
    private int listen_events =
            PhoneStateListener.LISTEN_CELL_LOCATION |
                    PhoneStateListener.LISTEN_CELL_INFO;
    private TelephonyManager teleManager = null;
    private PhoneStateListener phonestatelistener = null;

    public NeighboringCellMonitor(Context context) {
        this.mContext = context;
        teleManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        teleManager.listen(this, listen_events);
    }

    public void startMonitoring() {

        CellLocation.requestLocationUpdate();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onCellInfoChanged(List<CellInfo> nCellInfo) {
        super.onCellInfoChanged(nCellInfo);

        if(nCellInfo != null) {
            //TODO:
            //data here can be sent to DB so whoever is in charge of that
            //can use this data for dumping
            for (CellInfo cell : nCellInfo) {
                Log.i(TAG, "onCellInfoChanged Cell Info>>" + cell);
            }
        }else {
            // return if no cells detected
            return;
        }

    }

    @Override
    public void onCellLocationChanged(CellLocation location) {

        if (location instanceof GsmCellLocation) {
            GsmCellLocation gsmTypeCell = (GsmCellLocation) location;

            //data here can be sent to DB so whoever is in charge of that
            //can use this data for dumping and updating neighboring cells activity
            //TODO: GSM cell data handled here
            Log.i(TAG, "onCellLocationChanged New Cell>>> = " + String.format("\tCid:%d\tLac:%d\tPsc:%d",
                    gsmTypeCell.getCid()
                    ,gsmTypeCell.getLac(),
                    gsmTypeCell.getPsc()));


            List<NeighboringCellInfo> ncl = teleManager.getNeighboringCellInfo();

            if(ncl.size() > 0) {
                for (NeighboringCellInfo cell : ncl) {
                    //
                    Log.i(TAG,String.format("\tCid:%d\tLac:%d\tRssi:%d\tPsc:%d",
                            cell.getCid()
                            ,cell.getLac()
                            ,cell.getRssi(),
                            cell.getPsc()));

                }

            }

        }else if(location instanceof CdmaCellLocation){
            //TODO: CDMA cell data handled here

            CdmaCellLocation cdmaTypeCell = (CdmaCellLocation) location;
            int bid = cdmaTypeCell.getBaseStationId();
            int nid = cdmaTypeCell.getNetworkId();

         /*   if((nid < 0xfa) || (nid >= 0xff)) {
                //not a femeto cell

            }
            else {
                //connected to femeto cell
            }
            */
        }
    }


    }



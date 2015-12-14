package com.SecUpwN.AIMSICD.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.adapters.AIMSICDDbAdapter;
import com.SecUpwN.AIMSICD.adapters.BaseInflaterAdapter;
import com.SecUpwN.AIMSICD.adapters.BtsMeasureCardInflater;
import com.SecUpwN.AIMSICD.adapters.BtsMeasureItemData;
import com.SecUpwN.AIMSICD.adapters.CardItemData;
import com.SecUpwN.AIMSICD.adapters.DbViewerSpinnerAdapter;
import com.SecUpwN.AIMSICD.adapters.DbeImportCardInflater;
import com.SecUpwN.AIMSICD.adapters.DbeImportItemData;
import com.SecUpwN.AIMSICD.adapters.DefaultLocationCardInflater;
import com.SecUpwN.AIMSICD.adapters.EventLogCardInflater;
import com.SecUpwN.AIMSICD.adapters.EventLogItemData;
import com.SecUpwN.AIMSICD.adapters.MeasuredCellStrengthCardData;
import com.SecUpwN.AIMSICD.adapters.MeasuredCellStrengthCardInflater;
import com.SecUpwN.AIMSICD.adapters.SilentSmsCardData;
import com.SecUpwN.AIMSICD.adapters.UniqueBtsCardInflater;
import com.SecUpwN.AIMSICD.adapters.UniqueBtsItemData;
import com.SecUpwN.AIMSICD.constants.DBTableColumnIds;
import com.SecUpwN.AIMSICD.constants.Examples;
import com.SecUpwN.AIMSICD.enums.StatesDbViewer;
import com.SecUpwN.AIMSICD.smsdetection.CapturedSmsCardInflater;
import com.SecUpwN.AIMSICD.smsdetection.CapturedSmsData;
import com.SecUpwN.AIMSICD.smsdetection.DetectionStringsCardInflater;
import com.SecUpwN.AIMSICD.smsdetection.DetectionStringsData;

/**
 * Description:    Class that handles the display of the items in the 'Database Viewer' (DBV)
 * <p/>
 * Issues:
 * <p/>
 * Notes:          See issue #234 for details on how to format the UI
 * <p/>
 * ChangeLog:
 * <p/>
 * 2015-07-14      E:V:A       Changed the display names of several items (see issue #234)
 * 2015-07-31      E:V:A       Added comments and changed some inflater data to avoid using:
 * DBTableColumnIds.java. More to do... Use string convert trick:
 * "" + int = "string"  (See EventLog for example)
 */
public final class DbViewerFragment extends Fragment {

    private AIMSICDDbAdapter mDb;
    private StatesDbViewer mTableSelected;

    // Layout items
    private Spinner tblSpinner;
    private ListView lv;
    private View emptyView;

    public DbViewerFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDb = new AIMSICDDbAdapter(activity.getBaseContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.db_view, container, false);

        lv = (ListView) view.findViewById(R.id.list_view);
        emptyView = view.findViewById(R.id.db_list_empty);
        tblSpinner = (Spinner) view.findViewById(R.id.table_spinner);
        DbViewerSpinnerAdapter mSpinnerAdapter = new DbViewerSpinnerAdapter(getActivity(), R.layout.item_spinner_db_viewer);
        tblSpinner.setAdapter(mSpinnerAdapter);
        tblSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, final int position, long id) {
                Object selectedItem = tblSpinner.getSelectedItem();
                if (!(selectedItem instanceof StatesDbViewer)) {
                    return;
                }
                mTableSelected = (StatesDbViewer) selectedItem;
                new AsyncTask<Void, Void, BaseInflaterAdapter>() {

                    @Override
                    protected BaseInflaterAdapter doInBackground(Void... params) {
                        //# mDb.open();
                        Cursor result;

                        switch (position) {
                            case 0: // UNIQUE_BTS_DATA          ("DBi_bts")
                                // The unique BTSs we have been connected to in the past
                                result = mDb.returnDBiBts();
                                break;

                            case 1: // BTS_MEASUREMENTS:        ("DBi_measure")
                                // All BTS measurements we have done since start
                                result = mDb.returnDBiMeasure();
                                break;

                            case 2: // IMPORTED_OCID_DATA:      ("DBe_import")
                                // All Externally imported BTS data (from OCID, MLS, etc)
                                result = mDb.returnDBeImport();
                                break;

                            case 3: // DEFAULT_MCC_LOCATIONS:   ("defaultlocation")
                                // Default MCC location codes and approx corresponding GPS locations
                                result = mDb.returnDefaultLocation();
                                break;

                            case 4: // SILENT_SMS:              ("SmsData")
                                // SMS log data, such as SMSC, type, etc
                                result = mDb.returnSmsData();
                                break;

                            case 5: // MEASURED_SIGNAL_STRENGTHS: ("DBi_measure")
                                // TODO:     ToBe merged into "DBi_measure:rx_signal"
                                result = mDb.returnDBiMeasure();
                                break;

                            case 6: // EVENT_LOG:               ("EventLog")
                                // Table: "EventLog"
                                result = mDb.returnEventLogData();
                                break;
                            case 7:// SMS DETECTION STRINGS     ("DetectionStrings")
                                result = mDb.returnDetectionStrings();
                                break;

                            // TODO: Not yet implemented...leave as for time being
                            //case 8: // DETECTION_FLAGS:       ("DetectionFlags")
                            //    result = mDb.getDetectionFlagsData();
                            //    break;

                            default:
                                throw new IllegalArgumentException("Unknown type of table");
                        }

                        BaseInflaterAdapter adapter = null;
                        if (result != null) {
                            adapter = BuildTable(result);
                            result.close();
                        }

                        return adapter;
                    }

                    @Override
                    protected void onPostExecute(BaseInflaterAdapter adapter) {
                        if (getActivity() == null) return; // fragment detached

                        lv.setEmptyView(emptyView);
                        if (adapter != null) {
                            lv.setAdapter(adapter);
                            lv.setVisibility(View.VISIBLE);
                        } else {
                            lv.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        }

                        getActivity().setProgressBarIndeterminateVisibility(false);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        return view;
    }

    /**
     * Description:     Content layout and presentation of the Database Viewer
     * <p/>
     * This is where the text labels are created for each column in
     * the Database Viewer (DBV). For details of how this should be presented, see:
     * https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/234
     * <p/>
     * Lat/Lng:     Latitude / Longitude (We should use "Lon" instead of "Lng".)
     * AvgSignal:   Average Signal Strength
     * RSSI:        Received Signal Strength Indicator (previously "Signal Strength")
     * Can have different meanings on different RAN's, e.g. RSCP in UMTS.
     * RAN:         Radio Access Network (GSM, UMTS, LTE etc.)
     * <p/>
     * Notes:
     * <p/>
     * 1. Although "RAN" is more correct here, we'll use "RAT" (Radio Access Technology),
     * which is the more common terminology. Thus reverting.
     * <p/>
     * 2. Since Signal is not an "indicator" we should just call it "RSS" or "RXS"
     */
    private BaseInflaterAdapter BuildTable(Cursor tableData) {
        if (tableData != null && tableData.getCount() > 0) {
            switch (mTableSelected) {

                case UNIQUE_BTS_DATA: {     // DBi_bts

                    BaseInflaterAdapter<UniqueBtsItemData> adapter
                            = new BaseInflaterAdapter<>(new UniqueBtsCardInflater());
                    int count = tableData.getCount();
                    while (tableData.moveToNext()) {
                        UniqueBtsItemData data = new UniqueBtsItemData(
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_MCC))),   // MCC
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_MNC))),   // MNC
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_LAC))),   // LAC
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_CID))),   // CID
                                validatePscValue(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_PSC))),      // PSC
                                //String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_T3212))), // T3212
                                //String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_A5x))),   // A5x
                                //String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_ST_id))), // ST_id
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_TIME_FIRST)),         // time_first
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_TIME_LAST)),          // time_last
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_LAT)),                // gps_lat
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_LON)),                // gps_lon
                                (tableData.getPosition() + 1) + " / " + count                                               // item:  "n/X"
                        );
                        adapter.addItem(data, false);
                    }
                    if (!tableData.isClosed()) {
                        tableData.close();
                    }
                    return adapter;

                }

                case BTS_MEASUREMENTS: {    // DBi_measure

                    BaseInflaterAdapter<BtsMeasureItemData> adapter
                            = new BaseInflaterAdapter<>(new BtsMeasureCardInflater());
                    int count = tableData.getCount();

                    while (tableData.moveToNext()) {
                        // WARNING! The ORDER and number of these are crucial, and need to correspond
                        // to what's found in:  BtsMeasureCardInflater.java and BtsMeasureItemData.java
                        BtsMeasureItemData data = new BtsMeasureItemData(
                                "bts_id: " + String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_BTS_ID))), // TODO: Wrong! Should be DBi_bts:CID
                                "n/a", // + tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_NC_LIST)),        // nc_list      TODO: fix
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_TIME)),                       // time
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_GPSD_LAT)),                   // gpsd_lat
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_GPSD_LON)),                   // gpsd_lon
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_GPSD_ACCURACY))), // gpsd_accu    GPS accuracy in [m]
                                // tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_GPSE_LAT)),                // gpse_lat     TODO: remove
                                // tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_GPSE_LON)),                // gpse_lon     TODO: remove
                                // tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_BB_POWER)),                // bb_power     BP power usage [mA]
                                // tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_BB_RF_TEMP)),              // bb_rf_temp   BP temperature [C]
                                // tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_TX_POWER)),                // tx_power     BP TX Power [dB?]
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_RX_SIGNAL)),                  // rx_signal    BP RX signal [dBm]
                                // tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_RX_STYPE)),                // rx_stype     BP RX signal type [dBm, ASU, RSSI etc]?
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_RAT)),                        // RAT          (Aka RAN = INTEGER)
                                // tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_BCCH)),                    // BCCH         Broadcast Channel
                                // tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_TMSI)),                    // TMSI         Temporary IMSI
                                // String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_TA))),         // TA           Timing Advance (GSM/LTE)
                                // String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_PD))),         // PD           Propagation Delay (LTE)
                                // String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_BER))),        // BER          Bit Error Rate
                                // tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_AVG_EC_NO)),               // AvgEcNo      Average Ec/No
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_IS_SUBMITTED))),  // isSubmitted
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_IS_NEIGHBOUR))),  // isNeighbour
                                // tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_CON_STATE)),               // con_state    AOS data/connection states
                                (tableData.getPosition() + 1) + " / " + count                                                           // item:  "n/X"
                        );
                        adapter.addItem(data, false);
                    }
                    if (!tableData.isClosed()) {
                        tableData.close();
                    }
                    return adapter;

                }

                case IMPORTED_OCID_DATA: {      // DBe_import
                    /*
                     * Table: DBe_import
                     *
                     *   CSV:  lat,lon,mcc,mnc,lac,cellid,averageSignalStrength,range,samples,changeable,radio,rnc,cid,psc,  tac,pci,sid,nid,bid
                     *
                     *   new:   DBe_import
                     *          _id,DBsource,RAT,MCC,MNC,LAC,CID,PSC,gps_lat,gps_lon,isGPSexact,avg_range,avg_signal,samples,time_first,time_last,rej_cause
                     *
                     *  Thus for OCID data we cannot use: time_first or time_last.
                     *
                     */
                    BaseInflaterAdapter<DbeImportItemData> adapter
                            = new BaseInflaterAdapter<>(new DbeImportCardInflater());
                    int count = tableData.getCount();

                    while (tableData.moveToNext()) {
                        // WARNING! The ORDER and number of these are crucial, and need to correspond
                        // to what's found in:  DbeImportCardInflater.java and DbeImportItemData.java
                        // MUST also correspond to the imported OCID CSV order... ???
                        DbeImportItemData data = new DbeImportItemData(
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_DBSOURCE)),                    // DBsource
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_RAT)),                         // RAT
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_MCC))),            // MCC
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_MNC))),            // MNC
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_LAC))),            // LAC
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_CID))),            // CID
                                validatePscValue(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_PSC))),               // PSC (UMTS only)
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_GPS_LAT)),                     // gps_lat
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_GPS_LON)),                     // gps_lon
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_IS_GPS_EXACT))),   // isGPSexact
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_AVG_RANGE))),      // avg_range
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_AVG_SIGNAL))),     // avg_signal
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_SAMPLES))),        // samples
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_TIME_FIRST)),                  // time_first
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_TIME_LAST)),                   // time_last
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_REJ_CAUSE)),                   // rej_cause
                                (tableData.getPosition() + 1) + " / " + count                                                           // item:  "n/X"
                        );
                        adapter.addItem(data, false);
                    }
                    if (!tableData.isClosed()) {
                        tableData.close();
                    }
                    return adapter;
                }

                case DEFAULT_MCC_LOCATIONS: {       // defaultlocation

                    BaseInflaterAdapter<CardItemData> adapter
                            = new BaseInflaterAdapter<>(new DefaultLocationCardInflater());
                    int count = tableData.getCount();

                    while (tableData.moveToNext()) {
                        CardItemData data = new CardItemData(
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DEFAULT_LOCATION_COUNTRY)),           // country
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DEFAULT_LOCATION_MCC))),  // MCC
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DEFAULT_LOCATION_LAT)),               // lat
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DEFAULT_LOCATION_LON)),               // lon
                                (tableData.getPosition() + 1) + " / " + count                                                       // item:  "n/X"
                        );
                        adapter.addItem(data, false);
                    }
                    if (!tableData.isClosed()) {
                        tableData.close();
                    }
                    return adapter;
                }

                case SILENT_SMS: {                  // SmsData

                    BaseInflaterAdapter<CapturedSmsData> adapter
                            = new BaseInflaterAdapter<>(new CapturedSmsCardInflater());
                    if (tableData.getCount() > 0) {
                        while (tableData.moveToNext()) {
                            CapturedSmsData getdata = new CapturedSmsData();

                            // TODO: Add class and smsc
                            // TODO: Check order as in DB schema (ER diagram)
                            getdata.setSmsTimestamp(tableData.getString(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_TIMESTAMP)));        // time
                            getdata.setSmsType(tableData.getString(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_SMS_TYPE)));              // type
                            getdata.setSenderNumber(tableData.getString(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_SENDER_NUMBER)));    // number
                            //getdata.setSenderNumber(tableData.getString(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_SMSC)));           // smsc
                            getdata.setSenderMsg(tableData.getString(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_SENDER_MSG)));          // message
                            //getdata.setSenderNumber(tableData.getString(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_CLASS)));          // class
                            getdata.setCurrent_lac(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_LAC)));                  // lac
                            getdata.setCurrent_cid(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_CID)));                  // cid
                            getdata.setCurrent_nettype(tableData.getString(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_RAT)));           // rat
                            getdata.setCurrent_roam_status(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_ROAM_STATE)));   // isRoaming (BOOL)
                            getdata.setCurrent_gps_lat(tableData.getDouble(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_GPS_LAT)));       // gps_lat
                            getdata.setCurrent_gps_lon(tableData.getDouble(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_GPS_LON)));       // gps_lon

                            adapter.addItem(getdata, false);
                        }
                    }
                    if (!tableData.isClosed()) {
                        tableData.close();
                    }
                    return adapter;
                }

                case MEASURED_SIGNAL_STRENGTHS: {       // DBi_measure:rx_signal

                    // TODO: merge into "DBi_measure:rx_signal"
                    BaseInflaterAdapter<MeasuredCellStrengthCardData> adapter
                            = new BaseInflaterAdapter<>(new MeasuredCellStrengthCardInflater());
                    //int count = tableData.getCount();
                    while (tableData.moveToNext()) {
                        MeasuredCellStrengthCardData data = new MeasuredCellStrengthCardData(
                                tableData.getInt(tableData.getColumnIndex("bts_id")),                           // TODO: CID
                                Integer.parseInt(tableData.getString(tableData.getColumnIndex("rx_signal"))),   // rx_signal
                                tableData.getString(tableData.getColumnIndex("time"))                           // time
                                //"" + (tableData.getPosition() + 1) + " / " + count                            // item:  "n/X"
                        );
                        adapter.addItem(data, false);
                    }
                    if (!tableData.isClosed()) {
                        tableData.close();
                    }
                    return adapter;

                }

                case EVENT_LOG: {           // EventLog

                    BaseInflaterAdapter<EventLogItemData> adapter
                            = new BaseInflaterAdapter<>(new EventLogCardInflater());
                    int count = tableData.getCount();

                    // WARNING: Must correspond with:  EventLogCardInflater  and  EventLogItemData
                    while (tableData.moveToNext()) {
                        EventLogItemData data = new EventLogItemData(
                                // Use the trick to automatically converting int/real to strings by adding empty ""s.
                                "" + tableData.getString(tableData.getColumnIndex("time")),             // time
                                "" + tableData.getInt(tableData.getColumnIndex("LAC")),                 // LAC
                                "" + tableData.getInt(tableData.getColumnIndex("CID")),                 // CID
                                "" + validatePscValue(tableData.getInt(tableData.getColumnIndex("PSC"))),    // PSC
                                "" + tableData.getDouble(tableData.getColumnIndex("gpsd_lat")),         // gpsd_lat
                                "" + tableData.getDouble(tableData.getColumnIndex("gpsd_lon")),         // gpsd_lon
                                "" + tableData.getInt(tableData.getColumnIndex("gpsd_accu")),           // gpsd_accu (accuracy in [m])
                                "" + tableData.getInt(tableData.getColumnIndex("DF_id")),               // DF_id
                                "" + tableData.getString(tableData.getColumnIndex("DF_description")),   // TODO: DF_desc
                                "" + (tableData.getPosition() + 1) + " / " + count                      // item:  "n/X"
                        );
                        // TODO: Explain how to use this?
                        data.setIsFakeData(isExample(data));
                        adapter.addItem(data, false);
                    }
                    if (!tableData.isClosed()) {
                        tableData.close();
                    }
                    return adapter;
                }

                case DETECTION_STRINGS: {          // DetectionStrings
                    // Storage of Abnormal SMS detection strings
                    BaseInflaterAdapter<DetectionStringsData> adapter
                            = new BaseInflaterAdapter<>(new DetectionStringsCardInflater());
                    while (tableData.moveToNext()) {
                        DetectionStringsData data = new DetectionStringsData(
                                tableData.getString(tableData.getColumnIndex("det_str")),  // det_str
                                tableData.getString(tableData.getColumnIndex("sms_type"))  // sms_type
                        );
                        adapter.addItem(data, false);
                    }
                    if (!tableData.isClosed()) {
                        tableData.close();
                    }
                    return adapter;
                }
            }
        } else {
            return null;
        }
        return null;
    }

    private String validatePscValue(int dbPsc) {
        if (dbPsc > 511) {
            return "invalid";
        }
        return String.valueOf(dbPsc);
    }

    /*=========================================================================
     *          Add Example entries into the Database Viewer tables
     *=========================================================================*/

    // Table:           EventLog
    // Dependencies:    Examples.java
    //                  EventLogItemData.java
    private boolean isExample(EventLogItemData pEventLogItemData) {
        return pEventLogItemData != null &&
                pEventLogItemData.getLac().contains(Examples.EVENT_LOG_DATA.LAC) &&
                pEventLogItemData.getCellID().contains(Examples.EVENT_LOG_DATA.CID) &&
                pEventLogItemData.getPsc().contains(Examples.EVENT_LOG_DATA.PSC) &&
                pEventLogItemData.getLat().contains(Examples.EVENT_LOG_DATA.GPSD_LAT) &&
                pEventLogItemData.getLng().contains(Examples.EVENT_LOG_DATA.GPSD_LON) &&
                pEventLogItemData.getgpsd_accu().contains(Examples.EVENT_LOG_DATA.GPSD_ACCU) &&
                pEventLogItemData.getDF_id().contains(Examples.EVENT_LOG_DATA.DF_ID);
    }

    // Table:           SmsData
    // Dependencies:    Examples.java
    //                  SilentSmsCardData.java
    private boolean isExample(SilentSmsCardData pSilentSmsCardData) {
        return pSilentSmsCardData != null &&
                pSilentSmsCardData.getAddress().contains(Examples.SILENT_SMS_CARD_DATA.ADDRESS) &&
                pSilentSmsCardData.getDisplayAddress().contains(Examples.SILENT_SMS_CARD_DATA.DISPLAY);
    }

}

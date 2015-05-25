package com.SecUpwN.AIMSICD.fragments;

import android.app.Activity;
import android.content.Context;
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
import com.SecUpwN.AIMSICD.adapters.CardItemData;
import com.SecUpwN.AIMSICD.adapters.CellCardInflater;
import com.SecUpwN.AIMSICD.adapters.DbViewerSpinnerAdapter;
import com.SecUpwN.AIMSICD.adapters.DefaultLocationCardInflater;
import com.SecUpwN.AIMSICD.adapters.EventLogCardInflater;
import com.SecUpwN.AIMSICD.adapters.EventLogItemData;
import com.SecUpwN.AIMSICD.adapters.MeasuredCellStrengthCardData;
import com.SecUpwN.AIMSICD.adapters.MeasuredCellStrengthCardInflater;
import com.SecUpwN.AIMSICD.adapters.OpenCellIdCardInflater;
import com.SecUpwN.AIMSICD.adapters.SilentSmsCardData;
import com.SecUpwN.AIMSICD.adapters.SilentSmsCardInflater;
import com.SecUpwN.AIMSICD.constants.Examples;
import com.SecUpwN.AIMSICD.enums.StatesDbViewer;
import com.SecUpwN.AIMSICD.smsdetection.CapturedSmsCardInflater;
import com.SecUpwN.AIMSICD.smsdetection.CapturedSmsData;
import com.SecUpwN.AIMSICD.smsdetection.DetectionStringsCardInflater;
import com.SecUpwN.AIMSICD.smsdetection.DetectionStringsData;
import com.SecUpwN.AIMSICD.smsdetection.SmsDetectionDbAccess;
import com.SecUpwN.AIMSICD.smsdetection.SmsDetectionDbHelper;

public class DbViewerFragment extends Fragment {

    private AIMSICDDbAdapter mDb;
    private SmsDetectionDbAccess smsdetection_db;
    private StatesDbViewer mTableSelected;
    private Context mContext;

    //Layout items
    private Spinner tblSpinner;
    private ListView lv;
    private View emptyView;

    public DbViewerFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getBaseContext();
        mDb = new AIMSICDDbAdapter(mContext);
        smsdetection_db = new SmsDetectionDbAccess(mContext);//open new database for sms detection
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.db_view, container, false);

        lv = (ListView) view.findViewById(R.id.list_view);
        emptyView = view.findViewById(R.id.db_list_empty);
        tblSpinner = (Spinner) view.findViewById(R.id.table_spinner);
        DbViewerSpinnerAdapter mSpinnerAdapter = new DbViewerSpinnerAdapter(getActivity(), R.layout.item_spinner_db_viewer);
        tblSpinner.setAdapter(mSpinnerAdapter);

        Spinner spnLocale = (Spinner) view.findViewById(R.id.table_spinner);
        spnLocale.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, final int position, long id) {

                new AsyncTask<Void, Void, BaseInflaterAdapter> () {

                    @Override
                    protected BaseInflaterAdapter doInBackground(Void... params) {
                        mDb.open();
                        Cursor result;

                        //TODO Table: "DetectionFlags"
                        //case DETECTION_FLAGS:
                        //result = mDb.getDetectionFlagsData();
                        mTableSelected = (StatesDbViewer)tblSpinner.getSelectedItem();

                        switch (position) {
                            case 0: //UNIQUE_BTS_DATA
                                // The unique BTSs we have been connected to in the past
                                // EVA: Was "Cell Data" // Table: cellinfo
                                //      ToBe: "DBi_bts"
                                result = mDb.getCellData();
                                break;

                            case 1: //BTS_MEASUREMENTS:
                                // All BTS measurements we have done since start
                                // EVA: Was "Location Data" // Table: locationinfo
                                //      ToBe: "DBi_measure"
                                result = mDb.getLocationData();
                                break;

                            case 2: //IMPORTED_OCID_DATA:
                                // EVA: Was "OpenCellID Data" // Table: opencellid
                                //      ToBe: "DBe_import"
                                result = mDb.getOpenCellIDData();
                                break;

                            case 3: //DEFAULT_MCC_LOCATIONS:
                                result = mDb.getDefaultMccLocationData();
                                break;

                            case 4: //SILENT_SMS:
                                result = smsdetection_db.returnDetectedSmsData();
                                break;

                            case 5: //MEASURED_SIGNAL_STRENGTHS:
                                //      ToBe merged into "DBi_measure:rx_signal"
                                result = mDb.getSignalStrengthMeasurementData();
                                break;

                            case 6: //EVENT_LOG:
                                // Table: "EventLog"
                                result = mDb.getEventLogData();
                                break;
                            case 7:// SMS DETECTION STRINGS
                                result = smsdetection_db.getDetectionStringCursor();
                                break;
                            default:
                                throw new IllegalArgumentException("Unknown type of table");
                        }

                        BaseInflaterAdapter adapter = null;
                        if (result != null) {
                            adapter = BuildTable(result);
                        }
                        mDb.close();

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
                return;
            }
        });

        return view;
    }

    /**
     * Content layout and presentation of the Database Viewer
     *
     * TODO: This need more info as per issue:
     *       https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/234
     *
     * Lat/Lng:     Latitude / Longitude (We should use "Lon" instead of "Lng".)
     * AvgSignal:   Average Signal Strength
     * RSSI:        Received Signal Strength Indicator (previously "Signal Strength")
     *              Can have different meanings on different RAN's, e.g. RSCP in UMTS.
     * RAN:         Radio Access Network (GSM, UMTS, LTE etc.)
     *
     * 2014-12-18  -- E:V:A
     *  1. Although "RAN" is more correct here, we'll use "RAT" (Radio Access Technology),
     *     which is the more common terminology. Thus reverting.
     *  2. Since Signal is not an "indicator" we should just call it "RSS" or "RXS"
     *
     */
    private BaseInflaterAdapter BuildTable(Cursor tableData) {
        if (tableData != null && tableData.getCount() > 0) {
            switch (mTableSelected) {
                case IMPORTED_OCID_DATA: {
                /*
                 * Table: DBi_import
                 *
                 *   CSV:  lat,lon,mcc,mnc,lac,cellid,averageSignalStrength,range,samples,changeable,radio,rnc,cid,psc,  tac,pci,sid,nid,bid
                 *
                 *   old:   opencellid
                 *          _id|Lat|Lng|Mcc|Mnc|Lac|CellID|AvgSigStr|Samples|Timestamp
                 *
                 *   new:   DBe_import
                 *          _id,DBsource,RAT,MCC,MNC,LAC,CID,PSC,gps_lat,gps_lon,isGPSexact,avg_range,avg_signal,samples,time_first,time_last,rej_cause
                 *
                 *  Thus for OCID data we cannot use: time_first or time_last.
                 *
                 */
                    // Table:   DBe_import

                    BaseInflaterAdapter<CardItemData> adapter
                            = new BaseInflaterAdapter<>(new OpenCellIdCardInflater());
                    int count = tableData.getCount();
                    while (tableData.moveToNext()) {
                        // The getString(i) index refer to the table column in the "DBe_import" table
                        //                         OLD  opencellid(i)       // New "DBe_import" column name
                        CardItemData data = new CardItemData(
                                //"Source: " + tableData.getString(0),      // DBsource
                                //"RAT: "    + tableData.getString(0),      // RAT
                                "CID: " + tableData.getString(0),       //
                                "LAC: " + tableData.getString(1),       //
                                "MCC: " + tableData.getString(2),       //
                                "MNC: " + tableData.getString(3),       //
                                //"PSC: "    + tableData.getString(7),      // PSC
                                "Lat: " + tableData.getString(4),       // gps_lat
                                "Lon: " + tableData.getString(5),       // gps_lon
                                //"isExact: " + tableData.getString(7),     // isGPSexact
                                //"Range: "  + tableData.getString(7),      // avg_range //
                                "AvgSignal: " + tableData.getString(6),     // avg_signal
                                "Samples: " + tableData.getString(7),       // samples // NOTE: #7 is range from ocid csv
                                //"first: "  + tableData.getString(7),      // time_first
                                //"last: "   + tableData.getString(7),      // time_last
                                //"reject: " + tableData.getString(7),      // rej_cause
                                "" + (tableData.getPosition() + 1) + " / " + count);
                        adapter.addItem(data, false);
                    }
                    if (!tableData.isClosed()) {
                        tableData.close();
                    }
                    return adapter;
                }
                case DEFAULT_MCC_LOCATIONS: {

                    // Table:   defaultlocation
                    BaseInflaterAdapter<CardItemData> adapter
                            = new BaseInflaterAdapter<>(new DefaultLocationCardInflater());
                    int count = tableData.getCount();
                    while (tableData.moveToNext()) {
                        CardItemData data = new CardItemData(
                                "Country: " + tableData.getString(0),// Country --> country
                                "MCC: " + tableData.getString(1),   // Mcc --> MCC
                                "Lat: " + tableData.getString(2),   // Lat --> lat
                                "Lon: " + tableData.getString(3),   // Lng --> lon
                                "" + (tableData.getPosition() + 1) + " / " + count);
                        adapter.addItem(data, false);
                    }
                    if (!tableData.isClosed()) {
                        tableData.close();
                    }
                    return adapter;
                }
                case SILENT_SMS: {
                    BaseInflaterAdapter<CapturedSmsData> adapter
                            = new BaseInflaterAdapter<>(new CapturedSmsCardInflater());
                    //int count = tableData.getCount();
                    if (tableData.getCount() > 0) {
                        while (tableData.moveToNext()) {
                            CapturedSmsData getdata = new CapturedSmsData();
                            getdata.setSmsTimestamp(tableData.getString(tableData.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_TIMESTAMP)));
                            getdata.setSmsType(tableData.getString(tableData.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_SMS_TYPE)));
                            getdata.setSenderNumber(tableData.getString(tableData.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_SENDER_NUMBER)));
                            getdata.setSenderMsg(tableData.getString(tableData.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_SENDER_MSG)));
                            getdata.setCurrent_lac(tableData.getInt(tableData.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_CURRENT_LAC)));
                            getdata.setCurrent_cid(tableData.getInt(tableData.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_CURRENT_CID)));
                            getdata.setCurrent_nettype(tableData.getString(tableData.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_CURRENT_NETTYPE)));
                            getdata.setCurrent_roam_status(tableData.getString(tableData.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_CURRENT_ROAM_STATE)));
                            getdata.setCurrent_gps_lat(tableData.getDouble(tableData.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_CURRENT_GPS_LAT)));
                            getdata.setCurrent_gps_lon(tableData.getDouble(tableData.getColumnIndex(SmsDetectionDbHelper.SMS_DATA_CURRENT_GPS_LON)));
                            adapter.addItem(getdata, false);
                        }
                    }
                    if (!tableData.isClosed()) {
                        tableData.close();
                    }
                    return adapter;
                }
                case MEASURED_SIGNAL_STRENGTHS: {
                    // ToDo: merge into "DBi_measure:rx_signal"
                    BaseInflaterAdapter<MeasuredCellStrengthCardData> adapter
                            = new BaseInflaterAdapter<>(new MeasuredCellStrengthCardInflater());
                    //int count = tableData.getCount();
                    while (tableData.moveToNext()) {
                        MeasuredCellStrengthCardData data = new MeasuredCellStrengthCardData(
                                tableData.getInt(0),
                                tableData.getInt(1),
                                tableData.getLong(2));
                        //"" + (tableData.getPosition() + 1) + " / " + count);
                        adapter.addItem(data, false);
                    }
                    if (!tableData.isClosed()) {
                        tableData.close();
                    }
                    return adapter;
                }
                case EVENT_LOG: {

                    // Table:   EventLog
                    // Where:   Table is displayed with EventLogCardInflater and EventLogItemData
                    BaseInflaterAdapter<EventLogItemData> adapter
                            = new BaseInflaterAdapter<>(new EventLogCardInflater());

                    int count = tableData.getCount();
                    while (tableData.moveToNext()) {
                        EventLogItemData data = new EventLogItemData(
                                "Time: " + tableData.getString(0),   // time
                                "LAC: " + tableData.getInt(1),      // LAC
                                "CID: " + tableData.getInt(2),      // CID
                                "PSC: " + tableData.getInt(3),      // PSC
                                "Lat: " + tableData.getDouble(4),   // gpsd_lat
                                "Lon: " + tableData.getDouble(5),   // gpsd_lon
                                "Accuracy: " + tableData.getInt(6),      // gpsd_accu (accuracy in [m])
                                "DetID: " + tableData.getInt(7),      // DF_id
                                "Event: " + tableData.getString(8),   // DF_desc
                                "" + (tableData.getPosition() + 1) + " / " + count);
                        data.setIsFakeData(isExample(data));
                        adapter.addItem(data, false);
                    }
                    if (!tableData.isClosed()) {
                        tableData.close();
                    }
                    return adapter;
                }
                case DETECTION_STRINGS: {

                    BaseInflaterAdapter<DetectionStringsData> adapter
                            = new BaseInflaterAdapter<>(new DetectionStringsCardInflater());

                    int count = tableData.getCount();
                    while (tableData.moveToNext()) {
                        DetectionStringsData data = new DetectionStringsData(
                                tableData.getString(0),
                                tableData.getString(1));

                        adapter.addItem(data,false);
                    }
                    if (!tableData.isClosed()) {
                        tableData.close();
                    }
                    return adapter;
                }
/*                // Table:   EventLog
                case "EventLog Data": {
                    BaseInflaterAdapter<CardItemData> adapter
                            = new BaseInflaterAdapter<>( new EventLogCardInflater() );
                    int count = tableData.getCount();
                    while (tableData.moveToNext()) {
                        CardItemData data = new CardItemData(
                                "time: " + tableData.getString(0),  // time
                                "LAC: " + tableData.getString(1),   // LAC
                                "CID: " + tableData.getString(2),   // CID
                                "PSC: " + tableData.getString(3),   // PSC
                                "Lat: " + tableData.getString(4),   // gpsd_lat
                                "Lon: " + tableData.getString(5),   // gpsd_lon
                                "accu: " + tableData.getString(6),  // gpsd_accu (accuracy in [m])
                                "id: " + tableData.getString(7),    // DF_id
                                "event: " + tableData.getString(8), // DF_description
                                "" + (tableData.getPosition() + 1) + " / " + count);
                        adapter.addItem(data, false);
                    }
                    return adapter;
                }
                */
                /*
                // Maybe we can skip this one?
                // Table:   DetectionFlags
                case "DetectionFlags Data": {
                    BaseInflaterAdapter<CardItemData> adapter
                            = new BaseInflaterAdapter<>( new DetectionFlagsCardInflater() );
                    int count = tableData.getCount();
                    while (tableData.moveToNext()) {
                        CardItemData data = new CardItemData(
                                "code: " + tableData.getString(0),
                                "name: " + tableData.getString(1),
                                "description: " + tableData.getString(2),
                                "p1: " + tableData.getString(3),
                                "p2: " + tableData.getString(4),
                                "p3: " + tableData.getString(5),
                                "p1_fine: " + tableData.getString(6),
                                "p2_fine: " + tableData.getString(7),
                                "p3_fine: " + tableData.getString(8),
                                "app_text: " + tableData.getString(8),
                                "func_use: " + tableData.getString(8),
                                "istatus: " + tableData.getString(8),
                                "CM_id: " + tableData.getString(8),
                                "" + (tableData.getPosition() + 1) + " / " + count);
                        adapter.addItem(data, false);
                    }
                    return adapter;
                }

                // Table:   DBi_bts
                case "Unique BTS Data": {
                    BaseInflaterAdapter<CardItemData> adapter
                            = new BaseInflaterAdapter<>( new DBi_btsCardInflater() );
                    int count = tableData.getCount();
                    while (tableData.moveToNext()) {
                        CardItemData data = new CardItemData(
                                "RAT: " + tableData.getString(0),
                                "MCC: " + tableData.getString(1),
                                "MNC: " + tableData.getString(2),
                                "LAC: " + tableData.getString(3),
                                "CID: " + tableData.getString(4),
                                "PSC: " + tableData.getString(5),
                                "T3212: " + tableData.getString(6),
                                "A5x: " + tableData.getString(7),
                                "ST_id: " + tableData.getString(8),
                                "time_first: " + tableData.getString(9),
                                "time_first: " + tableData.getString(10),
                                "" + (tableData.getPosition() + 1) + " / " + count);
                        adapter.addItem(data, false);
                    }
                    return adapter;
                }

                /**
                 * "BTS Measurements" (DBi_measure)
                 *
                 * This is an advanced table, but we can simplify and leave out many
                 * items that we do not have and need.
                 *
                 *
                // Table:   DBi_measure
                case "BTS Measurements": {
                    BaseInflaterAdapter<CardItemData> adapter
                            = new BaseInflaterAdapter<>( new DBi_measureCardInflater() );
                    int count = tableData.getCount();
                    while (tableData.moveToNext()) {
                        CardItemData data = new CardItemData(
                                "bts_id: " + tableData.getString(0),
                                "nc_list: " + tableData.getString(0),
                                "time: " + tableData.getString(0),
                                "gpsd_lat: " + tableData.getString(4),      // gpsd_lat (device)
                                "gpsd_lon: " + tableData.getString(5),      // gpsd_lon (device)
                                "gpsd_accu: " + tableData.getString(6),     // gpsd_accu (accuracy in [m])
                                "gpse_lat: " + tableData.getString(4),      // gpse_lat (exact)
                                "gpse_lon: " + tableData.getString(5),      // gpse_lat (exact)
                                //"speed: " + tableData.getString(1),       //
                                //"bb_power: " + tableData.getString(1),    // BP power usage
                                //"bb_rf_temp: " + tableData.getString(1),  // BP RF temperture
                                "tx_power: " + tableData.getString(1),      // TX power
                                "rx_signal: " + tableData.getString(1),     // RX signal [dBm]
                                //"rx_stype: " + tableData.getString(1),    // RX signal type [dBm, ASU, RSSI etc]?
                                //"BCCH: " + tableData.getString(1),        // BCCH
                                "TMSI: " + tableData.getString(1),          // TMSI
                                "TA: " + tableData.getString(1),            // Timing Advance (GSM/LTE)
                                "PD: " + tableData.getString(1),            // Propagation Delay ? (LTE)
                                "BER: " + tableData.getString(1),           // Bit Error Rate
                                "AvgEcNo: " + tableData.getString(1),       // Avg Ec/No
                                "isSubmitted: " + tableData.getString(1),   // BOOL Submitted to OCID/MLS?
                                //"isNeighbour: " + tableData.getString(1), // BOOL ??is BTS a neighbor?
                                "" + (tableData.getPosition() + 1) + " / " + count);
                        adapter.addItem(data, false);
                    }
                    return adapter;
                }
                */

                /**
                * TODO:
                * This is the default for all other tables, so since we have different
                * info in the new tables we need to create individual entries
                * (instead of default) for these:
                *
                *  - "Unique BTS Data" (DBi_bts)
                *  - "BTS Measurements" (DBi_measure)
                *
                * Once implemented, remove this or make different default.
                */
            default:
                    BaseInflaterAdapter<CardItemData> adapter
                            = new BaseInflaterAdapter<>( new CellCardInflater() );
                    int count = tableData.getCount();
                    while (tableData.moveToNext()) {
                        CardItemData data = new CardItemData(
                                "CID: " + tableData.getString(0),
                                "LAC: " + tableData.getString(1),
                                "RAT: " + tableData.getString(2),
                                "Lat: " + tableData.getString(3),
                                "Lon: " + tableData.getString(4),
                                "RSS: " + tableData.getString(5),
                                "" + (tableData.getPosition() + 1) + " / " + count);
                        adapter.addItem(data, false);
                    }
                    if(!tableData.isClosed()) {
                        tableData.close();
                    }
                    return adapter;
            }
        } else {
            return null;
        }
    }

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

    private boolean isExample(SilentSmsCardData pSilentSmsCardData) {
        return pSilentSmsCardData != null &&
                pSilentSmsCardData.getAddress().contains(Examples.SILENT_SMS_CARD_DATA.ADDRESS) &&
                pSilentSmsCardData.getDisplayAddress().contains(Examples.SILENT_SMS_CARD_DATA.DISPLAY);
    }

}

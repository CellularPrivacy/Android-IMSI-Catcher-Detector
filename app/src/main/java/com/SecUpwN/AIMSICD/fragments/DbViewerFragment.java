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
import com.SecUpwN.AIMSICD.adapters.BtsMeasureCardInflater;
import com.SecUpwN.AIMSICD.adapters.BtsMeasureItemData;
import com.SecUpwN.AIMSICD.adapters.CardItemData;
import com.SecUpwN.AIMSICD.adapters.CellCardInflater;
import com.SecUpwN.AIMSICD.adapters.DbViewerSpinnerAdapter;
import com.SecUpwN.AIMSICD.adapters.DbeImportCardInflater;
import com.SecUpwN.AIMSICD.adapters.DbeImportItemData;
import com.SecUpwN.AIMSICD.adapters.DefaultLocationCardInflater;
import com.SecUpwN.AIMSICD.adapters.EventLogCardInflater;
import com.SecUpwN.AIMSICD.adapters.EventLogItemData;
import com.SecUpwN.AIMSICD.adapters.MeasuredCellStrengthCardData;
import com.SecUpwN.AIMSICD.adapters.MeasuredCellStrengthCardInflater;
import com.SecUpwN.AIMSICD.adapters.OpenCellIdCardInflater;
import com.SecUpwN.AIMSICD.adapters.SilentSmsCardData;
import com.SecUpwN.AIMSICD.adapters.SilentSmsCardInflater;
import com.SecUpwN.AIMSICD.adapters.UniqueBtsCardInflater;
import com.SecUpwN.AIMSICD.adapters.UniqueBtsItemData;
import com.SecUpwN.AIMSICD.constants.DBTableColumnIds;
import com.SecUpwN.AIMSICD.constants.Examples;
import com.SecUpwN.AIMSICD.enums.StatesDbViewer;
import com.SecUpwN.AIMSICD.smsdetection.CapturedSmsCardInflater;
import com.SecUpwN.AIMSICD.smsdetection.CapturedSmsData;
import com.SecUpwN.AIMSICD.smsdetection.DetectionStringsCardInflater;
import com.SecUpwN.AIMSICD.smsdetection.DetectionStringsData;

import java.util.ArrayList;

/**
 *      Class that handles the display of the items in the 'Database Viewer'
 *
 *      Description:
 *
 *      Issues:
 *
 *      Notes:
 *
 *      TODO:           see issue #234
 *
 *
 *      ChangeLog:
 *
 *      2015-07-14      E:V:A       Changed the display names of several items (see issue #234)
 *
 *
 */
public class DbViewerFragment extends Fragment {

    private AIMSICDDbAdapter mDb;
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
                        //# mDb.open();
                        Cursor result;
                        ArrayList<String> CellDetails = new ArrayList<String>();
                        //TODO Table: "DetectionFlags"
                        //case DETECTION_FLAGS:
                        //result = mDb.getDetectionFlagsData();
                        mTableSelected = (StatesDbViewer)tblSpinner.getSelectedItem();

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

                            case 4: // SILENT_SMS:              ("")
                                // SMS log data, such as SMSC, type, etc
                                result = mDb.returnSmsData();
                                break;

                            case 5: // MEASURED_SIGNAL_STRENGTHS: ("")
                                // TODO:     ToBe merged into "DBi_measure:rx_signal"
                                result = mDb.returnDBiMeasure();
                                break;

                            case 6: // EVENT_LOG:               ("EventLog")
                                // Table: "EventLog"
                                result = mDb.returnEventLogData();
                                break;
                            case 7:// SMS DETECTION STRINGS     ("")
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
                        }
                        //# mDb.close();
                        result.close();
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
     * Description: This is where the text labels are created for each column in the Database Viewer
     *              For details of how this should be presented, see:
     *              https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/234
     *
     * Lat/Lng:     Latitude / Longitude (We should use "Lon" instead of "Lng".)
     * AvgSignal:   Average Signal Strength
     * RSSI:        Received Signal Strength Indicator (previously "Signal Strength")
     *              Can have different meanings on different RAN's, e.g. RSCP in UMTS.
     * RAN:         Radio Access Network (GSM, UMTS, LTE etc.)
     *
     * Special Notes:
     *
     *  1. Although "RAN" is more correct here, we'll use "RAT" (Radio Access Technology),
     *     which is the more common terminology. Thus reverting.
     *  2. Since Signal is not an "indicator" we should just call it "RSS" or "RXS"
     *
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
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_MCC))), // MCC
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_MNC))), // MNC
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_LAC))), // LAC
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_CID))), // CID
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_PSC))), // PSC
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_TIME_FIRST)),       // time_first
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_TIME_LAST)),        // time_last
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_LAT)),              // gps_lat
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_BTS_LON))               // gps_lon
                        );
                        // "" + (tableData.getPosition() + 1) + " / " + count);
                        adapter.addItem(data,false);
                    }
                    if (!tableData.isClosed()) {
                        tableData.close();
                    }
                    return adapter;

                }

                case BTS_MEASUREMENTS: {    // DBi_measure

                    BaseInflaterAdapter<BtsMeasureItemData> adapter
                            = new BaseInflaterAdapter<>(new BtsMeasureCardInflater());
//                    int count = tableData.getCount();

                    while (tableData.moveToNext()) {
                        
                        // WARNING! check that the ORDER of these are not crucial??
                        BtsMeasureItemData data = new BtsMeasureItemData(
                                "bts_id: "      + String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_BTS_ID))),  // TODO: Wrong! Should be DBi_bts:CID
                                "nc_list: ",// + tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_NC_LIST)),               // nc_list
                                "time: "        + tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_TIME)),                 // time
                                "gpsd_lat: "    + tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_GPSD_LAT)),             // gpsd_lat
                                "gpsd_lon: "    + tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_GPSD_LON)),             // gpsd_lon
                                "gpsd_accu: "   + String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_GPSD_ACCURACY))), // gpsd_accu
                                //      "GPSE LAT: "  + tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_GPSE_LAT)),       // gpse_lat (remove?)
                                //      "GPSE LON: "  + tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_GPSE_LON)),       // gpse_lon (remove?)
                                "bb_power: "    + tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_BB_POWER)),             // bb_power
                                //      "bb_rf_temp: " + tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_BB_RF_TEMP)),    // bb_rf_temp
                                //      "tx_power: "  + tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_TX_POWER)),       // tx_power
                                "rx_signal: "   + tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_RX_SIGNAL)),            // rx_signal
                                //      "rx_stype: " + tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_RX_STYPE)),        // rx_stype
                                "RAT: "         + tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_RAT)),                  // RAT
                                //      "BCCH: " + tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_BCCH)),                // BCCH
                                //      "TMSI: " + tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_TMSI)),                // TMSI
                                //      "TA: " + String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_TA))),       // TA
                                //      "PD: " + String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_PD))),       // PD
                                //      "BER: " + String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_BER))),     // BER
                                //      "AvgEcNo: " + tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_AVG_EC_NO)),        // AvgEcNo
                                "isSubmitted: " + String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_IS_SUBMITTED))), // isSubmitted
                                "isNeighbour: " + String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_IS_NEIGHBOUR)))  // isNeighbour
                                //"" + (tableData.getPosition() + 1) + " / " + count
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
                 *   old:   opencellid
                 *          _id|Lat|Lng|Mcc|Mnc|Lac|CellID|AvgSigStr|Samples|Timestamp
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

                        // WARNING! The ORDER of these are crucial!!  MUST correspond to the imported OCID CSV order...
                        DbeImportItemData data = new DbeImportItemData(
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_DBSOURCE))+
                                        "\t\t" + (tableData.getPosition() + 1) + " / " + count,                                   // DBsource + count (record_id?)
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_RAT)),                   // RAT
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_MCC))),      // MCC
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_MNC))),      // MNC
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_LAC))),      // LAC
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_CID))),      // CID
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_PSC))),      // PSC (UMTS only)
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_GPS_LAT)),               // gps_lat
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_GPS_LON)),               // gps_lon
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_IS_GPS_EXACT))),   //isGPSexact                                                                     // isGPSexact
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_AVG_RANGE))),      // avg_range //
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_AVG_SIGNAL))),     // avg_signal
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_SAMPLES))),        // samples // NOTE: #7 is range from ocid csv
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_TIME_FIRST)),                  // time_first
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBE_IMPORT_TIME_LAST)));                  // time_last
                        // TODO move ? "source" here and add:
                        // "" + (tableData.getPosition() + 1) + " / " + count );
                        adapter.addItem(data, false);
                    }
                    if (!tableData.isClosed()) {
                        tableData.close();
                    }
                    return adapter;
                }

                case DEFAULT_MCC_LOCATIONS: {       // defaultlocation

                    // Table:   defaultlocation
                    BaseInflaterAdapter<CardItemData> adapter
                            = new BaseInflaterAdapter<>(new DefaultLocationCardInflater());
                    int count = tableData.getCount();
                    while (tableData.moveToNext()) {
                        CardItemData data = new CardItemData(
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DEFAULT_LOCATION_COUNTRY)),           // country
                                String.valueOf(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DEFAULT_LOCATION_MCC))),  // MCC
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DEFAULT_LOCATION_LAT)),               // lat
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DEFAULT_LOCATION_LON)),               // lon
                                (tableData.getPosition() + 1) + " / " + count);                                                     // item:  "n/X"
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
                    if (tableData.getCount() > 0) {
                        while (tableData.moveToNext()) {
                            CapturedSmsData getdata = new CapturedSmsData();
                            // TODO: Add human readable labels in same manner as for tables above
                            //The human readable labels are created in the layout so no need
                            getdata.setSmsTimestamp(tableData.getString(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_TIMESTAMP)));
                            getdata.setSmsType(tableData.getString(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_SMS_TYPE)));
                            getdata.setSenderNumber(tableData.getString(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_SENDER_NUMBER)));
                            getdata.setSenderMsg(tableData.getString(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_SENDER_MSG)));
                            getdata.setCurrent_lac(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_LAC)));
                            getdata.setCurrent_cid(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_CID)));
                            getdata.setCurrent_nettype(tableData.getString(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_RAT)));
                            getdata.setCurrent_roam_status(tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_ROAM_STATE)));
                            getdata.setCurrent_gps_lat(tableData.getDouble(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_GPS_LAT)));
                            getdata.setCurrent_gps_lon(tableData.getDouble(tableData.getColumnIndex(DBTableColumnIds.SMS_DATA_GPS_LON)));
                            adapter.addItem(getdata, false);
                        }
                    }
                    if (!tableData.isClosed()) {
                        tableData.close();
                    }
                    return adapter;
                }

                case MEASURED_SIGNAL_STRENGTHS: {

                    // TODO: merge into "DBi_measure:rx_signal"
                    BaseInflaterAdapter<MeasuredCellStrengthCardData> adapter
                            = new BaseInflaterAdapter<>(new MeasuredCellStrengthCardInflater());
                    //int count = tableData.getCount();
                    while (tableData.moveToNext()) {
                        MeasuredCellStrengthCardData data = new MeasuredCellStrengthCardData(
                                // TODO: Add human readable labels in same manner as for tables above
                                //The human readable labels are created in the layout so no need
                                tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_BTS_ID)),
                                Integer.parseInt(tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_BB_POWER))),
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DBI_MEASURE_TIME)));
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
                                "Time: " + tableData.getString(tableData.getColumnIndex(DBTableColumnIds.EVENTLOG_TIME)),   // time
                                "LAC: " + tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.EVENTLOG_LAC)),        // LAC
                                "CID: " + tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.EVENTLOG_CID)),        // CID
                                "PSC: " + tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.EVENTLOG_PSC)),        // PSC
                                "Lat: " + tableData.getDouble(tableData.getColumnIndex(DBTableColumnIds.EVENTLOG_LAT)),     // gpsd_lat
                                "Lon: " + tableData.getDouble(tableData.getColumnIndex(DBTableColumnIds.EVENTLOG_LON)),     // gpsd_lon
                                "Accu: " + tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.EVENTLOG_ACCU)),      // gpsd_accu (accuracy in [m])
                                "DF_id: " + tableData.getInt(tableData.getColumnIndex(DBTableColumnIds.EVENTLOG_DF_ID)),    // DF_id
                                "Event: " + tableData.getString(tableData.getColumnIndex(DBTableColumnIds.EVENTLOG_DF_DESC)),// DF_desc
                                "" + (tableData.getPosition() + 1) + " / " + count);
                        data.setIsFakeData(isExample(data));
                        adapter.addItem(data, false);
                    }
                    if (!tableData.isClosed()) {
                        tableData.close();
                    }
                    return adapter;
                }

                case DETECTION_STRINGS: {       // Abnormal SMS detection strings

                    BaseInflaterAdapter<DetectionStringsData> adapter
                            = new BaseInflaterAdapter<>(new DetectionStringsCardInflater());

                    int count = tableData.getCount();
                    while (tableData.moveToNext()) {
                        DetectionStringsData data = new DetectionStringsData(
                                // TODO: Add human readable labels in same manner as for tables above
                                //The human readable labels are created in the layout so no need
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DETECTION_STRINGS_LOGCAT_STRING)),
                                tableData.getString(tableData.getColumnIndex(DBTableColumnIds.DETECTION_STRINGS_SMS_TYPE)));

                        adapter.addItem(data, false);
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

            }
        } else {
            return null;
        }
        return null;
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

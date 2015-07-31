package com.SecUpwN.AIMSICD.constants;
/**
 * Description:     These are some static constants that represent the SQLite DB
 *                  table names. These should normally NOT be used, as hardcoded strings
 *                  make everything much more transparent...
 *
 * Note:            Try keep the same order as in the aimsicd.sql tables
 *
 */
public class DBTableColumnIds {
    //defaultlocation
    public static final String DEFAULT_LOCATION_TABLE_NAME = "defaultlocation";
    public static final String DEFAULT_LOCATION_ID = "_id";
    public static final String DEFAULT_LOCATION_COUNTRY = "country";
    public static final String DEFAULT_LOCATION_MCC = "MCC";
    public static final String DEFAULT_LOCATION_LAT = "lat";
    public static final String DEFAULT_LOCATION_LON = "lon";

    //API_keys
    public static final String API_KEYS_TABLE_NAME = "API_keys";
    public static final String API_KEYS_ID = "_id";
    public static final String API_KEYS_NAME = "name";
    public static final String API_KEYS_TYPE = "type";
    public static final String API_KEYS_KEY = "key";
    public static final String API_KEYS_TIME_ADD = "time_add";
    public static final String API_KEYS_TIME_EXP = "time_exp";

    //CounterMeasures
    public static final String COUNTER_MEASURES_TABLE_NAME = "CounterMeasures";
    public static final String COUNTER_MEASURES_ID = "_id";
    public static final String COUNTER_MEASURES_NAME = "name";
    public static final String COUNTER_MEASURES_DESCRIPTION = "description";
    public static final String COUNTER_MEASURES_THRESH = "thresh";
    public static final String COUNTER_MEASURES_THFINE = "thfine";

    //DBe_capabilities
    public static final String DBE_CAPABILITIES_TABLE_NAME = "DBe_capabilities";
    public static final String DBE_CAPABILITIES_ID = "_id";
    public static final String DBE_CAPABILITIES_MCC = "MCC";
    public static final String DBE_CAPABILITIES_MNC = "MNC";
    public static final String DBE_CAPABILITIES_LAC = "LAC";
    public static final String DBE_CAPABILITIES_OP_NAME = "op_name";
    public static final String DBE_CAPABILITIES_BAND_PLAN = "band_plan";
    public static final String DBE_CAPABILITIES_EXPAND = "__EXPAND__";

    //DBe_import
    public static final String DBE_IMPORT_TABLE_NAME = "DBe_import";
    public static final String DBE_IMPORT_ID = "_id";
    public static final String DBE_IMPORT_DBSOURCE = "DBsource";
    public static final String DBE_IMPORT_RAT = "RAT";
    public static final String DBE_IMPORT_MCC = "MCC";
    public static final String DBE_IMPORT_MNC = "MNC";
    public static final String DBE_IMPORT_LAC = "LAC";
    public static final String DBE_IMPORT_CID = "CID";
    public static final String DBE_IMPORT_PSC = "PSC";
    public static final String DBE_IMPORT_GPS_LAT = "gps_lat";
    public static final String DBE_IMPORT_GPS_LON = "gps_lon";
    public static final String DBE_IMPORT_IS_GPS_EXACT = "isGPSexact";
    public static final String DBE_IMPORT_AVG_RANGE = "avg_range";
    public static final String DBE_IMPORT_AVG_SIGNAL = "avg_signal";
    public static final String DBE_IMPORT_SAMPLES = "samples";
    public static final String DBE_IMPORT_TIME_FIRST = "time_first";
    public static final String DBE_IMPORT_TIME_LAST = "time_last";
    public static final String DBE_IMPORT_REJ_CAUSE = "rej_cause";

    //DBi_bts
    public static final String DBI_BTS_TABLE_NAME = "DBi_bts";
    public static final String DBI_BTS_ID = "_id";
    public static final String DBI_BTS_MCC = "MCC";
    public static final String DBI_BTS_MNC = "MNC";
    public static final String DBI_BTS_LAC = "LAC";
    public static final String DBI_BTS_CID = "CID";
    public static final String DBI_BTS_PSC = "PSC";
    public static final String DBI_BTS_T3212 = "T3212";
    public static final String DBI_BTS_A5X = "A5x";
    public static final String DBI_BTS_ST_ID = "ST_id";
    public static final String DBI_BTS_TIME_FIRST = "time_first";
    public static final String DBI_BTS_TIME_LAST = "time_last";
    public static final String DBI_BTS_LAT = "gps_lat";
    public static final String DBI_BTS_LON = "gps_lon";

    //DBi_measure
    public static final String DBI_MEASURE_TABLE_NAME = "DBi_measure";
    public static final String DBI_MEASURE_ID = "_id";
    public static final String DBI_MEASURE_BTS_ID = "bts_id";
    public static final String DBI_MEASURE_NC_LIST = "nc_list";
    public static final String DBI_MEASURE_TIME = "time";
    public static final String DBI_MEASURE_GPSD_LAT = "gpsd_lat";
    public static final String DBI_MEASURE_GPSD_LON = "gpsd_lon";
    public static final String DBI_MEASURE_GPSD_ACCURACY = "gpsd_accu";
    public static final String DBI_MEASURE_GPSE_LAT = "gpse_lat";
    public static final String DBI_MEASURE_GPSE_LON = "gpse_lon";
    public static final String DBI_MEASURE_BB_POWER = "bb_power";
    public static final String DBI_MEASURE_BB_RF_TEMP = "bb_rf_temp";
    public static final String DBI_MEASURE_TX_POWER = "tx_power";
    public static final String DBI_MEASURE_RX_SIGNAL = "rx_signal";
    public static final String DBI_MEASURE_RX_STYPE = "rx_stype";
    public static final String DBI_MEASURE_RAT = "RAT";
    public static final String DBI_MEASURE_BCCH = "BCCH";
    public static final String DBI_MEASURE_TMSI = "TMSI";
    public static final String DBI_MEASURE_TA = "TA";
    public static final String DBI_MEASURE_PD = "PD";
    public static final String DBI_MEASURE_BER = "BER";
    public static final String DBI_MEASURE_AVG_EC_NO = "AvgEcNo";
    public static final String DBI_MEASURE_IS_SUBMITTED = "isSubmitted";
    public static final String DBI_MEASURE_IS_NEIGHBOUR = "isNeighbour";
    public static final String DBI_MEASURE_FOREIGN_KEY = "bts_id"; // TODO: Remove?

    //DetectionFlags
    public static final String DETECTION_FLAGS_TABLE_NAME = "DetectionFlags";
    public static final String DETECTION_FLAGS_ID = "_id";
    public static final String DETECTION_FLAGS_CODE = "code";
    public static final String DETECTION_FLAGS_NAME = "name";
    public static final String DETECTION_FLAGS_DESCRIPTION = "description";
    public static final String DETECTION_FLAGS_P1 = "p1";
    public static final String DETECTION_FLAGS_P2 = "p2";
    public static final String DETECTION_FLAGS_P3 = "p3";
    public static final String DETECTION_FLAGS_P1_FINE = "p1_fine";
    public static final String DETECTION_FLAGS_P2_FINE = "p2_fine";
    public static final String DETECTION_FLAGS_P3_FINE = "p3_fine";
    public static final String DETECTION_FLAGS_APP_TEXT = "app_text";
    public static final String DETECTION_FLAGS_FUNC_USE = "func_use";
    public static final String DETECTION_FLAGS_IS_STATUS = "istatus";
    public static final String DETECTION_FLAGS_CM_ID = "CM_id";

    //EventLog
    public static final String EVENTLOG_TABLE_NAME = "EventLog";
    public static final String EVENTLOG_TIME = "time";
    public static final String EVENTLOG_LAC = "LAC";
    public static final String EVENTLOG_CID = "CID";
    public static final String EVENTLOG_PSC = "PSC";
    public static final String EVENTLOG_LAT = "gpsd_lat";
    public static final String EVENTLOG_LON = "gpsd_lon";
    public static final String EVENTLOG_ACCU = "gpsd_accu";
    public static final String EVENTLOG_DF_ID = "DF_id";
    public static final String EVENTLOG_DF_DESC = "DF_description"; // TODO: should be "DF_desc"

    //SectorType
    public static final String SECTOR_TYPE_TABLE_NAME = "SectorType";
    public static final String SECTOR_TYPE_ID = "_id";
    public static final String SECTOR_TYPE_DESCRIPTION = "description";

    //DetectionStrings  (For SMS)
    public static final String DETECTION_STRINGS_TABLE_NAME = "DetectionStrings";
    public static final String DETECTION_STRINGS_ID = "_id";
    public static final String DETECTION_STRINGS_LOGCAT_STRING = "det_str";
    public static final String DETECTION_STRINGS_SMS_TYPE = "sms_type";

    //SmsData  (For SMS)
    public static final String SMS_DATA_TABLE_NAME = "SmsData";
    public static final String SMS_DATA_ID = "_id";
    public static final String SMS_DATA_TIMESTAMP = "time";
    public static final String SMS_DATA_SENDER_NUMBER = "number";
    public static final String SMS_DATA_SENDER_SMSC = "smsc";
    public static final String SMS_DATA_SENDER_MSG = "message";
    public static final String SMS_DATA_SMS_TYPE = "type";
    public static final String SMS_DATA_SMS_CLASS = "class";
    public static final String SMS_DATA_LAC = "lac";
    public static final String SMS_DATA_CID = "cid";
    public static final String SMS_DATA_RAT = "rat";
    public static final String SMS_DATA_GPS_LAT = "gps_lat";
    public static final String SMS_DATA_GPS_LON = "gps_lon";
    public static final String SMS_DATA_ROAM_STATE = "isRoaming";


}

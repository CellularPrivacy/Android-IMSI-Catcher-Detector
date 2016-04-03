package com.secupwn.aimsicd.constants;
/**
 * These are some static constants that represent the SQLite DB table names.
 * These should normally NOT be used, as hardcoded strings make everything
 * much more transparent. Try keep the same order as in the aimsicd.sql tables.
 */
public class DBTableColumnIds {

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
    public static final String DBI_BTS_JOINED_RAT = "RAT";

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

}

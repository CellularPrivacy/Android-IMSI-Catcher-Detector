/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
 
/*
========================================================================
FileName:	aimsicd.sql
Version:	0.1
Formatting:	8 char TAB, ASCII, UNIX EOL
Author:		E:V:A (Team AIMSICD)
Date:		2015-01-20
Info:		https://github.com/SecUpwN/Android-IMSI-Catcher-Detector
========================================================================

Description:

	This is the aimsicd.db SQL script. It is used in the 
	"Android IMIS-Cather Detector" (AIMSICD) application, 
	to create all and pre-populate some of its DB tables.

Dependencies:


Pre-loaded Imports:

	* defaultlocation.csv	# The default MCC country values
	* DetectionFlags.csv	# Detection flag descriptions and parameter settings
	- CounterMeasures.csv	# Counter-measures descriptions and thresholds
	- API_keys.csv		# API keys for various external DBs (expiration)
	? DBe_capabilities	# MNO capabilities details (WIP)
	---------------------------------------------------------------
	[*,-,?] = required, WIP, maybe
	---------------------------------------------------------------

How to use:

	# To build the database use: 
	# cd /data/data/com.SecUpwN.AIMSICD/databases/
	# cat aimsicd.sql | sqlite3 aimsicd.db

Developer Notes:

	a) The sqlite_sequence table is created and initialized 
	   automatically whenever a normal table that contains 
	   an AUTOINCREMENT column is created.
	b) 

The old DB schema is created with:

	CREATE TABLE android_metadata	(locale TEXT);
	CREATE TABLE silentsms		(_id INTEGER PRIMARY KEY AUTOINCREMENT, Address VARCHAR, Display VARCHAR, Class VARCHAR, ServiceCtr VARCHAR, Message VARCHAR, Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);
	CREATE TABLE locationinfo	(_id integer primary key autoincrement, Lac INTEGER, CellID INTEGER, Net VARCHAR, Lat VARCHAR, Lng VARCHAR, Signal INTEGER, Connection VARCHAR, Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);
	CREATE TABLE cellinfo		(_id integer primary key autoincrement, Lac INTEGER, CellID INTEGER, Net INTEGER, Lat VARCHAR, Lng VARCHAR, Signal INTEGER, Mcc INTEGER, Mnc INTEGER, Accuracy REAL, Speed REAL, Direction REAL, NetworkType VARCHAR, MeasurementTaken VARCHAR, OCID_SUBMITTED INTEGER DEFAULT 0, Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);
	CREATE TABLE opencellid		(_id integer primary key autoincrement, Lat VARCHAR, Lng VARCHAR, Mcc INTEGER, Mnc INTEGER, Lac INTEGER, CellID INTEGER, AvgSigStr INTEGER, Samples INTEGER, Timestamp TIMESTAMP NOT NULL DEFAULT current_timestamp);
	CREATE TABLE defaultlocation	(_id integer primary key autoincrement, Country VARCHAR, Mcc INTEGER, Lat VARCHAR, Lng VARCHAR);



=======================================================================
*/

-- ========================================================
--  Special Table Notes
-- ========================================================
/* 
  DBe_capabilities:
  [1] http://en.wikipedia.org/wiki/Cellular_network#Coverage_comparison_of_different_frequencies



*/

-- ========================================================
--  START
-- ========================================================

PRAGMA foreign_keys=OFF;
BEGIN TRANSACTION;

-- ========================================================
-- Let's drop old tables if they already exsist 
-- ========================================================

DROP TABLE IF EXISTS "android_metadata";
DROP TABLE IF EXISTS "defaultlocation";
DROP TABLE IF EXISTS "API_keys";
DROP TABLE IF EXISTS "CounterMeasures";
DROP TABLE IF EXISTS "DBe_capabilities";
DROP TABLE IF EXISTS "DBe_import";
DROP TABLE IF EXISTS "DBi_bts";
DROP TABLE IF EXISTS "DBi_measure";
DROP TABLE IF EXISTS "DetectionFlags";
DROP TABLE IF EXISTS "EventLog";
DROP TABLE IF EXISTS "SectorType";
DROP TABLE IF EXISTS "silentsms";

-- ========================================================
-- CREATE new tables 
-- ========================================================

CREATE TABLE "android_metadata"  ( 
	"locale"	TEXT 
	);

CREATE TABLE "defaultlocation"  ( 
	"_id"     	INTEGER PRIMARY KEY,
	"country"	TEXT,
	"MCC"    	INTEGER,
	"lat"    	TEXT,
	"lon"    	TEXT
	);

CREATE TABLE "API_keys"  ( 
	"_id"      	INTEGER PRIMARY KEY,
	"name"    	TEXT,
	"type"    	TEXT,
	"key"     	TEXT,
	"time_add"	TEXT,
	"time_exp"	TEXT
	);

CREATE TABLE "CounterMeasures"  ( 
	"_id"         	INTEGER PRIMARY KEY,
	"name"       	TEXT,
	"description"	TEXT,
	"thresh"     	INTEGER,
	"thfine"     	REAL
	);

CREATE TABLE "DBe_capabilities"  ( 
	"_id"        	INTEGER PRIMARY KEY,
	"MCC"       	TEXT,
	"MNC"       	TEXT,
	"LAC"       	TEXT,
	"op_name"   	TEXT,
	"band_plan" 	TEXT,
	"__EXPAND__"	TEXT
	);

CREATE TABLE "DBe_import"  ( 
	"_id"        	INTEGER PRIMARY KEY AUTOINCREMENT,
	"DBsource"  	TEXT NOT NULL,
	"RAT"       	TEXT,
	"MCC"       	INTEGER,
	"MNC"       	INTEGER,
	"LAC"       	INTEGER,
	"CID"       	INTEGER,
	"PSC"       	INTEGER,
	"gps_lat"   	TEXT,
	"gps_lon"   	TEXT,
	"isGPSexact"	INTEGER,
	"avg_range" 	INTEGER,
	"avg_signal"	INTEGER,
	"samples"   	INTEGER,
	"time_first"	TEXT,
	"time_last" 	TEXT,
	"rej_cause" 	INTEGER
	);

CREATE TABLE "DBi_bts"  ( 
	"_id"        	INTEGER PRIMARY KEY AUTOINCREMENT,
	"RAT"       	TEXT NOT NULL,
	"MCC"       	INTEGER NOT NULL,
	"MNC"       	INTEGER NOT NULL,
	"LAC"       	INTEGER NOT NULL,
	"CID"       	INTEGER NOT NULL,
	"PSC"       	INTEGER,
	"T3212"     	INTEGER,
	"A5x"       	INTEGER,
	"ST_id"     	INTEGER,
	"time_first"	TEXT,
	"time_last" 	TEXT
	);

CREATE TABLE "DBi_measure"  ( 
	"_id"           INTEGER PRIMARY KEY AUTOINCREMENT,
	"bts_id"       	INTEGER NOT NULL,
	"nc_list"      	TEXT,
	"time"         	TEXT NOT NULL,
	"gpsd_lat"     	TEXT NOT NULL,
	"gpsd_lon"     	TEXT NOT NULL,
	"gpsd_accuracy"	INTEGER,
	"gpse_lat"     	TEXT,
	"gpse_lon"     	TEXT,
	"speed"        	TEXT,
	"bb_power"     	TEXT,
	"bb_rf_temp"   	TEXT,
	"tx_power"     	TEXT,
	"rx_signal"    	TEXT,
	"rx_stype"     	TEXT,
	"BCCH"         	TEXT,
	"TMSI"         	TEXT,
	"TA"           	INTEGER,
	"PD"           	INTEGER,
	"BER"          	INTEGER,
	"AvgEcNo"      	TEXT,
	"isSubmitted"  	INTEGER DEFAULT 0,
	"isNeighbour"  	INTEGER DEFAULT 0,
	FOREIGN KEY("bts_id")
	REFERENCES "DBi_bts"("_id")
	);


CREATE TABLE "DetectionFlags"  ( 
	"_id"         	INTEGER PRIMARY KEY,
	"code"       	INTEGER,
	"name"       	TEXT,
	"description"	TEXT,
	"p1"         	INTEGER,
	"p2"         	INTEGER,
	"p3"         	INTEGER,
	"p1_fine"    	REAL,
	"p2_fine"    	REAL,
	"p3_fine"    	REAL,
	"app_text"   	TEXT,
	"func_use"   	TEXT,
	"istatus"    	INTEGER,
	"CM_id"      	INTEGER
	);

CREATE TABLE "EventLog"  ( 
	"_id"            	INTEGER PRIMARY KEY AUTOINCREMENT,
	"time"     		TEXT NOT NULL,
	"LAC"           	INTEGER NOT NULL,
	"CID"           	INTEGER NOT NULL,
	"PSC"           	INTEGER,
	"gpsd_lat"      	TEXT,
	"gpsd_lon"      	TEXT,
	"gpsd_accu"     	INTEGER,
	"DF_id"         	INTEGER,
	"DF_description"	TEXT,
	-- Do we need these?
	FOREIGN KEY("DF_id")
	REFERENCES "DetectionFlags"("_id")
	);

CREATE TABLE "SectorType"  ( 
	"_id"         	INTEGER PRIMARY KEY,
	"description"	TEXT 
	);


CREATE TABLE "silentsms"  ( 
	"_id"     	INTEGER PRIMARY KEY,
	"time"   	TEXT
	"address"	TEXT,
	"display"	TEXT,
	"class"  	TEXT,
	"SMSC"   	TEXT,
	"message"	TEXT,
	);

-- ========================================================
--   END
-- ========================================================
COMMIT;

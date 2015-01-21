-- PRAGMA foreign_keys=OFF;
-- BEGIN TRANSACTION;

DROP TABLE IF EXISTS "DBe_import";

CREATE TABLE "DBe_import"  ( 
	"_id"        	INTEGER PRIMARY KEY AUTOINCREMENT,
	"DBsource"  	TEXT NOT NULL,	-- "OCID" or "MLS"
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
	"avg_signal"	INTEGER,	-- Does this need to be REAL for "-nn" [dBm]
	"samples"   	INTEGER,	-- Does this need to be REAL for "-1"
	"time_first"	TEXT,
	"time_last" 	TEXT,
	"rej_cause" 	INTEGER
	);

-- COMMIT;

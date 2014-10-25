<strong>This is Work in Progress!</strong>
Thus this is not yet accurate and quite possibly wrong info.

<h2>Introduction</h2>

<p>AIMSICD utillize several tables in a single SQLite3 database in <em>aimsicd.db</em> to keep track 
of all the network changes and the downloaded Open Cell ID (OCID) data. When you're making a file 
backup of the AOMSICD database, you're actually saving the various tables into individual 
<tablename>.CSV files. This make it easy to manually or externally update some tables.</p>

<p>All mentioned sqlite commands are properly documented on the <a href="http://www.sqlite.org">SQLite website</a></p>

<h2>Accessing the databases:</h2>

<h3>From a PC:</h3>

<p><code>adb shell</code></p>
<p><code>su</code></p>
<p><code>sqlite3 /data/data/com.SecUpwN.AIMSICD/databases/aimsicd.db</code></p>

<h3>From a Terminal Emulator within Android:</h3>

<p><code>su</code></p>
</p><code>sqlite3 /data/data/com.SecUpwN.AIMSICD/databases/aimsicd.db</code></p>
<p>*Note: You may need to install sqlite3 binaries</p>

<h2><em>aimsicd.db</em> consists of four relevant tables</h2>

<h3>TABLE:LOCATION_TABLE</h3>
<h3>TABLE:CELL_TABLE</h3>
<h3>TABLE:OPENCELLID_TABLE</h3>
<h3>TABLE:DEFAULT_MCC_TABLE</h3>
<h3>TABLE:SILENT_SMS_TABLE</h3>


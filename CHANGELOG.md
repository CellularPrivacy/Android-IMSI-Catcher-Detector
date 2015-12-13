# CHANGELOG of 'AIMSICD'
----------------------

#### [13.12.2015 - WIP-Release v0.1.37-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.37-alpha)

* Changed: Slimmed down permissions to the bare necessary ones to ensure peace for us privacy geeks
* Changed: Moving towards a more usable and polished interface by adding a bit Material Design
* Changed: Versioning from manually editing `AndroidManifest.xml` to using current `commitId`
* Changed: Replaced deprecated `Apache HttpClient` and `HttpUrlConnection` with `okhttp`
* Changed: Resized documentation button in About Fragment to fit screen for more devices
* Changed: Now loading `OSMBonusPack` as Maven dependency, renamed app module to `AIMSICD`
* Updated: Invalid `PSC` is now shown if saved value is higher than 511 (`PSC` max value)
* Updated: Now using Gradle Wrapper 2.9 and cleaned up `AIMSICDDbAdapter` implementation
* Updated: Now using a logging interface and better logging in Activities and Services
* Updated: Cleaned Proguard rules, several `README` improvements with updated links and guides
* Removed: Purged unused `activity_open_cell_id.xml`, `split.rb` and `signing.properties`
* Removed: Purged ProgressBar since it was accessed in a very strange way, will be replaced soon
* Removed: Purged obsolete and redundant things from our `build.gradle`
* Added: New translation for Ukrainian and Norwegian Bokmål, improved Japanese and Spanish
* Added: Gradle magic to also build a system app using system permissions and be included in ROMs
* Fixed: Calling `msgLong` on background thread (no `looper.prepare`) and `ShowToast` lint error
* Fixed: Gradle build fixed, ommiting missing translations while assembling release
* Fixed: `lastLocation` in `LocationTracker` gets assigned, preventing duplicate location reports
* Fixed: Antenna Map Viewer does not crash any more after fixing StringIndexOutOfBoundsException
* Fixed: Simplified version display in About Fragment, improved `lint` and fixed `buildnumbers`
* Fixed: Ensured OpenCellID string consistency so that everyone know which database is meant

#### [15.11.2015 - WIP-Release v0.1.36-alpha-build-00](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.36-alpha-b00)

* Changed: **We're now back from a huge break and intend to improve our project in all areas!**
* Updated: Improved Japanese, French, Russian German and Czech translations, added Lithuanian
* Updated: Formatted both comments and code, working torwards an easier code structure
* Updated: RootShell library pushed to version 1.3, thanked [smarek](https://github.com/smarek) in `CREDITS` for saving us
* Removed: Unused imports, unused class-global variables and dual `view.findViewById` calls
* Removed: Unnecessary `return` call and unused `count` variable
* Added: New vibration options in new menu `NOTIFICATION SETTINGS` (see `Preferences`)
* Fixed: Phone will no longer vibrate every few seconds on status changes (set it yourself)
* Fixed: Issue where `getSelectedItem()` was called in `doInBackground`
* Fixed: Handled unchecked type of `getSelectedItem()` return
* Fixed: Avoided NPE on `result.close();`

#### [20.09.2015 - WIP-Release v0.1.35-alpha-build-00](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.35-alpha-b00)

* Changed: Improved code quality and better error handling
* Added: Animated updates on "Phone/SIM Details" page
* Fixed: Resolved many security and performance issues [[#613](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/613)]

#### 16.09.2015 - WIP-Internal v0.1.33-alpha-build-01

* Updated: Old formatting updates for cleaner code structure
* Added: Database support for BtsLoc table in `aimsicd.db`
* Added: Translations for Japanese, Albanian, Spanish and Polish

#### [09.08.2015 - WIP-Release v0.1.34-alpha-build-00](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.34-alpha-b00)

* Updated: Finally found a **WHISTLEBLOWER**, removed position from [SEEKING](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/HEAD/CONTRIBUTING.md#seeking) ;-)
* Added: New language translation into Dutch

---

#### 06.08.2015 - WIP-Internal v0.1.33-alpha-build-02

* Updated: Cleaned up marker info layout (needs more items and work to be done)

---

#### 05.08.2015 - WIP-Internal v0.1.33-alpha-build-01

* Updated: `SmsDetector` is now using `toEventLog()`, fixed typo in `toEventlog`
* Added: Simplified placeholder for `insertEventLog` in `AIMSICDDbAdapter.java`
* Moved: Vibration code now resides in `toEventLog` for better maintainance

---

#### [02.08.2015 - WIP-Release v0.1.33-alpha-build-00](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.33-alpha-b00)

* Updated: Minor formatting of `dbe_import_items.xml` and `detection_sms_db_listview.xml`
* Updated: Improved `.gitignore` to ignore Android Studio Navigation editor temp files
* Updated: Improved Japanese and German translations, comments for `Helpers.java`
* Updated: Refactored string for Type-0 SMS in `aimsicd.db` to improve its detection
* Added: Implemented `rej_cause` into `DBe_import` and updated existing comments 
* Added: ProgressBar and updated SQL for `DBcheck()` in `AIMSICDDbAdapter.java`
* Fixed: Made ProgressBar in `AIMSICDDbAdapter.java` and logs work again

---

#### 01.08.2015 - WIP-Internal v0.1.32-alpha-build-21

* Reverted: Removed code for rechecking cell after OCID download after failed tests
* Updated: Refreshed Inflater data and comments in `DbViewerFragment.java`
* Fixed: Re-implemented possibility to delete and reset the internal database
* Fixed: Corrected typos in language source file `translatable_strings.xml`
* Fixed: Improved comments and `cleanseCellTable` in `CellTracker.java`
* Fixed: Corrected many layout Issues of XML for `DbViewerFragment.java`

---

#### 31.07.2015 - WIP-Internal v0.1.32-alpha-build-20

* Updated: Improved formatting of `AIMSICD.java`, `AIMSICDDbAdapter.java` and `Cell.java`
* Updated: Cleanup of `CellTracker.java`, consider removing old FemtoCell detection code
* Added: Implemented code for rechecking cell after OCID download (needs intensive testing)

---

#### 31.07.2015 - WIP-Internal v0.1.32-alpha-build-19

* Removed: Purged `CID -1` from EventLog, updated comments in `AIMSICDDbAdapter.java`
* Added: New restart of `AIMSICDDbAdapter` after deleting the internal database
* Fixed: Ordered navigation drawer and removed unused imports and attributes

---

#### 30.07.2015 - WIP-Internal v0.1.32-alpha-build-18

* Added: Vibration on no nc_list detection as well as many comments and formatting

---

#### 30.07.2015 - WIP-Internal v0.1.32-alpha-build-17

* Added: New `insertEventLog` query to prevent duplicates in the EvenLog table

---

#### 30.07.2015 - WIP-Internal v0.1.32-alpha-build-16

* Updated: Refreshed comments and fixed a few minor typos in several source files

---

#### 30.07.2015 - WIP-Internal v0.1.32-alpha-build-15

* Changed: Placed `DF_id` and `DF_desc` on separate lines for easier log reading
* Fixed: Refreshed EventLog DBE and formatting to use standard TableLayout
* Fixed: Reformatted `DBTableColumnIds.java` and `DbViewerFragment.java`

---

#### 30.07.2015 - WIP-Internal v0.1.32-alpha-build-14

* Fixed: Corrected several DBV Issues, removed old unused and commented out code

---

#### 30.07.2015 - WIP-Internal v0.1.32-alpha-build-13

* Removed: Purged ProgressBar calls when not visible (e.g. reading/writing CSVs)
* Fixed: Ordered navigation drawer and removed unused imports and attributes
* Fixed: Corrected defaultlocations layout and fixed BTS Measurements in DBV
* Fixed: Corrected typo of `T3212` incorrectly labeled as `T3213`

---

#### 29.07.2015 - WIP-Internal v0.1.32-alpha-build-12

* Changed: 2nd attempt to fix FC in DBV by commenting out `bb_power` in all files
* Updated: Notes and comments in `AIMSICDDbAdapter.java` have been renewed
* Added: Clarified difference between CellId and CID in `AIMSICDDbAdapter.java`
* Fixed: Corrected build error in `BtsMeasureCardInflater.java`
* Fixed: Corrected Log exceptions to use `.toString()`

---

#### 27.07.2015 - WIP-Internal v0.1.32-alpha-build-11

* Fixed: Corrected BTS Measurements and defaultlocations DBV layout 

---

#### 27.07.2015 - WIP-Internal v0.1.32-alpha-build-10

* Changed: Failed attempt to add "item" number to table layout
* Updated: Comments added, updated and removed in several files
* Fixed: Now showing `Unique BTS Data` items as intended in DBV
* Fixed: Corrected typo of `T3212` incorrectly labeled as `T3213`

---

#### 26.07.2015 - WIP-Internal v0.1.32-alpha-build-09

* Updated: Improved Japanese and Czech language translations (needs contributors)
* Fixed: OCID download now respects the filter for `MCC`, `MNC` and `LAC` again
* Fixed: Corrected spelling error to the correct display `DOWNLOAD_LOCAL_BTS_DATA`

---

#### 23.07.2015 - WIP-Internal v0.1.32-alpha-build-08

* Updated: Improved German language translations
* Added: New `DeviceApi18.java` to counteract wrong API for WCDMA cell info
* Fixed: API build versions issues for network related items has been resolved
* Fixed: Layout problems with new DBV xml style sheet have been resolved

---

#### 21.07.2015 - WIP-Internal v0.1.32-alpha-build-07

* Removed: Purged outdated `OCIDResponse.java` since no longer needed
* Changed: Attempt to reduze the font size in the DB viewer for the `DBe_import`
* Changed: Restructured `dbe_import_items.xml` for easier readbility to our users
* Changed: New color scheme within `dbe_import_items.xml` for cleaner overview
* Changed: Reversed logic code for `isExact`, changable on where data is imported
* Updated: Comments in `DBTableColumnIds.java` and `SignalStrengthTracker.java`
* Updated: TAGs and comments in `SmsDetector.java` are now reflecting current state
* Updated: Cleaned `DbeImportCardInflater.java` and `detection_sms_db_listview.xml`
* Updated: Improvement to SMS detection of WAP Push SMS messages with a new string
* Updated: TAGs in `AIMSICDDbAdapter.java` and formatting of `DbViewerFragment.java`
* Added: New server response codes in OpenCellIdActivity.java, removed old comments
* Added: New EventLog for changing `LAC` in GSM case (only had it in CDMA until now)
* Added: Minor comments in `DbViewerFragment.java` to clarify even more table data
* Added: More notes and TAGs to work on for `AimsicdService.java` and `Cell.java`
* Fixed: Downgrade of `targetSdkVersion` to `19` fixing whitish Icons on Android 5+
* Fixed: Repaired `CellTracker` polling neighbouring cells on unsupported phones
* Fixed: Repaired switched `LAC`/`CID` vs data labels of DBV in `DBe_import` table
* Fixed: Better comments, spellings and formatting in `AIMSICDDbAdapter.java`

#### 17.07.2015 - WIP-Internal v0.1.32-alpha-build-06

* Moved: `requestNewOCIDKey()` from `Celltracker` to `OpenCellIdActivity`
* Fixed: Repaired OCID crash when getting key, better DBV layout for `DBe_ipmort`

---

#### 17.07.2015 - WIP-Internal v0.1.32-alpha-build-04

* Updated: Minor changes to correct value for `avg_range` in `AIMSICDDbAdapter.java`
* Updated: from `DBE_UPLOAD_REQUEST` to `RESTORE_DATABASE` in `RequestTask.java`
* Added: New string for database restoration popup added in `RequestTask.java`
* Fixed: `CellTracker` and `RequestTask` are now works along with our new database
* Fixed: Comments in `CellTracker` have been answered to clarify current functionality

---

#### 16.07.2015 - WIP-Internal v0.1.32-alpha-build-03

* Updated: Shuffling, commenting and formatting of `AIMSICDDbAdapter.java`

---

#### 14.07.2015 - WIP-Internal v0.1.32-alpha-build-02

* Fixed: AIMSICD does not crash anymore when downloading OpenCellID data

---

#### 14.07.2015 - WIP-Internal v0.1.32-alpha-build-01

* Changed: **[MAJOR DATABASE OVERHAUL](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/215)** - huge THANKS flies out to [banjaxbanjo](https://github.com/banjaxbanjo)!
* Changed: Our new database is now pre-compiled, faster, better and uses correct tables
* Updated: Improved French and Polish, minor string changes for Database Viewer labels

---

#### [12.07.2015 - WIP-Release v0.1.32-alpha-build-00](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.32-alpha-b00)

* Updated: Enhanced `AIMSICD.java` for another attempt to fix AIMSICD not closing
* Updated: French, Polish and Russian translations, added: Czech and Swedish
* Updated: More updates of MWI detection strings with fixes to avoid failing build
* Added: Small note in `CONTRIBUTING.md` to actually *finish* the started translations
* Fixed: Reverted lacells pull request because it caused blockings and exceptions

---

#### 07.07.2015 - WIP-Internal v0.1.31-alpha-build-05

* Changed: Switched Coverity Scan analysis to `master` to avoid Travis-CI failures
* Updated: Improved French, German and Polish translations (needs more translators)
* Fixed: Corrected Type-0 silent SMS popup display and tried to fix app not closing

---

#### 07.07.2015 - WIP-Internal v0.1.31-alpha-build-04

* Updated: German translations have been improved upon changes in source strings
* Fixed: Corrected MWI code and removed unnecessary spaces from detection popup

---

#### 07.07.2015 - WIP-Internal v0.1.31-alpha-build-03

* Updated: German translations have been improved upon changes in source strings
* Removed: Purged E-Mail address to send logfiles to - a better way will come soon!
* Removed: Purged invitation to send logfiles for every single detection event

---

#### 07.07.2015 - WIP-Internal v0.1.31-alpha-build-02

* Added: New table for detection tests of Type-0 silent SMS and MWI in [Special SMS](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Special-SMS)
* Fixed: Declared Message Waiting Indicator strings correctly in our detection code

---

#### 07.07.2015 - WIP-Internal v0.1.31-alpha-build-01

* Updated: Enhanced Type-0 silent SMS detection with main buffer to logcat scraper
* Updated: Translations via Weblate are now finally getting pulled in automatically
* Updated: Thanked [thechangelog](https://github.com/thechangelog) in our `CREDITS` for their public announcements
* Added: Translations into English, German, French, Polish, Japanese and Russian

---

#### [05.07.2015 - WIP-Release v0.1.31-alpha-build-00](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.31-alpha-b00)

* Removed: Commented out unused imports and re-ordered variable declarations
* Updated: Shortened badge of `Development Status` in `README` for better display
* Added: More clarifying code comments in `CellTracker.java`

#### 05.07.2015 - WIP-Internal v0.1.30-alpha-build-16

* Fixed: Cleanup of timestamp code in `MiscUtils.java`
* Fixed: AIMSICD Status-Icons are now shown correctly in colored style on Android 5+


---

#### 04.07.2015 - WIP-Internal v0.1.30-alpha-build-14

* Added: New Log items for MCC and MNC parameters when downloading OCID for debugging
* Fixed: Reformatted 23 char limitation of Log TAG with our standard TAG, mTAG + "text"
* Fixed: Corrected MiscUtils TAGs and nulls in CellTracker and improved some formatting
* Fixed: Silenced even more XPrivacy logcat spam in DebugLogs

---

#### 03.07.2015 - WIP-Internal v0.1.30-alpha-build-13

* Fixed: Comments in `RequestTask.java` have been fixed to reflect code changes
* Fixed: Silenced some spammy XPrivacy items using `XPrivacy/XRuntime:S Xposed:S`

---

#### 03.07.2015 - WIP-Internal v0.1.30-alpha-build-12

* Added: Weblate translations badge added to our `README` for people to see progress
* Fixed: AIMSICD should now start again properly on previously complaining devices

---

#### 03.07.2015 - WIP-Internal v0.1.30-alpha-build-11

* Updated: `CREDITS` now reflect the latest awesome additions by our new developers
* Fixed: Small string fixes and translation improvements to move our new [Weblate](https://hosted.weblate.org/projects/aimsicd/strings/).

---

#### 03.07.2015 - WIP-Internal v0.1.30-alpha-build-10

* Fixed: Now truncating measured and used Lat/Lon GPS coordinates in `LocationTracker`

---

#### 27.06.2015 - WIP-Internal v0.1.30-alpha-build-09

* Changed: Timeout value in `RequestTask.java` has been increased to 80 seconds
* Fixed: Repaired OCIDCSV parsing which obviously temporarily broke during development

---

#### 27.06.2015 - WIP-Internal v0.1.30-alpha-build-08

* Added: Buildozer Buildnumber to has been added to About View for development builds

---

#### 26.06.2015 - WIP-Internal v0.1.30-alpha-build-07

* Changed: mTAG has been changed as requested to `SamsungMulticlientRilExecutor`
* Added: Small warning within code to not remove commented out stuff without prior asking

---

#### 25.06.2015 - WIP-Internal v0.1.30-alpha-build-06

* Updated: Log calls updated to common TAGs, added doublepoints and removed whitespaces

---

#### 25.06.2015 - WIP-Internal v0.1.30-alpha-build-05

* Changed: Customized build script in `.travis.yml` to override CoverityScan limits
* Changed: Made toast property static and non-final, splitting toast creation for `msgShort`
* Removed: Commented out `LeakCanary` until this libray has reived some code improvemens
* Fixed: Toasts are now displayed in the ccorrect position and duration to really read them

---

#### 24.06.2015 - WIP-Internal v0.1.30-alpha-build-04

* Added: Toasts are now being cancelled on new toast to prevent overlapping of toasts

---

#### 24.06.2015 - WIP-Internal v0.1.30-alpha-build-03

* Fixed: Info box toasts are now shown longer by adding a new singleton toaster

---

#### 23.06.2015 - WIP-Internal v0.1.30-alpha-build-02

* Changed: Context property changed to `appContext` in `DrawerMenuAdapter.java`
* Removed: Purged `this` prefix of `appContext` as it is not needed anymore

---

#### 23.06.2015 - WIP-Internal v0.1.30-alpha-build-01

* Changed: Help toast has been changed to long toast in `DrawerMenuAdapter.java`

---

#### [21.06.2015 - WIP-Release v0.1.30-alpha-build-00](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.30-alpha-b00)

* Updated: Refactored OpenCellId activity for better code quality and error handling
* Updated: A few more wording improvements for our `README` and additions in `CREDITS`
* Changed: Removed references and links to organizations not supporting our project
* Changed: Improved `LOGTAG` to use proper class name `SmsDetectionDbHelper`
* Added: Refresh Rate is now shown on menu entry in `Preferences` without opening it
* Fixed: Made buttons in `About` menu reappear and fixed code for shrinked buttons
* Fixed: Resolved reappearance of too many unnecessary database open/close operations

---

#### 17.06.2015 - WIP-Internal v0.1.29-alpha-build-06

* Updated: Slight translation updates of strings due to previous menu changes
* Changed: Removed unused imports and shrinked some buttons in `About` menu
* Fixed: Resolved UI Issue in `Database Viewer` and corrected button link naming

---

#### 17.06.2015 - WIP-Internal v0.1.29-alpha-build-05

* Changed: Removed old copyright and code of unused Femtocell toggle button
* Added: New `logcatTimeStampParser` for friendly timestamp from a logcat string
* Fixed: Endlessly receiving the same detected SMS has finally come to an end
* Fixed: Exception on UI and database has been fixed (possibly just tentative)

---

#### 16.06.2015 - WIP-Internal v0.1.29-alpha-build-04

* Updated: Better RIL/API support with refreshed `ServiceMode` parser
* Updated: Increased timeout of OCID download to avoid retrieval errors
* Added: New feature to read OCID data from generated [lacells.db](https://github.com/n76/Local-GSM-Backend) as well
* Fixed: Resolved WIN DEATH when using Advanced User Preferences
* Fixed: Startup crash fixed with `Lat` and `Lng` in `AIMSICDDbAdapter.java`

--- 

#### 12.06.2015 - WIP-Internal v0.1.29-alpha-build-03

* Changed: Moved hardcoding of OCDB download path to static variable
* Updated: Refactored OCID CSV parsing and truncated GPS coordinates

---

#### 09.06.2015 - WIP-Internal v0.1.29-alpha-build-02

* Updated: Improved menu translations in `Navigation Drawer` for better understanding

---

#### 08.06.2015 - WIP-Internal v0.1.29-alpha-build-01

* Removed: Purged unused imports and unnecessary `toString` calls
* Removed: Purged public modifier from interface methods
* Changed: Switched CoverityScan analysis to branch `development` to analyze current code
* Changed: Replaced deprecated `GridMarkerClusterer` with `RadiusMarkerClusterer`
* Changed: Closed some database cursors and replaced `dp` with `sp` for TextViews
* Updated: License header has been unified across all source files to ensure proper GPL
* Updated: Refactored `AIMSICDDbAdapter::DbHelper#onCreate` for easier reading
* Updated: Refactored redundant parsing from get OCID API request
* Added: Default `locale` has been implemented into `SimpleDateFormat` constructor
* Added: Now using parent view instead of null when inflating layouts
* Added: French translations as well as `leakcanary` analysis for detecting leaks
* Fixed: Minor translation and punctuation improvements in several translation string files
* Fixed: Corrected filename of CSV loaded in into `AIMSICDDbAdapter#updateOpenCellID`

---

#### [31.05.2015 - WIP-Release v0.1.29-alpha-build-00](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.29-alpha-b00)

* Added: New WAP Push Detection and corresponding popup as well as custom strings
* Added: All detection strings will now be added to and loaded from `det_strings.json`
* Fixed: Small typos in German translation removed, shortened OCID notifications

---

#### 28.05.2015 - WIP-Internal v0.1.28-alpha-build-03

* Changed: Many German translation improvements for much easier understanding
* Removed: SMS Detection `try` has been purged since it was not needed anymore
* Updated: Improvements across all translation files for better display of notifications
* Updated: SMS Detection improved and moved some functions in `CustomPopUp.java`
* Updated: Moved some Detection functions to `MiscUtils` and added minor comments
* Moved: Untranslatable strings have been removed from all existing translation files
* Fixed: Padding issue displaying detected SMS only partially has been resolved

---

#### 27.05.2015 - WIP-Internal v0.1.28-alpha-build-02

* Added: Translations into German have been started, many string improvements

---

#### 25.05.2015 - WIP-Internal v0.1.28-alpha-build-01

* Added: New SMS Detection to detect tracking via silent SMS and silent Voice ;-)
* Added: New WIKI entry on how to test your own detection strings in [Special SMS](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Special-SMS)

---

#### [24.05.2015 - WIP-Release v0.1.28-alpha-build-00](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.28-alpha-b00)

* Updated: `CREDITS` now reflect the latest contributions by our developers
* Changed: Tables in the `Database Viewer` will now be loaded automatically
* Added: New overlay BTS map legend now clarifies meanings of the map pins

---

#### 22.05.2015 - WIP-Internal v0.1.27-alpha-build-03

* Moved: App data as been moved to new location (will update automatically)

---

#### 21.05.2015 - WIP-Internal v0.1.27-alpha-build-02

* Fixed: Strange NPE related to `TelephonyManager.getNeighboringCellInfo()`

---

#### 15.05.2015 - WIP-Internal v0.1.27-alpha-build-01

* Changed: Links within our app have been changed to permanent `HEAD` links
* Added: Donations to encourage developers are now possible via [Bountysource](https://www.bountysource.com/teams/android-imsi-catcher-detector/issues)

---

#### [10.05.2015 - WIP-Release v0.1.27-alpha-build-00](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.27-alpha-b00)

* Removed: Buggy `Toast Extender` for making toasts last longer than 3.5 seconds
* Changed: Releases will now be published every Sunday to ease developers lifes
* Moved: `DATABASE.md` has been moved into section `Technical Details` in WIKI
* Fixed: Shout-out to @DimaKoz who cleared the mess done with `Toast Extender`
* Fixed: Huge thanks to @banjaxbanjo for correctly implementing toast expansion

---

#### 06.05.2015 - WIP-Internal v0.1.26-alpha-build-04

* Added: Implemented ability to erase the database through `Navigation Drawer`

---

#### 05.05.2015 - WIP-Internal v0.1.26-alpha-build-03

* Updated: Tried to increase the version of the support library (now reverted)
* Updated: Synced `buildToolsVersion` with dependencies for better support
* Updated: Unified and resized copyright header within several source files
* Added: Green checkmark now appears when pressing the toggle buttons
* Added: Included AppCompat library in attempt to heal broken Buildozer builds
* Fixed: Purged bug with doubling (and tripling, etc.) markers on the map

---

#### 02.05.2015 - WIP-Internal v0.1.26-alpha-build-02

* Updated: More improvements of the Russian translations
* Updated: Markers of cell towers are now drawn as immediately as possible
* Added: Progress spinner shows on OCID download in `Antenna Map Viewer`
* Fixed: Memory leaks and other warnings were partially eliminated

---

#### 02.05.2015 - WIP-Internal v0.1.26-alpha-build-01

* Updated: Translation into Russian has been improved

---

#### [01.05.2015 - WIP-Release v0.1.26-alpha-build-00](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.26-alpha-b00)

* Updated: Added [agilob](https://github.com/agilob) and [DimaKoz](https://github.com/DimaKoz) into our `CREDITS` for their awesome work
* Updated: Improved descriptions of menu and removed unused resources and a dot
* Updated: Translation instructions have been updated to reflect the latest procedure
* Changed: Intensified warning for dropped support for logfiles without a description
* Added: Translation into Russian has been started, improved Polish translations
* Fixed: Nuked error in Database Viewer getting data in languages other than English
* Fixed: Corrected spelling of string name and added small screen improvements

---

#### 27.04.2015 - WIP-Internal v0.1.25-alpha-build-47

* Changed: Transformed more strings for translation, some cosmetic changes in java code
* Changed: Merged `arrays.xml` to `strings.xml` (in `values-XX`) for easier translations
* Changed: Merged helpers to `strings.xml` and improved polish translation even more
* Changed: Files of resources were renamed properly, 'about' fragment has been changed
* Removed: Deleted unused imports (done automatically by Android Studio) and resources
* Added: Task `lintVitalRelease` has been added into `.travis.yml` for clean checks

---

#### 26.04.2015 - WIP-Internal v0.1.25-alpha-build-46

* Updated: Some updates to the translation instructions, a few more changes to come
* Updated: Version of library `slf4j-android` as well as `README` have been updated
* Changed: Re-ordered menu to a preferred layout in conjunction with UI/UX redesign
* Added: New Info-Buttons for getting help from within our app and without internet
* Added: Component `extra-android-m2repository` has been added to `.travis.yml`
* Fixed: NPE on `tm.getNeighboringCellInfo()` when neighboring cell is `null`
* Fixed: Progress bar is now showing correctly again and does not overlap things
* Fixed: Errors with strings after running `lintVitalRelease` in Gradle were fixed

---

#### 23.04.2015 - WIP-Internal v0.1.25-alpha-build-45

* Added: Translation instructions have been added and updated in `CONTRIBUTING.md`
* Fixed: Clarified examples in EventLog and DB Viewer with red `EXAMPLE!` warning

---

#### 23.04.2015 - WIP-Internal v0.1.25-alpha-build-44

* Changed: Moved strings from a few classes and from `array.xml` to `strings.xml`
* Added: Enabled app to be translatable and start up in users native system language
* Added: Polish translation and empty `values-de` folder for German language files
* Fixed: Re-labelled BTS pin-info samples as "0" when being empty (BLUE)

---

#### 21.04.2015 - WIP-Internal v0.1.25-alpha-build-43

* Updated: Version of the `osmbonuspack` library for maps has been updated
* Fixed: Ability of caching the tiles has been switched on, fixed slow speed

---

#### 21.04.2015 - WIP-Internal v0.1.25-alpha-build-42

* Updated: Compressed all Images and Icons to make our app even smaller
* Changed: Moved progress bar to overlay content and made it visible for debugging
* Added:  Included methods to show and the hide progress bar and added test

---

#### 18.04.2015 - WIP-Internal v0.1.25-alpha-build-41

* Updated: `CREDITS` have been updated with the wonderful BFG Repo-Cleaner
* Removed: Purged `NeighboringCellMonitor.java` since we don't need it anymore
* Changed: Moved check LAC code into `compareLac()` to check on every cell change
* Changed: Moved check from `updateNeighbouringCells` to `checkForNeighbourCells()`
* Fixed: Neighbouring cell list is now also updated outside the MapViewer

---

#### 17.04.2015 - WIP-Internal v0.1.25-alpha-build-40

* Updated: We've completely cleaned our GitHub repo. **Please re-fork us now!**
* Added: Permission `INTERACT_ACROSS_USERS_FULL` to allow building on phones
* Added: Re-created branch `development` to finally continue work on real Issues

---

#### 17.04.2015 - WIP-Internal v0.1.25-alpha-build-39

Updated: Renewed `buildToolsVersion` across the project to keep them current

---

#### 17.04.2015 - WIP-Internal v0.1.25-alpha-build-38

* Changed: Replaced `.jar` files by gradle dependencies where possible
* Changed: Simplified `README` and made everything much developer-friendly
* Changed: Sending debugging logs via our app, now **requires** a description
* Changed: Moved all large folders into our [SpiderOak storage](https://spideroak.com/browse/share/AIMSICD/GitHub) to clean up
* Added: New [Style Guide](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Style-Guide) to unify look and feel of our app in the near future
* Added: New section `SEEKING` in `CONTRIBUTING.md` for special positions
* Added: Warning banner to make people aware of possible false alarms

---

#### 28.03.2015 - WIP-Internal v0.1.25-alpha-build-37

* Updated: Reformatted `CREDITS` to look much better within our `CreditsRoll`
* Fixed: Neighbouring Cell List should now get updated outside the MapViewer

---

#### 20.03.2015 - WIP-Internal v0.1.25-alpha-build-36

* Changed: Simplified `CONTRIBUTING.md` for a much easier entry into development
* Added: Enabled automatic builds of internal testing APKs via Buildozer
* Added: New Custom Pop Class in `AndroidManifest.xml` for popups in About-Tab
* Added: Status-Icons in About-Tab are now clickable and show Detailed Explanations

---

#### 13.03.2015 - [WIP-Release v0.1.25-alpha-build-35](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.25-alpha-b35)

* Changed: Tried to fix the progress bar in the `RequestTask` Download Request
* Updated: Polished CellTracker from junk comments, added TAGs to Helpers and Device
* Added: New tab strip across the view pager to make it obvious that it can be swiped
* Added: New CreditsRoll with awesome Star Wars theme in the About-Menu of our AIMSICD
* Fixed: NC detection logic and added more Logs to be removed or verbose, once working
* Fixed: Better log function in `CellTracker.java` and corrected TAG in `DeviceAPI17`

---

#### 05.03.2015 - [WIP-Release v0.1.25-alpha-build-34](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.25-alpha-b34)

* Updated: `SCREENSHOTS` have been updated to reflect the latest changes of UI/UX
* Added: Some more small comments for better understanding of the added `TinyDB`
* Fixed: Inceremented Database Version to avoid crashes due to changed DB tables

---

#### 03.03.2015 - WIP-Internal v0.1.25-alpha-build-33

* Updated: `CREDITS` now do also reflect the current list of thanked people
* Changed: Replaced dirty `SharedPreferences` code with neat TinyDB one-liners
* Added: Placeholder for the NC Detection (code is commented out, needs testing)
* Added: `DBE_UPLOAD_REQUEST` Upload result Toast msg as feedback for uploading
* Fixed: Spelling mistakes have been corrected, code comments have been updated

---

#### 03.03.2015 - WIP-Internal v0.1.25-alpha-build-32

* Changed: Replaced get/setProp calls with using the simple TinyDB implementation
* Changed: Alert tag changed to `ALERT: Connected to unknown CID not in DBe_import:`
* Added: Link to our WIKI regarding the usage of so many deep-core permissions

---

#### 03.03.2015 - WIP-Internal v0.1.25-alpha-build-31

* Updated: List of team members has been reviewed to reflect current changes
* Updated: Referenced X-Cell Technologies within our own Glossary of Terms

---

#### 16.02.2015 - WIP-Internal v0.1.25-alpha-build-30

* Changed: Cleanup of various old junk and unneeded code comments, silenced spam logs on HTC
* Changed: ATCoP timout values from the selector have been adjusted to now also be 10 min
* Removed: ATCoP shell command support has been purged - SecUpwN lost ALL DATA of his phone
* Added: New TinyDB (courtesy of @kcochibili) for easy use of shared & persistent variables
* Added: Logging if no data available for upload, removed `OCID_UPLOAD_PREF` from `Preferences`
* Added: Button to upload local BTS Data to OCID and fix issues from former commits
* Added: Code for future toggle button for radio buffer in the Debug Logger logcat buffer
* Added: Easily selectable serial device list is now available in AT Command Interface
* Fixed: AT Command Processor Interface has finally been fixed - THANKS to @scintill!
* Fixed: False yellow flag has now been fixed by waiting until OCID DB has been downloaded
* Fixed: Ugly setprop log/toast message appearing every 10 seconds has now been resolved
* Fixed: Trivial code shortening in `Helpers` as well as corrected spelling of some items

---

#### 13.02.2015 - WIP-Internal v0.1.25-alpha-build-29

* Changed: Debug Log to include radio, removed `-d` flag, increased lines to 500
* Changed: Attempted fix on the AT Command fragment which suffers a freezer bug
* Changed: Attempted fix to false positives of silent SMS detection in `SmsReceiver`
* Changed: Tightened the BBOX from 5 to 2 Km and added it to the Logging as well
* Changed: Adjusted `SignalStrengthTracker` formatting and changed a few comments
* Changed: Reformatted code of SignalStrength table and added a few more comments
* Changed: Cleaned up some code and TAGs as well as ProgressBar in `DbAdapter`
* Removed: Time from logcat in DebugLogs activity has been purged for cleaner view
* Removed: The spooky Google permission `WRITE_GSERVICES` has been completely purged
* Added: Interface to `AtCommandFragment` (but now AT Interface crashes constantly)
* Added: Additional case in `TelephonyManager`: `PHONE_TYPE_NONE` and `PHONE_TYPE_SIP`
* Added: `onPreExecute()` and super keywords & Logs and posssible ProgressBar fix
* Added: Place holders for TP-MMS/SRI and `rej_cause` place holders to `DBe_import`
* Added: Dummy entry has been written to SilentSMS table, UpperCase of SQL statements
* Added: Comments in `CellTracker` for `LISTEN_CALL_STATE` and `LISTEN_SERVICE_STATE`
* Added: Tried to add a working `setSystemProp` by reflection (still not working)
* Added: `setMaxZoomLevel(19)` to OSM in hoping to fix zoom level
* Added: A few more pictures of potential IMSI-Catchers that we've seen in action
* Added: New blog post on Gun.io to introduce our project to more Freelancers
* Fixed: The null deref on unrooted-device exec failure has now been resolved
* Fixed: Shortened DB names and fixed Timestamp of our new EventLog table
* Fixed: Network Information is not shown empty any longer on certain conditions

---

#### 09.02.2015 - WIP-Internal v0.1.25-alpha-build-28

* Changed: Save EventLog action now takes place in aa more convenient location
* Added: New EventLog table with working DB View of EL-Table and Export of .csv
* Added: New file `EventLogItemData.java` and some editing to the table styling
* Added: Debugging code to dump full SMS in PDU format and easier string parsing

---

#### 04.02.2015 - WIP-Internal v0.1.25-alpha-build-27

* Changed: Logcat entry of CID alert moved from `AIMSICDDbAdapter` to `CellTracker`
* Added: Some pseudo code to start the detection of checking the BTS for the NC List
* Added: Code comments mentioning possible bug on CDMA SID/MNC Info
* Added: Missing SMS/MMS/WAP etc. Android permissions have now been added
* Added: Non-public (3rd party) Android permissions have been added as well
* Added: OEM / Samsung related permissions and comments on `SmsReceiver` permissions
* Added: Some minor code cosmetics making everything more understandable
* Added: Skipping the re-import of same CIDs into `DBe_import`
* Fixed: Missing bonuspack_bubble Error in logcat has been resolved

---

#### 29.01.2015 - WIP-Internal v0.1.25-alpha-build-26

* Updated: Completed `amisicd.db` backup to `aimsicd_dump.db` on the SD card
* Updated: `CONTRIBUTING.md` is now forcing to use our `development` branch
* Removed: Unused import statements have been purged to clean up the code
* Added: `checkDBe()` added for removing bad cells from OCID import table
* Added: Several DBe LAC/CID consistency checks via the new `checkDBe()`
* Added: Boolean (true/false) system property to show status of OCID download
* Added: New columns in `DBe_import` table: `avg_range`, `isGPSexact` and `Type`
* Added: German public notice to be displayed on all black boards in town

---

#### 28.01.2015 - WIP-Internal v0.1.25-alpha-build-25

* News-Alert: **We are improving things - please work on branch `Development`!**
* Added: `dumpDB()` to the DB backup function to make amonolithic DB dump file
* Added: Info message in DebugLogs and attempted to include `getprops`
* Added: A few more extra comments for developers to check out aand improve
* Fixed: Refactored and cleaned copied `getprop` code for debug the E-Mails

---

#### 27.01.2015 - WIP-Internal v0.1.25-alpha-build-24

* Changed: Reverted `]` to `|` in `getNetworkTypeName()`
* Changed: Some (code) formatting of `marker_info_window` items
* Removed: Purged black background line in "Neighboring Cells" window
* Removed: Commented out `MCC/MNC` in pin info detail to be replaced by ProviderCode
* Added: HEX value has been added to `CID` in Cell Strength table (DB Viewer)
* Added: Re-introduced the OCID `MNC` restriction on CSV data
* Added: EventLog has been added to selector in strings - please to improve this!
* Fixed: Name typo in Cell Tracker (`FEMTO_DECTECTION`) has been resolved

---

#### 23.01.2015 - WIP-Internal v0.1.25-alpha-build-23

* Changed: Moved service and receivers to the bottom to make file easier to maintain
* Changed: Error messages are now using `msgLong` instead of `msgShort`
* Changed: Reduced OCID data radius from 10 to 5 Km, still saturating DB limit
* Removed: Commented out `MCC` and `MNC` from Map Viewer pin info XML
* Removed: Commented out discovered bug prone to the EventLog table code
* Added: Exception handling for devices where certain providers may not be available
* Added: Handling of rotation for all activities, and removed the forced portrait mode
* Added: Attemppt to add EventLog `eventlog_items.xml` and `EvenLogCardInflater.java`
* Added: More code comments and placeholders for developers to continue work upon

---

#### 22.01.2015 - WIP-Internal v0.1.25-alpha-build-22

* Changed: `setLocationUpdateMinTime` in OpenStreetMap from 60 to 10 s
* Changed: `setLocationUpdateMinDistance` in OSM from 1000 to 100 meters
* Added: Even more code comments, TODO's and code formatting to digg into

---

#### 22.01.2015 - WIP-Internal v0.1.25-alpha-build-21

* Improved: Teweaked some log messages for a better output and clearer evaluation
* Removed: Commented out "Upload" code in the OCID download request handler
* Changed: Task request strings from `OPEN_CELL_ID_REQUEST` to `DBE_DOWNLOAD_REQUEST`
* Added: Lots of code placeholders to import CSV columns from OCID data and display them
* Added: Created `aimsicd.sql` as new SQL to create all new tables in the new structure
* Added: Created `CreateDBe_import.sql` which will be used to import the new Databases
* Added: More comments and basic code reformat for Database Viewer to support new tables
* Fixed: Clarified "range" vs "samples" confusion in Database Viewer

---

#### 20.01.2015 - WIP-Internal v0.1.25-alpha-build-20

* Improved: Silenced spammy Samsung Galaxy class debug logcat entries from DebugLog
* Updated: Added `import-summary.txt` and `manifest-merger*.txt` to `.gitignore`
* Fixed: Finally solved `CellTracker.java` not stopping correctly when pressing `Quit`
* Fixed: Erased a typo in DebugLog and added even more comments to the current code

---

#### 19.01.2015 - WIP-Internal v0.1.25-alpha-build-19

* Changed: Reformatted a bunch of code for law and order
* Fixed: Test to fix OCID 503 error with static contect `private static Context context;`

---

#### 19.01.2015 - WIP-Internal v0.1.25-alpha-build-18

* Changed: `-v brief` to `-v time` to get timestamps for debugging time critical Issues
* Added: Implemented `*:v` to ensure we also get verbose messages for better debugging
* Added: Picture and link to disguised Cell Towers (just to make you aware that these exist)
* Added: Comments for detection color codes instead of `DANGER`, `ALARM`, etc.
* Fixed: Bug report E-Mail address was previously not automatically added (in K-9 Mail)
* Fixed: Lack of notification and tickerText when cellID not existing in the OCID database
* Fixed: Tried to fix too chatty DebugLog

---

#### 17.01.2015 - WIP-Internal v0.1.25-alpha-build-17

* Updated: Added @d-mariano to our `CREDITS` because he has been of awesome help
* Moved: Contact details of the team members have been moved into [WIKI](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Contact) for better editing
* Fixed: Minor typos in `CHANGELOG.md` and `CONTRIBUTING.md` have been removed
* Fixed: Persistent Service does not have to be checked and unchecked after fresh install

---

#### 17.01.2015 - WIP-Internal v0.1.25-alpha-build-16

* Improved: Log strings for LAC detection and code layout for better visibility
* Added: Many comments for the pending DB update which will help understanding it
* Fixed: String changes in strings.xml causing DB Viewer failure have been reverted
* Fixed: Missing BTS Issue due to OCID download limitation of 1000 has been resolved

---

#### 14.01.2015 - [WIP-Release v0.1.25-alpha-build-15](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.25-alpha-b15)

* News-Alert: We have gone viral with our [official Twitter-Account](https://www.twitter.com/AIMSICD)! Follow and tweet about us!
* Changed: Color of text for credits of OpenStreetMaps changed within Antenna Map Viewer
* Removed: Deprecated Screenshots have been purged to clean up our repository
* Added: Leaked screenshots of IMSI-Catcher Software has been added to Glossary of Terms
* Added: Logcat-Alert when new Cell is not in OCID DB - which is likely an IMSI-Catcher

---

#### 12.01.2015 - WIP-Internal v0.1.25-alpha-build-14

* Removed: Purged unnecessary and misplaced folder containing older map pins
* Changed: Merged `SOURCES` into `CREDITS` to show it in our App in the future
* Updated: Some smaller changes made to descriptions in `about_fragment.xml`
* Added: We are now officially to be found on [Hackaday](https://hackaday.io/project/3824-android-imsi-catcher-detector), awaiting more developers
* Fixed: App should now not automatically restart once `Quit` has been pressed

---

#### 10.01.2015 - WIP-Internal v0.1.25-alpha-build-13

* Improved: AIMSICD now stores data straight in DB instead of waiting too long 
* Updated: Some more string changes as well as a hint on Insecure Service Area
* Changed: Better zooming available with max zoom level `3` in Antenna Map Viewer
* Added: osmbonuspack dependency to interact with OpenStreetMap data in our App
* Added: Basic clustering implementation for correct sizing of the new map pins
* Added: Scale bar and compass have been implemented into the Antenna Map Viewer
* Added: Copyright for OpenStreetMaps and THANK YOU to @ziem added to `CREDITS`
* Fixed: App now really quits all activities when pressing the `Quit` button
* Fixed: Having to check and uncheck Persistent Service in Preferences is solved
* Fixed: dBm values were considered positive when they were negative is solved
* Fixed: Many smaller fixes and enhancements for the `SignalStrengthTracker`

---

#### 08.01.2015 - WIP-Internal v0.1.25-alpha-build-12

* Removed: Button for Anonymous Donations has been removed as it is not needed
* Changed: Switched from RootTools to RootShell to slim down and refresh code
* Updated: Many smaller updates of strings for even more clearness on meanings
* Added: New map pins to reflect new connection defintions from the Map Viewer

---

#### 07.01.2015 - WIP-Internal v0.1.25-alpha-build-11

* Improved: Log View will be immediately cleared when pressing button
* Improved: Shorter time taken to plot OpenCellId markers on map
* Removed: Hybrid and Satellite Maps have been removed to avoid bugs
* Changed: Refactored code out of `MapViewerOsmDroid` for easier maintenance
* Added: More location providers added to improve location acquire time
* Added: `PC` and `MNC/MCC` zero-padding has been implemented


---

#### 07.01.2015 - WIP-Internal v0.1.25-alpha-build-10

* Improved: Checked App against spelling errors and also erase some
* Removed: Bad map options have been disabled by commenting out code
* Changed: Many text string changes for clarification and simplicity
* Changed: Signal strength and date strings changed in DBV for clarity
* Changed: LAC detection strings have also been simplified in the log
* Added: Placeholders for upcoming UI/UX redesign have been added

---

#### 06.01.2015 - [WIP-Release v0.1.25-alpha-build-9](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.25-alpha-b9)

* Improved: Better saving of timestamp for signal detection
* Changed: Re-adjusted some levels for Signal Strength Tracker
* Added: Implemented Database Viewer for measured Cell Signal Strength
* Added: Some more code documentation to understand the current changes
* Fixed: Cell signal strength should now be viewable in Database Viewer

---

#### 06.01.2015 - WIP-Internal v0.1.25-alpha-build-8

* Changed: Logging changed from changed `Log.i` to `Log.v` (4 rows deleted to avoid errors)
* Added: Logging for CellID to ChangingLAC, found new Cell that is not in OCID
* Added: Logging for Cell updating in local DB & Cell inserted into local DB
* Added: Logging for deleting Cell from local DB

---

#### 06.01.2015 - WIP-Internal v0.1.25-alpha-build-7

* News-Alert: It has been **proven** that IMSI-Catchers are currently being deployed and used **at all demonstrations** for and against PEGIDA in Germany to identify participants and spy on their mobile phones!
* Remember: If you're going to participate in **ANY** demonstration, **LEAVE YOUR PHONE AT HOME!**
* Improved: `PAPERS` moved to [SpiderOak](https://spideroak.com/browse/share/AIMSICD/GitHub/PAPERS/), repo cleaned to ~35 MB - please **refork** our project!
* Improved: After you have reforked our project, please follow our new instructions in [Building](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Building)
* Changed: Map pins are now round dots and use `msgLong` instead of `msgShort` for toaster message
* Changed: Persistent service is now disabled at startup by default to avoid dev annoyance
* Added: Minor code comments for easier development (Android Developers: submit your pull requests)

---

#### 01.01.2015 - [WIP-Release v0.1.25-alpha-build-6](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.25-alpha-b6)

* Improved: **HAPPY NEW YEAR EVERYONE!** We are going to change Release Cycles for you! ;-)
* Improved: Made `README` easier to read and updated links, hint me on further improvements!
* Changed: Reverted log level in `SamsungMulticlientRilExecutor` from `i` to `v`
* Updated: Gave proper credit in `CREDITS` to the latest awesome additions to our team
* Added: New directory for external pictures and pictures of photographed IMSI-Catchers
* Added: We now have a fresh [Glossary of Terms](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Glossary-of-Terms) to make it easier for you to contribute
* Fixed: Cell signal strength has been fixed to not always show 99db
* Fixed: `NullPointerException` when `getActionBar()` returns `null` has been purged

---

#### 29.12.2014 - WIP-Internal v0.1.25-alpha-build-5

* Improved: LAC is now shown in the InfoWindow on the MapViewer
* Changed: We do **NOT** support DarkCoin donation at the moment. Discuss [#74](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/74)!
* Added: New `SignalStrengthTracker` to detect abnormal and suspicious behavior
* Added: Notifications about Travis CI builds and GitHub in our internal chats
* Added: More [similiar projects](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Similar-Projects) arised. We are thankful, but we won't give up!
* Fixed: Looping builds and error messages from Travis CI should be gone now
* Fixed: Several small errors arising with new code have been purged ahead of time

---

#### 19.12.2014 - WIP-Internal v0.1.25-alpha-build-4

* Improved: Consistent icon usage based on current security status
* Improved: Icon and status-checking moved to dedicated static classes
* Improved: OpenCellId dialog now closes automatically on success
* Improved: MapViewer now defaults to MCC location on startup
* Removed: Debug Log has been purged to make logfiles smaller
* Removed: Some redudant icon update code has been purged
* Changed: Limited log output to 1000 lines to avoid hanging
* Changed: Numerous code comments and changes on DB Viewer tables
* Changed: Base activity class for icon updates moved to Broadcasts
* Changed: `MapViewerOsmDroid.java` now uses same BaseActivity
* Changed: Switched to new Container-Based Infrastructure in Travis CI
* Changed: Many small changes and improvements made to `.travis.yml`
* Added:`CHANGE_NETWORK_STATE` to change RAT and some other things
* Added: Example on code comments added to `CONTRIBUTING.md`
* Added: Espionage on Norwegian Politicians added to `README`
* Added: Cellular Exploitation talk added to `PAPERS/GSM_Security`
* Added: New directory `MISC` for miscellaneous unrelated items
* Added: Comment about origin added to `CellInfoFragment.java`
* Added: Getter function for DBM for future use added to `Device.java`
* Fixed: Missing store banners within our `README` have been re-added
* Fixed: Debugging view has been fixed to not jump up and down
* Fixed: Text labels in Map View and DB Viewer now display correctly
* Fixed: Randomly occuring app crash on map page has been erased

---

#### 09.12.2014 - WIP-Internal v0.1.25-alpha-build-3

* Improved: OpenCellId error messages now displays for longer period of time
* Improved: Specified UTF-8 charset for requesting key from OpenCellID server
* Changed: `Send Error Log` expanded into menu `Debugging` (Work-in-Progress)
* Added: Debug Logs screen to view, clear, copy, and e-mail enhanced app logs

---

#### 07.12.2014 - WIP-Internal v0.1.25-alpha-build-2

* Updated: Work-in-Progress on the General and Technical Overviews in our WIKI
* Updated: Clarified some important contribution instructions in `CONTRIBUTING.md`
* Updated: Minor modifications of our `Development Roadmap` to reflect current travel
* Updated: Gradle config now uses plugin v1.0.X for latest Gradle and Android Studio
* Changed: Sorted permissions by permission type for better overview and development
* Updated: Build tools in `.travis.yml` and `build.gradle` updated to latest version
* Removed: Disabled GAPI which had been used to enable MapViewer without using GPS
* Removed: Erased assertion for title as it might fail on first-launch (e.g. in Travis-CI)
* Added: Test for LAC change detection (low-level) has been implemented
* Added: Dummy instrumentation test for basing future tests on is now integrated
* Added: Null guard has been added on OCID API key to avoid failures without API key
* Added: Ability to request a new OCID key right from the `PREFERENCES` menu within our App
* Added: Granted permission `ACCESS_COARSE_UPDATES` required by `getNeighboringCellInfo`
* Added: Code comments on incorrect Silent SMS detection (working hard to improve this)
* Added: Detailled descriptions of the Status Icons of our App as a Work-in-Progress
* Added: Titles added to download links to notify about prior Aptoide App installation
* Added: Placed `testApplicationId` into `build.gradle` against more failing Travis CI builds
* Added: Even more memory to Gradle JVM has been granted to make builds more stable
* Fixed: Crash on startup due to missing check of `homeOperator` value has been resolved

---

#### 26.11.2014 - WIP-Internal v0.1.25-alpha-build-1

* Changed: Service now does not start up until our Disclaimer has been accepted
* Changed: Disclaimer buttons have been simplified to show "I Agree" and "I Disagree"
* Updated: Fresh screenshots of our currently released App
* Updated: More small updates of our `README` containing the history on IMSI-Catchers
* Updated: Added information about the Secret U.S. Spy Program to our `README`
* Updated: Refreshed badges of our official stores to make the align neatly
* Added: Declared `android.permission.ACCESS_SUPERUSER` in `AndroidManifest.xml`
* Added: AIMSICD Program Modules have been added to our Technical Overview in the WIKI
* Added: Dependency on `ShowcaseView` to `build.gradle` to craft a neat HowTo in holo style
* Added: New awesome article in German by the 'Piratenpartei' about IMSI-Catchers and us
* Added: Banner in SVG format as well as vertical direction to be used in posts and press
* Added: `FileTree` in `DOCUMENTATION` to describe what each file is doing and what it is used for
* Added: Full display of `README` in several directories throught our repo
* Added: Crafted file `signing.properties` for automatic release signing later on
* Added: Cryptanalysis of GSM has been added to `PAPERS/GSM_Security`
* Added: Slides on A5/1 Cracking from 26C3 have been added to `PAPERS/GSM_Security`
* Added: Triton Surveillance Brochure has been added to  `PAPERS/SysInfos`
* Added: Link to the Cryptome GSM Files (just in case you feel like hacking your mind)
* Added: Link to a new awesome Project called the "[Android-CipheringIndicator-API](https://github.com/PrivacyCollective/Android-CipheringIndicator-API)"
* Added: Link to the portal post on XDA-Developers to celebrate our FIRST ANNIVERSARY!
* Added: Added bloatware removal to `README` section stating what we **DO NOT** provide
* Fixed: Eliminated crash that appeared when disagreeing to our Disclaimer
* Fixed: Newly added APK naming code has been polished to work in the future
* Fixed: Travis CI builds should be stable and smooth now - what a rough ride!
* Fixed: Default MCC list now shows record count

---

#### 03.11.2014 - [WIP-Release v0.1.24-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.24-alpha)

* **LAUNCHED:** We are now officially on [Aptoide](http://aimsicd.store.aptoide.com/) and [F-Droid](https://f-droid.org/repository/browse/?fdid=com.SecUpwN.AIMSICD) (builds should appear shortly)
* **Changed:** Signing Keys have been renewed so you can make sure we officially build the APK :exclamation:
* Changed: Reworked all Icons to be rendered in best and highest quality possible
* Changed: Switched to `PDFMtEd Inspector` to erase metadata since it is safer than just `exiftool`
* Updated: Revamped About-Screen through the work of our awesome developer Toby Kurien
* Updated: Rewrote a major part of the `Application Goals` and `Development Roadmap`
* Updated: Added more great contributors to our `CREDITS` - huge thanks for all the hard work
* Added: Skull QR Code in `PROMOTION` folder to be printed as a sticker (use it wisely, please)
* Added: Paper about Eavesdropping Method Patent by Rohde & Schwarz in `PAPERS/Technical`
* Added: Paper about Court of Appeal judgment invalidating Rohde & Schwarz patent from above
* Added: New badges for development stage and statement against using GooglePlay
* Added: Multiple attempts to auto-rename APK file for later automatic releases (WIP)
* Added: Pseudo code for importing `versionCode` and `versionName` from `AndroidManifest.xml`
* Added: SIMAlliance permission for reading SIM related features from the Open Mobile API
* Added: Integrated Coverity Scan to allow testing with static analysis tools
* Added: New Sponsors have been added to our `README` - feel invited to visit them
* Added: E-Mail address has been added to receive Error Logs - only use on heavy crashes
* Fixed: Many string fixes and small adjustments to make usage of our App much more clear

---

#### 26.10.2014 - WIP-Internal v0.1.24-alpha-build-17

* Changed: Refreshed high quality Sense Style Icons
* Changed: Database file name changed to `aimsicd.db` to find it much faster
* Changed: Some default values have been modified to simplify debugging
* Changed: `msgLong` and `msgShort` changed to always find the `mainLooper` to display toasts
* Changed: Map pin icons with new colours as per #166 (neighbouring cells pins in orange)
* Added: White Style Icons in High Quality
* Added: Flat Style Icons in High Quality
* Added: Neighbouring cell info will now be shown with the map if available
* Added: Phone state listener to update the map when cell info changes
* Added: Experimental "send bug log" item that reads logcat output and shares via e-mail
* Added: Threading in "send bug log" (requires new `READ_LOGS` permission, may not work on KitKat+)
* Added: New error display for previously-silent error message
* Added: More code comments to make it easier for other developers to contribute
* Added: Further attempts to fix the ATCoP (still not working as it should)
* Fixed: Some strings and comments have received some more polishing

---

#### 25.10.2014 - WIP-Internal v0.1.24-alpha-build-16

* Added: Paper about the CellSense GSM Positioning System in `PAPERS/Technical`
* Added: `MODIFY_PHONE_STATE` in order to support ICC (SIM) related commands
* Added: New `DATABASE.md` info file to document how the DB of AIMSICD works (WIP)
* Added: New `DOCUMENTATION` directory for files and images used for documentation

---

#### 20.10.2014 - WIP-Internal v0.1.24-alpha-build-15

* Changed: Restructured `CHANGELOG.md` to allow better viewing and WIP-Release links
* Updated: Fresh `SCREENSHOTS` of the currents state of AIMSICD as well as updated Teaser
* Updated: Added a few comments to text fields in `DbViewerFragment.java`
* Removed: Erased the "Sanity Check" activity of RootTools from the launcher app drawer
* Removed: Erased thread contention
* Fixed: Solved problems with unclosed database connection
* Fixed: Edited `strings.xml` to fix some string on the Device Details page
* Fixed: Additional icon in the launcher app drawer created by RootTools

---

#### 19.10.2014 - WIP-Internal v0.1.24-alpha-build-14

* Removed: Erased binary JAR files to make our project become fully [FOSS](https://en.wikipedia.org/wiki/Free_and_open-source_software)! ;-)
* Changed: Re-configured build to pull artifacts from Maven Central
* Added: RootTools source so that AT Command Injector and other funky features keep working
* Added: Credits to [Stericson](https://github.com/Stericson) for his awesome RootTools and his help to our project
* Added: High Quality Launcher Icons to make our App look crisp on higher density screens
* Added: Sense Style Icons in High Quality (currently discussing how to continue in Android 5.0)
* Fixed: Corrected database problems due to database connection not being handled correctly
* Fixed: Removed Apache warnings from Proguard so that Travis CI cannot complain

---

#### 12.10.2014 - WIP-Internal v0.1.24-alpha-build-13

* Removed: Erased leftover PyGTK and NEATO Tutorials from our `PAPERS` section
* Changed: Unified project name as "AIMSICD-Project" - we erased "IMSI-Cure" and "I'm Secure"
* Updated: New rules for Code/Syntax Highlighting have been added to our `CONTRIBUTING.md`
* Moved: Api17+ stuff into separate `DeviceApi17` class to prevent verify failures on older APIs
* Added: `open_cellid_api_key` has been added to support optional `gradle.properties` 
* Added: Promotion material of the Speaker Identification Field Toolkit (SIFT) added to `PAPERS/SysInfo`
* Added: Master Thesis of Adam Kostrzewa about MITM-Attack on GSM added to `PAPERS/Thesis`
* Added: IMSI-Catcher Disclosure Request by several experts and researchers added to `PAPERS/Law`
* Added: Surveillance Catalogue by NeoSoft AG with different IMSI-Catchers added to `PAPERS/SysInfo`
* Fixed: Overlapping of AT Command Injector and About page has been solved
* Fixed: Startup error and crash of Cell Info on Android 4.2 have been solved as well
* Fixed: All links on our README have been verified (please let us know if anything broke)

---

#### 04.10.2014 - WIP-Internal v0.1.24-alpha-build-12

* Changed: Polished even more internal strings to reflect current functionality of buttons
* Changed: Switched to fresh new theme for our website which suits our project much better
* Changed: Images like Banners are now being pulled directly from our won GitHub repository
* Removed: Deleted old code of Map Viewer which had been left in the code structure
* Updated: Refreshed `SCREENSHOTS` and added new Images of our cool Map Viewer as well
* Added: New `PROMOTION` directory which will be expanded with awesome promotional material
* Added: GSMK Baseband Firewall Patent uploaded into our `PAPERS` as a reference
* Added: New page on Requirements in our WIKI to enable users to check App dependencies
* Added: New page in our WIKI on Privacy to ease the minds of scared contributers
* Added: Support for TRAVIS CI which will continually check if our code builds well
* Added: Ruby script to split OpenCellID CSV file into separate files for each MCC
* Added: Re-enabled slide-view for Cell Information, Database Viewer and Neighboring Cells View
* Moved: Donation Info now sits at the bottom of our README to make room for App Teaser
* Fixed: Enhanced code to remove multiple user prompts while phone had been locked
* Fixed: Enhanced code tp remove auto-starting of non-persistent service when GPS toggled

---

#### 30.09.2014 - WIP-Internal v0.1.24-alpha-build-11

* Updated: Some restructuring done to `README` for better mobile view
* Updated: Exspressed our teams gratitude to our new developer Toby Kurien in the `CREDITS`
* Updated: Aligned text in `strings.xml`, `DbViewerFragment`, `CellTracker` and `MapViewerOsmDroid`
* Updated: More code cleanup done by our fabulous developer Toby Kurien
* Added: New page with Media Material in our WIKI for people wanting to spread the word

---

#### 27.09.2014 - WIP-Internal v0.1.24-alpha-build-10

* Changed: Extracted `Accelerometer`, `Location Tracking`, and `RilExecutor` into separate classes
* Changed: Extracted `CellTracker` from the service and refactored code everywhere
* Changed: Small changes made to `README` to include General and Technical Overview
* Updated: Several small changes made to strings.xml for unified style (attempt to unify style)
* Fixed: Moved `RilExecutor` to correct package and hooked up `RilExecutor` into `CellInfoFragment`
* Fixed: Cleaned up the code to resolve title "Map Viewer" staying after switching tabs
* Fixed: Corrected Cell Tracking status - code got left out during refactoring
* Fixed: Corrected regression through which the service did not load correctly on startup

---

#### 26.09.2014 - WIP-Internal v0.1.24-alpha-build-9

* Changed: Re-worked accelerometer detection for better battery savings. Needs lots of testing.
* Added: More tweaks and different map tile sources for the various map type settings
* Fixed: Eliminated bug which terminates the service when AIMSICD is closed

---

#### 24.09.2014 - WIP-Internal v0.1.24-alpha-build-8

* Removed: Fully removed Google Play Services - WELCOME our new developer @tobykurien! ;-)
* Updated: Switch from Google Maps to OsmDroid (WIP), much smaller file size of APK
* Updated: OsmDroid mapview in progress - all pins added to map
* Updated: Integrated changes from upstream
* Updated: Cleaned up info window dialog box
* Added: We've got an official FAQ now. Feel free to check it out in the WIKI.
* Added: Implemented async map building and cleaned up
* Added: Implemented `MarkerData` to cell tower overlay items
* Fixed: Renewed default location usage
* Fixed: Stable null pointers on a clean install

---

#### 20.09.2014

* Changed: Added preference for Automatic Cell Monitoring which is now ENABLED by default
* Updated: Minor code cleanup, small visual improvements on the strings
* Added: Link of our WIKI has been implemented into to the About-Menu
* Fixed: Correct NPE experienced when trying to view the Silent SMS database contents
* Fixed: Additional check for Google Play Services prior to starting the MapViewer
* Fixed: Added MCC & MNC details Map Marker display window for local database cell information
* Fixed: Exit Button will now actually exit the activity instead of doing absolutely nothing at all
* Fixed: Use `GeoLocation` Class & store last Location to use it for any methods that require location

---

#### 17.09.2014 - WIP-Internal v0.1.24-alpha-build-7

* Updated: Modifed the OpenCellID methods to enable each user to request and use their own keys
* Updated: Added small clarifications to Phone Type and RIL Version within Device Information
* Updated: Added `abortOnError false` so that gradle build runs without a problem (thanks @ligi)

---

#### 15.09.2014 - WIP-Internal v0.1.24-alpha-build-6

* Improved: Several small adjustments of our GitHub Introduction (thanks @andr3jx)
* Changed: Reverted notification behaviour to correctly launch each Status Icon as meant to be
* Updated: Better Location Services (GPS) Handling to detect if GPS was actually activated
* Updated: Rewritten AT Command Injection v2 & `RootTools` to bring better level of functionality
* Fixed: Corrected Cell Monitoring method to correctly update the notification icon

---

#### 08.09.2014 - WIP-Internal v0.1.24-alpha-build-5

* Updated: Cleaned Cell Table Contents to assist with Detection Method 1
* Updated: Small tweaks to the updated neighbouring cell methods for better functionality
* Added: Initial Implementation of Detection Method 1: Changing LAC
* Added: New Papers about IMSI Privacy and Projects developing IMSI-Catcher-Catchers
* Fixed: CDMA Layout Correction to address NPE experienced by CDMA device users

---

#### 19.08.2014 - WIP-Internal v0.1.24-alpha-build-4

* Improved: Poll the telephone manager for neighboring cell infos
* Changed: Reverted "Shell & AT Command Fragment Tweaks"
* Changed: Reverted commit that was possibly causing instability compared with v0.1.21
* Changed: Rewrote both upload and download methods of OpenCellID and requested new API key
* Removed: Heading from Layout has been erased for a much clearer overview
* Updated: Cell Classes have been updated
* Updated: OpenCSV has been updated to v2.4
* Updated: Extensive updates to the Cell Class to provide an easy system for tracking and comparison
* Updated: Neighbouring Cells have been slightly teaked based on the commit made by @rtreffr
* Added: Small banner on AT Command Injector to notify of setup and ignore any error messages
* Fixed: Corrected Proguard Settings 

---

#### 13.08.2014 - WIP-Internal v0.1.24-alpha-build-3

* Updated: Slight updates to layouts to enable correct Right to Left support
* Updated: Changed the layout of neighbouring cell information to display PSC, RSSI & Network Type
* Updated: OpenCellID Contribution Upload finished to allow the use of the API functions
* Updated: Cleaned up some database cursors that were not being closed correctly
* Added: New preference for contribution of tracked cell information to the OpenCellID project
* Fixed: Removed code that attempts to draw the MCC & MNC whilst refreshing the device information
* Fixed: Changed wording within our `DISCLAIMER` to comply with the GPL and avoid confusion

---

#### 08.08.2014 - WIP-Internal v0.1.24-alpha-build-2

* Updated: Too many small improvements of our `README` (enough editing for now, off to development!)
* Updated: `SOURCES` file has been updated with the latest used code snippets
* Updated: `SCREENSHOTS` have been updated to reflect the latest UI of HushSMS
* Updated: Cell data table has been updated for correct contributions to OpenCellID
* Updated: Cell Clas & Card UI Updates to support extra data and extended functions
* Updated: Neighbouring cell methods have been rewritten to improve neighbouring cell functions
* Removed: Tested Gitter chat has fully been purged due to security issues we cannot support
* Added: `CONTRIBUTIONS` has been expanded with some links for help on GitHub Markdown
* Fixed: `getAllCellInfo` method has been corrected in a number of it's uses
* Fixed: Tweaks Device & Service to address numerous small bugs and NPEs in isolated cases
* Fixed: Small corrections of strings within AIMSICD, including a proper version formatting

---

#### 08.07.2014 - [WIP-Release v0.1.23-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.23-alpha)

* Updated: General code clean up as well as declaration access tweaks where appropriate
* Added: Gmaps undocumented API incorporated to translate a GSM CellID into a location value
* Added: Special thanks to [andr3jx](https://github.com/andr3jx) for the correct data writing layout and confirmation that above method works
* Added: Primary Scrambling Code (PSC) display added to the device fragment where available
* Fixed: Wcdma CellInfo type added to getAllCellInfo method to address unknown cell type exception
* Fixed: Custom map info window corrected to return to a black background of the info window

---

#### 03.07.2014 - [WIP-Release v0.1.22-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.22-alpha)

* Updated: Drawer UI components now allow removal of all option menu items
* Updated: Tracking functions are now available through the first section of the drawer
* Updated: Items will dynamically update to reflect the current status of the tracking item
* Updated: FemtoCell tracking will from now on only be shown for CDMA devices
* Updated: Moved classes & interface files into dedicated package folder for easier maintenance
* Updated: Small updates to methods providing data from the device class
* Updated: Screenshots updated to reflect latest changes within the UI and menu structure
* Updated: User Guide updated with new screenshots and explanations on several menus
* Added: Check for devices that are returning both the CID & LAC as `INTEGER.MAX` value
* Added: Spinner for selection of a number of timeout values to assist with AT Command responses
* Fixed: Missing check for bound service added to the MapViewer to address NPE
* Fixed: Method `getAllCellInfo` is encountering an unknown CellInfo type, added log to display the error
* Fixed: Corrected `RequestTask` class to correctly show progress bar for downloading OpenCellID data

---

#### 29.06.2014

* Updated: Screenshots updated to reflect latest changes within the UI and menu structure
* Updated: User Guide updated with new screenshots and explanations on several menus

---

#### 28.06.2014 - [WIP-Release v0.1.21-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.21-alpha)

* Updated: Moved large number of methods out of the service and into the Device class
* Added: Automatic detection system for possible candidates for AT command serial devices
* Added: Shell class added to provide ongoing root shell for correct execution of AT Commands
* Added: System will attempt to draw the system property `rild.libargs` for the AT serial device
* Added: ATCI* devices will be checked within `/dev/radio/` on MTK devices with the MT6282 chip
* Fixed: Consistent background colour for text items correcting strange look in several menus
* Fixed: Corrected some items to achieve correct display on different screen sizes

---

#### 21.06.2014 - [WIP-Release v0.1.20-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.20-alpha)

* Changed: Massive revamp of the UI for the application with newer drawer style UI
* Updated: AT Command Processor is **ENABLED** now - huge THANKS to [E3V3A](https://github.com/E3V3A)! **ROOT REQUIRED!**
* Updated: WIKI of [AT Command Processor](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/AT-Command-Processor) improved, please see this before using AT Commands
* Improved: Clean up and maintenance of code format, arrangement of imports and formatting style
* Removed: Unnecessary calls from methods based on fragment lifecycles
* Added: Method to the service to return the Mobile Country Code (MCC)
* Added: User preference selection of the application refresh rate
* Fixed: Corrected the list that stores neighbouring cell information
* Fixed: Modified layout for the neighbouring cells so they display now using the Card UI style
* Fixed: Change onDestory method to address the NPE some devices had when service was destroyed
* Fixed: Corrected NPE caused by null location returned during loadEntries method

---

#### 20.06.2014

* Updated: Massive revamp of the UI to nuke swiping and add a new menu button on the upper left side
* Added: App refresh rate implemented into the Device Information & Cell Information fragments
* Added: Re-added CMD Processor functions to support AT Command Injection via device terminal

---

#### 19.06.2014 - [WIP-Release v0.1.19-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.19-alpha)

* Updated: Android API version of Neighbouring Cell Information as it was broken
* Added: Local Broadcast Receiver handling OpenCellID data and ensure map markers reflect data
* Added: Cell class added to support modified API neighbouring cell data and simplify operations
* Fixed: Corrected async task calling UI methods, Database backup and restore should work now
* Fixed: Corrected naming value to correctly show our new awesome Icons
* Fixed: Modified MapViewer location logic which previously did not return any data
* Fixed: Fully removed CDMA specific location methods which caused crashes AGAIN for CDMA users

---

#### 18.06.2014

* Changed: Reverted commits that changed Silent SMS to Flash SMS. we **only** want to detect Silent SMS
* Changed: SMS Broadcast receiver modified to look for true TYPE 0 Stealth SMS messages
* Updated: Modified User Guide to reflect the latest changes and clarifications thereafter
* Added: Additional check for Message type indicator & the TP-PID value
* Added: Yet another additional check added as framework seems to use both in different places
* Fixed: Disabled notification alert once the application becomes visible to address persisting alert

---

#### 17.06.2014

* Updated: New explanations in User Guide to distinguish between Class 0 and Type 0 SMS
* Added: Link to the NSA’s Secret Role in the U.S. Assassination Program
* Fixed: Corrected code values and namings for Flash SMS in multiple files

---

#### 16.06.2014

* Updated: Changed naming of Icons within AimsicdService.java
* Added: [SCREENSHOTS](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/tree/master/SCREENSHOTS) for all our needs to explain our App - enjoy the eye candy!
* Added: Initial version of our [official User Guide](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/User-Guide), will be constantly updated
* Fixed: Corrected naming of Icons to reflect the new naming scheme

---

#### 15.06.2014

* Added: Fresh Icons indicating the new threat levels "MEDIUM", "HIGH" and "RUN!"

---

#### 14.06.2014 - [WIP-Release v0.1.18-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.18-alpha)

* **ATTENTION**: Backup your Database prior to installing this WIP-Release!
* Added: Database Restore added to repopulate database tables from exported CSV
* Added: Silent SMS Detection - Type 0 SMS will be intercepted and will display a system alert
* Added: OpenCellID, DB Backup & Restore have been added to main async class for better performance
* Fixed: OpenCellID download methods have been rewritten and should function correctly now
* Fixed: Device specific updates which addressed some issues being faced by users with CDMA devices

---

#### 13.06.2014

* Updated: Major revamp of our `README`, making it much fresher and structured
* Updated: Reworked our WIKI and instructions on how to correctly submit Issues
* Changed: `StyleGuide` is now `CONTRIBUTING.md` - will be shown when opening Issues
* Added: We now accept [ANONYMOUS DONATIONS](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Anonymous-Donations) through [DarkCoin](https://www.darkcoin.io/)! ;-)

---

#### 27.05.2014 - [WIP-Release v0.1.17-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.17-alpha)

* Updated: Better way of how AIMSICD draws and displays any SIM variable & further error handling
* Changed: Cell & Location tracking functions now require GPS location services to be enabled
* Added: Initial version of a custom InfoWindow in the Map Viewer to display further details
* Added: Record ID added to the database viewer cards
* Fixed: Reverted an update to the GMS Play Services library to solve issues with the Map Viewer

---

#### 26.05.2014

* Added: SHA-1 Checksums have been (and will be) added to all (previous) WIP-Releases

---

#### 21.05.2014 - [WIP-Release v0.1.16-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.16-alpha)

* Updated: Disabling Cell tracking will now also disable Location tracking if enabled
* Updated: Huge code cleanup and updates to a number of areas across a large number of packages
* Updated: Extension of the Samsung MultiRil to attempt hooking of the `OemHookStrings` method
* Removed: Unnecessary compatibility library (positive reduction in size of the APK)
* Added: AT Command Injector (currently disabled until further testing has been completed)
* Added: Code comments to a variety of methods
* Fixed: Adressed numerous possible NPE for better stability and an increased user experience

---

#### 10.05.2014 - [WIP-Release v0.1.15-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.15-alpha)

* Updated: Map Viewer type selection moved into a dedicated Map preference fragment
* Updated: About Page completed with links functioning correctly
* Updated: Cleaned up source file structure and moved items for a more logical structure
* Fixed: Main layout alignment corrected and page order changed so Cell Information is now page 2
* Fixed: Service Persistence Preferences updated to work with the change of the preference logic
* Fixed: Multiple small issues with some device variables with Network type

---

#### 06.05.2014 - [WIP-Release v0.1.14-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.14-alpha)

* Added: Neighbouring Cell details shown on Cell Information fragment
* Added: Ciphering Indicator provided through the Samsung MultiRil method by [Alexey Illarionov](https://github.com/illarionov/SamsungRilMulticlient)
* Added: Initial About-Dialog providing information about AIMSICD

---

#### 03.05.2014 - [WIP-Release v0.1.13-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.13-alpha)

* Added: Exception handling added to all methods that attempt to draw SIM specific information

---

#### 03.05.2014 - [WIP-Release v0.1.12-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.12-alpha)

* Updated: MASSIVE UI update implementing fragments to enable easy navigation through SWIPING! ;-)
* Updated: Default icon is now selected within the preferences screen
* Fixed: Solved AIMSICD crashing on HTC One M7 through several code improvements

---

#### 03.05.2014 - [WIP-Release v0.1.11-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.11-alpha)

* Updated: Consolidation of Signal Strength, Data Activity & Data State into one Phone State Listener
* Updated: Main menu updated to only display FemtoCell detection on CDMA devices only
* Added: Requirement to accept our Disclaimer (if you decline you MUST uninstall AIMSICD)
* Added: Support for [NoGAPPS Maps API](http://forum.xda-developers.com/showthread.php?t=1715375) if you don't like to install Google Maps
* Fixed: Map Viewer Tweaks to correct issues with initial location placement and map type preferences

---

#### 27.04.2014 - [WIP-Release v0.1.10-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.10-alpha)

* Improved: Database Viewer UI tweaked to use a gradient colouring scheme
* Improved: Map Viewer will fall back to a default location based on MCC and the Countries Capital City
* Fixed: Resolved bug with Signal Strength Tracking icon always showing as disabled

---

#### 25.04.2014 - [WIP-Release v0.1.9-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.9-alpha)

* Improved: Complete rewrite of Database helper functions for better logic of updating/inserting records
* Changed: Minimum SDK version increased to 16 (JB 4.1)
* Removed: Erased depreciated methods that were required to support Android versions < JB4.1
* Removed: `SupportMapFragment` usage in MapViewer allowing the use of the standard MapFragment
* Removed: Erased some unneeded imports and cleaned up little bits of code
* Updated: Preferences screen to use the newer Preference Fragment
* Updated: OpenCellID data is now stored within its own table (assists in IMSI-Catcher detection)
* Added: Ability to view the current database contents using a CardUI type interface
* Added: Access to all current contents including the newly created OpenCellID data table
* Fixed: Femtocell Detection preference selection now correctly actioned on start-up

---

#### 25.04.2014 - [WIP-Release v0.1.8-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.8-alpha)

* **CAUTION:** This version will erase your existing tracking information! Please backup first.
* Removed: Removed the `CMDProcessor` as this was not being utilised at all
* Added: Confirmation dialog once the database export has been successful
* Added: Lots of code & method comments so eventually a nice JavaDoc page could be created
* Added: CellInfo details to retrieve the LTE Timing Advance data (not activated yet)

---

#### 21.04.2014 - [WIP-Release v0.1.7-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.7-alpha)

* Added: Enabled CDMA Femtocell Detection for initial testing. (CDMA devices ONLY!)
* Added: Missing resources for Actionbar icons
* Added: [GeoLocation class](http://janmatuschek.de/LatitudeLongitudeBoundingCoordinates) to provides an equation to determine a bounding radius of a given point
* Added: Option to select map type and extended the details
* Fixed: Preference change is now detected correctly by the application showing immediate changes
* Fixed: MapViewer updated to check for Google Play Services
* Fixed: MapViewer will now default to last known location (if one is available) 

---

#### 11.04.2014 - [WIP-Release v0.1.6-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.6-alpha)

* Changed: Project changed to Gradle Build System to make use of Android Studio
* Added: Google Maps API v2 support to enable new features and gain an API Key
* Fixed: Signal strength data is now correctly overlayed on the map as well
* Fixed: Database export corrected and changed to use OpenCV library

---

#### 09.04.2014 - [WIP-Release v0.1.5-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.5-alpha)

* Improved: Universal Compatibility achieved via the [Universal Compatibility Commit](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/24)!
* Reduced functionality of AIMSICD until methods are discovered to issue AT commands.
* Improved: AIMSICD now can be installed as user application (no ROOT needed)!
* Improved: AIMSICD should now function on any device. We're open for your feedback!
* Femtocell Detection will be fully implemented for CDMA devices in the next commit by [xLaMbChOpSx](https://github.com/xLaMbChOpSx).

---

#### 08.04.2014 - [WIP-Release v0.1.4-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.4-alpha)

* Updated: `DISCLAIMER` to encourage people to talk to us BEFORE hunting our developers
* Updated: `CREDITS` to reflect latest contributions (please give me a hint if I missed someone)
* Removed: Purged folder `MERGESOURCE` to clean up unused code and avoid confusion
* Added: [SOURCES](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/SOURCES). Please actively use it. Know where your code comes from.
* Removed: Erased `TODO` and created `WANTED_FEATURES`. Hit us with your ideas there!
* Complete revamp of our [PAPERS-Directory](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/tree/master/PAPERS). Make sure to grab your nightly lecture.
* Another new and fresh Iconset has been added with a [pull request](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/20) [SgtObst](https://github.com/SgtObst). Cool! :)
* New [pull request](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/21) adding Service, Boot Completed Receiver, TableView and FemtoCatcher Additions
* To everyone developing here: You're doing a WONDERFUL job! THANK YOU!

---

#### 06.04.2014 - [WIP-Release v0.1.3-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.3-alpha)

* Applause to [xLaMbChOpSx](https://github.com/xLaMbChOpSx)! He submitted a new [pull request](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/14) with a complete rewrite of the menu system, implementing the Actionbar, rewrite of the Device class to perform better and change it from a static class, Persistent notification was added and Database helper class was created to provide application wide support.
* The old Icons of RawPhone have been fully replaced by the great work of [SgtObst](https://github.com/SgtObst). More to come soon!
* Our developers are currently working **hard** to find viable ways to acquire the ciphering info
* If you have **good** ideas or code and are a programmer, perticipate in the [official development thread](http://forum.xda-developers.com/showthread.php?t=1422969) (only technical talk).

---

#### 02.04.2014

* Thanks to the creative work of [SgtObst](https://github.com/SgtObst), we now have FRESH and NEW ICONS for AIMSICD! ;-)
* Added a `TODO` for collecting the things that are planned to be added in the long run.

---

#### 31.03.2014 - [WIP-Release v0.1.2-alpha](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/tag/v0.1.2-alpha)

* This Release has been signed using the platform keys provided in the AOSP source
* New [pull request](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/7) by [xLaMbChOpSx](https://github.com/xLaMbChOpSx) reducing the target SDK version and update the code.
* Bookmark our [WIP-RELEASES](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/) and feel free to [report feedback on XDA](http://forum.xda-developers.com/showthread.php?t=1422969).
* New [WIKI-Pages](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki) by [xLaMbChOpSx](https://github.com/xLaMbChOpSx) explaining building and installation. HAVE PHUN! ;-)

---

#### 27.03.2014

* Team members have been crawled by IP adresses not connected to any country (probably secret agencies)
* Members of the famous company Rohde & Schwarz (leading manufacturer of IMSI-Catchers) are watching us
* We know that with our actions we already have attracked dark forces out there. See our [Disclaimer](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/DISCLAIMER)!
* Just to mention it: I always have the latest copy of everything. Enjoy!

---

#### 25.03.2014 - WIP-Release v0.1.1-alpha

* Progress! We've merged an [Intitial Development Commit](/https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/5) by [xLaMbChOpSx](https://github.com/xLaMbChOpSx). HUGE THANKS!

---

#### 22.03.2014 - WIP-Release v0.1-alpha

* Added source code of [RawPhone](https://play.google.com/store/apps/details?id=com.jofrepalau.rawphone) into main file tree to start off with. Throw your commits at me! 

---

#### 21.03.2014

* Ladies and Gentlemen, I am **honored** to announce our [German Article featuring AIMSICD](http://www.kuketz-blog.de/imsi-catcher-erkennung-fuer-android-aimsicd)! ;-)
* THANK YOU, Mike! Bookmark www.kuketz-blog.de - a lot of awesome stuff to discover and learn!
* THANK YOU, [He3556](https://github.com/He3556)! You really are a gold nugget for this project. Keep rockin' with us!

---

#### 16.03.2014

* Beautified `README.md` and added [TextSecure](https://github.com/WhisperSystems/TextSecure) to the links worth checking out
* Finalized article for [Kuketz IT-Security Blog](http://www.kuketz-blog.de/), last review to be done by Mike Kuketz
* App Icon + Banner for article and GitHub **WANTED**, ideas welcome in our [Development Thread](http://forum.xda-developers.com/showthread.php?t=1422969)!

---

#### 10.03.2014
* Added file [CONTRIBUTING](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/CONTRIBUTING.md) to avoid neglected commits - please read it before commiting!

---

#### 17.02.2014

* Article for [Kuketz IT-Security Blog](http://www.kuketz-blog.de/) by [He3556](https://github.com/He3556) and me almost finished (still needs review)
* Cleanup of GitHub is being prepared in order to add code and commits. Stay tuned!

---

#### 02.02.2014

* The [EFF](https://www.eff.org/) and [The Guardian Project](https://guardianproject.info/) have been contacted to join our quest
* Mike Kuketz of the [Kuketz IT-Security Blog](http://www.kuketz-blog.de/) confirms article in the works about our project

---

#### 11.01.2014

* Creation of folder `MERGESOURCE` for source code of apps to be added to 'AIMSICD'
* Added source code of app '[RawPhone](https://play.google.com/store/apps/details?id=com.jofrepalau.rawphone)' to be used in the base of our own app

---

#### 23.11.2013

* XDA member '[SecUpwN](http://forum.xda-developers.com/member.php?u=4686037)' is still fire and flame for the project, this GitHub is born
* Added important files of abandoned GitHub-Projects, polished up our own Repository

---

#### 27.02.2013

* E:V:A created the [preliminary developer roadmap](http://forum.xda-developers.com/showpost.php?p=38386937&postcount=45) to break down app development
* Clarification of the apps purpose and heavy discussion around IMSI-Catchers starts to emerge

---

#### 19.01.2013

* Project now actively looking for talented and interested developers to produce the PoC-App

---

#### 02.01.2012

* Initial creation of the [project thread](http://forum.xda-developers.com/showthread.php?t=1422969) through XDA recognized developer '[E:V:A](http://forum.xda-developers.com/member.php?u=4372730)'
* Active discussion beginns and people start realizing the need for the proposed PoC-App

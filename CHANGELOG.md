# CHANGELOG of 'AIMSICD'
----------------------

#### 19.10.2014 - WIP-Internal v0.1.24-alpha-build-14

* Removed: Erased binary JAR files to make our project become fully [FOSS](https://en.wikipedia.org/wiki/Free_and_open-source_software)! ;-)
* Changed: Re-configured build to pull artifacts from Maven Central
* Added: RootTools source so that AT Command Injector and other funky features keep working
* Added: Credits to [Stericson](https://github.com/Stericson) for his awesome RootTools and his help to our project
* Added: High Quality Launcher Icons to make our App look crisp on higher density screens
* Added: Sense Style Icons in High Quality (currently discussing how to continue in Android 5.0)
* Fixed: Corrected database problems due to database connection not being handled correctly
* Fixed: Removed Apache warnings from Proguard so that Travis CI cannot complain

#### 12.10.2014 - WIP-Internal v0.1.24-alpha-build-13

* Removed: Erased leftover PyGTK and NEATO Tutorials from our PAPERS section
* Changed: Unified project name as "AIMSICD-Project" - we erased "IMSI-Cure" and "I'm Secure"
* Updated: New rules for Code/Syntax Highlighting have been added to our CONTRIBUTING.md
* Moved: Api17+ stuff into separate DeviceApi17 class to prevent verify failures on older APIs
* Added: open_cellid_api_key has been added to support optional gradle.properties 
* Added: Promotion material of the Speaker Identification Field Toolkit (SIFT) added to PAPERS/SysInfo
* Added: Master Thesis of Adam Kostrzewa about MITM-Attack on GSM added to PAPERS/Thesis
* Added: IMSI-Catcher Disclosure Request by several experts and researchers added to PAPERS/Law
* Added: Surveillance Catalogue by NeoSoft AG with different IMSI-Catchers added to PAPERS/SysInfo
* Fixed: Overlapping of AT Command Injector and About page has been solved
* Fixed: Startup error and crash of Cell Info on Android 4.2 have been solved as well
* Fixed: All links on our README have been verified (please let us know if anything broke)

#### 04.10.2014 - WIP-Internal v0.1.24-alpha-build-12

* Changed: Polished even more internal strings to reflect current functionality of buttons
* Changed: Switched to fresh new theme for our website which suits our project much better
* Changed: Images like Banners are now being pulled directly from our won GitHub repository
* Removed: Deleted old code of Map Viewer which had been left in the code structure
* Updated: Refreshed SCREENSHOTS and added new Images of our cool Map Viewer as well
* Added: New PROMOTION directory which will be expanded with awesome promotional material
* Added: GSMK Baseband Firewall Patent uploaded into our PAPERS as a reference
* Added: New page on Requirements in our WIKI to enable users to check App dependencies
* Added: New page in our WIKI on Privacy to ease the minds of scared contributers
* Added: Support for TRAVIS CI which will continually check if our code builds well
* Added: Ruby script to split OpenCellID CSV file into separate files for each MCC
* Added: Re-enabled slide-view for Cell Information, Database Viewer and Neighboring Cells View
* Moved: Donation Info now sits at the bottom of our README to make room for App Teaser
* Fixed: Enhanced code to remove multiple user prompts while phone had been locked
* Fixed: Enhanced code tp remove auto-starting of non-persistent service when GPS toggled

#### 30.09.2014 - WIP-Internal v0.1.24-alpha-build-11

* Updated: Some restructuring done to README for better mobile view
* Updated: Exspressed our teams gratitude to our new developer Toby Kurien in the CREDITS
* Updated: Aligned text in strings.xml, DbViewerFragment, CellTracker and MapViewerOsmDroid
* Updated: More code cleanup done by our fabulous developer Toby Kurien
* Added: New page with Media Material in our WIKI for people wanting to spread the word

#### 27.09.2014 - WIP-Internal v0.1.24-alpha-build-10

* Changed: Extracted Accelerometer, Location Tracking, and RilExecutor into separate classes
* Changed: Extracted CellTracker from the service and refactored code everywhere
* Changed: Small changes made to README to include General and Technical Overview
* Updated: Several small changes made to strings.xml for unified style (attempt to unify style)
* Fixed: Moved RilExecutor to correct package and hooked up RilExecutor into CellInfoFragment
* Fixed: Cleaned up the code to resolve title "Map Viewer" staying after switching tabs
* Fixed: Corrected Cell Tracking status - code got left out during refactoring
* Fixed: Corrected regression through which the service did not load correctly on startup

#### 26.09.2014 - WIP-Internal v0.1.24-alpha-build-9

* Changed: Re-worked accelerometer detection for better battery savings. Needs lots of testing.
* Added: More tweaks and different map tile sources for the various map type settings
* Fixed: Eliminated bug which terminates the service when AIMSICD is closed

#### 24.09.2014 - WIP-Internal v0.1.24-alpha-build-8

* Removed: Fully removed Google Play Services - WELCOME our new developer @tobykurien! ;-)
* Updated: Switch from Google Maps to OsmDroid (WIP), much smaller file size of APK
* Updated: OsmDroid mapview in progress - all pins added to map
* Updated: Integrated changes from upstream
* Updated: Cleaned up info window dialog box
* Added: We've got an official FAQ now. Feel free to check it out in the WIKI.
* Added: Implemented async map building and cleaned up
* Added: Implemented MarkerData to cell tower overlay items
* Fixed: Renewed default location usage
* Fixed: Stable null pointers on a clean install

#### 20.09.2014

* Changed: Added preference for Automatic Cell Monitoring which is now ENABLED by default
* Updated: Minor code cleanup, small visual improvements on the strings
* Added: Link of our WIKI has been implemented into to the About-Menu
* Fixed: Correct NPE experienced when trying to view the Silent SMS database contents
* Fixed: Additional check for Google Play Services prior to starting the MapViewer
* Fixed: Added MCC & MNC details Map Marker display window for local database cell information
* Fixed: Exit Button will now actually exit the activity instead of doing absolutely nothing at all
* Fixed: Use GeoLocation Class & store last Location to use it for any methods that require location

#### 17.09.2014 - WIP-Internal v0.1.24-alpha-build-7

* Updated: Modifed the OpenCellID methods to enable each user to request and use their own keys
* Updated: Added small clarifications to Phone Type and RIL Version within Device Information
* Updated: Added abortOnError false - so that gradle build runs without a problem (thanks @ligi)

#### 15.09.2014 - WIP-Internal v0.1.24-alpha-build-6

* Improved: Several small adjustments of our GitHub Introduction (thanks @andr3jx)
* Changed: Reverted notification behaviour to correctly launch each Status Icon as meant to be
* Updated: Better Location Services (GPS) Handling to detect if GPS was actually activated
* Updated: Rewritten AT Command Injection v2 & RootTools to bring better level of functionality
* Fixed: Corrected Cell Monitoring method to correctly update the notification icon

#### 08.09.2014 - WIP-Internal v0.1.24-alpha-build-5

* Updated: Cleaned Cell Table Contents to assist with Detection Method 1
* Updated: Small tweaks to the updated neighbouring cell methods for better functionality
* Added: Initial Implementation of Detection Method 1: Changing LAC
* Added: New Papers about IMSI Privacy and Projects developing IMSI-Catcher-Catchers
* Fixed: CDMA Layout Correction to address NPE experienced by CDMA device users

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


#### 13.08.2014 - WIP-Internal v0.1.24-alpha-build-3

* Updated: Slight updates to layouts to enable correct Right to Left support
* Updated: Changed the layout of neighbouring cell information to display PSC, RSSI & Network Type
* Updated: OpenCellID Contribution Upload finished to allow the use of the API functions
* Updated: Cleaned up some database cursors that were not being closed correctly
* Added: New preference for contribution of tracked cell information to the OpenCellID project
* Fixed: Removed code that attempts to draw the MCC & MNC whilst refreshing the device information
* Fixed: Changed wording within our DISCLAIMER to comply with the GPL and avoid confusion

#### 08.08.2014 - WIP-Internal v0.1.24-alpha-build-2

* Updated: Too many small improvements of our README (enough editing for now, off to development!)
* Updated: SOURCES file has been updated with the latest used code snippets
* Updated: SCREENSHOTS have been updated to reflect the latest UI of HushSMS
* Updated: Cell data table has been updated for correct contributions to OpenCellID
* Updated: Cell Clas & Card UI Updates to support extra data and extended functions
* Updated: Neighbouring cell methods have been rewritten to improve neighbouring cell functions
* Removed: Tested Gitter chat has fully been purged due to security issues we cannot support
* Added: CONTRIBUTIONS has been expanded with some links for help on GitHub Markdown
* Fixed: getAllCellInfo method has been corrected in a number of it's uses
* Fixed: Tweaks Device & Service to address numerous small bugs and NPEs in isolated cases
* Fixed: Small corrections of strings within AIMSICD, including a proper version formatting

#### 08.07.2014 - WIP-Release v0.1.23-alpha

* Updated: General code clean up as well as declaration access tweaks where appropriate
* Added: Gmaps undocumented API incorporated to translate a GSM CellID into a location value
* Added: Primary Scrambling Code display added to the device fragment where available
* Fixed: Wcdma CellInfo type added to getAllCellInfo method to address unknown cell type exception
* Fixed: Custom map info window corrected to return to a black background of the info window

#### 03.07.2014 - WIP-Release v0.1.22-alpha

* Updated: Drawer UI components now allow removal of all option menu items
* Updated: Tracking functions are now available through the first section of the drawer
* Updated: Items will dynamically update to reflect the current status of the tracking item
* Updated: FemtoCell tracking will from now on only be shown for CDMA devices
* Updated: Moved classes & interface files into dedicated package folder for easier maintenance
* Updated: Small updates to methods providing data from the device class
* Updated: Screenshots updated to reflect latest changes within the UI and menu structure
* Updated: User Guide updated with new screenshots and explanations on several menus
* Added: Check for devices that are returning both the CID & LAC as INTEGER.MAX value
* Added: Spinner for selection of a number of timeout values to assist with AT Command responses
* Fixed: Missing check for bound service added to the MapViewer to address NPE
* Fixed: Method getAllCellInfo is encountering an unknown CellInfo type, added log to display the error
* Fixed: Corrected RequestTask class to correctly show progress bar for downloading OpenCellID data

#### 29.06.2014

* Updated: Screenshots updated to reflect latest changes within the UI and menu structure
* Updated: User Guide updated with new screenshots and explanations on several menus

#### 28.06.2014 - WIP-Release v0.1.21-alpha

* Updated: Moved large number of methods out of the service and into the Device class
* Added: Automatic detection system for possible candidates for AT command serial devices
* Added: Shell class added to provide ongoing root shell for correct execution of AT Commands
* Fixed: Consistent background colour for text items correcting strange look in several menus
* Fixed: Corrected some items to achieve correct display on different screen sizes

#### 21.06.2014 - WIP-Release v0.1.20-alpha

* Updated: AT Command Processor is **ENABLED** now - huge THANKS to [E3V3A](https://github.com/E3V3A)! **ROOT REQUIRED!**
* Updated: WIKI of [AT Command Injector](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/User-Guide#at-command-injector) improved, please see this before using AT Commands
* Improved: Clean up and maintenance of code format, arrangement of imports and formatting style
* Removed: Unnecessary calls from methods based on fragment lifecycles
* Added: Method to the service to return the Mobile Country Code (MCC)
* Fixed: Corrected the list that stores neighbouring cell information
* Fixed: Modified layout for the neighbouring cells so they display now using the Card UI style
* Fixed: Change onDestory method to address the NPE some devices had when service was destroyed
* Fixed: Corected NPE caused by null location returned during loadEntries method

#### 20.06.2014

* Updated: Massive revamp of the UI to nuke swiping and add a new menu button on the upper left side
* Added: App refresh rate implemented into the Device Information & Cell Information fragments
* Added: Re-added CMD Processor functions to support AT Command Injection via device terminal

#### 19.06.2014 - WIP-Release v0.1.19-alpha

* Updated: Android API version of Neighbouring Cell Information as it was broken
* Added: Local Broadcast Receiver handling OpenCellID data and ensure map markers reflect data
* Added: Cell class added to support modified API neighbouring cell data and simplify operations
* Fixed: Corrected async task calling UI methods, Database backup and restore should work now
* Fixed: Corrected naming value to correctly show our new awesome Icons
* Fixed: Modified MapViewer location logic which previously did not return any data
* Fixed: Fully removed CDMA specific location methods which caused crashes AGAIN for CDMA users

#### 18.06.2014

* Changed: Reverted commits that changed Silent SMS to Flash SMS. we **only** want to detect Silent SMS
* Changed: SMS Broadcast receiver modified to look for true TYPE 0 Stealth SMS messages
* Updated: Modified User Guide to reflect the latest changes and clarifications thereafter
* Added: Additional check for Message type indicator & the TP-PID value
* Added: Yet another additional check added as framework seems to use both in different places
* Fixed: Disabled notification alert once the application becomes visible to address persisting alert

#### 17.06.2014

* Updated: New explanations in User Guide to distinguish between Class 0 and Type 0 SMS
* Added: Link to the NSA’s Secret Role in the U.S. Assassination Program
* Fixed: Corrected code values and namings for Flash SMS in multiple files

#### 16.06.2014

* Updated: Changed naming of Icons within AimsicdService.java
* Added: [SCREENSHOTS](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/tree/master/SCREENSHOTS) for all our needs to explain our App - enjoy the eye candy!
* Added: Initial version of our [official User Guide](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/User-Guide), will be constantly updated
* Fixed: Corrected naming of Icons to reflect the new naming scheme

#### 15.06.2014

* Added: Fresh Icons indicating the new threat levels "MEDIUM", "HIGH" and "RUN!"

#### 14.06.2014 - WIP-Release v0.1.18-alpha

* **ATTENTION**: Backup your Database prior to installing this WIP-Release!
* Added: Database Restore added to repopulate database tables from exported CSV
* Added: Silent SMS Detection - Type 0 SMS will be intercepted and will display a system alert
* Added: OpenCellID, DB Backup & Restore have been added to main async class for better performance
* Fixed: OpenCellID download methods have been rewritten and should function correctly now
* Fixed: Device specific updates which addressed some issues being faced by users with CDMA devices

#### 13.06.2014

* Updated: Major revamp of our README, making it much fresher and structured
* Updated: Reworked our WIKI and instructions on how to correctly submit Issues
* Changed: StyleGuide is now CONTRIBUTING.md - will be shown when opening Issues
* Added: We now accept [ANONYMOUS DONATIONS](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Anonymous-Donations) through [DarkCoin](https://www.darkcoin.io/)! ;-)

#### 27.05.2014 - WIP-Release v0.1.17-alpha

* Updated: Better way of how AIMSICD draws and displays any SIM variable & further error handling
* Added: Initial version of a custom InfoWindow in the Map Viewer to display further details
* Added: Record ID added to the database viewer cards
* Fixed: Reverted an update to the GMS Play Services library to solve issues with the Map Viewer

#### 26.05.2014

* Added: SHA-1 Checksums have been (and will be) added to all (previous) WIP-Releases

#### 21.05.2014 - WIP-Release v0.1.16-alpha

* Updated: Disabling Cell tracking will now also disable Location tracking if enabled
* Updated: Huge code cleanup and updates to a number of areas across a large number of packages
* Removed: Unnecessary compatibility library (positive reduction in size of the APK)
* Added: AT Command Injector (currently disabled until further testing has been completed)
* Added: Code comments to a variety of methods
* Fixed: Adressed numerous possible NPE for better stability and an increased user experience

#### 10.05.2014 - WIP-Release v0.1.15-alpha

* Updated: Map Viewer type selection moved into a dedicated Map preference fragment
* Updated: About Page completed with links functioning correctly
* Updated: Cleaned up source file structure and moved items for a more logical structure
* Fixed: Main layout alignment corrected and page order changed so Cell Information is now page 2
* Fixed: Service Persistence Preferences updated to work with the change of the preference logic
* Fixed: Multiple small issues with some device variables with Network type

#### 06.05.2014 - WIP-Release v0.1.14-alpha

* Added: Neighbouring Cell details shown on Cell Information fragment
* Added: Ciphering Indicator provided through the Samsung MultiRil method
* Added: Initial About-Dialog providing information about AIMSICD

#### 03.05.2014 - WIP-Release v0.1.13-alpha

* Added: Exception handling added to all methods that attempt to draw SIM specific information

#### 03.05.2014 - WIP-Release v0.1.12-alpha

* Updated: MASSIVE UI update implementing fragments to enable easy navigation through SWIPING! ;-)
* Updated: Default icon is now selected within the preferences screen
* Fixed: NPE-Issue corrected through several code improvements

#### 03.05.2014 - WIP-Release v0.1.11-alpha

* Updated: Consolidation of Signal Strength, Data Activity & Data State into one Phone State Listener
* Updated: Main menu updated to only display FemtoCell detection on CDMA devices only
* Added: Requirement to accept our Disclaimer (if you decline you MUST uninstall AIMSICD)
* Added: Support for [NoGAPPS Maps API](http://forum.xda-developers.com/showthread.php?t=1715375) if you don't like to install Google Maps
* Fixed: Map Viewer Tweaks to correct issues with initial location placement and map type preferences


#### 27.04.2014 - WIP-Release v0.1.10-alpha

* Improved: Database Viewer UI tweaked to use a gradient colouring scheme
* Improved: Map Viewer will fall back to a default location based on MCC and the Countries Capital City
* Fixed: Bug with Signal Strength Tracking icon always showing as disabled

#### 25.04.2014 - WIP-Release v0.1.9-alpha

* Added: Ability to view the current database contents using a CardUI type interface
* Added: Access to all current contents including the newly created OpenCellID data table

#### 25.04.2014 - WIP-Release v0.1.8-alpha

* **CAUTION:** This version will erase your existing tracking information! Please backup first.
* Improved: Complete rewrite of Database helper functions for better logic of updating/inserting records
* Changed: Minimum SDK version increased to API 16 (JB 4.1)
* Removed: depreciated methods that were required to support Android versions < JB 4.1
* Fixed: Femtocell Detection preference selection now correctly actioned on start-up

#### 21.04.2014 - WIP-Release v0.1.7-alpha

* Added: Enabled CDMA Femtocell Detection for initial testing. (CDMA devices ONLY!)
* Added: missing resources for Actionbar icons
* Added: GeoLocation class to provides an equation to determine a bounding radius of a given point
* Added: Option to select map type and extended the details
* Fixed: Preference change is now detected correctly by the application showing immediate changes
* Fixed: MapViewer updated to check for Google Play Services
* Fixed: MapViewer will now default to last known location (if one is available) 

#### 11.04.2014 - WIP-Release v0.1.6-alpha

* Changed: Project changed to Gradle Build System to make use of Android Studio
* Added: Google Maps API v2 support to enable new features and gain an API Key
* Fixed: Signal strength data is now correctly overlayed on the map as well
* Fixed: Database export corrected and changed to use OpenCV library

#### 09.04.2014 - WIP-Release v0.1.5-alpha

* Improved: Universal Compatibility achieved via the [Universal Compatibility Commit](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/24)!
* Reduced functionality of AIMSICD until methods are discovered to issue AT commands.
* Improved: AIMSICD now can be installed as user application (no ROOT needed)!
* Improved: AIMSICD should now function on any device. We're open for your feedback!
* Femtocell Detection will be fully implemented for CDMA devices in the next commit by [xLaMbChOpSx](https://github.com/xLaMbChOpSx).

#### 08.04.2014 - WIP-Release v0.1.4-alpha

* Updated: DISCLAIMER to encourage people to talk to us BEFORE hunting our developers
* Updated: CREDITS to reflect latest contributions (please give me a hint if I missed someone)
* Removed: folder 'MERGESOURCE' to clean up unused code and avoid confusion
* Added: [SOURCES](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/SOURCES). Please actively use it. Know where your code comes from.
* Removed: Erased TODO and created WANTED_FEATURES. Hit us with your ideas there!
* Complete revamp of our [PAPERS-Directory](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/tree/master/PAPERS). Make sure to grab your nightly lecture.
* Another new and fresh Iconset has been added with a [pull request](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/20) [SgtObst](https://github.com/SgtObst). Cool! :)
* New [pull request](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/21) adding Service, Boot Completed Receiver, TableView and FemtoCatcher Additions
* We're travelling towards a really awesome App! To everyone developing here: You're doing a WONDERFUL job! THANK YOU!

#### 06.04.2014 - WIP-Release v0.1.3-alpha

* Applause to [xLaMbChOpSx](https://github.com/xLaMbChOpSx)! He submitted a new [pull request](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/14) with a complete rewrite of the menu system, implementing the Actionbar, rewrite of the Device class to perform better and change it from a static class, Persistent notification was added and Database helper class was created to provide application wide support.
* The old Icons of RawPhone have been fully replaced by the great work of [SgtObst](https://github.com/SgtObst). More to come soon!
* Our developers are currently working **hard** to find viable ways to acquire the ciphering info - if you have **good** ideas or code and are a programmer, perticipate in the [official development thread](http://forum.xda-developers.com/showthread.php?t=1422969) (only technical talk).

#### 02.04.2014

* Thanks to the creative work of [SgtObst](https://github.com/SgtObst), we now have FRESH and NEW ICONS for AIMSICD! ;-)
* Added a TODO for collecting the things that are planned to be added in the long run.

#### 31.03.2014 - WIP-Release v0.1.2-alpha

* New [pull request](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/7) by [xLaMbChOpSx](https://github.com/xLaMbChOpSx) reducing the target SDK version and update the code.
* Bookmark our [WIP-RELEASES](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/) and feel free to [report feedback on XDA](http://forum.xda-developers.com/showthread.php?t=1422969).
* New [WIKI-Pages](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki) by [xLaMbChOpSx](https://github.com/xLaMbChOpSx) explaining building and installation. HAVE PHUN! ;-)


#### 27.03.2014

* A few team members reported that they've been crawled by IP adresses that where not connected to any country (probably   secret agencies) and stalked by members of the famous company Rohde & Schwarz (the leading manufacturer of IMSI-Catchers). We know that with our actions we already have attracked forces out there which get their will through more brutal methods. Thus, we added a small [Disclaimer](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/DISCLAIMER). Just to mention it: I always have the latest copy of everything. Enjoy!

#### 25.03.2014 - WIP-Release v0.1.1-alpha

* Progress! We've merged an [Intitial Development Commit](/https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/5) by [xLaMbChOpSx](https://github.com/xLaMbChOpSx). HUGE THANKS!

#### 22.03.2014 - WIP-Release v0.1-alpha

* Added source code of [RawPhone](https://play.google.com/store/apps/details?id=com.jofrepalau.rawphone) into main file tree to start off with. Throw your commits at me! 

#### 21.03.2014

* Ladies and Gentlemen, I am **honored** to announce our [German Article featuring AIMSICD](http://www.kuketz-blog.de/imsi-catcher-erkennung-fuer-android-aimsicd)! ;-)
* THANK YOU, Mike! Bookmark www.kuketz-blog.de - a lot of awesome stuff to discover and learn!
* THANK YOU, [He3556](https://github.com/He3556)! You really are a gold nugget for this project. Keep rockin' with us!

#### 16.03.2014

* Beautified README.md and added [TextSecure](https://github.com/WhisperSystems/TextSecure) to the links worth checking out
* Finalized article for [Kuketz IT-Security Blog](http://www.kuketz-blog.de/), last review to be done by Mike Kuketz
* App Icon + Banner for article and GitHub **WANTED**, ideas welcome in our [Development Thread](http://forum.xda-developers.com/showthread.php?t=1422969)!

#### 10.03.2014
* Added file [CONTRIBUTING](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/CONTRIBUTING.md) to avoid neglected commits - please read it before commiting!

#### 17.02.2014

* Article for [Kuketz IT-Security Blog](http://www.kuketz-blog.de/) by [He3556](https://github.com/He3556) and me almost finished (still needs review)
* Cleanup of GitHub is being prepared in order to add code and commits. Stay tuned!

#### 02.02.2014

* The [EFF](https://www.eff.org/) and [The Guardian Project](https://guardianproject.info/) have been contacted to join our quest
* Mike Kuketz of the [Kuketz IT-Security Blog](http://www.kuketz-blog.de/) confirms article in the works about our project

#### 11.01.2014

* Creation of folder 'MERGESOURCE' for source code of apps to be added to 'AIMSICD'
* Added source code of app '[RawPhone](https://play.google.com/store/apps/details?id=com.jofrepalau.rawphone)' to be used in the base of our own app

#### 23.11.2013

* XDA member '[SecUpwN](http://forum.xda-developers.com/member.php?u=4686037)' is still fire and flame for the project, this GitHub is born
* Added important files of abandoned GitHub-Projects, polished up our own GitHub

#### 27.02.2013

* E:V:A created the [preliminary developer roadmap](http://forum.xda-developers.com/showpost.php?p=38386937&postcount=45) to break down app development
* Clarification of the apps purpose and heavy discussion around IMSI-Catchers starts to emerge

#### 19.01.2013

* Project now actively looking for talented and interested developers to produce the PoC-App

#### 02.01.2012

* Initial creation of the [project thread](http://forum.xda-developers.com/showthread.php?t=1422969) through XDA recognized developer '[E:V:A](http://forum.xda-developers.com/member.php?u=4372730)'
* Active discussion beginns and people start realizing the need for the proposed PoC-App

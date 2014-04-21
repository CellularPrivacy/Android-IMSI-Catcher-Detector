# CHANGELOG of 'AIMSICD'
----------------------

#### 21.04.2014   v0.1.7-alpha

* Added: Enabled CDMA Femtocell Detection for initial testing. (CDMA devices ONLY!)
* Added: missing resources for Actionbar icons
* Fix: MapView updated to check for Google Play Services 
* Added: option to select map type and extended the details (the system will 
  apply to the map including markers for unique CellID's found and signal overlays.)
* Fix: MapView will now default to last known location (if one is available) 
  or will zoom to the last loaded location found in the tracking database, 
  other map controls enabled to allow gesture control.
* Added: GeoLocation class to provides an equation to determine a bounding radius 
  of a given point, for example a circle 50 miles/kilometers around a given location.
* Preference change is now detected correctly by the application showing immediate 
  changes to through the persistent notification for system status (Idle, Good & Alarm).

#### 11.04.2014

* Change: Project changed to Gradle Build System to make use of Android Studio
* Added: Google Maps API v2 support to enable new features and gain an API Key
* Fix: Signal strength data is now correctly overlayed on the map as well
* Fix: Database export corrected and changed to use OpenCV library

#### 09.04.2014

* Improvement: Universal Compatibility achieved via the [Universal Compatibility Commit](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/24)!
* Reduced functionality  of AIMSICD until methods are discovered to issue AT commands.
* Improvement: AIMSICD now can be installed as user application (no ROOT needed)!
* Improvement: AIMSICD should now function on any device. We're open for your feedback!
* Femtocell Detection will be fully implemented for CDMA devices in the next commit by [xLaMbChOpSx](https://github.com/xLaMbChOpSx).

#### 08.04.2014

* Updated Disclaimer to encourage people to talk to us BEFORE hunting our developers
* Updated Credits to reflect latest contributions (please give me a hint if I missed someone)
* Removed folder 'MERGESOURCE' to clean up unused code and avoid confusion
* Created new file: [SOURCES](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/SOURCES). Please actively use it. Know where your code comes from.
* Removed TODO and created [WANTED_FEATURES](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/WANTED_FEATURES). Hit us with your ideas there!
* Complete revamp of our [PAPERS-Directory](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/tree/master/PAPERS). Make sure to grab your nightly lecture.
* Another new and fresh Iconset has been added with a [pull request](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/20) [SgtObst](https://github.com/SgtObst). Cool! :)
* The latest [pull request](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/21) now adds Service, Boot Completed Receiver and TableView & FemtoCatcher Additions. We're travelling towards a really awesome App! To everyone developing here: You're doing a WONDERFUL job! THANK YOU!

#### 06.04.2014

* Applause to [xLaMbChOpSx](https://github.com/xLaMbChOpSx)! He submitted a new [pull request](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/14) with a complete rewrite of the menu system, implementing the Actionbar, rewrite of the Device class to perform better and change it from a static class, Persistent notification was added which can be tweaked down the line and the Database helper class was created to provide application wide support.
* The old Icons of RawPhone have been fully replaced by the great work of [SgtObst](https://github.com/SgtObst). More to come soon!
* Our developers are currently working **hard** to find viable ways to acquire the ciphering info - if you have **good** ideas or code and are a programmer, feel free to perticipate in the discussion on the [official development thread](http://forum.xda-developers.com/showthread.php?t=1422969) (only technical talk).

#### 02.04.2014

* Thanks to the creative work of [SgtObst](https://github.com/SgtObst), we now have FRESH and NEW ICONS for AIMSICD! ;-)
* Added a TODO for collecting the things that are planned to be added in the long run.

#### 31.03.2014

* New [pull request](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/7) by our awesome developer [xLaMbChOpSx](https://github.com/xLaMbChOpSx)! This one reduces the target SDK version and updates the code.
* Curious to get you hands on something to try out? Bookmark our [WIP-RELEASES](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases/) and feel free to [report feedback on XDA](http://forum.xda-developers.com/showthread.php?t=1422969).
* New [WIKI-Pages](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki) have been created by [xLaMbChOpSx](https://github.com/xLaMbChOpSx) to explain building and installation. HAVE PHUN! ;-)


#### 27.03.2014

* A few team members reported that they've been crawled by IP adresses that where not connected to any country (probably   secret agencies) and stalked by members of the famous company Rohde & Schwarz (the leading manufacturer for   
  IMSI-Catchers). We know that with our actions we already have attracked forces out there which get their will through 
  more brutal methods. Thus, we added a small [Disclaimer](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/DISCLAIMER). Just to mention it: I always have the latest copy of everything. Enjoy!

#### 25.03.2014

* Progress! We've merged an [Intitial Development Commit](/https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/pull/5) by [xLaMbChOpSx](https://github.com/xLaMbChOpSx). HUGE THANKS!

#### 22.03.2014

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
* Added a [STYLEGUIDE](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/STYLEGUIDE.md) to avoid neglected commits - please read it before commiting!

#### 17.02.2014
* Initial Development Commit by [xLaMbChOpSx](https://github.com/xLaMbChOpSx), yet to be reviewed and added ;-)
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

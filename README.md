## Android IMSI-Catcher Detector (AIMSICD)

Android-based project to detect and (hopefully one day) avoid fake base stations (IMSI-Catchers) in GSM/UMTS Networks. Sounds cool and security is important to you? Feel free to contribute! ;-)

**German Article about our Project**: [IMSI-Catcher Erkennung für Android – AIMSICD](http://www.kuketz-blog.de/imsi-catcher-erkennung-fuer-android-aimsicd/).

* Grab the [latest WIP-RELEASE of AIMSICD](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases) and make sure to [check out our WIKI](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki)!
* Discussion and constructive criticism: [Official Development Thread on XDA](http://forum.xda-developers.com/showthread.php?t=1422969).
* Before submitting a commit, please carefully read our [Styleguide](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/STYLEGUIDE.md).
* Storage for source code we should add: [MERGESOURCE](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/tree/master/MERGESOURCE), **carefully** follow this [README](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/MERGESOURCE/SOURCES_README).
* Developers will be [rewarded](http://forum.xda-developers.com/showthread.php?p=46957078). You know of a cool crowdfunding service? [Recommend it to us](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/1).
* Not a developer? You can help too! Get the [GSMmap-APP](https://opensource.srlabs.de/projects/mobile-network-assessment-tools/wiki/GSMmap-apk) and submit collected data to the [GSM Security Map](https://www.gsmmap.org/) to enlarge its database for comparison of mobile network protection capabilities!
* Want to know what's boiling under the hood? You're welcome to bookmark our [Changelog](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/CHANGELOG.md).

# Introduction

[![DEF CON 18: Practical Cellphone Spying](http://img.youtube.com/vi/fQSu9cBaojc/0.jpg)](https://www.youtube.com/watch?v=fQSu9cBaojc)

YouTube: DEF CON 18 - Practical Cellphone Spying with Kristin Paget

Unfortunately it seems that IMSI-Catchers have been exponentially popular lately, with an explosion of various "bastards" with governments and criminals all the same, using it. Anyone can now buy an IMSI-Catcher (or build a cheap one on his own). In addition they can all [crack the A5.1-3 encryption](http://www.infosecurity-magazine.com/view/6394/3g-encryption-cracked-in-less-than-two-hours) on the fly! This is why the original author named "E:V:A" started this project. Let's detect and protect against these threats! Never think that you've got "nothing to hide". You'll very likely regret it one day.

* Get scared on YouTube: [How easy it is to clone a phone + call when connected to a femtocell](http://www.youtube.com/watch?v=Ydo19YOzpzU).
* Also, check out this talk by Karsten Nohl and Luca Melette on [28c3: Defending mobile phones](http://youtu.be/YWdHSJsEOck).

#### Curious? Want to know what IMSI-Catchers can look like?

* This picture has been taken during the riots on Taksim Square in Instanbul:

![IMSI-Catcher during the riots on Taksim Square](http://i43.tinypic.com/2i9i0kk.jpg)

* Above example is way too conspicuous and you'll likely never encounter one of these.
* Todays IMSI-Catchers can be [body-worn](http://arstechnica.com/security/2013/09/the-body-worn-imsi-catcher-for-all-your-covert-phone-snooping-needs/), or are hidden inside comfortable Spy-Vehicles:

![Comfort inside IMSI-Catcher vehicle](http://oi42.tinypic.com/16ba4b4.jpg)

* Current IMSI-Catchers can be as **tiny** as the portable [Septier IMSI-Catcher Mini](http://www.septier.com/368.html) now:

![Septier IMSI-Catcher Mini](http://www.septier.com/contentManagment/uploadedFiles/Mini.png)

# Development Roadmap

##### Make an empty "shell" App that:

* a. collects relevant RF related variables using public API calls. (LAC etc)
* b. puts them in an SQLite database
* c. catches hidden SMS's
* d. catches hidden App installations

##### Make another empty "shell" App (or module) that:

* e. opens a device **local** terminal root shell
* f. uses (e.) to connect to the modem AT-Command Processor ATCoP via shared memory interface SHM
* g. displays the results from sent AT commands
* NOTE: This is **crucial** to our project. Please help E:V:A develop a [Native AT Command Injector](http://forum.xda-developers.com/showthread.php?t=1708598)!

##### [Possibly] Make another App that:

* h. use the OTG (USB-host-mode) interface to use FTDI serial cable to interface with another OsmocomBB compatible phone (using Android host as a GUI host)
* i. uses the "[CatcherCatcher](https://opensource.srlabs.de/projects/mobile-network-assessment-tools/wiki/CatcherCatcher)" detector SW on the 2nd phone
* j. can inject fake 2G GSM location data
* k. find out how to access L0-L2 data using the ATCoP connection
* l. use a statistical algorithm (and smart thinking) on the DB data to detect rogue IMSI catchers
* m. combine all of the above (steps h to l) into a BETA App for testing, add more languages
* n. improve BETA app by adding (many more) things like IMSI-Catcher counter measures

### TODO

If you'd like to move something and help us making this App come true, view our [TODO](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/TODO.md).


# Summary (please read carefully!)

### This project: 

* Detects IMSI based device location tracking
* Provides counter measures against device tracking
* Can provide swarm-wise-decision-based cellular service interruption
* Can provide secure wifi/wimax alternative data routes through MESH-like networking
* Detect and prevent remote hidden application installation
* Detect and prevent remote hidden SMS-based SIM attacks
* Prevent or spoof GPS data
* Does NOT secure any data transmissions
* Does NOT prevent already installed rogue application from full access

### Other projects (NOT this one):

* Provide full device encryption
* Provide secure application sand-boxing
* Provide secure data transmission
* Provide firewalls (awesome solution: [AFWall+](https://github.com/ukanth/afwall))

### License

This project is completely licensed under [GPL v3+](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/LICENSE).

### Bug tracker

Found a bug? Please [create an issue here on GitHub](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/new)!
### Disclaimer

For our own safety, here's our [Disclaimer](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/DISCLAIMER). In short terms: Think before you act! We're untouchable.

# Credits & Greetings

Our project would not have been possible without [these awesome people](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/CREDITS). HUGE THANKS! ;-)

This list will be updated as our project evolves and shall be included within the final app.

### Websites about security worth checking out:

* [Smartphone Attack Vector](http://smartphone-attack-vector.de/) - Smartphone flaws and countermeasures
* [Kuketz IT-Security Blog](http://www.kuketz-blog.de/) - Great Security Reviews (written in German)
* [PRISM Break](https://prism-break.org/) - Alternatives to opt out of global data surveillance
* [The Guardian Project](https://guardianproject.info/) - Secure Open Source Mobile Apps
* [Security Research Labs](https://srlabs.de/) - Stunning Security Revelations made in Berlin
* [The Surveillance Self-Defense Project](https://ssd.eff.org/) - Defend against the threat of surveillance
* [Electronic Frontier Foundation](https://www.eff.org/) - nonprofit organization defending civil liberties in the digital world
* [TextSecure](https://github.com/WhisperSystems/TextSecure) - Secure text messaging application for Android (replace WhatsApp)
* [RedPhone](https://github.com/WhisperSystems/RedPhone) - encrypted voice calls for Android
* [KillYourPhone](http://killyourphone.com) - make your own signal blocking phone pouch super fast for little money

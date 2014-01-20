Android-based project to detect and (hopefully one day) prevent fake base stations (IMSI-Catchers) in GSM/UMTS Networks. Sounds cool and security is important to you? Feel free to visit our [OFFICIAL DEVELOPMENT THREAD ON XDA](http://forum.xda-developers.com/showthread.php?t=1422969) and contribute! Not a developer? Don't worry, you can help too. Get the APP [GSMmap-APK](https://opensource.srlabs.de/projects/mobile-network-assessment-tools/wiki/GSMmap-apk) and submit collected data to the [GSM Security Map](https://www.gsmmap.org/) in order to enlarge its database for comparison of mobile network protection capabilities!

Found some source code of an app you think is important to add? Contribute it [here](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/tree/master/MERGESOURCE), but **please** carefully follow this [README](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/MERGESOURCE/SOURCES_README).

[![DEF CON 18: Practical Cellphone Spying](http://img.youtube.com/vi/fQSu9cBaojc/0.jpg)](https://www.youtube.com/watch?v=fQSu9cBaojc)

YouTube: DEF CON 18 - Practical Cellphone Spying with Kristin Paget

# Introduction

Unfortunately it seems that IMSI-Catchers have been exponentially popular lately, with an explosion of various "bastards" with governments and criminals all the same, using it. Anyone can now buy an IMSI-Catcher (or build a cheap one on his own). In addition they can all crack the A5.1-3 encryption on the fly! This is why the original author named "E:V:A" started this project. Let's detect and protect against threats like these! Never think that you've got "nothing to hide". You'll very likely regret it one day.

* Scary side note on YouTube: [How easy it is to clone a phone + call when connected to a femtocell](http://www.youtube.com/watch?v=Ydo19YOzpzU).
* Also, check out this talk by Karsten Nohl and Luca Melette on [28c3: Defending mobile phones](http://youtu.be/YWdHSJsEOck).

![IMSI-Catcher during the riots on Taksim Square](http://i43.tinypic.com/2i9i0kk.jpg)

### Credits & Greetings

Our project would not have been possible without [these awesome people](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/CREDITS). HUGE THANKS! ;-)

This list will be updated as our project evolves and shall be included within the final app.


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
* NOTE: This part is **crucial** to our project. Please help E:V:A to develop a [Native AT Command Injector](http://forum.xda-developers.com/showthread.php?t=1708598)!

##### [Possibly] Make another App that:

* h. use the OTG (USB-host-mode) interface to use FTDI serial cable to interface with another OsmocomBB compatible phone (using Android host as a GUI host)
* i. uses the "[CatcherCatcher](https://opensource.srlabs.de/projects/mobile-network-assessment-tools/wiki/CatcherCatcher)" detector SW on the 2nd phone
* j. can inject fake 2G GSM location data
* k. find out how to access L0-L2 data using the ATCoP connection
* l. use a statistical algorithm (and smart thinking) on the DB data to detect rogue IMSI catchers
* m. combine all of the above (steps h to l) into a BETA App for testing, (maybe) add other languages
* n. improve BETA app by adding (many more) things like IMSI-Catcher counter measures

**Further ideas**: Add option to make app device administrator, maybe also use ROOT and the [XPosed Framework](http://forum.xda-developers.com/showthread.php?t=1574401).

# Summary (please read carefully!)

### This project: 

* Detects IMSI based device location tracking
* Provides counter measures for device tracking
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
* Provide firewalls

### Websites about security worth checking out:

* [Smartphone Attack Vector](http://smartphone-attack-vector.de/) - Smartphone flaws and countermeasures
* [Kuketz IT-Security Blog](http://www.kuketz-blog.de/) - Great Security Reviews (written in German)
* [PRISM Break](https://prism-break.org/) - Alternatives to opt out of global data surveillance
* [The Guardian Project](https://guardianproject.info/) - Secure Open Source Mobile Apps
* [Security Research Labs](https://srlabs.de/) - Stunning Security revelations made in Berlin

  
DEVELOPERS are VERY WELCOME and will be [REWARDED](http://forum.xda-developers.com/showthread.php?p=46957078).

You know of a cool crowdfunding service? [Recommend it to us](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/1)!

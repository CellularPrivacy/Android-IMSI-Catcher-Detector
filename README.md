Android-based project to detect and (hopefully one day) prevent fake base stations (IMSI-Catchers) in GSM/UMTS Networks.
Project Website: http://secupwn.github.io/Android-IMSI-Catcher-Detector/

Official XDA development thread: http://forum.xda-developers.com/showthread.php?t=1422969

Introduction
============

Unfortunately it seems that IMSI catchers have been exponentially popular lately, with an explosion of various "bastards" with governments and 
criminals all the same, using it. Anyone can now buy an IMSI catcher... In addition they can all crack the A5.1-3 encryption on the fly!
This is why the original author named "E:V:A" started this project. Let's protect against threats like these!


![Riots on Taksim Square](http://i43.tinypic.com/2i9i0kk.jpg "IMSI Catcher during the Riots on Taksim Square")

Credits & Greetings
===================

https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/CREDITS

(list will be updated as project evolves and shall be included within the final app)

Development Roadmap
====================

##### Make an empty "shell" App that:
* a. collects relevant RF related variables using public API calls. (LAC etc)
* b. puts them in an SQLite database
* c. catches hidden SMS's
* d. catches hidden App installations

##### Make another empty "shell" App (or module) that:
* e. opens a device **local** terminal root shell
* f. uses (e.) to connect to the modem AT-Command Processor) ATCoP via shared memory interface SHM on those devices that uses that (Samsung) etc. 
* g. displays the results from sent AT commands

##### [Possibly] Make another App that:

* h. use the OTG (USB-host-mode) interface to use and FTDI serial cable to interface with another OsmocomBB compatible phone (use Android host as an GUI host)
* i. uses the "catchercatcher" detector SW on the 2nd phone
* j. can inject fake 2G GSM location data

* Find out how to access L0-L2 data using the ATCoP connection
* Use a statistical algorithm (and smart thinking) on the DB data to detect rogue IMSI catchers
* Combine all of the above (steps h to j) into a BETA App for testing, add other languages
* Improve Beta app by adding (many more) things like IMSI catcher counter measures

Further ideas: Add option to make app device administrator, maybe also use the XPosed Framework

Summary (please read carefully)
===============================

### This project: 
* Detects IMSI based device location tracking
* Provides counter measures for device tracking
* Can provide swarm-wise-decision-based cellular service interruption
* Can provide secure wifi/wimax alternative data routes through MESH-like networking.
* Detect and prevent remote hidden application installation
* Detect and prevent remote hidden SMS-based SIM attacks
* Prevent or spoof GPS data
* Does NOT secure any data transmissions
* Does NOT prevent already installed rogue application from full access

### Other projects:
* Provide full device encryption
* Provide secure application sand-boxing
* Provide secure data transmission
* Provide firewalls

                                        DEVELOPERS ARE VERY WELCOME! ;-)


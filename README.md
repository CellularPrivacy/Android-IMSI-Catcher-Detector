## Android IMSI-Catcher Detector (AIMSICD)
#### [alternative name: `IMSI-Cure` - pronounced `I'm-Secure`]

Android-based project to detect and (hopefully one day) avoid fake base stations ([IMSI-Catchers](https://en.wikipedia.org/wiki/IMSI-catcher)) in GSM/UMTS Networks. Sounds cool and security is important to you? Feel free to contribute! ;-)

**German Article about our Project**: [IMSI-Catcher Erkennung für Android – AIMSICD](http://www.kuketz-blog.de/imsi-catcher-erkennung-fuer-android-aimsicd/).

[![AIMSICD-Status](https://cloud.githubusercontent.com/assets/6475465/3053970/e50b28c8-e1b1-11e3-887a-e1b82a21f166.png)](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/User-Guide#status-icons-and-threat-levels)

---

**[DONATE](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Anonymous-Donations) to help us becoming strong supporters of:**

[![EFF](https://www.eff.org/sites/all/themes/frontier/images/logo_full.png)](https://www.eff.org/)
[![Guardian Project](https://guardianproject.info/wp-content/uploads/2013/09/cropped-GP_logo+txt_hires_black_on_trans_header.png)](https://guardianproject.info/)
[![Privacy International](https://www.privacyinternational.org/profiles/pi/themes/custom/privacy/logo.png)](https://www.privacyinternational.org/)

---

#  Index

* [Introduction](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector#introduction)
* [IMSI-Catchers](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector#curious-want-to-know-what-imsi-catchers-can-look-like)
* [Roadmap](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector#development-roadmap)
* [Goals](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector#this-project)
* [Limitations](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector#other-projects-not-this-one)
* [Disclaimer](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/DISCLAIMER)
* **[WIP-Releases](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases)**
* [Installation](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/How-to-install-the-AIMSICD)
* [User Guide](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/User-Guide)
* [Changelog](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/CHANGELOG.md)
* [Discussion](http://forum.xda-developers.com/showthread.php?t=1422969)
* [Build](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/How-to-build-the-AIMSICD)
* [Contributing](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/CONTRIBUTING.md)
* [Support](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector#support)
* [Bugs](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/How-to-correctly-submit-Issues)
* [Wiki](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki)
* [Sources](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/SOURCES)
* [Credits](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector#credits--greetings)
* [Contact](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector#get-in-touch-with-the-core-team)
* [License](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/README.md#license)
* [More Security](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector#websites-about-security-worth-checking-out)

---

# Introduction

Both law enforcement agencies and criminals use [IMSI-Catchers](https://en.wikipedia.org/wiki/IMSI-catcher), which are false mobile towers acting between the target mobile phone(s) and the service providers real towers. As such it is considered a Man In the Middle (MITM) attack. The FBI or local police might deploy the device at a protest to obtain a record of everyone who attended with a cell phone. In the USA this technology is known under the name "[Stingray](https://en.wikipedia.org/wiki/Stingray_phone_tracker)", which is even capable to track the people who are traveling together with an owner of a targeted phone across the country. [Here](http://www.pgsup.com/wp-content/uploads/2010/06/P4230059.jpg) you can see alleged Stingray tracking devices mounted to the roof of three SUVs. IMSI-Catchers can allow adversaries to intercept your conversations, text messages, and data. Police can also use them to determine your location, or to find out who is in a given geographic area at what time. Identity thieves might sit with an IMSI-Catcher in a parked car in a residential neighborhood, stealing passwords or credit card information from people nearby who make purchases on their phones. All of this surveillance happens in secret.

Powerful, expensive IMSI-Catchers are in use at federal agencies and some police departments. And if you think that IMSI-Catchers are not used in your own town, think twice! If you ever happen to be near a riot or demonstration (hint: leave you phone at home if participating), pay close attention to cars standing along the path of the demonstration - those might be IMSI-Catchers. It is common practice for police to position IMSI-Catchers at the beginning as well as the end of roads where the demonstrating crowd moves to capture and compare data in order to find out who participated. But most of the time IMSI-Catchers are well hidden and can be even [body-worn](http://arstechnica.com/security/2013/09/the-body-worn-imsi-catcher-for-all-your-covert-phone-snooping-needs/) - therefore you won't even discover these creepy devices. Current technology shrinks them to be as tiny as your phone! So again, if you really have to participate in a riot or demonstration, don't be a dumbass and leave your phones at home. Or, at the very least, build yourself a [signal blocking phone pouch](http://killyourphone.com/)!

[![DEF CON 18: Practical Cellphone Spying](http://img.youtube.com/vi/fQSu9cBaojc/0.jpg)](https://www.youtube.com/watch?v=fQSu9cBaojc)

YouTube: DEF CON 18 - Practical Cellphone Spying with Kristin Paget

Unfortunately it seems that IMSI-Catchers have been exponentially popular lately, with an explosion of various "bastards" with governments and criminals all the same, using it. Anyone can now buy an IMSI-Catcher (or build a cheap one on his own). Sending spam and phishing SMS via fake base stations is already a lucrative underground market, particularly in Russia, China and Brazil (see [The Mobile Cybercriminal Underground Market in China](http://www.trendmicro.com/cloud-content/us/pdfs/security-intelligence/white-papers/wp-the-mobile-cybercriminal-underground-market-in-china.pdf)). For example in China, 1.530 people got arrested for using [this kind of equipment](http://www.ecns.cn/business/2014/03-26/106525.shtml). Just recently, hackers decided to start [reverse-engineering the NSA toolset](http://heise.de/-2235339) and are releasing tools like [TWILIGHTVEGETABLE](http://www.nsaplayset.org/twilightvegetable) - an easy to use, boot and pwn toolkit for passive monitoring of GSM communications. It's just a matter of time of when your own neighbor will spy on you with simple self-build tools!

In addition, they can all crack A5/1 encryption which is most commonly used for GSM traffic on the fly (passively)! Only the latest A5/3 encryption which is used for securing mobile data (4G and 3G) and is offered as [new security standard for GSM encryption](http://www.telekom.com/media/company/210108) remains secure in practice while susceptible to theoretical attacks. Although A5/3 withstands passive eavesdropping, it can be bypassed by deploying an IMSI-Catcher which can force a mobile device into 2G mode and [downgrade then the encryption to A5/1 or disable it](http://www.septier.com/149.html).

There are almost no phones on the market which offer an option to check what kind of encryption is used to secure GSM traffic. And although the [Issue of not having a convenient display of the Ciphering Indictor](https://code.google.com/p/android/issues/detail?id=5353) has been assigned to Google since 2009, it seems they're getting paid (or are forced to) blatantly ignoring it. The only way to protect a mobile device from downgrade attacks is to disable 2G if this option is available. In this case the phone will not be able to receive or make calls in areas without 3G coverage. This is why the original author named "E:V:A" started this project. **Let's detect and protect against these threats!** Never think that you've got "nothing to hide". You'll very likely regret it one day.

* [NSA’s Secret Role in the U.S. Assassination Program](https://firstlook.org/theintercept/article/2014/02/10/the-nsas-secret-role/)
* Scary YouTube-Video: [How easy it is to clone phones](http://www.youtube.com/watch?v=Ydo19YOzpzU).
* Talk by Karsten Nohl and Luca Melette on [28c3: Defending mobile phones](http://youtu.be/YWdHSJsEOck).

---

#### Curious? Want to know what IMSI-Catchers can look like?

* Current IMSI-Catchers can be as **tiny** as the portable [Septier IMSI-Catcher Mini](http://www.septier.com/368.html) now:
* Below, the smartphone takes up the most space. IMSI-Catchers will even get smaller!

![Septier IMSI-Catcher Mini](http://www.septier.com/contentManagment/uploadedFiles/Mini.png)

* This picture has been taken during the riots on Taksim Square in Instanbul:

![IMSI-Catcher during the riots on Taksim Square](http://i43.tinypic.com/2i9i0kk.jpg)

* Above example is way too conspicuous and you'll likely never encounter one of these.
* Todays IMSI-Catchers can be [body-worn](http://arstechnica.com/security/2013/09/the-body-worn-imsi-catcher-for-all-your-covert-phone-snooping-needs/), or are hidden inside comfortable Spy-Vehicles:

![Comfort inside IMSI-Catcher vehicle](http://oi42.tinypic.com/16ba4b4.jpg)

---

# Development Roadmap

Below structure does NOT mean we will create 3 Apps. It will be "1 App to Rule Them ALL".

##### Make an empty "shell" App that:

* a. collects relevant RF related variables using public API calls. (LAC etc)
* b. puts them in an SQLite database
* c. catches hidden SMS's
* d. catches hidden App installations

##### Make another empty "shell" App (or module) that:

* e. opens a device **local** terminal root shell
* f. uses (e.) to connect to the modem AT-Command Processor ATCoP via shared memory interface SHM
* g. displays the results from sent AT commands
* **CRUCIAL** to our project: Please help E:V:A develop a [Native AT Command Injector](http://forum.xda-developers.com/showthread.php?t=1708598)!

##### [Possibly] Make another App that:

* h. use the OTG (USB-host-mode) interface to use FTDI serial cable to interface with another OsmocomBB compatible phone (using Android host as a GUI host)
* i. uses the "[CatcherCatcher](https://opensource.srlabs.de/projects/mobile-network-assessment-tools/wiki/CatcherCatcher)" detector SW on the 2nd phone
* j. can inject fake 2G GSM location data
* k. find out how to access L0-L2 data using the ATCoP connection
* l. use a statistical algorithm (and smart thinking) on the DB data to detect rogue IMSI catchers
* m. combine all of the above (steps h to l) into a BETA App for testing, add more languages
* n. improve BETA app by adding (many more) things like IMSI-Catcher counter measures

---

# Goals (please read carefully!)

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
* Aims to be added to the [Guardian Project's list of secure Apps](https://guardianproject.info/apps)
* Aims to be recommended by the [SSD Project of the Electronic Frontier Foundation](https://ssd.eff.org/)

### Other projects (NOT this one):

* Provide full device encryption
* Provide secure application sand-boxing
* Provide secure data transmission
* Provide firewalls (awesome solution: [AFWall+](https://github.com/ukanth/afwall))

---

### Disclaimer

For our own safety, here's our [Disclaimer](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/DISCLAIMER). In short terms: Think before you act! We're untouchable.

---

### Bug Tracker

Found a bug? Please carefully follow our guide on [how to correctly submit Issues](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/How-to-correctly-submit-Issues)!

---

# Support

Although this project is fully Open Source, developing AIMSICD is a lot of work and done by enthusiastic people during their free time. If you're a developer yourself, we welcome you with open arms! To keep developers in a great mood and support development, please consider making a fully anonymous donation through sending [DarkCoin](https://www.darkcoin.io/) to our new OFFICIAL DONATION ADDRESS: **XxEJvrYtkTZzvMUjtbZwPY34MyCGHSu4ys**

All collected donations will be split into appropriate pieces and directly sent to developers who contribute useful code. The amount of DarkCoins each developer receives will vary with the value of each merged commit. To be perfectly clear: We will **NOT** reward junk, only awesome stuff. Furthermore, donations will be used to support the [Electronic Frontier Foundation](https://www.eff.org/), [The Guardian Project](https://guardianproject.info/) as well as [Privacy International](https://www.privacyinternational.org/). If you are unsure how to donate, visit our WIKI-Page on [Anonymous Donations](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Anonymous-Donations).

---

### License

This project is completely licensed under [GPL v3+](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/LICENSE).

---

# Credits & Greetings

Our project would not have been possible without [these awesome people](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/CREDITS). HUGE THANKS! ;-)

This list will be updated as our project evolves and shall be included within the final app.

---

### Get in touch with the core team!

|                   Task                  |                              Developer                              |
|:---------------------------------------:|:-------------------------------------------------------------------:|
|      Project Inventor and smarthead     |                  [E:V:A](mailto:xdae3v3a@gmail.com)                 |
|      Lead developer and code-monkey     | [xLaMbChOpSx](http://forum.xda-developers.com/member.php?u=4661001) |
| Attack vectors & vulnerability-analyzer |    [He3556](http://forum.xda-developers.com/member.php?u=4600707)   |
|         Leading graphical designer      |   [Sgt-Obst](http://forum.xda-developers.com/member.php?u=5102584)  |
| Security enthusiast and Public Speaker  |   [SecUpwN](http://forum.xda-developers.com/member.php?u=4686037)   |

---

### Websites about security worth checking out:

* [Smartphone Attack Vector](http://smartphone-attack-vector.de/) - Smartphone flaws and countermeasures
* [Kuketz IT-Security Blog](http://www.kuketz-blog.de/) - Great Security Reviews (written in German)
* [PRISM Break](https://prism-break.org/) - Alternatives to opt out of global data surveillance
* [The Guardian Project](https://guardianproject.info/) - Secure Open Source Mobile Apps
* [Security Research Labs](https://srlabs.de/) - Stunning Security Revelations made in Berlin
* [The Surveillance Self-Defense Project](https://ssd.eff.org/) - Defend against the threat of surveillance
* [Electronic Frontier Foundation](https://www.eff.org/) - Nonprofit organization defending your rights in the digital world
* [Privacy International](https://www.privacyinternational.org/) - Charity ommitted to fighting for the right to privacy across the world
* [TextSecure](https://github.com/WhisperSystems/TextSecure) - Secure text messaging application for Android (replace WhatsApp)
* [RedPhone](https://github.com/WhisperSystems/RedPhone) - Encrypted voice calls for Android
* [KillYourPhone](http://killyourphone.com) - Make your own signal blocking phone pouch super fast for little money
* [GSM-Map](http://gsmmap.org/) - Compares the protection capabilities of mobile networks (contribute data!)
* [Datenschmutz Wiki](https://www.datenschmutz.de) - Awesome German wiki of databases and tools of law enforcements

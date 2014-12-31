### Android IMSI-Catcher Detector (AIMSICD)

[![Build Status](https://travis-ci.org/SecUpwN/Android-IMSI-Catcher-Detector.svg)](https://travis-ci.org/SecUpwN/Android-IMSI-Catcher-Detector) [![Development Status](http://img.shields.io/badge/Development_Status-ALPHA-brightgreen.svg)](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Development-Status) [![GooglePlay](http://img.shields.io/badge/GooglePlay-NOT%20SUPPORTED-brightgreen.svg)](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/FAQ) [![CoverityScan](https://scan.coverity.com/projects/3346/badge.svg)](https://scan.coverity.com/projects/3346)
--
Android-based project to detect and avoid fake base stations ([IMSI-Catchers](https://en.wikipedia.org/wiki/IMSI-catcher)) in GSM/UMTS Networks. Feel free to read the [Press Releases](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Press-Releases) about us, spread the word with our [Media Material](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Media-Material) and help us solving [current challenges](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues)!

---

[![Aptoide](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/raw/master/MISC/external/Aptoide.png)](http://aimsicd.store.aptoide.com/ "NOTE: Installs Aptoide-App first!")  [![GitHub](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/raw/master/MISC/external/GitHub.png)](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases "GitHub Releases") [![F-Droid](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/raw/master/MISC/external/F-Droid.png)](https://f-droid.org/repository/browse/?fdid=com.SecUpwN.AIMSICD "F-Droid Store") [![XDA](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/raw/master/MISC/external/XDA.png)](http://www.xda-developers.com/android/detect-avoid-imsi-catcher-attacks-with-imsi-catcher-detector/ "Portal Post on XDA-Developers") 

---

[![AIMSICD-Banner](https://raw.githubusercontent.com/SecUpwN/Android-IMSI-Catcher-Detector/master/PROMOTION/AIMSICD-Banner_Large.png)](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Status-Icons)

---

[![AIMSICD-Teaser](https://raw.githubusercontent.com/SecUpwN/Android-IMSI-Catcher-Detector/master/PROMOTION/AIMSICD-Teaser.png)](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki)

---

#  Index
* [Introduction](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector#introduction)
* [IMSI-Catchers](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector#want-to-know-what-imsi-catchers-look-like)
* [Project Goals](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector#application-goals-please-read-carefully)
* [Limitations](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector#this-project-does-not)
* [Roadmap](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector#development-roadmap)
* **[DOWNLOADS](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases)**
* [Requirements](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Requirements)
* [Installation](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Installation)
* [User Guide](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki)
* [Bugs](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Submitting-Issues)
* [FAQ](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/FAQ)

---

# Introduction

[IMSI-Catchers](https://en.wikipedia.org/wiki/IMSI-catcher) are creepy devices used by both law enforcement agencies and criminals. They are false mobile towers (base stations) acting between the target mobile phone(s) and the real towers of service providers. As such they are considered a Man-In-The-Middle (MITM) attack. This specific MITM attack was [patented](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/raw/master/PAPERS/Technical/%5BGER%5D-Eavesdropping_Method_Patent.pdf) and first commercialized by [Rohde & Schwarz](https://en.wikipedia.org/wiki/Rohde_%26_Schwarz) in 2003, although it would be hard to maintain such a patent, since in reality it is just a modified cell tower with a malicious operator. On 24 January 2012, the Court of Appeal of England and Wales held that the patent is [invalid for obviousness](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/raw/master/PAPERS/Technical/%5BENG%5D-Judgment_Invalid_R%26S_Patent.pdf). But ever since it was first invented, the technology has been used and "improved" by many different companies around the world. Other manufacturers (like Anite) prefer to refer to this spying and tracking equipment in glossy brochures using cozy marketing words like "[Subscriber Trackers](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/raw/01440ab399c929e0bedd5ab9603314855584528c/PAPERS/SysInfos/%5BENG%5D-Triton_Surveillance_Brochure.pdf)". IMSI-Catcher manufacturers are abusing your mind by disguising their spying products as "[life saving equipment](http://www.septier.com/276.html)". Their real purpose is **surveillance** and to **[kill people](https://firstlook.org/theintercept/article/2014/02/10/the-nsas-secret-role/)**. Don't get fooled by their unobtrusive product naming or heart-wrenching stories that may go along with it!

In the USA the IMSI-Catcher technology is known under the name "[StingRay](https://en.wikipedia.org/wiki/Stingray_phone_tracker)", often [mounted](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/raw/master/DOCUMENTATION/IMSI-Catchers/StingRays.jpg "Have a close look on the roof of the cars...") out of sight on top of cars. The FBI or local police regularly deploys IMSI-Catchers hidden in vehicles at a protest to obtain a record of everyone who attended with a cell phone (leave your phones at home by all means if you really have to attend). IMSI-Catchers also allow adversaries to intercept your conversations, text messages, and data. Police can use them to determine your location, or to find out who is in a given geographic area at what time. Identity thieves can use freely [available tools](http://www.nsaplayset.org/) to monitor GSM communications from a parked car in your residential neighborhood, stealing passwords or credit card information from people nearby who make purchases on their phones.

[![Vehicular Intercom System](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/raw/master/DOCUMENTATION/IMSI-Catchers/VehicularIntercomSystem.jpg "Comfortable spying in a so-called 'Vehicular Intercom System'")](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector)

Why are IMSI-Catchers exponentially [popular lately](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki#some-attacks-imsi-catchers-are-capable-of)? Because without our App you would not even notice that you're under attack! IMSI-Catchers, StingRays, GSM/UMTS Interceptors (or whichever names they invent) are perfectly stealth spying devices, crafted from the wet dreams of bastards like governments and criminals all alike. You think you have "[nothing to hide](https://en.wikipedia.org/wiki/Nothing_to_hide_argument)"? Think again! Anyone can now buy an IMSI-Catcher or [build a cheap one](http://www.engadget.com/2010/07/31/hacker-intercepts-phone-calls-with-homebuilt-1-500-imsi-catcher/) on his own. Sending spam and phishing SMS via fake base stations is already a lucrative [underground market](http://www.trendmicro.com/cloud-content/us/pdfs/security-intelligence/white-papers/wp-the-mobile-cybercriminal-underground-market-in-china.pdf)), particularly in Russia, [China](http://www.ecns.cn/business/2014/03-26/106525.shtml) and Brazil. In addition, all IMSI-Catchers can crack A5/1 encryption, which is most commonly used for GSM traffic, on the fly (passively)! A5/3 encryption which is used for securing 3G and is offered as [new security standard for GSM encryption](http://www.telekom.com/media/company/210108) remains secure in practice while susceptible to theoretical attacks. Although 3G and 4G offer sufficient protection from eavesdropping, the security measures can be bypassed by IMSI-Catchers forcing a mobile device into 2G mode and [downgrade encryption to A5/1 or disable it](http://www.septier.com/149.html). For further reading on the algorithms, check out the [Cryptome GSM Files](http://cryptome.org/gsm-a5-files.htm).

There are almost no phones on the market which offer an option to check what kind of encryption is used to "secure" GSM traffic (which is in fact already broken beyond repair). The ones you may find are very expensive and not open source. And although the [Issue of not having a convenient display of the Ciphering Indicator](https://code.google.com/p/android/issues/detail?id=5353) has been assigned to Google since 2009, it seems they're getting paid (or are forced to) blatantly ignoring it. The open source project "[Android-CipheringIndicator-API](https://github.com/PrivacyCollective/Android-CipheringIndicator-API)" aims to craft an API which fixes this Issue and merge the resulting API into the Android AOSP branch. But currently, the only way to protect a mobile device from downgrade attacks is to disable 2G if this option is available. In this case, the phone will not be able to receive or make calls in areas without 3G coverage. This is why we started development on this App: **To protect YOU.** Join our development, even the smallest pull requests are very welcome!

---

#### Want to know what IMSI-Catchers look like?

They come in uncountable shapes and sizes:

[![IMSI-Catchers](https://raw.githubusercontent.com/SecUpwN/Android-IMSI-Catcher-Detector/master/PROMOTION/IMSI-Catchers.png "IMSI-Catchers ready to spy on you")](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector)

* Current IMSI-Catchers can be as **tiny** as the portable [Septier IMSI-Catcher Mini](http://www.septier.com/368.html).

* The smartphone takes up the most space. IMSI-Catchers will even get smaller!

[![Septier IMSI-Catcher Mini](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/raw/master/DOCUMENTATION/IMSI-Catchers/IMSI-Catcher_Mini.png "Israeli IMSI-Catcher company Septier calls it 'GUARDIAN'")](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector)

* Below photograph has been taken during the riots on Taksim Square in Instanbul.
* Note: It is way too conspicuous and you'll likely never encounter one of these.

[![Taksim Square](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/raw/master/DOCUMENTATION/IMSI-Catchers/IMSI-Catcher_TaksimSquare.jpg "IMSI-Catcher during the riots on Taksim Square")](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector)

* Todays IMSI-Catchers can be [body-worn](http://arstechnica.com/security/2013/09/the-body-worn-imsi-catcher-for-all-your-covert-phone-snooping-needs/) or are hidden in GSM Interceptor vehicles:

[![IMSI-Catcher Vehicle](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/raw/master/DOCUMENTATION/IMSI-Catchers/CellularMonitoringVehicle.jpg "Inside an IMSI-Catcher Van")](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector)

Search for "GSM Interceptor", "IMSI-Catcher", "StingRay" or any combination thereof.

---

### Application Goals (please read carefully!)

* Detect IMSI based device location tracking
* Detect and prevent the use of false BTS towers used for illegal interception
* Detect and prevent the use of broken ciphering algorithms (A5/1) during calls
* Detect and prevent remote hidden application installation
* Detect and prevent remote hidden SMS-based SIM attacks
* Provide counter measures against tracking
* Prevent leakage of sensitive GPS data
* Provide swarm-wise-decision-based cellular service interruption
* Provide secure wifi/wimax alternative data routes through MESH-like networking
* Aims to be recommended and added to the [Guardian Project's list of secure Apps](https://guardianproject.info/apps)
* Aims to be recommended by the [SSD Project of the Electronic Frontier Foundation](https://ssd.eff.org/)
* Aims to be recommended by [Privacy International](https://www.privacyinternational.org/) (and like-minded organizations)

##### This project does NOT:

* Provide full device encryption
* Provide secure data transmission (VPN, [Tor](https://www.torproject.org/))
* Provide secure phone calls (we recommend: [RedPhone](https://github.com/WhisperSystems/RedPhone))
* Provide secure SMS (we recommend: [TextSecure](https://github.com/WhisperSystems/TextSecure))
* Provide secure application sand-boxing
* Provide application permission control (we recommend: [XPrivacy](http://forum.xda-developers.com/xposed/modules/xprivacy-ultimate-android-privacy-app-t2320783))
* Provide firewalls (we recommend: [AFWall+](https://github.com/ukanth/afwall))
* Prevent already installed rogue applications from full access and spying
* Provide ROOT and remove bloatware (we recommend: search [XDA](http://www.xda-developers.com/))

---

# Development Roadmap

In order to accomplish the goals set above, we'll need to overcome some of the deeply worrying and unfounded AOS limitations, as imposed by Googles API, in regard to relevant network variables and data. These include highly relevant and important things such as displaying the SIM/phone Ciphering Indicator, which tells you if your calls are being encrypted or not. This has been a required 3GPP feature for the last 15 years, but which Google and most Mobile Network providers have choosen to mostly ignore, although it has been [requested by users since 2009](https://code.google.com/p/android/issues/detail?id=5353). Another is finding the *Timing Advance* (TA) and various Network Timers, like those used in *Radio Resource Control* ([RRC](http://en.wikipedia.org/wiki/Radio_Resource_Control)), that can give very useful information regarding the status of the connections your phone is making.

All this can be fairly easily accomplished, given that we can have access to some of the lower level radio related information coming from the *Baseband Processor* (BP). But that is exactly our challenge. All the software and information about the interfaces providing this, is hidden from the user and developers by a huge amount of proprietary OEM *Non Disclosure Agreements* (NDA). But in the last years, there has been great progress in reverse enginering these protocols and interfaces. The use of these open source tools are the basis of our successful development of this App. 

**To summarize the main stages of this development:**

**A.** Using all available network data, implement the correct detection matrix consisting of a number of items, that each participate in detection of abnormal or abusive network bahaviour. This is the application *[Beta](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/README.md#beta-stage)* stage. 

**B.** Using all possible interfaces to obtain the many variables in (A). These interfaces include:
 - QMI/Sahara protocols for using on Qualcomm based devices (*Gobi3000, qmilib*)
 - Samsung IPC protocol for using on Intel XMM (XGOLD) based devices (*xgoldmon, Replicant*)
 - Direct use of AOS standard RIL interfaces (*/dev/rild* and */dev/rild-debug*)
 - SIM ICC interface for accessing SIM EF filesystem to provide deep access (*SEEK*)
 - Scraping *Service Mode* menus for relevant radio info
 - Scrape `logcat -b radio` for relevant radio info 
 - Use AT Command Processor (ATCoP) interface to get/set network parameters/bahaviour

**C.** Make (A) and (B) transparent across as many Android devices as possible.

##### ALPHA stage:

Make a baseline App that contains the basic functionality for collecting and presenting all available network variables and the detection results.

* a. Collects relevant RF related variables using public AOS API calls. (LAC, CID, TA etc)
* b. Collects detailed BTS information from a pulic database such as *OpenCellID* or *Mozilla Location Services*
* c. Save everything in our SQLite database
* d. Detect hidden/silent (Type-0) SMS's
* e. Detect hidden App installations (Googles INSTALL/REMOVE_ASSET)

##### BETA stage:

Improve ALPHA for leveraging and tune our detection matrix/algorithm.
* f. Implement **any** of the detection schemes we have
* g. Implement **any** of the interfaces in (**B**) 
* h. Test AIMSICD in a real IMSI-catcher environment 
* i. Fine-tune our detection matrix
* j. Implement our first counter interception measures
* k. Planning alternative data routes through MESH-like networking, when cellular services have been interrupted
* l. Planning swarm-wise decision-based cellular service analysis (advanced BTS statistics)

##### GOLDEN age:

This stage is essentially the completion of this project. However, we expect that long before this happens, the entire network industry will have changed to such a degree that many new privacy and security issues will have arised. Thus, we will likely have more things to add and maintain in this project. We are of the current understanding that this project is a never ending story, all for the peoples benefit and a more privacy oriented future.
* m. Implement **all** of the detection schemes we have
* n. Implement **all** of the interfaces in (B) 
* o. Test AIMSICD in a real IMSI-catcher environment
* p. Continue Fine-tune our detection matrix
* q. Complete alternative data routes using MESH-like networking, when cellular services have been interrupted
* r. Complete advanced statistical analysis of fake BTS towers

---

### Disclaimer

Safety first: Here's our [Disclaimer](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/DISCLAIMER).

---

### Bug Tracker

Please follow [how to correctly submit Issues](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Submitting-Issues)!

---

### Support

Although this project is fully Open Source, developing AIMSICD is a lot of work and done by enthusiastic people during their free time. If you're a developer yourself, we welcome you with open arms! To keep developers in a great mood and support development, please consider making a (fully anonymous) donation. We are currently resonsidering donations in [Issue 74](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/74). Join the discussion!

All collected donations shall be split into appropriate pieces and directly sent to developers who contribute useful code. The amount of donation each developer receives will vary with the value of each merged commit. To be perfectly clear: We will **NOT** reward junk, only awesome stuff. Additionally, donations will be used to support these organizations (contact us if you are a like-minded organization):

[![EFF](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/raw/master/MISC/external/EFF.png)](https://www.eff.org/)
[![Guardian Project](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/raw/master/MISC/external/GuardianProject.png)](https://guardianproject.info/)
[![Privacy International](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/raw/master/MISC/external/PrivacyInternational.png)](https://www.privacyinternational.org/)

If you are unsure how to donate, visit our WIKI-Page on [Anonymous Donations](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Anonymous-Donations).

---

### License

This project is completely licensed [GPL v3+](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/LICENSE).

---

### Credits & Greetings

Our project would not have been possible without [these awesome people](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/CREDITS). HUGE THANKS!

---

### Sponsors

Our gratitude flies out to our great Sponsors:

[![AquaFold](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/raw/master/MISC/external/AquaFold.png)](http://www.aquafold.com) [![Navicat](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/raw/master/MISC/external/Navicat.png)](http://www.navicat.com/) [![Scanova](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/raw/master/MISC/external/Scanova.png)](http://scnv.io/r/25e7713950)

---

### Get in touch with the core team!

|                              Developer                             |              Task            |
|:------------------------------------------------------------------:|:----------------------------:|
|[E:V:A](mailto:a3841c3c@opayq.com)                                  |       Project Initiator      |
|[Ueland](http://h3x.no/)                                            |      Bug Smashing Hammer     |
|[tobykurien](http://tobykurien.com/)                                |          Code-Monkey         |    
|[He3556](mailto:info@dm-development.de)                             |    Vulnerability Analyzer    |
|[Sgt-Obst](http://forum.xda-developers.com/member.php?u=5102584)    |      Graphical Designer      |
|[andr3jx](mailto:9414f52d@opayq.com)                                | Chief Cook and Bottle Washer |
|[SecUpwN](http://forum.xda-developers.com/member.php?u=4686037)     |         Public Speaker       |

---

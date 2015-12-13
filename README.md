# Android IMSI-Catcher Detector

[![BuildStatus](https://travis-ci.org/SecUpwN/Android-IMSI-Catcher-Detector.svg)](https://travis-ci.org/SecUpwN/Android-IMSI-Catcher-Detector) [![DevelopmentStatus](http://img.shields.io/badge/Development-ALPHA-brightgreen.svg)](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Development-Status) [![CoverityScan](https://scan.coverity.com/projects/3346/badge.svg)](https://scan.coverity.com/projects/3346) [![Translations](https://hosted.weblate.org/widgets/aimsicd/-/shields-badge.svg)](https://hosted.weblate.org/projects/aimsicd/strings/)
[![Bountysource](https://www.bountysource.com/badge/team?team_id=40338&style=bounties_received)](https://www.bountysource.com/teams/android-imsi-catcher-detector/issues?utm_source=Android%20IMSI-Catcher%20Detector&utm_medium=shield&utm_campaign=bounties_received)

--
AIMSICD is an app to detect [IMSI-Catchers](https://en.wikipedia.org/wiki/IMSI-catcher). IMSI-Catchers are false mobile towers (base stations) acting between the target mobile phone(s) and the real towers of service providers. As such they are considered a Man-In-The-Middle (MITM) attack. In the USA the IMSI-Catcher technology is known under the name "[StingRay](https://en.wikipedia.org/wiki/Stingray_phone_tracker)". Find out more in our [WIKI](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki).

---

[![AIMSICD-Banner](https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/PROMOTION/AIMSICD-Banner_Large.png)](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Status-Icons)

---

[![AIMSICD-Teaser](https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/PROMOTION/AIMSICD-Teaser.png)](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki)

---

[![Aptoide](https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/MISC/external/Aptoide.png)](http://aimsicd.store.aptoide.com/ "NOTE: Installs Aptoide-App first!")  [![GitHub](https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/MISC/external/GitHub.png)](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases "GitHub Releases") [![F-Droid](https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/MISC/external/F-Droid.png)](https://f-droid.org/repository/browse/?fdid=com.SecUpwN.AIMSICD "F-Droid Store")
[![NoGooglePlay](https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/MISC/external/NoGooglePlay.png)](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/FAQ#q-why-wont-you-upload-your-app-to-the-google-play-store "Statement against GooglePlay")
[![Twitter](https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/MISC/external/Twitter.png)](https://twitter.com/AIMSICD "Official Twitter-Account")

---

### Index

* [What it does](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector#what-it-does)
* [Why use it](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector#why-use-it)
* [**Contributing**](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/development/CONTRIBUTING.md)
* [Bug Tracker](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/development/CONTRIBUTING.md#debugging)
* [Warnings](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector#warnings)
* [Research](https://spideroak.com/browse/share/AIMSICD/GitHub)
* [Support](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector#support)
* [Contact](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Contact)
* [WIKI](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki)
* [FAQ](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/FAQ)

---

[![IMSI-Catchers](https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/DOCUMENTATION/IMSI-Catchers/IMSI-Catchers.png)](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki)

### What it does

AIMSICD attempts to detect IMSI-Catchers through [detection methods](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/230) such as these:
    
* Check [Tower Information Consistency](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/253)
* Check [LAC/Cell ID Consistency](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/91)
* Check [Neighbouring Cell Info](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/264)
* Prevent [silent app installations](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/151)
* Monitor [Signal Strength](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/97)
* Detect [silent SMS](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/glossary-of-terms#silent-sms)
* Detect [FemtoCells](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues/6)

See our [app goals](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Development-Status#application-goals) and [development status](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Development-Status)!

---

### Why use it

The FBI or local police regularly deploys IMSI-Catchers hidden in vehicles at protests to obtain a record of everyone who attended with a cell phone (leave your phones at home by all means if you really have to attend). IMSI-Catchers also allow adversaries to intercept your conversations, text messages, and data. Police can use them to determine your location or to find out who is in a given geographic area at what time. Identity thieves can use freely [available tools](http://www.nsaplayset.org/) to monitor and manipulate communications from a parked car in your residential neighborhood; notably for stealing passwords or credit card data from people nearby who make purchases on their phones.

The reason IMSI-Catchers are so commonly used is that it is very easy to get away with using them traceless. This app aims to make it possible to detect the IMSI-Catchers so that using them becomes a risky proposition. It also aims to make users safer by alerting on possible interception and tracking.

---

### Warnings

[![Warning](https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/DOCUMENTATION/Warning.png)](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Development-Status)

Please read our [Disclaimer](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/DISCLAIMER)! This app does **not**:

* Provide secure phone calls
* Provide secure data transmission
* Provide app permission control
* Provide secure application sand-boxing
* Provide [ROOT](http://www.xda-developers.com/root) and remove bloatware
* Provide secure SMS
* Provide firewalls
* Provide full device encryption
* Prevent already installed rogue apps from full access and spying

Solutions for the above may be found in our [Recommendations](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Recommendations).

---

### Bug Tracker

Please follow our [Bug Submission Guide](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/development/CONTRIBUTING.md#debugging).

---

### Support

Although this project is fully Open Source, developing AIMSICD is a lot of work and done by enthusiastic people during their free time. If you're a developer yourself, we welcome you with open arms! To keep developers in a great mood and support development, please consider making a [donation](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Donations). It will be split into appropriate pieces and directly sent to developers who solved the backed Issue.

---

### License

This project is completely licensed [GPLv3+](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/LICENSE).

---

### Credits & Greetings

Our project would not have been possible without [these awesome people](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/app/src/main/assets/CREDITS). THANK YOU!

---

### Sponsors

Our gratitude flies out to our great Sponsors:

[![AquaFold](https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/MISC/external/AquaFold.png)](http://www.aquafold.com) [![Navicat](https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/MISC/external/Navicat.png)](http://www.navicat.com/) [![Scanova](https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/MISC/external/Scanova.png)](http://scnv.io/r/25e7713950)

---

### Get in touch with the core team!

Core team members are listed [here](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Contact).

---

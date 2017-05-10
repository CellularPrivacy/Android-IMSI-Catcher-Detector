<h1 align="center">Android IMSI-Catcher Detector</h1>

<p align="center">
  <a target="_blank" href="https://travis-ci.org/CellularPrivacy/Android-IMSI-Catcher-Detector"><img src="https://travis-ci.org/CellularPrivacy/Android-IMSI-Catcher-Detector.svg"></a>
  <a target="_blank" href="https://scan.coverity.com/projects/3346"><img src="https://scan.coverity.com/projects/3346/badge.svg"></a>
  <a target="_blank" href="https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki/Development-Status"><img src="https://img.shields.io/badge/Development-ALPHA-blue.svg"></a>
  <a target="_blank" href="https://hosted.weblate.org/projects/aimsicd/strings/"><img src="https://hosted.weblate.org/widgets/aimsicd/-/svg-badge.svg"></a>
  <a target="_blank" href="https://www.bountysource.com/teams/android-imsi-catcher-detector/issues?utm_source=Android%20IMSI-Catcher%20Detector&utm_medium=shield&utm_campaign=bounties_received"><img src="https://www.bountysource.com/badge/team?team_id=40338&style=bounties_received"></a>
</p>

 <br /> <br />
<h2>This Project will have a revival soon. <br />
We are working on a light version of AIMSICD.</h2>
<a href="https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/issues/926">more information</a>

---

`AIMSICD` is an Android app to detect [IMSI-Catchers](https://en.wikipedia.org/wiki/IMSI-catcher). These devices are false mobile towers (base stations) acting between the target mobile phone(s) and the real towers of service providers. As such they are considered a Man-In-The-Middle (MITM) attack. This surveillance technology is also known as "[StingRay](https://en.wikipedia.org/wiki/Stingray_phone_tracker)", "Cellular Interception" and alike. Find out more in our [Wiki on GitHub](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki).

---

[![Banner](https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/PROMOTION/AIMSICD-Banner_Large.png)](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki/Status-Icons)

---

[![Teaser](https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/PROMOTION/AIMSICD-Teaser.png)](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki)

---

<p align="center">
  <a target="_blank" href="http://aimsicd.store.aptoide.com/"><img src="https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/MISC/external/Aptoide.png"></a>
  <a target="_blank" href="https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/releases"><img src="https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/MISC/external/GitHub.png"></a>
  <a target="_blank" href="https://f-droid.org/repository/browse/?fdid=com.SecUpwN.AIMSICD"><img src="https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/MISC/external/F-Droid.png"></a>
  <a target="_blank" href="https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki/FAQ#q-why-wont-you-upload-your-app-to-the-google-play-store"><img src="https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/MISC/external/NoGooglePlay.png"></a>
  <a target="_blank" href="https://twitter.com/AIMSICD"><img src="https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/MISC/external/Twitter.png"></a>
</p>

---

### Index

* [What it does](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector#what-it-does)
* [Why use it](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector#why-use-it)
* [**Contributing**](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/blob/development/.github/CONTRIBUTING.md)
* [Bug Tracker](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/blob/development/.github/CONTRIBUTING.md#debugging)
* [Warnings](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector#warnings)
* [Research](https://spideroak.com/browse/share/AIMSICD/GitHub)
* [Support](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector#support)
* [Contact](https://github.com/orgs/CellularPrivacy/people)
* [Wiki](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki)
* [FAQ](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki/FAQ)

---

[![IMSI-Catchers](https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/DOCUMENTATION/IMSI-Catchers/IMSI-Catchers.png)](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki)

### What it does

`AIMSICD` attempts to detect IMSI-Catchers through [detection methods](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/issues/230) such as these:
    
* Check [Tower Information Consistency](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/issues/253)
* Check [LAC/Cell ID Consistency](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/issues/91)
* Check [Neighboring Cell Info](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/issues/264)
* Prevent [silent app installations](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/issues/151)
* Monitor [Signal Strength](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/issues/97)
* Detect [silent SMS](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki/glossary-of-terms#silent-sms)
* Detect [FemtoCells](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/issues/6)

Make sure to see our [app goals](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki/Development-Status#application-goals) and [development status](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki/Development-Status) as well as [technical overview](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki/Technical-Overview).

---

### Why use it

The FBI or local police regularly deploys IMSI-Catchers hidden in vehicles at protests to obtain a record of everyone who attended with a cell phone (leave your phones at home by all means if you really have to attend). IMSI-Catchers also allow adversaries to intercept your conversations, text messages, and data. Police can use them to determine your location or to find out who is in a given geographic area at what time. Identity thieves can use freely [available tools](http://www.nsaplayset.org/) and even [build their own rogue GSM BTS](https://evilsocket.net/2016/03/31/how-to-build-your-own-rogue-gsm-bts-for-fun-and-profit/) to monitor and manipulate communications from a parked car in your residential neighborhood - notably for stealing passwords or credit card data from people nearby who make purchases on their phones. The reason IMSI-Catchers are so commonly used is that it is very easy to get away with using them since they leave no traces. Our app aims to make it possible to detect the IMSI-Catchers so that using them becomes a risky proposition. It also aims to make users safer by alerting on possible interception and tracking. Fight for your privacy!

---

### Warnings

[![Warning](https://spideroak.com/share/IFEU2U2JINCA/GitHub/home/SecUpwN/SpiderOak/DOCUMENTATION/Warning.png)](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki/Development-Status)

Please read our [Disclaimer](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/blob/master/DISCLAIMER)! This app does **not**:

* Provide secure phone calls
* Provide secure data transmission
* Provide app permission control
* Provide secure application sand-boxing
* Provide [ROOT](http://www.xda-developers.com/root) and remove bloatware
* Provide secure SMS
* Provide firewalls
* Provide full device encryption
* Prevent already installed rogue apps from full access and spying

Solutions for the above may be found in our [Recommendations](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki/Recommendations).

---

### Bug Tracker

Please follow our [Bug Submission Guide](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/blob/development/CONTRIBUTING.md#debugging).

---

### Support

Although our app is fully Open Source, developing it is a lot of work and done by privacy enthusiasts during their free time. If you're a developer yourself, we welcome and credit your [pull requests](https://help.github.com/articles/using-pull-requests/)! To keep developers in a great mood and support development, please consider making a [donation](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki/Donations). It will be split into pieces and directly sent to developers who solved the backed Issue.

---

### License

[![GPLv3+](http://gplv3.fsf.org/gplv3-127x51.png)](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/blob/master/LICENSE)
Please [contribute](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/blob/development/.github/CONTRIBUTING.md) to this repository instead of rebranding our app. Thank you!

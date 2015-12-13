# How to contribute to 'AIMSICD'
--------------------------------

To avoid confusion and rejected development commits, please use this guide!

#### DEVELOPING

Feel invited to develop with us using these steps:

1. [Fork our repository](https://help.github.com/articles/fork-a-repo/) and optionally give it a star when you like it.
2. Download your fork using a Git Cient like [SmartGit](http://www.syntevo.com/smartgit/) (all platforms).
3. Pick [an Issue](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues) you'd like to work on. Introduce yourself in that Issue.
4. Work on resolving the Issue you picked using your local code copy.
5. Add [Code Comments](https://source.android.com/source/code-style.html#java-style-rules) for documentation and follow our [Style Guide](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Style-Guide).
6. Increase the build number `*-alpha-bXX` in `android:versionName`.
7. Test your changes and submit a [pull request](https://help.github.com/articles/using-pull-requests/) when we shall add it.
8. Monitor our [Current Development Status and Development Cycle](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Development-Status).
9. Don't be shy to ask if you need help. Ask in your Issue or [contact us](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Contact).

---

#### TRANSLATING

Let our app start up in your native language!

* See [our translations](https://hosted.weblate.org/projects/aimsicd/strings/) and login via [GitHub](https://hosted.weblate.org/accounts/login/github/?next=/projects/aimsicd/strings/) or [others](https://hosted.weblate.org/accounts/login/?next=/projects/aimsicd/strings/) to add yours.
* When translating, keep small device screens in mind. Shorten it.
* Please make sure to finish all translations as best as possible.
* Translations will be pulled into our GitHub automatically. Enjoy!

---

#### TESTING

Can't code (yet)? No problem, we love you too!

1. Grab our [latest Release](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases) (or even better: compile from [development](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/tree/development)).
2. Read and understand our [WIKI](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki) and **fully agree** to our [Disclaimer](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/blob/master/DISCLAIMER).
3. Test our App on demonstrations and riots (warning: [Privacy](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Privacy/) at risk). 
4. Test all functions of our App and check for possible translation bugs.
5. Spread [our website](https://secupwn.github.io/Android-IMSI-Catcher-Detector), [tweet about us](https://twitter.com/AIMSICD) or use any other [Media Material](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Media-Material).

---

#### DONATING

Thank you for encouraging our developers!

* Feel invited to donate using [this guide](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Donations).

---

#### SEEKING

Special positions we are currently seeking skilled people for:

* People with a CryptoPhone for double app verification.
* Fixing the identified [Coverity Scan Defects](https://scan.coverity.com/projects/3346) of our app.

---

#### FORMATTING

Useful links to help you with correctly formatting posts.

* [Markdown CheatSheet](https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet)
* [GitHub flavored Markdown](https://help.github.com/articles/github-flavored-markdown)
* [Markdown Basics](https://help.github.com/articles/markdown-basics)
* [Writing on GitHub](https://help.github.com/articles/writing-on-github)
* [Mastering Markdown](https://guides.github.com/features/mastering-markdown/)
* [Markdown Tables Generator](http://www.tablesgenerator.com/markdown_tables)
* [Code/Syntax Highlighting](https://github.com/github/linguist/blob/master/lib/linguist/languages.yml)

---

#### DEBUGGING

#### DEBUGGING

Thanks for helping us squashing bugs! Please be patient.

---
**Important Bug Submission Rules:**

1. **SAFETY FIRST:** Remove CID, LAT, IMEI, IMSI and phone number from logs you submit!
2. ALWAYS use the [latest release](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/releases). Preferrably  [build the `development` branch from source](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Building).
3. If you use [Xprivacy](https://github.com/M66B/XPrivacy) read and understand [THIS](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/wiki/Permissions) first! Give our app another try after reading.
4. App still mocking around? See our [open Issues](https://github.com/SecUpwN/Android-IMSI-Catcher-Detector/issues) and look if your Issue already exists.
5. If your Issue does not exist yet, open a new Issue and give it a short descriptive title.
6. Describe your Issue as thoroughly as possible and *add logs* so that we can reproduce it.
8. Maintain your filed Issues! Nothing is more annoying than unresponsive bug reporters.

---

In all cases, you **MUST** include the following:

* AIMSICD version (see the About-Tab within our App)
* Your exact FW specification (ROM, AOS API, etc.)
* Your exact HW specification (processor, model number, etc.)
* The output of getprop command to a Pastebin-Site such as [PIE-Bin](https://defuse.ca/pastebin.htm)
* Logcat from button `Debugging` in Navigation Drawer (remove personal data)
* Feel free to attach any other logs made by a logcat tool like [MatLog](https://github.com/plusCubed/matlog)

---

Command line junkie?

Then you can use the following shell function to help you get only relevant logcat entries. Copy and paste the following to your terminal shell:

```bash
alias logrep='logcat -d -v time -b main -b system -b radio|grep -iE $@'
```

Furthermore, here are some great commands that will:

 1. Create a log directory in: `/sdcard/aimsicd_log/`
 2. cd into that directory 
 3. Clear all the existing logcats
 4. Run AIMSICD and wait for a key press to kill it
 5. Save a full *logcat* (excluding *radio*) into `/sdcard/aimsicd_log/aimdump.log`

Copy and paste the following to your android rooted shell:
  
```bash
alias cdaim='mkdir /sdcard/aimsicd_log; cd /sdcard/aimsicd_log'
alias logclr='logcat -c -b main -b system -b radio -b events' 
alias logdmp='logcat -d -v threadtime -b main -b system -b events -f /sdcard/aimsicd_log/aimdump.log'
export DUMTXT="When bug occurs, press any key to kill app and dump logcat to file..."
alias aimrun='cdaim; logclr; am start -n com.SecUpwN.AIMSICD/.AIMSICD; read dummy?"${DUMTXT}"; am force-stop com.SecUpwN.AIMSICD; logdmp;'
```
To run it, just type: `aimrun`.
If you want to also supply *radio* logcat, add `-b radio` somewhere in the `logdmp` alias, but know that your GPS location and cell info may be included when you do that.

---

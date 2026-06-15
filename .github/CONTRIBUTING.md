# Contribution Guide

Thank you for contributing! Please select:

* [Developing](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/blob/development/.github/CONTRIBUTING.md#developing)
* [Translating](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/blob/development/.github/CONTRIBUTING.md#translating)
* [Testing](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/blob/development/.github/CONTRIBUTING.md#testing)
* [Formatting](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/blob/development/.github/CONTRIBUTING.md#formatting)
* [Debugging](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/blob/development/.github/CONTRIBUTING.md#debugging)
* [Seeking](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/blob/development/.github/CONTRIBUTING.md#seeking)
* [Donating](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/blob/development/.github/CONTRIBUTING.md#donating)
* [Respecting](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/blob/development/.github/CONTRIBUTING.md#code-of-conduct)

## DEVELOPING

Feel invited to develop with us using these steps:

1. [Fork our repository](https://help.github.com/articles/fork-a-repo/) and optionally give it a star when you like it.
2. Download your fork using a Git client like [SmartGit](http://www.syntevo.com/smartgit/) (all platforms).
3. Pick [an Issue](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/issues) you'd like to work on. Introduce yourself in that Issue.
4. Work on resolving the Issue you picked using your local code copy.
5. Follow our [Style Guide](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki/Style-Guide) for higher code quality and corporate design.
6. Test your changes and submit a [pull request](https://help.github.com/articles/using-pull-requests/) when we shall add it.
7. Don't be shy to ask for help in an Issue and feel invited to [contact us](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki/Contact).

## TRANSLATING

Let our app start up in your native language!

* See [our translations](https://hosted.weblate.org/projects/aimsicd/strings/) and [login via GitHub](https://hosted.weblate.org/accounts/login/github/?next=/projects/aimsicd/strings/) or [others](https://hosted.weblate.org/accounts/login/?next=/projects/aimsicd/strings/) to add yours.
* When translating, keep small device screens in mind. Shorten it.
* Please make sure to *finish* your translation, if you start a new one!
* Translations will be pulled into our GitHub automatically. Enjoy!

## TESTING

Can't code (yet)? No problem, we love you too!

1. Grab our [latest Release](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/releases) (or even better: [compile `development` branch](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/tree/development)).
2. Read and understand our [WIKI](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki) and **fully agree** to our [Disclaimer](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/blob/master/DISCLAIMER).
3. Test our app on demonstrations and riots (warning: [Privacy](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki/Privacy/) at risk). 
4. Test all functions of our app and check for possible translation bugs.
5. Share [our website](https://secupwn.github.io/Android-IMSI-Catcher-Detector) and [Media Material](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki/Media-Material) or [tweet about us](https://twitter.com/AIMSICD).

## FORMATTING

Useful links to help you with correctly formatting posts.

* [Markdown CheatSheet](https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet)
* [GitHub flavored Markdown](https://help.github.com/articles/github-flavored-markdown)
* [Markdown Basics](https://help.github.com/articles/markdown-basics)
* [Writing on GitHub](https://help.github.com/articles/writing-on-github)
* [Mastering Markdown](https://guides.github.com/features/mastering-markdown/)
* [Markdown Tables Generator](http://www.tablesgenerator.com/markdown_tables)
* [Code/Syntax Highlighting](https://github.com/github/linguist/blob/master/lib/linguist/languages.yml)

## DEBUGGING

Thanks for helping us squashing bugs! Please be patient.

**Important Bug Submission Rules:**

1. **SAFETY FIRST:** Remove CID, LAT, IMEI, IMSI and phone number from logs you submit!
2. ALWAYS use the [latest release](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/releases). Preferrably  [build the `development` branch from source](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki/Building).
3. If you use [Xprivacy](https://github.com/M66B/XPrivacy) read and understand [THIS](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki/Permissions) first! Give our app another try after reading.
4. App still mocking around? See our [open Issues](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/issues) and look if your Issue already exists.
5. If your Issue does not exist yet, open a new Issue and give it a short descriptive title.
6. Describe your Issue as thoroughly as possible and *add logs* so that we can reproduce it.
8. Maintain your filed Issues! Nothing is more annoying than unresponsive bug reporters.

In all cases, you **MUST** include the following:

* AIMSICD version (see the About-Tab within our app)
* Your exact FW specification (ROM, AOS API, etc.)
* Your exact HW specification (processor, model number, etc.)
* The output of `getprop` command to a Pastebin-Site such as [PIE-Bin](https://defuse.ca/pastebin.htm)
* Logcat from button `Debugging` in Navigation Drawer (remove personal data)
* Feel free to attach any other logs made by a logcat tool like [MatLog](https://github.com/plusCubed/matlog)

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

## SEEKING

Special positions we are currently seeking skilled people for:

* People with a CryptoPhone for another detection verification.
* Bugfixer for [Coverity Scan Defects](https://scan.coverity.com/projects/3346) detected within our app.

## DONATING

Thank you for encouraging our developers!

* Feel invited to donate using [this guide](https://github.com/CellularPrivacy/Android-IMSI-Catcher-Detector/wiki/Donations).

## Code of Conduct

### Our Pledge

In the interest of fostering an open and welcoming environment, we as
contributors and maintainers pledge to making participation in our project and
our community a harassment-free experience for everyone, regardless of age, body
size, disability, ethnicity, gender identity and expression, level of experience,
nationality, personal appearance, race, religion, or sexual identity and
orientation.

### Our Standards

Examples of behavior that contributes to creating a positive environment
include:

* Using welcoming and inclusive language
* Being respectful of differing viewpoints and experiences
* Gracefully accepting constructive criticism
* Focusing on what is best for the community
* Showing empathy towards other community members

Examples of unacceptable behavior by participants include:

* The use of sexualized language or imagery and unwelcome sexual attention or
advances
* Trolling, insulting/derogatory comments, and personal or political attacks
* Public or private harassment
* Publishing others' private information, such as a physical or electronic
  address, without explicit permission
* Other conduct which could reasonably be considered inappropriate in a
  professional setting

### Our Responsibilities

Project maintainers are responsible for clarifying the standards of acceptable
behavior and are expected to take appropriate and fair corrective action in
response to any instances of unacceptable behavior.

Project maintainers have the right and responsibility to remove, edit, or
reject comments, commits, code, wiki edits, issues, and other contributions
that are not aligned to this Code of Conduct, or to ban temporarily or
permanently any contributor for other behaviors that they deem inappropriate,
threatening, offensive, or harmful.

### Scope

This Code of Conduct applies both within project spaces and in public spaces
when an individual is representing the project or its community. Examples of
representing a project or community include using an official project e-mail
address, posting via an official social media account, or acting as an appointed
representative at an online or offline event. Representation of a project may be
further defined and clarified by project maintainers.

### Enforcement

Instances of abusive, harassing, or otherwise unacceptable behavior may be
reported by contacting the [project maintainer](https://github.com/SecUpwN). All
complaints will be reviewed and investigated and will result in a response that
is deemed necessary and appropriate to the circumstances. The project team is
obligated to maintain confidentiality with regard to the reporter of an incident.
Further details of specific enforcement policies may be posted separately.

Project maintainers who do not follow or enforce the Code of Conduct in good
faith may face temporary or permanent repercussions as determined by other
members of the project's leadership.

### Attribution

This Code of Conduct is adapted from the [Contributor Covenant](http://contributor-covenant.org), Version 1.4

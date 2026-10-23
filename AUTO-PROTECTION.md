# AIMSICD — Automatic Protection & Countermeasures (SIM swap + IMSI catcher)

Design, implementation notes and roadmap for the automatic-protection work done on the
`arena/01a0b336-android-imsi-catcher-detector` branch.

> This builds on the upstream AIMSICD project (compileSdk 25 / minSdk 16 / Java 7 era code).
> It is a **privacy-defence code contribution**, not an interception tool.

---

## 1. Goal

Turn AIMSICD from a purely *passive* detector into one that can also **act**, in a safe,
reversible and user-configurable way, against two related threats:

1. **IMSI catcher / rogue base station (StingRay-style MITM)** — a false tower that sits
   between the phone and the real network, forces a downgrade to weak/no ciphering, intercepts
   calls/SMS/data, and tracks location.
2. **SIM swap fraud** — an attacker dupes the carrier into re-issuing your number onto a SIM
   they control. Your SIM silently loses service and the attacker receives your OTPs / calls.

Both threats share one observable, on-device symptom: **your phone unexpectedly loses mobile
service, or its subscriber identity changes.** That is the foundation of this implementation.

---

## 2. Threat model & detection signals

| Threat | On-device observable signals | Signals implemented here |
|---|---|---|
| IMSI catcher | sudden strong "cell" that is not in the OpenCellID DB; changing LAC; empty neighbour list; downgrade of ciphering; silent (Type-0) SMS to locate the victim | network-loss (radio kicked) → `EVENT_NETWORK_LOSS`; existing LAC/CID/neighbour heuristics retained and now escalable |
| SIM swap | SIM absent → network loss → SIM present with a different identity | `EVENT_SIM_ABSENT`, `EVENT_SIM_REINSERTED`, `EVENT_SIM_SWAP_DETECTED` (fingerprint mismatch) |

**Platform limits (must be stated honestly):**

- On stock Android, third-party apps **cannot** read the IMSI (API ≥ 18) or the MSISDN, so a
  perfect "detect my number was re-issued" check is impossible without carrier cooperation.
  AIMSICD therefore fingerprints what *is* readable: ICCID (`getSimSerialNumber`), SIM operator,
  SIM country, and IMSI where still permitted (pre-18).
- The fingerprint is stored **only as a SHA‑256 digest** — raw identifiers are never persisted.
- The most robust signal is **network loss + identity change combined**, which is exactly how a
  real SIM swap presents.

The correct mental model: this is a *risk tripwire and forensic log*, not a cryptographic
guarantee.

---

## 3. What was implemented

### New files

| File | Responsibility |
|---|---|
| `service/SimSwapper.java` | Watches SIM/subscriber identity (SHA‑256 fingerprint baseline vs. current) + coarse radio state. Raises typed events. |
| `protection/AutoProtector.java` | Executes the automatic countermeasures, each individually debounced (60 s). |
| `receiver/SimSwapAlarmReceiver.java` | Receives `SIM_STATE_CHANGED` broadcasts so **hot** swaps are seen even when the app UI is closed; forwards to the running service. |
| `constants/ProtectionConstants.java` | Event IDs (DF_id 100–109), SharedPreferences keys, cooldowns. |

### Modified files

| File | Change |
|---|---|
| `service/CellTracker.java` | Implements `SimSwapper.Reactor`; owns threat status + countermeasure dispatch; `startProtection()/stopProtection()` lifecycle; new `onSilentSmsThreat()`; added `HIGH` notification branch; SIM-swap / network-loss text. |
| `service/AimsicdService.java` | Static `isRunning()` / `getSimSwapper()`, `sInstance` lifecycle. |
| `smsdetection/SmsDetector.java` | Type-0 (silent) SMS now escalates the app threat status. |
| `utils/TinyDB.java` | Added `putString`, `putLong`, `getLong` (fingerprint + cooldown storage). |
| `res/values/…` + `xml/preferences.xml` | Two new preferences + user-facing + untranslatable strings. |
| `AndroidManifest.xml` | Registered `SimSwapAlarmReceiver` (SIM_STATE_CHANGED). |
| `data/model/Event.java` | Documented reserved DF_id range. |

### Data flow

```
TelephonyManager (SERVICE_STATE) ──┐
SIM_STATE_CHANGED broadcast ───────┼──> SimSwapper ──> Realm EventLog
                                   │        │
                                   │        └──> Reactor = CellTracker.onProtectionEvent()
                                   │                 ├─ sets threat status (HIGH/DANGER)
                                   │                 └─ setNotification()
                                   │                        └─ applyProtectionCountermeasures()
                                   │                              ├─ AutoProtector.enableAirplaneMode()
                                   │                              └─ AutoProtector.wipeSensitiveData()
Silent SMS (logcat, root) ─────────┴──> SmsDetector ──> CellTracker.onSilentSmsThreat()
```

---

## 4. Countermeasures (and their real-world limits)

Each response is **default-off**, debounced, and logged. On stock, un-rooted Android the options
are deliberately conservative:

| Response | Off by default | What it does | Limit |
|---|---|---|---|
| Escalate status + notify + EventLog | n/a (always on for detection) | raises the OS notification + vibrate | passive |
| **Airplane mode on HIGH/DANGER** | yes (`pref_sim_swap_protect`) | writes `Settings.Global.AIRPLANE_MODE_ON` + broadcasts change; cuts radio to an attacker immediately | `Settings.Global` writes from unprivileged apps are rejected on most modern Android — it degrades to a logged failure (see below) |
| **Wipe trails on DANGER** | yes (`pref_wipe_sensitive`) | deletes locally collected `Measure` (cell/movement history) + `SmsData` tables | only affects local Realm, not the network |

**Why airplane-mode toggling is best-effort:** since Android 4.2, `Settings.Global.AIRPLANE_MODE_ON`
is protected; a normal app cannot flip it without root/system privileges or Device-Owner. On a
rooted device we could invoke `su -c 'settings put global airplane_mode_on 1; am broadcast -a
android.intent.action.AIRPLANE_MODE --ez state true'` — that is the recommended follow-up (see
roadmap). The chosen design therefore **never fails loudly or mid-toggles**: it tries, logs the
result into the EventLog, and always keeps the user-visible alert as the guaranteed layer.

Safety choices:
- **No auto-disable of protection** (an attacker must not be able to flip it off from a
  notification tap); the user re-enables the network manually.
- **Latching status with reset on explicit stop** — prevents a detector from "clearing" a real
  alarm away.
- **One alarm per SIM change** — the identity re-baselines after an alert so the device does not
  vibrate continuously during a legitimate carrier SIM upgrade.

---

## 5. Preferences

Added under **Protection Settings** in `preferences.xml`:

| Key | Title | Default |
|---|---|---|
| `pref_sim_swap_protect` | SIM-Swap Auto-Protection (airplane mode on detection) | `false` |
| `pref_wipe_sensitive` | Wipe Trails on Danger (delete local cell & SMS logs) | `false` |

---

## 6. EventLog identifiers

The DF_id space `100..109` is reserved for this subsystem (documented in `Event.java` and
`ProtectionConstants.java`):

| DF_id | Meaning |
|---|---|
| 100 | SIM swap detected (fingerprint changed) |
| 101 | SIM card removed |
| 102 | SIM card re-inserted |
| 104 | Hard network loss (no signal / no network) |
| 105 | Auto-protection engaged airplane mode |
| 106 | Auto-protection wiped sensitive data |

---

## 7. Verification status — be honest

**What I could verify:** code is statically consistent — every new symbol, string resource, and
Realm model reference resolves; the XML resources compile logically; the diff is scoped to the
subsystem; `.gitignore` is untouched and `git status` is clean otherwise.

**What I could NOT verify in this sandbox:**

- **A full Gradle build.** The sandbox has no Java/Gradle/Android SDK, and its network egress
  blocks Maven Central, Google Maven, services.gradle.org, the Gradle distribution endpoint,
  JDK mirrors and apt. Only GitHub API/codeload are reachable. The repository targets
  Gradle 2.3.1 + AGP (Android plugin for Gradle) 2.3.1 + jcenter — and **jcenter is
  decommissioned**, so a legacy build would fail to resolve dependencies even with a toolchain.
- **Runtime behaviour** — no emulator/device. The SIM-swap logic and the `HIGH`/`DANGER`
  notification paths need on-device testing (see §9).

**Bottom line:** the code is written against the existing codebase's APIs and style, but it is
**unverified at compile/runtime time** in this environment.

---

## 8. Build-modernization roadmap (you chose "migrate")

The blocker for actually compiling is the 2017 toolchain + retired repositories. Recommended
path (kept **separate** from this feature work so each is reviewable):

1. **Version bumps**
   - Gradle wrapper → 8.x (`distributionUrl` in `gradle/wrapper/gradle-wrapper.properties`).
   - `com.android.tools.build:gradle` → 8.x (AGP).
   - `compileSdk`/`targetSdk` 25/22 → 34 (or latest for `settings.gradle` plugin DSL).
   - Remove/refresh `io.freefair.gradle:android-gradle-plugins` (jcenter-only, long abandoned).
2. **Repository swap** — replace `jcenter()` with `mavenCentral()` + `google()`; keep
   `jitpack.io` only for still-needed GitHub deps.
3. **Dependency refresh** — the heaviest part:
   - `io.realm:realm-gradle-plugin` (3.1.1) → current Realm Kotlin/Java; `@Cleanup Realm` usage
     and `Realm.getDefaultInstance()` API changed (realm-android legacy vs realm-kotlin `lib`).
   - `com.github.Stericson:RootShell` (jitpack commit) → `libsu` (TopJohnWu) — modern, maintained.
   - `io.freefair.*` injection/slf4j/color libs → plain Dagger/Hilt or manual DI + SLF4J Android
     1.7.x from Maven Central.
   - `com.github.MKergall.osmbonuspack` / `com.github.kaichunlin.transition` → current
     osmdroid/osmbonuspack artifacts.
   - `provided org.projectlombok` → annotationProcessor, or drop Lombok for plain accessors.
4. **Java 7 → 17** in `compileOptions` (and simplify the Java‑7-era code progressively).
5. **Manifest modernization** — explicit `android:exported` everywhere (already partially done),
   `package` → `namespace`, `tools:replace` cleanup for `allowBackup`.
6. **Detection refresh** — `TelephonyManager#getCellLocation` / `NeighboringCellInfo` are
   deprecated/returns-null on modern APIs; port to `CellInfo` + `CellLocation.requestCellInfoUpdate`
   and `TelephonyManager#getAllCellInfo`; SIM state via `TelephonyCallback`/`PhoneStateListener`
   continues to work on modern Android.
7. **CI** — add a GitHub Actions `android.yml` (JDK 17 + `gradle/actions/setup-gradle`) so the
   build is verified on GitHub even though it cannot be verified inside this sandbox.

---

## 9. Manual on-device verification

1. Install, accept the disclaimer, enable **Cell Tracking** (default ON) and the two new
   protection toggles if you want the active responses.
2. Baseline: first launch silently records the current SIM fingerprint
   (TinyDB key `aimsicd_last_subscriber_fingerprint`).
3. **SIM absent / reinsert:** pop the SIM tray out, then in → expect event 101 / 102 and a
   `HIGH` notification.
4. **SIM swap:** swap to another SIM → expect event 100 (`DANGER`) and, if the airplane-mode
   toggle is on and your ROM allows it, a radio cut.
5. **Network loss:** enable airplane mode from the system panel manually (or step into a
   basement with no signal) → expect event 104 (`HIGH`) after the debounce.
6. Review `Database Viewer → EventLog` for DF_id 100/101/102/104/105/106 rows.

> Known expectation: on stock Android the airplane-mode write will throw; it is logged and the
> app falls back to the notification. That is by design — see §4.

---

## 10. Out of scope (no false promises)

- This does **not** provide encryption of calls/SMS, carrier-side SIM-lock (that is a carrier
  feature / `SIM PIN` + carrier "port-out freeze"), or immunity to a determined active attacker
  with physical/network position.
- It cannot prevent a SIM swap that the carrier performs against you; it can only detect the
  fallout quickly and help you respond (and it logs evidence).

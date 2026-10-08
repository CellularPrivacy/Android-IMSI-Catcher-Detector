# Defender Agent — IMSI-Catcher & LTE Channel-Spoofing Auto-Protect

An always-on defense layer inside AIMSICD that fuses classic IMSI-catcher
heuristics with LTE channel-spoofing detection, automatic countermeasures, a
firewall with auto rule adder, and an in/out traffic monitoring scanner.

Open it from the navigation drawer: **Defender Agent**.

## What it does

| Module | Purpose |
|---|---|
| `DefenderAgent` | Fuses all signals into one threat level (`NONE/LOW/MEDIUM/HIGH/CRITICAL`), raises incidents, drives auto-protect + firewall. |
| `LteChannelSpoofDetector` | LTE-specific tells: TAC jumps / A→B→A flip-flop, PCI confusion/duplicates, RSRP overpower jumps, new/unexpected EARFCN, Timing-Advance anomalies, LTE→2G downgrade. |
| `AutoProtectController` | Escalation ladder: loud notification + vibrate → data lockdown → radio reset (airplane pulse). All steps best-effort, logged, cooldown-guarded. |
| `firewall.FirewallManager` | Rule CRUD + auto rule adder. Root backend = `iptables` chain `AIMSICD_DEF` (IP/CIDR + per-UID). Non-root backend = VPN sinkhole. |
| `firewall.DefenderVpnService` | Non-root blocking: routes only DENY CIDRs into a TUN and drops them. Normal traffic bypasses the VPN untouched. |
| `traffic.TrafficMonitor` | 2 s polling of `TrafficStats`: global + mobile in/out rates, per-app top talkers, upload-spike / sustained-exfil / bursty-UID anomalies. |
| `traffic.ProcNetConnectionScanner` | Parses `/proc/net/tcp[6]` + `udp[6]` to attribute suspicious uploads to remote IP:port + UID for auto-blocking. |
| `defender.ui.DefenderFragment` | Control panel: threat status, 5 toggles, live in/out rates, top talkers, firewall rules, VPN switch, recent events. |

## Threat fusion

`CellTracker` feeds every detection update into
`DefenderAgent.onCellTrackerUpdate(...)`:

* Classic signals: changing LAC (+35), empty neighbor list (+25), CID unknown
  to OCID (+20), femtocell (+45), silent SMS (+40).
* LTE spoof score (0–100) from `LteChannelSpoofDetector`.
* Traffic anomalies arrive asynchronously and can escalate the level.

Score → level: ≥80 CRITICAL, ≥55 HIGH, ≥35 MEDIUM, ≥15 LOW.
Per-class rate limiting (60–120 s) prevents notification spam.

HIGH+ incidents are also mirrored into the legacy Realm `EventLog` table with
new detector IDs (11–15), so they show up in Database Viewer → EventLog.

## Auto-protect ladder

| Level | Action (only if Auto-protect is ON) |
|---|---|
| MEDIUM+ | Priority notification + vibration (always, even with auto-protect off). |
| HIGH | + firewall lockdown + `svc data disable` attempt. |
| CRITICAL | + radio reset: airplane-mode pulse via root, else a prompt guiding the user to toggle manually. |

Aggressive actions share a 90 s cooldown so a flapping cell can't DoS your own
connectivity. Lockdown is cleared from the Defender panel ("Clear lockdown").

Preferences (`Settings → Defender Agent`): auto-protect, firewall, auto rules,
traffic monitor, LTE spoofing, data-lockdown-on-HIGH, radio-reset-on-CRITICAL.

## Firewall

* Rules match a remote **IP/CIDR** (both backends) or a local **UID/app**
  (root/iptables only — the `owner` match needs kernel support).
* `iptables` backend (root): dedicated chain `AIMSICD_DEF`, hooked on
  `OUTPUT`/`INPUT`, flushed + rebuilt on every rule change.
* VPN sinkhole backend (non-root): `DefenderVpnService` adds one VPN route per
  DENY CIDR (max 128) and drops matching packets. Requires one-time user
  consent via the system VPN dialog ("VPN ON" button). Dropped packet/byte
  counters are shown in the panel.
* The auto rule adder blocklists remote IPs seen during MEDIUM+ traffic
  anomalies and (on HIGH+) the offending app UID. Auto rules are tagged,
  deduplicated, capped at 200, and never touch loopback/LAN ranges.
* Manual rules: "Add" button (IP or CIDR, outbound block). Toggle per rule with
  the checkbox, delete with X, "Clear auto" purges all auto-added rules.

## Traffic monitoring scanner

* Global + per-UID deltas every 2 s; ring buffer of the last ~60 s.
* Anomaly thresholds tighten automatically (halved) while a catcher is
  suspected.
* Upload spike ≥256 KB/s, sustained mobile upload ≥64 KB/s for 10 s, or a
  single app bursting ≥128 KB/s outbound → incident + (if enabled) auto-block
  of the destination IP(s) resolved via `/proc/net` for that UID.
* The Defender panel shows live IN/OUT rates + totals and the top-8 talkers.

## Files

```
AIMSICD/src/main/java/com/secupwn/aimsicd/defender/
├── DefenderAgent.java            # fusion + incident pipeline + toggles
├── DefenderEvent.java            # incident POJO
├── DefenderListener.java         # observer callbacks
├── DefenderLogStore.java         # last-100 ring log (SharedPreferences)
├── LteChannelSpoofDetector.java  # LTE heuristics + scoring
├── AutoProtectController.java    # notify / lockdown / radio reset
├── firewall/
│   ├── FirewallRule.java         # rule model + CIDR matching
│   ├── FirewallRuleStore.java    # rule persistence
│   ├── FirewallManager.java      # CRUD + auto adder + iptables/VPN backends
│   └── DefenderVpnService.java   # non-root sinkhole VPN
├── traffic/
│   ├── TrafficSnapshot.java      # sample + per-UID rates
│   ├── TrafficMonitor.java       # polling + anomaly heuristics
│   └── ProcNetConnectionScanner.java  # /proc/net socket attribution
└── ui/
    ├── DefenderFragment.java     # control panel
    └── FirewallRulesAdapter.java # rule list adapter

AIMSICD/src/main/res/layout/fragment_defender.xml
AIMSICD/src/main/res/layout/item_firewall_rule.xml
```

Integration points: `AimsicdService` (owns the agent lifecycle),
`CellTracker.feedDefenderAgent()` (cell/signal/femto feed),
`SmsDetector.startPopUpInfo()` (silent-SMS feed), drawer menu (`DEFENDER_AGENT`
= 160), `preferences.xml` (Defender category), `AndroidManifest.xml` (INTERNET,
ACCESS_WIFI_STATE, `DefenderVpnService` with `BIND_VPN_SERVICE`).

## Tests

Pure-JVM unit tests (run by `./gradlew build check` on CI, no device needed):

```
AIMSICD/src/test/java/com/secupwn/aimsicd/defender/
├── DefenderScoreTest.java          # score→level boundaries, spoof-result clamping
├── firewall/FirewallRuleTest.java  # CIDR matching, normalization, describe()
├── firewall/FirewallBogonTest.java # local-range guard incl. 172.200.x.x regression
└── traffic/
    ├── TrafficSnapshotTest.java    # rate/total formatting, talker ordering
    └── ProcNetParsingTest.java     # /proc/net IPv4/IPv6/state parsers
```

`LteChannelSpoofDetector.observe()` is intentionally not unit-tested: it emits
an slf4j log on the suspect path, and the slf4j-android binding delegates to
`android.util.Log`, which throws on a bare JVM. Cover it with instrumented
tests or Robolectric if you extend the heuristics.

## Hardening notes

* Firewall lockdown is re-applied first on every iptables rebuild, so later
  rule edits can't silently drop it. Lockdown on non-root devices is reported
  honestly ("limited without root") since only DENY rules + the VPN sinkhole
  apply there.
* The local-range guard uses numeric CIDR checks (`172.16.0.0/12`), not string
  prefixes — naive prefix matching would misclassify public space such as
  `172.200.x.x`.
* Realm EventLog mirroring hops to the main thread: traffic anomalies arrive on
  a Looper-less executor thread where `executeTransactionAsync` would throw.
* The UID→app map refreshes ~once a minute so newly installed apps show up in
  top talkers; all fragment UI callbacks are guarded against detach races.
* The panel shows a consent hint when DENY rules exist but the VPN sinkhole
  isn't running yet on a non-root device.

## Limitations (read before relying on this)

* Heuristics are probabilistic: strong-signal / TAC / PCI anomalies can also
  be legitimate network behavior. Treat MEDIUM as "investigate", HIGH+ as
  "act".
* `svc data` / airplane-pulse / `iptables` need **root**. Without root you
  still get detection, notifications, traffic scanning, and VPN-sinkhole
  blocking of concrete attacker IPs.
* Per-UID firewall rules and `/proc/net` attribution depend on kernel/SELinux
  behavior and may be unavailable on some hardened ROMs — the UI degrades
  gracefully (alert-only).
* The sinkhole VPN routes only DENY rules; it is not a full-tunnel VPN and
  does not anonymize traffic.
* This is an ALPHA research tool, not a guarantee of protection. See
  `DISCLAIMER`.

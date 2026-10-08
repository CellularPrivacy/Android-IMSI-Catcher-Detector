/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.defender;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.CellInfo;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.defender.firewall.FirewallManager;
import com.secupwn.aimsicd.defender.traffic.TrafficMonitor;
import com.secupwn.aimsicd.defender.traffic.TrafficSnapshot;
import com.secupwn.aimsicd.service.CellTracker;
import com.secupwn.aimsicd.utils.Cell;
import com.secupwn.aimsicd.utils.RealmHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.realm.Realm;
import lombok.extern.slf4j.Slf4j;

/**
 * Central defender agent: fuses IMSI-catcher heuristics + LTE channel-spoofing
 * detection + traffic anomalies into one threat level, then drives auto-protect
 * and the firewall auto rule adder.
 *
 * <p>Lifecycle is owned by {@code AimsicdService} (see
 * {@link com.secupwn.aimsicd.service.AimsicdService#getDefenderAgent()}), which
 * feeds cell updates from {@link CellTracker}. The agent is a process-wide
 * singleton so fragments can observe it without a service binding race.</p>
 */
@Slf4j
public class DefenderAgent implements TrafficMonitor.TrafficListener {

    /** Aggregated threat severity. Order matters (ordinal comparisons). */
    public enum ThreatLevel { NONE, LOW, MEDIUM, HIGH, CRITICAL }

    public static final String PREF_LTE_SPOOF = "pref_defender_lte_spoof";
    public static final String PREF_TRAFFIC_MONITOR = "pref_defender_traffic_monitor";
    public static final String PREF_AUTO_RULES = "pref_defender_auto_rules";

    private static volatile DefenderAgent instance;

    private final Context appContext;
    private final LteChannelSpoofDetector lteDetector = new LteChannelSpoofDetector();
    private final FirewallManager firewall;
    private final TrafficMonitor trafficMonitor;
    private final AutoProtectController autoProtect;
    private final DefenderLogStore logStore;
    private final CopyOnWriteArrayList<DefenderListener> listeners =
            new CopyOnWriteArrayList<DefenderListener>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile ThreatLevel currentLevel = ThreatLevel.NONE;
    private volatile boolean started;
    private volatile String lastCellFingerprint = "";
    private volatile long lastDowngradeAlert;
    private volatile long lastCatcherAlert;
    private volatile long lastSpoofAlert;

    private DefenderAgent(Context context) {
        this.appContext = context.getApplicationContext();
        this.firewall = new FirewallManager(appContext);
        this.trafficMonitor = new TrafficMonitor(appContext);
        this.autoProtect = new AutoProtectController(appContext, firewall);
        this.logStore = new DefenderLogStore(appContext);
        this.trafficMonitor.addListener(this);
    }

    public static DefenderAgent getInstance(Context context) {
        if (instance == null) {
            synchronized (DefenderAgent.class) {
                if (instance == null) {
                    instance = new DefenderAgent(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    /** Non-creating accessor (may return null before service start). */
    public static DefenderAgent peekInstance() {
        return instance;
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------

    public synchronized void start() {
        if (started) {
            return;
        }
        started = true;
        SharedPreferences prefs = prefs();
        if (prefs.getBoolean(PREF_TRAFFIC_MONITOR, false)) {
            trafficMonitor.start();
        }
        // Firewall backend applies persisted rules when enabled.
        if (firewall.isEnabled()) {
            firewall.applyRules();
        }
        log.info("DefenderAgent started (autoProtect={}, firewall={}, traffic={}, lteSpoof={})",
                autoProtect.isAutoProtectEnabled(), firewall.isEnabled(),
                trafficMonitor.isRunning(), isLteSpoofEnabled());
        notifyStateChanged();
    }

    public synchronized void stop() {
        started = false;
        try {
            trafficMonitor.stop();
        } catch (Exception ignored) {
        }
        log.info("DefenderAgent stopped.");
    }

    public boolean isStarted() {
        return started;
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------

    public FirewallManager getFirewall() {
        return firewall;
    }

    public TrafficMonitor getTrafficMonitor() {
        return trafficMonitor;
    }

    public AutoProtectController getAutoProtect() {
        return autoProtect;
    }

    public LteChannelSpoofDetector getLteDetector() {
        return lteDetector;
    }

    public DefenderLogStore getLogStore() {
        return logStore;
    }

    public ThreatLevel getCurrentLevel() {
        return currentLevel;
    }

    public String getLastCellFingerprint() {
        return lastCellFingerprint;
    }

    public void addListener(DefenderListener l) {
        if (l != null) {
            listeners.addIfAbsent(l);
        }
    }

    public void removeListener(DefenderListener l) {
        listeners.remove(l);
    }

    // ---- Toggles --------------------------------------------------------

    public boolean isLteSpoofEnabled() {
        return prefs().getBoolean(PREF_LTE_SPOOF, true);
    }

    public void setLteSpoofEnabled(boolean enable) {
        prefs().edit().putBoolean(PREF_LTE_SPOOF, enable).apply();
        if (!enable) {
            lteDetector.reset();
        }
        notifyStateChanged();
    }

    public boolean isTrafficMonitorEnabled() {
        return prefs().getBoolean(PREF_TRAFFIC_MONITOR, false);
    }

    public void setTrafficMonitorEnabled(boolean enable) {
        prefs().edit().putBoolean(PREF_TRAFFIC_MONITOR, enable).apply();
        if (enable && started && !trafficMonitor.isRunning()) {
            trafficMonitor.start();
        } else if (!enable && trafficMonitor.isRunning()) {
            trafficMonitor.stop();
        }
        notifyStateChanged();
    }

    public boolean isAutoRulesEnabled() {
        return prefs().getBoolean(PREF_AUTO_RULES, true);
    }

    public void setAutoRulesEnabled(boolean enable) {
        prefs().edit().putBoolean(PREF_AUTO_RULES, enable).apply();
        notifyStateChanged();
    }

    public void setAutoProtectEnabled(boolean enable) {
        autoProtect.setAutoProtectEnabled(enable);
        notifyStateChanged();
    }

    // ------------------------------------------------------------------
    // Cell-tracker feed (called from CellTracker on cell/signal changes)
    // ------------------------------------------------------------------

    /**
     * @param cell              current serving cell (may be null)
     * @param allCellInfo       raw CellInfo list (may be null on old APIs)
     * @param networkType       TelephonyManager network type
     * @param changedLac        CellTracker: LAC changed for known CID
     * @param emptyNeighbors    CellTracker: no neighbours where expected
     * @param cidNotInOpenDb    CellTracker: CID unknown to OCID import
     * @param femtoDetected     CellTracker: CDMA femtocell hit
     * @param silentSmsDetected CellTracker/Service: type-0 SMS hit
     */
    public void onCellTrackerUpdate(Cell cell, List<CellInfo> allCellInfo, int networkType,
                                    boolean changedLac, boolean emptyNeighbors,
                                    boolean cidNotInOpenDb, boolean femtoDetected,
                                    boolean silentSmsDetected) {
        if (!started) {
            return;
        }
        try {
            lastCellFingerprint = fingerprint(cell, networkType);

            // --- 1) Classic IMSI-catcher score from CellTracker signals ---
            List<String> catcherSignals = new ArrayList<String>();
            int catcherScore = 0;
            if (changedLac) {
                catcherScore += 35;
                catcherSignals.add(appContext.getString(R.string.defender_sig_lac_change));
            }
            if (emptyNeighbors) {
                catcherScore += 25;
                catcherSignals.add(appContext.getString(R.string.defender_sig_no_neighbors));
            }
            if (cidNotInOpenDb) {
                catcherScore += 20;
                catcherSignals.add(appContext.getString(R.string.defender_sig_unknown_cid));
            }
            if (femtoDetected) {
                catcherScore += 45;
                catcherSignals.add(appContext.getString(R.string.defender_sig_femto));
            }
            if (silentSmsDetected) {
                catcherScore += 40;
                catcherSignals.add(appContext.getString(R.string.defender_sig_silent_sms));
            }

            // --- 2) LTE channel-spoofing score ---
            LteChannelSpoofDetector.SpoofResult spoof = null;
            if (isLteSpoofEnabled()) {
                spoof = lteDetector.observe(cell, allCellInfo, networkType);
            }

            // --- 3) Fuse ---
            int total = catcherScore + (spoof == null ? 0 : spoof.score);
            ThreatLevel level = scoreToLevel(total);
            trafficMonitor.setCatcherSuspected(level.ordinal() >= ThreatLevel.MEDIUM.ordinal());

            // --- 4) Raise incidents (rate-limited per class) ---
            long now = System.currentTimeMillis();
            if (catcherScore >= 35 && now - lastCatcherAlert > 60000L) {
                lastCatcherAlert = now;
                raise(DefenderEvent.Type.IMSI_CATCHER_SUSPECT, scoreToLevel(catcherScore),
                        appContext.getString(R.string.defender_evt_catcher_title),
                        join(catcherSignals, "; "), catcherSignals);
            }
            if (spoof != null && spoof.isSuspect() && now - lastSpoofAlert > 60000L) {
                lastSpoofAlert = now;
                boolean downgrade = isDowngradeResult(spoof);
                if (downgrade && now - lastDowngradeAlert < 120000L) {
                    // Downgrade already alerted recently; still update level.
                } else {
                    if (downgrade) {
                        lastDowngradeAlert = now;
                    }
                    raise(downgrade ? DefenderEvent.Type.DOWNGRADE_ATTACK
                                    : DefenderEvent.Type.LTE_SPOOF_SUSPECT,
                            scoreToLevel(spoof.score),
                            downgrade ? appContext.getString(R.string.defender_evt_downgrade_title)
                                    : appContext.getString(R.string.defender_evt_lte_title),
                            join(spoof.reasons, "; "), spoof.reasons);
                }
            }

            setThreatLevel(level, null);
        } catch (Exception e) {
            log.warn("onCellTrackerUpdate failed: {}", e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // TrafficMonitor.TrafficListener
    // ------------------------------------------------------------------

    @Override
    public void onTrafficSnapshot(TrafficSnapshot snapshot) {
        // UI pulls snapshots directly; nothing to do here.
    }

    @Override
    public void onTrafficAnomaly(TrafficMonitor.Anomaly anomaly) {
        if (!started) {
            return;
        }
        try {
            List<String> sigs = new ArrayList<String>();
            sigs.add(anomaly.description);
            DefenderEvent event = raise(DefenderEvent.Type.TRAFFIC_ANOMALY, anomaly.severity,
                    anomaly.title, anomaly.description, sigs);

            // Firewall auto rule adder: sinkhole the remote IPs / flag the UID.
            if (isAutoRulesEnabled() && firewall.isEnabled()
                    && anomaly.severity.ordinal() >= ThreatLevel.MEDIUM.ordinal()) {
                int added = 0;
                if (anomaly.remoteIps != null) {
                    for (String ip : anomaly.remoteIps) {
                        if (firewall.autoBlockIp(ip,
                                "auto: " + anomaly.title + " (" + anomaly.description + ")") != null) {
                            added++;
                        }
                    }
                }
                if (added == 0 && anomaly.uid >= 0
                        && anomaly.severity.ordinal() >= ThreatLevel.HIGH.ordinal()) {
                    if (firewall.autoBlockUid(anomaly.uid, anomaly.packageName,
                            "auto: " + anomaly.title) != null) {
                        added++;
                    }
                }
                if (added > 0) {
                    raise(DefenderEvent.Type.FIREWALL_BLOCK, ThreatLevel.MEDIUM,
                            appContext.getString(R.string.defender_evt_autorule_title,
                                    added),
                            anomaly.description, sigs);
                }
            }

            // Traffic anomalies can escalate the global level (never de-escalate here;
            // de-escalation happens on the next calm cell update).
            if (anomaly.severity.ordinal() > currentLevel.ordinal()) {
                setThreatLevel(anomaly.severity, event);
            } else {
                autoProtect.onThreat(event);
            }
        } catch (Exception e) {
            log.warn("onTrafficAnomaly failed: {}", e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Incident pipeline
    // ------------------------------------------------------------------

    private DefenderEvent raise(DefenderEvent.Type type, ThreatLevel level,
                                String title, String desc, List<String> signals) {
        DefenderEvent event = new DefenderEvent(logStore.nextId(), type, level,
                title, desc, signals, lastCellFingerprint);
        logStore.add(event);
        mirrorToEventLog(event);
        for (DefenderListener l : listeners) {
            try {
                l.onDefenderEvent(event);
            } catch (Exception ignored) {
            }
        }
        if (level.ordinal() > currentLevel.ordinal()) {
            setThreatLevel(level, event);
        } else {
            autoProtect.onThreat(event);
        }
        return event;
    }

    private void setThreatLevel(ThreatLevel level, DefenderEvent cause) {
        if (level == currentLevel) {
            if (cause != null) {
                autoProtect.onThreat(cause);
            }
            return;
        }
        currentLevel = level;
        log.info("Defender threat level -> {} ({})", level,
                cause == null ? "fused" : cause.getTitle());
        for (DefenderListener l : listeners) {
            try {
                l.onThreatLevelChanged(level, cause);
            } catch (Exception ignored) {
            }
        }
        if (cause != null) {
            autoProtect.onThreat(cause);
        }
    }

    /** Also mirror HIGH+ incidents into the legacy Realm EventLog table. */
    private void mirrorToEventLog(final DefenderEvent event) {
        if (event.getThreatLevel().ordinal() < ThreatLevel.HIGH.ordinal()) {
            return;
        }
        // RealmHelper.toEventLog() uses executeTransactionAsync, which needs a
        // Looper thread. Traffic anomalies arrive on a bare executor thread, so
        // hop to the main thread first; cell updates are already on it.
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mirrorToEventLog(event);
                }
            });
            return;
        }
        try {
            Realm realm = Realm.getDefaultInstance();
            try {
                RealmHelper helper = new RealmHelper(appContext);
                // DF ids 1-4 are taken by legacy detectors; use 10+ for defender.
                int dfId = 10;
                switch (event.getType()) {
                    case IMSI_CATCHER_SUSPECT:
                        dfId = 11;
                        break;
                    case LTE_SPOOF_SUSPECT:
                        dfId = 12;
                        break;
                    case DOWNGRADE_ATTACK:
                        dfId = 13;
                        break;
                    case TRAFFIC_ANOMALY:
                        dfId = 14;
                        break;
                    case FIREWALL_BLOCK:
                        dfId = 15;
                        break;
                    default:
                        dfId = 10;
                        break;
                }
                // toEventLog reads CellTracker.monitorCell; ensure it exists.
                if (CellTracker.monitorCell == null) {
                    CellTracker.monitorCell = new Cell();
                }
                helper.toEventLog(realm, dfId,
                        "DEFENDER " + event.getThreatLevel() + ": " + event.getTitle());
            } finally {
                realm.close();
            }
        } catch (Exception e) {
            log.debug("mirrorToEventLog failed: {}", e.getMessage());
        }
    }

    private void notifyStateChanged() {
        for (DefenderListener l : listeners) {
            try {
                l.onDefenderStateChanged();
            } catch (Exception ignored) {
            }
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    public static ThreatLevel scoreToLevel(int score) {
        if (score >= 80) {
            return ThreatLevel.CRITICAL;
        } else if (score >= 55) {
            return ThreatLevel.HIGH;
        } else if (score >= 35) {
            return ThreatLevel.MEDIUM;
        } else if (score >= 15) {
            return ThreatLevel.LOW;
        }
        return ThreatLevel.NONE;
    }

    private static boolean isDowngradeResult(LteChannelSpoofDetector.SpoofResult r) {
        if (r.reasons == null) {
            return false;
        }
        for (String s : r.reasons) {
            if (s.contains("downgrade")) {
                return true;
            }
        }
        return false;
    }

    private static String fingerprint(Cell cell, int networkType) {
        if (cell == null) {
            return "rat=" + Cell.getRatFromInt(networkType);
        }
        return "rat=" + Cell.getRatFromInt(networkType)
                + " mcc=" + cell.getMobileCountryCode()
                + " mnc=" + cell.getMobileNetworkCode()
                + " lac/tac=" + cell.getLocationAreaCode()
                + " cid=" + cell.getCellId()
                + " dbm=" + cell.getDbm();
    }

    private static String join(List<String> list, String sep) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(sep);
            }
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    private SharedPreferences prefs() {
        return PreferenceManager.getDefaultSharedPreferences(appContext);
    }
}

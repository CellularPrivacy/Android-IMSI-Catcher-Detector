/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.defender.traffic;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;

import com.secupwn.aimsicd.defender.DefenderAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 * In/out traffic monitoring scanner.
 *
 * <ul>
 *   <li>Polls {@link TrafficStats} every {@link #POLL_MS} for global + mobile
 *   in/out counters and computes live rates.</li>
 *   <li>Tracks per-UID deltas to surface "top talker" apps.</li>
 *   <li>Runs anomaly heuristics: upload spikes / sustained exfil, sudden new
 *   bursty UID, high mobile traffic while a catcher is suspected.</li>
 *   <li>Optionally resolves live remote IPs via
 *   {@link ProcNetConnectionScanner} so the firewall auto-rule adder can
 *   sinkhole them.</li>
 * </ul>
 */
@Slf4j
public class TrafficMonitor {

    /** Traffic anomaly handed to listeners / firewall auto-adder. */
    public static class Anomaly {
        public final long timeMillis = System.currentTimeMillis();
        public final DefenderAgent.ThreatLevel severity;
        public final String title;
        public final String description;
        public final int uid;               // -1 if global
        public final String packageName;    // may be null
        public final List<String> remoteIps;

        public Anomaly(DefenderAgent.ThreatLevel severity, String title, String description,
                       int uid, String packageName, List<String> remoteIps) {
            this.severity = severity;
            this.title = title;
            this.description = description;
            this.uid = uid;
            this.packageName = packageName;
            this.remoteIps = remoteIps == null
                    ? new ArrayList<String>() : new ArrayList<String>(remoteIps);
        }
    }

    public interface TrafficListener {
        void onTrafficSnapshot(TrafficSnapshot snapshot);
        void onTrafficAnomaly(Anomaly anomaly);
    }

    public static final long POLL_MS = 2000L;
    private static final int HISTORY = 30;               // 60 s at 2 s polls
    private static final int TOP_TALKERS = 8;
    private static final long UPLOAD_SPIKE_BPS = 256 * 1024;   // 256 KB/s
    private static final long SUSTAINED_EXFIL_BPS = 64 * 1024; // 64 KB/s
    private static final int SUSTAINED_SAMPLES = 5;            // 10 s
    private static final long UID_BURST_BPS = 128 * 1024;
    private static final long ANOMALY_COOLDOWN_MS = 60000L;
    private static final int UID_MAP_REFRESH_POLLS = 30; // re-scan installed apps ~every 60 s

    private final Context appContext;
    private final PackageManager pm;
    private final CopyOnWriteArrayList<TrafficListener> listeners =
            new CopyOnWriteArrayList<TrafficListener>();
    private final LinkedList<TrafficSnapshot> history = new LinkedList<TrafficSnapshot>();
    private final Map<Integer, long[]> lastUidCounters = new HashMap<Integer, long[]>();
    private final Map<Integer, String> uidToPackage = new HashMap<Integer, String>();

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> future;
    private volatile TrafficSnapshot lastSnapshot;
    private volatile boolean running;

    private long lastTotalRx = -1;
    private long lastTotalTx = -1;
    private long lastMobileRx = -1;
    private long lastMobileTx = -1;
    private long lastPollTime;
    private int sustainedExfilSamples;
    private long lastAnomalyTime;
    private int pollCount;
    private volatile boolean catcherSuspected; // boosts sensitivity

    public TrafficMonitor(Context context) {
        this.appContext = context.getApplicationContext();
        this.pm = appContext.getPackageManager();
    }

    public void addListener(TrafficListener l) {
        if (l != null) {
            listeners.addIfAbsent(l);
        }
    }

    public void removeListener(TrafficListener l) {
        listeners.remove(l);
    }

    /** Hint from DefenderAgent: a catcher/spoof is currently suspected. */
    public void setCatcherSuspected(boolean suspected) {
        this.catcherSuspected = suspected;
    }

    public boolean isRunning() {
        return running;
    }

    public TrafficSnapshot getLastSnapshot() {
        return lastSnapshot;
    }

    public synchronized List<TrafficSnapshot> getHistory() {
        return new ArrayList<TrafficSnapshot>(history);
    }

    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        refreshUidMap();
        executor = Executors.newSingleThreadScheduledExecutor();
        // Prime counters so the first delta is valid.
        primeCounters();
        future = executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    poll();
                } catch (Exception e) {
                    log.warn("TrafficMonitor.poll failed: {}", e.getMessage());
                }
            }
        }, POLL_MS, POLL_MS, TimeUnit.MILLISECONDS);
        log.info("TrafficMonitor started.");
    }

    public synchronized void stop() {
        running = false;
        if (future != null) {
            future.cancel(true);
            future = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        log.info("TrafficMonitor stopped.");
    }

    // ------------------------------------------------------------------

    private void primeCounters() {
        lastTotalRx = TrafficStats.getTotalRxBytes();
        lastTotalTx = TrafficStats.getTotalTxBytes();
        lastMobileRx = TrafficStats.getMobileRxBytes();
        lastMobileTx = TrafficStats.getMobileTxBytes();
        lastPollTime = System.currentTimeMillis();
        snapshotUidCounters(); // fills lastUidCounters baseline
    }

    private void poll() {
        long now = System.currentTimeMillis();
        long dtMs = now - lastPollTime;
        if (dtMs <= 0) {
            dtMs = POLL_MS;
        }
        double dtSec = dtMs / 1000.0;

        long totalRx = TrafficStats.getTotalRxBytes();
        long totalTx = TrafficStats.getTotalTxBytes();
        long mobileRx = TrafficStats.getMobileRxBytes();
        long mobileTx = TrafficStats.getMobileTxBytes();

        long rxRate = rate(lastTotalRx, totalRx, dtSec);
        long txRate = rate(lastTotalTx, totalTx, dtSec);
        long mRxRate = rate(lastMobileRx, mobileRx, dtSec);
        long mTxRate = rate(lastMobileTx, mobileTx, dtSec);

        lastTotalRx = totalRx;
        lastTotalTx = totalTx;
        lastMobileRx = mobileRx;
        lastMobileTx = mobileTx;
        lastPollTime = now;

        // Periodically pick up newly installed/updated apps (getInstalledApplications
        // is too heavy to run on every 2 s poll, but fine about once a minute).
        pollCount++;
        if (pollCount % UID_MAP_REFRESH_POLLS == 0) {
            refreshUidMap();
        }

        List<TrafficSnapshot.UidRate> talkers = computeUidRates(dtSec);

        TrafficSnapshot snap = new TrafficSnapshot(now, totalRx, totalTx, mobileRx, mobileTx,
                rxRate, txRate, mRxRate, mTxRate, talkers);
        lastSnapshot = snap;
        synchronized (this) {
            history.addLast(snap);
            while (history.size() > HISTORY) {
                history.removeFirst();
            }
        }
        for (TrafficListener l : listeners) {
            try {
                l.onTrafficSnapshot(snap);
            } catch (Exception ignored) {
            }
        }
        detectAnomalies(snap, talkers);
    }

    private static long rate(long before, long now, double dtSec) {
        if (before < 0 || now < 0 || now < before) {
            return 0;
        }
        return (long) ((now - before) / dtSec);
    }

    // ---- Per-UID ------------------------------------------------------

    private void refreshUidMap() {
        try {
            uidToPackage.clear();
            List<ApplicationInfo> apps = pm.getInstalledApplications(0);
            for (ApplicationInfo ai : apps) {
                if (!uidToPackage.containsKey(ai.uid)) {
                    CharSequence label = null;
                    try {
                        label = pm.getApplicationLabel(ai);
                    } catch (Exception ignored) {
                    }
                    uidToPackage.put(ai.uid, label != null ? label.toString() : ai.packageName);
                }
            }
        } catch (Exception e) {
            log.debug("refreshUidMap failed: {}", e.getMessage());
        }
    }

    private void snapshotUidCounters() {
        for (Integer uid : new ArrayList<Integer>(uidToPackage.keySet())) {
            long rx = TrafficStats.getUidRxBytes(uid);
            long tx = TrafficStats.getUidTxBytes(uid);
            lastUidCounters.put(uid, new long[]{rx, tx});
        }
    }

    private List<TrafficSnapshot.UidRate> computeUidRates(double dtSec) {
        List<TrafficSnapshot.UidRate> rates = new ArrayList<TrafficSnapshot.UidRate>();
        // NB: runs on the single poll thread, same thread that refreshes uidToPackage.
        for (Map.Entry<Integer, String> e : uidToPackage.entrySet()) {
            int uid = e.getKey();
            long rx = TrafficStats.getUidRxBytes(uid);
            long tx = TrafficStats.getUidTxBytes(uid);
            long[] last = lastUidCounters.get(uid);
            long rxRate = 0;
            long txRate = 0;
            if (last != null) {
                rxRate = rate(last[0], rx, dtSec);
                txRate = rate(last[1], tx, dtSec);
            }
            lastUidCounters.put(uid, new long[]{rx, tx});
            if (rxRate > 0 || txRate > 0) {
                rates.add(new TrafficSnapshot.UidRate(uid, e.getValue(), rxRate, txRate));
            }
        }
        Collections.sort(rates);
        if (rates.size() > TOP_TALKERS) {
            return new ArrayList<TrafficSnapshot.UidRate>(rates.subList(0, TOP_TALKERS));
        }
        return rates;
    }

    /** Resolve a UID to a user-visible app name. */
    public String packageNameForUid(int uid) {
        String n = uidToPackage.get(uid);
        if (n != null) {
            return n;
        }
        try {
            String[] pkgs = pm.getPackagesForUid(uid);
            if (pkgs != null && pkgs.length > 0) {
                return pkgs[0];
            }
        } catch (Exception ignored) {
        }
        return "uid:" + uid;
    }

    // ---- Anomaly heuristics -------------------------------------------

    private void detectAnomalies(TrafficSnapshot snap, List<TrafficSnapshot.UidRate> talkers) {
        long now = System.currentTimeMillis();
        boolean cooledDown = now - lastAnomalyTime > ANOMALY_COOLDOWN_MS;

        // 1) Global upload spike (possible live exfil / MITM relay).
        long spikeThreshold = catcherSuspected ? UPLOAD_SPIKE_BPS / 2 : UPLOAD_SPIKE_BPS;
        if (snap.txBytesPerSec >= spikeThreshold && cooledDown) {
            fire(new Anomaly(catcherSuspected
                            ? DefenderAgent.ThreatLevel.HIGH : DefenderAgent.ThreatLevel.MEDIUM,
                    "Upload spike",
                    "Outbound " + TrafficSnapshot.formatRate(snap.txBytesPerSec)
                            + " (inbound " + TrafficSnapshot.formatRate(snap.rxBytesPerSec) + ")"
                            + (catcherSuspected ? " while catcher suspected" : ""),
                    -1, null, null));
            lastAnomalyTime = now;
            cooledDown = false;
        }

        // 2) Sustained mobile upload (exfil pattern).
        long exfilThreshold = catcherSuspected ? SUSTAINED_EXFIL_BPS / 2 : SUSTAINED_EXFIL_BPS;
        if (snap.mobileTxPerSec >= exfilThreshold) {
            sustainedExfilSamples++;
        } else {
            sustainedExfilSamples = 0;
        }
        if (sustainedExfilSamples >= SUSTAINED_SAMPLES && cooledDown) {
            sustainedExfilSamples = 0;
            fire(new Anomaly(catcherSuspected
                            ? DefenderAgent.ThreatLevel.HIGH : DefenderAgent.ThreatLevel.MEDIUM,
                    "Sustained mobile upload",
                    "Mobile outbound " + TrafficSnapshot.formatRate(snap.mobileTxPerSec)
                            + " for >" + (SUSTAINED_SAMPLES * POLL_MS / 1000) + "s"
                            + (catcherSuspected ? " while catcher suspected" : ""),
                    -1, null, null));
            lastAnomalyTime = now;
            cooledDown = false;
        }

        // 3) Single bursty UID -> attribute + resolve remote IPs for auto-block.
        if (cooledDown && talkers != null && !talkers.isEmpty()) {
            TrafficSnapshot.UidRate top = talkers.get(0);
            long burstThreshold = catcherSuspected ? UID_BURST_BPS / 2 : UID_BURST_BPS;
            if (top.txBytesPerSec >= burstThreshold) {
                List<String> ips = new ArrayList<String>();
                try {
                    ips = ProcNetConnectionScanner.remoteIpsForUid(top.uid);
                } catch (Exception e) {
                    log.debug("remote ip resolve failed: {}", e.getMessage());
                }
                StringBuilder desc = new StringBuilder();
                desc.append(packageNameForUid(top.uid))
                        .append(" uploading ").append(TrafficSnapshot.formatRate(top.txBytesPerSec));
                if (!ips.isEmpty()) {
                    desc.append(" -> ").append(join(ips, ", "));
                }
                fire(new Anomaly(catcherSuspected
                                ? DefenderAgent.ThreatLevel.HIGH : DefenderAgent.ThreatLevel.MEDIUM,
                        "Suspicious app upload", desc.toString(),
                        top.uid, packageNameForUid(top.uid), ips));
                lastAnomalyTime = now;
            }
        }
    }

    private void fire(Anomaly anomaly) {
        log.info("Traffic anomaly: {} :: {}", anomaly.title, anomaly.description);
        for (TrafficListener l : listeners) {
            try {
                l.onTrafficAnomaly(anomaly);
            } catch (Exception ignored) {
            }
        }
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
}

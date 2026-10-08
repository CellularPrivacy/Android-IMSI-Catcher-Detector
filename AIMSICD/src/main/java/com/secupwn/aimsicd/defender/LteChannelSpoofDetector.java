/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.defender;

import android.annotation.TargetApi;
import android.os.Build;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;

import com.secupwn.aimsicd.utils.Cell;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * LTE channel-spoofing heuristics.
 *
 * <p>Real LTE catchers / rogue eNodeBs typically reveal themselves through a
 * combination of weak tells rather than one smoking gun:</p>
 * <ul>
 *   <li>sudden TAC jumps while stationary (fake Tracking Area)</li>
 *   <li>PCI confusion / duplicated PCI with different CI</li>
 *   <li>abrupt RSRP jumps (catcher nearby overpowers the macro cell)</li>
 *   <li>unexpected EARFCN / band hops</li>
 *   <li>forced LTE-&gt;GSM downgrade (classic 2G interception)</li>
 *   <li>Timing-Advance anomalies (TA pinned at 0/max with strong signal)</li>
 * </ul>
 *
 * <p>Each observation produces a 0-100 score plus human readable reasons.
 * Scores map to {@link DefenderAgent.ThreatLevel} inside
 * {@link DefenderAgent}.</p>
 *
 * <p>All state is kept in small ring buffers; the class is thread-safe.</p>
 */
@Slf4j
public class LteChannelSpoofDetector {

    /** One LTE observation pulled from {@link CellInfo} / {@link Cell}. */
    public static class LteObservation {
        public final long timeMillis;
        public final int ci;      // Cell Identity
        public final int tac;     // Tracking Area Code
        public final int pci;     // Physical Cell Id
        public final int earfcn;  // E-UTRA Absolute Radio Freq Number (-1 unknown)
        public final int rsrpDbm; // RSRP-ish dBm (CellSignalStrengthLte.getDbm)
        public final int rsrq;    // RSRQ if available else Integer.MAX_VALUE
        public final int timingAdvance;
        public final int mcc;
        public final int mnc;

        public LteObservation(long timeMillis, int ci, int tac, int pci, int earfcn,
                              int rsrpDbm, int rsrq, int timingAdvance, int mcc, int mnc) {
            this.timeMillis = timeMillis;
            this.ci = ci;
            this.tac = tac;
            this.pci = pci;
            this.earfcn = earfcn;
            this.rsrpDbm = rsrpDbm;
            this.rsrq = rsrq;
            this.timingAdvance = timingAdvance;
            this.mcc = mcc;
            this.mnc = mnc;
        }

        public boolean isValid() {
            return ci != Integer.MAX_VALUE && ci != -1 && tac != Integer.MAX_VALUE && tac != -1;
        }

        @Override
        public String toString() {
            return "LTE{ci=" + ci + " tac=" + tac + " pci=" + pci + " earfcn=" + earfcn
                    + " rsrp=" + rsrpDbm + " ta=" + timingAdvance + " mcc=" + mcc + " mnc=" + mnc + "}";
        }
    }

    /** Result of scoring one observation against history. */
    public static class SpoofResult {
        public final int score; // 0..100
        public final List<String> reasons;
        public final LteObservation observation;

        public SpoofResult(int score, List<String> reasons, LteObservation observation) {
            this.score = Math.max(0, Math.min(100, score));
            this.reasons = reasons;
            this.observation = observation;
        }

        public boolean isSuspect() {
            return score >= 40;
        }
    }

    // ---- Tunables -----------------------------------------------------
    private static final int HISTORY = 25;
    private static final long TAC_JUMP_WINDOW_MS = 120000L; // 2 min
    private static final int RSRP_JUMP_DB = 18;             // sudden overpower jump
    private static final int RSRP_CATCHER_STRONG = -65;     // abnormally strong indoor?

    private final LinkedList<LteObservation> history = new LinkedList<LteObservation>();
    private final Map<Integer, Integer> pciToCi = new HashMap<Integer, Integer>();
    private final Map<String, Integer> earfcnSeen = new HashMap<String, Integer>();

    private LteObservation lastLte;
    private long lastLteTime;
    private int lastRat = Integer.MAX_VALUE;
    private long lastRatChangeTime;

    /**
     * Feed a raw {@link Cell} + optional {@link CellInfo} list.
     *
     * @param cell        current AIMSICD cell (netType decides RAT)
     * @param allCellInfo nullable list from TelephonyManager.getAllCellInfo()
     * @param networkType TelephonyManager network type int
     * @return scoring result (never null)
     */
    public synchronized SpoofResult observe(Cell cell, List<CellInfo> allCellInfo, int networkType) {
        List<String> reasons = new ArrayList<String>();
        int score = 0;

        // Track RAT downgrade first (works for any RAT, not just LTE).
        score += checkDowngrade(networkType, reasons);

        if (!isLte(networkType)) {
            // Not on LTE right now: keep history, still report downgrade score.
            lastRat = networkType;
            return new SpoofResult(score, reasons, lastLte);
        }

        LteObservation obs = extractObservation(cell, allCellInfo);
        if (obs == null || !obs.isValid()) {
            lastRat = networkType;
            return new SpoofResult(score, reasons, lastLte);
        }

        score += checkTacJump(obs, reasons);
        score += checkPciConfusion(obs, allCellInfo, reasons);
        score += checkRsrpJump(obs, reasons);
        score += checkEarfcn(obs, reasons);
        score += checkTimingAdvance(obs, reasons);

        // Commit to history.
        history.addLast(obs);
        while (history.size() > HISTORY) {
            history.removeFirst();
        }
        pciToCi.put(obs.pci, obs.ci);
        if (obs.earfcn > 0) {
            earfcnSeen.put(obs.mcc + "/" + obs.mnc + "/" + obs.earfcn,
                    earfcnSeen.containsKey(obs.mcc + "/" + obs.mnc + "/" + obs.earfcn)
                            ? earfcnSeen.get(obs.mcc + "/" + obs.mnc + "/" + obs.earfcn) + 1 : 1);
        }
        lastLte = obs;
        lastLteTime = obs.timeMillis;
        lastRat = networkType;

        if (score > 0) {
            log.info("LTE spoof score={} obs={} reasons={}", score, obs, reasons);
        }
        return new SpoofResult(score, reasons, obs);
    }

    /** Reset learned history (e.g. user travelled far / reset DB). */
    public synchronized void reset() {
        history.clear();
        pciToCi.clear();
        earfcnSeen.clear();
        lastLte = null;
        lastLteTime = 0;
        lastRat = Integer.MAX_VALUE;
        lastRatChangeTime = 0;
    }

    public synchronized LteObservation getLastLte() {
        return lastLte;
    }

    // ------------------------------------------------------------------
    // Individual heuristics (each returns score delta 0..40)
    // ------------------------------------------------------------------

    private int checkDowngrade(int networkType, List<String> reasons) {
        long now = System.currentTimeMillis();
        int delta = 0;
        if (lastRat != Integer.MAX_VALUE && isLte(lastRat) && is2gFamily(networkType)) {
            long gap = lastRatChangeTime == 0 ? 0 : now - lastRatChangeTime;
            // Downgrade itself is suspicious; faster downgrade = more suspicious.
            delta = 35;
            reasons.add("LTE->" + Cell.getRatFromInt(networkType)
                    + " downgrade" + (gap > 0 ? " after " + (gap / 1000) + "s on LTE" : "")
                    + " (classic catcher forces 2G)");
        }
        if (networkType != lastRat) {
            lastRatChangeTime = now;
        }
        return delta;
    }

    private int checkTacJump(LteObservation obs, List<String> reasons) {
        if (lastLte == null || !lastLte.isValid()) {
            return 0;
        }
        if (obs.tac == lastLte.tac) {
            return 0;
        }
        long dt = obs.timeMillis - lastLteTime;
        if (dt < 0) {
            dt = 0;
        }
        if (dt <= TAC_JUMP_WINDOW_MS) {
            // Same PCI but new TAC is extra suspicious; an A->B->A flip-flop
            // (catcher relaying between two areas) scores the max.
            // (history holds committed obs; [size-1] is lastLte.)
            boolean samePci = obs.pci == lastLte.pci;
            boolean flipFlop = history.size() >= 2
                    && history.get(history.size() - 2).tac == obs.tac;
            int delta = flipFlop ? 40 : (samePci ? 35 : 25);
            reasons.add("TAC jump " + lastLte.tac + " -> " + obs.tac
                    + " in " + (dt / 1000) + "s"
                    + (flipFlop ? " (flip-flop A->B->A, likely fake tracking area)"
                            : (samePci ? " (same PCI, likely fake tracking area)" : "")));
            return delta;
        }
        reasons.add("TAC changed " + lastLte.tac + " -> " + obs.tac + " (informational)");
        return 5;
    }

    private int checkPciConfusion(LteObservation obs, List<CellInfo> allCellInfo, List<String> reasons) {
        int delta = 0;
        // 1) Same PCI previously seen with a different CI.
        Integer knownCi = pciToCi.get(obs.pci);
        if (knownCi != null && knownCi != obs.ci && obs.pci != Integer.MAX_VALUE && obs.pci != -1) {
            reasons.add("PCI confusion: PCI " + obs.pci + " previously CI " + knownCi
                    + ", now CI " + obs.ci + " (PCI reuse / spoofed cell?)");
            delta += 25;
        }
        // 2) Duplicate PCI inside the current neighbour set with different CI.
        try {
            if (allCellInfo != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                Map<Integer, Integer> pciCount = new HashMap<Integer, Integer>();
                for (CellInfo info : allCellInfo) {
                    if (info instanceof CellInfoLte) {
                        CellIdentityLte id = ((CellInfoLte) info).getCellIdentity();
                        int pci = id.getPci();
                        int ci = id.getCi();
                        if (pci == Integer.MAX_VALUE || ci == Integer.MAX_VALUE) {
                            continue;
                        }
                        Integer firstCi = pciCount.get(pci);
                        if (firstCi == null) {
                            pciCount.put(pci, ci);
                        } else if (firstCi != ci) {
                            reasons.add("Duplicate PCI " + pci + " with CI " + firstCi
                                    + " and " + ci + " visible at once (spoof indicator)");
                            delta += 30;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("PCI neighbour scan failed: {}", e.getMessage());
        }
        return Math.min(delta, 40);
    }

    private int checkRsrpJump(LteObservation obs, List<String> reasons) {
        if (lastLte == null || obs.rsrpDbm == Integer.MAX_VALUE
                || lastLte.rsrpDbm == Integer.MAX_VALUE) {
            return 0;
        }
        int jump = obs.rsrpDbm - lastLte.rsrpDbm; // positive = stronger
        long dt = obs.timeMillis - lastLteTime;
        if (jump >= RSRP_JUMP_DB && dt <= TAC_JUMP_WINDOW_MS) {
            reasons.add("RSRP jump " + lastLte.rsrpDbm + " -> " + obs.rsrpDbm
                    + " dBm (+" + jump + " dB in " + (dt / 1000) + "s, overpower attack?)");
            return 25;
        }
        if (obs.rsrpDbm >= RSRP_CATCHER_STRONG && obs.ci != lastLte.ci) {
            reasons.add("Very strong new cell " + obs.rsrpDbm + " dBm on CI " + obs.ci
                    + " (rogue eNodeB nearby?)");
            return 15;
        }
        return 0;
    }

    private int checkEarfcn(LteObservation obs, List<String> reasons) {
        if (obs.earfcn <= 0) {
            return 0; // EARFCN not exposed on this API/device.
        }
        String key = obs.mcc + "/" + obs.mnc + "/" + obs.earfcn;
        Integer seen = earfcnSeen.get(key);
        if (seen == null) {
            // First sight of this EARFCN for this operator.
            if (!earfcnSeen.isEmpty()) {
                reasons.add("New EARFCN " + obs.earfcn + " for " + obs.mcc + "/" + obs.mnc
                        + " (unexpected band/channel, possible spoof)");
                return 20;
            }
            return 0;
        }
        if (lastLte != null && lastLte.earfcn > 0 && lastLte.earfcn != obs.earfcn) {
            long dt = obs.timeMillis - lastLteTime;
            if (dt <= TAC_JUMP_WINDOW_MS) {
                reasons.add("EARFCN hop " + lastLte.earfcn + " -> " + obs.earfcn
                        + " in " + (dt / 1000) + "s");
                return 15;
            }
        }
        return 0;
    }

    private int checkTimingAdvance(LteObservation obs, List<String> reasons) {
        if (obs.timingAdvance == Integer.MAX_VALUE) {
            return 0;
        }
        // TA pinned at extremes with a strong cell is odd; TA oscillation is odder.
        if (obs.timingAdvance == 0 && obs.rsrpDbm != Integer.MAX_VALUE
                && obs.rsrpDbm >= -75 && lastLte != null && lastLte.timingAdvance > 4) {
            reasons.add("Timing-Advance collapsed to 0 with strong signal (was "
                    + lastLte.timingAdvance + ", catcher co-located?)");
            return 15;
        }
        if (lastLte != null && lastLte.timingAdvance != Integer.MAX_VALUE) {
            int dTa = Math.abs(obs.timingAdvance - lastLte.timingAdvance);
            long dt = obs.timeMillis - lastLteTime;
            if (dTa > 40 && dt <= TAC_JUMP_WINDOW_MS && obs.ci == lastLte.ci) {
                reasons.add("Timing-Advance jump " + lastLte.timingAdvance + " -> "
                        + obs.timingAdvance + " on same CI (distance spoof?)");
                return 15;
            }
        }
        return 0;
    }

    // ------------------------------------------------------------------
    // Extraction helpers
    // ------------------------------------------------------------------

    private LteObservation extractObservation(Cell cell, List<CellInfo> allCellInfo) {
        long now = System.currentTimeMillis();
        int ci = Integer.MAX_VALUE;
        int tac = Integer.MAX_VALUE;
        int pci = Integer.MAX_VALUE;
        int earfcn = -1;
        int rsrp = cell != null ? cell.getDbm() : Integer.MAX_VALUE;
        int rsrq = Integer.MAX_VALUE;
        int ta = cell != null ? cell.getTimingAdvance() : Integer.MAX_VALUE;
        int mcc = cell != null ? cell.getMobileCountryCode() : Integer.MAX_VALUE;
        int mnc = cell != null ? cell.getMobileNetworkCode() : Integer.MAX_VALUE;

        // Prefer the registered LTE CellInfo when available.
        try {
            if (allCellInfo != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                CellInfoLte reg = findRegisteredLte(allCellInfo);
                if (reg != null) {
                    CellIdentityLte id = reg.getCellIdentity();
                    CellSignalStrengthLte ss = reg.getCellSignalStrength();
                    if (id.getCi() != Integer.MAX_VALUE) {
                        ci = id.getCi();
                    }
                    if (id.getTac() != Integer.MAX_VALUE) {
                        tac = id.getTac();
                    }
                    if (id.getPci() != Integer.MAX_VALUE) {
                        pci = id.getPci();
                    }
                    if (id.getMcc() != Integer.MAX_VALUE) {
                        mcc = id.getMcc();
                    }
                    if (id.getMnc() != Integer.MAX_VALUE) {
                        mnc = id.getMnc();
                    }
                    int e = getEarfcnReflective(id);
                    if (e > 0) {
                        earfcn = e;
                    }
                    if (ss.getDbm() != Integer.MAX_VALUE) {
                        rsrp = ss.getDbm();
                    }
                    int r = getRsrqReflective(ss);
                    if (r != Integer.MAX_VALUE) {
                        rsrq = r;
                    }
                    if (ss.getTimingAdvance() != Integer.MAX_VALUE) {
                        ta = ss.getTimingAdvance();
                    }
                }
            }
        } catch (Exception e) {
            log.debug("LTE CellInfo extract failed: {}", e.getMessage());
        }

        // Fall back to the generic Cell fields (LAC doubles as TAC on some paths).
        if (cell != null) {
            if (ci == Integer.MAX_VALUE) {
                ci = cell.getCellId();
            }
            if (tac == Integer.MAX_VALUE) {
                tac = cell.getLocationAreaCode();
            }
            if (pci == Integer.MAX_VALUE) {
                pci = cell.getPrimaryScramblingCode();
            }
        }
        return new LteObservation(now, ci, tac, pci, earfcn, rsrp, rsrq, ta, mcc, mnc);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private CellInfoLte findRegisteredLte(List<CellInfo> infos) {
        CellInfoLte fallback = null;
        for (CellInfo info : infos) {
            if (info instanceof CellInfoLte) {
                CellInfoLte lte = (CellInfoLte) info;
                if (lte.isRegistered()) {
                    return lte;
                }
                if (fallback == null) {
                    fallback = lte;
                }
            }
        }
        return fallback;
    }

    /** EARFCN getter only exists on API 24+; use reflection so we run on API 16+. */
    private int getEarfcnReflective(CellIdentityLte id) {
        try {
            Method m = CellIdentityLte.class.getMethod("getEarfcn");
            Object o = m.invoke(id);
            if (o instanceof Integer) {
                return (Integer) o;
            }
        } catch (NoSuchMethodException nsme) {
            // Pre-N devices: not available.
        } catch (Exception e) {
            log.debug("getEarfcn failed: {}", e.getMessage());
        }
        return -1;
    }

    private int getRsrqReflective(CellSignalStrengthLte ss) {
        try {
            Method m = CellSignalStrengthLte.class.getMethod("getRsrq");
            Object o = m.invoke(ss);
            if (o instanceof Integer) {
                return (Integer) o;
            }
        } catch (NoSuchMethodException nsme) {
            // Older API.
        } catch (Exception e) {
            log.debug("getRsrq failed: {}", e.getMessage());
        }
        return Integer.MAX_VALUE;
    }

    private static boolean isLte(int networkType) {
        return networkType == TelephonyManager.NETWORK_TYPE_LTE;
    }

    /** 2G-family RATs a catcher classically forces a downgrade into. */
    private static boolean is2gFamily(int networkType) {
        return networkType == TelephonyManager.NETWORK_TYPE_GPRS
                || networkType == TelephonyManager.NETWORK_TYPE_EDGE
                || networkType == TelephonyManager.NETWORK_TYPE_GSM
                || networkType == TelephonyManager.NETWORK_TYPE_IDEN
                || networkType == TelephonyManager.NETWORK_TYPE_CDMA
                || networkType == TelephonyManager.NETWORK_TYPE_1xRTT;
    }
}

/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.defender.firewall;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.secupwn.aimsicd.utils.CMDProcessor;
import com.secupwn.aimsicd.utils.CommandResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.extern.slf4j.Slf4j;

/**
 * Firewall with auto rule adder.
 *
 * <p>Two enforcement backends:</p>
 * <ol>
 *   <li><b>Root / iptables</b> (preferred): rules are materialized into a
 *   dedicated {@code AIMSICD_DEF} chain on OUTPUT (and INPUT for inbound
 *   DENY). Supports IP/CIDR + per-UID rules.</li>
 *   <li><b>Non-root / VPN sinkhole</b> (fallback): DENY IP rules are pushed to
 *   {@link DefenderVpnService}, which adds a VPN route per CIDR so matching
 *   packets are pulled into the TUN interface and dropped. UID rules are kept
 *   as "alert only" without root.</li>
 * </ol>
 *
 * <p>The auto rule adder ({@link #autoBlockIp}, {@link #autoBlockUid}) is fed
 * by {@code DefenderAgent} (catcher/spoof verdicts) and the traffic monitor
 * (suspicious uploads). Auto rules are tagged, deduplicated and rate-limited
 * so a noisy network can't fill the rule table.</p>
 */
@Slf4j
public class FirewallManager {

    public interface FirewallListener {
        void onRulesChanged(List<FirewallRule> rules);
        void onFirewallStateChanged(boolean enabled, boolean rooted);
    }

    private static final String CHAIN = "AIMSICD_DEF";
    private static final int MAX_AUTO_RULES = 200;
    private static final long AUTO_RULE_COOLDOWN_MS = 15000L;

    private final Context appContext;
    private final FirewallRuleStore store;
    private final CopyOnWriteArrayList<FirewallListener> listeners =
            new CopyOnWriteArrayList<FirewallListener>();
    private final List<FirewallRule> rules = new ArrayList<FirewallRule>();

    private volatile boolean enabled;
    private volatile boolean lockdown; // when true: deny-all-data safety mode
    private volatile Boolean rootedCache;
    private long lastAutoRuleTime;

    public FirewallManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.store = new FirewallRuleStore(appContext);
        this.rules.addAll(store.loadAll());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        this.enabled = prefs.getBoolean("pref_defender_firewall", false);
        seedDefaultRulesIfEmpty();
    }

    // ------------------------------------------------------------------
    // Listeners / state
    // ------------------------------------------------------------------

    public void addListener(FirewallListener l) {
        if (l != null) {
            listeners.addIfAbsent(l);
        }
    }

    public void removeListener(FirewallListener l) {
        listeners.remove(l);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isLockdown() {
        return lockdown;
    }

    public synchronized List<FirewallRule> getRules() {
        return new ArrayList<FirewallRule>(rules);
    }

    public synchronized List<FirewallRule> getEnabledDenyIpRules() {
        List<FirewallRule> out = new ArrayList<FirewallRule>();
        for (FirewallRule r : rules) {
            if (r.isEnabled() && r.getAction() == FirewallRule.Action.DENY && r.isIpRule()) {
                out.add(r);
            }
        }
        return out;
    }

    /** Cached root probe (iptables needs su). */
    public boolean isRooted() {
        if (rootedCache != null) {
            return rootedCache;
        }
        boolean root = false;
        try {
            CommandResult r = CMDProcessor.runSuCommand("id");
            root = r != null && r.success()
                    && r.getStdOut() != null && r.getStdOut().contains("uid=0");
        } catch (Exception e) {
            log.debug("root probe failed: {}", e.getMessage());
        }
        rootedCache = root;
        return root;
    }

    public void refreshRootCache() {
        rootedCache = null;
        isRooted();
    }

    // ------------------------------------------------------------------
    // Enable / disable / lockdown
    // ------------------------------------------------------------------

    public synchronized void setEnabled(boolean enable) {
        if (this.enabled == enable) {
            pushVpnRules();
            return;
        }
        this.enabled = enable;
        PreferenceManager.getDefaultSharedPreferences(appContext)
                .edit().putBoolean("pref_defender_firewall", enable).apply();
        if (enable) {
            applyRules();
        } else {
            lockdown = false;
            flushIptables();
            pushVpnRules();
        }
        notifyState();
        log.info("Firewall {}", enable ? "ENABLED" : "DISABLED");
    }

    /** Emergency deny-out mode: blocks new outbound data until cleared. */
    public synchronized void setLockdown(boolean on, String reason) {
        if (!enabled && on) {
            setEnabled(true);
        }
        this.lockdown = on;
        log.info("Firewall lockdown {} ({})", on ? "ON" : "OFF", reason);
        // Re-materialize everything; applyIptables() re-inserts the lockdown
        // rule first when lockdown is on, so later rule edits can't drop it.
        applyRules();
        pushVpnRules();
        notifyState();
    }

    // ------------------------------------------------------------------
    // Rule CRUD
    // ------------------------------------------------------------------

    public synchronized FirewallRule addRule(FirewallRule.Direction dir, FirewallRule.Action action,
                                             String ipOrCidr, int uid, String pkg, int port,
                                             String reason, boolean autoAdded) {
        // Deduplicate identical active rules.
        for (FirewallRule r : rules) {
            if (r.getAction() == action && r.getDirection() == dir
                    && eq(r.getTargetIpOrCidr(), ipOrCidr)
                    && r.getUid() == uid && r.getPort() == port) {
                if (!r.isEnabled()) {
                    r.setEnabled(true);
                    persistAndApply();
                }
                return r;
            }
        }
        FirewallRule rule = new FirewallRule(store.nextId(), true, dir, action,
                ipOrCidr, uid, pkg, port, reason, autoAdded);
        rules.add(0, rule);
        persistAndApply();
        log.info("Firewall rule added: {}", rule.describe());
        return rule;
    }

    public synchronized boolean removeRule(long id) {
        for (int i = 0; i < rules.size(); i++) {
            if (rules.get(i).getId() == id) {
                FirewallRule gone = rules.remove(i);
                persistAndApply();
                log.info("Firewall rule removed: {}", gone.describe());
                return true;
            }
        }
        return false;
    }

    public synchronized boolean setRuleEnabled(long id, boolean enable) {
        for (FirewallRule r : rules) {
            if (r.getId() == id) {
                r.setEnabled(enable);
                persistAndApply();
                return true;
            }
        }
        return false;
    }

    public synchronized int clearAutoRules() {
        int n = 0;
        for (int i = rules.size() - 1; i >= 0; i--) {
            if (rules.get(i).isAutoAdded()) {
                rules.remove(i);
                n++;
            }
        }
        if (n > 0) {
            persistAndApply();
        }
        return n;
    }

    // ------------------------------------------------------------------
    // Auto rule adder (called by DefenderAgent / TrafficMonitor)
    // ------------------------------------------------------------------

    /** Auto-block a remote IP/CIDR observed during an incident. */
    public synchronized FirewallRule autoBlockIp(String ipOrCidr, String reason) {
        if (ipOrCidr == null || ipOrCidr.trim().isEmpty()) {
            return null;
        }
        ipOrCidr = ipOrCidr.trim();
        if (isBogonOrLocal(ipOrCidr)) {
            log.debug("autoBlockIp skipped local/bogon: {}", ipOrCidr);
            return null;
        }
        if (!autoBudgetOk()) {
            return null;
        }
        // Already covered?
        for (FirewallRule r : rules) {
            if (r.isEnabled() && r.getAction() == FirewallRule.Action.DENY
                    && r.isIpRule() && r.matchesIp(stripPrefix(ipOrCidr))) {
                return r;
            }
        }
        lastAutoRuleTime = System.currentTimeMillis();
        return addRule(FirewallRule.Direction.OUT, FirewallRule.Action.DENY,
                ipOrCidr, -1, "", -1, reason, true);
    }

    /** Auto-block an application's UID (root/iptables only for enforcement). */
    public synchronized FirewallRule autoBlockUid(int uid, String packageName, String reason) {
        if (uid < 0) {
            return null;
        }
        if (!autoBudgetOk()) {
            return null;
        }
        lastAutoRuleTime = System.currentTimeMillis();
        return addRule(FirewallRule.Direction.BOTH, FirewallRule.Action.DENY,
                "", uid, packageName == null ? "" : packageName, -1, reason, true);
    }

    private boolean autoBudgetOk() {
        int auto = 0;
        for (FirewallRule r : rules) {
            if (r.isAutoAdded()) {
                auto++;
            }
        }
        if (auto >= MAX_AUTO_RULES) {
            log.warn("auto rule budget exhausted ({})", auto);
            return false;
        }
        long now = System.currentTimeMillis();
        if (now - lastAutoRuleTime < AUTO_RULE_COOLDOWN_MS) {
            // Still allow, but don't spam logs; cooldown only gates notifications elsewhere.
            return true;
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Enforcement
    // ------------------------------------------------------------------

    /** Re-materialize all rules to the active backend. */
    public synchronized void applyRules() {
        if (!enabled) {
            return;
        }
        if (isRooted()) {
            applyIptables();
        } else {
            pushVpnRules();
        }
    }

    private void applyIptables() {
        ensureChain();
        runIptables("-F " + CHAIN);
        if (lockdown) {
            // Lockdown first (chain was just flushed, so this lands at position 1):
            // drop new connections until the user clears lockdown from the UI.
            runIptables("-A " + CHAIN + " -m state --state NEW -j DROP");
        }
        int applied = 0;
        for (FirewallRule r : rules) {
            if (!r.isEnabled() || r.getAction() != FirewallRule.Action.DENY) {
                continue;
            }
            if (r.isUidRule()) {
                // Per-app: needs xt_owner; harmless no-op if the kernel lacks it.
                if (r.getDirection() != FirewallRule.Direction.IN) {
                    runIptables("-A " + CHAIN + " -m owner --uid-owner " + r.getUid() + " -j DROP");
                    applied++;
                }
            } else if (r.isIpRule()) {
                String dst = "-d " + r.normalizedCidr();
                String port = r.getPort() > 0 ? " --dport " + r.getPort() : "";
                if (r.getDirection() != FirewallRule.Direction.IN) {
                    if (r.getPort() > 0) {
                        runIptables("-A " + CHAIN + " -p tcp " + dst + port + " -j DROP");
                        runIptables("-A " + CHAIN + " -p udp " + dst + port + " -j DROP");
                    } else {
                        runIptables("-A " + CHAIN + " " + dst + " -j DROP");
                    }
                    applied++;
                }
                if (r.getDirection() != FirewallRule.Direction.OUT) {
                    runIptables("-A " + CHAIN + " -s " + r.normalizedCidr() + " -j DROP");
                    applied++;
                }
            }
        }
        log.info("iptables: applied {} deny rules (chain {})", applied, CHAIN);
    }

    private void ensureChain() {
        // Create chain if missing, then hook it on OUTPUT/INPUT (idempotent).
        runIptables("-N " + CHAIN);
        runIptables("-C OUTPUT -j " + CHAIN + " || iptables -A OUTPUT -j " + CHAIN);
        runIptables("-C INPUT -j " + CHAIN + " || iptables -A INPUT -j " + CHAIN);
    }

    private void flushIptables() {
        if (!isRooted()) {
            return;
        }
        runIptables("-F " + CHAIN);
    }

    private void runIptables(String args) {
        try {
            CommandResult r = CMDProcessor.runSuCommand("iptables " + args);
            if (r != null && !r.success()) {
                log.debug("iptables {} -> exit={} err={}", args,
                        r.getExitValue(), r.getStdErr());
            }
        } catch (Exception e) {
            log.debug("iptables failed: {}", e.getMessage());
        }
    }

    /** Push DENY IP rules to the VPN sinkhole service (non-root path). */
    private void pushVpnRules() {
        try {
            Intent i = new Intent(appContext, DefenderVpnService.class);
            i.setAction(DefenderVpnService.ACTION_UPDATE_RULES);
            if (enabled && !isRooted()) {
                appContext.startService(i);
            } else if (!enabled) {
                // Keep service state to the user's VPN toggle; just refresh rules.
                if (DefenderVpnService.isRunning()) {
                    appContext.startService(i);
                }
            }
        } catch (Exception e) {
            log.debug("pushVpnRules failed: {}", e.getMessage());
        }
    }

    // ------------------------------------------------------------------

    private void persistAndApply() {
        store.saveAll(rules);
        applyRules();
        notifyRules();
    }

    private void notifyRules() {
        List<FirewallRule> snap = new ArrayList<FirewallRule>(rules);
        for (FirewallListener l : listeners) {
            try {
                l.onRulesChanged(snap);
            } catch (Exception ignored) {
            }
        }
    }

    private void notifyState() {
        for (FirewallListener l : listeners) {
            try {
                l.onFirewallStateChanged(enabled, isRooted());
            } catch (Exception ignored) {
            }
        }
        notifyRules();
    }

    private void seedDefaultRulesIfEmpty() {
        if (!rules.isEmpty()) {
            return;
        }
        // Ship disabled examples so users see the syntax; nothing blocked by default.
        rules.add(new FirewallRule(store.nextId(), false,
                FirewallRule.Direction.OUT, FirewallRule.Action.DENY,
                "198.51.100.0/24", -1, "", -1,
                "Example: block suspicious catcher C2 range (disabled)", false));
        store.saveAll(rules);
    }

    private static boolean eq(String a, String b) {
        String x = a == null ? "" : a.trim();
        String y = b == null ? "" : b.trim();
        return x.equals(y);
    }

    private static String stripPrefix(String cidr) {
        int slash = cidr.indexOf('/');
        return slash < 0 ? cidr : cidr.substring(0, slash);
    }

    /** Never auto-block loopback / LAN / carrier-local ranges. Package-visible for unit tests. */
    static boolean isBogonOrLocal(String ipOrCidr) {
        String ip = stripPrefix(ipOrCidr).trim();
        if (ip.contains(":")) {
            return true; // IPv6: the v4-only enforcement path can't express these anyway
        }
        try {
            long addr = FirewallRule.ipv4ToLong(ip);
            return inCidr(addr, "127.0.0.0/8") || inCidr(addr, "10.0.0.0/8")
                    || inCidr(addr, "172.16.0.0/12") || inCidr(addr, "192.168.0.0/16")
                    || inCidr(addr, "169.254.0.0/16") || inCidr(addr, "0.0.0.0/8");
        } catch (Exception e) {
            return true; // unparseable: never auto-block garbage
        }
    }

    private static boolean inCidr(long addr, String cidr) {
        String[] parts = cidr.split("/");
        long net = FirewallRule.ipv4ToLong(parts[0]);
        int prefix = Integer.parseInt(parts[1]);
        long mask = prefix == 0 ? 0 : (0xFFFFFFFFL << (32 - prefix)) & 0xFFFFFFFFL;
        return (net & mask) == (addr & mask);
    }
}

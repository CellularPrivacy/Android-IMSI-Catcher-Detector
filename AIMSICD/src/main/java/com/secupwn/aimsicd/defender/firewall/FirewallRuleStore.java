/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.defender.firewall;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import lombok.extern.slf4j.Slf4j;

/**
 * Persists {@link FirewallRule}s as escaped lines in SharedPreferences.
 * Format per line (tab separated):
 * id \t enabled \t direction \t action \t ipOrCidr \t uid \t package \t port \t auto \t reason
 */
@Slf4j
public class FirewallRuleStore {

    private static final String PREFS = "defender_firewall_rules";
    private static final String KEY_NEXT_ID = "next_id";
    private static final String KEY_RULES = "rules";

    private final SharedPreferences prefs;
    private final AtomicLong idGen;

    public FirewallRuleStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        idGen = new AtomicLong(prefs.getLong(KEY_NEXT_ID, 1L));
    }

    public synchronized long nextId() {
        long id = idGen.getAndIncrement();
        prefs.edit().putLong(KEY_NEXT_ID, idGen.get()).apply();
        return id;
    }

    public synchronized List<FirewallRule> loadAll() {
        List<FirewallRule> out = new ArrayList<FirewallRule>();
        String raw = prefs.getString(KEY_RULES, "");
        if (raw == null || raw.isEmpty()) {
            return out;
        }
        for (String line : raw.split("\n")) {
            if (line.trim().isEmpty()) {
                continue;
            }
            try {
                String[] p = line.split("\t", -1);
                if (p.length < 10) {
                    continue;
                }
                FirewallRule r = new FirewallRule(
                        Long.parseLong(p[0]),
                        "1".equals(p[1]),
                        FirewallRule.Direction.valueOf(p[2]),
                        FirewallRule.Action.valueOf(p[3]),
                        unesc(p[4]),
                        Integer.parseInt(p[5]),
                        unesc(p[6]),
                        Integer.parseInt(p[7]),
                        unesc(p[9]),
                        "1".equals(p[8]));
                out.add(r);
            } catch (Exception e) {
                log.warn("Skipping bad firewall rule line: {}", e.getMessage());
            }
        }
        return out;
    }

    public synchronized void saveAll(List<FirewallRule> rules) {
        StringBuilder sb = new StringBuilder();
        if (rules != null) {
            for (FirewallRule r : rules) {
                sb.append(r.getId()).append('\t')
                        .append(r.isEnabled() ? "1" : "0").append('\t')
                        .append(r.getDirection().name()).append('\t')
                        .append(r.getAction().name()).append('\t')
                        .append(esc(r.getTargetIpOrCidr())).append('\t')
                        .append(r.getUid()).append('\t')
                        .append(esc(r.getPackageName())).append('\t')
                        .append(r.getPort()).append('\t')
                        .append(r.isAutoAdded() ? "1" : "0").append('\t')
                        .append(esc(r.getReason())).append('\n');
            }
        }
        prefs.edit().putString(KEY_RULES, sb.toString()).apply();
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\t", " ").replace("\n", " ");
    }

    private static String unesc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\\\", "\\");
    }
}

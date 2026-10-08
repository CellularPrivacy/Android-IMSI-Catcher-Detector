/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.defender;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import lombok.extern.slf4j.Slf4j;

/**
 * Tiny ring-buffer event log persisted as human readable lines in
 * {@link SharedPreferences}. Keeps the last {@link #MAX_EVENTS} defender
 * incidents so the UI can show "recent events" without touching Realm.
 */
@Slf4j
public class DefenderLogStore {

    private static final String PREFS = "defender_event_log";
    private static final String KEY_NEXT_ID = "next_id";
    private static final String KEY_EVENTS = "events_json";
    private static final int MAX_EVENTS = 100;

    private final SharedPreferences prefs;
    private final AtomicLong idGen;
    private final List<DefenderEvent> cache = new ArrayList<DefenderEvent>();

    public DefenderLogStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        idGen = new AtomicLong(prefs.getLong(KEY_NEXT_ID, 1L));
        load();
    }

    public synchronized long nextId() {
        long id = idGen.getAndIncrement();
        prefs.edit().putLong(KEY_NEXT_ID, idGen.get()).apply();
        return id;
    }

    public synchronized void add(DefenderEvent event) {
        if (event == null) {
            return;
        }
        cache.add(0, event);
        while (cache.size() > MAX_EVENTS) {
            cache.remove(cache.size() - 1);
        }
        persist();
    }

    public synchronized List<DefenderEvent> recent(int max) {
        if (max <= 0 || max >= cache.size()) {
            return new ArrayList<DefenderEvent>(cache);
        }
        return new ArrayList<DefenderEvent>(cache.subList(0, max));
    }

    public synchronized void clear() {
        cache.clear();
        persist();
    }

    // ------------------------------------------------------------------
    // Persistence: one escaped line per event, newest first.
    // Format: id \t timeMillis \t type \t level \t title \t description \t fingerprint \t sig1|sig2
    // ------------------------------------------------------------------

    private void persist() {
        try {
            StringBuilder sb = new StringBuilder();
            for (DefenderEvent e : cache) {
                sb.append(e.getId()).append('\t')
                        .append(e.getTimestamp().getTime()).append('\t')
                        .append(e.getType().name()).append('\t')
                        .append(e.getThreatLevel().name()).append('\t')
                        .append(esc(e.getTitle())).append('\t')
                        .append(esc(e.getDescription())).append('\t')
                        .append(esc(e.getCellFingerprint())).append('\t');
                List<String> sigs = e.getSignals();
                for (int i = 0; i < sigs.size(); i++) {
                    if (i > 0) {
                        sb.append('|');
                    }
                    sb.append(esc(sigs.get(i)));
                }
                sb.append('\n');
            }
            prefs.edit().putString(KEY_EVENTS, sb.toString()).apply();
        } catch (Exception ex) {
            log.warn("DefenderLogStore.persist failed: {}", ex.getMessage());
        }
    }

    private void load() {
        try {
            String raw = prefs.getString(KEY_EVENTS, "");
            if (raw == null || raw.isEmpty()) {
                return;
            }
            String[] lines = raw.split("\n");
            List<DefenderEvent> loaded = new ArrayList<DefenderEvent>();
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] p = line.split("\t", -1);
                if (p.length < 8) {
                    continue;
                }
                long id = Long.parseLong(p[0]);
                DefenderEvent.Type type;
                DefenderAgent.ThreatLevel level;
                try {
                    type = DefenderEvent.Type.valueOf(p[2]);
                } catch (IllegalArgumentException iae) {
                    type = DefenderEvent.Type.INFO;
                }
                try {
                    level = DefenderAgent.ThreatLevel.valueOf(p[3]);
                } catch (IllegalArgumentException iae) {
                    level = DefenderAgent.ThreatLevel.NONE;
                }
                List<String> sigs = new ArrayList<String>();
                if (!p[7].isEmpty()) {
                    String[] sp = p[7].split("\\|");
                    Collections.addAll(sigs, sp);
                    List<String> unesc = new ArrayList<String>(sigs.size());
                    for (String s : sigs) {
                        unesc.add(unesc(s));
                    }
                    sigs = unesc;
                }
                DefenderEvent e = new DefenderEvent(id, type, level,
                        unesc(p[4]), unesc(p[5]), sigs, unesc(p[6]));
                loaded.add(e);
            }
            cache.clear();
            cache.addAll(loaded);
        } catch (Exception ex) {
            log.warn("DefenderLogStore.load failed: {}", ex.getMessage());
        }
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\t", " ").replace("\n", " ").replace("\r", " ").replace("|", "/");
    }

    private static String unesc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\\\", "\\");
    }
}

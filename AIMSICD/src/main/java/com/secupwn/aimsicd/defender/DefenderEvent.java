/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.defender;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A single defender incident: IMSI-catcher heuristic hit, LTE channel spoofing
 * hit, traffic anomaly or firewall auto-block.
 *
 * <p>Kept as a plain POJO (no Realm dependency) so it can be created from any
 * thread, passed to listeners, persisted by {@link DefenderLogStore} and also
 * mirrored into the legacy EventLog table.</p>
 */
public class DefenderEvent {

    /** Machine readable incident classes. */
    public enum Type {
        IMSI_CATCHER_SUSPECT,
        LTE_SPOOF_SUSPECT,
        DOWNGRADE_ATTACK,
        TRAFFIC_ANOMALY,
        FIREWALL_BLOCK,
        AUTO_PROTECT_ACTION,
        INFO
    }

    private final long id;
    private final Date timestamp;
    private final Type type;
    private final DefenderAgent.ThreatLevel threatLevel;
    private final String title;
    private final String description;
    private final List<String> signals;
    private final String cellFingerprint;

    public DefenderEvent(long id, Type type, DefenderAgent.ThreatLevel threatLevel,
                         String title, String description,
                         List<String> signals, String cellFingerprint) {
        this.id = id;
        this.timestamp = new Date();
        this.type = type == null ? Type.INFO : type;
        this.threatLevel = threatLevel == null ? DefenderAgent.ThreatLevel.NONE : threatLevel;
        this.title = title == null ? "" : title;
        this.description = description == null ? "" : description;
        this.signals = signals == null ? new ArrayList<String>() : new ArrayList<String>(signals);
        this.cellFingerprint = cellFingerprint == null ? "" : cellFingerprint;
    }

    public long getId() {
        return id;
    }

    public Date getTimestamp() {
        return new Date(timestamp.getTime());
    }

    public Type getType() {
        return type;
    }

    public DefenderAgent.ThreatLevel getThreatLevel() {
        return threatLevel;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getSignals() {
        return new ArrayList<String>(signals);
    }

    public String getCellFingerprint() {
        return cellFingerprint;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "][" + threatLevel + "][" + type + "] " + title + " :: " + description;
    }
}

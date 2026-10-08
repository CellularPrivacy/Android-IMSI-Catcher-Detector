/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.defender.firewall;

import java.util.Date;

/**
 * One firewall rule.
 *
 * <p>Rules either match a remote IP/CIDR ({@link #targetIpOrCidr}) or a local
 * application UID ({@link #uid}). IP rules work in both iptables (root) and
 * VPN sinkhole (non-root) modes; UID rules need root/iptables with the
 * {@code owner} match.</p>
 */
public class FirewallRule {

    public enum Direction { IN, OUT, BOTH }
    public enum Action { ALLOW, DENY }

    private final long id;
    private final Date created;
    private boolean enabled;
    private Direction direction;
    private Action action;
    /** Remote IP or CIDR (e.g. "203.0.113.7" or "203.0.113.0/24"), or "" for UID rules. */
    private String targetIpOrCidr;
    /** Android UID for per-app rules, or -1 for IP rules. */
    private int uid;
    /** Friendly package/app label for UID rules (may be ""). */
    private String packageName;
    /** Destination port or -1 for any. */
    private int port;
    private String reason;
    private boolean autoAdded;

    public FirewallRule(long id, boolean enabled, Direction direction, Action action,
                        String targetIpOrCidr, int uid, String packageName,
                        int port, String reason, boolean autoAdded) {
        this.id = id;
        this.created = new Date();
        this.enabled = enabled;
        this.direction = direction == null ? Direction.BOTH : direction;
        this.action = action == null ? Action.DENY : action;
        this.targetIpOrCidr = targetIpOrCidr == null ? "" : targetIpOrCidr.trim();
        this.uid = uid;
        this.packageName = packageName == null ? "" : packageName;
        this.port = port;
        this.reason = reason == null ? "" : reason;
        this.autoAdded = autoAdded;
    }

    public long getId() {
        return id;
    }

    public Date getCreated() {
        return new Date(created.getTime());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getTargetIpOrCidr() {
        return targetIpOrCidr;
    }

    public void setTargetIpOrCidr(String targetIpOrCidr) {
        this.targetIpOrCidr = targetIpOrCidr == null ? "" : targetIpOrCidr.trim();
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName == null ? "" : packageName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason == null ? "" : reason;
    }

    public boolean isAutoAdded() {
        return autoAdded;
    }

    public void setAutoAdded(boolean autoAdded) {
        this.autoAdded = autoAdded;
    }

    public boolean isIpRule() {
        return targetIpOrCidr != null && !targetIpOrCidr.isEmpty();
    }

    public boolean isUidRule() {
        return uid >= 0;
    }

    /** Human readable one-liner for lists/notifications. */
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append(action == Action.DENY ? "BLOCK " : "ALLOW ");
        if (isUidRule()) {
            sb.append(packageName.isEmpty() ? ("uid:" + uid) : packageName);
        } else {
            sb.append(targetIpOrCidr);
        }
        if (port > 0) {
            sb.append(':').append(port);
        }
        sb.append(" [").append(direction).append(']');
        if (autoAdded) {
            sb.append(" (auto)");
        }
        if (!enabled) {
            sb.append(" (disabled)");
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // IPv4 CIDR matching (pure java, no InetAddress dependency quirks)
    // ------------------------------------------------------------------

    /** True if the dotted-quad {@code ip} matches this rule's IP/CIDR. */
    public boolean matchesIp(String ip) {
        if (!isIpRule() || ip == null) {
            return false;
        }
        String cidr = targetIpOrCidr.trim();
        try {
            if (!cidr.contains("/")) {
                return cidr.equals(ip.trim());
            }
            String[] parts = cidr.split("/");
            long net = ipv4ToLong(parts[0]);
            int prefix = Integer.parseInt(parts[1]);
            long mask = prefix == 0 ? 0 : (0xFFFFFFFFL << (32 - prefix)) & 0xFFFFFFFFL;
            long addr = ipv4ToLong(ip);
            return (net & mask) == (addr & mask);
        } catch (Exception e) {
            return false;
        }
    }

    public static long ipv4ToLong(String ip) {
        String[] p = ip.trim().split("\\.");
        if (p.length != 4) {
            throw new IllegalArgumentException("bad ipv4: " + ip);
        }
        long v = 0;
        for (int i = 0; i < 4; i++) {
            v = (v << 8) | (Long.parseLong(p[i]) & 0xFF);
        }
        return v & 0xFFFFFFFFL;
    }

    public static String longToIpv4(long v) {
        return ((v >> 24) & 0xFF) + "." + ((v >> 16) & 0xFF) + "."
                + ((v >> 8) & 0xFF) + "." + (v & 0xFF);
    }

    /** Normalize "ip" -> "ip/32" so iptables + VPN routes handle both forms. */
    public String normalizedCidr() {
        if (!isIpRule()) {
            return "";
        }
        if (targetIpOrCidr.contains("/")) {
            return targetIpOrCidr;
        }
        return targetIpOrCidr + "/32";
    }
}

/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.defender.traffic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * Best-effort parser for {@code /proc/net/tcp[6]} and {@code /proc/net/udp[6]}.
 *
 * <p>Maps live connections to owning UID + remote IP:port so the traffic
 * monitor can attribute suspicious uploads to a concrete destination and the
 * firewall auto-rule adder can sinkhole it. Works without root on most
 * devices; on SELinux-hardened ROMs the files may be unreadable, in which case
 * we simply return an empty list.</p>
 */
@Slf4j
public final class ProcNetConnectionScanner {

    /** One live socket. */
    public static class Connection {
        public final String protocol; // TCP/UDP
        public final String localIp;
        public final int localPort;
        public final String remoteIp;
        public final int remotePort;
        public final String state;    // ESTABLISHED etc (TCP)
        public final int uid;

        public Connection(String protocol, String localIp, int localPort,
                          String remoteIp, int remotePort, String state, int uid) {
            this.protocol = protocol;
            this.localIp = localIp;
            this.localPort = localPort;
            this.remoteIp = remoteIp;
            this.remotePort = remotePort;
            this.state = state;
            this.uid = uid;
        }

        public boolean isEstablished() {
            return "ESTABLISHED".equals(state) || "UDP".equals(protocol);
        }

        @Override
        public String toString() {
            return protocol + " " + localIp + ":" + localPort + " -> "
                    + remoteIp + ":" + remotePort + " " + state + " uid=" + uid;
        }
    }

    private static final Map<String, String> TCP_STATES = new HashMap<String, String>();

    static {
        TCP_STATES.put("01", "ESTABLISHED");
        TCP_STATES.put("02", "SYN_SENT");
        TCP_STATES.put("03", "SYN_RECV");
        TCP_STATES.put("04", "FIN_WAIT1");
        TCP_STATES.put("05", "FIN_WAIT2");
        TCP_STATES.put("06", "TIME_WAIT");
        TCP_STATES.put("07", "CLOSE");
        TCP_STATES.put("08", "CLOSE_WAIT");
        TCP_STATES.put("09", "LAST_ACK");
        TCP_STATES.put("0A", "LISTEN");
        TCP_STATES.put("0B", "CLOSING");
    }

    private ProcNetConnectionScanner() {
    }

    /** Scan all four proc files; never throws. */
    public static List<Connection> scan() {
        List<Connection> out = new ArrayList<Connection>();
        out.addAll(parseFile("/proc/net/tcp", "TCP", false));
        out.addAll(parseFile("/proc/net/tcp6", "TCP", true));
        out.addAll(parseFile("/proc/net/udp", "UDP", false));
        out.addAll(parseFile("/proc/net/udp6", "UDP", true));
        return out;
    }

    /** Remote IPs currently fed by the given UID (established only). */
    public static List<String> remoteIpsForUid(int uid) {
        List<String> ips = new ArrayList<String>();
        for (Connection c : scan()) {
            if (c.uid == uid && c.isEstablished()
                    && !"0.0.0.0".equals(c.remoteIp) && !"::".equals(c.remoteIp)) {
                if (!ips.contains(c.remoteIp)) {
                    ips.add(c.remoteIp);
                }
            }
        }
        return ips;
    }

    private static List<Connection> parseFile(String path, String proto, boolean v6) {
        List<Connection> out = new ArrayList<Connection>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] f = line.split("\\s+");
                if (f.length < 8) {
                    continue;
                }
                try {
                    String[] local = splitAddr(f[1], v6);
                    String[] remote = splitAddr(f[2], v6);
                    String state = v6 || "UDP".equals(proto)
                            ? tcpStateName(f[3]) : tcpStateName(f[3]);
                    if ("UDP".equals(proto)) {
                        state = "UDP";
                    }
                    int uid = Integer.parseInt(f[7]);
                    out.add(new Connection(proto, local[0], Integer.parseInt(local[1]),
                            remote[0], Integer.parseInt(remote[1]), state, uid));
                } catch (Exception inner) {
                    // Skip malformed row.
                }
            }
        } catch (Exception e) {
            log.debug("ProcNet {} unreadable: {}", path, e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception ignored) {
                }
            }
        }
        return out;
    }

    private static String[] splitAddr(String hex, boolean v6) {
        int colon = hex.lastIndexOf(':');
        String ipHex = hex.substring(0, colon);
        int port = Integer.parseInt(hex.substring(colon + 1), 16);
        return new String[]{v6 ? parseIpv6(ipHex) : parseIpv4(ipHex), String.valueOf(port)};
    }

    // Package-visible for unit tests.
    static String parseIpv4(String hex) {
        // Little-endian hex, e.g. 0100007F -> 127.0.0.1
        try {
            long v = Long.parseLong(hex, 16);
            return ((v) & 0xFF) + "." + ((v >> 8) & 0xFF) + "."
                    + ((v >> 16) & 0xFF) + "." + ((v >> 24) & 0xFF);
        } catch (Exception e) {
            return "0.0.0.0";
        }
    }

    // Package-visible for unit tests.
    static String parseIpv6(String hex) {
        try {
            // 32 hex chars, 4x 32-bit words little-endian each.
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                String word = hex.substring(i * 8, i * 8 + 8);
                long w = Long.parseLong(word, 16);
                // Byte-swap the 32-bit word.
                long swapped = ((w & 0xFF) << 24) | (((w >> 8) & 0xFF) << 16)
                        | (((w >> 16) & 0xFF) << 8) | ((w >> 24) & 0xFF);
                sb.append(String.format("%04x", (swapped >> 16) & 0xFFFF));
                sb.append(':');
                sb.append(String.format("%04x", swapped & 0xFFFF));
                if (i < 3) {
                    sb.append(':');
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "::";
        }
    }

    // Package-visible for unit tests.
    static String tcpStateName(String hex) {
        String s = TCP_STATES.get(hex.toUpperCase());
        return s == null ? hex : s;
    }
}

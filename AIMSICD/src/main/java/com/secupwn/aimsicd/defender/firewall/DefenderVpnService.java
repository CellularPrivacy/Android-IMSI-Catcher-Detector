/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.defender.firewall;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;

import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

/**
 * Non-root firewall backend: a <b>sinkhole VPN</b>.
 *
 * <p>Instead of tunneling all traffic (which would need a userspace TCP/IP
 * stack to forward), this service only routes the firewall's DENY CIDRs into
 * its TUN interface via {@link Builder#addRoute}. Packets matching a DENY rule
 * are pulled into the TUN and dropped (counted for the UI); everything else
 * bypasses the VPN and flows normally. This gives working non-root blocking
 * for concrete attacker IPs/CIDRs that the auto rule adder discovers, without
 * breaking connectivity and without a fake "full tunnel".</p>
 *
 * <p>Global in/out monitoring stays in
 * {@link com.secupwn.aimsicd.defender.traffic.TrafficMonitor} (TrafficStats),
 * which needs no VPN at all.</p>
 */
@Slf4j
public class DefenderVpnService extends VpnService {

    public static final String ACTION_START = "com.secupwn.aimsicd.defender.firewall.VPN_START";
    public static final String ACTION_STOP = "com.secupwn.aimsicd.defender.firewall.VPN_STOP";
    public static final String ACTION_UPDATE_RULES =
            "com.secupwn.aimsicd.defender.firewall.VPN_UPDATE_RULES";

    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);
    private static volatile long droppedPackets;
    private static volatile long droppedBytes;
    private static volatile int routeCount;

    private ParcelFileDescriptor tun;
    private Thread readerThread;
    private volatile boolean wantRun;
    /** Bumped on every closeTun(): lets a replaced reader exit itself. */
    private volatile long tunGeneration;

    public static boolean isRunning() {
        return RUNNING.get();
    }

    public static long getDroppedPackets() {
        return droppedPackets;
    }

    public static long getDroppedBytes() {
        return droppedBytes;
    }

    public static int getRouteCount() {
        return routeCount;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? ACTION_START : intent.getAction();
        if (ACTION_STOP.equals(action)) {
            stopSinkhole();
            stopSelf();
            return START_NOT_STICKY;
        }
        // START or UPDATE_RULES: (re)build the TUN with current DENY routes.
        startSinkhole();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopSinkhole();
        super.onDestroy();
    }

    @Override
    public void onRevoke() {
        stopSinkhole();
        super.onRevoke();
    }

    private synchronized void startSinkhole() {
        // Tear down any previous instance first (rules may have changed).
        closeTun();

        List<FirewallRule> denyRules;
        try {
            // NB: store-only read here; no iptables calls from the VPN service.
            FirewallRuleStore store = new FirewallRuleStore(this);
            denyRules = store.loadAll();
            // Filter to enabled DENY IP rules.
            for (int i = denyRules.size() - 1; i >= 0; i--) {
                FirewallRule r = denyRules.get(i);
                if (!r.isEnabled() || r.getAction() != FirewallRule.Action.DENY || !r.isIpRule()) {
                    denyRules.remove(i);
                }
            }
        } catch (Exception e) {
            log.warn("VPN sinkhole: rule load failed: {}", e.getMessage());
            return;
        }

        try {
            Builder b = new Builder();
            b.setSession("AIMSICD Defender Firewall");
            b.addAddress("10.8.77.1", 24);
            // NB: no addDnsServer — a sinkhole forwards nothing, so it must
            // not advertise itself as a resolver.
            b.setMtu(1400);
            int routes = 0;
            for (FirewallRule r : denyRules) {
                try {
                    String cidr = r.normalizedCidr();
                    int slash = cidr.indexOf('/');
                    String addr = slash < 0 ? cidr : cidr.substring(0, slash);
                    int prefix = slash < 0 ? 32 : Integer.parseInt(cidr.substring(slash + 1));
                    if (addr.contains(":")) {
                        continue; // v4-only sinkhole for now
                    }
                    b.addRoute(addr, prefix);
                    routes++;
                    if (routes >= 128) {
                        break; // keep the builder light
                    }
                } catch (Exception inner) {
                    log.debug("Bad VPN route {}: {}", r.getTargetIpOrCidr(), inner.getMessage());
                }
            }
            routeCount = routes;
            if (routes == 0) {
                // No DENY routes: nothing to sinkhole. Stay stopped so we don't
                // hold the VPN slot / key icon for no reason.
                log.info("VPN sinkhole: no DENY IP rules, not starting TUN.");
                RUNNING.set(false);
                return;
            }
            tun = b.establish();
            if (tun == null) {
                log.warn("VPN sinkhole: establish() returned null (permission revoked?).");
                RUNNING.set(false);
                return;
            }
            wantRun = true;
            RUNNING.set(true);
            startReader();
            log.info("VPN sinkhole started with {} DENY routes.", routes);
        } catch (SecurityException se) {
            log.warn("VPN sinkhole: no VPN permission: {}", se.getMessage());
            RUNNING.set(false);
        } catch (Exception e) {
            log.warn("VPN sinkhole start failed: {}", e.getMessage());
            RUNNING.set(false);
            closeTun();
        }
    }

    private synchronized void stopSinkhole() {
        wantRun = false;
        RUNNING.set(false);
        closeTun();
        if (readerThread != null) {
            readerThread.interrupt();
            readerThread = null;
        }
        log.info("VPN sinkhole stopped. dropped={} pkts / {} bytes",
                droppedPackets, droppedBytes);
    }

    private void closeTun() {
        wantRun = false;
        tunGeneration++;
        Thread old = readerThread;
        readerThread = null;
        if (old != null) {
            // NB: interrupt alone can't unblock FileInputStream.read(); the
            // fd close below is what releases the reader.
            old.interrupt();
        }
        if (tun != null) {
            try {
                tun.close();
            } catch (Exception ignored) {
            }
            tun = null;
        }
        if (old != null) {
            try {
                // Wait briefly so TUN rebuilds (rule updates) don't leak a
                // reader thread spinning on a closed fd.
                old.join(2000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    /**
     * Drain the TUN: every packet arriving here matched a DENY route, so we
     * count + drop it. Also parses the IPv4 header for logging (src/dst).
     */
    private void startReader() {
        final ParcelFileDescriptor fd = tun;
        final long myGeneration = tunGeneration;
        readerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                FileInputStream in = new FileInputStream(fd.getFileDescriptor());
                byte[] buf = new byte[2048]; // headroom above the 1400 MTU
                // Exit if replaced by a newer TUN generation even when the
                // closeTun() join timed out and wantRun flipped back to true.
                while (wantRun && tunGeneration == myGeneration) {
                    try {
                        int n = in.read(buf);
                        if (n <= 0) {
                            // End of stream / closed.
                            Thread.sleep(200);
                            continue;
                        }
                        droppedPackets++;
                        droppedBytes += n;
                        if ((droppedPackets % 25) == 1) {
                            log.debug("VPN sinkhole dropped {} ({} bytes total)",
                                    describeIpv4(buf, n), droppedBytes);
                        }
                    } catch (InterruptedException ie) {
                        break;
                    } catch (Exception e) {
                        if (!wantRun) {
                            break;
                        }
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException ie2) {
                            break;
                        }
                    }
                }
                try {
                    in.close();
                } catch (Exception ignored) {
                }
            }
        }, "DefenderVpnReader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private static String describeIpv4(byte[] pkt, int len) {
        try {
            if (len < 20 || (pkt[0] >> 4) != 4) {
                return "non-v4 pkt len=" + len;
            }
            String src = (pkt[12] & 0xFF) + "." + (pkt[13] & 0xFF) + "."
                    + (pkt[14] & 0xFF) + "." + (pkt[15] & 0xFF);
            String dst = (pkt[16] & 0xFF) + "." + (pkt[17] & 0xFF) + "."
                    + (pkt[18] & 0xFF) + "." + (pkt[19] & 0xFF);
            int proto = pkt[9] & 0xFF;
            return "IPv4 " + src + " -> " + dst + " proto=" + proto;
        } catch (Exception e) {
            return "pkt len=" + len;
        }
    }
}

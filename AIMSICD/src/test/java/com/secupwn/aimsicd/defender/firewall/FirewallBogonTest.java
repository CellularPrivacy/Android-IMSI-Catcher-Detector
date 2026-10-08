package com.secupwn.aimsicd.defender.firewall;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The auto rule adder must never block loopback/LAN/link-local ranges, but
 * must not mistake public space (e.g. 172.200.x.x) for the 172.16/12 block.
 */
public class FirewallBogonTest {

    @Test
    public void testLocalRangesAreBogon() {
        assertTrue(FirewallManager.isBogonOrLocal("127.0.0.1"));
        assertTrue(FirewallManager.isBogonOrLocal("127.0.0.1/32"));
        assertTrue(FirewallManager.isBogonOrLocal("10.5.6.7"));
        assertTrue(FirewallManager.isBogonOrLocal("10.0.0.0/8"));
        assertTrue(FirewallManager.isBogonOrLocal("172.16.0.1"));
        assertTrue(FirewallManager.isBogonOrLocal("172.29.0.1"));
        assertTrue(FirewallManager.isBogonOrLocal("172.31.255.255"));
        assertTrue(FirewallManager.isBogonOrLocal("192.168.1.1"));
        assertTrue(FirewallManager.isBogonOrLocal("169.254.10.20"));
        assertTrue(FirewallManager.isBogonOrLocal("0.0.0.0"));
    }

    @Test
    public void testPublicRangesAreNotBogon() {
        assertFalse(FirewallManager.isBogonOrLocal("8.8.8.8"));
        assertFalse(FirewallManager.isBogonOrLocal("8.8.8.8/24"));
        assertFalse(FirewallManager.isBogonOrLocal("203.0.113.7"));
        assertFalse(FirewallManager.isBogonOrLocal("192.167.1.1"));
        assertFalse(FirewallManager.isBogonOrLocal("172.15.0.1"));
        assertFalse(FirewallManager.isBogonOrLocal("172.32.0.1"));
        // Regression: a naive "172.2" startsWith check swallows public 172.200+ space.
        assertFalse(FirewallManager.isBogonOrLocal("172.200.1.1"));
        assertFalse(FirewallManager.isBogonOrLocal("172.217.3.110"));
    }

    @Test
    public void testIpv6AndGarbageAreSkipped() {
        // v6 + garbage can never be expressed by the v4 enforcement path.
        assertTrue(FirewallManager.isBogonOrLocal("::1"));
        assertTrue(FirewallManager.isBogonOrLocal("fe80::1"));
        assertTrue(FirewallManager.isBogonOrLocal("garbage"));
        assertTrue(FirewallManager.isBogonOrLocal(""));
    }
}

package com.secupwn.aimsicd.defender.firewall;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link FirewallRule} CIDR matching and helpers.
 * Pure JVM (no Android runtime needed).
 */
public class FirewallRuleTest {

    private static FirewallRule ipRule(String ipOrCidr) {
        return new FirewallRule(1L, true, FirewallRule.Direction.OUT,
                FirewallRule.Action.DENY, ipOrCidr, -1, "", -1, "test", false);
    }

    @Test
    public void testExactIpMatch() {
        FirewallRule rule = ipRule("203.0.113.7");
        assertTrue(rule.matchesIp("203.0.113.7"));
        assertFalse(rule.matchesIp("203.0.113.8"));
        assertFalse(rule.matchesIp(null));
    }

    @Test
    public void testCidr24Match() {
        FirewallRule rule = ipRule("203.0.113.0/24");
        assertTrue(rule.matchesIp("203.0.113.1"));
        assertTrue(rule.matchesIp("203.0.113.200"));
        assertFalse(rule.matchesIp("203.0.114.1"));
        assertFalse(rule.matchesIp("8.8.8.8"));
    }

    @Test
    public void testCidrBoundaries() {
        assertTrue(ipRule("10.0.0.0/8").matchesIp("10.255.255.255"));
        assertFalse(ipRule("10.0.0.0/8").matchesIp("11.0.0.1"));
        assertTrue(ipRule("192.168.1.0/24").matchesIp("192.168.1.0"));
        // /32 behaves like an exact match, /0 matches everything.
        assertTrue(ipRule("1.2.3.4/32").matchesIp("1.2.3.4"));
        assertFalse(ipRule("1.2.3.4/32").matchesIp("1.2.3.5"));
        assertTrue(ipRule("0.0.0.0/0").matchesIp("123.45.67.89"));
    }

    @Test
    public void testInvalidRuleNeverMatchesNorThrows() {
        assertFalse(ipRule("not-an-ip").matchesIp("1.2.3.4"));
        assertFalse(ipRule("not-an-ip").matchesIp("not-an-ip-either"));
    }

    @Test
    public void testNormalizedCidr() {
        assertEquals("1.2.3.4/32", ipRule("1.2.3.4").normalizedCidr());
        assertEquals("1.2.3.0/24", ipRule("1.2.3.0/24").normalizedCidr());
    }

    @Test
    public void testIpv4Conversions() {
        assertEquals(0x7F000001L, FirewallRule.ipv4ToLong("127.0.0.1"));
        assertEquals(0xFFFFFFFFL, FirewallRule.ipv4ToLong("255.255.255.255"));
        assertEquals(0L, FirewallRule.ipv4ToLong("0.0.0.0"));
        assertEquals("192.168.0.13", FirewallRule.longToIpv4(FirewallRule.ipv4ToLong("192.168.0.13")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIpv4ToLongRejectsShortAddress() {
        FirewallRule.ipv4ToLong("1.2.3");
    }

    @Test
    public void testRuleKindAndDescription() {
        FirewallRule ip = ipRule("9.9.9.9");
        assertTrue(ip.isIpRule());
        assertFalse(ip.isUidRule());
        assertTrue(ip.describe().contains("BLOCK"));
        assertTrue(ip.describe().contains("9.9.9.9"));

        FirewallRule uid = new FirewallRule(2L, false, FirewallRule.Direction.BOTH,
                FirewallRule.Action.DENY, "", 10123, "Some App", -1, "r", true);
        assertTrue(uid.isUidRule());
        assertFalse(uid.isIpRule());
        assertTrue(uid.describe().contains("Some App"));
        assertTrue(uid.describe().contains("(auto)"));
        assertTrue(uid.describe().contains("(disabled)"));
    }
}

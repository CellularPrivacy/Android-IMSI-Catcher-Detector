package com.secupwn.aimsicd.defender.traffic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the /proc/net address parsers. /proc stores IPv4 in
 * little-endian hex (0100007F = 127.0.0.1) and IPv6 as four
 * little-endian 32-bit words.
 */
public class ProcNetParsingTest {

    @Test
    public void testParseIpv4() {
        assertEquals("127.0.0.1", ProcNetConnectionScanner.parseIpv4("0100007F"));
        assertEquals("192.168.0.13", ProcNetConnectionScanner.parseIpv4("0D00A8C0"));
        assertEquals("8.8.8.8", ProcNetConnectionScanner.parseIpv4("08080808"));
    }

    @Test
    public void testParseIpv4Malformed() {
        assertEquals("0.0.0.0", ProcNetConnectionScanner.parseIpv4("ZZZ"));
        assertEquals("0.0.0.0", ProcNetConnectionScanner.parseIpv4(""));
    }

    @Test
    public void testParseIpv6() {
        // ::ffff:127.0.0.1 in /proc's word-swapped layout (no zero compression here).
        assertEquals("0000:0000:0000:0000:0000:ffff:7f00:0001",
                ProcNetConnectionScanner.parseIpv6("0000000000000000FFFF00000100007F"));
    }

    @Test
    public void testParseIpv6Malformed() {
        assertEquals("::", ProcNetConnectionScanner.parseIpv6("short"));
    }

    @Test
    public void testTcpStateName() {
        assertEquals("ESTABLISHED", ProcNetConnectionScanner.tcpStateName("01"));
        assertEquals("LISTEN", ProcNetConnectionScanner.tcpStateName("0A"));
        assertEquals("LISTEN", ProcNetConnectionScanner.tcpStateName("0a"));
        assertEquals("TIME_WAIT", ProcNetConnectionScanner.tcpStateName("06"));
        // Unknown states pass through untouched.
        assertEquals("FF", ProcNetConnectionScanner.tcpStateName("FF"));
    }
}

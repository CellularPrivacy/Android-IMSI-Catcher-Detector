package com.secupwn.aimsicd.defender;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for threat-score mapping and spoof-result clamping.
 *
 * <p>Note: {@link LteChannelSpoofDetector#observe} itself is not covered here
 * because it emits an slf4j log on the suspect path, and the slf4j-android
 * binding delegates to android.util.Log, which throws on a plain JVM; the
 * pure scoring math below is what CI can verify.</p>
 */
public class DefenderScoreTest {

    @Test
    public void testScoreToLevelBoundaries() {
        assertEquals(DefenderAgent.ThreatLevel.NONE, DefenderAgent.scoreToLevel(-5));
        assertEquals(DefenderAgent.ThreatLevel.NONE, DefenderAgent.scoreToLevel(0));
        assertEquals(DefenderAgent.ThreatLevel.NONE, DefenderAgent.scoreToLevel(14));
        assertEquals(DefenderAgent.ThreatLevel.LOW, DefenderAgent.scoreToLevel(15));
        assertEquals(DefenderAgent.ThreatLevel.LOW, DefenderAgent.scoreToLevel(34));
        assertEquals(DefenderAgent.ThreatLevel.MEDIUM, DefenderAgent.scoreToLevel(35));
        assertEquals(DefenderAgent.ThreatLevel.MEDIUM, DefenderAgent.scoreToLevel(54));
        assertEquals(DefenderAgent.ThreatLevel.HIGH, DefenderAgent.scoreToLevel(55));
        assertEquals(DefenderAgent.ThreatLevel.HIGH, DefenderAgent.scoreToLevel(79));
        assertEquals(DefenderAgent.ThreatLevel.CRITICAL, DefenderAgent.scoreToLevel(80));
        assertEquals(DefenderAgent.ThreatLevel.CRITICAL, DefenderAgent.scoreToLevel(100));
        assertEquals(DefenderAgent.ThreatLevel.CRITICAL, DefenderAgent.scoreToLevel(1000));
    }

    @Test
    public void testSpoofResultClampsAndFlagsSuspect() {
        LteChannelSpoofDetector.SpoofResult hi = new LteChannelSpoofDetector.SpoofResult(
                120, new ArrayList<String>(), null);
        assertEquals(100, hi.score);
        assertTrue(hi.isSuspect());

        LteChannelSpoofDetector.SpoofResult lo = new LteChannelSpoofDetector.SpoofResult(
                -10, new ArrayList<String>(), null);
        assertEquals(0, lo.score);
        assertFalse(lo.isSuspect());

        LteChannelSpoofDetector.SpoofResult edge = new LteChannelSpoofDetector.SpoofResult(
                39, new ArrayList<String>(), null);
        assertFalse(edge.isSuspect());

        LteChannelSpoofDetector.SpoofResult suspect = new LteChannelSpoofDetector.SpoofResult(
                40, new ArrayList<String>(), null);
        assertTrue(suspect.isSuspect());
    }
}

package com.secupwn.aimsicd.protection;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the APN matching helpers.
 *
 * <p>Only the Android-free static helpers are covered here: anything touching
 * {@code ContentResolver}, {@code Realm} or slf4j logging cannot run on a plain JVM
 * (see the note in {@code DefenderScoreTest}).</p>
 */
public class ApnGuardTest {

    @Test
    public void testNormalizeApn() {
        assertEquals("", ApnGuard.normalizeApn(null));
        assertEquals("", ApnGuard.normalizeApn(""));
        assertEquals("", ApnGuard.normalizeApn("   "));
        assertEquals("internet", ApnGuard.normalizeApn("internet"));
        assertEquals("internet", ApnGuard.normalizeApn("  Internet  "));
        assertEquals("ims", ApnGuard.normalizeApn("IMS"));
    }

    @Test
    public void testApnMatchesEitherColumn() {
        // Exact APN-address match.
        assertTrue(ApnGuard.apnMatches("internet", "My Carrier", "internet"));
        // Display-name match (user typed what they see in the system APN screen).
        assertTrue(ApnGuard.apnMatches("My Carrier", "My Carrier", "internet"));
        // Case-insensitive + whitespace tolerant.
        assertTrue(ApnGuard.apnMatches("  INTERNET ", "other", "Internet"));
        assertTrue(ApnGuard.apnMatches("my carrier", "My Carrier", "internet"));
    }

    @Test
    public void testApnMatchesRejects() {
        assertFalse(ApnGuard.apnMatches("internet", "My Carrier", "wap.carrier"));
        assertFalse(ApnGuard.apnMatches("internet", "My Carrier", null));
        assertFalse(ApnGuard.apnMatches("internet", null, null));
        // Empty expectation never matches (caller treats it as "no binding").
        assertFalse(ApnGuard.apnMatches("", "My Carrier", "internet"));
        assertFalse(ApnGuard.apnMatches(null, "My Carrier", "internet"));
        assertFalse(ApnGuard.apnMatches("   ", "My Carrier", "internet"));
    }

    @Test
    public void testPickBaselineValuePrefersApnColumn() {
        assertEquals("internet", ApnGuard.pickBaselineValue("My Carrier", "internet"));
        assertEquals("internet", ApnGuard.pickBaselineValue("My Carrier", "  internet  "));
        // Fallback to the display name when the APN address is missing.
        assertEquals("My Carrier", ApnGuard.pickBaselineValue("My Carrier", null));
        assertEquals("My Carrier", ApnGuard.pickBaselineValue("My Carrier", ""));
        assertEquals("", ApnGuard.pickBaselineValue(null, null));
    }

    @Test
    public void testCurrentApnIsEmpty() {
        assertTrue(new ApnGuard.CurrentApn(null, null).isEmpty());
        assertTrue(new ApnGuard.CurrentApn("", "").isEmpty());
        assertFalse(new ApnGuard.CurrentApn("My Carrier", null).isEmpty());
        assertFalse(new ApnGuard.CurrentApn(null, "internet").isEmpty());
    }
}

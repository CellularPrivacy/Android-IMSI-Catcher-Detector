package com.secupwn.aimsicd.defender.traffic;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link TrafficSnapshot} formatting and talker ordering.
 */
public class TrafficSnapshotTest {

    @Test
    public void testFormatRate() {
        assertEquals("n/a", TrafficSnapshot.formatRate(-1));
        assertEquals("0 B/s", TrafficSnapshot.formatRate(0));
        assertEquals("1023 B/s", TrafficSnapshot.formatRate(1023));
        assertEquals("1.0 KB/s", TrafficSnapshot.formatRate(1024));
        assertEquals("1.5 KB/s", TrafficSnapshot.formatRate(1536));
        assertEquals("1.00 MB/s", TrafficSnapshot.formatRate(1024 * 1024));
    }

    @Test
    public void testFormatTotal() {
        assertEquals("n/a", TrafficSnapshot.formatTotal(-1));
        assertEquals("0 B", TrafficSnapshot.formatTotal(0));
        assertEquals("1023 B", TrafficSnapshot.formatTotal(1023));
        assertEquals("1.0 KB", TrafficSnapshot.formatTotal(1024));
        assertEquals("1.00 MB", TrafficSnapshot.formatTotal(1024 * 1024));
        assertEquals("1.00 GB", TrafficSnapshot.formatTotal(1024L * 1024L * 1024L));
    }

    @Test
    public void testTopTalkersSortedByTotalRateDesc() {
        List<TrafficSnapshot.UidRate> in = new ArrayList<TrafficSnapshot.UidRate>();
        in.add(new TrafficSnapshot.UidRate(1, "quiet", 10, 10));
        in.add(new TrafficSnapshot.UidRate(2, "loud", 0, 5000));
        in.add(new TrafficSnapshot.UidRate(3, "mid", 100, 100));
        TrafficSnapshot snap = new TrafficSnapshot(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, in);
        List<TrafficSnapshot.UidRate> out = snap.getTopTalkers();
        assertEquals(3, out.size());
        assertEquals("loud", out.get(0).packageName);
        assertEquals("mid", out.get(1).packageName);
        assertEquals("quiet", out.get(2).packageName);
    }
}

/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.defender.traffic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One traffic sample: global in/out counters plus per-app rates.
 */
public class TrafficSnapshot {

    /** Per-UID talker row. */
    public static class UidRate implements Comparable<UidRate> {
        public final int uid;
        public final String packageName;
        public final long rxBytesPerSec;
        public final long txBytesPerSec;

        public UidRate(int uid, String packageName, long rxBytesPerSec, long txBytesPerSec) {
            this.uid = uid;
            this.packageName = packageName == null ? ("uid:" + uid) : packageName;
            this.rxBytesPerSec = rxBytesPerSec;
            this.txBytesPerSec = txBytesPerSec;
        }

        public long totalPerSec() {
            return rxBytesPerSec + txBytesPerSec;
        }

        @Override
        public int compareTo(UidRate o) {
            return Long.compare(o.totalPerSec(), totalPerSec());
        }
    }

    public final long timeMillis;
    public final long totalRxBytes;
    public final long totalTxBytes;
    public final long mobileRxBytes;
    public final long mobileTxBytes;
    public final long rxBytesPerSec;
    public final long txBytesPerSec;
    public final long mobileRxPerSec;
    public final long mobileTxPerSec;
    private final List<UidRate> topTalkers;

    public TrafficSnapshot(long timeMillis,
                           long totalRxBytes, long totalTxBytes,
                           long mobileRxBytes, long mobileTxBytes,
                           long rxBytesPerSec, long txBytesPerSec,
                           long mobileRxPerSec, long mobileTxPerSec,
                           List<UidRate> topTalkers) {
        this.timeMillis = timeMillis;
        this.totalRxBytes = totalRxBytes;
        this.totalTxBytes = totalTxBytes;
        this.mobileRxBytes = mobileRxBytes;
        this.mobileTxBytes = mobileTxBytes;
        this.rxBytesPerSec = rxBytesPerSec;
        this.txBytesPerSec = txBytesPerSec;
        this.mobileRxPerSec = mobileRxPerSec;
        this.mobileTxPerSec = mobileTxPerSec;
        this.topTalkers = topTalkers == null
                ? new ArrayList<UidRate>() : new ArrayList<UidRate>(topTalkers);
        Collections.sort(this.topTalkers);
    }

    public List<UidRate> getTopTalkers() {
        return new ArrayList<UidRate>(topTalkers);
    }

    public static String formatRate(long bytesPerSec) {
        if (bytesPerSec < 0) {
            return "n/a";
        }
        if (bytesPerSec < 1024) {
            return bytesPerSec + " B/s";
        } else if (bytesPerSec < 1024 * 1024) {
            return String.format("%.1f KB/s", bytesPerSec / 1024.0);
        } else {
            return String.format("%.2f MB/s", bytesPerSec / (1024.0 * 1024.0));
        }
    }

    public static String formatTotal(long bytes) {
        if (bytes < 0) {
            return "n/a";
        }
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024L * 1024L * 1024L) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}

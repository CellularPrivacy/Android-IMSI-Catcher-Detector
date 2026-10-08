/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.defender;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.secupwn.aimsicd.R;
import com.secupwn.aimsicd.defender.firewall.FirewallManager;
import com.secupwn.aimsicd.ui.activities.MainActivity;
import com.secupwn.aimsicd.utils.CMDProcessor;
import com.secupwn.aimsicd.utils.CommandResult;
import com.secupwn.aimsicd.utils.Helpers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Executes automatic countermeasures when {@link DefenderAgent} raises a
 * threat.
 *
 * <p>Escalation ladder (all steps are best-effort and logged):</p>
 * <ol>
 *   <li>MEDIUM+: loud notification + vibration + event log.</li>
 *   <li>HIGH+: also firewall lockdown of freshly flagged destinations and
 *   (if enabled) mobile-data lockdown.</li>
 *   <li>CRITICAL: also radio reset (airplane-mode pulse) when root is
 *   available; otherwise opens guidance for the user.</li>
 * </ol>
 *
 * <p>Every aggressive action has a cooldown so flapping cells can't DoS the
 * user's own connectivity.</p>
 */
@Slf4j
public class AutoProtectController {

    public static final String PREF_AUTO_PROTECT = "pref_defender_auto_protect";
    public static final String PREF_DATA_LOCKDOWN = "pref_defender_data_lockdown";
    public static final String PREF_RADIO_RESET = "pref_defender_radio_reset";
    public static final String PREF_NOTIFY_ONLY_BELOW = "pref_defender_notify_level"; // ordinal

    private static final int NOTIF_ID = 0xDEF1;
    private static final long ACTION_COOLDOWN_MS = 90000L; // 90 s between aggressive actions
    private static final long NOTIF_COOLDOWN_MS = 15000L;

    private final Context appContext;
    private final FirewallManager firewall;

    /**
     * Aggressive actions (su shell-outs, sleeps, iptables) must never run on
     * the caller thread: onThreat() is invoked from the main thread on the
     * cell-update path. Single daemon thread, process-lifetime, no shutdown
     * needed (controller is a singleton owned by DefenderAgent).
     */
    private final ExecutorService actions = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "DefenderActions");
            t.setDaemon(true);
            return t;
        }
    });

    private volatile long lastAggressiveAction;
    private volatile long lastNotification;

    public AutoProtectController(Context context, FirewallManager firewall) {
        this.appContext = context.getApplicationContext();
        this.firewall = firewall;
    }

    public boolean isAutoProtectEnabled() {
        return prefs().getBoolean(PREF_AUTO_PROTECT, false);
    }

    public void setAutoProtectEnabled(boolean enable) {
        prefs().edit().putBoolean(PREF_AUTO_PROTECT, enable).apply();
        log.info("Auto-protect {}", enable ? "ENABLED" : "DISABLED");
    }

    /**
     * Main entry: called for every defender incident at/above MEDIUM.
     */
    public void onThreat(DefenderEvent event) {
        if (event == null) {
            return;
        }
        DefenderAgent.ThreatLevel level = event.getThreatLevel();
        if (level.ordinal() < DefenderAgent.ThreatLevel.MEDIUM.ordinal()) {
            return;
        }

        notifyUser(event);

        if (!isAutoProtectEnabled()) {
            log.debug("Auto-protect off; notification only for {}", event.getTitle());
            return;
        }

        if (level.ordinal() < DefenderAgent.ThreatLevel.HIGH.ordinal()) {
            // MEDIUM = notify-only tier (already done above).
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastAggressiveAction < ACTION_COOLDOWN_MS) {
            log.debug("Auto-protect cooldown active; skipping aggressive actions.");
            return;
        }
        // Claim the cooldown slot on the caller thread, then do the blocking
        // work (su, iptables, sleeps) in the background: this method runs on
        // the main thread for cell-update incidents.
        lastAggressiveAction = now;
        final DefenderEvent cause = event;
        final DefenderAgent.ThreatLevel finalLevel = level;
        actions.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (prefs().getBoolean(PREF_DATA_LOCKDOWN, true)) {
                        lockdownData(true, cause);
                    }
                    if (finalLevel == DefenderAgent.ThreatLevel.CRITICAL) {
                        if (prefs().getBoolean(PREF_RADIO_RESET, false)) {
                            radioResetPulse(cause);
                        } else {
                            log.info("Radio reset disabled in prefs; skipping.");
                        }
                    }
                } catch (Exception e) {
                    log.warn("Auto-protect action failed: {}", e.getMessage());
                }
            }
        });
    }

    /** Manually clear a data lockdown (UI button / threat cleared). */
    public void clearLockdown(final String reason) {
        // Called from the UI thread; shell-outs go to the background.
        // (Toaster hops to main itself, so the toast is safe from here.)
        actions.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    firewall.setLockdown(false, reason);
                } catch (Exception e) {
                    log.debug("clearLockdown firewall failed: {}", e.getMessage());
                }
                setMobileDataEnabled(true, reason);
                Helpers.msgShort(appContext, appContext.getString(R.string.defender_lockdown_cleared));
            }
        });
    }

    /** Manual "reset radio now" button (2.5 s pulse runs off the UI thread). */
    public void manualRadioReset() {
        actions.execute(new Runnable() {
            @Override
            public void run() {
                radioResetPulse(null);
            }
        });
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------

    private void lockdownData(boolean on, DefenderEvent cause) {
        String reason = cause == null ? "manual" : cause.getTitle();
        boolean rooted = false;
        try {
            rooted = firewall.isRooted();
        } catch (Exception ignored) {
        }
        try {
            firewall.setLockdown(on, reason);
        } catch (Exception e) {
            log.warn("Firewall lockdown failed: {}", e.getMessage());
        }
        // Also try to switch mobile data off at the OS level (root/system only).
        setMobileDataEnabled(!on, reason);
        if (!on) {
            Helpers.msgLong(appContext, appContext.getString(R.string.defender_lockdown_cleared));
        } else if (!rooted) {
            // No iptables without root: DENY rules + VPN sinkhole still apply,
            // but be honest that this is not a full data cut-off.
            Helpers.msgLong(appContext, appContext.getString(R.string.defender_lockdown_limited));
        } else {
            Helpers.msgLong(appContext, appContext.getString(R.string.defender_data_lockdown_on));
        }
        log.info("DATA LOCKDOWN {} (rooted={} {})", on ? "ON" : "OFF", rooted, reason);
    }

    /**
     * Best-effort mobile-data toggle. Uses {@code svc data} via su (works on
     * most rooted ROMs); falls back gracefully when unavailable.
     */
    private void setMobileDataEnabled(boolean enable, String reason) {
        try {
            CommandResult r = CMDProcessor.runSuCommand("svc data " + (enable ? "enable" : "disable"));
            if (r != null && r.success()) {
                log.info("svc data {} ok ({})", enable ? "enable" : "disable", reason);
                return;
            }
            log.debug("svc data toggle unavailable (exit={}); firewall lockdown still active.",
                    r == null ? -1 : r.getExitValue());
        } catch (Exception e) {
            log.debug("setMobileDataEnabled failed: {}", e.getMessage());
        }
    }

    /**
     * Airplane-mode pulse: forces a full radio re-attach so the phone drops a
     * suspicious cell and re-selects. Root path via settings+am broadcast;
     * non-root path notifies the user to toggle manually (toggle is a secure
     * setting since 4.2 and can't be flipped by a normal app).
     */
    private void radioResetPulse(DefenderEvent cause) {
        String reason = cause == null ? "manual" : cause.getTitle();
        boolean ok = false;
        try {
            CommandResult on = CMDProcessor.runSuCommand(
                    "settings put global airplane_mode_on 1 && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true");
            Thread.sleep(2500);
            CommandResult off = CMDProcessor.runSuCommand(
                    "settings put global airplane_mode_on 0 && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false");
            ok = on != null && on.success() && off != null && off.success();
        } catch (Exception e) {
            log.debug("radioResetPulse failed: {}", e.getMessage());
        }
        if (ok) {
            log.info("Radio reset pulse done ({})", reason);
            Helpers.msgLong(appContext, appContext.getString(R.string.defender_radio_reset_done));
        } else {
            log.info("Radio reset needs root; prompting user ({})", reason);
            Helpers.msgLong(appContext, appContext.getString(R.string.defender_radio_reset_manual));
            notifyManualRadioReset();
        }
    }

    // ------------------------------------------------------------------
    // Notifications
    // ------------------------------------------------------------------

    private void notifyUser(DefenderEvent event) {
        long now = System.currentTimeMillis();
        if (now - lastNotification < NOTIF_COOLDOWN_MS
                && event.getThreatLevel() != DefenderAgent.ThreatLevel.CRITICAL) {
            return;
        }
        lastNotification = now;

        try {
            Intent intent = new Intent(appContext, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pi = PendingIntent.getActivity(appContext, NOTIF_ID, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            int color;
            switch (event.getThreatLevel()) {
                case CRITICAL:
                    color = 0xFFB71C1C;
                    break;
                case HIGH:
                    color = 0xFFE65100;
                    break;
                default:
                    color = 0xFFF9A825;
                    break;
            }

            Notification n = new NotificationCompat.Builder(appContext)
                    .setSmallIcon(R.drawable.tower48)
                    .setColor(color)
                    .setContentTitle(appContext.getString(R.string.defender_notif_title,
                            event.getThreatLevel().name()))
                    .setContentText(event.getTitle())
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(event.getTitle() + "\n" + event.getDescription()))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build();
            NotificationManagerCompat.from(appContext).notify(NOTIF_ID, n);
        } catch (Exception e) {
            log.debug("defender notify failed: {}", e.getMessage());
        }

        // Vibrate per user prefs (reuse AIMSICD vibration settings when present).
        try {
            boolean vib = prefs().getBoolean(
                    appContext.getString(R.string.pref_notification_vibrate_enable), true);
            if (vib) {
                Vibrator v = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) {
                    long[] pattern = event.getThreatLevel() == DefenderAgent.ThreatLevel.CRITICAL
                            ? new long[]{0, 200, 150, 200, 150, 400}
                            : new long[]{0, 250};
                    v.vibrate(pattern, -1);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void notifyManualRadioReset() {
        try {
            Intent airplane = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
            airplane.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pi = PendingIntent.getActivity(appContext, NOTIF_ID + 1, airplane,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            Notification n = new NotificationCompat.Builder(appContext)
                    .setSmallIcon(R.drawable.tower48)
                    .setContentTitle(appContext.getString(R.string.defender_notif_radio_title))
                    .setContentText(appContext.getString(R.string.defender_radio_reset_manual))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();
            NotificationManagerCompat.from(appContext).notify(NOTIF_ID + 1, n);
        } catch (Exception e) {
            log.debug("radio reset notify failed: {}", e.getMessage());
        }
    }

    private SharedPreferences prefs() {
        return PreferenceManager.getDefaultSharedPreferences(appContext);
    }
}

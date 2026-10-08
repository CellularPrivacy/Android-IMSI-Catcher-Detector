/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.prometheus;

import android.content.Context;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import lombok.extern.slf4j.Slf4j;

/**
 * Neural error logger (client side of the self-learning loop).
 *
 * <p>Installs a {@link Thread.UncaughtExceptionHandler} that persists the crashing
 * stack trace to an app-private file, then chains to the previous handler so the
 * normal crash UX is preserved. On the next {@link #install} (i.e. next process
 * start) the pending report is uploaded through {@link PrometheusUplink} and the
 * file is deleted. At most one pending report is kept — a crash loop must not fill
 * storage.</p>
 */
@Slf4j
public final class NeuralErrorLogger {

    private static final String TAG = "NeuralErrorLogger";
    private static final String PENDING_FILE = "prometheus_crash_pending.json";

    private static volatile boolean sInstalled;

    private NeuralErrorLogger() {
    }

    /**
     * Installs the crash hook (idempotent per process) and flushes any report
     * pending from a previous run. Call once at service start.
     */
    public static synchronized void install(Context context, final PrometheusUplink uplink) {
        final Context appContext = context.getApplicationContext();
        flushPending(appContext, uplink);
        if (sInstalled) {
            return;
        }
        sInstalled = true;
        final Thread.UncaughtExceptionHandler previous =
                Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                try {
                    persist(appContext, thread, throwable);
                } catch (Exception e) {
                    log.warn("{} persist failed: {}", TAG, e.getMessage());
                }
                if (previous != null) {
                    previous.uncaughtException(thread, throwable);
                }
            }
        });
    }

    /* ------------------------------------------------------------------ */

    private static void persist(Context context, Thread thread, Throwable throwable) {
        try {
            JSONObject report = new JSONObject();
            report.put("exception_type", throwable.getClass().getName());
            String message = throwable.getMessage();
            report.put("message", message == null ? "" : message);
            report.put("stack_trace", stackTraceString(throwable));
            report.put("thread", thread == null ? "" : thread.getName());
            report.put("occurred_at", PrometheusUplink.utcNow());

            FileOutputStream out = context.openFileOutput(PENDING_FILE, Context.MODE_PRIVATE);
            try {
                out.write(report.toString().getBytes("UTF-8"));
            } finally {
                out.close();
            }
        } catch (Exception e) {
            log.warn("{} persist failed: {}", TAG, e.getMessage());
        }
    }

    private static void flushPending(Context context, PrometheusUplink uplink) {
        String raw = readPending(context);
        if (raw == null || raw.isEmpty()) {
            return;
        }
        try {
            JSONObject report = new JSONObject(raw);
            uplink.reportError(
                    report.optString("exception_type", "unknown"),
                    report.optString("message", ""),
                    report.optString("stack_trace", ""));
        } catch (Exception e) {
            log.warn("{} pending report unreadable, dropping: {}", TAG, e.getMessage());
        }
        context.deleteFile(PENDING_FILE);
    }

    private static String readPending(Context context) {
        FileInputStream in = null;
        try {
            in = context.openFileInput(PENDING_FILE);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int read;
            while ((read = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return buffer.toString("UTF-8");
        } catch (Exception e) {
            return null; // No pending report (normal case).
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /** Renders the full causal chain; JVM-testable (no Android calls). */
    static String stackTraceString(Throwable throwable) {
        StringWriter writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer);
        try {
            throwable.printStackTrace(printer);
        } finally {
            printer.close();
        }
        return writer.toString();
    }
}

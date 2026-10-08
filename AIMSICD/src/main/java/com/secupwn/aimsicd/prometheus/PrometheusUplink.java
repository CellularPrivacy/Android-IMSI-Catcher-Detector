/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.secupwn.aimsicd.prometheus;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;

import com.secupwn.aimsicd.utils.TinyDB;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;

/**
 * Streams detection telemetry and crash reports from this DEF field sensor to the
 * Prometheus Infinity backend over HTTPS. Payloads conform to
 * {@code contracts/telemetry-event.schema.json} and
 * {@code contracts/error-report.schema.json}.
 *
 * <p>Stdlib + {@code org.json} only (no new Gradle dependencies). All network I/O runs
 * on throwaway background threads; failures are logged and stashed into a single
 * pending slot that is flushed on the next report.</p>
 *
 * <p><b>Security:</b> plain HTTP is refused (except loopback/emulator hosts for
 * on-device development). The device JWT is minted server-side (see
 * {@code backend/README.md} → provisioning) and pasted into
 * <em>Settings → Prometheus Uplink</em>; without it the uplink stays silent.</p>
 */
@Slf4j
public class PrometheusUplink {

    private static final String TAG = "PrometheusUplink";

    private static final String PATH_TELEMETRY = "/v1/telemetry";
    private static final String PATH_ERRORS = "/v1/errors";

    private static final String PREF_PENDING_TELEMETRY = "prometheus_pending_telemetry";
    private static final String PREF_PENDING_ERROR = "prometheus_pending_error";

    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;
    private static final int READ_TIMEOUT_MILLIS = 10_000;

    private static final String FACTION = "DEF";
    private static final String APP_ID = "aimsicd-android";

    private final Context mContext;
    private final TinyDB mTinydb;

    private boolean mEnabled;
    private String mEndpoint = "";
    private String mDeviceToken = "";

    public PrometheusUplink(Context context) {
        mContext = context.getApplicationContext();
        mTinydb = TinyDB.getInstance();
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public void setEndpoint(String endpoint) {
        mEndpoint = endpoint == null ? "" : endpoint.trim();
        if (mEndpoint.endsWith("/")) {
            mEndpoint = mEndpoint.substring(0, mEndpoint.length() - 1);
        }
    }

    public void setDeviceToken(String token) {
        mDeviceToken = token == null ? "" : token.trim();
    }

    /**
     * Reports a protection event (DF_id + threat level + description).
     *
     * @param eventId     AIMSICD DF_id (1-7, 100-109).
     * @param threatLevel one of IDLE/OK/MEDIUM/HIGH/DANGER.
     * @param description human readable text (already localized or log text).
     * @param apn         active APN value, or null when not relevant.
     */
    public void reportTelemetry(final int eventId, final String threatLevel,
                                final String description, final String apn) {
        if (!ready()) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject payload = new JSONObject();
                    payload.put("event_id", eventId);
                    payload.put("device_id", getDeviceId());
                    payload.put("faction", FACTION);
                    payload.put("threat_level", threatLevel);
                    payload.put("description", description == null ? "" : description);
                    if (apn != null) {
                        payload.put("apn", apn);
                    }
                    payload.put("app_version", getAppVersion());
                    payload.put("occurred_at", utcNow());
                    flushPending(PATH_TELEMETRY, PREF_PENDING_TELEMETRY);
                    postOrStash(PATH_TELEMETRY, PREF_PENDING_TELEMETRY, payload);
                } catch (Exception e) {
                    log.warn("{} telemetry build failed: {}", TAG, e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Reports an uncaught exception captured by {@link NeuralErrorLogger}.
     */
    public void reportError(final String exceptionType, final String message,
                            final String stackTrace) {
        if (!ready()) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject payload = new JSONObject();
                    payload.put("app", APP_ID);
                    payload.put("app_version", getAppVersion());
                    payload.put("os", getOsString());
                    payload.put("device_id", getDeviceId());
                    payload.put("exception_type", exceptionType);
                    payload.put("message", message == null ? "" : message);
                    payload.put("stack_trace", stackTrace);
                    payload.put("occurred_at", utcNow());
                    flushPending(PATH_ERRORS, PREF_PENDING_ERROR);
                    postOrStash(PATH_ERRORS, PREF_PENDING_ERROR, payload);
                } catch (Exception e) {
                    log.warn("{} error-report build failed: {}", TAG, e.getMessage());
                }
            }
        }).start();
    }

    /* ------------------------------------------------------------------ */

    private boolean ready() {
        if (!mEnabled) {
            return false;
        }
        if (mEndpoint == null || mEndpoint.isEmpty()) {
            log.warn("{} enabled but no endpoint configured.", TAG);
            return false;
        }
        if (!isHttpsEndpoint(mEndpoint)) {
            log.warn("{} refusing non-HTTPS endpoint: {}", TAG, mEndpoint);
            return false;
        }
        if (mDeviceToken == null || mDeviceToken.isEmpty()) {
            log.warn("{} enabled but no device token configured.", TAG);
            return false;
        }
        return true;
    }

    /**
     * HTTPS-only guard. Plain HTTP is accepted solely for loopback/emulator hosts
     * so developers can point a test device at a local backend.
     */
    static boolean isHttpsEndpoint(String endpoint) {
        if (endpoint == null) {
            return false;
        }
        String lower = endpoint.trim().toLowerCase(Locale.US);
        if (lower.startsWith("https://")) {
            return true;
        }
        if (lower.startsWith("http://")) {
            String host = lower.substring("http://".length());
            int slash = host.indexOf('/');
            if (slash >= 0) {
                host = host.substring(0, slash);
            }
            int colon = host.indexOf(':');
            if (colon >= 0) {
                host = host.substring(0, colon);
            }
            return host.equals("localhost") || host.equals("127.0.0.1")
                    || host.equals("10.0.2.2");
        }
        return false;
    }

    private void postOrStash(String path, String pendingKey, JSONObject payload) {
        if (postJson(path, payload.toString())) {
            return;
        }
        mTinydb.putString(pendingKey, payload.toString());
    }

    private void flushPending(String path, String pendingKey) {
        String pending = mTinydb.getString(pendingKey);
        if (pending == null || pending.isEmpty()) {
            return;
        }
        if (postJson(path, pending)) {
            mTinydb.remove(pendingKey);
        }
    }

    private boolean postJson(String path, String body) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(mEndpoint + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
            connection.setReadTimeout(READ_TIMEOUT_MILLIS);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Authorization", "Bearer " + mDeviceToken);
            byte[] bytes = body.getBytes("UTF-8");
            connection.setFixedLengthStreamingMode(bytes.length);
            OutputStream out = connection.getOutputStream();
            try {
                out.write(bytes);
            } finally {
                out.close();
            }
            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_CREATED) {
                return true;
            }
            log.warn("{} POST {} -> HTTP {}", TAG, path, status);
            return false;
        } catch (Exception e) {
            log.warn("{} POST {} failed: {}", TAG, path, e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String getDeviceId() {
        try {
            String id = Settings.Secure.getString(
                    mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
            return id == null ? "unknown" : id;
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getAppVersion() {
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), 0);
            return info.versionName == null ? "unknown" : info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "unknown";
        }
    }

    private String getOsString() {
        try {
            return "Android " + android.os.Build.VERSION.RELEASE
                    + " / SDK " + android.os.Build.VERSION.SDK_INT;
        } catch (Exception e) {
            return "Android";
        }
    }

    /** Current UTC time in ISO-8601, e.g. {@code 2026-09-21T12:00:00.000Z}. */
    static String utcNow() {
        SimpleDateFormat format = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }
}

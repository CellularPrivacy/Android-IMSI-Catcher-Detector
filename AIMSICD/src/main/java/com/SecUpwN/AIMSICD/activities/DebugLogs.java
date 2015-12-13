/* Android IMSI-Catcher Detector | (c) AIMSICD Privacy Project
 * -----------------------------------------------------------
 * LICENSE:  http://git.io/vki47 | TERMS:  http://git.io/vki4o
 * -----------------------------------------------------------
 */
package com.SecUpwN.AIMSICD.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;
import com.SecUpwN.AIMSICD.utils.Helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import io.freefair.android.util.logging.AndroidLogger;
import io.freefair.android.util.logging.Logger;

/**
 *  Description:    This class is providing for the Debug log feature in the swipe menu.
 *                  It reads the last 500 lines from the Logcat ring buffers: main and radio.
 *
 *  Dependencies:
 *                  menu/activity_debug_logs.xml **
 *                  layout/activity_debug_logs.xml
 *                  values/strings.xml
 *  Issues:
 *
 *          [ ]     Are we clearing logcat when starting it? If we are, we miss all previous errors
 *                  if any has occurred. We need to clear the logcat when app starts. Also there
 *                  is no reason to clear it if we only catch the last 500 lines anyway.
 *
 *          [ ]     Add the output of "getprop |sort". But Java CLI processes doesn't handle pipes.
 *                  Try with:  Collections.sort(list, String.CASE_INSENSITIVE_ORDER)
 *
 *          [ ]     Apparently the button for radio log has been added to the strings.xml,
 *                  but never implemented here. We need to add the buffer selector button
 *                  to the top bar, next to email icon button. **
 *
 *  TODO:   [ ]     We should add an XPrivacy button (or automatic) to add XPrivacy filters when used.
 *
 *  ChangeLog:
 *
 *          2015-01-27  E:V:A   Added "getprop|sort" info to log.
 *          2015-01-28  Toby    Fixed "getprop" info to log (but not sorted)
 *          2015-02-11  E:V:A   Increased to 500 lines and removed "-d" and
 *                              incl. radio log, but not working. Permission problem?
 *          2015-02-24  E:V:A   Silent some spam logs on HTC devices.
 *          2015-07-03  E:V:A   Silent some spam logs from the XPosed framework
 *
 */

public class DebugLogs extends BaseActivity {

    //TODO: @Inject
    private final Logger log = AndroidLogger.forClass(DebugLogs.class);

    private LogUpdaterThread logUpdater = null;
    private boolean updateLogs = true;
    private boolean isRadioLogs = true; // Including this, should be a toggle.

    private TextView logView = null;
    private Button btnClear = null;
    private Button btnCopy = null;
    private Button btnStop = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_logs);
        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        logView = (TextView) findViewById(R.id.debug_log_view);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnStop = (Button) findViewById(R.id.btnStopLogs);
        btnCopy = (Button) findViewById(R.id.btnCopy);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logView.setFocusable(false);
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    clearLogs();
                } catch (IOException e) {
                    log.error("Error clearing logs", e);
                }
            }
        });

        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData cd = ClipData.newPlainText("log", logView.getText());
                clipboard.setPrimaryClip(cd);
                Helpers.msgShort(DebugLogs.this, getString(R.string.msg_copied_to_clipboard));
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (updateLogs) {
                    updateLogs = false;
                    btnStop.setText(getString(R.string.btn_start_logs));
                } else {
                    startLogging();
                }
            }
        });

        /*
        // logcat radio buffer toggle on/off
        btnRadio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRadioLogs) {
                    isRadioLogs = false;
                    btnRadio.setText(getString(R.string.btn_radio_logs));
                } else {
                    isRadioLogs = true;
                    btnRadio.setText(getString(R.string.btn_main_logs));
                }
            }
        });
        */

    }

    @Override
    protected void onPause() {
        updateLogs = false; // exit the log updater
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLogging();
    }

    private void startLogging() {
        updateLogs = true;

        logUpdater = new LogUpdaterThread();
        logUpdater.start();

        btnStop.setText(getString(R.string.btn_stop_logs));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_debug_logs, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send_logs:
                sendEmail();
                return true;

            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendEmail() {
        new Thread() {
            @Override
            public void run() {
                // Send Error Log
                try {
                    String helpUs = getString(R.string.describe_the_problem_you_had);
                    String log = helpUs + "\n\n" + "GETPROP:" + "\n\n" + getProp() +
                                          "\n\n" + "LOGCAT:" + "\n\n" + getLogs() + "\n\n" + helpUs;

                    // show a share intent
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/html");
                    // E-Mail address will ONLY be handed out when a DEVELOPER asked for the logs!
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"See GitHub Issues first!"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "AIMSICD Error Log");
                    intent.putExtra(Intent.EXTRA_TEXT, log);
                    startActivity(Intent.createChooser(intent, "Send Error Log"));
                } catch (IOException e) {
                    log.warn("Error reading logs", e);
                }
            }
        }.start();
    }

    /**
     * Read getprop and return the sorted result as a string
     *
     * TODO: Need a way to sort properties for easy reading
     *
     * @return
     * @throws IOException
     */
    public String getProp() throws IOException {
        return runProcess("/system/bin/getprop");
    }

    /**
     *  Description:    Read logcat and return as a string
     *
     *  Notes:
     *
     *  1) " *:V" makes log very spammy due to verbose OemRilRequestRaw debug output (AIMSICD_Helpers).
     *          ==> Now disabled!
     *  2) Need to silent some spammy Samsung Galaxy's:     " AbsListView:S PackageInfo:S"
     *  3) Need to silent some Qualcomm QMI:                " RILQ:S"
     *  4) Need to silent some Qualcomm GPS:                " LocSvc_eng:S LocSvc_adapter:S LocSvc_afw:S"
     *  5) "-d" is not necessary when using "-t".
     *  6) Need to silent some spammy HTC's:                "QC-QMI:S AudioPolicyManager:S"
     *  7) Need to silent some spammy XPrivacy items:       "XPrivacy/XRuntime:S Xposed:S"
     *  8) Need to silent even more XPrivacy items:         "XPrivacy/XTelephonyManager:S XPrivacy/XLocationManager:S XPrivacy/XPackageManager:S"
     *
     */
    private String getLogs() throws IOException {
        return runProcess(
            "logcat -t 500 -v brief -b main" +
                    (isRadioLogs ? " -b radio RILQ:S" : "") +
                    " AbsListView:S PackageInfo:S" +
                    " LocSvc_eng:S LocSvc_adapter:S LocSvc_afw:S" +
                    " QC-QMI:S AudioPolicyManager:S" +
                    " XPrivacy/XRuntime:S Xposed:S" +
                    " XPrivacy/XTelephonyManager:S XPrivacy/XLocationManager:S XPrivacy/XPackageManager:S" + " *:D"
        );
    }

    /**
     * Run a shell command and return the results
     */
    private String runProcess(String command) throws IOException {
        return runProcess(new String[]{ command });
    }

    /**
     * Run a shell command and return the results
     *
     * @param command
     * @return
     * @throws IOException
     */
    private String runProcess(String[] command) throws IOException {
        Process process = null;
        if (command.length == 1)
            process = Runtime.getRuntime().exec(command[0]);
        else
            Runtime.getRuntime().exec(command);

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        StringBuilder log = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            log.append(line);
            log.append("\n");
        }
        bufferedReader.close();
        return log.toString();
    }

    /**
     * Clear logcat
     * @return
     * @throws IOException
     */
    private void clearLogs() throws IOException {
        new Thread() {
            @Override
            public void run() {
                try {
                    Runtime.getRuntime().exec("logcat -c -b main -b system -b radio -b events");
                } catch (IOException e) {
                    log.error("Error clearing logs", e);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logView.setText("");
                    }
                });
            }
        }.start();
    }

    class LogUpdaterThread extends Thread {
        @Override
        public void run() {
            while (updateLogs) {
                try {
                    final String logs = getLogs();
                    if (!logs.equals(logView.getText().toString())) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // update log display
                                logView.setText(logs);

                                // scroll to the bottom of the log display
                                final ScrollView scroll = ((ScrollView)logView.getParent());
                                scroll.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        scroll.fullScroll(View.FOCUS_DOWN);
                                    }
                                });
                            }
                        });
                    }
                } catch (IOException e) {
                    log.warn("Error updating logs", e);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.warn("Thread was interrupted", e);
                }
            }
        }
    }
}

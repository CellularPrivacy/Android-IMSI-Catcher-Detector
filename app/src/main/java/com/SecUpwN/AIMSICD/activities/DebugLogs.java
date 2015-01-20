package com.SecUpwN.AIMSICD.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
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

public class DebugLogs extends BaseActivity {
    private LogUpdaterThread logUpdater = null;
    private boolean updateLogs = true;
    private boolean isRadioLogs = false;

    private TextView logView = null;
    private Button btnClear = null;
    private Button btnCopy = null;
    private Button btnStop = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_logs);
        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

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
                try  {
                    clearLogs();
                } catch (Exception e) {
                    Log.e("DebugLogs", "Error clearing logs", e);
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
        try {
            logUpdater = new LogUpdaterThread();
            logUpdater.start();
        } catch (Exception e) {
            Log.e("DebugLogs", "Error starting log updater thread", e);
        }
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
                    String log = getLogs();

                    // show a share intent
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/html");
                    // This is a masked email to one of our developers. In case of spam re-mask.
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "a3841c3c@opayq.com" });
                    intent.putExtra(Intent.EXTRA_SUBJECT, "AIMSICD Error Log");
                    intent.putExtra(Intent.EXTRA_TEXT, log);
                    startActivity(Intent.createChooser(intent, "Send Error Log"));
                } catch (IOException e) {
                    Log.e("DebugLogs", "Error reading logs", e);
                }
            }
        }.start();
    }

    /**
     * Read logcat and return as a string
     * @return
     * @throws IOException
     */
    private String getLogs() throws IOException {
        // + " *:v" makes log very spammy due to verbose OemRilRequestRaw debug output (AIMSICD_Helpers).
        // Silent Samsung Galaxy devices spam debug: " AbsListView:S PackageInfo:S"
        Process process = Runtime.getRuntime().exec("logcat -t 1000 -d -v time" + (isRadioLogs ? " -b radio" : "") + " AbsListView:S PackageInfo:S *:D" );
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        StringBuilder log = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            log.append(line);
            log.append("\n");
        }

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
                } catch (Exception e) {
                    Log.e("DebugLogs", "Error clearing logs", e);
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
                    //Log.d("log_thread", "running");
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
                } catch (Exception e) {
                    Log.e("DebugLogs", "Error updating logs", e);
                }
                try { Thread.sleep(1000); } catch (Exception e) {}
            }
        }
    }
}

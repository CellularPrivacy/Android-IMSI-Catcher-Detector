package com.SecUpwN.AIMSICD.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.SecUpwN.AIMSICD.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DebugLogs extends Activity {
    private boolean updateLogs = true;
    private boolean isRadioLogs = false;

    private TextView logView = null;
    private Button btnSendEmail = null;
    private Button btnClear = null;
    private Button btnRadioLogs = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_logs);
        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        logView = (TextView) findViewById(R.id.debug_log_view);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnRadioLogs = (Button) findViewById(R.id.btnRadioLogs);
        btnSendEmail = (Button) findViewById(R.id.btnSendEmail);

        logUpdater.start();

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

        btnRadioLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRadioLogs) {
                    isRadioLogs = false;
                    btnRadioLogs.setText(getString(R.string.btn_radio_logs));
                } else {
                    isRadioLogs = true;
                    btnRadioLogs.setText(getString(R.string.btn_app_logs));
                }
            }
        });

        btnSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();
            }
        });
    }

    @Override
    protected void onDestroy() {
        updateLogs = false; // exit the log updater
        super.onDestroy();
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
                // TODO - clear log using 'logcat -c' on app startup
                try {
                    String log = getLogs();

                    // show a share intent
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/html");
                    intent.putExtra(Intent.EXTRA_EMAIL, "a3841c3c@opayq.com");
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
        Process process = Runtime.getRuntime().exec("logcat -d -v brief" + (isRadioLogs ? " -b radio" : ""));
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
                    Runtime.getRuntime().exec("logcat -c");
                } catch (Exception e) {
                    Log.e("DebugLogs", "Error clearing logs", e);
                }
            }
        }.start();
    }

    private Thread logUpdater = new Thread() {
        @Override
        public void run() {
            while (updateLogs) {
                try {
                    final String logs = getLogs();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ScrollView container = ((ScrollView)logView.getParent());
                            int scrollY = container.getScrollY();
                            logView.setText(logs);
                            container.setScrollY(scrollY);
                        }
                    });
                } catch (Exception e) {
                    Log.e("DebugLogs", "Error updating logs", e);
                }
                try { Thread.sleep(1000); } catch (Exception e) {}
            }
        }
    };
}

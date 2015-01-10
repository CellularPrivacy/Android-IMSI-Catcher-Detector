/* 
 * This file is part of the RootShell Project: http://code.google.com/p/RootShell/
 *  
 * Copyright (c) 2012 Stephen Erickson, Chris Ravenscroft, Dominik Schuermann, Adam Shanks
 *  
 * This code is dual-licensed under the terms of the Apache License Version 2.0 and
 * the terms of the General Public License (GPL) Version 2.
 * You may use this code according to either of these licenses as is most appropriate
 * for your project on a case-by-case basis.
 * 
 * The terms of each license can be found in the root directory of this project's repository as well as at:
 * 
 * * http://www.apache.org/licenses/LICENSE-2.0
 * * http://www.gnu.org/licenses/gpl-2.0.txt
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under these Licenses is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See each License for the specific language governing permissions and
 * limitations under that License.
 */

package com.stericson.RootShellTests;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.widget.ScrollView;
import android.widget.TextView;

import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;

public class SanityCheckRootShell extends Activity
{
    private ScrollView mScrollView;
    private TextView mTextView;
    private ProgressDialog mPDialog;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

        RootShell.debugMode = true;

        mTextView = new TextView(this);
        mTextView.setText("");
        mScrollView = new ScrollView(this);
        mScrollView.addView(mTextView);
        setContentView(mScrollView);

        print("SanityCheckRootShell \n\n");

        if (RootShell.isRootAvailable())
        {
            print("Root found.\n");
        }
        else
        {
            print("Root not found");
        }

        try
        {
            RootShell.getShell(true);
        }
        catch (IOException e2)
        {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        catch (TimeoutException e)
        {
            print("[ TIMEOUT EXCEPTION! ]\n");
            e.printStackTrace();
        }
        catch (RootDeniedException e)
        {
            print("[ ROOT DENIED EXCEPTION! ]\n");
            e.printStackTrace();
        }

        try
        {
            if (!RootShell.isAccessGiven())
            {
                print("ERROR: No root access to this device.\n");
                return;
            }
        }
        catch (Exception e)
        {
            print("ERROR: could not determine root access to this device.\n");
            return;
        }

        // Display infinite progress bar
        mPDialog = new ProgressDialog(this);
        mPDialog.setCancelable(false);
        mPDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        new SanityCheckThread(this, new TestHandler()).start();
    }

    protected void print(CharSequence text)
    {
        mTextView.append(text);
        mScrollView.post(new Runnable()
        {
            public void run()
            {
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    // Run our long-running tests in their separate thread so as to
    // not interfere with proper rendering.
    private class SanityCheckThread extends Thread
    {
        private Handler mHandler;

        public SanityCheckThread(Context context, Handler handler)
        {
            mHandler = handler;
        }

        public void run()
        {
            visualUpdate(TestHandler.ACTION_SHOW, null);

            // First test: Install a binary file for future use
            // if it wasn't already installed.
            /*
            visualUpdate(TestHandler.ACTION_PDISPLAY, "Installing binary if needed");
            if(false == RootShell.installBinary(mContext, R.raw.nes, "nes_binary")) {
                visualUpdate(TestHandler.ACTION_HIDE, "ERROR: Failed to install binary. Please see log file.");
                return;
            }
            */

            boolean result;

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing getPath");
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ getPath ]\n");

            try
            {
                List<String> paths = RootShell.getPath();

                for (String path : paths)
                {
                    visualUpdate(TestHandler.ACTION_DISPLAY, path + " k\n\n");
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing A ton of commands");
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ Ton of Commands ]\n");

            for (int i = 0; i < 100; i++)
            {
                RootShell.exists("/system/xbin/busybox");
            }

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing Find Binary");
            result = RootShell.isRootAvailable();
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ Checking Root ]\n");
            visualUpdate(TestHandler.ACTION_DISPLAY, result + " k\n\n");

            result = RootShell.isBusyboxAvailable();
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ Checking Busybox ]\n");
            visualUpdate(TestHandler.ACTION_DISPLAY, result + " k\n\n");

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing file exists");
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ Checking Exists() ]\n");
            visualUpdate(TestHandler.ACTION_DISPLAY, RootShell.exists("/system/sbin/[") + " k\n\n");

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing Is Access Given");
            result = RootShell.isAccessGiven();
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ Checking for Access to Root ]\n");
            visualUpdate(TestHandler.ACTION_DISPLAY, result + " k\n\n");


            Shell shell;

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing output capture");
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ busybox ash --help ]\n");

            try
            {
                shell = RootShell.getShell(true);
                Command cmd = new Command(
                        0,
                        "busybox ash --help")
                {

                    @Override
                    public void commandOutput(int id, String line)
                    {
                        visualUpdate(TestHandler.ACTION_DISPLAY, line + "\n");
                        //super.commandOutput(id, line);
                    }
                };
                shell.add(cmd);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Switching RootContext - SYSTEM_APP");
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ Switching Root Context - SYSTEM_APP ]\n");

            try
            {
                shell = RootShell.getShell(true, Shell.ShellContext.SYSTEM_APP);
                Command cmd = new Command(
                        0,
                        "id")
                {

                    @Override
                    public void commandOutput(int id, String line)
                    {
                        visualUpdate(TestHandler.ACTION_DISPLAY, line + "\n");
                        super.commandOutput(id, line);
                    }
                };
                shell.add(cmd);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Switching RootContext - UNTRUSTED");
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ Switching Root Context - UNTRUSTED ]\n");

            try
            {
                shell = RootShell.getShell(true, Shell.ShellContext.UNTRUSTED_APP);
                Command cmd = new Command(
                        0,
                        "id")
                {

                    @Override
                    public void commandOutput(int id, String line)
                    {
                        visualUpdate(TestHandler.ACTION_DISPLAY, line + "\n");
                        super.commandOutput(id, line);
                    }
                };
                shell.add(cmd);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            try
            {
                shell = RootShell.getShell(true);

                Command cmd = new Command(42, false, "echo done")
                {

                    boolean _catch = false;

                    @Override
                    public void commandOutput(int id, String line)
                    {
                        if (_catch)
                        {
                            RootShell.log("CAUGHT!!!");
                        }

                        super.commandOutput(id, line);

                    }

                    @Override
                    public void commandTerminated(int id, String reason)
                    {
                        synchronized (com.stericson.RootShellTests.SanityCheckRootShell.this)
                        {

                            _catch = true;
                            visualUpdate(TestHandler.ACTION_PDISPLAY, "All tests complete.");
                            visualUpdate(TestHandler.ACTION_HIDE, null);

                            try
                            {
                                RootShell.closeAllShells();
                            }
                            catch (IOException e)
                            {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void commandCompleted(int id, int exitCode)
                    {
                        synchronized (com.stericson.RootShellTests.SanityCheckRootShell.this)
                        {
                            _catch = true;

                            visualUpdate(TestHandler.ACTION_PDISPLAY, "All tests complete.");
                            visualUpdate(TestHandler.ACTION_HIDE, null);

                            try
                            {
                                RootShell.closeAllShells();
                            }
                            catch (IOException e)
                            {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }
                    }
                };

                shell.add(cmd);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }

        private void visualUpdate(int action, String text)
        {
            Message msg = mHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putInt(TestHandler.ACTION, action);
            bundle.putString(TestHandler.TEXT, text);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }

    private class TestHandler extends Handler
    {
        static final public String ACTION = "action";
        static final public int ACTION_SHOW = 0x01;
        static final public int ACTION_HIDE = 0x02;
        static final public int ACTION_DISPLAY = 0x03;
        static final public int ACTION_PDISPLAY = 0x04;
        static final public String TEXT = "text";

        public void handleMessage(Message msg)
        {
            int action = msg.getData().getInt(ACTION);
            String text = msg.getData().getString(TEXT);

            switch (action)
            {
                case ACTION_SHOW:
                    mPDialog.show();
                    mPDialog.setMessage("Running Root Library Tests...");
                    break;
                case ACTION_HIDE:
                    if (null != text)
                    { print(text); }
                    mPDialog.hide();
                    break;
                case ACTION_DISPLAY:
                    print(text);
                    break;
                case ACTION_PDISPLAY:
                    mPDialog.setMessage(text);
                    break;
            }
        }
    }
}

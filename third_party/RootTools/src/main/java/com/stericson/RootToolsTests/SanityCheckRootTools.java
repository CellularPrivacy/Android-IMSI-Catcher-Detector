/* 
 * This file is part of the RootTools Project: http://code.google.com/p/roottools/
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

package com.stericson.RootToolsTests;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.widget.ScrollView;
import android.widget.TextView;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.containers.Permissions;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.JavaCommandCapture;
import com.stericson.RootTools.execution.Shell;

public class SanityCheckRootTools extends Activity {
    private ScrollView mScrollView;
    private TextView mTextView;
    private ProgressDialog mPDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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

        RootTools.debugMode = true;

        mTextView = new TextView(this);
        mTextView.setText("");
        mScrollView = new ScrollView(this);
        mScrollView.addView(mTextView);
        setContentView(mScrollView);

        // Great the user with our version number
        String version = "?";
        try {
            PackageInfo packageInfo =
                    this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }

        print("SanityCheckRootTools v " + version + "\n\n");

        if(RootTools.isRootAvailable()) {
            print("Root found.\n");
        }
        else
        {
            print("Root not found");
        }

        try {
            Shell.startRootShell();
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        } catch (TimeoutException e) {
            print("[ TIMEOUT EXCEPTION! ]\n");
            e.printStackTrace();
        } catch (RootDeniedException e) {
            print("[ ROOT DENIED EXCEPTION! ]\n");
            e.printStackTrace();
        }

        try {
            if (!RootTools.isAccessGiven()) {
                print("ERROR: No root access to this device.\n");
                return;
            }
        } catch (Exception e) {
            print("ERROR: could not determine root access to this device.\n");
            return;
        }

        // Display infinite progress bar
        mPDialog = new ProgressDialog(this);
        mPDialog.setCancelable(false);
        mPDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        new SanityCheckThread(this, new TestHandler()).start();
    }

    protected void print(CharSequence text) {
        mTextView.append(text);
        mScrollView.post(new Runnable() {
            public void run() {
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    // Run our long-running tests in their separate thread so as to
    // not interfere with proper rendering.
    private class SanityCheckThread extends Thread {
        private Handler mHandler;

        public SanityCheckThread(Context context, Handler handler) {
            mHandler = handler;
        }

        public void run() {
            visualUpdate(TestHandler.ACTION_SHOW, null);

            // First test: Install a binary file for future use
            // if it wasn't already installed.
            /*
            visualUpdate(TestHandler.ACTION_PDISPLAY, "Installing binary if needed");
            if(false == RootTools.installBinary(mContext, R.raw.nes, "nes_binary")) {
                visualUpdate(TestHandler.ACTION_HIDE, "ERROR: Failed to install binary. Please see log file.");
                return;
            }
            */

            boolean result;

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing getPath");
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ getPath ]\n");

            try {
                List<String> paths = RootTools.getPath();

                for(String path : paths)
                {
                    visualUpdate(TestHandler.ACTION_DISPLAY,  path + " k\n\n");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing A ton of commands");
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ Ton of Commands ]\n");

            for (int i = 0; i < 100; i++) {
                RootTools.exists("/system/xbin/busybox");
            }

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing Find Binary");
            result = RootTools.isRootAvailable();
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ Checking Root ]\n");
            visualUpdate(TestHandler.ACTION_DISPLAY, result + " k\n\n");

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing file exists");
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ Checking Exists() ]\n");
            visualUpdate(TestHandler.ACTION_DISPLAY, RootTools.exists("/system/sbin/[") + " k\n\n");

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing Is Access Given");
            result = RootTools.isAccessGiven();
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ Checking for Access to Root ]\n");
            visualUpdate(TestHandler.ACTION_DISPLAY, result + " k\n\n");

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing Remount");
            result = RootTools.remount("/system", "rw");
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ Remounting System as RW ]\n");
            visualUpdate(TestHandler.ACTION_DISPLAY, result + " k\n\n");

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing CheckUtil");
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ Checking busybox is setup ]\n");
            visualUpdate(TestHandler.ACTION_DISPLAY, RootTools.checkUtil("busybox") + " k\n\n");

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing getBusyBoxVersion");
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ Checking busybox version ]\n");
            visualUpdate(TestHandler.ACTION_DISPLAY, RootTools.getBusyBoxVersion("/system/bin/") + " k\n\n");

            try {
                visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing fixUtils");
                visualUpdate(TestHandler.ACTION_DISPLAY, "[ Checking Utils ]\n");
                visualUpdate(TestHandler.ACTION_DISPLAY, RootTools.fixUtils(new String[]{"ls", "rm", "ln", "dd", "chmod", "mount"}) + " k\n\n");
            } catch (Exception e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }

            try {
                visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing getSymlink");
                visualUpdate(TestHandler.ACTION_DISPLAY, "[ Checking [[ for symlink ]\n");
                visualUpdate(TestHandler.ACTION_DISPLAY, RootTools.getSymlink("/system/bin/[[") + " k\n\n");
            } catch (Exception e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing getInode");
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ Checking Inodes ]\n");
            visualUpdate(TestHandler.ACTION_DISPLAY, RootTools.getInode("/system/bin/busybox") + " k\n\n");

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing GetBusyBoxapplets");
            try {

                visualUpdate(TestHandler.ACTION_DISPLAY, "[ Getting all available Busybox applets ]\n");
                for (String applet : RootTools.getBusyBoxApplets("/data/data/stericson.busybox.donate/files/bb")) {
                    visualUpdate(TestHandler.ACTION_DISPLAY, applet + " k\n\n");
                }

            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing getFilePermissionsSymlinks");
            Permissions permissions = RootTools.getFilePermissionsSymlinks("/system/bin/busybox");
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ Checking busybox permissions and symlink ]\n");

            if (permissions != null) {
                visualUpdate(TestHandler.ACTION_DISPLAY, "Symlink: " + permissions.getSymlink() + " k\n\n");
                visualUpdate(TestHandler.ACTION_DISPLAY, "Group Permissions: " + permissions.getGroupPermissions() + " k\n\n");
                visualUpdate(TestHandler.ACTION_DISPLAY, "Owner Permissions: " + permissions.getOtherPermissions() + " k\n\n");
                visualUpdate(TestHandler.ACTION_DISPLAY, "Permissions: " + permissions.getPermissions() + " k\n\n");
                visualUpdate(TestHandler.ACTION_DISPLAY, "Type: " + permissions.getType() + " k\n\n");
                visualUpdate(TestHandler.ACTION_DISPLAY, "User Permissions: " + permissions.getUserPermissions() + " k\n\n");
            } else {
                visualUpdate(TestHandler.ACTION_DISPLAY, "Permissions == null k\n\n");
            }

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Switching RootContext");
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ Switching Root Context and running code... ]\n");

            Shell shell;
            try {
                shell = RootTools.getShell(true, Shell.ShellContext.SYSTEM_APP);
                CommandCapture cmd = new CommandCapture(
                        0,
                        "id") {

                    @Override
                    public void commandOutput(int id, String line) {
                        super.commandOutput(id, line);
                        visualUpdate(TestHandler.ACTION_DISPLAY, line + "\n");
                    }
                };
                shell.add(cmd);

            } catch (Exception e) {
                // Oops. Say, did you run RootClass and move the resulting anbuild.dex " file to res/raw?
                // If you don't you will not be able to check root mode Java.
                e.printStackTrace();
            }

            visualUpdate(TestHandler.ACTION_PDISPLAY, "Testing df");
            long spaceValue = RootTools.getSpace("/data");
            visualUpdate(TestHandler.ACTION_DISPLAY, "[ Checking /data partition size]\n");
            visualUpdate(TestHandler.ACTION_DISPLAY, spaceValue + "k\n\n");

            try {
                shell = RootTools.getShell(true);

                CommandCapture cmd = new CommandCapture(42, false, "find /") {

                    boolean _catch = false;

                    @Override
                    public void commandOutput(int id, String line) {
                        super.commandOutput(id, line);

                        if (_catch) {
                            RootTools.log("CAUGHT!!!");
                        }
                    }

                    @Override
                    public void commandTerminated(int id, String reason) {
                        synchronized (SanityCheckRootTools.this) {

                            _catch = true;
                            visualUpdate(TestHandler.ACTION_PDISPLAY, "All tests complete.");
                            visualUpdate(TestHandler.ACTION_HIDE, null);

                            try {
                                RootTools.closeAllShells();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void commandCompleted(int id, int exitCode) {
                        synchronized (SanityCheckRootTools.this) {
                            _catch = true;

                            visualUpdate(TestHandler.ACTION_PDISPLAY, "All tests complete.");
                            visualUpdate(TestHandler.ACTION_HIDE, null);

                            try {
                                RootTools.closeAllShells();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }
                    }
                };

                shell.add(cmd);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        private void visualUpdate(int action, String text) {
            Message msg = mHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putInt(TestHandler.ACTION, action);
            bundle.putString(TestHandler.TEXT, text);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }

    private class TestHandler extends Handler {
        static final public String ACTION = "action";
        static final public int ACTION_SHOW = 0x01;
        static final public int ACTION_HIDE = 0x02;
        static final public int ACTION_DISPLAY = 0x03;
        static final public int ACTION_PDISPLAY = 0x04;
        static final public String TEXT = "text";

        public void handleMessage(Message msg) {
            int action = msg.getData().getInt(ACTION);
            String text = msg.getData().getString(TEXT);

            switch (action) {
                case ACTION_SHOW:
                    mPDialog.show();
                    mPDialog.setMessage("Running Root Library Tests...");
                    break;
                case ACTION_HIDE:
                    if (null != text)
                        print(text);
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

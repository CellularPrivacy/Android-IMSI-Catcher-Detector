/* 
 * This file is part of the RootShell Project: http://code.google.com/p/RootShell/
 *  
 * Copyright (c) 2014 Stephen Erickson, Chris Ravenscroft
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
package com.stericson.RootShell;


import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public class RootShell {

    // --------------------
    // # Public Variables #
    // --------------------

    public static boolean debugMode = false;

    public static final String version = "RootShell v1.3";

    /**
     * Setting this to false will disable the handler that is used
     * by default for the 3 callback methods for Command.
     * <p/>
     * By disabling this all callbacks will be called from a thread other than
     * the main UI thread.
     */
    public static boolean handlerEnabled = true;


    /**
     * Setting this will change the default command timeout.
     * <p/>
     * The default is 20000ms
     */
    public static int defaultCommandTimeout = 20000;

    public static enum LogLevel {
        VERBOSE,
        ERROR,
        DEBUG,
        WARN
    }
    // --------------------
    // # Public Methods #
    // --------------------

    /**
     * This will close all open shells.
     */
    public static void closeAllShells() throws IOException {
        Shell.closeAll();
    }

    /**
     * This will close the custom shell that you opened.
     */
    public static void closeCustomShell() throws IOException {
        Shell.closeCustomShell();
    }

    /**
     * This will close either the root shell or the standard shell depending on what you specify.
     *
     * @param root a <code>boolean</code> to specify whether to close the root shell or the standard shell.
     */
    public static void closeShell(boolean root) throws IOException {
        if (root) {
            Shell.closeRootShell();
        } else {
            Shell.closeShell();
        }
    }

    /**
     * Use this to check whether or not a file exists on the filesystem.
     *
     * @param file String that represent the file, including the full path to the
     *             file and its name.
     * @return a boolean that will indicate whether or not the file exists.
     */
    public static boolean exists(final String file) {
        return exists(file, false);
    }

    /**
     * Use this to check whether or not a file OR directory exists on the filesystem.
     *
     * @param file  String that represent the file OR the directory, including the full path to the
     *              file and its name.
     * @param isDir boolean that represent whether or not we are looking for a directory
     * @return a boolean that will indicate whether or not the file exists.
     */
    public static boolean exists(final String file, boolean isDir) {
        final List<String> result = new ArrayList<String>();

        String cmdToExecute = "ls " + (isDir ? "-d " : " ");

        Command command = new Command(0, false, cmdToExecute + file) {
            @Override
            public void commandOutput(int id, String line) {
                RootShell.log(line);
                result.add(line);

                super.commandOutput(id, line);
            }
        };

        try {
            //Try without root...
            RootShell.getShell(false).add(command);
            commandWait(RootShell.getShell(false), command);

        } catch (Exception e) {
            return false;
        }

        for (String line : result) {
            if (line.trim().equals(file)) {
                return true;
            }
        }

        result.clear();

        try {
            RootShell.getShell(true).add(command);
            commandWait(RootShell.getShell(true), command);

        } catch (Exception e) {
            return false;
        }

        //Avoid concurrent modification...
        List<String> final_result = new ArrayList<String>();
        final_result.addAll(result);

        for (String line : final_result) {
            if (line.trim().equals(file)) {
                return true;
            }
        }

        return false;

    }

    /**
     * @param binaryName String that represent the binary to find.
     *
     * @return <code>List<String></code> containing the locations the binary was found at.
     */
    public static List<String> findBinary(final String binaryName) {
        return findBinary(binaryName, null);
    }

    /**
     * @param binaryName <code>String</code> that represent the binary to find.
     * @param searchPaths <code>List<String></code> which contains the paths to search for this binary in.
     *
     * @return <code>List<String></code> containing the locations the binary was found at.
     */
    public static List<String> findBinary(final String binaryName, List<String> searchPaths) {

        final List<String> foundPaths = new ArrayList<String>();

        boolean found = false;

        if(searchPaths == null)
        {
            searchPaths = RootShell.getPath();
        }

        RootShell.log("Checking for " + binaryName);

        //Try to use stat first
        try {
            for (String path : searchPaths) {

                if(!path.endsWith("/"))
                {
                    path += "/";
                }

                final String currentPath = path;

                Command cc = new Command(0, false, "stat " + path + binaryName) {
                    @Override
                    public void commandOutput(int id, String line) {
                        if (line.contains("File: ") && line.contains(binaryName)) {
                            foundPaths.add(currentPath);

                            RootShell.log(binaryName + " was found here: " + currentPath);
                        }

                        RootShell.log(line);

                        super.commandOutput(id, line);
                    }
                };

                RootShell.getShell(false).add(cc);
                commandWait(RootShell.getShell(false), cc);

            }

            found = !foundPaths.isEmpty();
        } catch (Exception e) {
            RootShell.log(binaryName + " was not found, more information MAY be available with Debugging on.");
        }

        if (!found) {
            RootShell.log("Trying second method");

            for (String path : searchPaths) {

                if(!path.endsWith("/"))
                {
                    path += "/";
                }

                if (RootShell.exists(path + binaryName)) {
                    RootShell.log(binaryName + " was found here: " + path);
                    foundPaths.add(path);
                } else {
                    RootShell.log(binaryName + " was NOT found here: " + path);
                }
            }
        }

        Collections.reverse(foundPaths);

        return foundPaths;
    }

    /**
     * This will open or return, if one is already open, a custom shell, you are responsible for managing the shell, reading the output
     * and for closing the shell when you are done using it.
     *
     * @param shellPath a <code>String</code> to Indicate the path to the shell that you want to open.
     * @param timeout   an <code>int</code> to Indicate the length of time before giving up on opening a shell.
     * @throws TimeoutException
     * @throws com.stericson.RootShell.exceptions.RootDeniedException
     * @throws IOException
     */
    public static Shell getCustomShell(String shellPath, int timeout) throws IOException, TimeoutException, RootDeniedException
    {
        return RootShell.getCustomShell(shellPath, timeout);
    }

    /**
     * This will return the environment variable PATH
     *
     * @return <code>List<String></code> A List of Strings representing the environment variable $PATH
     */
    public static List<String> getPath() {
        return Arrays.asList(System.getenv("PATH").split(":"));
    }

    /**
     * This will open or return, if one is already open, a shell, you are responsible for managing the shell, reading the output
     * and for closing the shell when you are done using it.
     *
     * @param root         a <code>boolean</code> to Indicate whether or not you want to open a root shell or a standard shell
     * @param timeout      an <code>int</code> to Indicate the length of time to wait before giving up on opening a shell.
     * @param shellContext the context to execute the shell with
     * @param retry        a <code>int</code> to indicate how many times the ROOT shell should try to open with root priviliges...
     */
    public static Shell getShell(boolean root, int timeout, Shell.ShellContext shellContext, int retry) throws IOException, TimeoutException, RootDeniedException {
        if (root) {
            return Shell.startRootShell(timeout, shellContext, retry);
        } else {
            return Shell.startShell(timeout);
        }
    }

    /**
     * This will open or return, if one is already open, a shell, you are responsible for managing the shell, reading the output
     * and for closing the shell when you are done using it.
     *
     * @param root         a <code>boolean</code> to Indicate whether or not you want to open a root shell or a standard shell
     * @param timeout      an <code>int</code> to Indicate the length of time to wait before giving up on opening a shell.
     * @param shellContext the context to execute the shell with
     */
    public static Shell getShell(boolean root, int timeout, Shell.ShellContext shellContext) throws IOException, TimeoutException, RootDeniedException {
        return getShell(root, timeout, shellContext, 3);
    }

    /**
     * This will open or return, if one is already open, a shell, you are responsible for managing the shell, reading the output
     * and for closing the shell when you are done using it.
     *
     * @param root         a <code>boolean</code> to Indicate whether or not you want to open a root shell or a standard shell
     * @param shellContext the context to execute the shell with
     */
    public static Shell getShell(boolean root, Shell.ShellContext shellContext) throws IOException, TimeoutException, RootDeniedException {
        return getShell(root, 0, shellContext, 3);
    }

    /**
     * This will open or return, if one is already open, a shell, you are responsible for managing the shell, reading the output
     * and for closing the shell when you are done using it.
     *
     * @param root    a <code>boolean</code> to Indicate whether or not you want to open a root shell or a standard shell
     * @param timeout an <code>int</code> to Indicate the length of time to wait before giving up on opening a shell.
     */
    public static Shell getShell(boolean root, int timeout) throws IOException, TimeoutException, RootDeniedException {
        return getShell(root, timeout, Shell.defaultContext, 3);
    }

    /**
     * This will open or return, if one is already open, a shell, you are responsible for managing the shell, reading the output
     * and for closing the shell when you are done using it.
     *
     * @param root a <code>boolean</code> to Indicate whether or not you want to open a root shell or a standard shell
     */
    public static Shell getShell(boolean root) throws IOException, TimeoutException, RootDeniedException {
        return RootShell.getShell(root, 0);
    }

    /**
     * @return <code>true</code> if your app has been given root access.
     * @throws TimeoutException if this operation times out. (cannot determine if access is given)
     */
    public static boolean isAccessGiven() {
        final Set<String> ID = new HashSet<String>();
        final int IAG = 158;

        try {
            RootShell.log("Checking for Root access");

            Command command = new Command(IAG, false, "id") {
                @Override
                public void commandOutput(int id, String line) {
                    if (id == IAG) {
                        ID.addAll(Arrays.asList(line.split(" ")));
                    }

                    super.commandOutput(id, line);
                }
            };

            Shell.startRootShell().add(command);
            commandWait(Shell.startRootShell(), command);

            //parse the userid
            for (String userid : ID) {
                RootShell.log(userid);

                if (userid.toLowerCase().contains("uid=0")) {
                    RootShell.log("Access Given");
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @return <code>true</code> if BusyBox was found.
     */
    public static boolean isBusyboxAvailable()
    {
        return (findBinary("busybox")).size() > 0;
    }

    /**
     * @return <code>true</code> if su was found.
     */
    public static boolean isRootAvailable() {
        return (findBinary("su")).size() > 0;
    }

    /**
     * This method allows you to output debug messages only when debugging is on. This will allow
     * you to add a debug option to your app, which by default can be left off for performance.
     * However, when you need debugging information, a simple switch can enable it and provide you
     * with detailed logging.
     * <p/>
     * This method handles whether or not to log the information you pass it depending whether or
     * not RootShell.debugMode is on. So you can use this and not have to worry about handling it
     * yourself.
     *
     * @param msg The message to output.
     */
    public static void log(String msg) {
        log(null, msg, LogLevel.DEBUG, null);
    }

    /**
     * This method allows you to output debug messages only when debugging is on. This will allow
     * you to add a debug option to your app, which by default can be left off for performance.
     * However, when you need debugging information, a simple switch can enable it and provide you
     * with detailed logging.
     * <p/>
     * This method handles whether or not to log the information you pass it depending whether or
     * not RootShell.debugMode is on. So you can use this and not have to worry about handling it
     * yourself.
     *
     * @param TAG Optional parameter to define the tag that the Log will use.
     * @param msg The message to output.
     */
    public static void log(String TAG, String msg) {
        log(TAG, msg, LogLevel.DEBUG, null);
    }

    /**
     * This method allows you to output debug messages only when debugging is on. This will allow
     * you to add a debug option to your app, which by default can be left off for performance.
     * However, when you need debugging information, a simple switch can enable it and provide you
     * with detailed logging.
     * <p/>
     * This method handles whether or not to log the information you pass it depending whether or
     * not RootShell.debugMode is on. So you can use this and not have to worry about handling it
     * yourself.
     *
     * @param msg  The message to output.
     * @param type The type of log, 1 for verbose, 2 for error, 3 for debug, 4 for warn
     * @param e    The exception that was thrown (Needed for errors)
     */
    public static void log(String msg, LogLevel type, Exception e) {
        log(null, msg, type, e);
    }

    /**
     * This method allows you to check whether logging is enabled.
     * Yes, it has a goofy name, but that's to keep it as short as possible.
     * After all writing logging calls should be painless.
     * This method exists to save Android going through the various Java layers
     * that are traversed any time a string is created (i.e. what you are logging)
     * <p/>
     * Example usage:
     * if(islog) {
     * StrinbBuilder sb = new StringBuilder();
     * // ...
     * // build string
     * // ...
     * log(sb.toString());
     * }
     *
     * @return true if logging is enabled
     */
    public static boolean islog() {
        return debugMode;
    }

    /**
     * This method allows you to output debug messages only when debugging is on. This will allow
     * you to add a debug option to your app, which by default can be left off for performance.
     * However, when you need debugging information, a simple switch can enable it and provide you
     * with detailed logging.
     * <p/>
     * This method handles whether or not to log the information you pass it depending whether or
     * not RootShell.debugMode is on. So you can use this and not have to worry about handling it
     * yourself.
     *
     * @param TAG  Optional parameter to define the tag that the Log will use.
     * @param msg  The message to output.
     * @param type The type of log, 1 for verbose, 2 for error, 3 for debug
     * @param e    The exception that was thrown (Needed for errors)
     */
    public static void log(String TAG, String msg, LogLevel type, Exception e) {
        if (msg != null && !msg.equals("")) {
            if (debugMode) {
                if (TAG == null) {
                    TAG = version;
                }

                switch (type) {
                    case VERBOSE:
                        Log.v(TAG, msg);
                        break;
                    case ERROR:
                        Log.e(TAG, msg, e);
                        break;
                    case DEBUG:
                        Log.d(TAG, msg);
                        break;
                    case WARN:
                        Log.w(TAG, msg);
                        break;
                }
            }
        }
    }

    // --------------------
    // # Public Methods #
    // --------------------

    private static void commandWait(Shell shell, Command cmd) throws Exception {
        while (!cmd.isFinished()) {

            RootShell.log(version, shell.getCommandQueuePositionString(cmd));
            RootShell.log(version, "Processed " + cmd.totalOutputProcessed + " of " + cmd.totalOutput + " output from command.");

            synchronized (cmd) {
                try {
                    if (!cmd.isFinished()) {
                        cmd.wait(2000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!cmd.isExecuting() && !cmd.isFinished()) {
                if (!shell.isExecuting && !shell.isReading) {
                    RootShell.log(version, "Waiting for a command to be executed in a shell that is not executing and not reading! \n\n Command: " + cmd.getCommand());
                    Exception e = new Exception();
                    e.setStackTrace(Thread.currentThread().getStackTrace());
                    e.printStackTrace();
                } else if (shell.isExecuting && !shell.isReading) {
                    RootShell.log(version, "Waiting for a command to be executed in a shell that is executing but not reading! \n\n Command: " + cmd.getCommand());
                    Exception e = new Exception();
                    e.setStackTrace(Thread.currentThread().getStackTrace());
                    e.printStackTrace();
                } else {
                    RootShell.log(version, "Waiting for a command to be executed in a shell that is not reading! \n\n Command: " + cmd.getCommand());
                    Exception e = new Exception();
                    e.setStackTrace(Thread.currentThread().getStackTrace());
                    e.printStackTrace();
                }
            }

        }
    }
}

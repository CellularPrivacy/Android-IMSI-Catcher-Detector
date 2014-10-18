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
package com.stericson.RootTools.execution;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import android.content.Context;
import android.provider.DocumentsContract;
import android.util.Log;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;

public class Shell {

    public static enum ShellType {
        NORMAL,
        ROOT,
        CUSTOM
    }

    //this is only used with root shells
    public static enum ShellContext {
        NORMAL("normal"), //The normal context...
        SHELL("u:r:shell:s0"), //Unpriviliged shell (such as an adb shell)
        SYSTEM_SERVER("u:r:system_server:s0"), // system_server, u:r:system:s0 on some firmwares
        SYSTEM_APP("u:r:system_app:s0"), // System apps
        PLATFORM_APP("u:r:platform_app:s0"), // System apps
        UNTRUSTED_APP("u:r:untrusted_app:s0"), // Third-party apps
        RECOVERY("u:r:recovery:s0"); //Recovery

        private String value;

        private ShellContext(String value)
        {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

    }

    //Statics -- visible to all
    private static final String token = "F*D^W@#FGF";
    private static Shell rootShell = null;
    private static Shell shell = null;
    private static Shell customShell = null;

    //the default context for root shells...
    public static ShellContext defaultContext = ShellContext.NORMAL;

    //per shell
    private int shellTimeout = 25000;
    private ShellType shellType = null;
    private ShellContext shellContext = Shell.ShellContext.NORMAL;

    private String error = "";

    private final Process proc;
    private final BufferedReader in;
    private final OutputStreamWriter out;
    private final List<Command> commands = new ArrayList<Command>();

    //indicates whether or not to close the shell
    private boolean close = false;

    public boolean isExecuting = false;
    public boolean isReading = false;
    public boolean isClosed = false;

    private int maxCommands = 5000;
    private int read = 0;
    private int write = 0;
    private int totalExecuted = 0;
    private int totalRead = 0;
    private boolean isCleaning = false;

    private Shell(String cmd, ShellType shellType, ShellContext shellContext, int shellTimeout) throws IOException, TimeoutException, RootDeniedException {

        RootTools.log("Starting shell: " + cmd);
        RootTools.log("Context: " + shellContext.getValue());
        RootTools.log("Timeout: " + shellTimeout);

        this.shellType = shellType;
        this.shellTimeout = shellTimeout > 0 ? shellTimeout : this.shellTimeout;
        this.shellContext = shellContext;

        if(this.shellContext == ShellContext.NORMAL)
        {
            this.proc = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        }
        else
        {
            //only done for root shell...
            this.proc = new ProcessBuilder(cmd, "--context " + this.shellContext.getValue()).redirectErrorStream(true).start();
        }

        this.in = new BufferedReader(new InputStreamReader(this.proc.getInputStream(), "UTF-8"));
        this.out = new OutputStreamWriter(this.proc.getOutputStream(), "UTF-8");

        /**
         * Thread responsible for carrying out the requested operations
         */
        Worker worker = new Worker(this);
        worker.start();

        try {
            /**
             * The flow of execution will wait for the thread to die or wait until the
             * given timeout has expired.
             *
             * The result of the worker, which is determined by the exit code of the worker,
             * will tell us if the operation was completed successfully or it the operation
             * failed.
             */
            worker.join(this.shellTimeout);

            /**
             * The operation could not be completed before the timeout occured.
             */
            if (worker.exit == -911) {

                try {
                    this.proc.destroy();
                } catch (Exception e) {}

                closeQuietly(this.in);
                closeQuietly(this.out);

                throw new TimeoutException(this.error);
            }
            /**
             * Root access denied?
             */
            else if (worker.exit == -42) {

                try {
                    this.proc.destroy();
                } catch (Exception e) {}

                closeQuietly(this.in);
                closeQuietly(this.out);

                throw new RootDeniedException("Root Access Denied");
            }
            /**
             * Normal exit
             */
            else {
                /**
                 * The shell is open.
                 *
                 * Start two threads, one to handle the input and one to handle the output.
                 *
                 * input, and output are runnables that the threads execute.
                 */
                Thread si = new Thread(this.input, "Shell Input");
                si.setPriority(Thread.NORM_PRIORITY);
                si.start();

                Thread so = new Thread(this.output, "Shell Output");
                so.setPriority(Thread.NORM_PRIORITY);
                so.start();
            }
        } catch (InterruptedException ex) {
            worker.interrupt();
            Thread.currentThread().interrupt();
            throw new TimeoutException();
        }
    }


    public Command add(Command command) throws IOException {
        if (this.close)
            throw new IllegalStateException(
                    "Unable to add commands to a closed shell");

        while (this.isCleaning) {
            //Don't add commands while cleaning
            ;
        }
        this.commands.add(command);

        this.notifyThreads();

        return command;
    }

    public void useCWD(Context context) throws IOException, TimeoutException, RootDeniedException {
        add(
                new CommandCapture(
                        -1,
                        false,
                        "cd " + context.getApplicationInfo().dataDir)
        );
    }

    private void cleanCommands() {
        this.isCleaning = true;
        int toClean = Math.abs(this.maxCommands - (this.maxCommands / 4));
        RootTools.log("Cleaning up: " + toClean);

        for (int i = 0; i < toClean; i++) {
            this.commands.remove(0);
        }

        this.read = this.commands.size() - 1;
        this.write = this.commands.size() - 1;
        this.isCleaning = false;
    }

    private void closeQuietly(final Reader input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (Exception ignore) {}
    }

    private void closeQuietly(final Writer output) {
        try {
            if (output != null) {
                output.close();
            }
        } catch (Exception ignore) {}
    }

    public void close() throws IOException {

        synchronized (this.commands) {
            /**
             * instruct the two threads monitoring input and output
             * of the shell to close.
             */
            this.close = true;
            this.notifyThreads();
        }

        int count = 0;
        while(isExecuting)
        {
            RootTools.log("Waiting on shell to finish executing before closing...");
            count++;

            //failsafe to keep from hanging...
            if(count > 1000)
            {
                break;
            }
        }

        RootTools.log("Shell Closed!");

        if (this == Shell.rootShell)
            Shell.rootShell = null;
        else if (this == Shell.shell)
            Shell.shell = null;
        else if (this == Shell.customShell)
            Shell.customShell = null;
    }

    public static void closeCustomShell() throws IOException {
        if (Shell.customShell == null)
            return;
        Shell.customShell.close();
    }

    public static void closeRootShell() throws IOException {
        if (Shell.rootShell == null)
            return;
        Shell.rootShell.close();
    }

    public static void closeShell() throws IOException {
        if (Shell.shell == null)
            return;
        Shell.shell.close();
    }

    public static void closeAll() throws IOException {
        Shell.closeShell();
        Shell.closeRootShell();
        Shell.closeCustomShell();
    }

    public int getCommandQueuePosition(Command cmd) {
        return this.commands.indexOf(cmd);
    }

    public String getCommandQueuePositionString(Command cmd) {
        return "Command is in position " + getCommandQueuePosition(cmd) + " currently executing command at position " + this.write + " and the number of commands is " + commands.size();
    }

    public static Shell getOpenShell() {
        if (Shell.customShell != null)
            return Shell.customShell;
        else if (Shell.rootShell != null)
            return Shell.rootShell;
        else
            return Shell.shell;
    }

    public static boolean isShellOpen() {
        return Shell.shell == null;
    }

    public static boolean isCustomShellOpen() {
        return Shell.customShell == null;
    }

    public static boolean isRootShellOpen() {
        return Shell.rootShell == null;
    }

    public static boolean isAnyShellOpen() {
        return Shell.shell != null || Shell.rootShell != null || Shell.customShell != null;
    }

    /**
     * Runnable to write commands to the open shell.
     * <p/>
     * When writing commands we stay in a loop and wait for new
     * commands to added to "commands"
     * <p/>
     * The notification of a new command is handled by the method add in this class
     */
    private Runnable input = new Runnable() {
        public void run() {

            try {
                while (true) {

                    synchronized (commands) {
                        /**
                         * While loop is used in the case that notifyAll is called
                         * and there are still no commands to be written, a rare
                         * case but one that could happen.
                         */
                        while (!close && write >= commands.size()) {
                            isExecuting = false;
                            commands.wait();
                        }
                    }

                    if (write >= maxCommands) {

                        /**
                         * wait for the read to catch up.
                         */
                        while (read != write)
                        {
                            RootTools.log("Waiting for read and write to catch up before cleanup.");
                        }
                        /**
                         * Clean up the commands, stay neat.
                         */
                        cleanCommands();
                    }

                    /**
                     * Write the new command
                     *
                     * We write the command followed by the token to indicate
                     * the end of the command execution
                     */
                    if (write < commands.size()) {
                        isExecuting = true;
                        Command cmd = commands.get(write);
                        cmd.startExecution();
                        RootTools.log("Executing: " + cmd.getCommand() + " with context: " + shellContext);

                        out.write(cmd.getCommand());
                        String line = "\necho " + token + " " + totalExecuted + " $?\n";
                        out.write(line);
                        out.flush();
                        write++;
                        totalExecuted++;
                    } else if (close) {
                        /**
                         * close the thread, the shell is closing.
                         */
                        isExecuting = false;
                        out.write("\nexit 0\n");
                        out.flush();
                        RootTools.log("Closing shell");
                        return;
                    }
                }
            } catch (IOException e) {
                RootTools.log(e.getMessage(), 2, e);
            } catch (InterruptedException e) {
                RootTools.log(e.getMessage(), 2, e);
            } finally {
                write = 0;
                closeQuietly(out);
            }
        }
    };

    protected void notifyThreads() {
        Thread t = new Thread() {
            public void run() {
                synchronized (commands) {
                    commands.notifyAll();
                }
            }
        };

        t.start();
    }

    /**
     * Runnable to monitor the responses from the open shell.
     */
    private Runnable output = new Runnable() {
        public void run() {
            try {
                Command command = null;

                while (!close) {
                    isReading = false;
                    String line = in.readLine();
                    isReading = true;

                    /**
                     * If we recieve EOF then the shell closed
                     */
                    if (line == null)
                        break;

                    if (command == null) {
                        if (read >= commands.size()) {
                            if (close)
                                break;

                            continue;
                        }

                        command = commands.get(read);
                    }

                    /**
                     * trying to determine if all commands have been completed.
                     *
                     * if the token is present then the command has finished execution.
                     */
                    int pos = line.indexOf(token);


                    if (pos == -1) {
                        /**
                         * send the output for the implementer to process
                         */
                        command.output(command.id, line);
                    }
                    if (pos > 0) {
                    	/**
                    	 * token is suffix of output, send output part to implementer
                    	 */
                    	command.output(command.id, line.substring(0, pos));
                    }
                    if (pos >= 0) {
                    	line = line.substring(pos);
                        String fields[] = line.split(" ");

                        if (fields.length >= 2 && fields[1] != null) {
                            int id = 0;

                            try {
                                id = Integer.parseInt(fields[1]);
                            } catch (NumberFormatException e) {
                            }

                            int exitCode = -1;

                            try {
                                exitCode = Integer.parseInt(fields[2]);
                            } catch (NumberFormatException e) {
                            }

                            if (id == totalRead) {
                                command.setExitCode(exitCode);
                                command.commandFinished();
                                command = null;

                                read++;
                                totalRead++;
                                continue;
                            }
                        }
                    }
                }

                RootTools.log("Read all output");
                try {
                    proc.waitFor();
                    proc.destroy();
                } catch (Exception e) {}

                closeQuietly(out);
                closeQuietly(in);

                while (read < commands.size()) {
                    if (command == null)
                        command = commands.get(read);

                    command.terminated("Unexpected Termination.");
                    command = null;
                    read++;
                }

                read = 0;

            } catch (IOException e) {
                RootTools.log(e.getMessage(), 2, e);
            }
            finally {
                RootTools.log("Shell destroyed");
                isClosed = true;
                isReading = false;
            }
        }
    };

    public static void runRootCommand(Command command) throws IOException, TimeoutException, RootDeniedException {
        Shell.startRootShell().add(command);
    }

    public static void runCommand(Command command) throws IOException, TimeoutException {
        Shell.startShell().add(command);
    }

    public static Shell startRootShell() throws IOException, TimeoutException, RootDeniedException {
        return Shell.startRootShell(0, 3);
    }

    public static Shell startRootShell(int timeout) throws IOException, TimeoutException, RootDeniedException {
        return Shell.startRootShell(timeout, 3);
    }

    public static Shell startRootShell(int timeout, int retry) throws IOException, TimeoutException, RootDeniedException {
        return Shell.startRootShell(timeout, Shell.defaultContext, retry);
    }

    public static Shell startRootShell(int timeout, ShellContext shellContext, int retry) throws IOException, TimeoutException, RootDeniedException {

        if (Shell.rootShell == null) {

            RootTools.log("Starting Root Shell!");
            String cmd = "su";
            // keep prompting the user until they accept for x amount of times...
            int retries = 0;
            while (Shell.rootShell == null) {
                try {
                    Shell.rootShell = new Shell(cmd, ShellType.ROOT, shellContext, timeout);
                } catch (IOException e) {
                    if (retries++ >= retry) {
                        RootTools.log("IOException, could not start shell");
                        throw e;
                    }
                }
            }
        }
        else if (Shell.rootShell.shellContext != shellContext) {
            try {
                RootTools.log("Context is different than open shell, switching context... " + Shell.rootShell.shellContext + " VS " + shellContext);
                Shell.rootShell.switchRootShellContext(shellContext);
            } catch (IOException e) {
                RootTools.log("Context could not be switched for existing root shell...");
                throw e;
            }
        } else {
            RootTools.log("Using Existing Root Shell!");
        }

        return Shell.rootShell;
    }

    public static Shell startCustomShell(String shellPath) throws IOException, TimeoutException, RootDeniedException {
        return Shell.startCustomShell(shellPath, 0);
    }

    public static Shell startCustomShell(String shellPath, int timeout) throws IOException, TimeoutException, RootDeniedException {

        if (Shell.customShell == null) {
            RootTools.log("Starting Custom Shell!");
            Shell.customShell = new Shell(shellPath, ShellType.CUSTOM, ShellContext.NORMAL, timeout);
        } else
            RootTools.log("Using Existing Custom Shell!");

        return Shell.customShell;
    }

    public static Shell startShell() throws IOException, TimeoutException {
        return Shell.startShell(0);
    }

    public static Shell startShell(int timeout) throws IOException, TimeoutException {

        try {
            if (Shell.shell == null) {
                RootTools.log("Starting Shell!");
                Shell.shell = new Shell("/system/bin/sh", ShellType.NORMAL, ShellContext.NORMAL, timeout);
            } else
                RootTools.log("Using Existing Shell!");
            return Shell.shell;
        } catch (RootDeniedException e) {
            //Root Denied should never be thrown.
            throw new IOException();
        }
    }

    public Shell switchRootShellContext(ShellContext shellContext) throws IOException, TimeoutException, RootDeniedException {
        if(this.shellType == ShellType.ROOT)
        {
            try {
                Shell.closeRootShell();
            } catch(Exception e) {
                RootTools.log("Problem closing shell while trying to switch context...");
            }

            //create new root shell with new context...

            return Shell.startRootShell(this.shellTimeout, shellContext, 3);
        }
        else
        {
            //can only switch context on a root shell...
            RootTools.log("Can only switch context on a root shell!");
            return this;
        }
    }

    protected static class Worker extends Thread {
        public int exit = -911;

        public Shell shell;

        private Worker(Shell shell) {
            this.shell = shell;
        }

        public void run() {

            /**
             * Trying to open the shell.
             *
             * We echo "Started" and we look for it in the output.
             *
             * If we find the output then the shell is open and we return.
             *
             * If we do not find it then we determine the error and report
             * it by setting the value of the variable exit
             */
            try {
                shell.out.write("echo Started\n");
                shell.out.flush();

                while (true) {
                    String line = shell.in.readLine();
                    if (line == null) {
                        throw new EOFException();
                    }
                    if ("".equals(line))
                        continue;
                    if ("Started".equals(line)) {
                        this.exit = 1;
                        setShellOom();
                        break;
                    }

                    shell.error = "unkown error occured.";
                }
            } catch (IOException e) {
                exit = -42;
                if (e.getMessage() != null)
                    shell.error = e.getMessage();
                else
                    shell.error = "RootAccess denied?.";
            }

        }

        /*
         * setOom for shell processes (sh and su if root shell)
         * and discard outputs
         * 
         */
        private void setShellOom() {
			try {
				Class<?> processClass = shell.proc.getClass();
				Field field;
				try {
					field = processClass.getDeclaredField("pid");
				} catch (NoSuchFieldException e) {
					field = processClass.getDeclaredField("id");
				}
				field.setAccessible(true);
				int pid = (Integer) field.get(shell.proc);
                shell.out.write("(echo -17 > /proc/" + pid + "/oom_adj) &> /dev/null\n");
                shell.out.write("(echo -17 > /proc/$$/oom_adj) &> /dev/null\n");
                shell.out.flush();
			} catch (Exception e) {
                e.printStackTrace();
			}
		}
    }
}

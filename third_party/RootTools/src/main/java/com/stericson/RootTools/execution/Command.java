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

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;

import com.stericson.RootTools.RootTools;

public abstract class Command {

    ExecutionMonitor executionMonitor = null;
    Handler mHandler = null;
    boolean executing = false;

    String[] command = {};
    boolean javaCommand = false;
    Context context = null;
    boolean finished = false;
    boolean terminated = false;
    boolean handlerEnabled = true;
    int exitCode = -1;
    int id = 0;
    int timeout = RootTools.default_Command_Timeout;

    public abstract void commandOutput(int id, String line);
    public abstract void commandTerminated(int id, String reason);
    public abstract void commandCompleted(int id, int exitCode);

    /**
     * Constructor for executing a normal shell command
     * @param id the id of the command being executed
     * @param command the command, or commands, to be executed.
     */
    public Command(int id, String... command) {
        this.command = command;
        this.id = id;

        createHandler(RootTools.handlerEnabled);
    }

    /**
     * Constructor for executing a normal shell command
     * @param id the id of the command being executed
     * @param handlerEnabled when true the handler will be used to call the
     *                       callback methods if possible.
     * @param command the command, or commands, to be executed.
     */
    public Command(int id, boolean handlerEnabled, String... command) {
        this.command = command;
        this.id = id;

        createHandler(handlerEnabled);
    }

    /**
     * Constructor for executing a normal shell command
     * @param id the id of the command being executed
     * @param timeout the time allowed before the shell will give up executing the command
     *                and throw a TimeoutException.
     * @param command the command, or commands, to be executed.
     */
    public Command(int id, int timeout, String... command) {
        this.command = command;
        this.id = id;
        this.timeout = timeout;

        createHandler(RootTools.handlerEnabled);
    }

    /**
     * Constructor for executing Java commands rather than binaries
     * @param javaCommand when True, it is a java command.
     * @param context     needed to execute java command.
     */
    public Command(int id, boolean javaCommand, Context context, String... command) {
        this(id, command);
        this.javaCommand = javaCommand;
        this.context = context;
    }

    /**
     * Constructor for executing Java commands rather than binaries
     * @param javaCommand when True, it is a java command.
     * @param context     needed to execute java command.
     */
    public Command(int id, boolean handlerEnabled, boolean javaCommand, Context context, String... command) {
        this(id, handlerEnabled, command);
        this.javaCommand = javaCommand;
        this.context = context;
    }

    /**
     * Constructor for executing Java commands rather than binaries
     * @param javaCommand when True, it is a java command.
     * @param context     needed to execute java command.
     */
    public Command(int id, int timeout, boolean javaCommand, Context context, String... command) {
        this(id, timeout, command);
        this.javaCommand = javaCommand;
        this.context = context;
    }

    protected void finishCommand() {
        executing = false;
        finished = true;
        this.notifyAll();
    }

    protected void commandFinished() {
        if (!terminated) {
            synchronized (this) {
                if (mHandler != null && handlerEnabled) {
                    Message msg = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putInt(CommandHandler.ACTION, CommandHandler.COMMAND_COMPLETED);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                }
                else {
                    commandCompleted(id, exitCode);
                }

                RootTools.log("Command " + id + " finished.");
                finishCommand();
            }
        }
    }

    private void createHandler(boolean handlerEnabled) {

        this.handlerEnabled = handlerEnabled;

        if (Looper.myLooper() != null && handlerEnabled) {
            RootTools.log("CommandHandler created");
            mHandler = new CommandHandler();
        }
        else {
            RootTools.log("CommandHandler not created");
        }
    }

    public String getCommand() {
        StringBuilder sb = new StringBuilder();

        if(javaCommand) {
            String filePath = context.getFilesDir().getPath();
            for (int i = 0; i < command.length; i++) {

                if(i > 0)
                {
                    sb.append('\n');
                }

                /*
                 * TODO Make withFramework optional for applications
                 * that do not require access to the fw. -CFR
                 */
                sb.append(
                        "dalvikvm -cp " + filePath + "/anbuild.dex"
                        + " com.android.internal.util.WithFramework"
                        + " com.stericson.RootTools.containers.RootClass "
                        + command[i]);
            }
        }
        else {
            for (int i = 0; i < command.length; i++) {
                if(i > 0)
                {
                    sb.append('\n');
                }

                sb.append(command[i]);
            }
        }
        return sb.toString();
    }

    public boolean isExecuting() {
        return executing;
    }

    public boolean isHandlerEnabled() {
        return handlerEnabled;
    }

    public boolean isFinished() {
        return finished;
    }

    public int getExitCode() {
        return this.exitCode;
    }

    protected void setExitCode(int code) {
        synchronized (this) {
            exitCode = code;
        }
    }

    protected void startExecution() {
        executionMonitor = new ExecutionMonitor();
        executionMonitor.setPriority(Thread.MIN_PRIORITY);
        executionMonitor.start();
        executing = true;
    }

    public void terminate(String reason) {
        try {
            Shell.closeAll();
            RootTools.log("Terminating all shells.");
            terminated(reason);
        } catch (IOException e) {}
    }

    protected void terminated(String reason) {
        synchronized (Command.this) {


            if (mHandler != null && handlerEnabled) {
                Message msg = mHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putInt(CommandHandler.ACTION, CommandHandler.COMMAND_TERMINATED);
                bundle.putString(CommandHandler.TEXT, reason);
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }
            else {
                commandTerminated(id, reason);
            }

            RootTools.log("Command " + id + " did not finish because it was terminated. Termination reason: " + reason);
            setExitCode(-1);
            terminated = true;
            finishCommand();
        }
    }

    protected void output(int id, String line) {
        if (mHandler != null && handlerEnabled) {
            Message msg = mHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putInt(CommandHandler.ACTION, CommandHandler.COMMAND_OUTPUT);
            bundle.putString(CommandHandler.TEXT, line);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
        else {
            commandOutput(id, line);
        }
    }

    private class ExecutionMonitor extends Thread {
        public void run() {
            while (!finished) {

                synchronized (Command.this) {
                    try {
                        Command.this.wait(timeout);
                    } catch (InterruptedException e) {}
                }

                if (!finished) {
                    RootTools.log("Timeout Exception has occurred.");
                    terminate("Timeout Exception");
                }
            }
        }
    }

    private class CommandHandler extends Handler {
        static final public String ACTION = "action";
        static final public String TEXT = "text";

        static final public int COMMAND_OUTPUT = 0x01;
        static final public int COMMAND_COMPLETED = 0x02;
        static final public int COMMAND_TERMINATED = 0x03;

        public void handleMessage(Message msg) {
            int action = msg.getData().getInt(ACTION);
            String text = msg.getData().getString(TEXT);

            switch (action) {
                case COMMAND_OUTPUT:
                    commandOutput(id, text);
                    break;
                case COMMAND_COMPLETED:
                    commandCompleted(id, exitCode);
                    break;
                case COMMAND_TERMINATED:
                    commandTerminated(id, text);
                    break;
            }
        }
    }
}

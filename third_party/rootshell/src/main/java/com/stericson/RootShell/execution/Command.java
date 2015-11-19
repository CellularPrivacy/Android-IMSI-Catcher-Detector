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

package com.stericson.RootShell.execution;

import com.stericson.RootShell.RootShell;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;

public class Command {

    //directly modified by JavaCommand
    protected boolean javaCommand = false;
    protected Context context = null;

    public int totalOutput = 0;

    public int totalOutputProcessed = 0;

    ExecutionMonitor executionMonitor = null;

    Handler mHandler = null;

    boolean executing = false;

    String[] command = {};

    boolean finished = false;

    boolean terminated = false;

    boolean handlerEnabled = true;

    int exitCode = -1;

    int id = 0;

    int timeout = RootShell.defaultCommandTimeout;

    /**
     * Constructor for executing a normal shell command
     *
     * @param id      the id of the command being executed
     * @param command the command, or commands, to be executed.
     */
    public Command(int id, String... command) {
        this.command = command;
        this.id = id;

        createHandler(RootShell.handlerEnabled);
    }

    /**
     * Constructor for executing a normal shell command
     *
     * @param id             the id of the command being executed
     * @param handlerEnabled when true the handler will be used to call the
     *                       callback methods if possible.
     * @param command        the command, or commands, to be executed.
     */
    public Command(int id, boolean handlerEnabled, String... command) {
        this.command = command;
        this.id = id;

        createHandler(handlerEnabled);
    }

    /**
     * Constructor for executing a normal shell command
     *
     * @param id      the id of the command being executed
     * @param timeout the time allowed before the shell will give up executing the command
     *                and throw a TimeoutException.
     * @param command the command, or commands, to be executed.
     */
    public Command(int id, int timeout, String... command) {
        this.command = command;
        this.id = id;
        this.timeout = timeout;

        createHandler(RootShell.handlerEnabled);
    }

    //If you override this you MUST make a final call
    //to the super method. The super call should be the last line of this method.
    public void commandOutput(int id, String line) {
        RootShell.log("Command", "ID: " + id + ", " + line);
        totalOutputProcessed++;
    }

    public void commandTerminated(int id, String reason) {
        //pass
    }

    public void commandCompleted(int id, int exitcode) {
        //pass
    }

    protected final void commandFinished() {
        if (!terminated) {
            synchronized (this) {
                if (mHandler != null && handlerEnabled) {
                    Message msg = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putInt(CommandHandler.ACTION, CommandHandler.COMMAND_COMPLETED);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                } else {
                    commandCompleted(id, exitCode);
                }

                RootShell.log("Command " + id + " finished.");
                finishCommand();
            }
        }
    }

    private void createHandler(boolean handlerEnabled) {

        this.handlerEnabled = handlerEnabled;

        if (Looper.myLooper() != null && handlerEnabled) {
            RootShell.log("CommandHandler created");
            mHandler = new CommandHandler();
        } else {
            RootShell.log("CommandHandler not created");
        }
    }

    public final void finish()
    {
        RootShell.log("Command finished at users request!");
        commandFinished();
    }

    protected final void finishCommand() {
        executing = false;
        finished = true;
        this.notifyAll();
    }


    public final String getCommand() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < command.length; i++) {
            if (i > 0) {
                sb.append('\n');
            }

            sb.append(command[i]);
        }

        return sb.toString();
    }

    public final boolean isExecuting() {
        return executing;
    }

    public final boolean isHandlerEnabled() {
        return handlerEnabled;
    }

    public final boolean isFinished() {
        return finished;
    }

    public final int getExitCode() {
        return this.exitCode;
    }

    protected final void setExitCode(int code) {
        synchronized (this) {
            exitCode = code;
        }
    }

    protected final void startExecution() {
        executionMonitor = new ExecutionMonitor();
        executionMonitor.setPriority(Thread.MIN_PRIORITY);
        executionMonitor.start();
        executing = true;
    }

    public final void terminate()
    {
        RootShell.log("Terminating command at users request!");
        terminated("Terminated at users request!");
    }

    protected final void terminate(String reason) {
        try {
            Shell.closeAll();
            RootShell.log("Terminating all shells.");
            terminated(reason);
        } catch (IOException e) {
        }
    }

    protected final void terminated(String reason) {
        synchronized (Command.this) {

            if (mHandler != null && handlerEnabled) {
                Message msg = mHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putInt(CommandHandler.ACTION, CommandHandler.COMMAND_TERMINATED);
                bundle.putString(CommandHandler.TEXT, reason);
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            } else {
                commandTerminated(id, reason);
            }

            RootShell.log("Command " + id + " did not finish because it was terminated. Termination reason: " + reason);
            setExitCode(-1);
            terminated = true;
            finishCommand();
        }
    }

    protected final void output(int id, String line) {
        totalOutput++;

        if (mHandler != null && handlerEnabled) {
            Message msg = mHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putInt(CommandHandler.ACTION, CommandHandler.COMMAND_OUTPUT);
            bundle.putString(CommandHandler.TEXT, line);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        } else {
            commandOutput(id, line);
        }
    }

    public final void resetCommand()
    {
        this.finished = false;
        this.totalOutput = 0;
        this.totalOutputProcessed = 0;
        this.executing = false;
        this.terminated = false;
        this.exitCode = -1;
    }

    private class ExecutionMonitor extends Thread {

        public void run() {

            if(timeout > 0)
            {
                //We need to kill the command after the given timeout
                while (!finished) {

                    synchronized (Command.this) {
                        try {
                            Command.this.wait(timeout);
                        } catch (InterruptedException e) {
                        }
                    }

                    if (!finished) {
                        RootShell.log("Timeout Exception has occurred.");
                        terminate("Timeout Exception");
                    }
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

        public final void handleMessage(Message msg) {
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

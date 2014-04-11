/**   Copyright (C) 2013  Louis Teboul (a.k.a Androguide)
 *
 *    admin@pimpmyrom.org  || louisteboul@gmail.com
 *    http://pimpmyrom.org || http://androguide.fr
 *    71 quai Clémenceau, 69300 Caluire-et-Cuire, FRANCE.
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License along
 *      with this program; if not, write to the Free Software Foundation, Inc.,
 *      51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 **/

package com.SecUpwN.AIMSICD.cmdprocessor;

import android.os.AsyncTask;

/**
 * An abstract implentation of AsyncTask
 * <p/>
 * since our needs are simple send a command, perform a task when we finish
 * this implentation requires you send the command as String...
 * in the .execute(String) so you can send String[] of commands if needed
 * <p/>
 * This class is not for you if...
 * 1) You do not need to perform any action after command execution
 * you want a Thread not this.
 * 2) You need to perform more complex tasks in doInBackground
 * than simple script/command sequence of commands
 * you want your own AsyncTask not this.
 * <p/>
 * This class is for you if...
 * 1) You need to run a command/script/sequence of commands without
 * blocking the UI thread and you must perform actions after the
 * task completes.
 * 2) see #1.
 */
public abstract class AbstractAsyncSuCMDProcessor extends AsyncTask<String, Void, String> {
    // if /system needs to be mounted before command
    private boolean mMountSystem;
    // return if we receive a null command or empty command
    public final String FAILURE = "failed_no_command";

    /**
     * Constructor that allows mounting/dismounting
     * of /system partition while in background thread
     */
    public AbstractAsyncSuCMDProcessor(boolean mountSystem) {
        this.mMountSystem = mountSystem;
    }

    /**
     * Constructor that assumes /system should not be mounted
     */
    public AbstractAsyncSuCMDProcessor() {
        this.mMountSystem = false;
    }

    /**
     * DO NOT override this method you should simply send your commands off
     * as params and expect to handle results in {@link #onPostExecute}
     * <p/>
     * if you find a need to @Override this method then you should
     * consider using a new AsyncTask implementation instead
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     */
    @Override
    protected String doInBackground(String... params) {
        // don't bother if we don't get a command
        if (params[0] == null || params[0].trim().equals("")) {
            return FAILURE;
        }

        String stdout = null;

        // conditionally enforce mounting
        if (mMountSystem) {
            Helpers.getMount("rw");
        }
        try {
            // process all commands ***DO NOT SEND null OR ""; you have been warned***
            for (String param : params) {
                // always watch for null and empty strings, lazy devs :/
                if (param != null && !param.trim().equals("")) {
                    stdout = CMDProcessor.runSuCommand(param).getStdout();
                } else {
                    // bail because of careless devs
                    return FAILURE;
                }
            }
            // always unmount
        } finally {
            if (mMountSystem) {
                Helpers.getMount("ro");
            }
        }
        // return the stdout from the command
        return stdout;
    }

    /**
     * <p>Runs on the UI thread after {@link #doInBackground}. The
     * specified result is the value returned by {@link #doInBackground}.</p>
     * <p/>
     * <p>This method won't be invoked if the task was cancelled.</p>
     * <p/>
     * You MUST @Override this method if you don't need the result
     * then you should consider using a new Thread implentation instead
     *
     * @param result The result of the operation computed by {@link #doInBackground}.
     */
    @Override
    protected abstract void onPostExecute(String result);
}

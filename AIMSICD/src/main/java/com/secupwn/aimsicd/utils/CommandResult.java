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

package com.secupwn.aimsicd.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import io.freefair.android.util.logging.AndroidLogger;
import io.freefair.android.util.logging.Logger;
import lombok.Getter;

@SuppressWarnings("AccessOfSystemProperties")
public class CommandResult implements Parcelable {

    private final Logger log = AndroidLogger.forClass(CommandResult.class);
    private long startTime;
    @Getter
    private int exitValue;
    @Getter
    private String stdOut;
    @Getter
    private String stdErr;
    private long endTime;

    public CommandResult(long startTime, int exitValue,
                         String stdout, String stderr, long endTime) {
        this.startTime = startTime;
        this.exitValue = exitValue;
        this.stdOut = stdout;
        this.stdErr = stderr;
        this.endTime = endTime;

        log.debug("Time to execute: " + (this.endTime - this.startTime) + " ns (nanoseconds)");
        // this is set last so log from here
        checkForErrors();
    }

    // pretty much just forward the constructor from parcelable to our main
    // loading constructor
    @SuppressWarnings("CastToConcreteClass")
    public CommandResult(Parcel inParcel) {
        this(inParcel.readLong(), inParcel.readInt(), inParcel.readString(),
                inParcel.readString(), inParcel.readLong());
    }

    public boolean success() {
        return (exitValue == 0);
    }

    @SuppressWarnings("UnnecessaryExplicitNumericCast")
    private void checkForErrors() {
        if (exitValue != 0 || !stdErr.trim().isEmpty()) {
            // don't log the commands that failed
            // because the cpu was offline
            boolean skipOfflineCpu =
                    // if core is off locking fails
                    stdErr.contains("chmod: /sys/devices/system/cpu/cpu")
                            // if core is off applying cpu freqs fails
                            || stdErr.contains(": can't create /sys/devices/system/cpu/cpu");
            String lineEnding = System.getProperty("line.separator");
            FileWriter errorWriter = null;
            try {
                File errorLogFile = new File(RealmHelper.mExternalFilesDirPath + "error.txt");
                if (!errorLogFile.exists()) {
                    errorLogFile.createNewFile();
                }
                errorWriter = new FileWriter(errorLogFile, true);
                // only log the cpu state as offline while writing
                if (skipOfflineCpu) {
                    errorWriter.write(lineEnding);
                    errorWriter.write("Attempted to write to an offline cpu core (ignore me).");
                } else {
                    errorWriter.write("shell error detected!");
                    errorWriter.write(lineEnding);
                    errorWriter.write("CommandResult {" + this.toString() + '}');
                    errorWriter.write(lineEnding);
                }
                errorWriter.write(lineEnding);
            } catch (IOException e) {
                log.error("Failed to write command result to error file", e);
            } finally {
                if (errorWriter != null) {
                    try {
                        errorWriter.close();
                    } catch (IOException ignored) {
                        log.error("Failed to close error writer", ignored);
                    }
                }
            }
        }
    }

    // implement parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(startTime);
        parcel.writeInt(exitValue);
        parcel.writeString(stdOut);
        parcel.writeString(stdErr);
        parcel.writeLong(endTime);
    }

    @Override
    public String toString() {
        return "CommandResult{" +
                ", startTime=" + startTime +
                ", exitValue=" + exitValue +
                ", stdout='" + stdOut + "'" +
                ", stderr='" + stdErr + "'" +
                ", endTime=" + endTime +
                '}';
    }

    public static final Parcelable.Creator<CommandResult> CREATOR
            = new Parcelable.Creator<CommandResult>() {
        public CommandResult createFromParcel(Parcel in) {
            return new CommandResult(in);
        }

        public CommandResult[] newArray(int size) {
            return new CommandResult[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommandResult)) {
            return false;
        }

        CommandResult that = (CommandResult) o;

        return (startTime == that.startTime &&
                exitValue == that.exitValue &&
                stdOut.equals(that.stdOut) &&
                stdErr.equals(that.stdErr) &&
                endTime == that.endTime);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
        result = 31 * result + exitValue;
        result = 31 * result + (stdOut != null ? stdOut.hashCode() : 0);
        result = 31 * result + (stdErr != null ? stdErr.hashCode() : 0);
        result = 31 * result + (int) (endTime ^ (endTime >>> 32));
        return result;
    }
}

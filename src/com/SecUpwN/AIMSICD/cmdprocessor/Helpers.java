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

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static com.SecUpwN.AIMSICD.cmdprocessor.CMDProcessor.*;

public class Helpers {
    private static final String BUILD_PROP = "/system/build.prop";
    // don't show unavoidable warnings
    @SuppressWarnings({
            "UnusedDeclaration",
            "MethodWithMultipleReturnPoints",
            "ReturnOfNull",
            "NestedAssignment",
            "DynamicRegexReplaceableByCompiledPattern",
            "BreakStatement"
    })

    // avoids hardcoding the tag
    private static final String TAG = "AIMSICD_Helpers";

    public Helpers() {
        // dummy constructor
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }

        return sb.toString();
    }

    public static String getStringFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        fin.close();

        return ret;
    }

    public static Boolean hasBuildPropValue(String buildProp) throws Exception {
        Boolean has = false;
        String buildprop = "";
        buildprop = getStringFromFile(BUILD_PROP);
        if (buildprop.matches(buildProp + "=.+$"))
            has = true;
        return has;
    }

    /**
     * Checks device for SuperUser permission
     *
     * @return If SU was granted or denied
     */
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    public static boolean checkSu() {
        if (!new File("/system/bin/su").exists()
                && !new File("/system/xbin/su").exists()) {
            Log.e(TAG, "su binary does not exist!!!");
            return false; // tell caller to bail...
        }
        try {
            if (runSuCommand("ls /data/app-private").success()) {
                Log.i(TAG, " SU exists and we have permission");
                return true;
            } else {
                Log.i(TAG, " SU exists but we don't have permission");
                return false;
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointer throw while looking for su binary", e);
            return false;
        }
    }

    /**
     * Checks to see if Busybox is installed in "/system/"
     *
     * @return If busybox exists
     */
    public static boolean checkBusybox() {
        if (!new File("/system/bin/busybox").exists()
                && !new File("/system/xbin/busybox").exists()) {
            Log.e(TAG, "Busybox not in xbin or bin!");
            return false;
        }
        try {
            if (!runSuCommand("busybox mount").success()) {
                Log.e(TAG, "Busybox is there but it is borked! ");
                return false;
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "NullpointerException thrown while testing busybox", e);
            return false;
        }
        return true;
    }

    public static String[] getMounts(CharSequence path) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/mounts"), 256);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(path)) {
                    return line.split(" ");
                }
            }
        } catch (FileNotFoundException ignored) {
            Log.d(TAG, "/proc/mounts does not exist");
        } catch (IOException ignored) {
            Log.d(TAG, "Error reading /proc/mounts");
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ignored) {
                    // ignored
                }
            }
        }
        return null;
    }

    public static boolean getMount(String mount) {
        String[] mounts = getMounts("/system");
        if (mounts != null && mounts.length >= 3) {
            String device = mounts[0];
            String path = mounts[1];
            String point = mounts[2];
            String preferredMountCmd = "mount -o " + mount + ",remount -t " + point + ' ' + device + ' ' + path;
            if (runSuCommand(preferredMountCmd).success()) {
                return true;
            }
        }
        String fallbackMountCmd = "busybox mount -o remount," + mount + " /system";
        return runSuCommand(fallbackMountCmd).success();
    }

    public static String readOneLine(String fname) {
        BufferedReader br = null;
        String line = null;
        try {
            br = new BufferedReader(new FileReader(fname), 1024);
            line = br.readLine();
        } catch (FileNotFoundException ignored) {
            Log.d(TAG, "File was not found! trying via shell...");
            return readFileViaShell(fname, true);
        } catch (IOException e) {
            Log.d(TAG, "IOException while reading system file", e);
            return readFileViaShell(fname, true);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                    // failed to close reader
                }
            }
        }
        return line;
    }

    public static String readFileViaShell(String filePath, boolean useSu) {
        String command = "cat " + filePath;
        return useSu ? runSuCommand(command).getStdout()
                : runShellCommand(command).getStdout();
    }

    public static boolean writeOneLine(String filename, String value) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filename);
            fileWriter.write(value);
        } catch (IOException e) {
            String Error = "Error writing { " + value + " } to file: " + filename;
            Log.e(TAG, Error, e);
            return false;
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException ignored) {
                    // failed to close writer
                }
            }
        }
        return true;
    }

    public static String[] getAvailableIOSchedulers() {
        String[] schedulers = null;
        String[] aux = readStringArray("/sys/block/mmcblk0/queue/scheduler");
        if (aux != null) {
            schedulers = new String[aux.length];
            for (int i = 0; i < aux.length; i++) {
                schedulers[i] = aux[i].charAt(0) == '['
                        ? aux[i].substring(1, aux[i].length() - 1)
                        : aux[i];
            }
        }
        return schedulers;
    }

    private static String[] readStringArray(String fname) {
        String line = readOneLine(fname);
        if (line != null) {
            return line.split(" ");
        }
        return null;
    }

    public static String getIOScheduler() {
        String scheduler = null;
        String[] schedulers = readStringArray("/sys/block/mmcblk0/queue/scheduler");
        if (schedulers != null) {
            for (String s : schedulers) {
                if (s.charAt(0) == '[') {
                    scheduler = s.substring(1, s.length() - 1);
                    break;
                }
            }
        }
        return scheduler;
    }

    /**
     * Long toast message
     *
     * @param context Application Context
     * @param msg     Message to send
     */
    public static void msgLong(Context context, String msg) {
        if (context != null && msg != null) {
            Toast.makeText(context, msg.trim(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Short toast message
     *
     * @param context Application Context
     * @param msg     Message to send
     */
    public static void msgShort(Context context, String msg) {
        if (context != null && msg != null) {
            Toast.makeText(context, msg.trim(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Long toast message
     *
     * @param context Application Context
     * @param msg     Message to send
     */
    public static void sendMsg(Context context, String msg) {
        if (context != null && msg != null) {
            msgLong(context, msg);
        }
    }

    /**
     * Return a timestamp
     *
     * @param context Application Context
     */
    @SuppressWarnings("UnnecessaryFullyQualifiedName")
    public static String getTimestamp(Context context) {
        String timestamp = "unknown";
        Date now = new Date();
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        java.text.DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        timestamp = dateFormat.format(now) + ' ' + timeFormat.format(now);
        return timestamp;
    }

    public static boolean isPackageInstalled(String packageName, PackageManager pm) {
        try {
            String mVersion = pm.getPackageInfo(packageName, 0).versionName;
            if (mVersion == null) {
                return false;
            }
        } catch (PackageManager.NameNotFoundException notFound) {
            Log.e(TAG, "Package could not be found!", notFound);
            return false;
        }
        return true;
    }

    public static void restartSystemUI() {
        startSuCommand("pkill -TERM -f com.android.systemui");
    }

    public static void setSystemProp(String prop, String val) {
        startSuCommand("setprop " + prop + " " + val);
    }

    public static String getSystemProp(Context context, String prop, String def) {
        String result = null;
        try {
            result = SystemPropertiesReflection.get(context, prop);
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Failed to get prop: " + prop);
        }
        return result == null ? def : result;
    }

    // If the value is empty or null, fallback to the second property if there's one
    public static String getPropWithFallback(Context context, String[] props, String def) {
        String value = "";
        for (String prop : props) {
            value = Helpers.getSystemProp(context, prop, def);
            if (!value.equals("")) {
                return value;
            }
        }
        return "";
    }

    public static String toMHz(String mhzString) {
        return new StringBuilder().append(Integer.valueOf(mhzString) / 1000)
                .append(" MHz").toString();
    }

    public static int getNumOfCpus() {
        int numOfCpu = 1;
        String numOfCpus = Helpers.readOneLine("/sys/devices/system/cpu/present");
        String[] cpuCount = numOfCpus.split("-");
        if (cpuCount.length > 1) {
            try {
                int cpuStart = Integer.parseInt(cpuCount[0]);
                int cpuEnd = Integer.parseInt(cpuCount[1]);

                numOfCpu = cpuEnd - cpuStart + 1;

                if (numOfCpu < 0)
                    numOfCpu = 1;
            } catch (NumberFormatException ex) {
                numOfCpu = 1;
            }
        }
        return numOfCpu;
    }

    public static ArrayList<String> getTcpAlgorithms() {
        String result = String.valueOf(runShellCommand("cat /proc/sys/net/ipv4/tcp_available_congestion_control").getStdout());
        Log.e("TCP", result);
        String[] algorithms = result.split("\\s");
        ArrayList<String> holder = new ArrayList<String>();
        Collections.addAll(holder, algorithms);
        return holder;
    }

    public static void setPmrProp(String prop, Boolean isOn) {
        if (isOn)
            runSuCommand(Shell.ECHO + "\"" + prop + "=true" + "\" >> " + Shell.BUILD_PROP);
        else
            runSuCommand(Shell.ECHO + "\"" + prop + "=false" + "\" >> " + Shell.BUILD_PROP);
    }

    public static String getProp(String prop) {
        return CMDProcessor.runSuCommand("getprop " + prop).getStdout();
    }

    public static void setpropBoolean(String prop, Boolean isOn) {
        if (isOn)
            runSuCommand("setprop " + prop + " true");
        else
            runSuCommand("setprop " + prop + " false");
    }

    public static void applyBuildPropTweak(String buildProp, String propValue) {
        final String prop = buildProp;
        final String value = propValue;
        new Thread(new Runnable() {

            @Override
            public void run() {
                CMDProcessor.runSuCommand(Shell.MOUNT_SYSTEM_RW);
                CMDProcessor.runSuCommand(Shell.SED + prop + "/d\" " + Shell.BUILD_PROP);
                CMDProcessor.runSuCommand(Shell.ECHO + "\"" + prop + "=" + value + "\" >> " + Shell.BUILD_PROP);
                CMDProcessor.runSuCommand("setprop " + prop + " " + value);
                CMDProcessor.runSuCommand(Shell.MOUNT_SYSTEM_RO);
            }
        }).start();

    }
}


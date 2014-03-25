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

public class Shell {

    // Mount /system as Read-Write or Read-Only
    public static final String MOUNT_SYSTEM_RW = "busybox mount -o rw,remount -t auto /system";
    public static final String MOUNT_SYSTEM_RO = "busybox mount -o ro,remount -t auto /system";
    public static final String ECHO = "busybox echo ";
    public static final String BUILD_PROP = "/system/build.prop";
    public static final String SED = "busybox sed -i /\"";

    public static void su(String command) {
        CMDProcessor.runSuCommand(command);
    }

    public static void sh(String command) {
        CMDProcessor.runShellCommand(command);
    }
}

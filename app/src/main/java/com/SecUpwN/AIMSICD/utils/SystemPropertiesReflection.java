/**   Copyright (C) 2013  Louis Teboul <louisteboul@gmail.com>
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
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 **/

package com.SecUpwN.AIMSICD.utils;

import android.content.Context;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexFile;

/**
 *  Description:    Class using reflection to grant access to the private hidden
 *                  android.os.SystemProperties class
 *
 *  Dependency:     SystemProperties.java
 *
 *  Usage:          Helpers.java
 *
 *  Issues:
 *              [ ]  Can simplify the code according to below link
 *
 *  Notes:
 *
 *    https://github.com/android/platform_frameworks_base/blob/master/core/java/android/os/SystemProperties.java
 *    http://stackoverflow.com/a/28402378/1147688
 */
public class SystemPropertiesReflection {

    /**
     * This class cannot be instantiated
     */
    private SystemPropertiesReflection() {

    }

    /**
     * Get the value for the given key.
     *
     * @return an empty string if the key isn't found
     * @throws IllegalArgumentException if the key exceeds 32 characters
     */
    public static String get(Context context, String key) throws IllegalArgumentException {

        String ret = "";

        try {
            ClassLoader cl = context.getClassLoader();
            Class<?> SystemProperties = cl.loadClass("android.os.SystemProperties");

            //Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[1];
            paramTypes[0] = String.class;

            Method get = SystemProperties.getMethod("get", paramTypes);

            //Parameters
            Object[] params = new Object[1];
            params[0] = key;

            ret = (String) get.invoke(SystemProperties, params);

        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            ret = "";
        }

        return ret;

    }

    /**
     * Get the value for the given key.
     *
     * @return if the key isn't found, return def if it isn't null, or an empty string otherwise
     * @throws IllegalArgumentException if the key exceeds 32 characters
     */
    public static String get(Context context, String key, String def)
            throws IllegalArgumentException {

        String ret = def;

        try {

            ClassLoader cl = context.getClassLoader();
            Class<?> SystemProperties = cl.loadClass("android.os.SystemProperties");

            //Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = String.class;

            Method get = SystemProperties.getMethod("get", paramTypes);

            //Parameters
            Object[] params = new Object[2];
            params[0] = key;
            params[1] = def;

            ret = (String) get.invoke(SystemProperties, params);

        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            ret = def;
        }

        return ret;

    }

    /**
     * Get the value for the given key, and return as an integer.
     *
     * @param key the key to lookup
     * @param def a default value to return
     * @return the key parsed as an integer, or def if the key isn't found or cannot be parsed
     * @throws IllegalArgumentException if the key exceeds 32 characters
     */
    public static Integer getInt(Context context, String key, int def)
            throws IllegalArgumentException {

        Integer ret = def;

        try {

            ClassLoader cl = context.getClassLoader();
            Class<?> SystemProperties = cl.loadClass("android.os.SystemProperties");

            //Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = int.class;

            Method getInt = SystemProperties.getMethod("getInt", paramTypes);

            //Parameters
            Object[] params = new Object[2];
            params[0] = key;
            params[1] = def;

            ret = (Integer) getInt.invoke(SystemProperties, params);

        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            ret = def;
        }

        return ret;

    }

    /**
     * Get the value for the given key, and return as a long.
     *
     * @param key the key to lookup
     * @param def a default value to return
     * @return the key parsed as a long, or def if the key isn't found or
     * cannot be parsed
     * @throws IllegalArgumentException if the key exceeds 32 characters
     */
    public static Long getLong(Context context, String key, long def)
            throws IllegalArgumentException {

        Long ret = def;

        try {

            ClassLoader cl = context.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class SystemProperties = cl.loadClass("android.os.SystemProperties");

            //Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = long.class;

            Method getLong = SystemProperties.getMethod("getLong", paramTypes);

            //Parameters
            Object[] params = new Object[2];
            params[0] = key;
            params[1] = def;

            ret = (Long) getLong.invoke(SystemProperties, params);

        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            ret = def;
        }

        return ret;

    }

    /**
     * Get the value for the given key, returned as a boolean.
     * Values 'n', 'no', '0', 'false' or 'off' are considered false.
     * Values 'y', 'yes', '1', 'true' or 'on' are considered true.
     * (case insensitive).
     * If the key does not exist, or has any other value, then the default
     * result is returned.
     *
     * @param key the key to lookup
     * @param def a default value to return
     * @return the key parsed as a boolean, or def if the key isn't found or is
     * not able to be parsed as a boolean.
     * @throws IllegalArgumentException if the key exceeds 32 characters
     */
    public static Boolean getBoolean(Context context, String key, boolean def)
            throws IllegalArgumentException {

        Boolean ret = def;

        try {

            ClassLoader cl = context.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class SystemProperties = cl.loadClass("android.os.SystemProperties");

            //Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = boolean.class;

            Method getBoolean = SystemProperties.getMethod("getBoolean", paramTypes);

            //Parameters
            Object[] params = new Object[2];
            params[0] = key;
            params[1] = def;

            ret = (Boolean) getBoolean.invoke(SystemProperties, params);

        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            ret = def;
        }

        return ret;

    }

    /**
     * Set the value for the given key.
     *
     * @throws IllegalArgumentException if the key exceeds 32 characters
     *                          OR      if the value exceeds 92 characters
     */
    public static void set(Context context, String key, String val)
            throws IllegalArgumentException {

        try {

            @SuppressWarnings("unused")
            DexFile df = new DexFile(new File("/system/app/Settings.apk"));
            @SuppressWarnings("unused")
            ClassLoader cl = context.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class SystemProperties = Class.forName("android.os.SystemProperties");

            //Parameters Types
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = String.class;

            Method set = SystemProperties.getMethod("set", paramTypes);

            //Parameters
            Object[] params = new Object[2];
            params[0] = key;
            params[1] = val;

            set.invoke(SystemProperties, params);

        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception ignored) {
        }

    }
}
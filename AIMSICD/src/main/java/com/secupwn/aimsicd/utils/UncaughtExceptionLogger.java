package com.secupwn.aimsicd.utils;

import android.os.Build;
import android.os.Environment;
import android.support.annotation.UiThread;

import com.secupwn.aimsicd.BuildConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.freefair.android.util.function.Optional;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;

import static java.text.DateFormat.LONG;
import static java.util.Locale.ENGLISH;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class UncaughtExceptionLogger implements Thread.UncaughtExceptionHandler, Runnable {

    private final Thread.UncaughtExceptionHandler originalExceptionHandler;

    public static Optional<File> getDir() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Optional.empty();
        }

        File dir = new File(Environment.getExternalStorageDirectory(), "AIMSICD");

        if (dir.mkdirs() || dir.isDirectory()) {
            return Optional.of(dir);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        processException(thread, ex);

        if (originalExceptionHandler != null) {
            originalExceptionHandler.uncaughtException(thread, ex);
        }
    }

    private void processException(Thread thread, Throwable ex) {
        Optional<File> optional = getDir();

        if (optional.isPresent()) {
            Date date = new Date();
            File file = new File(getDir().get(), "error-" + date.getTime() + "-" + BuildConfig.VERSION_CODE + ".log");

            try {
                @Cleanup PrintWriter printWriter = new PrintWriter(file);

                printWriter.println("System Information:");
                printClass(printWriter, Build.VERSION.class);
                printWriter.println();
                printWriter.println();

                printWriter.println("App Information:");
                printClass(printWriter, BuildConfig.class);
                printWriter.println();
                printWriter.println();

                printWriter.println("Crash Information");
                printWriter.print("Timestamp: ");
                printWriter.println(DateFormat.getDateTimeInstance(LONG, LONG, ENGLISH).format(date));
                printWriter.print("Thread: ");
                printWriter.println(thread.toString());
                printWriter.println("Stacktrace:");
                ex.printStackTrace(printWriter);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Prints all public static fields of the given Class in the given PrintWriter
     */
    private static void printClass(PrintWriter printWriter, Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
                field.setAccessible(true);
                printWriter.print(field.getName());
                printWriter.print(": ");
                try {
                    if (field.getType().isArray()) {
                        printWriter.println(Arrays.toString((Object[]) field.get(null)));
                    } else {
                        printWriter.println(field.get(null));
                    }
                } catch (IllegalAccessException e) {
                    printWriter.println("IllegalAccessException");
                }
            }
        }

    }

    @UiThread
    public static void init() {
        Thread.UncaughtExceptionHandler originalExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        UncaughtExceptionLogger logger = new UncaughtExceptionLogger(originalExceptionHandler);
        Thread.setDefaultUncaughtExceptionHandler(logger);

        new Thread(logger).start();
    }

    static Pattern fileNamePattern = Pattern.compile("error-(\\d*)-(\\d*).log");

    /**
     * Delete all logs from older app versions
     */
    @Override
    public void run() {
        Optional<File> dir = getDir();

        if (dir.isPresent()) {
            File[] files = dir.get().listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    Matcher matcher = fileNamePattern.matcher(filename);
                    return matcher.matches() && Integer.valueOf(matcher.group(2)) < BuildConfig.VERSION_CODE;
                }
            });

            for (File file : files) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
    }
}

package com.secupwn.aimsicd.utils;

import android.support.annotation.NonNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * GZIPInputStream that works correctly with files more than 2 GiB
 * <p/>
 * <p><b>Rationale</b>
 * <p>Android Java implementation of {@link GZIPInputStream} contains verifyCrc method that
 * incorrectly checks for gzip size on EOF. It happens when the uncompressed
 * file is greater than 2 GiB and {@link java.util.zip.Inflater#getTotalOut()} returns
 * a negative number since the return type is int and {@code Integer.MAX_VALUE} is about 2GiB.
 * So it throws {@code IOException("Size mismatch")}
 * <p/>
 * <p>Oracle already fixed this bug in Java 6 (see https://bugs.openjdk.java.net/browse/JDK-5092263)
 * but Android implementation is still affected.
 * <p/>
 * <p><b>Workaround</b>
 * <p>If read throws a specific exception in {@code InputStream#read} we are sure that it is an EOF
 * and we can consume this exception and just return EOF. There could be a problem with a possible
 * loss of last buffer but it is better than a loss of full stream.
 */
public class FixedGZIPInputStream extends FilterInputStream {
    public FixedGZIPInputStream(GZIPInputStream in) {
        super(in);
    }

    @Override
    public int read(@NonNull byte[] buffer) throws IOException {
        try {
            return super.read(buffer);
        } catch (IOException e) {
            if (e.getMessage().equals("Size mismatch")) {
                return -1;
            }
            throw e;
        }
    }

    @Override
    public int read(@NonNull byte[] buffer, int byteOffset, int byteCount) throws IOException {
        try {
            return super.read(buffer, byteOffset, byteCount);
        } catch (IOException e) {
            if (e.getMessage().equals("Size mismatch")) {
                return -1;
            }
            throw e;
        }
    }
}

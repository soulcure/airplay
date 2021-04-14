package com.swaiotos.skymirror.sdk.util;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtil {

    private static final String TAG = "MirClientService";

    public static byte[] compress(byte[] dst) {
        int startSize = dst.length;
        Log.d(TAG, "gzip compress start length---" + startSize);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(dst);
            gzip.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "gzip compress error---" + e.getMessage());
        }
        byte[] res = out.toByteArray();

        int endSize = res.length;
        Log.d(TAG, "gzip compress end length---" + endSize);

        float rate = endSize * 1.0f / startSize;
        Log.e(TAG, "gzip compress rate---" + rate + "  compress:" + (startSize - endSize) + " bit ");

        return res;
    }


    public static ByteBuffer compress(ByteBuffer buffer) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(buffer.remaining());
        byteBuffer.put(buffer);
        byteBuffer.flip();

        byte[] bytes = byteBuffer.array();
        byte[] temp = compress(bytes);
        return ByteBuffer.wrap(temp);
    }


    public static byte[] uncompress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        Log.d(TAG, "gzip uncompress start length---" + bytes.length);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream unGzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = unGzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "gzip uncompress error---" + e.getMessage());
        }

        byte[] res = out.toByteArray();
        Log.d(TAG, "gzip uncompress end length---" + res.length);
        return res;
    }


    public static ByteBuffer uncompress(ByteBuffer dst) {
        return ByteBuffer.wrap(uncompress(dst.array()));
    }

}

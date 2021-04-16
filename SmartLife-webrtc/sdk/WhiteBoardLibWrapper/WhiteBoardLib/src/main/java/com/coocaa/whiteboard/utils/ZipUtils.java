package com.coocaa.whiteboard.utils;


import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 字符串的压缩与解压
 * Deflater 是同时使用了LZ77算法与哈夫曼编码的一个无损数据压缩算法
 */
public class ZipUtils {

    /**
     * 压缩
     */
    public static String zipString(String unzipString) {
        String dest = null;
        if (TextUtils.isEmpty(unzipString)){
            return dest;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gout = null;
        try {
            gout = new GZIPOutputStream(bos);
            gout.write(unzipString.getBytes("utf-8"));
            gout.close();
            byte[] buffer = bos.toByteArray();
            dest = Base64.encodeToString(buffer,Base64.CRLF);
        } catch (IOException e) {
            e.printStackTrace();
            dest = null;
        }finally {
            try {
                if (null != bos){
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return dest;
    }

    /**
     * 解压缩
     */
    public static String unzipString(String zipString) {
        String dest = null;
        if (TextUtils.isEmpty(zipString)){
            return dest;
        }
        ByteArrayOutputStream bout = null;
        ByteArrayInputStream bin = null;
        GZIPInputStream zin = null;

        try {
            byte[] compressed = Base64.decode(zipString,Base64.CRLF);
            bout = new ByteArrayOutputStream();
            bin = new ByteArrayInputStream(compressed);
            zin = new GZIPInputStream(bin);
            byte[] buffer = new byte[1024];
            int len  =-1;
            while ((len = zin.read(buffer))!=-1){
                bout.write(buffer,0,len);
            }
            bout.flush();
            dest = bout.toString();
        } catch (IOException e) {
            e.printStackTrace();
            dest = null;
        }finally {
            try {
                if (null != zin){
                    zin.close();
                }
                if (null != bin){
                    bin.close();
                }
                if (null != bout){
                    bout.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return dest;
    }
}

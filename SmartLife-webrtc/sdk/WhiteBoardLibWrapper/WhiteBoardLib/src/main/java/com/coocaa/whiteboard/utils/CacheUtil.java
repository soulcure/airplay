package com.coocaa.whiteboard.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheUtil {
    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
    public static final ExecutorService pool = Executors.newCachedThreadPool(new ThreadFactory() {
        AtomicInteger count = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "save-cache-" + count.getAndIncrement());
        }
    });

    public static boolean checkPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    //TODO 先忽略权限申请
    public static void saveCache(Context ctx, final String data) {
        if (data == null) return;
        if (checkPermission() && ctx.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //Todo
            Log.e("AAA", "permission denied");
            return;
        }
        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File flie = new File(storePath);
        if (!flie.exists()) {
            flie.mkdirs();
        }
        final File saveFile = new File(flie, "cc_white_cache.dat");
        pool.execute(new Runnable() {
            @Override
            public void run() {
                String svg = ZipUtils.zipString(data);
                JSONObject jobj = new JSONObject();
                jobj.put("svg", svg);
                jobj.put("timestamp", simpleDateFormat.format(new Date()));
                String saveData = jobj.toJSONString();
                FileOutputStream fos = null;
                try {
                    byte[] mBytes = saveData.getBytes();
                    int total = mBytes.length;
                    fos = new FileOutputStream(saveFile, false);
                    int start = 0;
                    while (start < total) {
                        fos.write(mBytes, start, 4096);
                        start += 4096;
                    }
                    int diff = start - total;
                    if (diff > 0) {
                        fos.write(mBytes, start, total - diff);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public static String restoreCache(Context ctx) {
        if (checkPermission() && ctx.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //Todo
            Log.e("AAA", "permission denied");
            return null;
        }

        final File flie = new File(Environment.getExternalStorageDirectory(), "cc_white_cache.dat");
        if (!flie.exists()) {
            return null;
        }

        FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(flie));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                try {
                    JSONObject jobj = JSONObject.parseObject(sb.toString());
                    String mTimestamp = (String) jobj.get("timestamp");
                    return ZipUtils.unzipString((String) jobj.get("svg"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return "";
            }
        });
        pool.execute(task);
        try {
            String svgData = task.get();
            return svgData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {
        String saveData = "AAABBBbCCCCC";
        File saveFile = new File("", "AAA.dat");
        FileOutputStream fos = null;
        try {
            if (!saveFile.exists())
                saveFile.createNewFile();
            System.out.println("path = " + saveFile.getAbsolutePath());
            byte[] mBytes = saveData.getBytes();
            int total = mBytes.length;
            fos = new FileOutputStream(saveFile, false);
            int start = 0;
            while (start < total) {
                fos.write(mBytes, start, 4);
                start += 4;
            }
            int diff = start - total;
            System.out.println("data =" + diff);
            if (diff > 0) {
                fos.write(mBytes, start, total - diff);
            }
        } catch (Exception e) {
        }
    }
}

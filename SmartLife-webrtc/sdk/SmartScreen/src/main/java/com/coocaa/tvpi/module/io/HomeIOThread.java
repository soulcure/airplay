package com.coocaa.tvpi.module.io;

import android.os.Handler;
import android.os.Looper;

/**
 * @Author: yuzhan
 */
public class HomeIOThread {

    public static final Looper IO_LOOPER = HomeExecutors.ioLooper();
    private static final Handler ioHandler = new Handler(IO_LOOPER);

    public static void execute(Runnable r){
        HomeExecutors.ioExecutor().execute(r);
    }

    public static void execute(long delay, final Runnable r){
        ioHandler.postDelayed(r, delay);
    }

    public static void executeInSingleThread(Runnable r) {
        ioHandler.post(r);
    }

    public static void executeRemoveableTask(Runnable r) {
        ioHandler.post(r);
    }

    public static void executeRemoveableTask(long delay, Runnable r) {
        ioHandler.postDelayed(r, delay);
    }

    public static void removeTask(Runnable r) {
        ioHandler.removeCallbacks(r);
    }
}

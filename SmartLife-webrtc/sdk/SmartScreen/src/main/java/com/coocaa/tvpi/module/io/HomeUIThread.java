package com.coocaa.tvpi.module.io;

import android.os.Handler;
import android.os.Looper;

/**
 * @Author: yuzhan
 */
public class HomeUIThread {
    private static Handler uiHandler = new Handler(Looper.getMainLooper());

    public static void execute(final Runnable r) {
        uiHandler.post(r);
    }

    public static void execute(long delay, final Runnable r) {
        uiHandler.postDelayed(r, delay);
    }

    public static void removeTask(final Runnable r) {
        uiHandler.removeCallbacks(r);
    }
}

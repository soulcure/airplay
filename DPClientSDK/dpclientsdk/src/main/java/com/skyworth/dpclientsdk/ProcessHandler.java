package com.skyworth.dpclientsdk;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;


public class ProcessHandler extends HandlerThread {

    private Handler mHandler;

    public ProcessHandler(String name) {
        this(name, false);
    }

    public ProcessHandler(String name, boolean start) {
        super(name);
        if (start) {
            start();
        }
    }

    @Override
    protected void onLooperPrepared() {
        synchronized (this) {
            mHandler = new Handler(getLooper()) {
                @Override
                public void dispatchMessage(Message msg) {
                    try {
                        super.dispatchMessage(msg);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            };
            notifyAll();
        }
    }

    public final void post(Runnable runnable) {
        postDelay(runnable, 0);
    }

    public final void postDelay(Runnable runnable, long delay) {
        wait4init();
        mHandler.postDelayed(runnable, delay);
    }

    public final void removeCallbacks(Runnable runnable) {
        wait4init();
        mHandler.removeCallbacks(runnable);
    }

    public final void removeCallbacks(Runnable r, Object token) {
        wait4init();
        mHandler.removeCallbacks(r, token);
    }

    private void wait4init() {
        synchronized (this) {
            if (mHandler == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

package com.coocaa.tvpi.module.io;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: yuzhan
 */
public class HomeExecutors {

    protected static HomeExecutors INSTANCE = new HomeExecutors();

    private final Executor uiThread;
    private final Handler mainHandler;
    private final HandlerThread handlerThread;
    private final ExecutorService newthread;//线程池

    private HomeExecutors() {
        uiThread = new UIThreadExecutor();
        handlerThread = new HandlerThread("tvpi-io-task");
        handlerThread.start();
        mainHandler = new Handler(Looper.getMainLooper());
        newthread = Executors.newCachedThreadPool(new IoThreadFactory("tvpi-io-"));
    }

    public static Looper ioLooper(){
        return INSTANCE.handlerThread.getLooper();
    }

    public static ExecutorService ioExecutor() {
        return INSTANCE.newthread;
    }

    private static class UIThreadExecutor implements Executor {
        @Override
        public void execute(Runnable command) {
            INSTANCE.mainHandler.post(command);
        }
    }

    private static class IoThreadFactory implements ThreadFactory {
        private String mPrefix = "";
        private final AtomicInteger mThreadIndex;
        public IoThreadFactory(String prefix){
            this.mPrefix = prefix;
            this.mThreadIndex = new AtomicInteger(1);
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName(mPrefix + mThreadIndex.getAndIncrement());
            return t;
        }
    }
}

package com.coocaa.smartscreen.repository.http;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: yuzhan
 */
public class HttpExecutors {

    protected static HttpExecutors INSTANCE = new HttpExecutors();

    private final ExecutorService newthread;//线程池

    private HttpExecutors() {
        newthread = Executors.newCachedThreadPool(new IoThreadFactory("tvpi-http-"));
    }

    public static void execute(Runnable r) {
        INSTANCE.newthread.execute(r);
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

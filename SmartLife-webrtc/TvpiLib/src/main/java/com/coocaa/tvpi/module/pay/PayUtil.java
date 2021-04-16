package com.coocaa.tvpi.module.pay;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class PayUtil {
    public static  final  ExecutorService mService = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"ali-pay");
        }
    });

    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException("reference is null");
        }
        return reference;
    }

}

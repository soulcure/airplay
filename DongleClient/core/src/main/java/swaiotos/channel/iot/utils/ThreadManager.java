package swaiotos.channel.iot.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Description: 线程工具类
 * Create by wzh on 2019-11-15
 */
public class ThreadManager {

    private final static String TAG = "ThreadManager";

    private volatile static ThreadManager instance;
    /**
     * 网络IO线程池
     **/
    private final ExecutorService ioThread;
    /**
     * UI线程
     **/
    private final Handler uiThread;
    private final Handler delayHandler;

    public static ThreadManager getInstance() {
        if (instance == null) {
            synchronized (ThreadManager.class) {
                if (instance == null) {
                    instance = new ThreadManager();
                }
            }
        }
        return instance;
    }

    private ThreadManager() {
        uiThread = new Handler(Looper.getMainLooper());
        HandlerThread ht = new HandlerThread(TAG);
        ht.start();

        delayHandler = new Handler(ht.getLooper());

        ioThread = new ThreadPoolExecutor(5, 20, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue(), new ThreadFactory() {
            int count = 0;

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "io_pool_thread_" + (count++));
                t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        e.printStackTrace();
                    }
                });
                return t;
            }
        });
    }

    public void ioThread(Runnable runnable) {
        ioThread.execute(runnable);
    }

    public void ioThread(final Runnable runnable, long delay) {
        delayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ioThread.execute(runnable);
            }
        }, delay);
    }

    public void uiThread(Runnable runnable) {
        uiThread.post(runnable);
    }

    public void uiThread(Runnable runnable, long delay) {
        uiThread.postDelayed(runnable, delay);
    }

    public void removeUiThread(Runnable runnable) {
        uiThread.removeCallbacks(runnable);
    }

    public void checkMainThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("Do not call this on main thread!!!");
        }
    }

}

package swaiotos.runtime.base;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @Author: yuzhan
 */
public class AppletThread {

    private static HandlerThread thread = null;
    private static Handler ioHandler = null;
    private static Handler uiHandler = new Handler(Looper.getMainLooper());
    private static Executor executor = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "Applet-CIO-");
            return t;
        }
    });

    static {
        thread = new HandlerThread("Applet-IO");
        thread.start();
        ioHandler = new Handler(thread.getLooper());
    }


    public static void IO(Runnable r) {
        ioHandler.post(r);
    }

    public static void removeIO(Runnable r) {
        ioHandler.removeCallbacks(r);
    }

    public static void execute(Runnable r) {
        executor.execute(r);
    }

    public static void UI(Runnable r) {
        uiHandler.post(r);
    }

    public static void removeUI(Runnable r) {
        uiHandler.removeCallbacks(r);
    }
}

package com.coocaa.tvpi.module.connection.wifi;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;



public class WifiConnectTimeoutHandler {
    private static final String TAG = "WifiConnect";
    private final Handler handler;
    private final WifiConnectCallback wifiConnectionCallback;

    public WifiConnectTimeoutHandler(@NonNull Handler handler, @NonNull final WifiConnectCallback wifiConnectionCallback) {
        this.handler = handler;
        this.wifiConnectionCallback = wifiConnectionCallback;
    }

    private final Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run: timeoutRunnable");
            if (wifiConnectionCallback != null) {
                wifiConnectionCallback.onConnectFail(WifiConnectErrorCode.CONNECT_TIMEOUT);
            }
        }
    };

    public void startTimeout(final long timeout) {
        handler.removeCallbacks(timeoutRunnable);
        handler.postDelayed(timeoutRunnable, timeout);
    }

    public void stopTimeout() {
        handler.removeCallbacks(timeoutRunnable);
    }
}

package com.swaiot.webrtcd.data;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.io.File;


public class WebRTCManager {
    private enum BIND_STATUS {
        IDLE, BINDING, BIND
    }

    /**
     * SDK初始化结果监听器
     */
    public interface InitListener {
        void success();

        void fail();
    }


    public interface SenderImpl {
        void onSend(String content);
    }


    public interface WebRtcResult {
        void onResult(int code, String info);
    }


    @SuppressLint("StaticFieldLeak")
    private static WebRTCManager instance;

    private WebRTCManager() {
    }


    private CaptureServiceData.ReverseServiceBinder reverseService;

    private BIND_STATUS bind = BIND_STATUS.IDLE;

    private InitListener mInitListener;

    private Context mContext;

    /**
     * bind service callback
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof CaptureServiceData.ReverseServiceBinder) {
                reverseService = (CaptureServiceData.ReverseServiceBinder) service;
                bind = BIND_STATUS.BIND;
                if (mInitListener != null) {
                    mInitListener.success();
                }
            }
        }

        // 连接服务失败后，该方法被调用
        @Override
        public void onServiceDisconnected(ComponentName name) {
            reverseService = null;
            bind = BIND_STATUS.IDLE;
            if (mInitListener != null) {
                mInitListener.fail();
            }
        }
    };


    public static WebRTCManager instance() {
        if (instance == null) {
            synchronized (WebRTCManager.class) {
                if (instance == null) {
                    instance = new WebRTCManager();
                }
            }
        }
        return instance;
    }

    public void start() {
        reverseService.start();
    }

    public boolean isStart() {
        return reverseService.isStart();
    }


    public void stop() {
        reverseService.stop();
    }

    public void sendMessage(String data) {
        reverseService.sendMessage(data);
    }

    public void sendFile(File file) {
        reverseService.sendFile(file);
    }


    public void setSender(WebRTCManager.SenderImpl sender) {
        reverseService.setSender(sender);
    }

    public void setResult(WebRTCManager.WebRtcResult result) {
        reverseService.setResult(result);
    }


    public void init(Context context) {
        init(context, null);
    }

    public void init(Context context, InitListener listener) {
        mContext = context.getApplicationContext();
        mInitListener = listener;

        if (bind == BIND_STATUS.BIND) {
            if (mInitListener != null) {
                mInitListener.success();
            }
        } else if (bind == BIND_STATUS.BINDING) {
            //do nothing
        } else {
            bind = BIND_STATUS.BINDING;
            Intent intent = new Intent(context.getApplicationContext(), CaptureServiceData.class);
            context.getApplicationContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

    }

    public boolean isBind() {
        return bind == BIND_STATUS.BIND;
    }


    public void destroy() {
        if (mContext != null && bind == BIND_STATUS.BIND) {
            bind = BIND_STATUS.IDLE;
            mContext.getApplicationContext().unbindService(serviceConnection);
        }

    }


}

package com.swaiot.webrtcc.sound;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.swaiot.webrtcc.video.CaptureServiceVideo;


public class WebRTCSoundManager {
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
    private static WebRTCSoundManager instance;

    private WebRTCSoundManager() {
    }


    private CaptureServiceSound.ReverseServiceBinder reverseService;

    private BIND_STATUS bind = BIND_STATUS.IDLE;

    private InitListener mInitListener;

    private Context mContext;

    /**
     * bind service callback
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof CaptureServiceSound.ReverseServiceBinder) {
                reverseService = (CaptureServiceSound.ReverseServiceBinder) service;
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


    public static WebRTCSoundManager instance() {
        if (instance == null) {
            synchronized (WebRTCSoundManager.class) {
                if (instance == null) {
                    instance = new WebRTCSoundManager();
                }
            }
        }
        return instance;
    }

    public void start() {
        reverseService.start();
    }

    public void start(Intent intent) {
        reverseService.start(intent);
    }

    public boolean isStart() {
        return reverseService.isStart();
    }


    public void stop() {
        reverseService.stop();
    }


    public void setSender(WebRTCSoundManager.SenderImpl sender) {
        reverseService.setSender(sender);
    }

    public void setResult(WebRTCSoundManager.WebRtcResult result) {
        reverseService.setResult(result);
    }


    public void init(Context context) {
        init(context, null);
    }

    public void init(Context context, InitListener listener) {
        mContext = context;
        mInitListener = listener;

        if (bind == BIND_STATUS.BIND) {
            if (mInitListener != null) {
                mInitListener.success();
            }
        } else if (bind == BIND_STATUS.BINDING) {
            //do nothing
        } else {
            bind = BIND_STATUS.BINDING;
            Intent intent = new Intent(context.getApplicationContext(), CaptureServiceSound.class);
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

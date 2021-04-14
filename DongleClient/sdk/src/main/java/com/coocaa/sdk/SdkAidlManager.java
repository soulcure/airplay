package com.coocaa.sdk;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.coocaa.sdk.entity.IMMessage;
import com.coocaa.sdk.entity.Session;

import static com.coocaa.sdk.BinderPool.BIND_SEND_MSG;
import static com.coocaa.sdk.BinderPool.BIND_RECEIVE_MSG;
import static com.coocaa.sdk.BinderPool.BIND_DEVICE;


public class SdkAidlManager {

    private static final String TAG = "aidl";

    //custom app define this  //todo
    public static final String SOURCE_CLIENT = "ss-clientID-SmartScreen";

    private static final int HANDLER_AIDL = 1;

    private static volatile SdkAidlManager instance;

    private BinderPool binderPool;
    private Handler mHandler;
    private ProcessHandler mProcessHandler;

    /**
     * SDK初始化结果监听器
     */
    public interface InitListener {
        void success();

        void fail();
    }


    public static SdkAidlManager instance() {
        if (null == instance) {
            synchronized (BinderPool.class) {
                if (null == instance) {
                    instance = new SdkAidlManager();
                }
            }
        }
        return instance;
    }

    private SdkAidlManager() {
        initHandler();
    }


    /**
     * sdk初始化
     */
    public void init(Context context) {
        this.init(context, null);
    }


    /**
     * sdk初始化
     */
    public void init(Context context, InitListener listener) {
        Log.e("yao", "SdkAidlManager init pid=" + android.os.Process.myPid());
        if (binderPool == null) {
            binderPool = new BinderPool(context, mProcessHandler);
        }
        binderPool.bindPoolService(listener);
    }


    public Session getMySession() {
        if (binderPool.checkBind()) {
            IBinder binder = binderPool.queryBinder(BIND_SEND_MSG);//获取Binder后使用
            IIMChannel imChannel = IIMChannel.Stub.asInterface(binder);
            try {
                return imChannel.getMySession();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public Session getTargetSession() {
        if (binderPool.checkBind()) {
            IBinder binder = binderPool.queryBinder(BIND_SEND_MSG);//获取Binder后使用
            IIMChannel imChannel = IIMChannel.Stub.asInterface(binder);
            try {
                return imChannel.getTargetSession();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * 发送消息
     *
     * @param content 消息体
     */
    public void sendMessage(String content, String targetClient) {
        sendMessage(content, targetClient, null);
    }


    /**
     * 发送消息
     *
     * @param content 消息体
     */
    public void sendMessage(String content, String targetClient, IResultListener callback) {
        Session source = getMySession();
        Session target = getTargetSession();

        if (source == null || target == null) {
            if (callback != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            callback.onResult(-4, "发送或接收session为空");
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        String sourceClient = SOURCE_CLIENT;
        IMMessage imMessage = IMMessage.Builder.createTextMessage(source, target,
                sourceClient, targetClient, content);

        sendMessage(imMessage, callback);
    }


    /**
     * 发送消息
     *
     * @param msg 消息体
     */
    public void sendMessage(IMMessage msg) {
        sendMessage(msg, null);
    }


    /**
     * 发送消息
     *
     * @param msg      消息体
     * @param callback 回调
     */
    public void sendMessage(final IMMessage msg, final IResultListener callback) {
        binderPool.checkBind(() -> {
            IBinder binder = binderPool.queryBinder(BIND_SEND_MSG);//获取Binder后使用
            IIMChannel imChannel = IIMChannel.Stub.asInterface(binder);
            try {
                imChannel.send(msg, callback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    public void bindDevice(String bindCode, IBindListener callback) {
        binderPool.checkBind(() -> {
            IBinder binder = binderPool.queryBinder(BIND_DEVICE);//获取Binder后使用
            IDevice iDevice = IDevice.Stub.asInterface(binder);
            try {
                iDevice.bindDevice(bindCode, callback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }


    public Session connect(String sid, long timeout) {
        if (binderPool.checkBind()) {
            IBinder binder = binderPool.queryBinder(BIND_DEVICE);//获取Binder后使用
            IDevice iDevice = IDevice.Stub.asInterface(binder);
            try {
                return iDevice.connect(sid, timeout);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;

    }


    public void addNotifyListener(final String targetClient, final IReceiveMessage listener) {
        binderPool.checkBind(() -> {
            IBinder binder = binderPool.queryBinder(BIND_RECEIVE_MSG);//获取Binder后使用
            INotityCallBack iNotityCallBack = INotityCallBack.Stub.asInterface(binder);
            try {
                iNotityCallBack.registerCallback(targetClient, listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }


    public void removeNotifyListener(final String targetClient, final IReceiveMessage listener) {
        binderPool.checkBind(() -> {
            IBinder binder = binderPool.queryBinder(BIND_RECEIVE_MSG);//获取Binder后使用
            INotityCallBack iNotityCallBack = INotityCallBack.Stub.asInterface(binder);
            try {
                iNotityCallBack.unregisterCallback(targetClient, listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    public String getCurrentDevice() {
        if (binderPool.checkBind()) {
            IBinder binder = binderPool.queryBinder(BIND_DEVICE);//获取Binder后使用
            IDevice iDevice = IDevice.Stub.asInterface(binder);
            try {
                return iDevice.getCurrentDevice();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public void post(Runnable r) {
        mProcessHandler.post(r);
    }


    public void postDelayed(Runnable r, long delayMillis) {
        mProcessHandler.postDelayed(r, delayMillis);
    }


    /**
     * sdk销毁
     */
    public void destroy() {
        binderPool.destroy();
    }


    /**
     * 线程初始化
     */
    private void initHandler() {
        mHandler = new Handler(Looper.getMainLooper());
        if (mProcessHandler == null) {
            HandlerThread handlerThread = new HandlerThread(
                    "handler looper Thread");
            handlerThread.start();
            mProcessHandler = new ProcessHandler(handlerThread.getLooper());
        }
    }


    /**
     * 子线程handler,looper
     *
     * @author Administrator
     */
    private class ProcessHandler extends Handler {

        public ProcessHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_AIDL:
                    //do something
                    break;
                default:
                    break;
            }

        }

    }

}

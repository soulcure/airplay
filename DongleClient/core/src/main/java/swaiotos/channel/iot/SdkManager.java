package swaiotos.channel.iot;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.WorkerThread;

import com.coocaa.sdk.SdkAidlManager;
import com.coocaa.sdk.entity.IMMessage;
import com.coocaa.sdk.entity.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import swaiotos.channel.iot.callback.BindResult;
import swaiotos.channel.iot.callback.CheckListener;
import swaiotos.channel.iot.callback.ConnectCallback;
import swaiotos.channel.iot.callback.ConnectStatusListener;
import swaiotos.channel.iot.callback.DeviceCallback;
import swaiotos.channel.iot.callback.InitListener;
import swaiotos.channel.iot.callback.LoginCallback;
import swaiotos.channel.iot.callback.NotifyListener;
import swaiotos.channel.iot.callback.ResultListener;
import swaiotos.channel.iot.callback.UnBindResult;
import swaiotos.channel.iot.db.bean.Device;
import swaiotos.channel.iot.db.helper.DeviceHelper;
import swaiotos.channel.iot.db.manager.DaoManager;
import swaiotos.channel.iot.utils.AppUtils;
import swaiotos.channel.iot.utils.SameLan;

/**
 * sdk 接口类
 */
public class SdkManager {
    private static final String TAG = "yao";

    private static final int HANDLER_THREAD_INIT_CONFIG_START = 1;
    private static final int HANDLER_THREAD_AUTO_LOGIN = 2;

    private static final String ACTION = "com.coocaa.service.client.BIND";  //SkyAidlServer action

    //custom app define this //todo
    public static final String SOURCE_CLIENT = "ss-clientID-SmartScreen";

    @SuppressLint("StaticFieldLeak")
    private static SdkManager instance;


    private enum BIND_STATUS {
        IDLE, BINDING, BIND
    }

    private SkyServiceBinder skyService = null;
    private BIND_STATUS bind = BIND_STATUS.IDLE;

    private Context mContext;

    private final List<InitListener> mInitListenerList;

    private ProcessHandler mProcessHandler;
    private Handler mUIHandler;

    private CheckListener mCheckListener;


    /**
     * 私有构造函数
     */
    private SdkManager() {
        mInitListenerList = new ArrayList<>();
    }


    /**
     * 获取sdk单例索引
     *
     * @return
     */
    public static SdkManager instance() {
        if (instance == null) {
            instance = new SdkManager();
        }
        return instance;
    }


    /**
     * sdk初始化
     *
     * @param context
     */
    public void init(Context context) {
        this.init(context, null);
    }


    /**
     * sdk初始化
     *
     * @param context
     */
    public void init(final Context context, InitListener listener) {
        String processName = AppUtils.getProcessName(context, android.os.Process.myPid());
        if (processName != null) {
            boolean defaultProcess = processName.equals(context.getPackageName());
            if (!defaultProcess) {//非当前主进程
                SdkAidlManager.instance().init(context);
                return;
            }
        }

        Log.e("yao", "SdkManager init pid=" + android.os.Process.myPid());

        mContext = context.getApplicationContext();

        if (listener != null) {
            mInitListenerList.add(listener);
        }

        if (bind == BIND_STATUS.IDLE) {
            bind = BIND_STATUS.BINDING;
            initHandler();
            mProcessHandler.sendEmptyMessage(HANDLER_THREAD_INIT_CONFIG_START);
        } else if (bind == BIND_STATUS.BINDING) {
            //do nothing
        } else if (bind == BIND_STATUS.BIND) {
            for (InitListener item : mInitListenerList) {
                item.success();
            }
            mInitListenerList.clear();
        }
    }


    private void initWork(Context context) {
        Log.v(TAG, "SdkManager in init");

        Intent intent = new Intent(context.getApplicationContext(), SkyServer.class);
        intent.setAction(ACTION);
        context.getApplicationContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        DaoManager.instance(mContext).getDaoSession();

    }


    /**
     * sdk销毁
     *
     * @param
     */
    public void destroy() {
        if (mContext != null && bind == BIND_STATUS.BIND) {
            bind = BIND_STATUS.IDLE;
            mContext.getApplicationContext().unbindService(serviceConnection);
        }

    }


    /**
     * 判断SDK是否登录
     *
     * @return
     */
    public void loginSSE(String sid, String token, LoginCallback callback) {
        checkBind(() -> {
            skyService.loginSSE(sid, token, callback);
        });
    }


    public void connect(String sid, long timeout, final ConnectCallback callback) {

        if (TextUtils.isEmpty(sid)) {
            Log.e(TAG, "Connect Fail target Sid is null");
            if (callback != null) {
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onConnectFail(-1, "Connect Fail target Sid is null");
                    }
                });
            }
            return;
        }

        mProcessHandler.post(new Runnable() {
            @Override
            public void run() {
                if (checkBind()) {
                    try {
                        final Session session = skyService.connect(sid, timeout);
                        if (callback != null) {
                            mUIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess(session);
                                }
                            });
                        }

                    } catch (RuntimeException | TimeoutException e) {
                        final String msg = e.getMessage();
                        Log.e(TAG, "Connect Fail:" + msg);
                        if (callback != null) {
                            mUIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onConnectFail(-1, msg);
                                }
                            });
                        }
                    }
                }
            }
        });
    }


    public String fileServer(String path) {
        if (checkBind()) {
            return skyService.fileServer(path);
        }
        return null;
    }


    public void loginOut() {
        if (checkBind()) {
            skyService.loginOut();
        }
    }


    public List<Device> getBindDevice() {
        if (checkBind()) {
            return DeviceHelper.instance().toQueryDeviceList(mContext);
        }
        return null;
    }

    public void reqBindDevice(final DeviceCallback callback) {
        if (checkBind()) {
            skyService.reqBindDevice(callback);
        }
    }


    public void bindDevice(String bindCode, BindResult callback) {
        if (checkBind()) {
            skyService.bindDevice(bindCode, callback);
        }
    }

    public void refreshOnlineStatus(DeviceCallback callback) {
        if (checkBind()) {
            skyService.refreshOnlineStatus(callback);
        }
    }

    public void unbindDevice(String targetSid, int deleteType, UnBindResult callback) {
        if (checkBind()) {
            skyService.unbindDevice(targetSid, deleteType, callback);
        }
    }


    public Session getMySession() {
        if (checkBind()) {
            return skyService.getMySession();
        }
        return null;
    }

    public String getSid() {
        if (checkBind()) {
            return skyService.getSid();
        }
        return null;
    }


    public Session getTargetSession() {
        if (checkBind()) {
            return skyService.getTargetSession();
        }
        return null;
    }

    public String getAccessToken() {
        if (checkBind()) {
            return skyService.getAccessToken();
        }
        return null;
    }


    /**
     * 判断是否同在一个局域网
     *
     * @param ip
     * @return
     */
    @WorkerThread
    public boolean isSameLan(String ip) {
        return SameLan.isInSameLAN(ip);
    }


    /**
     * 判断SSE是否登录
     *
     * @return
     */
    public boolean isSSEConnected() {
        if (checkBind()) {
            return skyService.isSSEConnected();
        }
        return false;
    }


    /**
     * 判断LOCAL是否连接
     *
     * @return
     */
    public boolean isLocalConnect() {
        if (checkBind()) {
            return skyService.isLocalConnect();
        }
        return false;
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
    public void sendMessage(String content, String targetClient, ResultListener callback) {
        if (checkBind()) {
            Session source = getMySession();
            Session target = getTargetSession();
            String sourceClient = SOURCE_CLIENT;
            IMMessage imMessage = IMMessage.Builder.createTextMessage(source, target,
                    sourceClient, targetClient, content);

            skyService.sendMessage(imMessage, callback);
        }
    }


    /**
     * 发送消息
     *
     * @param msg 消息体
     */
    public void sendMessage(IMMessage msg) {
        if (checkBind()) {
            skyService.sendMessage(msg, null);
        }
    }


    /**
     * 发送消息
     *
     * @param msg      消息体
     * @param callback 回调
     */
    public void sendMessage(IMMessage msg, ResultListener callback) {
        if (checkBind()) {
            skyService.sendMessage(msg, callback);
        }
    }


    public void addNotifyListener(String targetClient, NotifyListener listener) {
        checkBind(() -> {
            skyService.addNotifyListener(targetClient, listener);
        });
    }

    public void removeNotifyListener(String targetClient) {
        checkBind(() -> {
            skyService.removeNotifyListener(targetClient);
        });
    }


    /**
     * 监听连接状态
     *
     * @param listener
     */
    public void setConnectListener(ConnectStatusListener listener) {
        checkBind(() -> {
            skyService.setConnectListener(listener);
        });
    }


    public void sendBroadCastByHttp(IMMessage message, ResultListener callback) {
        checkBind(() -> {
            skyService.sendBroadCastByHttp(message, callback);
        });
    }


    private boolean checkBind() {
        if (mContext == null) {
            throw new IllegalStateException("sdk no init");
        }

        if (bind != BIND_STATUS.BIND) {
            throw new IllegalStateException("sdk no bind");
        }

        return true;
    }


    private void checkBind(CheckListener listener) {
        if (mContext == null) {
            throw new IllegalStateException("sdk no init");
        }

        if (bind != BIND_STATUS.BIND) {
            mCheckListener = listener;
            init(mContext, new InitListener() {
                @Override
                public void success() {
                    Log.d(TAG, "bind server success!");
                }


                @Override
                public void fail() {
                    Log.e(TAG, "bind server fail!");
                }
            });
        } else {
            if (listener != null) {
                listener.success();
            }
        }

    }


    /**
     * bind service callback
     */
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof SkyServiceBinder) {
                skyService = (SkyServiceBinder) service;
                bind = BIND_STATUS.BIND;
                for (InitListener item : mInitListenerList) {
                    item.success();
                }
                mInitListenerList.clear();

                if (mCheckListener != null) {
                    mCheckListener.success();
                }

                Log.v(TAG, "Service Connected...");
            }
        }

        // 连接服务失败后，该方法被调用
        @Override
        public void onServiceDisconnected(ComponentName name) {
            skyService = null;
            bind = BIND_STATUS.IDLE;
            for (InitListener item : mInitListenerList) {
                item.fail();
            }
            mInitListenerList.clear();
            Log.e(TAG, "Service Failed...");
        }
    };


    /**
     * 线程初始化
     */
    private void initHandler() {
        if (mProcessHandler == null) {
            HandlerThread handlerThread = new HandlerThread(
                    "handler looper Thread");
            handlerThread.start();
            mProcessHandler = new ProcessHandler(handlerThread.getLooper());
        }

        if (mUIHandler == null) {
            mUIHandler = new Handler(Looper.getMainLooper());
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
                case HANDLER_THREAD_INIT_CONFIG_START:
                    initWork(mContext);
                    break;
                case HANDLER_THREAD_AUTO_LOGIN:
                    break;
                default:
                    break;
            }

        }

    }


}

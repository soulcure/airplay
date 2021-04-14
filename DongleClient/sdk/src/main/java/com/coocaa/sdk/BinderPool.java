package com.coocaa.sdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.coocaa.sdk.callback.CheckListener;

import java.util.ArrayList;
import java.util.List;


public class BinderPool {
    private static final String TAG = "yao";

    private static final String ACTION = "com.coocaa.service.client.AIDL.BIND";  //SkyAidlServer action
    private static final String PROXY_SERVICE_PACKAGE_NAME = "swaiotos.channel.iot.dongle"; //package name

    public static final int BIND_SEND_MSG = 1;
    public static final int BIND_RECEIVE_MSG = 2;
    public static final int BIND_DEVICE = 3;

    private final Handler mProcessHandler;


    private enum BIND_STATUS {
        IDLE, BINDING, BIND
    }


    private BIND_STATUS bind = BIND_STATUS.IDLE;

    private final Context mContext;
    private IBinderPool mBinderPool;

    private final List<SdkAidlManager.InitListener> mInitListenerList;


    private final IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {

        @Override
        public void binderDied() {
            if (mBinderPool == null)
                return;
            mBinderPool.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mBinderPool = null;

            bindService();
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinderPool = IBinderPool.Stub.asInterface(service);
            Log.d(TAG, "ServiceConnection onServiceConnected");

            try {
                //server端死亡 代理回调
                service.linkToDeath(mDeathRecipient, 0);

            } catch (RemoteException e) {
                e.printStackTrace();
            }

            bind = BIND_STATUS.BIND;

            if (mCheckListener != null) {
                mCheckListener.success();
                mCheckListener = null;
            }

            for (SdkAidlManager.InitListener item : mInitListenerList) {
                item.success();
            }
            mInitListenerList.clear();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "onServiceDisconnected = " + name);
            mBinderPool = null;
            bind = BIND_STATUS.IDLE;
            for (SdkAidlManager.InitListener item : mInitListenerList) {
                item.fail();
            }
            mInitListenerList.clear();
        }
    };


    public BinderPool(Context context, Handler handler) {
        mContext = context.getApplicationContext();
        mInitListenerList = new ArrayList<>();
        mProcessHandler = handler;
    }

    private CheckListener mCheckListener;

    public void checkBind(CheckListener listener) {
        if (mContext == null) {
            throw new IllegalStateException("sdk no init");
        }

        if (bind != BIND_STATUS.BIND) {
            mCheckListener = listener;
            bindPoolService(null);
        } else {
            if (listener != null) {
                listener.success();
            }
        }

    }

    public boolean checkBind() {
        if (mContext == null) {
            throw new IllegalStateException("sdk no init");
        }

        if (bind != BIND_STATUS.BIND) {
            throw new IllegalStateException("sdk no bind");
        }

        return true;
    }


    public synchronized void bindPoolService(SdkAidlManager.InitListener listener) {
        Log.e(TAG, "bindPoolService");

        if (listener != null) {
            mInitListenerList.add(listener);
        }

        if (bind == BIND_STATUS.IDLE) {
            bind = BIND_STATUS.BINDING;

            mProcessHandler.post(new Runnable() {
                @Override
                public void run() {
                    bindService();
                }
            });

        } else if (bind == BIND_STATUS.BINDING) {
            //do nothing
        } else if (bind == BIND_STATUS.BIND) {
            for (SdkAidlManager.InitListener item : mInitListenerList) {
                item.success();
            }
            mInitListenerList.clear();
        }
    }

    private void bindService() {
        Log.d(TAG, "bind Pool Service！");
        Intent intent = new Intent(ACTION);
        intent.setPackage(PROXY_SERVICE_PACKAGE_NAME);

        mContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    //获取Binder
    public IBinder queryBinder(int binderCode) {
        IBinder binder = null;
        try {
            if (mBinderPool != null) {
                binder = mBinderPool.queryBinder(binderCode);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "queryBinder exception:" + e.getMessage());
            e.printStackTrace();
            binder = null;
        }
        return binder;
    }


    /**
     * sdk销毁
     */
    public void destroy() {
        if (mContext != null) {
            if (bind == BIND_STATUS.BIND) {
                bind = BIND_STATUS.IDLE;
                mContext.unbindService(serviceConnection);
            }
        }
    }


}

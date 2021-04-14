package swaiotos.channel.iot.aidl;


import android.os.RemoteException;

import com.coocaa.sdk.IResultListener;
import com.coocaa.sdk.IIMChannel;
import com.coocaa.sdk.entity.IMMessage;
import com.coocaa.sdk.entity.Session;

import swaiotos.channel.iot.SkyServer;
import swaiotos.channel.iot.callback.ResultListener;

public class IMChannelImpl extends IIMChannel.Stub {//Stub内部类，其实就是一个Binder类

    private static final String TAG = "aidl";

    private SkyServer mServer;

    IMChannelImpl(SkyServer server) {
        super();
        mServer = server;
    }

    @Override
    public void send(IMMessage message, final IResultListener callback) throws RemoteException {
        try {
            ResultListener listener = new ResultListener() {
                @Override
                public void onResult(int code, String msg) {
                    try {
                        if (callback != null) {
                            callback.onResult(code, msg);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            };
            mServer.sendMessage(message, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendBroadCastByHttp(IMMessage message, IResultListener callback) throws RemoteException {
        try {
            ResultListener listener = new ResultListener() {
                @Override
                public void onResult(int code, String msg) {
                    try {
                        if (callback != null) {
                            callback.onResult(code, msg);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            };
            mServer.sendBroadCastByHttp(message, listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String fileService(String path) throws RemoteException {
        return mServer.fileServer(path);
    }

    @Override
    public Session getMySession() throws RemoteException {
        return mServer.getMySession();
    }

    @Override
    public Session getTargetSession() throws RemoteException {
        return mServer.getTargetSession();
    }
}



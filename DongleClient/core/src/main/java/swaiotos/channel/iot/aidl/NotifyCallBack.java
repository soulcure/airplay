package swaiotos.channel.iot.aidl;

import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.sdk.INotityCallBack;
import com.coocaa.sdk.IReceiveMessage;

import swaiotos.channel.iot.SkyServer;


public class NotifyCallBack extends INotityCallBack.Stub {//Stub内部类，其实就是一个Binder类

    private static final String TAG = "aidl";


    private SkyServer mServer;

    NotifyCallBack(SkyServer server) {
        super();
        mServer = server;
    }


    @Override
    public void registerCallback(String targetClient, IReceiveMessage cb) throws RemoteException {
        if (!TextUtils.isEmpty(targetClient) && cb != null) {
            Log.d(TAG, "registerCallback" + " targetClient= " + targetClient + " cb= " + cb.toString());
            mServer.registerCallback(targetClient, cb);
        }
    }

    @Override
    public void unregisterCallback(String targetClient, IReceiveMessage cb) throws RemoteException {
        if (!TextUtils.isEmpty(targetClient) && cb != null) {
            Log.d(TAG, "unregisterCallback" + " targetClient= " + targetClient + " cb= " + cb.toString());
            mServer.unregisterCallback(targetClient, cb);
        }
    }
}


package swaiotos.channel.iot.aidl;


import android.os.RemoteException;

import com.coocaa.sdk.IBindListener;
import com.coocaa.sdk.IDevice;
import com.coocaa.sdk.entity.Session;

import java.util.concurrent.TimeoutException;

import swaiotos.channel.iot.SkyServer;
import swaiotos.channel.iot.callback.BindResult;
import swaiotos.channel.iot.db.bean.Device;

public class IDeviceImpl extends IDevice.Stub {//Stub内部类，其实就是一个Binder类

    private static final String TAG = "aidl";

    private SkyServer mServer;

    IDeviceImpl(SkyServer server) {
        super();
        mServer = server;
    }


    @Override
    public void bindDevice(String bindCode, IBindListener callback) throws RemoteException {
        BindResult result = new BindResult() {
            @Override
            public void onSuccess(Device deviceData) {
                if (callback != null) {
                    try {
                        callback.onSuccess(deviceData.toJson());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFail(int code, String message) {
                if (callback != null) {
                    try {
                        callback.onFail(code, message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        mServer.bindDevice(bindCode, result);

    }

    @Override
    public Session connect(String sid, long timeout) throws RemoteException {
        Session session = null;
        try {
            session = mServer.connect(sid, timeout);
        } catch (TimeoutException e) {
            e.printStackTrace();
            session = null;
        }
        return session;
    }

    @Override
    public String getCurrentDevice() throws RemoteException {
        Device device = mServer.getCurrentDevice();
        if (device != null) {
            return device.toJson();
        }

        return null;
    }
}



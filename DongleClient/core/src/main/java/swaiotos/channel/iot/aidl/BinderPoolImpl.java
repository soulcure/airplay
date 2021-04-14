package swaiotos.channel.iot.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import com.coocaa.sdk.IBinderPool;

import swaiotos.channel.iot.SkyServer;

import static com.coocaa.sdk.BinderPool.BIND_SEND_MSG;
import static com.coocaa.sdk.BinderPool.BIND_RECEIVE_MSG;
import static com.coocaa.sdk.BinderPool.BIND_DEVICE;

public class BinderPoolImpl extends IBinderPool.Stub {

    private SkyServer mServer;

    public BinderPoolImpl(SkyServer server) {
        super();
        mServer = server;
    }

    @Override
    public IBinder queryBinder(int binderCode) throws RemoteException {
        Binder binder = null;
        switch (binderCode) {
            case BIND_SEND_MSG:
                binder = new IMChannelImpl(mServer);
                break;

            case BIND_RECEIVE_MSG:
                binder = new NotifyCallBack(mServer);
                break;

            case BIND_DEVICE:
                binder = new IDeviceImpl(mServer);
                break;
        }
        return binder;
    }
}
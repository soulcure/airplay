package swaiotos.channel.iot;

import android.os.Binder;

import com.coocaa.sdk.entity.IMMessage;
import com.coocaa.sdk.entity.Session;

import java.util.concurrent.TimeoutException;

import swaiotos.channel.iot.callback.BindResult;
import swaiotos.channel.iot.callback.ConnectStatusListener;
import swaiotos.channel.iot.callback.DeviceCallback;
import swaiotos.channel.iot.callback.LoginCallback;
import swaiotos.channel.iot.callback.NotifyListener;
import swaiotos.channel.iot.callback.ResultListener;
import swaiotos.channel.iot.callback.UnBindResult;

public class SkyServiceBinder extends Binder {

    private SkyServer mServer;

    public SkyServiceBinder(SkyServer server) {
        super();
        mServer = server;
    }


    public void loginSSE(final String sid, final String token,
                         final LoginCallback callback) {
        mServer.loginSSE(sid, token, callback);
    }


    public Session connect(String sid, long timeout) throws TimeoutException {
        return mServer.connect(sid, timeout);
    }


    public String fileServer(String path) {
        return mServer.fileServer(path);
    }


    public void loginOut() {
        mServer.loginOut();
    }

    public boolean isSSEConnected() {
        return mServer.isSSEConnected();
    }

    public boolean isLocalConnect() {
        return mServer.isLocalConnect();
    }


    public void sendMessage(IMMessage msg, ResultListener listener) {
        mServer.sendMessage(msg, listener);
    }

    public void sendBroadCastByHttp(IMMessage msg, ResultListener listener) {
        mServer.sendBroadCastByHttp(msg, listener);
    }


    public void addNotifyListener(String targetClient, NotifyListener listener) {
        mServer.addNotifyListener(targetClient, listener);
    }

    public void removeNotifyListener(String targetClient) {
        mServer.removeNotifyListener(targetClient);
    }

    public void setConnectListener(ConnectStatusListener listener) {
        mServer.setConnectListener(listener);
    }

    public Session getMySession() {
        return mServer.getMySession();
    }

    public String getSid() {
        return mServer.getSid();
    }


    public Session getTargetSession() {
        return mServer.getTargetSession();
    }

    public String getAccessToken() {
        return mServer.getAccessToken();
    }


    public void reqBindDevice(final DeviceCallback callback) {
        mServer.reqBindDevice(callback);
    }


    public void bindDevice(String bindCode, BindResult callback) {
        mServer.bindDevice(bindCode, callback);
    }

    public void unbindDevice(String targetSid, int deleteType, UnBindResult callback) {
        mServer.unbindDevice(targetSid, deleteType, callback);
    }


    public void refreshOnlineStatus(final DeviceCallback callback) {
        mServer.post(new Runnable() {
            @Override
            public void run() {
                mServer.refreshOnlineStatus(callback);
            }
        });
    }


}

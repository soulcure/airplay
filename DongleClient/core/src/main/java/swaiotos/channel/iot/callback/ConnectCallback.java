package swaiotos.channel.iot.callback;


import com.coocaa.sdk.entity.Session;

public interface ConnectCallback {
    void onSuccess(Session session);

    void onConnectFail(int code, String msg);

}

package com.coocaa.smartscreen.connect.callback;

import com.coocaa.smartscreen.data.channel.events.ConnectEvent;
import com.coocaa.smartscreen.data.channel.events.UnbindEvent;

import java.util.List;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.session.Session;

/**
 * @ClassName ConnectCallback
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/5/16
 * @Version TODO (write something)
 */
public interface ConnectCallback {
    void onConnecting();
    void onHistoryConnecting();
    void onSuccess(ConnectEvent connectEvent);
    void onFailure(ConnectEvent connectEvent);
    void onCheckConnect(ConnectEvent connectEvent);
    void onHistorySuccess(ConnectEvent connectEvent);
    void onHistoryFailure(ConnectEvent connectEvent);
    void onDeviceSelected(ConnectEvent connectEvent);
    void onUnbind(UnbindEvent unbindEvent);
    void onUnbindByDevice(UnbindEvent unbindEvent);

    /**
     * On session connect.
     *
     * @param session the session
     */
    void onSessionConnect(Session session);

    /**
     * On session update.
     *
     * @param session the session
     */
    void onSessionUpdate(Session session);

    /**
     * On session disconnect.
     *
     * @param session the session
     */
    void onSessionDisconnect(Session session);

    void onDeviceOffLine(Device device);

    void onDeviceOnLine(Device device);

    void onDeviceUpdate(Device device);

    void onDeviceReflushUpdate(List<Device> devices);

    void sseLoginSuccess();
    void loginState(int code, String info);
    void loginConnectingState(int code, String info);
}

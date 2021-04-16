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
public class ConnectCallbackImpl implements ConnectCallback {
    @Override
    public void onConnecting() {

    }

    @Override
    public void onHistoryConnecting() {

    }

    @Override
    public void onSuccess(ConnectEvent connectEvent) {

    }

    @Override
    public void onFailure(ConnectEvent connectEvent) {

    }

    @Override
    public void onCheckConnect(ConnectEvent connectEvent) {

    }

    @Override
    public void onHistorySuccess(ConnectEvent connectEvent) {

    }

    @Override
    public void onHistoryFailure(ConnectEvent connectEvent) {

    }

    @Override
    public void onDeviceSelected(ConnectEvent connectEvent) {

    }

    @Override
    public void onUnbind(UnbindEvent unbindEvent) {

    }

    @Override
    public void onUnbindByDevice(UnbindEvent unbindEvent) {

    }

    @Override
    public void onSessionConnect(Session session) {

    }

    @Override
    public void onSessionUpdate(Session session) {

    }

    @Override
    public void onSessionDisconnect(Session session) {

    }

    @Override
    public void onDeviceOffLine(Device device) {

    }

    @Override
    public void onDeviceOnLine(Device device) {

    }

    @Override
    public void onDeviceUpdate(Device device) {

    }

    @Override
    public void onDeviceReflushUpdate(List<Device> devices) {

    }

    @Override
    public void sseLoginSuccess() {

    }

    @Override
    public void loginState(int code, String info) {

    }

    @Override
    public void loginConnectingState(int code, String info) {

    }
}

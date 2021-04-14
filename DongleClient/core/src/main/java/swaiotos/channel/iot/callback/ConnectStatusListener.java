package swaiotos.channel.iot.callback;

import com.coocaa.sdk.entity.Session;

import java.util.List;

import swaiotos.channel.iot.db.bean.Device;

public interface ConnectStatusListener {
    //0 无连接 ，1 SSE连接，2 local连接， 3 SSE和local都连接
    void onConnectStatus(int code, String msg);

    //连接目标的Session变化
    void onTargetSessionUpdate(Session session);

    //绑定设备变化
    void onBindDevice(List<Device> list);
}
package swaiotos.sensor.client;

import android.hardware.SensorEvent;
import android.view.MotionEvent;
import android.view.View;

import swaiotos.sensor.connect.IConnectCallback;

/**
 * @Author: yuzhan
 */
public interface IConnectClient {
    boolean isConnected();
    void connect(String url, IConnectCallback callback);
    void disconnect();
    void send(String text);
    void sendMotionEvent(MotionEvent event, View v);
    void setSmartApi(ISmartApi smartApi);
    void sendSensorEvent(SensorEvent event, View v);
}

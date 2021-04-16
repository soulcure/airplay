package swaiotos.sensor.server;

import android.util.Log;

import java.util.Arrays;
import java.util.Collection;

/**
 * @Author: yuzhan
 */
public interface IConnectServer {

    void start();

    void stop();

    void setCallback(IServerCallback callback);

    void onClientTryConnect(String cid);

    void broadcast(String text);

    void broadcastTo(String text, String cid);

    /**
     * 发送给指定手机端，排除单个端
     * @param text
     */
    void broadcastExclude(String text, String cid);

    /**
     * 发送给指定手机端
     * @param text
     * @param clients
     */
    void broadcastTo(String text, Collection<String> clients);

    int getClientSize();
}

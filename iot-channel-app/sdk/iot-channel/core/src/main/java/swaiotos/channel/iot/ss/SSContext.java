package swaiotos.channel.iot.ss;

import android.content.Context;

import swaiotos.channel.iot.ss.channel.im.IMChannelManager;
import swaiotos.channel.iot.ss.client.ClientManager;
import swaiotos.channel.iot.ss.controller.ControllerServer;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.DeviceManagerServer;
import swaiotos.channel.iot.ss.device.IConnectResult;
import swaiotos.channel.iot.ss.manager.SmartScreenManager;
import swaiotos.channel.iot.ss.server.ServerInterface;
import swaiotos.channel.iot.ss.session.SessionManagerServer;
import swaiotos.channel.iot.ss.webserver.WebServer;

/**
 * @ClassName: SSContext
 * @Author: lu
 * @CreateDate: 2020/4/10 6:43 PM
 * @Description:
 */
public interface SSContext {
    Context getContext();

    String getLSID();

    String getAccessToken();

    String getTempBindCode();

    DeviceInfo getDeviceInfo();

    SmartScreenManager getSmartScreenManager();

    ControllerServer getController();

    SessionManagerServer getSessionManager();

    WebServer getWebServer();

    IMChannelManager getIMChannel();

    DeviceManagerServer getDeviceManager();

    ClientManager getClientManager();

    ServerInterface getServerInterface();

    TransmitterCallBack getTransmitter();

    void post(Runnable runnable);

    void postDelay(Runnable runnable, long delay);

    void removeCallbacks(Runnable runnable);

    void reset(String sid, String token);

    void reset(String sid, String token,String userId);

    void connectSSETest(String lsid, IConnectResult result);

    void connectLocalTest(String ip, IConnectResult result);
}

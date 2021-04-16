package swaiotos.channel.iot.ss.controller;

import swaiotos.channel.iot.ss.session.Session;

/**
 * @ClassName: IController
 * @Author: lu
 * @CreateDate: 2020/3/30 2:48 PM
 * @Description:
 */
public interface ControllerServer extends Controller {
    interface OnDeviceAliveChangeListener {
        void onDeviceOnline(String lsid);

        void onDeviceOffline(String lsid);
    }

    interface OnDeviceDistanceChangeListener {
        void onDeviceLeave(String lsid);

        void onDeviceJoin(String lsid);
    }

    interface OnDeviceBindStatusListener {
        void onDeviceBind(String lsid);

        void onDeviceUnBind(String lsid);
    }

    void open();

    DeviceStateManager getDeviceStateManager();

    void addOnDeviceAliveChangeListener(OnDeviceAliveChangeListener listener);

    void removeOnDeviceAliveChangeListener(OnDeviceAliveChangeListener listener);

    void addOnDeviceBindStatusListener(OnDeviceBindStatusListener listener);

    void removeOnDeviceBindStatusListener(OnDeviceBindStatusListener listener);

    void addOnDeviceDistanceChangeListener(OnDeviceDistanceChangeListener listener);

    void removeOnDeviceDistanceChangeListener(OnDeviceDistanceChangeListener listener);

    void close();

    void onSpaceAccount(String spaceAccount);

    String getSpaceAccount();

    Session connectSSE(final String lsid, long timeout, final boolean isConnectLocal) throws Exception;
}

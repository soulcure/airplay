package swaiotos.channel.iot.ss.device;

import android.os.RemoteException;

import java.util.List;

import swaiotos.channel.iot.ss.session.Session;

/**
 * @ClassName: Devices
 * @Author: colin
 * @CreateDate: 2020/4/15 19:48 PM
 * @Description:
 */
public interface DeviceManager {
    /**
     * 设备列表查询
     *
     * @return List<Device> 返回所有绑定的设备
     * @throws Exception the exception
     */
    List<Device> getDevices() throws Exception;

    /**
     * 设备列表查询 并实时返回设备在线状态
     */
    List<Device> getDeviceOnlineStatus() throws Exception;


    Device getCurrentDevice() throws Exception;


    Session getLocalSessionBySid(String sid) throws Exception;

    /**
     * 设备属性变化
     */
    interface OnDeviceChangedListener {
        void onDeviceOffLine(Device device);

        void onDeviceOnLine(Device device);

        void onDeviceUpdate(Device device);
    }

    void addOnDeviceChangedListener(OnDeviceChangedListener listener) throws RemoteException;

    void removeOnDeviceChangedListener(OnDeviceChangedListener listener) throws RemoteException;


    interface OnDeviceBindListener {
        void onDeviceBind(String lsid);

        void onDeviceUnBind(String lsid);
    }

    /**
     * 设备网络请求监听
     */
    interface OnDevicesReflushListener {
        void onDeviceReflushUpdate(List<Device> devices);
    }

    void addDeviceBindListener(OnDeviceBindListener listener) throws RemoteException;

    void removeDeviceBindListener(OnDeviceBindListener listener) throws RemoteException;

    interface OnDeviceInfoUpdateListener {
        void onDeviceInfoUpdate(List<Device> devices);

        void sseLoginSuccess();

        void loginState(int code, String info);

        void loginConnectingState(int code,String info);
    }

    void addDeviceInfoUpdateListener(OnDeviceInfoUpdateListener listener) throws RemoteException;

    void removeDeviceInfoUpdateListener(OnDeviceInfoUpdateListener listener) throws RemoteException;

    void addDevicesReflushListener(OnDevicesReflushListener listener) throws RemoteException;

    void removeDevicesReflushListener(OnDevicesReflushListener listener) throws RemoteException;

    /**
     * 同步网络请求拉取数据
     **/
    List<Device> updateDeviceList();

    String getAccessToken() throws RemoteException;

}

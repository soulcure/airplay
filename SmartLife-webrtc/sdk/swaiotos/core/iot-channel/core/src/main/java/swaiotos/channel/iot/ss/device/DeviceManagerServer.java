package swaiotos.channel.iot.ss.device;


import swaiotos.channel.iot.ss.controller.DeviceState;
import swaiotos.channel.iot.ss.session.Session;

/**
 * The interface Device manager server.
 */
public interface DeviceManagerServer extends DeviceManager, DeviceAdminManager {


    interface LsidListener {
        void onUpdateEnd();
    }

    /**
     * add device.
     */
    boolean addDevice(Device device);

    /**
     * remove lsid.
     */
    boolean removeDevice(String lsid);

    /**
     * remove lsid.
     */
    boolean updateDeviceState(DeviceState state);

    /**
     * Update lsid.
     *
     * @param lsidListener the lsid listener
     */
    void updateLsid(LsidListener lsidListener, int callbackType);


    /**
     * 校验source和target是否有有效的设备关系
     *
     * @param source the source
     * @param target the target
     * @return true :有有效的关系 false:无有效关系
     */
    boolean validate(String source, String target);

    boolean updateCurrentDevice(Session s);

    void onDeviceInfoUpdateList();

    //进入房间接口
    int join(String roomId);

    //离开房间接口
    int leave(String roomId);

    void sseLoginSuccess();

    void loginState(int state, String info);

    void loginConnectingState(int state, String info);
}

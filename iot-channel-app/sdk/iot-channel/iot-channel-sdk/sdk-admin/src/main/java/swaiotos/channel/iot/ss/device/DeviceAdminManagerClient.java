package swaiotos.channel.iot.ss.device;

import java.util.List;

import swaiotos.channel.iot.ss.SSChannel;

/**
 * @ClassName: IDeviceAdminManagerClient
 * @Author: lu
 * @CreateDate: 2020/4/18 5:27 PM
 * @Description:
 */
public interface DeviceAdminManagerClient extends DeviceAdminManager, SSChannel.IClient<IDeviceAdminManagerService> {
    void setDeviceManager(DeviceManager manager);

    /**异步拉取网络数据*/
    interface DeviceListCallBack {
        void onDevices(List<Device> list);
    }

    void updateDeviceList(DeviceListCallBack callBack);

    void close();
}

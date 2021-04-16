package swaiotos.channel.iot.ss.device;

import java.util.List;

import swaiotos.channel.iot.ss.SSChannel;

/**
 * @ClassName: IDeviceManagerClient
 * @Author: lu
 * @CreateDate: 2020/4/18 5:14 PM
 * @Description:
 */
public interface DeviceManagerClient extends DeviceManager, SSChannel.IClient<IDeviceManagerService> {

    /**异步拉取网络数据*/
    interface DeviceListCallBack {
        void onDevices(List<Device> list);
    }

    void updateDeviceList(DeviceListCallBack callBack);

    void close();

}

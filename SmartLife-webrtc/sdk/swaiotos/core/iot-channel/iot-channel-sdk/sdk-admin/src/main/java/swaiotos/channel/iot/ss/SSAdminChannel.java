package swaiotos.channel.iot.ss;

import swaiotos.channel.iot.ss.controller.Controller;
import swaiotos.channel.iot.ss.device.DeviceAdminManager;


/**
 * The interface SSChannel.
 *
 * @ClassName: SSChannel
 * @Author: lu
 * @CreateDate: 2020 /3/21 2:14 PM
 * @Description:
 */
public interface SSAdminChannel extends SSChannel {

    /**
     * 获取控制器实例
     *
     * @return the controller
     */
    Controller getController();

    DeviceAdminManager getDeviceAdminManager();
}

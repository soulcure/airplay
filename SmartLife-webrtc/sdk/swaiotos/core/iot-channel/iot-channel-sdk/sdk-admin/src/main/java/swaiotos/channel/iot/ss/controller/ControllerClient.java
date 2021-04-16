package swaiotos.channel.iot.ss.controller;

import swaiotos.channel.iot.ss.SSChannel;

/**
 * @ClassName: ControllerClient
 * @Author: lu
 * @CreateDate: 2020/4/13 3:06 PM
 * @Description:
 */
public interface ControllerClient extends Controller, SSChannel.IClient<IControllerService> {

    void close();
}

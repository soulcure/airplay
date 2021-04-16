package swaiotos.channel.iot.tv.iothandle.handle.base;


import com.coocaa.statemanager.common.bean.CmdData;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;

public interface MessageHandle {

    void onHandle(IMMessage message, SSChannel channel, CmdData cmdData);

}

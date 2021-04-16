package com.swaiotos.testdemo_pad.message;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.device.Device;

public interface IMessage {
    IMessage MSG = new MessageManager();

    IMMessage  getTestMessage(SSChannel channel);

    boolean sendMessage(IMMessage msg);

    Device getCurrentDevice();
}

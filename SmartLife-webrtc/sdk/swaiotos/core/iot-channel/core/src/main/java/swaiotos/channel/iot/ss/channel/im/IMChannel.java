package swaiotos.channel.iot.ss.channel.im;

import java.util.List;

import swaiotos.channel.iot.ss.channel.IChannel;
import swaiotos.channel.iot.ss.session.Session;

/**
 * @ClassName: IMChannel
 * @Author: lu
 * @CreateDate: 2020/4/10 3:53 PM
 * @Description:
 */
public interface IMChannel extends IChannel, IMChannelServer, IIMChannelCore {

    void send(Session target, IMMessage message) throws Exception;

    void send(Session target, IMMessage message, IMMessageCallback callback) throws Exception;

    boolean serverSend(IMMessage message, IMMessageCallback callback) throws Exception;

    List<String> serverSendList() throws Exception;

    void removeServerConnect(String sid);
}

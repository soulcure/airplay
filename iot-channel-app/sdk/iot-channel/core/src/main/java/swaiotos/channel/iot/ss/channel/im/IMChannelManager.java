package swaiotos.channel.iot.ss.channel.im;

import swaiotos.channel.iot.ss.session.Session;

/**
 * @ClassName: IMChannelManager
 * @Author: lu
 * @CreateDate: 2020/4/11 11:10 AM
 * @Description:
 */
public interface IMChannelManager extends IMChannel.Receiver, IMChannelServer, IIMChannel {
    void open();

    boolean availableLocal(Session session);

    boolean availableCloud(Session session);

    void close();

    boolean isConnectSSE();

    void removeServerConnect(String sid);
}

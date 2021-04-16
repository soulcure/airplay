package swaiotos.channel.iot.ss.session;

import swaiotos.channel.iot.ss.SSChannel;

/**
 * @ClassName: SessionManagerClient
 * @Author: lu
 * @CreateDate: 2020/4/13 3:06 PM
 * @Description:
 */
public interface SessionManagerClient extends SessionManager, SSChannel.IClient<ISessionManagerService> {
    void close();
}

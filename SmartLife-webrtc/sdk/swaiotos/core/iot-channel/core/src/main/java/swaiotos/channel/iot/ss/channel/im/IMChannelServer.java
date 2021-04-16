package swaiotos.channel.iot.ss.channel.im;

import swaiotos.channel.iot.ss.session.Session;

/**
 * @ClassName: IMChannelServer
 * @Author: lu
 * @CreateDate: 2020/4/11 3:22 PM
 * @Description:
 */
public interface IMChannelServer {
    interface Receiver {
        void onReceive(IMChannel channel, IMMessage message);
    }

    interface TcpClientResult {
        void onResult(int code, String message);
    }

    void setReceiver(Receiver receiver);

    void openClient(Session session, TcpClientResult callback);

    void reOpenLocalClient(Session session);

    void reOpenSSE();

    boolean available(Session session);

    void closeClient(Session session, boolean forceClose);
}

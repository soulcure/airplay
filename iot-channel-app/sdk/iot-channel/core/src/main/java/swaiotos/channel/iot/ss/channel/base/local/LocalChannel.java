package swaiotos.channel.iot.ss.channel.base.local;

import java.util.List;

import swaiotos.channel.iot.ss.channel.base.BaseChannel;
import swaiotos.channel.iot.ss.channel.im.IMChannelServer;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.channel.stream.IStreamChannel;

/**
 * @ClassName: LocalChannel
 * @Author: lu
 * @CreateDate: 2020/4/10 3:48 PM
 * @Description:
 */
public interface LocalChannel extends BaseChannel {
    interface LocalChannelMonitor {
        void onReceiverAvailableChanged(IStreamChannel.Receiver receiver, boolean available);

        void onSenderAvailableChanged(IStreamChannel.Sender sender, boolean available);
    }

    void setLocalChannelMonitor(LocalChannelMonitor monitor);

    int openReceiver(IStreamChannel.Receiver receiver);

    boolean available(IStreamChannel.Receiver receiver);

    void closeReceiver(int channelId);

    IStreamChannel.Sender openSender(String ip, int channelId,
                                     IMChannelServer.TcpClientResult callback,
                                     IMChannelServer.Receiver receiver);

    boolean available(IStreamChannel.Sender sender);

    void closeSender(IStreamChannel.Sender sender);

    boolean sendServerMessage(IMMessage message);

    List<String> serverConnectList();

    void removeServerConnect(String sid);
}

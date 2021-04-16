package swaiotos.channel.iot.ss.channel.base;

import swaiotos.channel.iot.ss.channel.IChannel;

/**
 * @ClassName: BaseChannel
 * @Author: lu
 * @CreateDate: 2020/4/13 6:38 PM
 * @Description:
 */
public interface BaseChannel extends IChannel {
    interface Callback {
        void onConnected(BaseChannel channel);

        void onDisconnected(BaseChannel channel);
    }

    void addCallback(Callback callback);

    void removeCallback(Callback callback);
}

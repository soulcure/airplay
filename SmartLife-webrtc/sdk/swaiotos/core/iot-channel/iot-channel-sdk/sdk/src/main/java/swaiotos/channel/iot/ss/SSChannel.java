package swaiotos.channel.iot.ss;

import android.content.Context;
import android.os.IInterface;

import swaiotos.channel.iot.ss.channel.im.IIMChannel;
import swaiotos.channel.iot.ss.device.DeviceManager;
import swaiotos.channel.iot.ss.session.SessionManager;


/**
 * The interface SSChannel.
 *
 * @ClassName: SSChannel
 * @Author: lu
 * @CreateDate: 2020 /3/21 2:14 PM
 * @Description:
 */
public interface SSChannel {
    interface IClient<T extends IInterface> {
        void setService(T service);
    }

    String SERVICE_IM = "IMChannel";

    /**
     * The constant FORCE_SSE.
     */
    String FORCE_SSE = "force-sse";


    /**
     * 协议版本标识
     */
    String PROTO_VERSION = "proto-version";


    /**
     * 局域网IM通道标识
     */
    String IM_LOCAL = "im-local";

    /**
     * 云端IM通道标识
     */
    String IM_CLOUD = "im-cloud";


    /**
     * 局域网Stream通道标识
     */
    String STREAM_LOCAL = "stream-local";


    /**
     * 云端Stream通道标识
     */
    String STREAM_CLOUD = "stream-cloud";


    String ADDRESS_LOCAL = "address-local";

    /**
     * Open.
     *
     * @param context the context
     * @param service
     * @throws Exception the exception
     */
    void open(Context context, IMainService service) throws Exception;

    void open(ISSChannelService service) throws Exception;


    /**
     * 获取Session管理器
     *
     * @return the session manager
     */
    SessionManager getSessionManager();

    /**
     * 获取IM通道
     *
     * @return the im channel
     */
    IIMChannel getIMChannel();

    DeviceManager getDeviceManager();

    /**
     * Close.
     */
    void close();
}

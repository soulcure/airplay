package swaiotos.channel.iot;

import android.content.Context;

import swaiotos.channel.iot.ss.IMainService;
import swaiotos.channel.iot.ss.SSChannel;

/**
 * The interface Iot channel.
 *
 * @ClassName: IOTChannel
 * @Author: lu
 * @CreateDate: 2020 /4/14 11:12 AM
 * @Description:
 */
public interface IOTChannel {
    /**
     * 异步Open的回调监听
     */
    interface OpenCallback {
        /**
         * 绑定成功后回调
         * 如果SSChannelService退出（kill or crash），会自动重新绑定，所以这个方法会在这种情况下再次被调用到
         *
         * @param channel 成功绑定上的SSChanel实例
         */
        void onConntected(SSChannel channel);

        /**
         * 绑定失败后回调
         *
         * @param s 错误信息
         */
        void onError(String s);
    }

    /**
     * IOTChannel 的实例
     */
    IOTChannel mananger = new IOTChannelImpl();

    void open(Context context, IMainService service) throws Exception;

    /**
     * 异步绑定，此方法绑定的SSChannelService是context所在package中的实现
     *
     * @param context  the context
     * @param callback the callback
     */
    void open(Context context, OpenCallback callback);

    /**
     * 异步绑定，此方法绑定的SSChannelService是packageName所指定的package中的实现
     *
     * @param context     the context
     * @param packageName the package name
     * @param callback    the callback
     */
    void open(Context context, String packageName, OpenCallback callback);

    /**
     * 获取SSChannel实例
     *
     * @return the ss channel
     */
    SSChannel getSSChannel();

    /**
     * 是否绑定
     */
    boolean isOpen();


    /**
     * 判断服务存不存在
     *
     * @param context 上下文句柄
     * @param type    1:tv 2:pad 3:phone/mobile
     */
    boolean isServiceRun(Context context, int type);

    /**
     * 解绑
     */
    void close();
}

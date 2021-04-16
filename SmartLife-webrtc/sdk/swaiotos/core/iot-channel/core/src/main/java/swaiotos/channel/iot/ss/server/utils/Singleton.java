package swaiotos.channel.iot.ss.server.utils;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv.utils
 * @ClassName: Singleton
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/9 0:14
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/9 0:14
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class Singleton<T> {

    public Singleton() {
    }

    private T mInstance;

    protected abstract T create();

    public final T get() {
        if (mInstance == null) {
            synchronized (this) {
                if (mInstance == null) {
                    mInstance = create();
                }
            }
        }
        return mInstance;
    }
}
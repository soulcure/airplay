package swaiotos.channel.iot.ss.device;

import android.os.Parcelable;

/**
 * @ClassName: DeviceInfo
 * @Author: lu
 * @CreateDate: 2020/4/22 2:43 PM
 * @Description:
 */
public abstract class DeviceInfo implements Parcelable {
    public enum TYPE {
        TV,
        PAD,
        PHONE,
        THIRD
    }

    public final String clazzName = getClass().getName();

    public abstract TYPE type();

    public abstract String encode();
}

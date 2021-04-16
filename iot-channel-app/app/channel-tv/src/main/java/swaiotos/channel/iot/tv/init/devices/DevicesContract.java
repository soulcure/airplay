package swaiotos.channel.iot.tv.init.devices;

import android.content.Context;

import java.util.List;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.tv.base.iiface.IBasePresenter;
import swaiotos.channel.iot.tv.base.iiface.IBaseView;

/**
 * @author wagnyuehui
 * @time 2020/3/27
 * @describe
 */
public interface DevicesContract {

    interface View extends IBaseView<Presenter> {
        void refreshDevices(List<Device> devices);
    }

    interface Presenter extends IBasePresenter<View> {
        void init(Context context);

    }
}

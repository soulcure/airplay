package swaiotos.channel.iot.tv.init;

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
public interface SmallContract {

    interface View extends IBaseView<Presenter> {

        void refushOrUpdateQRCode(String qrcodeInfo, String url, String qrcodeExpire);

        //type: 1：初始化设备数量  2.监听设备被绑定
        void triggerQuerDevices();

        void refreshTips();
    }

    interface Presenter extends IBasePresenter<View> {
        void init(Context context);

    }
}

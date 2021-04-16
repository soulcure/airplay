package swaiotos.channel.iot.tv.pad;

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
public interface PadInitContract {

    interface View extends IBaseView<Presenter> {
        void showToast(String msg);
        void onBindStartShow(String msg);
        void onBindEndShow(String pushToken);
        void refrushTips(String msg, boolean success);

        void showDevices(List<Device> devices);
    }

    interface Presenter extends IBasePresenter<View> {
        void init(Context context);

        void bind(String bindCode, String accessToken);

        /**
         * accessToken 本机accessToken  sid：被解绑机型  type：类型 ：1主动解绑
         * */
        void unBind(String accessToken,String sid,int type);

        /**
         * 获取设备列表
         * */
        void queryDevices();
    }
}

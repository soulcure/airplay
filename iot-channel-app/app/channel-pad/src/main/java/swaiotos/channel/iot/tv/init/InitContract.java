package swaiotos.channel.iot.tv.init;

import android.content.Context;

import swaiotos.channel.iot.tv.base.iiface.IBasePresenter;
import swaiotos.channel.iot.tv.base.iiface.IBaseView;

/**
 * @author wagnyuehui
 * @time 2020/3/27
 * @describe
 */
public interface InitContract {

    interface View extends IBaseView<Presenter> {

        void refushOrUpdateQRCode(String qrcodeInfo, String qrcodeExpire);

        void showBindView(String pushMsg);

        void hideBindDialog();

        void refreshTips(String msg, boolean success);
    }

    interface Presenter extends IBasePresenter<View> {
        void init(Context context);
    }
}

package swaiotos.channel.iot.tv.base.iiface;

import android.content.Context;

/**
 * @author wagnyuehui
 * @time 2020/3/27
 * @describe
 */
public interface IAppBaseView<T extends IBasePresenter> extends IBaseView<T> {

    /**
     *
     * get activity context.
     *
     * */
    Context getActivityContext();

}

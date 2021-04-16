package swaiotos.channel.iot.tv.base.iiface;

/**
 * @author wagnyuehui
 * @time 2020/3/27
 * @describe
 */
public interface IBaseView<T extends IBasePresenter> {

    void setPresenter(T presenter);

    boolean isActive();

    interface OnCancleListener {
        void onCancle();
    }

}

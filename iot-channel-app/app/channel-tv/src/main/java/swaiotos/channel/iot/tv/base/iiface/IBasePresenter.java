package swaiotos.channel.iot.tv.base.iiface;

/**
 * @author wagnyuehui
 * @time 2020/3/27
 * @describe
 */
public interface IBasePresenter<V extends IBaseView> {
    void attachView(V mvpView);

    void detachView();

    void start();

    void unsubscribe();
}

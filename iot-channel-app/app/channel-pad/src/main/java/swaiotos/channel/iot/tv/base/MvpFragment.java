package swaiotos.channel.iot.tv.base;

import swaiotos.channel.iot.tv.base.iiface.IBasePresenter;

/**
 * @author wagnyuehui
 * @time 2020/3/27
 * @describe
 */
public abstract class MvpFragment<P extends IBasePresenter> extends BaseFragment {

    protected abstract P getPresenter();


    @Override
    public void onDestroy() {
        super.onDestroy();
        P presenter = getPresenter();
        if (presenter != null) {
            presenter.detachView();
        }
    }
}

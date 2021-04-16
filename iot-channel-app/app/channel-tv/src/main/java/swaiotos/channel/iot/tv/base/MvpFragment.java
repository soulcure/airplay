package swaiotos.channel.iot.tv.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import swaiotos.channel.iot.tv.TVChannelApplication;
import swaiotos.channel.iot.tv.base.iiface.IBasePresenter;

/**
 * @author wagnyuehui
 * @time 2020/3/27
 * @describe
 */
public abstract class MvpFragment<P extends IBasePresenter> extends BaseFragment {

    protected abstract P getPresenter();

    protected abstract void onUnBindCallBack(String sid);
    private DeviceChange deviceChange;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceChange = new DeviceChange();
        ((TVChannelApplication)getActivity().getApplication()).getListeners().add(deviceChange);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (deviceChange != null)
            ((TVChannelApplication)getActivity().getApplication()).getListeners().remove(deviceChange);

        P presenter = getPresenter();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    class DeviceChange implements DeviceChangeListener {

        @Override
        public void OnUnBindCallBack(String sid) {
            onUnBindCallBack(sid);
        }

        @Override
        public void onBindCallBack() {

        }
    }



}

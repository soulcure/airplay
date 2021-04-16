package swaiotos.channel.iot.tv.base;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import swaiotos.channel.iot.tv.base.iiface.IBasePresenter;
import swaiotos.channel.iot.tv.base.iiface.IBaseView;

/**
 * @author wagnyuehui
 * @time 2020/3/27
 * @describe
 */
public class BasePresenter<V extends IBaseView> implements IBasePresenter<V> {
    private Reference<V> mViewRef;


    @Override
    public void attachView(V mvpView) {
        setMvpView(mvpView);
    }

    @Override
    public void detachView() {
        setMvpView(null);
    }

    @Override
    public void start() {

    }

    @Override
    public void unsubscribe() {

    }

    private void setMvpView(V mvpView) {
        if (null == mvpView) {
            if (mViewRef != null) {
                mViewRef.clear();
                mViewRef = null;
            }
        } else {
            mViewRef = new WeakReference<>(mvpView);
        }
    }

    protected V getView() {
        if (mViewRef != null) {
            return mViewRef.get();
        }
        return null;
    }
}

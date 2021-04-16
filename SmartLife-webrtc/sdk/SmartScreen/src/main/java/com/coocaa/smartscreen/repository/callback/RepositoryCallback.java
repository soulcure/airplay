package com.coocaa.smartscreen.repository.callback;


/**
 * 数据仓库回调
 * Created by songxing on 2020/6/5
 */
public interface RepositoryCallback<T> {
    void onStart();

    void onSuccess(T success);

    void onFailed(Throwable e);

    /**
     * 不需要onStart()的接口仓库回调
     * Created by songxing on 2020/6/5
     */
    public abstract class Default<T> implements RepositoryCallback<T> {
        @Override
        public void onStart() {

        }
    }
}

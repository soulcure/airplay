package com.coocaa.smartscreen.repository.future;


import com.coocaa.smartscreen.repository.callback.RepositoryCallback;

/**
 * 仓库接口返回类，提供一个设置回调方法
 * @param <T>
 */
public interface InvocateFuture<T> {
    void setCallback(RepositoryCallback<T> callback);
}

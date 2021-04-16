package com.coocaa.smartmall.data.api;

/**
 * Description: 自定义返回接口
 * Create by wzh on 2019-11-13
 */
public interface HttpSubscribe<T> {
    void onSuccess(T result);

    void onError(HttpThrowable error);
}

package com.coocaa.movie.product.m;

public interface NCallBack<T> {
    void onSuccess(T t);
    void onFailed(String s);
}

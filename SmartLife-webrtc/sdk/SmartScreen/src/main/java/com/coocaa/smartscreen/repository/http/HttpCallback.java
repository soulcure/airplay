package com.coocaa.smartscreen.repository.http;

public interface HttpCallback<T> {
    public void callback(T t);
    public void error(Throwable e);
}

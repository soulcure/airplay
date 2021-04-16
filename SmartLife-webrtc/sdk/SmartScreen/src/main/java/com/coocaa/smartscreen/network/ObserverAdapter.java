package com.coocaa.smartscreen.network;

import android.util.Log;

import io.reactivex.observers.DefaultObserver;

/**
 * 提供空实现的Observer
 * {@link DefaultObserver}
 * Created by songxing on 2020/3/23
 */
public class ObserverAdapter<T> extends DefaultObserver<T> {
    private static final String TAG = ObserverAdapter.class.getSimpleName();


    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
    }

    @Override
    public void onNext(T t) {
        Log.d(TAG, "onNext: " + t.toString());
    }

    @Override
    public void onError(Throwable e) {
        Log.d(TAG,"onError" + e.toString());
    }

    @Override
    public void onComplete() {
        Log.d(TAG, "onComplete");
    }
}

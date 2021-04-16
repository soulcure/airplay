package com.coocaa.tvpi.module.homepager.cotroller;


import android.os.Handler;
import android.util.Log;

import com.coocaa.publib.PublibHelper;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.Observer;


/**
 * 镜像超时监听，超过15秒没有收到电视机启动接受镜像回调会触发这个超时
 * Created by songxing on 2020/5/18
 */
public class MirrorScreenTimeoutObserver {
    private static final String TAG = "MirrorScreenTimeout";
    private List<TimeoutObserver> timeoutObservers = new ArrayList<>();
    private List<Observer<Integer>> timeoutObserverLocal = new ArrayList<>(1);
    private Handler uiHandler;


    private static class InstanceHolder {
        public final static MirrorScreenTimeoutObserver instance = new MirrorScreenTimeoutObserver();
    }

    public static MirrorScreenTimeoutObserver getInstance() {
        return InstanceHolder.instance;
    }

    private MirrorScreenTimeoutObserver() {
        uiHandler = new Handler(PublibHelper.getContext().getMainLooper());
    }


    // 通知APP观察者
    private <T> void notifyObservers(List<Observer<T>> observers, T result) {
        if (observers == null || observers.isEmpty()) {
            return;
        }

        // 创建副本，为了使得回调到app后，app如果立即注销观察者，会造成List异常。
        List<Observer<T>> copy = new ArrayList<>(observers.size());
        copy.addAll(observers);

        for (Observer<T> o : copy) {
            o.onChanged(result);
//            o.onEvent(result);
        }
    }

    // 注册注销APP观察者
    private <T> void registerObservers(List<Observer<T>> observers, final Observer<T> observer, boolean register) {
        if (observers == null || observer == null) {
            return;
        }

        if (register) {
            observers.add(observer);
        } else {
            observers.remove(observer);
        }
    }

    public void observeTimeout(Observer<Integer> observer, boolean register) {
        registerObservers(timeoutObserverLocal, observer, register);
        if (register) {
            addTimeout();
        } else {
            removeAllTimeout();
        }
    }


    private class TimeoutObserver implements Runnable {

        TimeoutObserver() {

        }

        @Override
        public void run() {
            Log.i(TAG, "notify timeout ");
            notifyObservers(timeoutObserverLocal, 0);
        }
    }



    private void removeAllTimeout() {
        Log.i(TAG, "remove all timeout");
        for (TimeoutObserver observer : timeoutObservers) {
            uiHandler.removeCallbacks(observer);
        }
        timeoutObservers.clear();
    }

    private void addTimeout() {
        TimeoutObserver timeoutObserver = new TimeoutObserver();
        timeoutObservers.add(timeoutObserver);
        int TIME_OUT = 15 * 1000;
        uiHandler.postDelayed(timeoutObserver, TIME_OUT);
    }
}

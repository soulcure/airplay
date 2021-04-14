package com.swaiot.webrtc.http;

import com.swaiot.webrtc.response.BindDeviceResp;
import com.swaiot.webrtc.response.LinkCodeResp;

import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class HttpEngine {

    private static final HttpMethod deviceService = HttpManager.instance().create(HttpMethod.class);

    /**
     * 获取
     */
    public static void getLinkCode(String url,Map<String, String> map,
                                     Observer<LinkCodeResp> observer) {
        setSubscribe(deviceService.getLinkCode(url,map), observer);
    }


    private static <T> void setSubscribe(Observable<T> observable, Observer<T> observer) {
        observable.subscribeOn(Schedulers.io())
                .subscribeOn(Schedulers.newThread())//子线程访问网络
                .observeOn(AndroidSchedulers.mainThread())//回调到主线程
                .subscribe(observer);
    }
}
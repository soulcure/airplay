package com.coocaa.smartscreen.connect.service;

import com.coocaa.smartscreen.data.channel.events.ProgressEvent;

/**
 * @Author: yuzhan
 */
public class MsgProgressEventObserver {

    public interface IProgressEventObserver {
        void onProgressLoading(ProgressEvent event);
        void onProgressResult(ProgressEvent event);
    }

    private static IProgressEventObserver observer;

    public static void setObserver(IProgressEventObserver o) {
        observer = o;
    }

    public static void onProgressLoading(ProgressEvent event) {
        if(observer != null) {
            observer.onProgressLoading(event);
        }
    }

    public static void onProgressResult(ProgressEvent event) {
        if(observer != null) {
            observer.onProgressResult(event);
        }
    }
}

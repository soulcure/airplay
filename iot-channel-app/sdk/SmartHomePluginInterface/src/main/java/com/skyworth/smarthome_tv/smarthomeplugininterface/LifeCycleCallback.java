package com.skyworth.smarthome_tv.smarthomeplugininterface;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 2020/6/11
 */
public interface LifeCycleCallback {
    /**
     * 主页在该版面onResume
     */
    void onResume();

    /**
     * 主页在该版面onPause
     */
    void onPause();

    /**
     * 主页在该版面onStop
     */
    void onStop();

    /**
     * 主页生命周期onDestroy
     */
    void onDestroy();
}

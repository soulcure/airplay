package com.coocaa.tvpi.module.local.utils;

import android.content.Context;

import com.coocaa.tvpi.module.io.HomeIOThread;

/**
 * Created by dvlee1 on 11/11/15.
 * 作用：预先初始化一些数据（影响界面速度的不能放这里）
 */
public class PreInitDataCenter {

    private static PreInitDataCenter mInstance = new PreInitDataCenter();

    public static PreInitDataCenter getInstance(){
        if(mInstance == null){
            mInstance = new PreInitDataCenter();
            return mInstance;
        }
        return mInstance;
    }

    public void init(final Context context){
        HomeIOThread.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // TODO dvlee，需要简化逻辑，目前的相册并不需要遍历全部照片
                    MediaStoreHelper.init(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

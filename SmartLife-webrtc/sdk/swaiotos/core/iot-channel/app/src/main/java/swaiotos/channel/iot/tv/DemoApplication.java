package swaiotos.channel.iot.tv;

import android.app.Application;
import android.util.Log;


import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.tv.utils.Constant;
import swaiotos.channel.iot.ss.SSAdminChannel;

public class DemoApplication extends Application {

    private static final String TAG = "iot";

    private boolean mInitIOTChannel;

    @Override
    public void onCreate() {
        super.onCreate();
        initIOTChannel();

    }

    /**
     * iot-channel 初始化 mInitIOTChannel 为true初始化成功 ，false为初始化失败
     */
    private void initIOTChannel() {
        /** 以下二选一**/
        //有iot-admin-channel_v{$$}.aar 和 iot-channel-v{$$}.aar 初始化如下
        IOTAdminChannel.mananger.open(this, Constant.COOCAA_IOT_CHANNEL_PACKAGENAME,
                new IOTAdminChannel.OpenCallback() {
                    @Override
                    public void onConntected(SSAdminChannel channel) {
                        Log.d(TAG, "onConntected: 绑定channel成功");
                        mInitIOTChannel = true;
                    }

                    @Override
                    public void onError(String s) {
                        Log.d(TAG, "---onError---:" + s);
                        mInitIOTChannel = false;
                    }
                });

        //仅有iot-channel-v{$$}.aar 初始化如下
        /*IOTChannel.mananger.open(this, Constant.COOCAA_IOT_CHANNEL_PACKAGENAME, new IOTChannel.OpenCallback() {
            @Override
            public void onConntected(SSChannel ssChannel) {
                Log.d(TAG, "onConntected: 绑定channel成功");
                mInitIOTChannel = true;
            }

            @Override
            public void onError(String s) {
                Log.e(TAG, "---onError---:"+s);
                mInitIOTChannel = false;
            }
        });*/
    }

    public boolean getInitIOTChannel() {
        return mInitIOTChannel;
    }

}

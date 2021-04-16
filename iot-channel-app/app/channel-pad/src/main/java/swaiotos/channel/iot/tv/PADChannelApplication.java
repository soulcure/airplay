package swaiotos.channel.iot.tv;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.uuzuche.lib_zxing.activity.ZXingLibrary;

/**
 * @ProjectName: iot-channel-tv
 * @Package: swaiotos.channel.iot.tv
 * @ClassName: AiotApplication
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/9 16:56
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/9 16:56
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class PADChannelApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ZXingLibrary.initDisplayOpinion(this);

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                Log.e("yao", "uncaughtException");
            }
        });

    }


}

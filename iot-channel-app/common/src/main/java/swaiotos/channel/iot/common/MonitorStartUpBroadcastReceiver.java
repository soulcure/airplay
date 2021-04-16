package swaiotos.channel.iot.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.ss.SSAdminChannel;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.common
 * @ClassName: MonitorStartUpBroadcastReceiver
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/4/30 11:13
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/4/30 11:13
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MonitorStartUpBroadcastReceiver extends BroadcastReceiver {
    private final String TAG = MonitorStartUpBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG," MonitorStartUpBroadcastReceiver onReceive start----");

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    IOTAdminChannel.mananger.open(context.getApplicationContext(), context.getApplicationContext().getPackageName(), new IOTAdminChannel.OpenCallback() {
                        @Override
                        public void onConntected(SSAdminChannel channel) {
                            Log.d(TAG," MonitorStartUpBroadcastReceiver onReceive onConntected");
                        }

                        @Override
                        public void onError(String s) {
                            Log.d(TAG," MonitorStartUpBroadcastReceiver onReceive onError");
                        }
                    });
                }
            }).start();

        }
    }

}

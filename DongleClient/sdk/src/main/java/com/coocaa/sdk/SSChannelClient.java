package com.coocaa.sdk;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;

import com.coocaa.sdk.entity.IMMessage;

public class SSChannelClient {
    public static final String DEFAULT_ACTION = "swaiotos.intent.action.channel.iot.SSCLIENT";
    public static final String META_ID = "ss-clientID";
    public static final String META_KEY = "ss-clientKey";
    public static final String META_VERSION = "ss-clientVersion";

    static final String KEY_MESSAGE = "message";

    public static IMMessage parseMessage(Intent intent) {
        return intent.getParcelableExtra(KEY_MESSAGE);
    }

    /**
     * @ClassName: SSChannelClientService
     * @Author: lu
     * @CreateDate: 2020/3/21 3:36 PM
     * @Description: 响应智屏消息的客户端服务
     * <p>
     * 每个apk可以有一个或多个此服务，按业务结构来设计即可。
     * <p>
     * 每个服务的注册步骤：
     * 1、新建Service  extends SSChannelClientService
     * <p>
     * 2、Manifest中对应的Service增加intent-filter
     * <intent-filter>
     * <action android:name="swaiotos.intent.action.channel.iot.SSCLIENT" />
     * </intent-filter>
     * <p>
     * 3、Manifest中对应的Service增加metadata
     * <meta-data
     * android:name="ss-clientID"
     * android:value="swaiotos.channel.iot.tv.demo1" />
     * <meta-data
     * android:name="ss-clientKey"
     * android:value="key~1234567" />
     * <meta-data
     * android:name="ss-clientVersion"
     * android:value="1" />
     * <p>
     * <p>
     * example:
     * <service
     * android:name=".demo1.SSClient1Service"
     * android:enabled="true"
     * android:exported="true"
     * android:process=":demo1">
     * <intent-filter>
     * <action android:name="swaiotos.intent.action.channel.iot.SSCLIENT" />
     * </intent-filter>
     * <p>
     * <meta-data
     * android:name="ss-clientID"
     * android:value="swaiotos.channel.iot.tv.demo1" />
     * <meta-data
     * android:name="ss-clientKey"
     * android:value="key~1234567" />
     * <meta-data
     * android:name="ss-clientVersion"
     * android:value="1" />
     * </service>
     */
    public abstract static class SSChannelClientService extends IntentService {
        public SSChannelClientService(String name) {
            super(name);
        }

        @Override
        public void onCreate() {
            super.onCreate();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                NotificationChannel channel = new NotificationChannel("40", "App Service", NotificationManager.IMPORTANCE_DEFAULT);
                channel.setSound(null, null);

                //数字是随便写的“40”，
                nm.createNotificationChannel(channel);
                Notification.Builder builder = new Notification.Builder(this, "40");

                //其中的2，是也随便写的，正式项目也是随便写
                startForeground(2, builder.build());
            }
        }

        @Override
        protected final void onHandleIntent(Intent intent) {
            IMMessage message = parseMessage(intent);
            handleIMMessage(message);
        }

        protected abstract void handleIMMessage(IMMessage message);
    }
}

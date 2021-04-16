package swaiotos.channel.iot.ss;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;

import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.utils.ipc.ParcelableBinder;

/**
 * @ClassName: SSChannelClient
 * @Author: lu
 * @CreateDate: 2020/4/7 8:49 PM
 * @Description:
 */
public class SSChannelClient {
    public static final String DEFAULT_ACTION = "swaiotos.intent.action.channel.iot.SSCLIENT";
    public static final String META_ID = "ss-clientID";
    public static final String META_KEY = "ss-clientKey";
    public static final String META_VERSION = "ss-clientVersion";

    static final String KEY_MESSAGE = "message";
    static final String KEY_BINDER = "binder";

    public static IMMessage parseMessage(Intent intent) {
        return intent.getParcelableExtra(KEY_MESSAGE);
    }

    public static SSChannel parseChannel(Intent intent) {
        try {
            ParcelableBinder binder = intent.getParcelableExtra(KEY_BINDER);
            ISSChannelService service = ISSChannelService.Stub.asInterface(binder.mBinder);
            SSChannel channel = new SSChannelImpl();
            channel.open(service);
            return channel;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @ClassName: SSChannelClientActivity
     * @Author: lu
     * @CreateDate: 2020/3/21 3:36 PM
     * @Description: 响应智屏消息的客户端Activity
     * <p>
     * 每个apk可以有一个或多个此响应智屏消息的客户端Activity，按业务结构来设计即可。
     * <p>
     * 每个服务的注册步骤：
     * 1、新建Activity  extends SSChannelClientActivity
     * <p>
     * 2、Manifest中对应的Service增加intent-filter
     * <intent-filter>
     * <action android:name="swaiotos.intent.action.channel.iot.SSCLIENT" />
     * </intent-filter>
     * <p>
     * 3、Manifest中对应的Activity增加metadata
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
     * <activity
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
     * </activity>
     */
    public static abstract class SSChannelClientActivity extends Activity {
        private SSChannel mSSChannel;
        private Intent mIntent;

        @Override
        protected void onNewIntent(Intent intent) {
            super.onNewIntent(intent);
            setIntent(intent);
        }

        @Override
        protected void onResume() {
            super.onResume();
            Intent intent = getIntent();
            if (intent != null && intent != mIntent) {
                mIntent = intent;
                parseIntent(mIntent);
            }
        }

        private void parseIntent(Intent intent) {
            if (intent == null) {
                return;
            }
            IMMessage message = parseMessage(intent);
            if (mSSChannel == null) {
                mSSChannel = parseChannel(intent);
            }
            if (mSSChannel != null) {
                handleIMMessage(message, mSSChannel);
            }
        }

        protected abstract boolean handleIMMessage(IMMessage message, SSChannel channel);
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

        private SSChannel mSSChannel;

        public SSChannelClientService(String name) {
            super(name);
        }

        @Override
        public void onCreate() {
            super.onCreate();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                NotificationChannel channel = new NotificationChannel("40", "App Service", NotificationManager.IMPORTANCE_LOW);
                channel.setSound(null, null);

                //数字是随便写的“40”，
                nm.createNotificationChannel(channel);
                Notification.Builder builder = new Notification.Builder(this, "40");
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "40");

                //其中的2，是也随便写的，正式项目也是随便写
                startForeground(2, builder.build());
            }
        }

        @Override
        protected final void onHandleIntent(Intent intent) {
            try {
                IMMessage message = parseMessage(intent);
                if (mSSChannel == null) {
                    mSSChannel = parseChannel(intent);
                }
                if (mSSChannel != null) {
                    handleIMMessage(message, mSSChannel);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected abstract boolean handleIMMessage(IMMessage message, SSChannel channel);
    }
}

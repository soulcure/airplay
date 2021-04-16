package swaiotos.channel.iot.common;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import swaiotos.channel.iot.IOTAdminChannel;
import swaiotos.channel.iot.common.utils.Constants;
import swaiotos.channel.iot.common.utils.FileAccessTokenUtils;
import swaiotos.channel.iot.ss.SSAdminChannel;
import swaiotos.channel.iot.ss.SSChannelService;
import swaiotos.channel.iot.ss.server.ShareUtls;
import swaiotos.channel.iot.utils.AndroidLog;

/**
 * @ProjectName: iot-channel-app
 * @Package: swaiotos.channel.iot.common
 * @ClassName: ProvisionServices
 * @Description: java类作用描述
 * @Author: wangyuehui
 * @CreateDate: 2020/5/27 11:25
 * @UpdateUser: 更新者
 * @UpdateDate: 2020/5/27 11:25
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class ProvisionService extends IntentService {

    private static final String TAG = ProvisionService.class.getSimpleName();
    private AtomicBoolean mAtomicBoolean = new AtomicBoolean(true);

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ProvisionService(String name) {
        super(name);
    }

    public ProvisionService() {
        this(TAG);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) { // 注意notification也要适配Android 8 哦
            startForeground(android.os.Process.myPid(), getNotification(this));// 通知栏标识符 前台进程对象唯一ID
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try{
            if (intent != null) {
                //add ASH-5992
                int intentType = intent.getIntExtra(Constants.COOCAA_PROVISION_SERVICE_TYPE,-1);
                Log.d(TAG,"ProvisionService intent type:"+intentType);
                if (intentType == 99) {
                    int count = 0;
                    IOTAdminChannel.mananger.open(getApplicationContext(), getPackageName(), new IOTAdminChannel.OpenCallback() {
                        @Override
                        public void onConntected(SSAdminChannel channel) {
                            Log.d(TAG,"ProvisionService open success");
                            mAtomicBoolean.compareAndSet(true,false);

                            sendCoreData();

                        }

                        @Override
                        public void onError(String s) {
                            Log.d(TAG,"ProvisionService open onError");
                            mAtomicBoolean.compareAndSet(true,false);
                        }
                    });
                    while (mAtomicBoolean.get()) {
                        count ++ ;
                        Thread.sleep(1000);
                        if (count == 10) {
                            break;
                        }
                    }
                    return;
                }
            }

            Bundle bundle = intent.getBundleExtra(Constants.SYSTEM_INTENT_BUNDLE);

            int type = bundle.getInt(Constants.COOCAA_PROVISION_SERVICE_TYPE, 0);
            Log.d(TAG,"ProvisionService bundle type:"+type);
            Messenger messenger = bundle.getParcelable(Constants.SYSTEM_START_MESSENGER);
            if (messenger == null)
                Toast.makeText(getApplicationContext(), "messenger is null,please transmit the messenger", Toast.LENGTH_LONG).show();

            if (type == 1) {
                String mAccessToken = "";
                try {
                    if (new File(getApplicationContext().getFilesDir(),Constants.COOCAA_FILE_ACCESSTOKEN_NAME).exists()) {
                        //首先读取文件中的accessToken
                        mAccessToken = FileAccessTokenUtils.getDataFromFile(getApplicationContext(),Constants.COOCAA_FILE_ACCESSTOKEN_NAME);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //为空情况读取SharedPreferences
                if (TextUtils.isEmpty(mAccessToken)) {
                    mAccessToken = ShareUtls.getInstance(getApplicationContext()).getString(Constants.COOCAA_PREF_ACCESSTOKEN,"");
                }

                Message msg = Message.obtain();
                msg.what = 10000;
                Bundle bundle2 = new Bundle();
                bundle2.putString(Constants.COOCAA_PREF_ACCESSTOKEN,mAccessToken);
                msg.setData(bundle2);

                try {
                    messenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendCoreData() {
        try {
            Intent intent = new Intent();
            intent.setPackage("com.skyworth.smarthome_tv");
            intent.setAction("swaiotos.channel.iot.intent.flush.data");
            sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
            AndroidLog.androidLog("sendCoreData"+e.getLocalizedMessage());
        }
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private static Notification getNotification(Service service) {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) service.getSystemService(NOTIFICATION_SERVICE);
            //数字是随便写的“40”，
            nm.createNotificationChannel(new NotificationChannel("23", "App Service", NotificationManager.IMPORTANCE_NONE));
            builder = new Notification.Builder(service, "23");
        } else {
            builder = new Notification.Builder(service);
        }
        builder.setContentTitle("服务运行于前台")
                .setContentText("service被设为前台进程")
                .setTicker("service正在后台运行...")
                .setPriority(Notification.PRIORITY_MAX)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        return notification;
    }
}

package swaiotos.channel.iot.ss;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class KeepAliveHelperService extends IntentService {
    public static void keep(Service service) {
        try {
            int id = android.os.Process.myPid();

            //判断是否是酷开系统
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                service.startForegroundService(new Intent(service, service.getClass()));
            } else {
                service.startService(new Intent(service, service.getClass()));
            }

            service.startForeground(id, getNotification(service));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                return;
            }
            removeNotification(service.getApplicationContext(), id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void removeNotification(Context context, int id) {
        Intent intent = new Intent();
        intent.setClass(context, KeepAliveHelperService.class);
        intent.putExtra("id", id);
        context.startService(intent);
    }

    public KeepAliveHelperService() {
        super("keep-alive-helper-service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int id = intent.getIntExtra("id", android.os.Process.myPid());
        startForeground(id, getNotification(KeepAliveHelperService.this));
        stopForeground(true);
    }

    private static Notification getNotification(Service service) {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) service.getSystemService(NOTIFICATION_SERVICE);
            //数字是随便写的“40”，
            nm.createNotificationChannel(new NotificationChannel("23", "App Service", NotificationManager.IMPORTANCE_LOW));
            builder = new Notification.Builder(service, "23");
        } else {
            builder = new Notification.Builder(service);
        }
        builder.setContentTitle("服务运行于前台")
                .setContentText("service被设为前台进程")
                .setTicker("service正在后台运行...")
                .setPriority(Notification.PRIORITY_LOW)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        return notification;
    }
}

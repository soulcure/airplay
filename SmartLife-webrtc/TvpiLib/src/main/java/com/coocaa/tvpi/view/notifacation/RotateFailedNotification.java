package com.coocaa.tvpi.view.notifacation;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.coocaa.tvpi.module.homepager.RotateScreenFailedActivity;
import com.coocaa.tvpilib.R;


/**
 * 旋转失败通知
 * Created by songxing on 2020/3/10
 */
public class RotateFailedNotification {
    private static final int ROTATE_NOTIFY_ID = 111;

    private Context context;
    private NotificationManager notificationManager;
    private Notification rotateFailedNotification;

    private String deviceName;

    public RotateFailedNotification(Context context) {
        this.context = context;
    }

    public void init(String deviceName) {
        this.deviceName = deviceName;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannelCompat26.createNIMMessageNotificationChannel(context);
    }


    private void buildRotateNotification() {
        if (rotateFailedNotification == null) {
            Intent localIntent = new Intent();
            localIntent.setClass(context, RotateScreenFailedActivity.class);
            localIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            String tickerText = deviceName + "旋转失败，请检查";
            int iconId = R.drawable.logo;
            PendingIntent pendingIntent = PendingIntent.getActivity(context, ROTATE_NOTIFY_ID,
                    localIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rotateFailedNotification = makeNotification(pendingIntent,tickerText, "",
                    tickerText, iconId, false, false);
        }
    }


    private Notification makeNotification(PendingIntent pendingIntent, String title, String content,
                                          String tickerText, int iconId, boolean ring, boolean vibrate) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                NotificationChannelCompat26.getNIMChannelId(context));
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setTicker(tickerText)
                .setSmallIcon(iconId);
        int defaults = Notification.DEFAULT_LIGHTS;
        if (vibrate) {
            defaults |= Notification.DEFAULT_VIBRATE;
        }
        if (ring) {
            defaults |= Notification.DEFAULT_SOUND;
        }
        builder.setDefaults(defaults);
        return builder.build();
    }

    public void activeRotateNotification(boolean active) {
        if (notificationManager != null) {
            if (active) {
                buildRotateNotification();
                notificationManager.notify(ROTATE_NOTIFY_ID, rotateFailedNotification);
            } else {
                notificationManager.cancel(ROTATE_NOTIFY_ID);
            }
        }
    }
}

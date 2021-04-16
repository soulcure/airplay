package swaiotos.runtime.h5.core.os.exts.channel;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;

import swaiotos.runtime.h5.core.os.exts.utils.ExtLog;
import swaiotos.runtime.h5.remotectrl.State;

public class StateIntentService extends IntentService {
    private static final String TAG = "StateIntentService";
    private static final String ACTION_STATE = "swaiotos.intent.action.channel.app_status.changed";

    public StateIntentService() {
        super("StateIntentService");
    }

//    public static void notifyState(Context context, String param1, String param2) {
//        Intent intent = new Intent(context, StateIntentService.class);
//        intent.setAction(ACTION_STATE);
//        intent.putExtra(EXTRA_PARAM1, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
//        context.startService(intent);
//    }


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
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "40");

            //其中的2，是也随便写的，正式项目也是随便写
            startForeground(2, builder.build());
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_STATE.equals(action)) {
                handleState(intent.getStringExtra("state"));
            }
        }
    }

    private void handleState(String state) {
        if (TextUtils.isEmpty(state)) {
            ExtLog.w(TAG, "onStartCommand, state == null");
        } else {
            ExtLog.d(TAG, "onStartCommand, state : " + state);
            State stateData = State.decode(state);
            if (stateData == null) {
                ExtLog.w(TAG, "onStartCommand, stateData == null");
            } else {
                EventBus.getDefault().post(stateData);
            }
        }
    }
}
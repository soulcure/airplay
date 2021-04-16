package com.coocaa.swaiotos.virtualinput;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.coocaa.tvpi.module.io.HomeIOThread;

/**
 * @Author: yuzhan
 */
public class VirtualInputKeepAliveManager {

    final String TAG = "KeepAliveH5";
    private Context context;

    private final static VirtualInputKeepAliveManager instance = new VirtualInputKeepAliveManager();

    private VirtualInputKeepAliveManager(){}

    public static VirtualInputKeepAliveManager getInstance() {
        return instance;
    }

    public void start(Context context) {
        this.context = (context instanceof Application)? context : context.getApplicationContext();
        bindService();
    }

    private void bindService() {
        HomeIOThread.removeTask(bindRunnable);
        HomeIOThread.execute(1000, bindRunnable);
    }

    private Runnable bindRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Log.d(TAG, "start bind.");
                Intent intent = new Intent();
                intent.setPackage(context.getPackageName());
                intent.setAction("coocaa.intent.action.virtualinput.keep_alive");
                context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(TAG, "onServiceConnected ++ : " + name);
            if(binder != null) {
                try {
                    binder.linkToDeath(deathRecipient, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected -- : " + name);
        }
    };

    private IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {

        @Override
        public void binderDied() {
            Log.d(TAG, "binderDied ##");
            bindService();
        }
    };
}

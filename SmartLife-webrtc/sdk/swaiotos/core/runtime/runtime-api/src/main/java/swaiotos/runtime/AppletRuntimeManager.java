package swaiotos.runtime;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * @ClassName: AppletRuntimeManager
 * @Author: lu
 * @CreateDate: 2020/10/24 4:34 PM
 * @Description:
 */
public class AppletRuntimeManager {
    private static AppletRuntimeManager manager;
    private Context context;
    public static ClassLoader classLoader;

    public static synchronized AppletRuntimeManager get(Context context) {
        if (manager == null) {
            manager = new AppletRuntimeManager(context);
        }
        return manager;
    }

    private static final String ACTION = "swaiotos.intent.action.SERVICE_APPLET_RUNTIME";

    private IAppletRuntimeService service;

    private AppletRuntimeManager(Context context) {
        this.context = context;
        classLoader = context.getClassLoader();
        bindService();
    }

    private void bindService() {
        Intent intent = new Intent(ACTION);
        intent.setPackage(context.getPackageName());
        Log.d("Runtime", "AppletRuntimeManager start bind service, action=" + ACTION);
        context.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d("Runtime", "AppletRuntimeManager onServiceConnected : " + componentName);
                service = IAppletRuntimeService.Stub.asInterface(iBinder);
                try {
                    iBinder.linkToDeath(deathRecipient, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d("Runtime", "AppletRuntimeManager onServiceDisconnected ... : " + componentName);
            }
        }, Context.BIND_AUTO_CREATE);
    }

    private IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d("Runtime", "AppletRuntimeManager binderDied : ");
            if(service != null) {
                service.asBinder().unlinkToDeath(this, 0);
                service = null;
            }
            bindService();
        }
    };

    public boolean initDone() {
        return service != null;
    }

    public boolean startApplet(Uri applet) {
        if(service == null) {
            bindService();
            return false;
        }
        try {
            int r = service.startApplet(applet);
            return r == 0;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean startApplet(Uri applet, Bundle bundle) {
        if(service == null) {
            bindService();
            return false;
        }
        try {
            int r = service.startApplet2(applet, bundle);
            return r == 0;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }
}

package swaiotos.runtime.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import swaiotos.runtime.Applet;
import swaiotos.runtime.IAppletRuntimeService;
import swaiotos.runtime.base.AppletRunner;
import swaiotos.runtime.h5.H5AppletRunner;
import swaiotos.runtime.np.NPAppletRunner;

import static swaiotos.runtime.Applet.APPLET_H;
import static swaiotos.runtime.Applet.APPLET_MP;
import static swaiotos.runtime.Applet.APPLET_NP;

public class AppletRuntimeService extends Service {
    private static class AppletRuntimeServiceImpl extends IAppletRuntimeService.Stub {
        private Context mContext;

        public AppletRuntimeServiceImpl(Context context) {
            super();
            mContext = context;
        }

        @Override
        public int startApplet(Uri applet) throws RemoteException {
            return startApplet2(applet, null);
        }

        @Override
        public int startApplet2(Uri applet, Bundle bundle) throws RemoteException {
            Applet applet1 = Applet.Builder.parse(applet);
            String scheme = applet1.getType();
            if (TextUtils.isEmpty(scheme)) {
                return -1;
            }
            AppletRunner runner = null;
            if (APPLET_H.contains(scheme)) {
                runner = H5AppletRunner.get();
            } else if (APPLET_MP.equals(scheme)) {
//                runner = MPAppletRunner.get();
            } else if (APPLET_NP.equals(scheme)) {
                runner = NPAppletRunner.get();
            } else {
                return -2;
            }
            if (runner != null) {
                try {
                    runner.start(mContext, applet1, bundle);
                    return 0;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return -3;
        }
    }

    private IAppletRuntimeService.Stub impl;

    @Override
    public void onCreate() {
        super.onCreate();
        impl = new AppletRuntimeServiceImpl(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return impl;
    }
}

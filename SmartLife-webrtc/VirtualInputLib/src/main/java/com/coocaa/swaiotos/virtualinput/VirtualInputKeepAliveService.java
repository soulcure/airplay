package com.coocaa.swaiotos.virtualinput;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * 用来给h5保活的
 * @Author: yuzhan
 */
public class VirtualInputKeepAliveService extends Service {

    final String TAG = "KeepAliveH5";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate..");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy..");
        super.onDestroy();
    }

//    private IVirtualInputManagerService.Stub stub = new IVirtualInputManagerService.Stub() {
//
//        @Override
//        public IVirtualInput getVirtualInput() throws RemoteException {
//            return null;
//        }
//
//        @Override
//        public void addListener(IVirtualInputManagerListener listener) throws RemoteException {
//
//        }
//
//        @Override
//        public void setState(int state, String url) throws RemoteException {
//
//        }
//    };
}

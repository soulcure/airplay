package com.threesoft.webrtc.webrtcroom;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import androidx.annotation.NonNull;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.threesoft.webrtc.webrtcroom.webrtcmodule.WebRtcClient2;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.util.List;

public class MyApplication extends Application {
    private final static String TAG = "MyApplication";
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate");
        Thread.setDefaultUncaughtExceptionHandler(new OwnUncaughtExceptionHandler());

        openPermisission1();
        WebRtcClient2.getInstance().init(MyApplication.this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG,"onCreate");
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }


    private void openPermisission1(){
        if(AndPermission.hasPermissions(this,Permission.Group.STORAGE)){
            openLocationPermission2();
        }else {
            AndPermission.with(this)
                    .runtime()
                    .permission(Permission.Group.STORAGE)
                    .onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            openLocationPermission2();
                        }
                    })
                    .onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            //???????????????????????????????????????????????????
                            //Toast.makeText(MapActivity.this, "????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                        }
                    }).start();
        }
    }

    private void openLocationPermission2(){
        if(AndPermission.hasPermissions(this, Permission.Group.LOCATION)){
            //?????????????????????
            SDKInitializer.initialize(getApplicationContext());
            //??????BD09LL???GCJ02????????????????????????BD09LL?????????
            SDKInitializer.setCoordType(CoordType.BD09LL);
        }else{
            AndPermission.with(this)
                    .runtime()
                    .permission(Permission.Group.LOCATION)
                    .onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            //??????????????????
                            //?????????????????????
                            SDKInitializer.initialize(getApplicationContext());
                            //??????BD09LL???GCJ02????????????????????????BD09LL?????????
                            SDKInitializer.setCoordType(CoordType.BD09LL);
                        }
                    })
                    .onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            //???????????????????????????????????????????????????
                            //Toast.makeText(MapActivity.this, "????????????????????????????????????????????????", Toast.LENGTH_SHORT).show();
                        }
                    }).start();
        }
    }

    public class OwnUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
            StackTraceElement [] elements = throwable.getStackTrace();
            StringBuilder reason =new StringBuilder(throwable.toString());
            if (elements !=null && elements.length >0) {

                for (StackTraceElement element : elements) {

                    reason.append("\n");
                    reason.append(element.toString());
                }
                Log.e("zyq", reason.toString());
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
    }


}

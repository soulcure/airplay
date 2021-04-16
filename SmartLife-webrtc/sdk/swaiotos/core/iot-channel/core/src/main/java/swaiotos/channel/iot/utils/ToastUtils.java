package swaiotos.channel.iot.utils;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ToastUtils {
    private static Object iNotificationManagerObj;
    private Handler mHandler;


    private static ToastUtils instance;


    public static ToastUtils instance() {
        if (instance == null) {
            instance = new ToastUtils();
        }
        return instance;
    }


    private ToastUtils() {
        mHandler = new Handler(Looper.getMainLooper());
    }


    /**
     * @param context
     */
    public void showToast(final Context context, final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {  //android 8.0 && tv
                    show(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
                } else {
                    Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * @param context
     * @param message
     */
    private void show(Context context, String message, int duration) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        //后setText 兼容小米默认会显示app名称的问题
        Toast toast = Toast.makeText(context, null, duration);
        toast.setText(message);
        showSystemToast(toast);
    }

    /**
     * 显示系统Toast
     */
    private void showSystemToast(Toast toast) {
        try {
            Method getServiceMethod = Toast.class.getDeclaredMethod("getService");
            getServiceMethod.setAccessible(true);
            //hook INotificationManager
            if (iNotificationManagerObj == null) {
                iNotificationManagerObj = getServiceMethod.invoke(null);

                Class iNotificationManagerCls = Class.forName("android.app.INotificationManager");
                Object iNotificationManagerProxy = Proxy.newProxyInstance(toast.getClass().getClassLoader(), new Class[]{iNotificationManagerCls}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        //强制使用系统Toast
                        if ("enqueueToast".equals(method.getName())
                                || "enqueueToastEx".equals(method.getName())) {  //华为p20 pro上为enqueueToastEx
                            args[0] = "android";
                        }
                        return method.invoke(iNotificationManagerObj, args);
                    }
                });
                Field sServiceFiled = Toast.class.getDeclaredField("sService");
                sServiceFiled.setAccessible(true);
                sServiceFiled.set(null, iNotificationManagerProxy);
            }
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
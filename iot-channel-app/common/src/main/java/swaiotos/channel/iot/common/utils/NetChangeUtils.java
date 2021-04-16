package swaiotos.channel.iot.common.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.RemoteException;
import android.util.Log;

import swaiotos.channel.iot.common.SendLetterServiceImpl;
import swaiotos.channel.iot.utils.AndroidLog;
import swaiotos.channel.iot.utils.NetUtils;

public class NetChangeUtils {


    public interface NetworkReceiverCallback {
        void onConnected();

        void onDisconnected();
    }

    public abstract static class NetworkChangeReceiver extends BroadcastReceiver implements NetworkReceiverCallback {


        public static void register(Context context, NetworkChangeReceiver networkChangeReceiver) {
            IntentFilter intentFilter = new IntentFilter();
            // 添加广播值
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            // 注册广播
            context.registerReceiver(networkChangeReceiver, intentFilter);
            Log.d("新新网络", "register: ");
        }


        public static void unregister(Context context, NetworkChangeReceiver receiver) {
            try {
                context.unregisterReceiver(receiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取管理网络连接的系统服务类的实例
            ConnectivityManager connectivityManager = (ConnectivityManager) context.
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            // 判断网络是否可用
            if (networkInfo != null && networkInfo.isAvailable()) {
                Log.d("TAG", "onReceive: 新网络可用");
                onConnected();
//                AndroidLog.androidLog("新网络连接回调---onConnected---loadInfo---1--");
//                //去除首次配网连接上更新信息，由绑定服务方式加载触发刷新二维码
//                if (myBinder != null && isOpenSuccess) {
//                    try {
//                        myBinder.loadInfo(1);
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//                }
            } else {
                Log.d("TAG", "onReceive: 新网络不可用");
                onDisconnected();
//                AndroidLog.androidLog("新网络断网回调---onDisconnected---loadInfo---1--");
//                if (myBinder != null) {
//                    try {
//                        myBinder.loadInfo(1);
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        }
    }
}

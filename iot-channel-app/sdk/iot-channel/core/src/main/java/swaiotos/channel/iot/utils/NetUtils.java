package swaiotos.channel.iot.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * @ClassName: NetUtils
 * @Author: lu
 * @CreateDate: 2020/4/13 5:36 PM
 * @Description:
 */
public class NetUtils {
    public interface NetworkReceiverCallback {
        void onConnected();

        void onDisconnected();
    }

    public static abstract class NetworkReceiver extends BroadcastReceiver implements NetworkReceiverCallback {
        public static void register(Context context, NetworkReceiver receiver) {

            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeInfo = manager.getActiveNetworkInfo();
            if (activeInfo != null) {
                receiver.mType = activeInfo.getType();
            }

            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            context.registerReceiver(receiver, filter);
        }

        public static void unregister(Context context, NetworkReceiver receiver) {
            try {
                context.unregisterReceiver(receiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private boolean bInit = false;
        private int mType = -1;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!bInit) {
                bInit = true;
                return;
            }
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeInfo = manager.getActiveNetworkInfo();
            Log.d("NI", "ni:" + activeInfo + " mType:" + mType);
            if (activeInfo != null) {
                Log.d("NI", "mType:" + mType + " ni-type:" + activeInfo.getType() + "  name:" + activeInfo.getTypeName());
                int type = activeInfo.getType();
                if (mType != type) {
                    mType = type;
//                    onDisconnected();
                    onConnected();
                }
            } else {
                if (mType != -1) {
                    mType = -1;
                    onDisconnected();
                }
            }
        }
    }

    public static String getLocalAddress(Context context) {
        String ip = DeviceUtil.getLocalIPAddress(context);
        if (ip == null) {
            ip = "";
        }

        Log.d("yao", "getLocalAddress:" + ip);
        return ip;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (EmptyUtils.isNotEmpty(networkInfo)) {
            return networkInfo.isConnected();
        }
        return false;
    }
}

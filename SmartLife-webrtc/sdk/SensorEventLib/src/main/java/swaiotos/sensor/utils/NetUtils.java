package swaiotos.sensor.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @Author: yuzhan
 */
public class NetUtils {

    public static String getIP(Context context) {
        String ip = "0.0.0.0";
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        int type = info.getType();
        if (type == ConnectivityManager.TYPE_ETHERNET) {
            Log.d("SSCServer", "connect ethernet.");
            ip = getEthernetIP();
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            Log.d("SSCServer", "connect wifi.");
            ip = getWifiIP(context);
        }
        Log.d("SSCServer", "getIp : " + ip);
        return ip;
    }

    public static String getEthernetIP() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return "0.0.0.0";
    }

    public static String getWifiIP(Context context) {
        android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(android.content.Context.WIFI_SERVICE);
        WifiInfo wifiinfo = wifi.getConnectionInfo();
        int intaddr = wifiinfo.getIpAddress();
        byte[] byteaddr = new byte[] { (byte) (intaddr & 0xff),
                (byte) (intaddr >> 8 & 0xff), (byte) (intaddr >> 16 & 0xff),
                (byte) (intaddr >> 24 & 0xff) };
        InetAddress addr = null;
        try {
            addr = InetAddress.getByAddress(byteaddr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String mobileIp = addr.getHostAddress();
        return mobileIp;
    }
}

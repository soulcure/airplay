package swaiotos.channel.iot.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @ClassName: DeviceUtil
 * @Author: AwenZeng
 * @CreateDate: 2020/3/19 10:38
 * @Description: 设备信息工具类
 */
public class DeviceUtil {
    public static String getNetworkType(Context context) {
        String type = "UNKNOWN";
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {     // wifi
                type = "WIFI";
            } else if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {    //有线
                type = "ETHERNET";
            }
        }
        Log.d("yao", "getNetworkType = " + type);
        return type;
    }


    public static String getLocalIPAddress(Context context) {
        String ip = getEthIPAddress();

        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {    //3G/4G网络
                Log.d("yao", "getLocalIPAddress ConnectivityManager.TYPE_MOBILE---");
                try {
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                ip = inetAddress.getHostAddress();
                                break;
                            }
                        }
                    }
                } catch (SocketException e) {
                    Log.e("yao", "获取3G/4G网络IP失败");
                    ip = null;
                }
            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {     // wifi
                Log.d("yao", "getLocalIPAddress ConnectivityManager.TYPE_WIFI---");
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                ip = intToIp(wifiInfo.getIpAddress());
            } else if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {    //有线
                Log.d("yao", "getLocalIPAddress ConnectivityManager.TYPE_ETHERNET(有线)---");
                ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                Network network;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    network = mConnectivityManager.getActiveNetwork();
                    LinkProperties linkProperties = mConnectivityManager.getLinkProperties(network);
                    for (LinkAddress linkAddress : linkProperties.getLinkAddresses()) {
                        InetAddress address = linkAddress.getAddress();
                        if (address instanceof Inet4Address) {
                            ip = address.getHostAddress();
                            break;
                        }
                    }
                }
            }
        }


        if (TextUtils.isEmpty(ip) || TextUtils.equals("0.0.0.0", ip)) {
            ip = getWifiIPAddress(context);
        }

        Log.d("yao", "getLocalIPAddress = " + ip);
        return ip;
    }


    public static String getWifiIPAddress(Context context) {
        Log.d("yao", "getWifiIPAddress---");
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            return null;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return intToIp(ipAddress);
    }

    public static String getEthIPAddress() {
        try {
            Log.d("yao", "getEthIPAddress---");
            Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip = null;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                String interfaceName = netInterface.getDisplayName();
                if (interfaceName.equals("eth0")) {
                    Enumeration addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = (InetAddress) addresses.nextElement();
                        if (ip instanceof Inet4Address && !ip.getHostAddress().contains("127.0.0.1")) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }
}

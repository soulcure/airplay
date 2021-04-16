package com.coocaa.tvpi.util;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by dvlee1 on 11/19/15.
 */
public class NetworkUtil {
    public static final String NETWORK_TYPE_WIFI = "wifi";
    public static final String NETWORK_TYPE_3G = "eg";
    public static final String NETWORK_TYPE_2G = "2g";
    public static final String NETWORK_TYPE_WAP = "wap";
    public static final String NETWORK_TYPE_UNKNOWN = "unknown";
    public static final String NETWORK_TYPE_DISCONNECT = "disconnect";

    public static final int NETWORK_WIFI = 1;    // wifi network
    public static final int NETWORK_4G = 4;    // "4G" networks
    public static final int NETWORK_3G = 3;    // "3G" networks
    public static final int NETWORK_2G = 2;    // "2G" networks
    public static final int NETWORK_UNKNOWN = 5;    // unknown network
    public static final int NETWORK_NO = -1;   // no network

    private static final int NETWORK_TYPE_GSM = 16;
    private static final int NETWORK_TYPE_TD_SCDMA = 17;
    private static final int NETWORK_TYPE_IWLAN = 18;

    /**
     * 打开网络设置界面
     * <p>3.0以下打开设置界面</p>
     *
     * @param context 上下文
     */
    public static void openWirelessSettings(Context context) {
        if (Build.VERSION.SDK_INT > 10) {
            context.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
        } else {
            context.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        }
    }

    /**
     * 获取活动网路信息
     *
     * @param context 上下文
     * @return NetworkInfo
     */
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * 判断网络是否可用
     * <p>需添加权限 android.permission.ACCESS_NETWORK_STATE</p>
     */
    public static boolean isAvailable(Context context) {
        NetworkInfo info = getActiveNetworkInfo(context);
        return info != null && info.isAvailable();
    }

    /**
     * 判断网络是否连接
     * <p>需添加权限 android.permission.ACCESS_NETWORK_STATE</p>
     *
     * @param context 上下文
     * @return true: 是<br>false: 否
     */
    public static boolean isConnected(Context context) {
        NetworkInfo info = getActiveNetworkInfo(context);
        return info != null && info.isConnected();
    }

    /**
     * 判断网络是否是4G
     * <p>需添加权限 android.permission.ACCESS_NETWORK_STATE</p>
     *
     * @param context 上下文
     * @return true: 是<br>false: 不是
     */
    public static boolean is4G(Context context) {
        NetworkInfo info = getActiveNetworkInfo(context);
        return info != null && info.isAvailable() && info.getSubtype() == TelephonyManager.NETWORK_TYPE_LTE;
    }

    /**
     * 判断wifi是否连接状态
     * <p>需添加权限 android.permission.ACCESS_NETWORK_STATE</p>
     *
     * @param context 上下文
     * @return true: 连接<br>false: 未连接
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 获取移动网络运营商名称
     * <p>如中国联通、中国移动、中国电信</p>
     *
     * @param context 上下文
     * @return 移动网络运营商名称
     */
    public static String getNetworkOperatorName(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null ? tm.getNetworkOperatorName() : null;
    }

    /**
     * 获取移动终端类型
     *
     * @param context 上下文
     * @return 手机制式
     * <ul>
     * <li>PHONE_TYPE_NONE  : 0 手机制式未知</li>
     * <li>PHONE_TYPE_GSM   : 1 手机制式为GSM，移动和联通</li>
     * <li>PHONE_TYPE_CDMA  : 2 手机制式为CDMA，电信</li>
     * <li>PHONE_TYPE_SIP   : 3</li>
     * </ul>
     */
    public static int getPhoneType(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null ? tm.getPhoneType() : -1;
    }


    /**
     * 获取当前的网络类型(WIFI,2G,3G,4G)
     * <p>需添加权限 android.permission.ACCESS_NETWORK_STATE</p>
     *
     * @param context 上下文
     * @return 网络类型
     * <ul>
     * <li>NETWORK_WIFI    = 1;</li>
     * <li>NETWORK_4G      = 4;</li>
     * <li>NETWORK_3G      = 3;</li>
     * <li>NETWORK_2G      = 2;</li>
     * <li>NETWORK_UNKNOWN = 5;</li>
     * <li>NETWORK_NO      = -1;</li>
     * </ul>
     */
    public static int getNetWorkType(Context context) {
        int netType = NETWORK_NO;
        NetworkInfo info = getActiveNetworkInfo(context);
        if (info != null && info.isAvailable()) {

            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                netType = NETWORK_WIFI;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (info.getSubtype()) {

                    case NETWORK_TYPE_GSM:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        netType = NETWORK_2G;
                        break;

                    case NETWORK_TYPE_TD_SCDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        netType = NETWORK_3G;
                        break;

                    case NETWORK_TYPE_IWLAN:
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        netType = NETWORK_4G;
                        break;
                    default:

                        String subtypeName = info.getSubtypeName();
                        if (subtypeName.equalsIgnoreCase("TD-SCDMA")
                                || subtypeName.equalsIgnoreCase("WCDMA")
                                || subtypeName.equalsIgnoreCase("CDMA2000")) {
                            netType = NETWORK_3G;
                        } else {
                            netType = NETWORK_UNKNOWN;
                        }
                        break;
                }
            } else {
                netType = NETWORK_UNKNOWN;
            }
        }
        return netType;
    }

    /**
     * 获取当前的网络类型(WIFI,2G,3G,4G)
     * <p>依赖上面的方法</p>
     *
     * @param context 上下文
     * @return 网络类型名称
     * <ul>
     * <li>NETWORK_WIFI   </li>
     * <li>NETWORK_4G     </li>
     * <li>NETWORK_3G     </li>
     * <li>NETWORK_2G     </li>
     * <li>NETWORK_UNKNOWN</li>
     * <li>NETWORK_NO     </li>
     * </ul>
     */
    public static String getNetWorkTypeName(Context context) {
        switch (getNetWorkType(context)) {
            case NETWORK_WIFI:
                return "NETWORK_WIFI";
            case NETWORK_4G:
                return "NETWORK_4G";
            case NETWORK_3G:
                return "NETWORK_3G";
            case NETWORK_2G:
                return "NETWORK_2G";
            case NETWORK_NO:
                return "NETWORK_NO";
            default:
                return "NETWORK_UNKNOWN";
        }
    }

    /*
     *获取连接上的wifi的名字
     * */
    public static String getConnectWifiSsid(Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();

        //8.0需要通过这种方式获取。。。
        if(TextUtils.isEmpty(ssid) || "<unknown ssid>".equals(ssid)){
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            assert cm != null;
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                ssid = info.getExtraInfo();
            }
        }
        return ssid;
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static String getLocalHostIp() {
        String localIp = null;
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            for (; enumeration.hasMoreElements(); ) {
                NetworkInterface networkInterface = enumeration.nextElement();
                Enumeration<InetAddress> ipAddress = networkInterface.getInetAddresses();
                while (ipAddress.hasMoreElements()) {
                    InetAddress inetAddress = ipAddress.nextElement();
                    if (!inetAddress.isLoopbackAddress() && isWlanOrEth(networkInterface.getName())) {
                        localIp = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return localIp;
    }

    private static boolean isWlanOrEth(String name) {

        return name.contains("wlan") || name.contains("eth");
    }

    public static String getNetworkTypeStr(Context context){
        int type = getNetworkType(context);
        if(type == -1){
            return "no_net";
        }else if(type == ConnectivityManager.TYPE_WIFI){
            return "wifi";
        }else if(type == ConnectivityManager.TYPE_MOBILE){
            return "mobile";
        }else{
            return "other";
        }
    }
    /**
     * Get network type
     *
     * @param context
     * @return
     */
    public static int getNetworkType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        return networkInfo == null ? -1 : networkInfo.getType();
    }

    /**
     * Get network type name
     *
     * @param context
     * @return
     */
    public static String getNetworkTypeName(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo;
        String type = NETWORK_TYPE_DISCONNECT;
        if (manager == null || (networkInfo = manager.getActiveNetworkInfo()) == null) {
            return type;
        }
        ;

        if (networkInfo.isConnected()) {
            String typeName = networkInfo.getTypeName();
            if ("WIFI".equalsIgnoreCase(typeName)) {
                type = NETWORK_TYPE_WIFI;
            } else if ("MOBILE".equalsIgnoreCase(typeName)) {
                String proxyHost = android.net.Proxy.getDefaultHost();
                type = TextUtils.isEmpty(proxyHost) ? (isFastMobileNetwork(context) ? NETWORK_TYPE_3G : NETWORK_TYPE_2G)
                        : NETWORK_TYPE_WAP;
            } else {
                type = NETWORK_TYPE_UNKNOWN;
            }
        }
        return type;
    }

    /**
     * Whether is fast mobile network
     *
     * @param context
     * @return
     */
    private static boolean isFastMobileNetwork(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            return false;
        }

        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return false;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return false;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return false;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return true;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return true;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return false;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return true;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return true;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return true;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return true;
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return true;
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return true;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return true;
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return false;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return true;
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return false;
            default:
                return false;
        }
    }

    public static boolean ping(String ipAddress, int time) throws Exception {
        boolean status = InetAddress.getByName(ipAddress).isReachable(time);
        return status;
    }

    public static LocalIp getLocalIp(Context context) {
        WifiManager wifii = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo d = wifii.getDhcpInfo();

        LocalIp localIp = new LocalIp();
        localIp.ip = intToInetAddress(d.ipAddress).getHostAddress();
        localIp.netmask = intToInetAddress(d.netmask).getHostAddress();
        localIp.gateway = intToInetAddress(d.gateway).getHostAddress();

        return localIp;
    }

    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};
        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }


    /**
     * 获取局域网内所有ip
     * @param local
     * @param mask
     * @return
     */
    public static List<String> getLanIps(String local, String mask) {

        ArrayList<String> ipList = new ArrayList<>();
        String[] localIp = local.split("\\.");
        String[] maskIp = mask.split("\\.");

        if (localIp.length != 4 || maskIp.length != 4) {
            return ipList;
        }

        try {
            int localIp2 = Integer.parseInt(localIp[2]);    // 本地ip第三段
            int maskIp2 = Integer.parseInt(maskIp[2]);      // 子网掩码第三段
            if (maskIp2 < 250) {
                maskIp2 = 255;
            }
            int networkIp2 = localIp2 & maskIp2;            // 网络地址第三段

            int cCount = 255 - maskIp2; // 第三段需要遍历的次数

            // 遍历 xxx.xxx.c.d
            for (int c = 0; c <= cCount; c++) {
                for (int d = 2; d < 255; d++) {
                    String ip = localIp[0] + "." + localIp[1] + "." + (networkIp2 + c) + "." + d;
                    if(ip.equals(local)){
                        continue;   // 跳过本机ip
                    }
                    //Log.i("dvlee",ip);
                    ipList.add(ip);
                }
            }
        } catch (Exception e) {
        }
        return ipList;
    }

    // 得到本机Mac地址
    public String getLocalMac(Context context) {
        String mac = "";
        // 获取wifi管理器
        WifiManager wifiMng = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfor = wifiMng.getConnectionInfo();
        mac = "本机的mac地址是：" + wifiInfor.getMacAddress();
        return mac;
    }

    public static class LocalIp {
        public String ip;
        public String netmask;
        public String gateway;
        public String dns1;
        public String dns2;
        public String dhcpServer;

        public String toString(){
            return "ip:" + ip + " netmask:" + netmask + " gateway:" + gateway;
        }
    }
}

package swaiotos.channel.iot.utils;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;

public class WifiConnectManager {


    /**
     * 连接指定的wifi
     *
     * @param context
     * @param targetSid
     * @param targetPsd
     * @param enc
     */
    public static void connectWifi(Context context, String targetSid,
                                   String targetPsd, String enc) {
        // 1、注意热点和密码均包含引号，此处需要需要转义引号
        String sid = "\"" + targetSid + "\"";
        String psd = "\"" + targetPsd + "\"";

        //2、配置wifi信息
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = sid;
        conf.hiddenSSID = true;//wifi模块隐藏以后需要设置该参数
        switch (enc) {
            case "WEP":
                // 加密类型为WEP
                conf.wepKeys[0] = psd;
                conf.wepTxKeyIndex = 0;
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                break;
            case "WPA":
                // 加密类型为WPA
                conf.preSharedKey = psd;
                break;
            case "OPEN":
                //开放网络
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        //3、链接wifi
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Log.e("WifiResult", "wifiManager setWifiEnabled true---");
            wifiManager.setWifiEnabled(true);
        }

        wifiManager.addNetwork(conf);
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();

        /*if (enc.equals("OPEN")) {
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals(sid)) {
                    connect(context, i.networkId);
                    break;
                }
            }
        } else {
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals(sid)) {
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(i.networkId, true);
                    wifiManager.reconnect();
                    break;
                }
            }
        }*/

        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals(sid)) {
                connect(context, i.networkId);
                break;
            }
        }
    }


    public static void connect(Context context, int networkId) {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            Method connect = manager.getClass().getDeclaredMethod("connect", int.class,
                    Class.forName("android.net.wifi.WifiManager$ActionListener"));
            connect.setAccessible(true);
            connect.invoke(manager, networkId, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
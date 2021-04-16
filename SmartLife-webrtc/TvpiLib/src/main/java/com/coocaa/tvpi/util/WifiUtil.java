package com.coocaa.tvpi.util;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class WifiUtil {
    private static final String TAG = "WifiUtil";
    private static final int CIPHER_NONE = 0;
    private static final int CIPHER_WEP = 1;
    private static final int CIPHER_WPA = 2;
    private static final int CIPHER_EAP = 3;

    @IntDef({CIPHER_NONE, CIPHER_WEP, CIPHER_WPA, CIPHER_EAP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CipherType {
    }

    public static boolean isSupportConnectWifi() {
        //android 8.0 8.1只能手动连接wifi
        boolean isSupportConnectWifi = Build.VERSION.SDK_INT != Build.VERSION_CODES.O
                && Build.VERSION.SDK_INT != Build.VERSION_CODES.O_MR1;
        Log.w(TAG, "isSupportConnectWifi:" + isSupportConnectWifi);
        return isSupportConnectWifi;
    }

    public static boolean isWifiEnabled(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        boolean opened = wifiManager.isWifiEnabled();
        Log.w(TAG, "isWifiOpen:" + opened);
        return opened;
    }

    public static boolean setWifiEnable(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            return wifiManager.setWifiEnabled(true);
        }
        return true;
    }

    public static String getConnectWifiSsid(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && wifiManager.getConnectionInfo() != null
                && wifiManager.getConnectionInfo().getSSID() != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            Log.w(TAG, "getConnectedWifiSSID:" + ssid);
            return ssid.replace("\"", "").trim();
        }
        return null;
    }


    public static void connectWifi(Context context, String wifiSSID, String wifiPsd) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int networkId;

        if (removeConfiguredWifi(context, wifiSSID)) {
            Log.d(TAG, "connectWifi: removeConfiguredWifi success and recreate it");
            //如果删除的成功说明这个wifi配置是由本APP配置出来的,这样可以避免密码更改之后，同名字的wifi配置存在，无法连接；
            networkId = wifiManager.addNetwork(createWifiConfiguration(wifiSSID, wifiPsd, CIPHER_WPA));
        } else {
            //删除不成功，要么这个wifi由系统或者其他App配置过，要么是还没连接过的
            Log.d(TAG, "connectWifi: removeConfiguredWifi failed");
            WifiConfiguration configuredWifi = getConfiguredWifi(context, wifiSSID);
            if (configuredWifi != null) {
                Log.d(TAG, "connectWifi: exist configuration");
                configuredWifi.hiddenSSID = true;
                networkId = configuredWifi.networkId;
            } else {
                Log.d(TAG, "connectWifi: not exist configuration,create it");
                networkId = wifiManager.addNetwork(createWifiConfiguration(wifiSSID, wifiPsd, CIPHER_WPA));
            }
        }

        //这个方法的第一个参数是需要连接wifi网络的networkId，第二个参数是指连接当前wifi网络是否需要断开其他网络
        //无论是否连接上，都返回true。。。。
        wifiManager.enableNetwork(networkId, true);
    }

    private static boolean removeConfiguredWifi(Context context, String wifiSSID) {
        if (getConfiguredWifi(context, wifiSSID) != null) {
            return removeNetwork(context, getConfiguredWifi(context, wifiSSID).networkId);
        } else {
            return false;
        }
    }

    /**
     * 移除wifi，因为权限，无法移除的时候，需要手动去翻wifi列表删除
     * 注意：！！！只能移除自己应用创建的wifi。
     * 删除掉app，再安装的，都不算自己应用，具体看removeNetwork源码
     *
     * @param networkId wifi的id
     */
    public static boolean removeNetwork(Context context, int networkId) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.removeNetwork(networkId);
    }

    /**
     * 获取配置过的wifiConfiguration
     */
    @SuppressLint("MissingPermission")
    public static WifiConfiguration getConfiguredWifi(Context context, String wifiSSID) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> configurationList = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConfiguration : configurationList) {
            if (wifiConfiguration.SSID.equals("\"" + wifiSSID + "\"")) {
                return wifiConfiguration;
            }
        }
        return null;
    }

    /**
     * 创建一个wifiConfiguration
     *
     * @param wifiSSID   wifi名称
     * @param wifiPwd    wifi密码
     * @param cipherType 加密类型
     * @return
     */
    public static WifiConfiguration createWifiConfiguration(String wifiSSID, String wifiPwd, @CipherType int cipherType) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + wifiSSID + "\"";
        //无密码
        if (cipherType == CIPHER_NONE) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (cipherType == CIPHER_WEP) {
            //WEP加密
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + wifiPwd + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (cipherType == CIPHER_WPA) {
            //WPA加密
            config.hiddenSSID = true;
            config.preSharedKey = "\"" + wifiPwd + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    public static boolean isWifiHasPassword(Context context, String wifiSSID) {
        Log.d(TAG, "isWifiHasPassword: " + wifiSSID);
        if (TextUtils.isEmpty(wifiSSID)) {
            return true;
        }
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
        List<ScanResult> scanResults = wifiManager.getScanResults();
        for (ScanResult scanResult : scanResults) {
            if (wifiSSID.equals(scanResult.SSID.replace("\"", "").trim())) {
                Log.w(TAG, "isWifiHasPassword： " + isWifiHasPassword(scanResult));
                return isWifiHasPassword(scanResult);
            }
        }
        return true;
    }

    public static boolean isWifiHasPassword(ScanResult result) {
        return getCipherType(result) != CIPHER_NONE;
    }

    private static int getCipherType(ScanResult result) {
        if (result == null) {
            return CIPHER_NONE;
        }
        String capabilities = result.capabilities;
        if (capabilities == null || capabilities.isEmpty()) {
            return CIPHER_NONE;
        }
        // 如果包含WAP-PSK的话，则为WAP加密方式
        if (capabilities.contains("WPA-PSK") || capabilities.contains("WPA2-PSK")) {
            return CIPHER_WPA;
        } else if (capabilities.contains("WPA2-EAP")) {
            return CIPHER_EAP;
        } else if (capabilities.contains("WEP")) {
            return CIPHER_WEP;
        } else if (capabilities.contains("ESS")) {
            // 如果是ESS则没有密码
            return CIPHER_NONE;
        }
        return CIPHER_NONE;
    }


    public static void registerReceiver(@NonNull final Context context, @Nullable final BroadcastReceiver receiver, @NonNull final IntentFilter filter) {
        if (receiver != null) {
            try {
                context.registerReceiver(receiver, filter);
            } catch (Exception ignored) {
            }
        }
    }

    public static void unregisterReceiver(@NonNull final Context context, @Nullable final BroadcastReceiver receiver) {
        if (receiver != null) {
            try {
                context.unregisterReceiver(receiver);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }
}

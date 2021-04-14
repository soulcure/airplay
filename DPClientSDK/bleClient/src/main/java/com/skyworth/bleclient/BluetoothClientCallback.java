package com.skyworth.bleclient;

import java.util.Collection;

public interface BluetoothClientCallback {

    enum DeviceState {
        /**
         * 设备断连
         */
        DISCONNECT,
        /**
         * 设备连接ing
         */
        CONNECTING,
        /**
         * 设备连接
         */
        CONNECTED,
        /**
         * 连接失败
         */
        FAILED,
        /**
         * 扫描设备
         */
        SCANING,
        /**
         * 蓝牙不支持
         */
        NOTSUPPORT,
        /**
         * 蓝牙关闭了
         */
        BLE_DISABLE,
        /**
         * 无位置权限，不能开启扫描
         */
        LOCATION_PERMISSION,
    }

    void onMessageShow(BlePdu blePdu);

    void onStateChange(DeviceState res);

    void onScanResult(String mac);

    void onScanList(Collection<BleDeviceInfo> bleDeviceInfoList);

}
package com.skyworth.dpclientsdk.ble;

import android.bluetooth.BluetoothDevice;

public interface BluetoothServerCallBack {

    void onMessageShow(BlePdu blePdu, BluetoothDevice device);

    void onStartSuccess(String deviceName);

    void onStartFail(String info);
}

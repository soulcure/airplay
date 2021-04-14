package com.skyworth.dpclientsdk.ble;

import android.bluetooth.BluetoothDevice;

public interface IBluetoothServer {
    void setCustomData(String hexData);

    void openBle();

    void removeService();

    void sendMessage(String msg, byte tempCmd, BluetoothDevice mBluetoothDevice);
}

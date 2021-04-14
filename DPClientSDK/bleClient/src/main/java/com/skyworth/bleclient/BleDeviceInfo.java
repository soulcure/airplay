package com.skyworth.bleclient;

public class BleDeviceInfo {
    public String key;
    public String address;
    public String customData;
    public String deviceName;
    public long timestamp;
    public String serviceUUID;
    public String characteristicUUID;
    public int rssi;

    public BleDeviceInfo(String key, String address, String customData, String deviceName, long timestamp, String serviceUUID, String characteristicUUID, int rssi) {
        this.key = key;
        this.address = address;
        this.customData = customData;
        this.deviceName = deviceName;
        this.timestamp = timestamp;
        this.serviceUUID = serviceUUID;
        this.characteristicUUID = characteristicUUID;
        this.rssi = rssi;
    }

    @Override
    public String toString() {
        return "BleDeviceInfo{" +
                "address='" + address + '\'' +
                ", customData='" + customData + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", timestamp=" + timestamp +
                ", rssi=" + rssi +
                ", serviceUUID=" + serviceUUID +
                '}';
    }
}

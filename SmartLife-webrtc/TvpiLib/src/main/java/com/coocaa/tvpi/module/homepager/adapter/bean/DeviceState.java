package com.coocaa.tvpi.module.homepager.adapter.bean;

public enum DeviceState {
    //未添加设备
    STATE_NO_ADD_DEVICE(0),
    //设备离线
    STATE_OFFLINE(1),
    //设备AI待机
    STATE_AI_STANDBY(2),
    //截图同屏显示设备
    STATE_ONLINE_IMAGE(3),
    //无网络
    STATE_NO_INTERNET(5);

    private int value;

    DeviceState(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}


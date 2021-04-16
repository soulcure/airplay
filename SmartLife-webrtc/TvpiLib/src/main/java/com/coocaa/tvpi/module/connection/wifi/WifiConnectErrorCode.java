package com.coocaa.tvpi.module.connection.wifi;


public enum WifiConnectErrorCode {
    NO_GPS_PERMISSION, //未打开GPS定位服务总开关

    NO_LOCATION_PERMISSION, //未授权该app使用位置权限

    NO_OPEN_WIFI,  //未打开WiFi

    CONNECT_TIMEOUT //连接失败超时
}

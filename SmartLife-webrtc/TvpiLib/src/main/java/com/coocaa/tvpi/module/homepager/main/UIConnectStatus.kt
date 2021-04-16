package com.coocaa.tvpi.module.homepager.main

enum class UIConnectStatus {
    NOT_CONNECTED,         //未连接(扫码连接设备状态)
    CONNECTING,            //连接中
    CONNECTED,             //已连接
    CONNECT_NOT_SAME_WIFI, //已连接不在同一wifi
    CONNECT_ERROR          //连接错误
}
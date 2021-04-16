package com.coocaa.publib.data.local;

import java.util.List;

/**
 * @ClassName TVFunction
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020-03-03
 * @Version TODO (write something)
 */
public class TVFunction {

    public List<String> supportCmdList;

    public enum TVFunctionEnum {
        /**
         * 遥控事件
         */
        RemoteKeyEvent,
        /**
         * 激光电视
         */
        LaserTVEvent,
        /**
         *截屏
         */
        Capture,
        /**
         *获取电视基础信息
         */
        GetTVInfo,
        /**
         *获取当前电视支持的指令列表
         */
        GetFunction,
        /**
         *获取信号源
         */
        GetSourceInfo,
        /**
         *设置信号源
         */
        SetSource,
        /**
         *推送在线视频
         */
        PlayMedia,
        /**
         *获取媒体库厂商
         */
        getMediaSorce,
        /**
         *蜂蜜直播
         */
        PlayLiveTV,
        /**
         *向其他应用发送命令
         */
        Write,
        /**
         *从其他应用获取命令
         */
        Read,
        /**
         *获取电视App列表
         */
        GetAppList,
        /**
         *安装电视App
         */
        InstallApp,
        /**
         *卸载电视App
         */
        UninstallApp,
        /**
         *查询是否支持旋转
         */
        IsRotatable,
        /**
         *发送旋转指令
         */
        SwitchRotation,
        /**
         *查询当前电视的旋转方向
         */
        GetRotation
    }
}

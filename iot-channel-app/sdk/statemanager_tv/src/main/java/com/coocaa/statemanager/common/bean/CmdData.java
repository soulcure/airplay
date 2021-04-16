package com.coocaa.statemanager.common.bean;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * @ClassName CmdData
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2019-12-27
 * @Version TODO (write something)
 */
public class CmdData implements Serializable {
    public String cmd;//具体指令
    public String param;//指令内部参数,用指定的bean转成json
    public String type;//指令的种类

    public enum CMD_TYPE {
        /**
         * 设备相关
         */
        DEVICE,
        /**
         * event
         */
        KEY_EVENT,
        TOUCH_EVENT,
        CUSTOM_EVENT,
        /**
         * 应用圈
         */
        APP_STORE,
        /**
         * 启动应用(直播投屏、一键清理)-- 解析 param，用OnClickData
         */
        START_APP,
        /**
         * 多媒体推送，在线，直播，本地（video，music，picture）
         */
        MEDIA,
        /**
         * 语音
         */
        VOICE,
        /**
         * 截屏
         */
        SCREEN_SHOT,
        /**
         * 本地（video，music，picture）
         */
        LOCAL_MEDIA,
        /**
         * 账号相关
         */
        ACCOUNT,
        /**
         * 设备状态
         */
        STATE,
        /**
         * App信息
         */
        APP_INFOS,
        /**
         * Dongle信息
         */
        DONGLE_INFO

    }

    public CmdData() {
    }


    public CmdData(String cmd, String type, String param) {
        this.cmd = cmd;
        this.type = type;
        this.param = param;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

}

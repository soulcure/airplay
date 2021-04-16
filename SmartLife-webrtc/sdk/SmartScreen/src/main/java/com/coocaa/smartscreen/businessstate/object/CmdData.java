package com.coocaa.smartscreen.businessstate.object;


import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * Describe: CmdData消息指令定义
 * Created by AwenZeng on 2020/12/18
 */
public class CmdData implements Serializable {
    /**
     * 具体指令
     */
    public String cmd;//具体指令

    /**
     * 指令内部参数,用指定的bean转成json
     */
    public String param;

    /**
     * 指令的种类
     */
    public String type;

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
        STATE

    }

    public CmdData() {
    }


    public CmdData(String cmd, String type, String param) {
        this.cmd = cmd;
        this.type = type;
        this.param = param;
    }

    public String toJson() {
        return JSONObject.toJSONString(this);
    }

}

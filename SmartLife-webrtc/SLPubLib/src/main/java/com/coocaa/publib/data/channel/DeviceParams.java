package com.coocaa.publib.data.channel;

import com.google.gson.Gson;

/**
 * @ClassName DeviceParams
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/4/8
 * @Version TODO (write something)
 */
public class DeviceParams {
    /**
     * 用于给CmdData cmd字段赋值
     */
    public enum CMD{
        CONNECT,
        DISCONNECT,
        DEVICE_INTO,
    }
    public String name;//名字
    public String room;//房间
    public String model;//型号
    public String activeId;//激活id
    public String ip;
    public int isAIStandby;//ai待机 1是 2否

    public String toJson() {
        return new Gson().toJson(this);
    }
}

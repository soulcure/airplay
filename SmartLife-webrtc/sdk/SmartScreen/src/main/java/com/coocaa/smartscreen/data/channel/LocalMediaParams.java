package com.coocaa.smartscreen.data.channel;

import com.google.gson.Gson;

/**
 * @ClassName LocalMediaParams
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/4/8
 * @Version TODO (write something)
 */
public class LocalMediaParams {
    /**
     * 用于给CmdData cmd字段赋值
     */
    public enum CMD{
        PLAY,//播放
        SEND
    }
    public String name;//名字

    public LocalMediaParams(String name) {
        this.name = name;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}

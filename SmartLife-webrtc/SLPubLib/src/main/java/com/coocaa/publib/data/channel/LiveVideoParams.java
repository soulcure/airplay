package com.coocaa.publib.data.channel;

import com.google.gson.Gson;

/**
 * @ClassName LongVideoParams
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2019-12-27
 * @Version TODO (write something)
 */
public class LiveVideoParams {
    public String channel_id;
    public String category_id;

    /**
     * 用于给CmdData cmd字段赋值
     */
    public enum CMD{
        LIVE_VIDEO
    }

    public LiveVideoParams(String channel_id, String category_id) {
        this.channel_id = channel_id;
        this.category_id = category_id;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}

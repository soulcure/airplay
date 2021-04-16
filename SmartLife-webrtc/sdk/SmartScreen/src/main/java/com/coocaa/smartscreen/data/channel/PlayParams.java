package com.coocaa.smartscreen.data.channel;

import com.google.gson.Gson;

public class PlayParams  {

    public enum CMD {
        ONLINE_VIDEO,//推送在线视频
        REGISTER_MEDIA_STATUS,//注册播放器状态监听
        UNREGISTER_MEDIA_STATUS,//反注册播放器状态监听
        GET_SOURCE,//获取电视机的源
    }

    public String id; //mediaID
    public String name; //name
    public String url;
    public String url_type;
    public boolean needParse;
    public String company; //content_providers
    public String child_id; //childId
    public String vid;
    public String position;
    public boolean read_history = true;
    public boolean need_history = true;
    public String simple_detail;
    public String user_info;
    public String account_source;//账号类型，tv影视根据该字段决定是否展示购买vip二维码

    public String toJson() {
        return new Gson().toJson(this);
    }

}

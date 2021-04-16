package com.coocaa.smartscreen.data.movie;


import java.io.Serializable;
import java.util.List;

/**
 * Created by IceStorm on 2017/9/15.
 */
// 短视频(影视)和长视频（电视派）
public class LongVideoSearchResultModel implements Serializable {

    public int video_id; // 视频id
    public String video_title; // 标题
    public String video_poster; // 视频海报
    public String router;       // 路由跳转
    public String play_length; // 时长（秒）（数据库定义的是字符串所以此处返回字符串）
    public LongVideoDetailModel video_detail;
    public List<Episode> episodes_list;

}


package com.coocaa.smartscreen.data.movie;

/**
 * Created by IceStorm on 2018/1/8.
 */

public class LongVideoListModel {
    public String third_album_id;          // 第三方正片专辑id
    public String album_title;             // 专辑标题
    public String album_subtitle;          // 专辑副标题
    public String video_poster;            // 海报
    public String router;                  // 点击跳转路由

    public String source;//	string	视频来源。注：爱艺奇:iqiyi,腾讯:qq
    public String source_sign;	//string	源标识，如yinhe是爱艺奇 奇异果
    public String director;//	string	专辑导演
    public String actor;//	string	专辑演员
    public String score;//	string	评分
    public String description;//	string	简介
    public int charge_type;//	int	收费类型；0免费，1会员免费，2必须单点收费。
	public String video_url;//	string	视频推送地址

    public String publish_date;//上线时间
    public String video_type;//类型
    public String prompt_info;//		string	电视剧更新时间信息

    public String container_name;//	string	容器的大标题（即大标题）（没有则为空）

    @Override
    public String toString() {
        return "LongVideoListModel{" +
                "third_album_id='" + third_album_id + '\'' +
                ", album_title='" + album_title + '\'' +
                '}';
    }
}

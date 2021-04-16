package com.coocaa.smartscreen.data.movie;
public  class LongVideoDetailModel {
    //奇异果
    public static final String VIP_QiYiGuo = "yinhe";
    //黄金vip
    public static final String VIP_GOLD = "yinhe-gold";
    //qq影视vip
    public static final String VIP_TENCENT = "6";
    //qq鼎级剧场
    public static final String VIP_DingJiJuChang = "7";
    //qq腾讯体育
    public static final String VIP_TENCENT_SPORT= "36";


    public String source;//		string	视频来源。如：爱艺奇:iqiyi,腾讯:qq
    public String source_sign;	//string	源标识，如yinhe是爱艺奇 奇异果
    public String third_album_id;//		string	第三方正片专辑id
    public String album_title;//		string	专辑标题
    public String album_subtitle;//		string	专辑副标题
    public String video_poster;//		string	海报
    public String director;//		string	导演
    public String actor;//		string	演员
    public String description;//		string	简介
    public int is_trailer;//		int	是否为预告片；1是，2不是；默认为2
    public int publist_segment;//		int	官方预计发行或已完整发行集数
    public int updated_segment;//		int	当前已经更新到第几集（从1开始）
    public int is_collect;//		int	是否收藏,1:是,2:否
    public int is_approval;//		int	是否点赞,1:是,2:否
    public int approval_num;//		int	点赞数
    //        public PlaySource play_source;//		object-json	播放信息（默认是第一集）
    public String score;//	string	评分
    public String video_tags;//标签
    public String play_count;//播放次数字段
    public String video_type;
    public String prompt_info;//		string	电视剧更新时间信息
    public String publish_date;//		string	上映时间
}

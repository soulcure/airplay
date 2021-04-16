package com.coocaa.smartscreen.data.movie;

public class Episode {
    public boolean isSelected;//是否选中 给界面展示ui使用的
    public String source;//	string	来源
    public String third_album_id;//	string	第三方正片专辑id
    public String video_third_id;//	string	第三方视频id（无 oqy 等前缀）
    public String video_title;//	string	视频标题
    public String video_subtitle;//	string	视频副标题
    public String video_url;//	string	视频播放地址
    public String video_poster;//海报，短视频转需要
    public int segment_index;//	int	集数序号；表示当前视频为第几集
    public int charge_type;//	int	收费类型；0免费，1会员免费，2必须单点收费。
    public int play_type;//	int	播放规则，0表示试看，1表示可全集播放，2不可看
    public int try_watch_seconds;//	int	当play_way=2时试看秒数
}

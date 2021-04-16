package com.coocaa.publib.data.category;

import com.coocaa.smartscreen.data.movie.LongVideoListModel;

import java.util.List;

public class HomeRecommend {
    public int tag_id; //	tagid用在获取详细数据
    public String title;//标题，显示
    public String router;// 点击跳转路由
    public List<LongVideoListModel> video_list;
}

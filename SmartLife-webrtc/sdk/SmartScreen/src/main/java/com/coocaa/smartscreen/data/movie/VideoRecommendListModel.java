package com.coocaa.smartscreen.data.movie;

import java.util.List;

public class VideoRecommendListModel {
/*    public int code;                  // 0正常
    public String msg;                // 对code的简要描述
    public List<VideoRecommendItemModel> data; // 短视频列表

    public static final class VideoRecommendItemModel{*/
        public int tag_id; //	tagid用在获取详细数据
        public String title;//标题，显示
        public String router;// 点击跳转路由
        public List<LongVideoListModel> video_list;
//    }


        @Override
        public String toString() {
                return "VideoRecommendListModel{" +
                        "title='" + title + '\'' +
                        ", video_list=" + video_list +
                        '}';
        }
}

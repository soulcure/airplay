package com.coocaa.smartscreen.data.movie;


import java.util.List;

/**
 * @ClassName PushHistoryDataModel
 * @Description 推送历史实体
 * @User heni
 * @Date 18-8-28
 */
public class PushHistoryModel {
    public List<PushHistoryVideoModel> movies_within_serven_days;
    public List<PushHistoryVideoModel>  movies_over_serven_days;

    public static class PushHistoryVideoModel {
        public String title; //	是string	标题
        public String video_type; //视频类型 0：短视频， 1：长视频
        public String video_id; //是	string	视频id
        public String album_id; //是	string	视频专辑id
        public String category; //否	string	分类
        public String poster_v; //否	string	竖版海报
        public String poster_h; //否	string	横版海报
        public String router;   //点击跳转路由

        public boolean isSelected;    // 是否处于编辑模式下的选中状态
        public boolean isInEditMode;  // 是否处于编辑模式
    }
}

package com.coocaa.smartscreen.data.movie;

/**
 * Created by IceStorm on 2017/12/22.
 */

public class PlayRecordListModel {
    public int playrecord_id;     // 播放记录id
    public String video_title;    // 标题
    public String video_poster;   // 海报
    public String last_play_time;  // 最后一次播放时间
    public int segment_index;     // 正片，播放的集数
    public String router;         // 点击跳转路由

    public boolean isSelected;    // 是否处于编辑模式下的选中状态
    public boolean isInEditMode;  // 是否处于编辑模式
}

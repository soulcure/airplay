package com.coocaa.smartscreen.data.movie;

/**
 *
 * 喜欢收藏列表实体
 * Created by IceStorm on 2017/12/28.
 */

public class CollectionModel {
    public int collect_id;      // 收藏的Id
    public String video_title;  // 标题
    public String video_poster; // 海报
    public String collect_time; // 收藏时间
    public String router;       // 点击跳转路由
    public boolean isSelected;    // 是否处于编辑模式下的选中状态
    public boolean isInEditMode;  // 是否处于编辑模式
}

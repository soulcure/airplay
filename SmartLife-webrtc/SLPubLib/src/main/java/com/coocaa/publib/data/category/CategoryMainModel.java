package com.coocaa.publib.data.category;

/**
 * Created by IceStorm on 2018/1/8.
 */

public class CategoryMainModel {
    public String classify_pic;  // 分类图片
    public String classify_id;   // 分类Id
    public String classify_name; // 分类名
    public String router;

    public MultiTypeEnum container_type;//	int	容器的类型，101:Banner模块,102:双列模块,103:三列模块,104:传统追剧预定模块,201:灰色分割线模块
    public String container_name;//	string	容器的大标题（即大标题）（没有则为空）
}

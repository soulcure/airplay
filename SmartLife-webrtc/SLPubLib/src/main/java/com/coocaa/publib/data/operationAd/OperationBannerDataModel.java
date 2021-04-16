package com.coocaa.publib.data.operationAd;

import com.coocaa.publib.data.category.MultiTypeEnum;

import java.util.List;

/**
 * Created by IceStorm on 2017/12/29.
 */

public class OperationBannerDataModel {
    public int id;
    public OperationBannerLayoutModel layout;
    public List<OperationBannerItemsModel> items;

    public MultiTypeEnum container_type;    // int 容器的类型，101:Banner模块,102:双列模块,103:三列模块,104:传统追剧预定模块
    public String container_name;//	string	容器的大标题（即大标题）（没有则为空）
}

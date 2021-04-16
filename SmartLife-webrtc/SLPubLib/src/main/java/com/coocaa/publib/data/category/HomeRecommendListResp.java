package com.coocaa.publib.data.category;

import java.util.List;

/**
 * Created by WHY on 2018/3/6.
 */

public class HomeRecommendListResp {
    public int code;                  // 0正常
    public String msg;                // 对code的简要描述
    public List<HomeRecommend> data; // 短视频列表
}

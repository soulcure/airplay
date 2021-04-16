package com.coocaa.publib.data.category;

import java.util.List;

/**
 * Created by IceStorm on 2017/12/15.
 */

public class CategoryAppResp {
    public int code;                 // 0正常
    public String msg;               // 对code的简要描述
    public int has_more;             // 是否有下一页,1:是,2:否
    public List<CategoryMainModel> data; // 正片列表
}

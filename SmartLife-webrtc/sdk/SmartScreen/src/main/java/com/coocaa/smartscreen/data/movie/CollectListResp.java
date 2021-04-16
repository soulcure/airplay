package com.coocaa.smartscreen.data.movie;

import java.util.List;

/**
 * Created by IceStorm on 2017/12/28.
 */

public class CollectListResp {
    public int code;        // 0正常
    public String msg;      // 对code的简要描述
    public int has_more;    // 是否有下一页,1:是,2:否
    public int total;       // 总数
    public List<CollectionModel> data;
}

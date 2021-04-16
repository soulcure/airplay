package com.coocaa.smartscreen.data.movie;


import java.util.List;

/**
 * Created by IceStorm on 2017/12/19.
 */

public class LongVideoListResp {

    public int code;                          // 0 正常
    public String msg;                        // 对code的简要描述
    public int has_more;                      // 是否有下一页,1:是,2:否
    public List<LongVideoListModel> data; // 正片列表
}

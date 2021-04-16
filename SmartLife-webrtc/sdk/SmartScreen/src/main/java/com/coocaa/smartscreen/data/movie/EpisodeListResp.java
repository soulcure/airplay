package com.coocaa.smartscreen.data.movie;

import java.util.List;

/**
 * Created by WHY on 2018/1/11.
 */

public class EpisodeListResp {
    public int code;                          // 0 正常
    public String msg;                        // 对code的简要描述
    public int has_more;                      // 是否有下一页,1:是,2:否
    public int total_episodes;//int	总共剧集数
    public List<Episode> data;	//	array	剧集列表
}

package com.coocaa.publib.data.tvlive;

import java.io.Serializable;

/**
 * @ClassName TVLiveChannelsData
 * @Description TODO (write something)
 * @User heni
 * @Date 2019/1/11
 */
public class TVLiveChannelsData implements Serializable {
    public String channel;
    public String channel_name;
    public String channel_class;
    public String channel_poster;
    public TVLiveProgramData program;
    public boolean isCollected; //本地变量，表示是否收藏
}

package com.coocaa.publib.data.tvlive;

import java.io.Serializable;

/**
 * @ClassName TVLiveProgramData
 * @Description
 * @User heni
 * @Date 2019/1/11
 */
public class TVLiveProgramData implements Serializable {
    public String source;
    public String channel;
    public String channel_class;
    public String channel_name;
    public String program_title;
    public String content_name;
    public long start_time;
    public long end_time;
    public String pic;
    public String tag;
}

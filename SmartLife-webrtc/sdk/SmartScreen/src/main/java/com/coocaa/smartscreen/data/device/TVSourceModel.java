package com.coocaa.smartscreen.data.device;

import java.io.Serializable;

/**
 * Created by IceStorm on 2018/1/15.
 */

public class TVSourceModel implements Serializable {
    public String tv_source; // 源,qq:腾讯,iqiyi:爱艺奇
    public String last_link_time; // 最后连接时间。如果是查最新的则为当前时间
    public int is_history; // 是不是历史连接源。1:历史的记录, 2:当前mac查询
    public String tv_name; // 电视的名称
    public String model; // 型号，如:G5

}

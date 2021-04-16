package com.coocaa.smartscreen.data.device;

import java.io.Serializable;

/**
 * @ClassName TvProperty
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/8/5
 * @Version TODO (write something)
 */
public class TvProperty implements Serializable {
    public String chip;//机芯
    public String model;//机型
    public int channel;//支持几路视频聊天
    public boolean isSupportHomeCare;//是否支持家庭看护
    public boolean isSupportSyncScreen;//是否支持同屏
}

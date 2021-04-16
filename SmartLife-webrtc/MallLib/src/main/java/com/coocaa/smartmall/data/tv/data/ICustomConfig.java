package com.coocaa.smartmall.data.tv.data;

import java.util.Map;

/**
 * @ClassName ICustomConfig
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/8/27
 * @Version TODO (write something)
 */
public interface ICustomConfig {
    public String getAccessToken();
    public Map<String, String> getCustomHeader();
}

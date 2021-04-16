package com.coocaa.smartscreen.data.function.homepage;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName SSHomePageResp
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 4/9/21
 * @Version TODO (write something)
 */
public class SSHomePageResp implements Serializable {
    public int code;
    public String msg;
    public List<SSHomePageData> data;
}
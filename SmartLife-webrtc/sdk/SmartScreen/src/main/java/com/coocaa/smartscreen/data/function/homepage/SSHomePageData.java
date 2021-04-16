package com.coocaa.smartscreen.data.function.homepage;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName SSHomePageData
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 4/9/21
 * @Version TODO (write something)
 */
public class SSHomePageData implements Serializable {
    public String tab_name;
    public int tab_id;
    public List<SSHomePageBlock> blocks;
}
package com.coocaa.smartscreen.data.upgrade;

import com.google.gson.Gson;

/**
 * @ClassName UpgradeData
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 2020/11/1
 * @Version TODO (write something)
 */
public class UpgradeData {
    public int id;
    public int app_id;
    public String app_version;
    public long version_code;
    public String download_url;
    public long filesize;
    public String update_log;

    public String toJson() {
        return new Gson().toJson(this);
    }
}

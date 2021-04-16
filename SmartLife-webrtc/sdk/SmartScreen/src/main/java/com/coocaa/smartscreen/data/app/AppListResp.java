package com.coocaa.smartscreen.data.app;

import java.util.List;

public class AppListResp {
    public int ret;
    public String msg;
    public AppListData data;

    public static class AppListData {
        public int total;//应用总数
        public int page;//当前页码
        public int count;//每页数量
        public List<AppModel> appList;
    }

}

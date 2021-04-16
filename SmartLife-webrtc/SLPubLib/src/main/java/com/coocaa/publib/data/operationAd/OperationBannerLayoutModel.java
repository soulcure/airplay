package com.coocaa.publib.data.operationAd;

/**
 * Created by IceStorm on 2017/12/29.
 */

public class OperationBannerLayoutModel {
    public int id;    // 布局id
    public int type;  // 布局类型，客户端可根据此类型解析params，目前仅定义了一种类型type为1
//    public OperationBannerLayoutModelParams params; // 具体布局
    public String params; // 具体布局
}

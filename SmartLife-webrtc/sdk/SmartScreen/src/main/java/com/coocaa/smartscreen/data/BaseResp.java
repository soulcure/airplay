package com.coocaa.smartscreen.data;



/**
 * 视频通话部分接口返回数据的基类
 * Created by songxing on 2020/3/21
 */
public class BaseResp<T> {

    //视频通话模块1表示正常 其他表示异常
    public int code ; //状态码

    //应用模块 0 表示正常 其他异常
    public int ret ;

    public String msg; //信息


    public T data; //其他数据

    @Override
    public String toString() {
        return "BaseResp{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}

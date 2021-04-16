package com.coocaa.smartscreen.businessstate;

import android.content.Context;

/**
 * Describe: 手机端-业务状态获取接口
 *
 *  1.业务初始接口：<br>
 *    接口定义：init(Context context)<br>
 *       描述：初始化环境变量<br>
 *   <br><br>
 *
 *  1.获取当前业务状态接口：<br>
 *       描述：主要为移动端向Dangle端请求当前业务状态，实际操作为向业务端发送一条CMD消息。<br>
 *
 *<br><br>
 * Created by AwenZeng on 2020/12/18
 */
public interface IBusinessStatePhoneReport {
    /**
     * 业务状态初始化
     * @param context 环境变量
     */
    void init(Context context);

    /**
     * 获取当前业务状态
     */
    void getBusinessState();


    void addListener(IBusinessStateListener listener);


    void removeListener(IBusinessStateListener listener);




}

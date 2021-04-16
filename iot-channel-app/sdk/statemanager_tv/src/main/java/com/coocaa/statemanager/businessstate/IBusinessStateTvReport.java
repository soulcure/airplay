package com.coocaa.statemanager.businessstate;

import android.content.Context;

import com.coocaa.statemanager.common.bean.BusinessState;

import java.util.Map;

import swaiotos.channel.iot.ss.channel.im.IMMessage;


/**
 * Describe: TV端-业务状态上报接口
 *  详情：见各个业务接口定义
 * Created by AwenZeng on 2020/12/18
 */
public interface IBusinessStateTvReport {
    /**
     * 业务状态初始化
     * 注意：每次进入业务，建议都初始化一次
     */
    void init(Context context);

    /**
     * 断开连接返回主页
     * @param openId
     */
    void disconnectBackHome(String openId);

    /**
     * 启动和更新业务状态上报<br>
     */
    void updateBusinessState(String id, String values);

    /**
     * 退出业务<br>
     * 建议：业务退出，必须要调用此接口<br>
     */
    void exitBusiness();

    /**
     * 获取当前业务状态
     */
    void getDangleTvBusinessState();

    /**
     * 设置当前业务状态
     * @param state
     */
    void setCurrentBusinessState(BusinessState state);

    /**
     * 设置业务拥有者
     * @param owner
     */
    void setOwner(String owner);

    /**
     * 设置移动传过来的额外参数
     * @param extra
     */
    void setExtra(Map<String, String> extra);

    void reportSystemUpdateState(IMMessage imMessage);
}

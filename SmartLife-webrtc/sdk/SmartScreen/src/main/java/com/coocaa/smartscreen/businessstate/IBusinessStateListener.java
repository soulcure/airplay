package com.coocaa.smartscreen.businessstate;


import com.coocaa.smartscreen.businessstate.object.BusinessState;

/**
 * Describe:
 * Created by AwenZeng on 2020/12/22
 */
public interface IBusinessStateListener {

    /**
     * 更新业务场景状态
     * @param businessState
     */
    void onUdpateBusinessState(BusinessState businessState);
}

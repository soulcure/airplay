package com.coocaa.statemanager.model;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.coocaa.statemanager.common.bean.MiracastAppInfo;
import com.coocaa.statemanager.common.bean.ScreenApps;
import com.coocaa.statemanager.common.http.StateHttpManager;
import com.coocaa.statemanager.common.utils.SystemSetting;
import com.coocaa.statemanager.data.AppResolver;

import java.util.List;

import swaiotos.channel.iot.ss.server.http.api.HttpApi;
import swaiotos.channel.iot.ss.server.http.api.HttpResult;
import swaiotos.channel.iot.ss.server.http.api.HttpSubscribe;
import swaiotos.channel.iot.ss.server.http.api.HttpThrowable;
import swaiotos.channel.iot.utils.EmptyUtils;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

/**
 * @ClassName: StateModel
 * @Author: AwenZeng
 * @CreateDate: 2021/3/30 11:15
 * @Description: 状态model模块
 */
public class StateModel {
    private Context mContext;
    private int count = 0;
    private static final String TAG = "configInfo";

    public StateModel(Context context) {
        this.mContext = context;
    }

    /**
     * 获取配置信息
     */
    public void getConfigInfo() {
        if (count > 3) {   //最多尝试更新三次
            return;
        }
        count++;
        getScreenApps();
        getForbidScreenApps();
    }

    /**
     * 获取投屏业务配置
     */
    public void getScreenApps() {
        HttpApi.getInstance().request(StateHttpManager.SERVICE.getScreenApps(mContext), new HttpSubscribe<HttpResult<ScreenApps>>() {
            @Override
            public void onSuccess(HttpResult<ScreenApps> result) {
                Log.d(TAG, " HttpApi  (result != null && result.data != null):" + (result != null && result.data != null));
                if (result != null && result.data != null) {
                    AppResolver.updateScreenApps(mContext, result.data, true);
                }
            }

            @Override
            public void onError(HttpThrowable error) {
                Log.d(TAG, " HttpApi error:" + error.getErrMsg());
            }
        });
    }

    /**
     * 获取禁止投屏业务配置
     */
    public void getForbidScreenApps() {
        HttpApi.getInstance().request(StateHttpManager.SERVICE.getDongleConfig(mContext), new HttpSubscribe<HttpResult<List<MiracastAppInfo>>>() {
            @Override
            public void onSuccess(HttpResult<List<MiracastAppInfo>> result) {
                try {//请求异常处理
                    if (EmptyUtils.isNotEmpty(result) && EmptyUtils.isNotEmpty(result.data)) {
                        Log.d(TAG, "result:" + JSONObject.toJSONString(result.data));
                        for (MiracastAppInfo item : result.data) {
                            if (item.isDisable) {
                                SystemSetting.setComponentEnabledSetting(mContext, item.pkgName, item.claseName, COMPONENT_ENABLED_STATE_DISABLED);
                            } else {
                                SystemSetting.setComponentEnabledSetting(mContext, item.pkgName, item.claseName, COMPONENT_ENABLED_STATE_DEFAULT);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(HttpThrowable error) {

            }
        });
    }
}

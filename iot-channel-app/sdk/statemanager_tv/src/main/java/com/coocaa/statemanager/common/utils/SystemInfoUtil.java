package com.coocaa.statemanager.common.utils;

import android.content.Context;
import android.text.TextUtils;

import com.coocaa.statemanager.StateManager;

import org.json.JSONException;
import org.json.JSONObject;

import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.utils.EmptyUtils;
import swaiotos.channel.iot.utils.SpaceAccountManager;
import swaiotos.sal.SAL;

/**
 * Describe:系统信息工具类
 * Created by AwenZeng on 2021/02/22
 */
public class SystemInfoUtil {

    /**
     * 是否dangle
     *
     * @return
     */
    public static boolean isDangle() {
        return Constants.isDangle();
    }


    /**
     * 是否是C端用户；默认为C端用户
     *
     * @param context
     * @return
     */
    public static boolean isFamilyClient(Context context) {
        SpaceAccountManager mSpaceAccountManager = new SpaceAccountManager();
        String spaceAccount = mSpaceAccountManager.getSpaceAccount(context);
        String merchant_id = null;
        if (!TextUtils.isEmpty(spaceAccount)) {
            try {
                JSONObject jsonObject = new org.json.JSONObject(spaceAccount);
                merchant_id = jsonObject.getString("merchant_id");
                if (EmptyUtils.isNotEmpty(merchant_id)) {
                    return false;
                } else {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 系统更新是否存在
     * @return true:存在 false:不存在
     */
    public static boolean isSystemUpgradeExist(){
        return SAL.getModule(StateManager.INSTANCE.getContext(), SAL.SETTING).isSystemUpgradeExist();
    }
}

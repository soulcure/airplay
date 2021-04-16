package com.coocaa.tvpi.module.onlineservice;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;
import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.LoginRepository;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.util.Utils;
import com.qiyukf.nimlib.sdk.StatusBarNotificationConfig;
import com.qiyukf.unicorn.api.ConsultSource;
import com.qiyukf.unicorn.api.Unicorn;
import com.qiyukf.unicorn.api.YSFOptions;
import com.qiyukf.unicorn.api.YSFUserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import swaiotos.channel.iot.ss.device.Device;
import swaiotos.channel.iot.ss.device.DeviceInfo;
import swaiotos.channel.iot.ss.device.TVDeviceInfo;
import swaiotos.channel.iot.ss.server.ShareUtls;
import swaiotos.channel.iot.utils.EmptyUtils;
import swaiotos.channel.iot.utils.ThreadManager;

/**
 * @ClassName: OnlineServiceHelp
 * @Author: AwenZeng
 * @CreateDate: 2021/3/29 16:44
 * @Description: 网易七鱼在线客服服务Help类
 */
public class OnlineServiceHelp {
    private Context mContext;
    private static volatile OnlineServiceHelp instance;
    public static final String APPKEY_QIYU = "e155d99a36a6d6524f1d87476cee6a84";//在线客户，网易七鱼

    public static OnlineServiceHelp getInstance() {
        if (instance == null) {
            synchronized (OnlineServiceHelp.class){
                if (instance == null) {
                    instance = new OnlineServiceHelp();
                }
            }
        }
        return instance;
    }

    public void openOnlinServieActivity() {
        String title = "在线客服";
        ConsultSource source = new ConsultSource("发现", "共享屏客服", "");
        Unicorn.openServiceActivity(mContext, title, source);
        setUserInfo();
    }

    public void init(Context context){
        mContext = context;
        YSFOptions options = new YSFOptions();
        options.statusBarNotificationConfig = new StatusBarNotificationConfig();
        Unicorn.init(mContext, APPKEY_QIYU, options, new QiyuImageLoader(mContext));
    }


    /**
     * 设置用户信息
     */
    private void setUserInfo() {
        CoocaaUserInfo coocaaUserInfo = Repository.get(LoginRepository.class).queryCoocaaUserInfo();
        if (EmptyUtils.isNotEmpty(coocaaUserInfo) && (UserInfoCenter.getInstance().isLogin())) {
            ThreadManager.getInstance().ioThread(new Runnable() {
                @Override
                public void run() {
                    YSFUserInfo userInfo = new YSFUserInfo();
                    userInfo.userId = coocaaUserInfo.open_id;
                    userInfo.authToken = coocaaUserInfo.access_token;
                    List<HashMap<String, Object>> userInfoList = new ArrayList<>();
                    HashMap<String, Object> accountMap = new HashMap<>();
                    accountMap.put("index", 0);
                    accountMap.put("key", "account");
                    accountMap.put("label", "账号");
                    accountMap.put("value", coocaaUserInfo.getMobile());
                    userInfoList.add(accountMap);
                    HashMap<String, Object> nickNameMap = new HashMap<>();
                    nickNameMap.put("index", 1);
                    nickNameMap.put("key", "nick_name");
                    nickNameMap.put("label", "昵称");
                    nickNameMap.put("value", coocaaUserInfo.getNick_name());
                    userInfoList.add(nickNameMap);
                    HashMap<String, Object> appVersionMap = new HashMap<>();
                    appVersionMap.put("index", 2);
                    appVersionMap.put("key", "app_version");
                    appVersionMap.put("label", "共享屏APP版本");
                    appVersionMap.put("value", "Version " + Utils.getAppVersionName(null));
                    userInfoList.add(appVersionMap);
                    Device device = SSConnectManager.getInstance().getHistoryDevice();
                    if (EmptyUtils.isNotEmpty(device) && EmptyUtils.isNotEmpty(device.getInfo()) && device.getInfo().type() == DeviceInfo.TYPE.TV) {
                        DeviceInfo deviceInfo = device.getInfo();
                        TVDeviceInfo tvDeviceInfo = (TVDeviceInfo) deviceInfo;
                        HashMap<String, Object> activationIdMap = new HashMap<>();
                        activationIdMap.put("index", 3);
                        activationIdMap.put("key", "dongle_activation_id");
                        activationIdMap.put("label", "Dongle激活ID");
                        activationIdMap.put("value", tvDeviceInfo.activeId);
                        userInfoList.add(activationIdMap);
                        HashMap<String, Object> dongleVersion = new HashMap<>();
                        dongleVersion.put("index", 4);
                        dongleVersion.put("key", "dongle_version");
                        dongleVersion.put("label", "Dongle版本号");
                        dongleVersion.put("value", tvDeviceInfo.cTcVersion);
                        userInfoList.add(dongleVersion);
                    } else {
                        HashMap<String, Object> activationIdMap = new HashMap<>();
                        activationIdMap.put("index", 3);
                        activationIdMap.put("key", "dongle_activation_id");
                        activationIdMap.put("label", "Dongle激活ID");
                        activationIdMap.put("value", "--");
                        userInfoList.add(activationIdMap);
                        HashMap<String, Object> dongleVersion = new HashMap<>();
                        dongleVersion.put("index", 4);
                        dongleVersion.put("key", "dongle_version");
                        dongleVersion.put("label", "Dongle版本号");
                        dongleVersion.put("value", "--");
                        userInfoList.add(dongleVersion);
                    }
                    userInfo.data = JSONObject.toJSONString(userInfoList);
                    Unicorn.setUserInfo(userInfo);
                }
            });
        } else {
            ThreadManager.getInstance().ioThread(new Runnable() {
                @Override
                public void run() {
                    YSFUserInfo userInfo = new YSFUserInfo();
                    if(EmptyUtils.isEmpty(ShareUtls.getInstance(mContext).getString("uuid",""))){
                        userInfo.userId = UUID.randomUUID().toString();
                        ShareUtls.getInstance(mContext).putString("uuid",userInfo.userId);
                    }else{
                        userInfo.userId = ShareUtls.getInstance(mContext).getString("uuid","");
                    }
                    List<HashMap<String, Object>> userInfoList = new ArrayList<>();
                    HashMap<String, Object> accountMap = new HashMap<>();
                    accountMap.put("index", 0);
                    accountMap.put("key", "account");
                    accountMap.put("label", "账号");
                    accountMap.put("value", "--");
                    userInfoList.add(accountMap);
                    HashMap<String, Object> nickNameMap = new HashMap<>();
                    nickNameMap.put("index", 1);
                    nickNameMap.put("key", "nick_name");
                    nickNameMap.put("label", "昵称");
                    nickNameMap.put("value", "--");
                    userInfoList.add(nickNameMap);
                    HashMap<String, Object> appVersionMap = new HashMap<>();
                    appVersionMap.put("index", 2);
                    appVersionMap.put("key", "app_version");
                    appVersionMap.put("label", "共享屏APP版本");
                    appVersionMap.put("value", "Version " + Utils.getAppVersionName(null));
                    userInfoList.add(appVersionMap);

                    HashMap<String, Object> activationIdMap = new HashMap<>();
                    activationIdMap.put("index", 3);
                    activationIdMap.put("key", "dongle_activation_id");
                    activationIdMap.put("label", "Dongle激活ID");
                    activationIdMap.put("value", "--");
                    userInfoList.add(activationIdMap);
                    HashMap<String, Object> dongleVersion = new HashMap<>();
                    dongleVersion.put("index", 4);
                    dongleVersion.put("key", "dongle_version");
                    dongleVersion.put("label", "Dongle版本号");
                    dongleVersion.put("value", "--");
                    userInfoList.add(dongleVersion);
                    userInfo.data = JSONObject.toJSONString(userInfoList);
                    Unicorn.setUserInfo(userInfo);
                }
            });
        }
    }

    /**
     * 账号退出，七鱼也退出
     */
    public void logout() {
        Unicorn.logout();
    }

}

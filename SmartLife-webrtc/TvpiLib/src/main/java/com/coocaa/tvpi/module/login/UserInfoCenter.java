package com.coocaa.tvpi.module.login;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.data.account.YunXinUserInfo;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.LoginRepository;
import com.coocaa.smartscreen.repository.utils.Preferences;
import com.coocaa.tvpi.module.login.provider.ProviderClient;

/**
 * 用户信息管理类
 * Created by songxing on 2020/6/17
 */
public class UserInfoCenter {
    private static final String TAG = UserInfoCenter.class.getSimpleName();
    private static UserInfoCenter sInstance;

    private Context context;
    private String accessToken;
    private String tpToken;
    private String tvSource;
    private YunXinUserInfo yunXinUserInfo;
    private CoocaaUserInfo coocaaUserInfo;

    private UserInfoCenter(){
    }

    public static UserInfoCenter getInstance() {
        if (sInstance == null) {
            synchronized (UserInfoCenter.class) {
                if (sInstance == null) {
                    sInstance = new UserInfoCenter();
                }
            }
        }
        return sInstance;
    }

    public void init(Context context) {
        this.context = context;
    }

    public boolean isLogin(){
        accessToken = getAccessToken();
        if(accessToken != null && !TextUtils.isEmpty(accessToken)){
            Log.d(TAG, "isLogin true：accessToken" + accessToken);
            return true;
        }else {
            Log.d(TAG, "isLogin false");
            return false;
        }
    }

    public String getAccessToken(){
        if(accessToken == null || TextUtils.isEmpty(accessToken)){
            accessToken = Repository.get(LoginRepository.class).queryToken();
        }
        return accessToken;
    }

    public String getTpToken(){
        if(TextUtils.isEmpty(tpToken)){
//            tpToken = Repository.get(LoginRepository.class).queryTpTokenInfo();
        }
        return tpToken;
    }

    public YunXinUserInfo getYunXinUserInfo() {
        if(yunXinUserInfo == null){
           yunXinUserInfo = Repository.get(LoginRepository.class).queryYunXinUserInfo();
        }
        return yunXinUserInfo;
    }


    public CoocaaUserInfo getCoocaaUserInfo(){
        if(coocaaUserInfo == null){
            coocaaUserInfo = Repository.get(LoginRepository.class).queryCoocaaUserInfo();
        }
        return coocaaUserInfo;
    }

    public void setCoocaaUserInfo(CoocaaUserInfo coocaaUserInfo) {
        this.coocaaUserInfo = coocaaUserInfo;
    }

    public String getTvSource(){
        if(tvSource == null || TextUtils.isEmpty(tvSource)){
            tvSource = Repository.get(LoginRepository.class).queryToken();
        }
        return tvSource;
    }

    public void clearUserInfo(){
        accessToken = null;
        coocaaUserInfo = null;
        yunXinUserInfo = null;
        Preferences.clear();
        ProviderClient.getClient().clear();
        sendAccountChangedBroadcast();
    }

    public void registerAccountReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        String ACCOUNT_CHANGED = "com.tianci.user.account_changed";
        intentFilter.addAction(ACCOUNT_CHANGED);
        context.registerReceiver(mAccountReceiver, intentFilter);
    }

    public void unRegisterAccountReceiver() {
        try {
            context.unregisterReceiver(mAccountReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendAccountChangedBroadcast() {
        String ACCOUNT_CHANGED = "com.tianci.user.account_changed";
        context.sendBroadcast(new Intent(ACCOUNT_CHANGED));
    }

    private BroadcastReceiver mAccountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (action.equals(UserCmdDefine.ACCOUNT_CHANGED)) {
//                boolean hasLogin = skyUserApi.hasLogin();
//                Log.i(TAG, "hasLogin: " + hasLogin);
//                if (!hasLogin) {
//                    LogoutHelp.logout();
//                }
//            } else {
//
//
//            }

        }
    };
}

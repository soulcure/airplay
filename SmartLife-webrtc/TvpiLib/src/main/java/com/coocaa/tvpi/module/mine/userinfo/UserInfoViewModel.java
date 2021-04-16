package com.coocaa.tvpi.module.mine.userinfo;

import android.text.TextUtils;
import android.util.Log;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.data.account.UpdateAvatarBean;
import com.coocaa.smartscreen.network.exception.ApiException;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.callback.RepositoryCallback;
import com.coocaa.smartscreen.repository.service.LoginRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;
import com.coocaa.tvpi.event.UserLoginEvent;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.module.login.provider.ProviderClient;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * 登录模块的ViewModel
 * Created by songxing on 2020/7/8
 */
public class UserInfoViewModel extends BaseViewModel {

    private String TAG = UserInfoViewModel.class.getSimpleName();
    private MutableLiveData<String> updateResposeLD = new MutableLiveData<>();
    private MutableLiveData<Boolean> updateAvatarResultLD = new MutableLiveData<>();
    private CoocaaUserInfo mCoocaaUserInfo;
    public static final int ERROR1 = 1;
    public static final int ERROR2 = 2;

    public LiveData<String> updateUserInfo(String accessToken, String nickName, String gender,
                                           String birthday) {
        Repository.get(LoginRepository.class).updateCoocaaUserInfo(accessToken, nickName, gender,
                birthday)
                .setCallback(new BaseRepositoryCallback<String>() {
                    @Override
                    public void onSuccess(String response) {
                        updateResposeLD.setValue(response);
                        updateUserInfo();
                    }

                    @Override
                    public void onFailed(Throwable e) {
//                        super.onFailed(e);
                        updateResposeLD.setValue(e.getMessage());
                    }
                });
        return updateResposeLD;
    }

    public LiveData<Boolean> updateUserAvatar(String accessToken, String base64Avatar,
                                              String type) {
        Repository.get(LoginRepository.class).updateAvatar(accessToken, base64Avatar, type).setCallback(new RepositoryCallback<List<UpdateAvatarBean>>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(List<UpdateAvatarBean> avatarList) {
                Log.d(TAG, "onSuccess: " + avatarList);
                updateAvatarResultLD.setValue(true);
                updateUserInfo();
            }

            @Override
            public void onFailed(Throwable e) {
                updateAvatarResultLD.setValue(false);
                ToastUtils.getInstance().showGlobalShort("抱歉，头像修改失败了！");
                Log.e(TAG, "onFailed: " + e.getMessage());
            }
        });
        return updateAvatarResultLD;
    }

    private void updateUserInfo() {
        if (!UserInfoCenter.getInstance().isLogin()) {
            return;
        }
        String accessToken = UserInfoCenter.getInstance().getAccessToken();
        Repository.get(LoginRepository.class)
                .getCoocaaUserInfo(accessToken)
                .setCallback(new BaseRepositoryCallback<CoocaaUserInfo>(ApiException.AUTH_TOKEN_EXPIRED_ACCOUNT) {
                    @Override
                    public void onSuccess(CoocaaUserInfo coocaaUserInfo) {
                        super.onSuccess(coocaaUserInfo);
                        Log.d(TAG, "syncLoginData: accessToken = " + accessToken);
                        Log.d(TAG, "syncLoginData: userInfo = " + coocaaUserInfo);
                        if (TextUtils.isEmpty(coocaaUserInfo.access_token) || "null".equalsIgnoreCase(coocaaUserInfo.access_token)) {
                            coocaaUserInfo.access_token = accessToken;
                        }

                        //更新缓存
                        ProviderClient.getClient().saveInfo(accessToken, new Gson().toJson(coocaaUserInfo));
                        Repository.get(LoginRepository.class).saveCoocaaUserInfo(coocaaUserInfo);
                        UserInfoCenter.getInstance().setCoocaaUserInfo(coocaaUserInfo);

                        //告知上一页，是否该清理图片缓存
                        EventBus.getDefault().post(new UserLoginEvent(true));
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                    }
                });
    }

    public String getUserName() {
        if (mCoocaaUserInfo == null) {
            mCoocaaUserInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
        }
        if (mCoocaaUserInfo != null && mCoocaaUserInfo.nick_name != null) {
            return mCoocaaUserInfo.nick_name;
        }
        return null;
    }


    public String getUserPhoneNum() {
        if (mCoocaaUserInfo == null) {
            mCoocaaUserInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
        }
        if (mCoocaaUserInfo != null && mCoocaaUserInfo.getMobile() != null) {
            String mobile =
                    mCoocaaUserInfo.getMobile().substring(0, 3) + "****" + mCoocaaUserInfo.getMobile().substring(7);
            return mobile;
        }
        return null;
    }

    public String getUserAvatar() {
        if (mCoocaaUserInfo == null) {
            mCoocaaUserInfo = UserInfoCenter.getInstance().getCoocaaUserInfo();
        }
        return mCoocaaUserInfo.avatar;
    }

    public int isNameCorrect(String text) {
        Log.d(TAG, "isNameCorrect: " + text);
        if (TextUtils.isEmpty(text)) {
            return ERROR1;
        }
        if (text.length() < 2) {

        }
        if (false == valiadNickname(text)) {
            return ERROR2;
        }
        return 0;
    }

    private boolean valiadNickname(String str) {
        // 将字符串转换成char[]
        char[] charArray = str.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if (false == checkChar(charArray[i])) {
                Log.d(TAG, "valiadNickname: char: " + charArray[i]);
                return false;
            }
        }
        return true;
    }

    private boolean checkChar(char c) {
        Pattern p = Pattern.compile("[0-9]*");
        Matcher m = p.matcher(String.valueOf(c));
        if (m.matches()) {
            return true;
        }
        p = Pattern.compile("[a-zA-Z]");
        m = p.matcher(String.valueOf(c));
        if (m.matches()) {
            return true;
        }
        p = Pattern.compile("[\u4e00-\u9fa5]");
        m = p.matcher(String.valueOf(c));
        if (m.matches()) {
            return true;
        }
        return false;
    }
}

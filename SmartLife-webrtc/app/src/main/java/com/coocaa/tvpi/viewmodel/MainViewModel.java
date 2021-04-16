package com.coocaa.tvpi.viewmodel;

import android.text.TextUtils;
import android.util.Log;

import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.data.account.TpTokenInfo;
import com.coocaa.smartscreen.network.exception.ApiException;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.LoginRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;
import com.coocaa.tvpi.module.homepager.main.SmartScreenViewModel;
import com.coocaa.tvpi.module.login.UserInfoCenter;
import com.coocaa.tvpi.module.login.provider.ProviderClient;
import com.google.gson.Gson;

public class MainViewModel extends BaseViewModel {

    private static final String TAG = SmartScreenViewModel.class.getSimpleName();

    public void getTpToken() {
        if (!UserInfoCenter.getInstance().isLogin()) {
            return;
        }
        Repository.get(LoginRepository.class)
                .getTpToken(UserInfoCenter.getInstance().getAccessToken())
                .setCallback(new BaseRepositoryCallback<TpTokenInfo>(ApiException.AUTH_TOKEN_EXPIRED_ACCOUNT) {
                    @Override
                    public void onSuccess(TpTokenInfo tpTokenInfo) {
                        super.onSuccess(tpTokenInfo);
                        Log.d(TAG, "onSuccess: " + tpTokenInfo.tp_token);
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                    }
                });
    }


    public void updateUserInfo() {
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
                        ProviderClient.getClient().saveInfo(accessToken, new Gson().toJson(coocaaUserInfo));
                        Repository.get(LoginRepository.class).saveCoocaaUserInfo(coocaaUserInfo);
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                    }
                });
    }
}

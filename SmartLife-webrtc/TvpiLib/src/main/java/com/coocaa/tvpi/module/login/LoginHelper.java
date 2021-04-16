package com.coocaa.tvpi.module.login;

import android.text.TextUtils;
import android.util.Log;

import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.data.device.RegisterLogin;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.LoginRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.module.login.provider.ProviderClient;
import com.google.gson.Gson;

/**
 * @ClassName LoginManager
 * @Description TODO (write something)
 * @User wuhaiyuan
 * @Date 4/8/21
 * @Version TODO (write something)
 */
public class LoginHelper {

    private static final String TAG = LoginHelper.class.getSimpleName();

    public static void loginByAccessToken(String accessToken) {
        Repository.get(LoginRepository.class)
                .getCoocaaUserInfo(accessToken)
                .setCallback(new BaseRepositoryCallback<CoocaaUserInfo>() {
                    @Override
                    public void onSuccess(CoocaaUserInfo coocaaUserInfo) {
                        Log.d("LoginHelper", "loginByAccessToken success : " + coocaaUserInfo);
                        syncLoginData(accessToken, coocaaUserInfo);

                        Repository.get(LoginRepository.class)
                                .registerDevice(accessToken,coocaaUserInfo.nick_name,coocaaUserInfo.open_id)
                                .setCallback(new BaseRepositoryCallback<RegisterLogin>() {
                                    @Override
                                    public void onSuccess(RegisterLogin registerLogin) {
                                        Repository.get(LoginRepository.class)
                                                .updateDeviceInfo(registerLogin.access_token,coocaaUserInfo.nick_name,coocaaUserInfo.open_id)
                                                .setCallback(new BaseRepositoryCallback<Integer>() {
                                                    @Override
                                                    public void onFailed(Throwable e) {
                                                        super.onFailed(e);
                                                    }
                                                });


                                        SSConnectManager.getInstance().resetLsid(
                                                registerLogin.zpLsid,
                                                registerLogin.access_token,
                                                coocaaUserInfo.mobile);

                                        Repository.get(LoginRepository.class).saveCoocaaUserInfo(coocaaUserInfo);
                                        Repository.get(LoginRepository.class).saveToken(accessToken);
                                        Repository.get(LoginRepository.class).saveDeviceRegisterLoginInfo(registerLogin);
                                    }

                                    @Override
                                    public void onFailed(Throwable e) {
                                        super.onFailed(e);
                                    }
                                });
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                    }
                });
    }


    private static void syncLoginData(String accessToken, CoocaaUserInfo coocaaUserInfo) {
        Log.d(TAG, "syncLoginData: accessToken = " + accessToken);
        Log.d(TAG, "syncLoginData: userInfo = " + coocaaUserInfo);
        if (TextUtils.isEmpty(coocaaUserInfo.access_token) || "null".equalsIgnoreCase(coocaaUserInfo.access_token)) {
            coocaaUserInfo.access_token = accessToken;
        }
        ProviderClient.getClient().saveInfo(accessToken, new Gson().toJson(coocaaUserInfo));
    }

}
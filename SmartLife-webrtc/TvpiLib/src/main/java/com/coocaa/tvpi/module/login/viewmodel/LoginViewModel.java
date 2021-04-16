package com.coocaa.tvpi.module.login.viewmodel;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.smartscreen.connect.SSConnectManager;
import com.coocaa.smartscreen.data.account.AccountLoginInfo;
import com.coocaa.smartscreen.data.account.CoocaaUserInfo;
import com.coocaa.smartscreen.data.account.TpTokenInfo;
import com.coocaa.smartscreen.data.device.RegisterLogin;
import com.coocaa.smartscreen.repository.Repository;
import com.coocaa.smartscreen.repository.service.LoginRepository;
import com.coocaa.tvpi.base.BaseRepositoryCallback;
import com.coocaa.tvpi.base.mvvm.BaseViewModel;
import com.coocaa.tvpi.module.login.provider.ProviderClient;
import com.google.gson.Gson;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * 登录模块的ViewModel
 * Created by songxing on 2020/7/8
 */
public class LoginViewModel extends BaseViewModel {

    private MutableLiveData<Bitmap> imageCaptcha = new MutableLiveData<>();
    private MutableLiveData<Boolean> smsCaptcha = new MutableLiveData<>();
    private MutableLiveData<Boolean> smsLoginResult = new MutableLiveData<>();
    private MutableLiveData<Boolean> oneKeyLoginResult = new MutableLiveData<>();

    public LoginViewModel() {
        Log.d(TAG, "LoginViewModel: init");
    }

    public LiveData<Bitmap> getImageCaptcha(int width, int height) {
        Repository.get(LoginRepository.class)
                .getImageCaptcha(width, height)
                .setCallback(new BaseRepositoryCallback<Bitmap>() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        imageCaptcha.setValue(bitmap);
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                        imageCaptcha.setValue(null);
                    }
                });
        return imageCaptcha;
    }

    public LiveData<Boolean> getSmsCaptcha(String phoneNum) {
        Repository.get(LoginRepository.class)
                .getSmsCaptcha(phoneNum)
                .setCallback(new BaseRepositoryCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        smsCaptcha.setValue(result);
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                        smsCaptcha.setValue(false);
                    }
                });
        return smsCaptcha;
    }


    public LiveData<Boolean> getSmsCaptcha(String phoneNum, String imageCaptcha) {
        Repository.get(LoginRepository.class)
                .getSmsCaptcha(phoneNum, imageCaptcha)
                .setCallback(new BaseRepositoryCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        smsCaptcha.setValue(result);
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                        smsCaptcha.setValue(false);
                    }
                });
        return smsCaptcha;
    }

    //todo call hell
    public LiveData<Boolean> oneKeyLogin(String sanYanToken) {
        Repository.get(LoginRepository.class)
                .oneKeyLogin(sanYanToken)
                .setCallback(new BaseRepositoryCallback<AccountLoginInfo>() {
                    @Override
                    public void onSuccess(AccountLoginInfo accountLoginInfo) {
                        Repository.get(LoginRepository.class)
                                .getTpToken(accountLoginInfo.access_token)
                                .setCallback(new BaseRepositoryCallback<TpTokenInfo>() {
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

                        Repository.get(LoginRepository.class)
                                .getCoocaaUserInfo(accountLoginInfo.access_token)
                                .setCallback(new BaseRepositoryCallback<CoocaaUserInfo>() {
                                    @Override
                                    public void onSuccess(CoocaaUserInfo coocaaUserInfo) {
                                        syncLoginData(accountLoginInfo.access_token, coocaaUserInfo);

                                        Repository.get(LoginRepository.class)
                                                .registerDevice(accountLoginInfo.access_token,coocaaUserInfo.nick_name,coocaaUserInfo.open_id)
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
                                                        Repository.get(LoginRepository.class).saveToken(accountLoginInfo.access_token);
                                                        Repository.get(LoginRepository.class).saveDeviceRegisterLoginInfo(registerLogin);
                                                        oneKeyLoginResult.setValue(true);

                                                        /*Repository.get(LoginRepository.class)
                                                                .getYunXinUserInfo(accountLoginInfo.access_token)
                                                                .setCallback(new BaseRepositoryCallback<YunXinUserInfo>() {
                                                                    @Override
                                                                    public void onSuccess(YunXinUserInfo yunXinUserInfo) {
                                                                        LoginInfo info = new LoginInfo(yunXinUserInfo.yxOpenId, yunXinUserInfo.yxThirdToken); // 账号、密码
                                                                        NIMClient.getService(AuthService.class)
                                                                                .login(info)
                                                                                .setCallback(new RequestCallback<LoginInfo>() {
                                                                                    @Override
                                                                                    public void onSuccess(LoginInfo loginInfo) {
                                                                                        Log.w(TAG, "云信登录成功" + loginInfo.toString());
                                                                                        //登录成功后才保存
                                                                                        Repository.get(LoginRepository.class).saveToken(accountLoginInfo.access_token);
                                                                                        Repository.get(LoginRepository.class).saveDeviceRegisterLoginInfo(registerLogin);
                                                                                        Repository.get(LoginRepository.class).saveCoocaaUserInfo(coocaaUserInfo);
                                                                                        Repository.get(LoginRepository.class).saveYunXinUserInfo(yunXinUserInfo);
                                                                                        oneKeyLoginResult.setValue(true);
                                                                                    }

                                                                                    @Override
                                                                                    public void onFailed(int code) {
                                                                                        Log.w(TAG, "云信失败" + code);
                                                                                        oneKeyLoginResult.setValue(false);
                                                                                    }

                                                                                    @Override
                                                                                    public void onException(Throwable exception) {
                                                                                        Log.w(TAG, "云信登录失败" + exception.getMessage());
                                                                                        oneKeyLoginResult.setValue(false);
                                                                                    }
                                                                                });
                                                                    }

                                                                    @Override
                                                                    public void onFailed(Throwable e) {
                                                                        super.onFailed(e);
                                                                        oneKeyLoginResult.setValue(false);
                                                                    }
                                                                });*/
                                                    }

                                                    @Override
                                                    public void onFailed(Throwable e) {
                                                        super.onFailed(e);
                                                        oneKeyLoginResult.setValue(false);
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onFailed(Throwable e) {
                                        super.onFailed(e);
                                        oneKeyLoginResult.setValue(false);
                                    }
                                });
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                        oneKeyLoginResult.setValue(false);
                    }
                });
        return oneKeyLoginResult;
    }


    //todo call hell
    public LiveData<Boolean> smsLogin(String phoneNum, String smsCaptcha) {
        Repository.get(LoginRepository.class)
                .smsCaptchaLogin(phoneNum, smsCaptcha)
                .setCallback(new BaseRepositoryCallback<AccountLoginInfo>() {
                    @Override
                    public void onSuccess(AccountLoginInfo accountLoginInfo) {
                        Repository.get(LoginRepository.class)
                                .getTpToken(accountLoginInfo.access_token)
                                .setCallback(new BaseRepositoryCallback<TpTokenInfo>() {
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
                        ;

                        Repository.get(LoginRepository.class)
                                .getCoocaaUserInfo(accountLoginInfo.access_token)
                                .setCallback(new BaseRepositoryCallback<CoocaaUserInfo>() {
                                    @Override
                                    public void onSuccess(CoocaaUserInfo coocaaUserInfo) {

                                        syncLoginData(accountLoginInfo.access_token, coocaaUserInfo);

                                        Repository.get(LoginRepository.class)
                                                .registerDevice(accountLoginInfo.access_token,coocaaUserInfo.nick_name,coocaaUserInfo.open_id)
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
                                                        Repository.get(LoginRepository.class).saveToken(accountLoginInfo.access_token);
                                                        Repository.get(LoginRepository.class).saveDeviceRegisterLoginInfo(registerLogin);
                                                        smsLoginResult.setValue(true);

                                                        /*Repository.get(LoginRepository.class)
                                                                .getYunXinUserInfo(accountLoginInfo.access_token)
                                                                .setCallback(new BaseRepositoryCallback<YunXinUserInfo>() {
                                                                    @Override
                                                                    public void onSuccess(YunXinUserInfo yunXinUserInfo) {

                                                                        LoginInfo info = new LoginInfo(yunXinUserInfo.yxOpenId, yunXinUserInfo.yxThirdToken); // 账号、密码
                                                                        NIMClient.getService(AuthService.class)
                                                                                .login(info)
                                                                                .setCallback(new RequestCallback<LoginInfo>() {
                                                                                    @Override
                                                                                    public void onSuccess(LoginInfo loginInfo) {
                                                                                        Log.w(TAG, "云信登录成功" + loginInfo.toString());
                                                                                        Repository.get(LoginRepository.class).saveCoocaaUserInfo(coocaaUserInfo);
                                                                                        Repository.get(LoginRepository.class).saveToken(accountLoginInfo.access_token);
                                                                                        Repository.get(LoginRepository.class).saveDeviceRegisterLoginInfo(registerLogin);
                                                                                        Repository.get(LoginRepository.class).saveYunXinUserInfo(yunXinUserInfo);
                                                                                        smsLoginResult.setValue(true);
                                                                                    }

                                                                                    @Override
                                                                                    public void onFailed(int code) {
                                                                                        Log.w(TAG, "云信失败" + code);
                                                                                        smsLoginResult.setValue(false);
                                                                                    }

                                                                                    @Override
                                                                                    public void onException(Throwable exception) {
                                                                                        Log.w(TAG, "云信登录失败" + exception.getMessage());
                                                                                        smsLoginResult.setValue(false);
                                                                                    }
                                                                                });
                                                                    }

                                                                    @Override
                                                                    public void onFailed(Throwable e) {
                                                                        super.onFailed(e);
                                                                        smsLoginResult.setValue(false);
                                                                    }
                                                                });*/
                                                    }

                                                    @Override
                                                    public void onFailed(Throwable e) {
                                                        super.onFailed(e);
                                                        smsLoginResult.setValue(false);
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onFailed(Throwable e) {
                                        super.onFailed(e);
                                        smsLoginResult.setValue(false);
                                    }
                                });
                    }

                    @Override
                    public void onFailed(Throwable e) {
                        super.onFailed(e);
                        smsLoginResult.setValue(false);
                    }
                });

        return smsLoginResult;
    }


    private void syncLoginData(String accessToken, CoocaaUserInfo coocaaUserInfo) {
        Log.d(TAG, "syncLoginData: accessToken = " + accessToken);
        Log.d(TAG, "syncLoginData: userInfo = " + coocaaUserInfo);
        if (TextUtils.isEmpty(coocaaUserInfo.access_token) || "null".equalsIgnoreCase(coocaaUserInfo.access_token)) {
            coocaaUserInfo.access_token = accessToken;
        }
        ProviderClient.getClient().saveInfo(accessToken, new Gson().toJson(coocaaUserInfo));
//        LoginResultData loginResultData = new LoginResultData();
//        loginResultData.data = new LoginData();
//        loginResultData.data.access_token = accessToken;
//        loginResultData.data.account = new Gson().toJson(coocaaUserInfo);
//        Log.d(TAG, "syncLoginData: coocaaUserInfo = " + loginResultData.data.account);
//        boolean result = ProviderUtils.INSTANCE.syncLoginData(loginResultData);
//        Log.d(TAG, "onNext: ProviderUtils result" + result);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "LoginViewModel: onCleared ");
    }
}

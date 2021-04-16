package com.coocaa.tvpi.module.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.Observer;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartmall.data.api.HttpSubscribe;
import com.coocaa.smartmall.data.api.HttpThrowable;
import com.coocaa.smartmall.data.mobile.data.LoginResult;
import com.coocaa.smartmall.data.mobile.http.MobileRequestService;
import com.coocaa.tvpi.base.mvvm.BaseViewModelActivity;
import com.coocaa.tvpi.event.AgreementEvent;
import com.coocaa.tvpi.event.UserLoginEvent;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.module.log.LoginEvent;
import com.coocaa.tvpi.module.login.viewmodel.LoginViewModel;
import com.example.sanyansdk.SanYanManager;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @ClassName LoginActivity
 * @User wuhaiyuan
 * @Date 2020/4/27
 * 一键登录
 */
public class LoginActivity extends BaseViewModelActivity<LoginViewModel> {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private boolean backMainActivity = false;   //按返回键跳转首页,用于登录被踢出的情况

    public static boolean checkLogin(Context context) {
        if (!UserInfoCenter.getInstance().isLogin()) {
            LoginActivity.start(context);
            return false;
        }
        return true;
    }

    public static void start(Context context) {
        start(context, false);
    }

    public static void start(Context context, boolean backMainActivity) {
        Intent starter = new Intent(context, LoginActivity.class);
        starter.putExtra("backMainActivity", backMainActivity);
        starter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        EventBus.getDefault().register(this);
        parserIntent();
        setListener();
        oneKeyLogin();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAgreementEvent(AgreementEvent event) {
        if(event != null) {
            if (event.isAgree) {
                SanYanManager.getInstance().setCheckboxValue(true);
            } else {
                ToastUtils.getInstance().showGlobalLong("请勾选同意用户协议与隐私政策");
            }
        }
    }


    private void parserIntent() {
        if (getIntent() != null) {
            backMainActivity = getIntent().getBooleanExtra("backMainActivity", false);
        }
    }


    private void setListener() {
        SanYanManager.getInstance().setPrivacyCheckListener(new SanYanManager.PrivacyCheckListener() {
            @Override
            public void isPrivacyCheckWhenOneKeyLogin(boolean isChecked) {
                if (!isChecked) {
                    startActivity(new Intent(LoginActivity.this, LoginAgreementActivity.class));
                }
            }
        });
    }

    private void oneKeyLogin() {
        SanYanManager.getInstance().openLoginAuth(new SanYanManager.LoginAuthResult() {
            @Override
            public void getOpenLoginAuthStatus(int code, String result) {
                if (1000 == code) {
                    //拉起授权页成功
                    Log.d(TAG, "拉起授权页成功： _code==" + code + "   _result==" + result);
                } else {
                    //拉起授权页失败
                    Log.d(TAG, "拉起授权页失败： _code==" + code + "   _result==" + result);
                    SmsLoginActivity.start(LoginActivity.this, backMainActivity);
                    finish();
                }
            }

            @Override
            public void getOneKeyLoginStatus(int code, String result) {
                if (1011 == code) {
                    goBack();
                    Log.d(TAG, "用户点击授权页返回： _code==" + code + "   _result==" + result);
                } else if (1000 == code) {
                    Log.d(TAG, "用户点击登录获取token成功： _code==" + code + "   _result==" + result);
                    try {
                        submitLoginType(true);
                        JSONObject object = new JSONObject(result);
                        String token = object.getString("token");
                        doOneKeyLogin(token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "用户点击登录获取token失败： _code==" + code + "   _result==" + result);
                    goBack();
                }
            }

            @Override
            public void onCustomClick(Context context, View view) {
                submitLoginType(false);
                SmsLoginActivity.start(LoginActivity.this, backMainActivity);
                finish();
            }
        }, this);

    }

    private void doOneKeyLogin(String token) {
        viewModel.oneKeyLogin(token).observeForever(oneKeyLoginObserver);
    }

    private final Observer<Boolean> oneKeyLoginObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean success) {
            Log.d(TAG, "doOneKeyLogin onChanged: " + success);
            SanYanManager.getInstance().finishLoginAuth();
            dismissLoading();
            finish();
            if (success) {
                ToastUtils.getInstance().showGlobalLong("登录成功");
                MobileRequestService.getInstance().setLoginToken(UserInfoCenter.getInstance().getAccessToken());
                MobileRequestService.getInstance().login(new HttpSubscribe<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult result) {
                        Log.d(TAG, "onSuccess: " + result);
                    }

                    @Override
                    public void onError(HttpThrowable error) {
                        Log.d(TAG, "onError: " + error.toString());
                    }
                }, UserInfoCenter.getInstance().getAccessToken());
                String ACCOUNT_CHANGED = "com.tianci.user.account_changed";
                sendBroadcast(new Intent(ACCOUNT_CHANGED));
                EventBus.getDefault().post(new UserLoginEvent(true));
                LoginEvent.submitLogin("login");
            }
            submitLoginResult(success);
        }
    };

    private void goBack() {
        if (backMainActivity) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.kickout.back");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        finish();
    }

    private void submitLoginType(boolean oneKeyLogin) {
        LogParams params = LogParams.newParams()
                .append("btn_name", oneKeyLogin ? "this_number_login" : "other_number_login");
        LogSubmit.event("login_type_btn_clicked", params.getParams());
    }

    private void submitLoginResult(boolean success) {
        LogParams params = LogParams.newParams()
                .append("login_result", success ? "success" : "fail");
        LogSubmit.event("login_result", params.getParams());
    }
}

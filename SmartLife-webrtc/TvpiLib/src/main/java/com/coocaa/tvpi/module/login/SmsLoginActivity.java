package com.coocaa.tvpi.module.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartmall.data.api.HttpSubscribe;
import com.coocaa.smartmall.data.api.HttpThrowable;
import com.coocaa.smartmall.data.mobile.data.LoginResult;
import com.coocaa.smartmall.data.mobile.http.MobileRequestService;
import com.coocaa.smartscreen.network.common.Constants;
import com.coocaa.tvpi.base.mvvm.BaseViewModelActivity;
import com.coocaa.tvpi.event.UserLoginEvent;
import com.coocaa.tvpi.module.log.LogParams;
import com.coocaa.tvpi.module.log.LogSubmit;
import com.coocaa.tvpi.module.log.LoginEvent;
import com.coocaa.tvpi.module.login.view.LoginAgreementDialog;
import com.coocaa.tvpi.module.login.viewmodel.LoginViewModel;
import com.coocaa.tvpi.util.OnDebouncedClick;
import com.coocaa.tvpi.util.TextWatchAdapter;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpi.view.DeletableEditText;
import com.coocaa.tvpi.view.LoadingButton;
import com.coocaa.tvpi.view.webview.SimpleWebViewActivity;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import static com.coocaa.tvpi.common.UMengEventId.LOGIN_IMAGE_CAPTCHA;
import static com.coocaa.tvpi.common.UMengEventId.LOGIN_SMS;

/**
 * 验证码登录
 * Created by songxing on 2020/7/8
 */
public class SmsLoginActivity extends BaseViewModelActivity<LoginViewModel> {
    private static final String TAG = SmsLoginActivity.class.getSimpleName();
    private static final int IMAGE_CAPTCHA_WIDTH = 80;
    private static final int IMAGE_CAPTCHA_HEIGHT = 33;

    private CommonTitleBar titleBar;
    private DeletableEditText etPhoneNumber;
    private DeletableEditText etImageCaptcha;
    private DeletableEditText etSmsCaptcha;
    private RelativeLayout imageCaptchaLayout;
    private ImageView ivImageCaptcha;
    private TextView tvImageCaptchaTip;
    private TextView tvSmsCaptcha;
    private TextView tvAgreementTip;
    private LoadingButton tvLogin;
    private CheckBox cbAgreement;

    private boolean showImageCaptcha = false;   //是否显示图片验证码
    private boolean isCountdown;
    private CountDownTimer countDownTimer;
    private int smsErrorCounter;

    private LoginAgreementDialog loginAgreementDialog;

    private boolean backMainActivity = false;   //按返回键跳转首页,用于登录被踢出的情况

    public static void start(Context context, boolean backMainActivity) {
        Intent starter = new Intent(context, SmsLoginActivity.class);
        starter.putExtra("backMainActivity", backMainActivity);
        starter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_login);
        overridePendingTransition(R.anim.push_bottom_in, R.anim.alpha_outer);
        parserIntent();
        initViews();
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

    private void parserIntent() {
        if (getIntent() != null) {
            backMainActivity = getIntent().getBooleanExtra("backMainActivity", false);
        }
    }

    protected void initViews() {
        titleBar = findViewById(R.id.titleBar);
        etPhoneNumber = findViewById(R.id.etPhoneNum);
        etImageCaptcha = findViewById(R.id.etImageCaptcha);
        etSmsCaptcha = findViewById(R.id.etSmsCaptcha);
        ivImageCaptcha = findViewById(R.id.ivImageCaptcha);
        tvSmsCaptcha = findViewById(R.id.tvSmsCaptchaTip);
        tvLogin = findViewById(R.id.loginButton);
        tvAgreementTip = findViewById(R.id.tvAgreementTip);
        tvAgreementTip.setMovementMethod(LinkMovementMethod.getInstance());
        tvAgreementTip.setText(createAgreementStringBuilder());
        tvAgreementTip.setHighlightColor(getResources().getColor(R.color.transparent));
        imageCaptchaLayout = findViewById(R.id.imageCaptchaLayout);
        tvImageCaptchaTip = findViewById(R.id.tvImageCaptchaTip);
        cbAgreement = findViewById(R.id.cbAgreement);
        tvLogin.setEnabled(false);
        ivImageCaptcha.setOnClickListener(viewClickLis);
        tvSmsCaptcha.setOnClickListener(new OnDebouncedClick(viewClickLis));
        tvLogin.setOnClickListener(new OnDebouncedClick(viewClickLis));
        etImageCaptcha.setVisibility(View.GONE);
        imageCaptchaLayout.setVisibility(View.GONE);
        etPhoneNumber.addTextChangedListener(new PhoneTextWatcher());
        etImageCaptcha.addTextChangedListener(new ImageCaptchaTextWatcher());
        etSmsCaptcha.addTextChangedListener(new SmsCaptchaTextWatcher());
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (CommonTitleBar.ClickPosition.LEFT == position) {
                    goBack();
                }
            }
        });

        //把获取验证码设为不可点击
        setSmsCaptchaButtonEnable(false);
    }


    private final View.OnClickListener viewClickLis = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.ivImageCaptcha) {
                getImageCaptcha();
            } else if (id == R.id.tvSmsCaptchaTip) {
                getSmsCaptcha();
            } else if (id == R.id.loginButton) {
                smsLogin();
            }
        }
    };

    private void getImageCaptcha() {
        MobclickAgent.onEvent(this, LOGIN_IMAGE_CAPTCHA);
        viewModel.getImageCaptcha(IMAGE_CAPTCHA_WIDTH, IMAGE_CAPTCHA_HEIGHT)
                .observe(this, imageCaptchaObserver);
    }

    private final Observer<Bitmap> imageCaptchaObserver = new Observer<Bitmap>() {
        @Override
        public void onChanged(Bitmap bitmap) {
            if (bitmap != null) {
                tvImageCaptchaTip.setText("");
                ivImageCaptcha.setImageBitmap(bitmap);
            } else {
                tvImageCaptchaTip.setText("点击刷新");
                ivImageCaptcha.setImageBitmap(null);
            }
        }
    };

    private void getSmsCaptcha() {
        setSmsCaptchaButtonEnable(false);
        String phone = Objects.requireNonNull(etPhoneNumber.getText()).toString();
        if (showImageCaptcha) {
            String imageCaptcha = Objects.requireNonNull(etImageCaptcha.getText()).toString();
            viewModel.getSmsCaptcha(phone, imageCaptcha).observe(this, smsCaptchaObserver);
        } else {
            viewModel.getSmsCaptcha(phone).observe(this, smsCaptchaObserver);
        }
        MobclickAgent.onEvent(this, LOGIN_SMS);
    }

    private final Observer<Boolean> smsCaptchaObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean success) {
            setSmsCaptchaButtonEnable(true);
            if (success) {
                countdownSmsCaptcha();
                ToastUtils.getInstance().showGlobalLong("验证码已发送");
            } else {
                getImageCaptcha();
            }
        }
    };

    private void smsLogin() {
        if (!cbAgreement.isChecked()) {
            if (loginAgreementDialog == null) {
                loginAgreementDialog = new LoginAgreementDialog();
                loginAgreementDialog.setLoginAgreementListener(new LoginAgreementDialog.LoginAgreementListener() {
                    @Override
                    public void onAgreeClick() {
                        cbAgreement.setChecked(true);
                    }
                });
            }
            if (!loginAgreementDialog.isAdded()) {
                loginAgreementDialog.show(getSupportFragmentManager(), "LoginAgreementDialog");
            }
            return;
        }

        if (etPhoneNumber.getText() == null || etSmsCaptcha.getText() == null) {
            Log.d(TAG, "smsLogin: etPhoneNumber or etSmsCaptcha getText() is null");
            return;
        }

        tvLogin.start();
        String phone = etPhoneNumber.getText().toString();
        String smsCaptcha = etSmsCaptcha.getText().toString();
        viewModel.smsLogin(phone, smsCaptcha).observe(this, smsLoginObserver);
    }

    private final Observer<Boolean> smsLoginObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean success) {
            tvLogin.complete();
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
                finish();
            } else {
                smsErrorCounter++;
                if (smsErrorCounter == 3) {
                    showImageCaptcha = true;
                    etImageCaptcha.setVisibility(View.VISIBLE);
                    imageCaptchaLayout.setVisibility(View.VISIBLE);
                    tvLogin.setEnabled(canLogin());
                    getImageCaptcha();
                }
            }
            submitLoginResult(success);
        }
    };


    //手机号码输入框监听
    private class PhoneTextWatcher extends TextWatchAdapter {
        @Override
        public void onTextChanged(CharSequence c, int start, int before, int count) {
            setSmsCaptchaButtonEnable(canInputSmsCaptcha());
            tvLogin.setEnabled(canLogin());
        }
    }

    //图片验证码输入框监听
    private class ImageCaptchaTextWatcher extends TextWatchAdapter {
        @Override
        public void onTextChanged(CharSequence c, int start, int before, int count) {
            setSmsCaptchaButtonEnable(canInputSmsCaptcha());
            tvLogin.setEnabled(canLogin());
        }
    }

    //短信验证码输入框监听
    private class SmsCaptchaTextWatcher extends TextWatchAdapter {
        @Override
        public void onTextChanged(CharSequence c, int start, int before, int count) {
            tvLogin.setEnabled(canLogin());
        }
    }

    private SpannableStringBuilder createAgreementStringBuilder() {
        String userAgreementBeforeStr = "我已阅读并同意：";
        String userAgreementStr = "《用户协议》";
        String privacyBeforeStr = "";
        String privacyStr = "《隐私条款》";
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(userAgreementBeforeStr)
                .append(userAgreementStr)
                .append(privacyBeforeStr)
                .append(privacyStr);

        int agreementStart = userAgreementBeforeStr.length();
        int agreementEnd = userAgreementBeforeStr.length() + userAgreementStr.length();
        builder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                enterUserAgreementWebView();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
            }
        }, agreementStart, agreementEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int privacyStart = userAgreementBeforeStr.length() + userAgreementStr.length() + privacyBeforeStr.length();
        int privacyEnd = userAgreementBeforeStr.length() + userAgreementStr.length() + privacyBeforeStr.length() + privacyStr.length();
        builder.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.color_red)),
                agreementStart, agreementEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                enterPrivacyWebView();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
            }
        }, privacyStart, privacyEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.color_red)),
                privacyStart, privacyEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }


    //更新获取验证码的状态，是否可点击，txt颜色
    private void setSmsCaptchaButtonEnable(boolean isEnable) {
        //短信验证码倒计时过程不用许改变状态
        if (isCountdown) {
            return;
        }
        tvSmsCaptcha.setEnabled(isEnable);
        if (isEnable) {
            tvSmsCaptcha.setTextColor(getResources().getColor(R.color.c_2));
        } else {
            tvSmsCaptcha.setTextColor(getResources().getColor(R.color.c_5));
        }
    }


    //是否能获取验证码
    private boolean canInputSmsCaptcha() {
        if (showImageCaptcha) {
            return etPhoneNumber.getText() != null && etPhoneNumber.getText().length() == 11
                    && etImageCaptcha.getText() != null && etImageCaptcha.getText().length() == 4;
        } else {
            return etPhoneNumber.getText() != null && etPhoneNumber.getText().length() == 11;
        }
    }

    //是否能登录
    private boolean canLogin() {
        return canInputSmsCaptcha() && etSmsCaptcha.getText() != null
                && etSmsCaptcha.getText().length() == 6;
    }


    //获取短信验证码倒计时
    private void countdownSmsCaptcha() {
        if (isCountdown) {
            return;
        }
        setSmsCaptchaButtonEnable(false);
        if (countDownTimer == null) {
            countDownTimer = new CountDownTimer(60 * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    tvSmsCaptcha.setText(String.format(getString(R.string.request_captcha_after), millisUntilFinished / 1000 + ""));
                }

                @Override
                public void onFinish() {
                    isCountdown = false;
                    tvSmsCaptcha.setText(getString(R.string.yunxin_get_verification_code_again));
                    setSmsCaptchaButtonEnable(true);
                }
            };
        }
        countDownTimer.cancel();
        countDownTimer.start();
        isCountdown = true;
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.alpha_enter, R.anim.push_bottom_out);
    }

    private void enterPrivacyWebView() {
        String privacy = "http://sky.fs.skysrt.com/statics/server/kkzp_privacy.html";
        Intent intent = new Intent(SmsLoginActivity.this, SimpleWebViewActivity.class);
//        intent.putExtra(Constants.Cordova.url, ConstantsUrl.URL_USER_PRIVACY);
        intent.putExtra(Constants.Cordova.url, privacy);
        startActivity(intent);
    }

    private void enterUserAgreementWebView() {
        String agreement = "http://sky.fs.skysrt.com/statics/server/kkzp_service.html";
        Intent intent = new Intent(SmsLoginActivity.this, SimpleWebViewActivity.class);
        intent.putExtra(Constants.Cordova.url, agreement);
        startActivity(intent);
    }

    private void goBack() {
        if (backMainActivity) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.kickout.back");
            startActivity(intent);
        }
        finish();
    }

    private void submitLoginResult(boolean success) {
        LogParams params = LogParams.newParams()
                .append("login_result", success ? "success" : "fail");
        LogSubmit.event("login_result", params.getParams());
    }
}

package com.coocaa.tvpi.module.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.smartscreen.network.common.Constants;
import com.coocaa.tvpi.event.AgreementEvent;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.view.webview.SimpleWebViewActivity;
import com.coocaa.tvpilib.R;

import org.greenrobot.eventbus.EventBus;

/**
 * 登录协议
 * Created by songxing on 2020/11/12
 */
public class LoginAgreementActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_login_agreement);
        StatusBarHelper.translucent(this);
        initView();
    }

    private void initView(){
        View cancel = findViewById(R.id.bt_cancel);
        View agree = findViewById(R.id.bt_agree);
        TextView tvContent = findViewById(R.id.tv_content);
        tvContent.setMovementMethod(LinkMovementMethod.getInstance());
        tvContent.setText(createAgreementStringBuilder());
        tvContent.setHighlightColor(getResources().getColor(R.color.transparent));

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new AgreementEvent(false));
                finish();
            }
        });

        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new AgreementEvent(true));
                finish();
            }
        });
    }

    private SpannableStringBuilder createAgreementStringBuilder() {
        String userAgreementBeforeStr = "亲爱的用户，在登录之前，请您阅读并充分理解共享屏的";
        String userAgreementStr = "《用户协议》";
        String centerStr = "和";
        String privacyStr = "《隐私协议》";
        String privacyAfterStr = "，点击“同意”表示您已经充分阅读并接受以上协议内容。";
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(userAgreementBeforeStr)
                .append(userAgreementStr)
                .append(centerStr)
                .append(privacyStr)
                .append(privacyAfterStr);
        int agreementStart = userAgreementBeforeStr.length();
        int agreementEnd = userAgreementBeforeStr.length() + userAgreementStr.length();
        builder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                String agreement = "http://sky.fs.skysrt.com/statics/server/kkzp_service.html";
                Intent intent = new Intent(LoginAgreementActivity.this, SimpleWebViewActivity.class);
                intent.putExtra(Constants.Cordova.url, agreement);
                intent.putExtra(Constants.Cordova.title, "用户协议");
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
            }
        }, agreementStart, agreementEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        int privacyStart = userAgreementBeforeStr.length() + userAgreementStr.length() + centerStr.length();
        int privacyEnd = userAgreementBeforeStr.length() + userAgreementStr.length() + centerStr.length() + privacyStr.length();
        builder.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.color_red)),
                agreementStart, agreementEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                String privacy = "http://sky.fs.skysrt.com/statics/server/kkzp_privacy.html";
                Intent intent = new Intent(LoginAgreementActivity.this, SimpleWebViewActivity.class);
                intent.putExtra(Constants.Cordova.url, privacy);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
            }
        }, privacyStart, privacyEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorText_F86239)),
                privacyStart, privacyEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }
}

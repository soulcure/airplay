package com.coocaa.tvpi.module.mall;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.views.SDialog;
import com.coocaa.tvpi.module.mall.dialog.Save2AlbumDialog;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import androidx.annotation.NonNull;

/**
 * 客服页面
 * Created by wuhaiyuan on 2020/8/25
 */
public class CustomerServiceActivity extends BaseActivity {

    private static final String TAG = CustomerServiceActivity.class.getSimpleName();

    private CommonTitleBar titleBar;
    private TextView qrTipsTV, callTipsTV, callBtn;
    private ImageView qrIV;

    public static void start(Context context) {
        context.startActivity(new Intent(context, CustomerServiceActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_service);

        StatusBarHelper.translucent(getWindow());
        StatusBarHelper.setStatusBarLightMode(this);

        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
    }

    private void initViews() {
        titleBar = findViewById(R.id.titleBar);
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    finish();
                }
            }
        });

        String tips1 = "长按保存二维码，通过微信扫一扫识别加微信好友”，点击 <font color='#ff5b55'> " + "“小酷跟班”" + " </font>获取帮助。";
        qrTipsTV = findViewById(R.id.qr_tips_tv);
        qrTipsTV.setText(Html.fromHtml(tips1));

        String tips2 = "拨打电话  <font color='#ff5b55'> " + "400-168-8880" + " </font> ，或直接点击下方按钮与客服取得联系获取帮助。";

        SpannableString spannableString = new SpannableString("拨打电话 400-168-8880 ，或直接点击下方按钮与客服取得联系获取帮助。");

        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Log.d(TAG, "onClick: ");
                showCallDialog();
            }
        }, 5, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new NoUnderlineSpan(), 5, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.color_red)),
                5, 17, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        callTipsTV = findViewById(R.id.call_tips_tv);
//        callTipsTV.setText(Html.fromHtml(tips2));
        callTipsTV.setText(spannableString);
        callTipsTV.setMovementMethod(LinkMovementMethod.getInstance());//开始响应点击事件
        callTipsTV.setHighlightColor(getResources().getColor(R.color.transparent));//方法重新设置文字背景为透明色。

        callBtn = findViewById(R.id.call_btn);
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCallDialog();
            }
        });

        qrIV = findViewById(R.id.qr_iv);
        qrIV.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new Save2AlbumDialog().with(CustomerServiceActivity.this).show();
                return true;
            }
        });
    }

    private void showCallDialog() {
        final SDialog dialog = new SDialog(this,"拨打客服电话 400-168-8880", R.string.cancel, R.string.call,
                new SDialog.SDialog2Listener() {
                    @Override
                    public void onClick(boolean l, View view) {
                        if(!l) {
                            callPhone("400-168-8880");
                        }
                    }
                });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    /**
     * 拨打电话（直接拨打电话）
     * @param phoneNum 电话号码
     */
    private void callPhone(String phoneNum){

//        Intent intent = new Intent(Intent.ACTION_CALL);//需要权限
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        startActivity(intent);
    }

    // 创建一个自定义的去除下划线的 Span  内部类或者外部类,自行选择
    class NoUnderlineSpan extends UnderlineSpan {

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(ds.linkColor);
            ds.setUnderlineText(false);
        }
    }

}

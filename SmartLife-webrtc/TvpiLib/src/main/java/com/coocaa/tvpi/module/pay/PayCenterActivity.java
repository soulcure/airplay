package com.coocaa.tvpi.module.pay;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.publib.views.SDialog;
import com.coocaa.smartsdk.pay.PayResultEvent;
import com.coocaa.tvpi.module.pay.bean.PayConstant;
import com.coocaa.tvpilib.R;

import java.net.URLDecoder;

import swaiotos.runtime.np.NPAppletActivity;


public class PayCenterActivity extends NPAppletActivity {

    private ImageView aliPay = null; // 支付宝单选按钮
    private ImageView wcPay = null; // 微信单选按钮
    private Button btn = null;
    TextView amount = null;
    private FrameLayout wechatItem = null;
    private FrameLayout aliItem = null;
    public static boolean paylock = false;
    String TAG = "pay_tag";
    String request_pay_params="";
    public String id = "";
    SDialog confirmWeXinDialog;
    SDialog confirmAliDialog;
    public static PayCenterActivity instance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBackButtonVisible(true);
        initData();
        setContentView(R.layout.activity_checkout_page);
        init();
        instance = this;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        synchronized (iPayResultNotifyAty){
            paylock = false;
        }

    }

    private void initData(){
        Intent intent = this.getIntent();
        Uri uri = intent.getData();
        if (uri== null) {
            Log.d(TAG,"uri is null");
            return;
        }

        request_pay_params  = uri.getQueryParameter("request_pay_params");
        id  = uri.getQueryParameter("id");
        Log.d(TAG,"request_pay_params:"+request_pay_params);
        Log.d(TAG,"id:"+id);
    }

    private String getAmount(){
        if(TextUtils.isEmpty(request_pay_params)){
            return "-1(数据有误)";
        }
        String payParams = URLDecoder.decode(request_pay_params);
        String[] params = payParams.split("&");
        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            if("amount".equals(name))
                return "¥"+value;
        }
        return "-1(数据有误)";
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void init(){
        // 获取界面组件
        amount = findViewById(R.id.amount);
        amount.setText(getAmount());
        amount.getPaint().setFakeBoldText(true);

        aliPay = findViewById(R.id.ali_pay);

        wcPay =  findViewById(R.id.wechat_pay);



        btn = findViewById(R.id.enter_pay);
        btn.setOnClickListener(clickListener);


        wechatItem = findViewById(R.id.weixin_pay_item);
        wechatItem.setTag(0);
        wechatItem.setFocusable(true);
        wechatItem.setFocusableInTouchMode(true);
        wechatItem.setOnTouchListener(touchListener);
        wechatItem.setOnClickListener(payTypeClick);

        aliItem = findViewById(R.id.ali_pay_item);
        aliItem.setTag(1);
        aliItem.setFocusable(true);
        aliItem.setFocusableInTouchMode(true);
        aliItem.setOnTouchListener(touchListener);
        aliItem.setOnClickListener(payTypeClick);


        confirmWeXinDialog = new SDialog(this, "", "“共享屏”想要打开“微信”",
                R.string.confirm_cancel,R.string.confirm_ok,
                new SDialog.SDialog2Listener() {
                    @Override
                    public void onClick(boolean l, View view) {
                        if (l) {
                            confirmWeXinDialog.dismiss();
                        } else {
                            startPay(PayConstant.PAY_WE);


                        }
                    }
                });

//        confirmWeXinDialog.setClickCancel(false);

        confirmAliDialog = new SDialog(this, "", "“共享屏”想要打开“支付宝”",
                R.string.confirm_cancel,R.string.confirm_ok,
                new SDialog.SDialog2Listener() {
                    @Override
                    public void onClick(boolean l, View view) {
                        if (l) {
                            confirmAliDialog.dismiss();
                        } else {
                            startPay(PayConstant.PAY_ALI);
                        }
                    }
                });

    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isFastDoubleClick() || paylock)
                return;
            Log.d(TAG,"start change...");
            if((int)wechatItem.getTag() == 1){
                Log.d(TAG,"start.....come in wechat");
                if(!checkWechatInstalled()){
                    ToastUtils.getInstance().showGlobalShort("未安装微信，无法完成支付!");
                    return;
                }
                if(!confirmWeXinDialog.isShowing())
                    confirmWeXinDialog.show();
            }else if((int)aliItem.getTag() == 1){
                Log.d(TAG,"start.....come in ali");
                if(!checkAliPayInstalled()){
                    ToastUtils.getInstance().showGlobalShort("未安装支付宝，无法完成支付!");
                    return;
                }
                if(!confirmAliDialog.isShowing())
                    confirmAliDialog.show();
            }
        }
    };

    private  boolean checkAliPayInstalled() {
        Uri uri = Uri.parse("alipays://platformapi/startApp");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        ComponentName componentName = intent.resolveActivity(this.getPackageManager());
        return componentName != null;
    }


    private boolean checkWechatInstalled(){
        try {
            PackageInfo newpackageInfo = this.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo("com.tencent.mm", 0);//4.x智慧家庭包名,智慧家庭包名有变动，新包名没有确认
            if (newpackageInfo == null) {
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void startPay(String payType){
        synchronized (iPayResultNotifyAty){
            if(paylock){
                return;
            }
            paylock = true;
            switch (payType){
                case PayConstant.PAY_ALI:
                    startAli();
                    break;
                case PayConstant.PAY_WE:
                    startWeChat();
                    break;
            }
        }
    }


    private void startWeChat(){
        PayPresenter.getInstance().requestPay(PayCenterActivity.this, id, request_pay_params, PayConstant.PAY_WE,  iPayResultNotifyAty);
    }
    private void startAli(){
        PayPresenter.getInstance().requestPay(PayCenterActivity.this, id, request_pay_params, PayConstant.PAY_ALI, iPayResultNotifyAty);
    }

    IPayResultNotifyAty iPayResultNotifyAty = new IPayResultNotifyAty() {
        @Override
        public void notifityActivity(String result) {
            Log.d("pay_callback","notifityActivity....result:"+result);
            switch (result){
                case PayResultEvent.STATUS_SUCCESS:
                    PayCenterActivity.this.finish();
                    break;
                case PayResultEvent.STATUS_CANCEL:
                case PayResultEvent.STATUS_FAIL:
                    break;
            }
            synchronized (iPayResultNotifyAty){
                paylock = false;
            }

        }
    };


    View.OnClickListener payTypeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(paylock)
                return;
            if(v.getId() == wechatItem.getId()){
                aliItem.setTag(0);
                wechatItem.setTag(1);
                aliPay.setBackgroundResource( R.drawable.unselected_icon);
                wcPay.setBackgroundResource( R.drawable.selected_icon);
            }else if(v.getId() == aliItem.getId()){
                wechatItem.setTag(0);
                aliItem.setTag(1);
                wcPay.setBackgroundResource( R.drawable.unselected_icon);
                aliPay.setBackgroundResource( R.drawable.selected_icon);
            }
        }
    };


    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(paylock)
                return false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    if(v.getId() == wechatItem.getId()){
                        aliItem.setTag(0);
                        wechatItem.setTag(1);
                        aliPay.setBackgroundResource( R.drawable.unselected_icon);
                        wcPay.setBackgroundResource( R.drawable.selected_icon);
                    }else if(v.getId() == aliItem.getId()){
                        wechatItem.setTag(0);
                        aliItem.setTag(1);
                        wcPay.setBackgroundResource( R.drawable.unselected_icon);
                        aliPay.setBackgroundResource( R.drawable.selected_icon);
                    }
                    return true;
            }
            return false;
        }
    };

    long  lastClickTime= 0;
    public  boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 2500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
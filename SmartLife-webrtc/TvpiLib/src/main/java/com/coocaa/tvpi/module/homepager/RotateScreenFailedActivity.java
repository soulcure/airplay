package com.coocaa.tvpi.module.homepager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.tvpi.util.AppProcessUtil;
import com.coocaa.tvpi.view.STextView;
import com.coocaa.tvpilib.R;

/**
 * 旋转屏幕失败后进程处于前台时弹窗，处于后台是吐司
 * Created by songxing on 2020/2/29
 */
public class RotateScreenFailedActivity extends BaseActivity {

    public static void start(Context context) {
        if(AppProcessUtil.isBackground(context)){
           /* if(DeviceControllerManager.getInstance().getConnectDevice() != null) {
                RotateFailedNotification rotateFailedNotification = new RotateFailedNotification(context);
                rotateFailedNotification.init(DeviceControllerManager.getInstance().getConnectDevice().getName());
                rotateFailedNotification.activeRotateNotification(true);
            }*/
        }else {
            Intent starter = new Intent(context, RotateScreenFailedActivity.class);
            context.startActivity(starter);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.dialog_alpha_enter, R.anim.dialog_alpha_outer);
        setContentView(R.layout.activity_rotate_screen_failed);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().setGravity(Gravity.CENTER);
        initView();
    }

    private void initView(){
        TextView tvTip = findViewById(R.id.dialog_tips);
        STextView tvConfirm = findViewById(R.id.stv_confirm);
        /*DeviceInfo connectDevice = DeviceControllerManager.getInstance().getConnectDevice();
        if(connectDevice!= null) {
            tvTip.setText(String.format("%s旋转失败，请检查", connectDevice.getName()));
        }*/
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.dialog_alpha_enter, R.anim.dialog_alpha_outer);
    }
}

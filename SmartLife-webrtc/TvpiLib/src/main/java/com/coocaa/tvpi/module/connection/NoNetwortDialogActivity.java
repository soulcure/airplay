package com.coocaa.tvpi.module.connection;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coocaa.publib.base.DialogActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

/**
 * @Author: wuhaiyuan
 */
public class NoNetwortDialogActivity extends DialogActivity {

    private static final String KEY_MAC = "KEY_MAC";

    private static final String TAG = NoNetwortDialogActivity.class.getSimpleName();

    private View mLayout;
    private TextView mTitleTV;
    private TextView mSubtitleTV;
    private TextView mConfirmTV;
    private ImageView mNoNetworkTipIV;
    private ImageView mNoNetworkIV;
    private View mConfirmLayout;

    private String mac;

    public static void start(Context context, String mac) {
        Intent starter = new Intent(context, NoNetwortDialogActivity.class);
        if (!TextUtils.isEmpty(mac)) {
            starter.putExtra(KEY_MAC, mac);
        }
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarHelper.translucent(this);
        if (null != getIntent()) {
            mac = getIntent().getStringExtra(KEY_MAC);
            Log.d(TAG, "onCreate: mac = " + mac);
        }
        initView();
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

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void finish() {
        super.finish();
//        overridePendingTransition(0, R.anim.new_connect_device_exit);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        mLayout = LayoutInflater.from(this).inflate(R.layout.activity_no_network, null);
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        contentRl.addView(mLayout, params);

        mLayout.findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mLayout.findViewById(R.id.btClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mTitleTV = mLayout.findViewById(R.id.title_tv);
        mSubtitleTV = mLayout.findViewById(R.id.subtile_tv);
        mConfirmTV = mLayout.findViewById(R.id.confirm_tv);
        mNoNetworkTipIV = mLayout.findViewById(R.id.no_network_tip_iv);
        mNoNetworkIV = mLayout.findViewById(R.id.no_network_iv);
        mConfirmLayout = mLayout.findViewById(R.id.connet_network_btn_layout);

        if (TextUtils.isEmpty(mac)) {
            mTitleTV.setText(R.string.no_network_title_no_ble);
            mSubtitleTV.setText(R.string.no_network_subtitle_no_ble);
            mConfirmTV.setText(R.string.no_network_confirm_no_ble);
            mConfirmTV.setTextColor(getColor(R.color.black_80));
            mNoNetworkTipIV.setBackgroundResource(R.drawable.bg_no_network_no_ble_tip);
            mNoNetworkIV.setVisibility(View.GONE);
            mConfirmLayout.setBackgroundResource(R.drawable.bg_connect_network_confirm_btn_no_ble);
            mConfirmLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        } else {
            mTitleTV.setText(R.string.no_network_title);
            mSubtitleTV.setText(R.string.no_network_subtitle);
            mConfirmTV.setText(R.string.no_network_confirm);
            mConfirmTV.setTextColor(getColor(R.color.c_6));
            mNoNetworkTipIV.setBackgroundResource(R.drawable.bg_no_network_tip);
            mNoNetworkIV.setVisibility(View.VISIBLE);
            mConfirmLayout.setBackgroundResource(R.drawable.bg_connect_network_confirm_btn);
            mConfirmLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PermissionsUtil.getInstance().requestPermission(NoNetwortDialogActivity.this, new PermissionListener() {
                        @Override
                        public void permissionGranted(String[] permission) {
                            ConnectNetForDongleActivity.start(NoNetwortDialogActivity.this, mac);
                            finish();
                        }

                        @Override
                        public void permissionDenied(String[] permission) {
                            ToastUtils.getInstance().showGlobalShort("需要获取位置信息权限才能读取Wi-Fi");
                        }
                    }, Manifest.permission.ACCESS_FINE_LOCATION);
                }
            });
        }
    }
}

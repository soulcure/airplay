package com.coocaa.tvpi.module.connection;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.coocaa.publib.base.DialogActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

import static com.coocaa.tvpi.common.UMengEventId.MAIN_PAGE_SCAN;

/**
 * @Author: yuzhan
 */
public class ConnectDialogActivity extends DialogActivity {

    private static final String TAG = ConnectDialogActivity.class.getSimpleName();

    private View mLayout;

    public static void start(Context context) {
        Intent starter = new Intent(context, ConnectDialogActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        overridePendingTransition(R.anim.new_connect_device_enter, 0);
//        setContentView(R.layout.activity_connect_device_dialog);
        StatusBarHelper.translucent(this);
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
        mLayout = LayoutInflater.from(this).inflate(R.layout.activity_connect_device_dialog, null);
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
        mLayout.findViewById(R.id.scan_btn_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PermissionsUtil.getInstance().requestPermission(ConnectDialogActivity.this, new PermissionListener() {
                    @Override
                    public void permissionGranted(String[] permission) {
                        ScanActivity2.start(ConnectDialogActivity.this);
                        finish();
                    }

                    @Override
                    public void permissionDenied(String[] permission) {
                        ToastUtils.getInstance().showGlobalShort(getResources().getString(R.string.request_camera_permission_tips));
                    }
                }, Manifest.permission.CAMERA);
            }
        });
    }
}

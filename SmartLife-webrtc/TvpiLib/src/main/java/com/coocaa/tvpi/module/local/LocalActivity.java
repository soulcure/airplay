package com.coocaa.tvpi.module.local;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.tvpi.module.local.utils.LocalResourceHelper;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpilib.R;
import com.umeng.analytics.MobclickAgent;

/**
 * @ClassName LocalActivity
 * @Description TODO (write something)
 * @User heni
 * @Date 2020-04-23
 */
public class LocalActivity extends BaseActivity {

    private static final String TAG = LocalActivity.class.getSimpleName();
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 0x01;

    private View rootView;
    private View pictureView, videoView, musicView;

    LocalResourceHelper mLocalResourceHelper;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    mLocalResourceHelper.initData();
                }
            }
        }
    }

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_activity_layout);
        StatusBarHelper.translucent(this);
        StatusBarHelper.setStatusBarLightMode(this);
        initView();
        mLocalResourceHelper = new LocalResourceHelper(LocalActivity.this, rootView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG); // 统计页面
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG); // 统计页面
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocalResourceHelper != null) {
            mLocalResourceHelper.destory();
        }
    }

    private void initView() {
        rootView = findViewById(R.id.local_activity_layout_root);
        pictureView = findViewById(R.id.local_picture_rl);
        videoView = findViewById(R.id.local_video_rl);
        musicView = findViewById(R.id.local_music_rl);

        pictureView.setOnClickListener(viewOnClickLis);
        videoView.setOnClickListener(viewOnClickLis);
        musicView.setOnClickListener(viewOnClickLis);
        CommonTitleBar titleBar = findViewById(R.id.titleBar);
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if(position == CommonTitleBar.ClickPosition.LEFT){
                    finish();
                }
            }
        });
    }

    View.OnClickListener viewOnClickLis = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == pictureView.getId()) {
                if (checkSDCardPermisson()) {
                    startActivity(new Intent(LocalActivity.this, PictureActivity.class));
                }
            } else if (v.getId() == videoView.getId()) {
                if (checkSDCardPermisson()) {
                    startActivity(new Intent(LocalActivity.this, VideoActivity.class));
                }
            } else if (v.getId() == musicView.getId()) {
                if (checkSDCardPermisson()) {
                    startActivity(new Intent(LocalActivity.this, MusicActivity.class));
                }
            }
        }
    };

    private boolean checkSDCardPermisson() {
        if (ContextCompat.checkSelfPermission(LocalActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(LocalActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            } else {
                //用户勾选了禁止权限弹窗
                ToastUtils.getInstance().showGlobalShort("SD卡读写权限被禁，请前往手机设置打开");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            }
            return false;
        } else {
            return true;
        }
    }
}

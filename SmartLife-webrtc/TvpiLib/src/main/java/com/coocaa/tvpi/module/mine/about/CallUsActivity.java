package com.coocaa.tvpi.module.mine.about;

import android.Manifest;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.coocaa.publib.base.BaseActivity;
import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.tvpi.util.StatusBarHelper;
import com.coocaa.tvpi.util.permission.PermissionListener;
import com.coocaa.tvpi.util.permission.PermissionsUtil;
import com.coocaa.tvpi.view.CommonTitleBar;
import com.coocaa.tvpilib.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CallUsActivity extends BaseActivity implements SaveCodeConfirmDFragment.OnConfirmListener{

    private static final String PATH = "/sdcard/Pictures/GXP";
    private SaveCodeConfirmDFragment saveCodeConfirmDFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarHelper.translucent(this);
        setContentView(R.layout.activity_call_us);
        StatusBarHelper.setStatusBarLightMode(this);
        initView();
    }

    private void initView() {
        CommonTitleBar titleBar = findViewById(R.id.titleBar);
        titleBar.setOnClickListener(new CommonTitleBar.OnClickListener() {
            @Override
            public void onClick(CommonTitleBar.ClickPosition position) {
                if (position == CommonTitleBar.ClickPosition.LEFT) {
                    finish();
                }
            }
        });
        findViewById(R.id.weixin_code_img).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                saveCodeConfirmDFragment = new SaveCodeConfirmDFragment();
                saveCodeConfirmDFragment.setConfirmListener(CallUsActivity.this);
                saveCodeConfirmDFragment.show(getSupportFragmentManager(),SaveCodeConfirmDFragment.DIALOG_FRAGMENT_TAG);
                return false;
            }
        });
    }

    @Override
    public void onConfirmOK() {
        saveLocalBitmap(findViewById(R.id.weixin_code_img));
    }

    @Override
    public void onConfirmCancel() {

    }

    //二维码保存的相关逻辑
    private void saveLocalBitmap(View view) {
        Bitmap cacheBitmap = convertViewToBitmap(view);
        if (cacheBitmap == null) {
            Log.i(TAG, "cacheBitmap=null");
            return;
        }
        Bitmap saveBitmap = Bitmap.createBitmap(cacheBitmap);
        if (saveBitmap == null) {
            Log.i(TAG, "newBitmap=null");
            return;
        }
        PermissionsUtil.getInstance().requestPermission(this, new PermissionListener() {
            @Override
            public void permissionGranted(String[] permission) {
                saveFile(saveBitmap);
            }

            @Override
            public void permissionDenied(String[] permission) {
                ToastUtils.getInstance().showGlobalLong("保存失败");
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private Bitmap convertViewToBitmap(View view) {
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        if (bitmap != null) {
            Bitmap.Config cfg = bitmap.getConfig();
            Log.d(TAG, "----------------------- cache.getConfig() = " + cfg);
        }
        return bitmap;
    }

    private void saveFile(Bitmap saveBitmap) {
        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File photoFile = new File(PATH, System.currentTimeMillis() + ".jpg");
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(photoFile);
            if (saveBitmap != null) {
                if (saveBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)) {
                    fileOutputStream.flush();
                    ToastUtils.getInstance().showGlobalLong("已保存到相册");
                }
            }
        } catch (FileNotFoundException e) {
            photoFile.delete();
            e.printStackTrace();
            ToastUtils.getInstance().showGlobalLong("保存失败");
        } catch (IOException e) {
            photoFile.delete();
            e.printStackTrace();
            ToastUtils.getInstance().showGlobalLong("保存失败");
        } finally {
            try {
                fileOutputStream.close();
                MediaScannerConnection.scanFile(CallUsActivity.this, new String[]{PATH}, null, null);
                //刷新
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

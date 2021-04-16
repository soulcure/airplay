package com.coocaa.tvpi.util.permission;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;



public class PermissionActivity extends AppCompatActivity {
    private final int PERMISSION_REQUEST_CODE = 101;

    private Context context;
    private boolean isRequireCheck;

    private String key;
    private String[] permission;
    private PermissionsUtil.TipInfo tipInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null) {
            finish();
            return;
        }

        context = this;
        isRequireCheck = true;
        key = getIntent().getStringExtra("key");
        permission = getIntent().getStringArrayExtra("permission");
        tipInfo = (PermissionsUtil.TipInfo) getIntent().getSerializableExtra("tip");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRequireCheck) {
            if (PermissionsUtil.getInstance().hasPermission(this, permission)) {
                permissionsGranted();
            } else {
                requestPermissions(permission); // 请求权限,回调时会触发onResume
                isRequireCheck = false;
            }
        } else {
            isRequireCheck = true;
        }
    }

    // 请求权限兼容低版本
    private void requestPermissions(String[] permission) {
        ActivityCompat.requestPermissions(this, permission, PERMISSION_REQUEST_CODE);
    }


    /**
     * 用户权限处理,
     * 如果全部获取, 则直接过.
     * 如果权限缺失, 则提示Dialog.
     *
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @Nullable String[] permissions, @Nullable int[] grantResults) {
        if(permissions == null || grantResults == null) {
            return;
        }
        //部分厂商手机系统返回授权成功时，厂商可以拒绝权限，所以要用PermissionChecker二次判断
        if (requestCode == PERMISSION_REQUEST_CODE
                && PermissionsUtil.getInstance().isGranted(grantResults)
                && PermissionsUtil.getInstance().hasPermission(context, permissions)) {
            permissionsGranted();
        } else {
            showPermissionDeniedDialog();
        }
    }


    // 显示缺失权限提示
    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(tipInfo.title)
                .setMessage(tipInfo.content)
                .setNegativeButton(tipInfo.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        permissionsDenied();
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(tipInfo.ensure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionsUtil.getInstance().gotoSetting();
                        dialog.dismiss();
                    }
                })
                .setCancelable(false);
        builder.show();
    }

    private void permissionsDenied() {
        PermissionListener listener = PermissionsUtil.getInstance().getListener(key);
        if (listener != null) {
            listener.permissionDenied(permission);
        }
        finish();
    }

    // 全部权限均已获取
    private void permissionsGranted() {
        PermissionListener listener = PermissionsUtil.getInstance().getListener(key);
        if (listener != null) {
            listener.permissionGranted(permission);
        }
        finish();
    }

    protected void onDestroy() {
        super.onDestroy();
        PermissionsUtil.getInstance().getListener(key);
        if (tipInfo != null) {
            tipInfo = null;
        }
    }
}

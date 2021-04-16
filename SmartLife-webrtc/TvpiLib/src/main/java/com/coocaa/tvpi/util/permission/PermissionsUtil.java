package com.coocaa.tvpi.util.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.PermissionChecker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PermissionsUtil {
    private static Context sApplicationContext;
    private static HashMap<String, PermissionListener> listenerMap = new HashMap<>();
    private static PermissionsUtil permissionsUtil;

    public static void init(Context applicationContext) {
        sApplicationContext = applicationContext;
    }

    public static PermissionsUtil getInstance() {
        if (permissionsUtil == null) {
            synchronized (PermissionsUtil.class) {
                if (permissionsUtil == null) {
                    permissionsUtil = new PermissionsUtil();
                }
            }
        }
        return permissionsUtil;
    }


    /**
     * 申请授权，当用户拒绝时，会显示默认一个默认的Dialog提示用户
     *
     * @param context
     * @param listener
     * @param permission 要申请的权限
     */
    public void requestPermission(Context context, PermissionListener listener, String... permission) {
        String[] newPermission = checkReadWritePermission(permission);
        requestPermission(context, listener, newPermission,
                new TipInfo("帮助",
                        "当前应用缺少必要权限。\n \n 请点击 \"设置\"-\"权限\"-打开所需权限。",
                        "取消",
                        "设置"));
    }

    /**
     * 申请授权，当用户拒绝时，可以设置是否显示Dialog提示用户，也可以设置提示用户的文本内容
     *
     * @param context
     * @param listener
     * @param permission 需要申请授权的权限
     * @param tip        当用户拒绝时要显示Dialog设置
     */
    private void requestPermission(Context context, PermissionListener listener,
                                   String[] permission, TipInfo tip) {
        if (listener == null) {
            Log.e("PermissionsUtil", "listener is null");
            return;
        }

        if (hasPermission(context, permission)) {
            listener.permissionGranted(permission);//通过授权
        } else {
            if (Build.VERSION.SDK_INT < 23) {
                listener.permissionDenied(permission);
            } else {
                String key = String.valueOf(System.currentTimeMillis());
                listenerMap.put(key, listener);
                Intent intent = new Intent(context, PermissionActivity.class);
                intent.putExtra("permission", permission);
                intent.putExtra("key", key);
                intent.putExtra("tip", tip);
                if (!(context instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                context.startActivity(intent);
            }
        }
    }

    /**
     * 检查本地存储读写权限，如果只申请了一个，把另一个也加上，因为某些手机只申请了READ_EXTERNAL_STORAGE的话，其他需要WRITE_EXTERNAL_STORAGE的地方并不会再次提示用户去允许(比如小米手机)
     *
     * @param permission
     * @return
     */
    private String[] checkReadWritePermission(String[] permission) {
        List<String> permissionList = new ArrayList<>(Arrays.asList(permission));
        boolean hasReadPermission = permissionList.contains(Manifest.permission.READ_EXTERNAL_STORAGE);
        boolean hasWritePermission = permissionList.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasReadPermission && !hasWritePermission) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else if (!hasReadPermission && hasWritePermission) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            return permission;
        }
        String[] newPermission = new String[permissionList.size()];
        permissionList.toArray(newPermission);
        return newPermission;
    }

    /**
     * 判断权限是否授权
     *
     * @param context
     * @param permissions
     * @return
     */
    public boolean hasPermission(Context context, String... permissions) {

        if (permissions.length == 0) {
            return false;
        }

        for (String per : permissions) {
            int result = PermissionChecker.checkSelfPermission(context, per);
            if (result != PermissionChecker.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    /**
     * 判断一组授权结果是否为授权通过
     *
     * @param grantResult
     * @return
     */
    public boolean isGranted(int... grantResult) {

        if (grantResult.length == 0) {
            return false;
        }

        for (int result : grantResult) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 跳转到当前应用对应的设置页面
     */
    public void gotoSetting() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + sApplicationContext.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sApplicationContext.startActivity(intent);
    }

    /**
     * 获取对应的PermissionListener
     *
     * @param key
     * @return
     */
    public PermissionListener getListener(String key) {
        return listenerMap.remove(key);
    }

    public static class TipInfo implements Serializable {

        private static final long serialVersionUID = 1L;

        public String title;
        public String content;
        public String cancel;  //取消按钮文本
        public String ensure;  //确定按钮文本

        TipInfo(String title, String content, String cancel, String ensure) {
            this.title = title;
            this.content = content;
            this.cancel = cancel;
            this.ensure = ensure;
        }
    }
}
package com.coocaa.tvpi.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.coocaa.publib.utils.ToastUtils;

/**
 * @Description: 第三方应用启动工具类
 * @Author: wzh
 * @CreateDate: 1/12/21
 */
public class ThirdAppLaunch {
    /**
     * 启动微信
     * @param context
     * @return
     */
    public static boolean startWechat(Context context) {
        try {
            Uri uri = Uri.parse("weixin://");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            ToastUtils.getInstance().showGlobalShort("未安装微信");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 启动QQ
     * @param context
     * @return
     */
    public static boolean startQQ(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage("com.tencent.mobileqq");
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            ToastUtils.getInstance().showGlobalShort("未安装QQ");
            e.printStackTrace();
        }
        return false;
    }
}

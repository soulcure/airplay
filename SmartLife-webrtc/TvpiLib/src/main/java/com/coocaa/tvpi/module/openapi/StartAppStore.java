package com.coocaa.tvpi.module.openapi;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.publib.utils.ToastUtils;
import com.coocaa.smartscreen.constant.SmartConstans;

public class StartAppStore {

    public static void startAppStore(Context context, String pkg) {
        if(context == null || TextUtils.isEmpty(pkg)) {
            return ;
        }
        String marketPkg = getMarketPkgName(context);
        if(isAppExist(context, marketPkg)) {
            launchAppDetail(context, pkg, marketPkg);
        } else {
            launchAppDetail(context, pkg, null);
        }
    }


    public static void launchAppDetail(Context context,String appPkg, String marketPkg) {
        Log.d("SmartApi", "launchAppDetail, pkg=" + appPkg + ", marketPkg=" + marketPkg);
        try {
            if (TextUtils.isEmpty(appPkg))
                return;
            Uri uri = Uri.parse("market://details?id=" + appPkg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (!TextUtils.isEmpty(marketPkg))
                intent.setPackage(marketPkg);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            if(TextUtils.isEmpty(marketPkg)) {
                ToastUtils.getInstance().showGlobalShort("您的手机未安装应用商店，无法打开");
            } else {
                launchAppDetail(context, appPkg, null);//如果是应用商店包名不对，就不传，重新打开一次
            }
            e.printStackTrace();
        }
    }

    public static String getMarketPkgName(Context context) {
        String brand = SmartConstans.getPhoneInfo().brand;
        if(brand == null)
            return null;
        brand = brand.toLowerCase();

        if("huawei".equals(brand)) {
            return "com.huawei.appmarket";
        } else if("honor".equals(brand)) {

        } else if("xiaomi".equals(brand)) {
            return "com.xiaomi.market";
        } else if("oneplus".equals(brand)) {
            return "com.heytap.market";
        } else if("oppo".equals(brand)) {
            return "com.oppo.market";
        } else if("vivo".equals(brand)) {
            return "com.bbk.appstore";
        } else if("meizu".equals(brand)) {
            return "com.meizu.mstore";
        } else if("samsung".equals(brand)) {
            return "com.sec.android.app.samsungapps";
        } else if("lenovo".equals(brand)) {
            return "com.lenovo.leos.appstore";
        } else if("zte".equals(brand)) {
            return "zte.com.market";
        } else if("sony".equals(brand)) {

        } else if("lg".equals(brand)) {

        } else if("coolpad".equals(brand)) {

        }
        return null;
    }

    public static boolean isAppExist(Context context, String pkg) {
        try {
            context.getPackageManager().getPackageInfo(pkg, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}

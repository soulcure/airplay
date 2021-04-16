package com.tianci.user.api.factory;

import com.tianci.user.api.IUser;
import com.tianci.user.api.utils.ULog;

/**
 * 通过反射动态创建产品类实例
 */
public class UserImplFactory {
    public static IUser create() {
        String clsName = isCcos() ? "com.tianci.user.api.coocaa.UserCoocaaImpl" : "com.tianci.user.api.common.UserCommonImpl";
        return create(clsName);
    }

    public static IUser create(String className) {
        ULog.i("UserImplFactory - create, className = " + className);

        try {
            // 1. 根据 传入的产品类名 获取 产品类类型的Class对象
            Class userClass = Class.forName(className);
            // 2. 通过Class对象动态创建该产品类的实例
            return (IUser) userClass.newInstance();
        } catch (Exception e) {
            ULog.e("UserImplFactory - create, exception = " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private static boolean isCcos() {
        return false;
//        return new File("/vendor/TianciVersion").exists()
//                || new File("/system/vendor/TianciVersion").exists();//判断是否是酷开系统
    }
}

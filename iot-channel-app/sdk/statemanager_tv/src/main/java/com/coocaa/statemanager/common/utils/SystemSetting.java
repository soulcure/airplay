package com.coocaa.statemanager.common.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.RemoteException;

import com.coocaa.statemanager.StateManager;

/**
 * @ClassName: SystemSetting
 * @Author: AwenZeng
 * @CreateDate: 2021/3/24 19:30
 * @Description:
 */
public class SystemSetting {
    /**
     * 禁止掉应用
     * @param packageName
     * @return
     * @throws RemoteException
     */
    public static boolean disablePackage(String packageName) throws RemoteException {
        PackageManager pm = StateManager.INSTANCE.getContext().getPackageManager();
        if(pm.getApplicationEnabledSetting(packageName) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED){
            pm.setApplicationEnabledSetting(packageName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
        }
        return PackageManager.COMPONENT_ENABLED_STATE_DISABLED == pm.getApplicationEnabledSetting(packageName);
    }


    /**
     * 激活应用
     * @param packageName
     * @return
     * @throws RemoteException
     */
    public static boolean enablePackage(String packageName) throws RemoteException {
        PackageManager pm = StateManager.INSTANCE.getContext().getPackageManager();
        if(pm.getApplicationEnabledSetting(packageName) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED){
            pm.setApplicationEnabledSetting(packageName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
        }
        return PackageManager.COMPONENT_ENABLED_STATE_DEFAULT == pm.getApplicationEnabledSetting(packageName);
    }


    /**
     * 按Component形式设置禁止或激活业务
     * @param context
     * @param pkgName 包名
     * @param className 类名
     * @param state PackageManager.COMPONENT_ENABLED_STATE_DISABLED | PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
     */
    public static void setComponentEnabledSetting(Context context,String pkgName,String className, int state) {
        try {
            PackageManager pm = context.getPackageManager();
            ComponentName componentName = new ComponentName(pkgName,className);
            pm.setComponentEnabledSetting(componentName, state, PackageManager.DONT_KILL_APP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

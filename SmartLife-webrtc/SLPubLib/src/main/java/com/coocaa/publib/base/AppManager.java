package com.coocaa.publib.base;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Chenglong
 * @ClassName AppManager
 * @Description 管理Activity, 可实现Activity栈，而很好的管理Activity
 * @date 2014年6月28日
 */
public class AppManager {

	private static final String TAG = "AppManager";
	private static List<Activity> mListActivity = new ArrayList<>();
    private static AppManager instance;

    private AppManager() {
    }

    public static AppManager getInstance() {
        if (null == instance) {
            instance = new AppManager();
        }
        return instance;
    }

    //存放Activity到list中
    public void addActivity(Activity activity) {
        mListActivity.add(activity);
    }


    //移除list的值
    public void removeActivity(Activity activity) {
        Log.d(TAG, "removeActivity");
        mListActivity.remove(activity);
    }

    //遍历存放在list中的Activity并退出
    //ConcurrentModificationException 增加同步锁防止多线程操作 modified by wuhaiyuan
    public synchronized void closeAllActivity() {
		Log.d(TAG, "closeAllActivity");
        try {
            for (Iterator<Activity> act = mListActivity.iterator(); act.hasNext(); ) {
                act.next().finish();
                act.remove();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		//万不得已，不能强杀APP进程，会导致后台服务等组件异常。
        //android.os.Process.killProcess(android.os.Process.myPid());
		//System.exit(0);
    }

    @Deprecated
    public static boolean isExsitActivity(Context ctx,
                                          Class<? extends Activity> cls) {
        Intent intent = new Intent(ctx, cls);
        ComponentName cmpName = intent.resolveActivity(ctx.getPackageManager());
        boolean flag = false;
        if (cmpName != null) { // 说明系统中存在这个activity
            for (Activity activity : mListActivity) {
                if (activity.getComponentName().equals(cmpName)) { // 说明它已经启动了
                    flag = true;
                    break; // 跳出循环，优化效率
                }
            }
        }
        return flag;
    }

}

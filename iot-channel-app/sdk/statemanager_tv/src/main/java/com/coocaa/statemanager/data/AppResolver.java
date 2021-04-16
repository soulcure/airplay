package com.coocaa.statemanager.data;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import com.coocaa.statemanager.common.bean.CmdData;
import com.coocaa.statemanager.common.bean.ScreenApps;
import com.google.gson.Gson;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import swaiotos.channel.iot.ss.SSChannel;
import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.channel.iot.ss.server.ShareUtls;
import swaiotos.channel.iot.ss.server.utils.Constants;
import swaiotos.channel.iot.utils.EmptyUtils;
import swaiotos.channel.iot.utils.ThreadManager;

public class AppResolver {
    private static Map<String, Map<String, AppBean>> clientMap = new HashMap<>();
    public static String homePkg = SystemProperties.get("persist.service.homepage.pkg");
    private static long updateTime = 0;
    private static ArrayList<String> filters = new ArrayList<>();

    static {
        filters.add("com.yozo.office.education");//永中业务
        filters.add("com.tianci.de");//爱投屏业务
        filters.add("com.coocaa.whiteboard.tv");//白板业务
    }

    public static Map<String, Map<String, AppBean>> getClientMap(){
        return clientMap;
    }

    public static Map<String, AppBean> getAppBean(String pkgName) {
        try {
            return clientMap.get(pkgName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void killBackProcess(final Context mcontext, final String startPkg, final SSChannel mSSChannel) {
        Log.d("state", " killBackProcess  startPkg:" + startPkg);
        if(("com.yozo.office.education".equals(startPkg)))
            return;
        for (String pkg : clientMap.keySet()) {
            if (!pkg.equals(startPkg)) {
//                Log.d("state", " killBackProcess pkg:" + pkg);
                if ("com.tianci.movieplatform".equals(pkg) && !("com.tianci.movieplatform").equals(homePkg)) {
                    forceStopPackage(mcontext, "com.tianci.movieplatform");
                } else if ("swaiotos.channel.iot".equals(pkg)) {
                    try {
                        //结构原形在徐泽骁那里
                        CmdData data = new CmdData("", "custom_event", "{\n" +
                                "        \"cmd\": \"exit\",\n" +
                                "        \"param\": \"\",\n" +
                                "        \"type\": \"control\"\n" +
                                "    }");
                        String content = data.toJson();
//                        Log.d("state", " killBackProcess content:" + content);
                        Intent i = new Intent();
                        i.setPackage("swaiotos.channel.iot");
                        i.setAction("coocaa.intent.action.universalmediaplayer");
                        i.putExtra("content", content);
                        mcontext.sendBroadcast(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (filters.contains(pkg)) {//过滤不被杀的业务
                    //文档不做kill的处理
                } else {
                    forceStopPackage(mcontext, pkg);
                }
            }
        }
    }

    private static void forceStopPackage(final Context context, final String pkg) {
        //延迟1.5s杀应用
        ThreadManager.getInstance().uiThread(new Runnable() {
            @Override
            public void run() {
                ActivityManager activityManager = (ActivityManager) context
                        .getSystemService(Context.ACTIVITY_SERVICE);
                try {
                    Method forceStopPackage;
                    forceStopPackage = activityManager.getClass().getDeclaredMethod("forceStopPackage", String.class);
                    forceStopPackage.setAccessible(true);
                    forceStopPackage.invoke(activityManager, pkg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },1500);
    }

    public static class AppBean implements Serializable {
        public String pkg;
        public String className;
        public String clientID;
        public IMMessage.TYPE mediaType;
        public boolean isAutoExitNoDevice;
        public long disNetworExitTime;
        public long noDeviceExitTime;

        public AppBean(String p, String cName, String id,IMMessage.TYPE type,boolean _isAutoExit,long _disNetworkExitTime,long _noDeviceExitTime) {
            pkg = p;
            className = cName;
            clientID = id;
            mediaType = type;
            isAutoExitNoDevice = _isAutoExit;
            disNetworExitTime = _disNetworkExitTime;
            noDeviceExitTime = _noDeviceExitTime;
        }
    }

    public static void initScreenApps(Context context) {
        try {
            String json = ShareUtls.getInstance(context).getString(Constants.COOCAA_PREF_SCREEN_APPS, "");
            Log.d("state", " appresolve  sp  json:" + json);
            if (!TextUtils.isEmpty(json)) {
                try {
                    ScreenApps apps = new Gson().fromJson(json, ScreenApps.class);
                    updateTime = apps.timestamp;
                    updateScreenApps(context, apps, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void updateScreenApps(Context c, ScreenApps apps, boolean check) {
        try {
//            long time = apps.timestamp;
//            if (check && time <= updateTime) {
//                Log.d("state", " updateScreenApps  return:");
//                return;
//            }
            List<ScreenApps.AppItem> appItems = apps.app_list;
            if (appItems != null && appItems.size() > 0) {
                clientMap.clear();
                clientMap = new HashMap<>();
            }
            for (ScreenApps.AppItem appItem : appItems) {
                Map<String, AppBean> map = new HashMap<>();
                String pkg = appItem.pkgname;
                List<ScreenApps.AppWay> appWays = appItem.app_way;
                AppBean appBean = null;
                for (ScreenApps.AppWay ways : appWays) {
                    appBean = new AppBean(ways.pkgname, ways.className, "", TextUtils.isEmpty(ways.mediaType) ? IMMessage.TYPE.valueOf(ways.type) : IMMessage.TYPE.valueOf(ways.mediaType),
                            ways.isAutoExitNoDevice,ways.disNetworExitTime,ways.noDeviceExitTime);
                    map.put(ways.className,appBean);
                    map.put(ways.type,appBean);
                    Log.d("state", " updateScreenApps  ways.pkgname:" + ways.pkgname + " ways.className:" + ways.className);
                }
                Log.d("state", " updateScreenApps  pkg:" + pkg);
                clientMap.put(pkg, map);
            }
            if (check) {
                String json = new Gson().toJson(apps);
                Log.d("state", " updateScreenApps  save  json:" + json);
                ShareUtls.getInstance(c).putString(Constants.COOCAA_PREF_SCREEN_APPS, json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void comebackHome() {
        ThreadManager.getInstance().ioThread(new Runnable() {
            @Override
            public void run() {
                try {
                    new Instrumentation().sendKeyDownUpSync(3);  //模拟home建
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static ComponentName getTopComponet(Context context) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            return am.getRunningTasks(1).get(0).topActivity;
        } catch (Exception e) {
        }
        return null;
    }


    /**
     * 获取投屏业务配置信息
     * @return
     */
    public static AppBean getScreenAppConfig(Context context){
        ComponentName cn = getTopComponet(context);
        if(EmptyUtils.isEmpty(cn)){
            return null;
        }
        String pkgName = cn.getPackageName();
        String className = cn.getClassName();

        Map<String, AppBean> appInfoMap = getAppBean(pkgName);
        if(EmptyUtils.isNotEmpty(appInfoMap)){
            AppBean appBean = null;
            if(pkgName.equals("com.yozo.office.education")){//永中app会生成多个Activity页面，所以不能以className为取值
                appBean = appInfoMap.get("DOC");
            }else{
                appBean = appInfoMap.get(className);
            }
            if(EmptyUtils.isNotEmpty(appBean)){
                return appBean;
            }
        }
        return null;
    }


    /***
     * 是否是Dongle投屏业务
     * @param pkgName
     * @param className
     * @return
     */
    public static boolean isDongleCastBusiness(String pkgName,String className){

        //影视为launcher的情况处理
        if((AppResolver.homePkg.equals(pkgName)||pkgName.equals("com.tianci.movieplatform")) &&!className.equals("com.tianci.ThirdPlayer.ThirdDetailInfoActivity")){
            return false;
        }

        //由于永中会自动生成多个Activity,所以当检测到位永中app时，及为投屏业务
        if(pkgName.equals("com.yozo.office.education")){
            return true;
        }

        Map<String, AppBean> appInfoMap = getAppBean(pkgName);
        if(EmptyUtils.isNotEmpty(appInfoMap)){
            AppBean appBean = appInfoMap.get(className);
            if(EmptyUtils.isNotEmpty(appBean)){
                return true;
            }
        }
       return false;
    }
}

package swaiotos.runtime.h5.core.os.exts.system;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coocaa.smartsdk.SmartApi;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import swaiotos.runtime.h5.H5ChannelInstance;
import swaiotos.runtime.h5.H5CoreExt;
import swaiotos.runtime.h5.H5Style;
import swaiotos.runtime.h5.core.os.exts.utils.ExtLog;

/**
 * @ClassName: AccountExt
 * @Author: lu
 * @CreateDate: 11/18/20 2:33 PM
 * @Description:
 */
public class SystemExt extends H5CoreExt {
    public static final String TAG = "SystemExt";
    public static final String NAME = "system";
    private Context mContext; //for activity

    private static H5CoreExt ext = null;
    private H5Style style;
    private final Set<String> lifeCycleListenerSet = new TreeSet<>();

    public static synchronized H5CoreExt get(Context context) {
        if (ext == null) {
            ext = new SystemExt();
        }
        return ext;
    }

    @Override
    public void attach(Context context) {
        mContext = context;
        ExtLog.d(TAG, "attach : " + context);
        super.attach(context);
    }

    @Override
    public void detach(Context context) {
        ExtLog.d(TAG, "detach : " + context);
        mContext = null;
        super.detach(context);
    }

    @JavascriptInterface
    public void getSystemInfo(String id) {
        ExtLog.d(TAG, "getSystemInfo(), id: " + id);
        // TODO
        JSONObject params = new JSONObject();
        params.put("brand", getBrand());
        params.put("model", getModel());
        params.put("language", getLanguage());
        params.put("version", getVersion());
        params.put("platform", getPlatform());
        params.put("SDKVersion", getSdkVersion());
        native2js(id, "success", params.toJSONString());
    }

    @JavascriptInterface
    public void startPage(String id, String json) {
        ExtLog.d(TAG, "startPage(), id: " + id + ", json=" + json);
        boolean ret = false;
        try {
            IntentData intentData = JSON.parseObject(json, IntentData.class);
            ret = intentData.start(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject params = new JSONObject();
        params.put("isSuccess", String.valueOf(ret));
        native2js(id, ret? RET_SUCCESS : RET_FAIL, params.toString());
    }

    @JavascriptInterface
    public void startBrowser(String id, String json) {
        ExtLog.d(TAG, "startBrowser(), id: " + id + ", json=" + json);
        boolean ret = false;
        try {
            JSONObject jsonObject = JSON.parseObject(json);
            String url = jsonObject.getString("url");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.d(TAG, "startBrowser : " + intent);
            context.startActivity(intent);
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject params = new JSONObject();
        params.put("isSuccess", String.valueOf(ret));
        native2js(id, ret? RET_SUCCESS : RET_FAIL, params.toString());
    }

    @JavascriptInterface
    public void exitPage(String id) {
        ExtLog.d(TAG, "exitPage(), id: " + id);
        ExtLog.d(TAG, "ctx=" + mContext);
        boolean ret = false;
        if(mContext instanceof Activity) {
            ret = true;
        }
        JSONObject params = new JSONObject();
        native2js(id, ret ? RET_SUCCESS : RET_FAIL, params.toString());
        if(mContext instanceof Activity) {
            ((Activity) mContext).finish();
        }
    }

    @JavascriptInterface
    public void getDeviceInfo(String id) {
        ExtLog.d(TAG, "getDeviceInfo(), id: " + id);
        JSONObject params = new JSONObject();
        params.put("systemType", "android");
        if(style != null) {
            params.put("controlBarHeight", String.valueOf(style.getSafeDistanceBottom()));
        } else {
            params.put("controlBarHeight", "0");
        }
        params.put("screenWidth", context.getResources().getDisplayMetrics().widthPixels);
        params.put("screenHeight", context.getResources().getDisplayMetrics().heightPixels);
        params.put("deviceId", getUniquePsuedoID());
        native2js(id, RET_SUCCESS, params.toString());
    }

    @JavascriptInterface
    public void startWechatApplet(String id, String json) {
        ExtLog.d(TAG, "startWechatApplet(), id: " + id + ", json=" + json);
        String appId = null;
        String path = null;
        try {
            JSONObject jsonObject = JSON.parseObject(json);
            appId = jsonObject.getString("id");
            path = jsonObject.getString("path");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!TextUtils.isEmpty(appId)) {
            SmartApi.startWxMP(appId, path);
        }
        native2js(id, TextUtils.isEmpty(appId) ? RET_FAIL : RET_SUCCESS, new JSONObject().toString());
    }

    @JavascriptInterface
    public void startAppStorePage(String id, String json) {
        ExtLog.d(TAG, "startAppStorePage(), id: " + id + ", json=" + json);
        String pkg = null;
        try {
            JSONObject jsonObject = JSON.parseObject(json);
            pkg = jsonObject.getString("pkgName");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!TextUtils.isEmpty(pkg)) {
            SmartApi.startAppStore(pkg);
        }
        native2js(id, TextUtils.isEmpty(pkg) ? RET_FAIL : RET_SUCCESS, new JSONObject().toString());
    }

    @JavascriptInterface
    public void getAppInfo(String id, String json) {
        ExtLog.d(TAG, "getAppInfo(), id: " + id + ", json=" + json);
        String pkg = null;
        try {
            JSONObject jsonObject = JSON.parseObject(json);
            pkg = jsonObject.getString("pkgName");
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject obj = new JSONObject();
        if(!TextUtils.isEmpty(pkg)) {
            if(context != null) {
                try {
                    PackageInfo info = context.getPackageManager().getPackageInfo(pkg, 0);
                    obj.put("status", 0);//应用存在
                    obj.put("versionName", info.versionName);
                    obj.put("versionCode", info.versionCode);
                } catch (PackageManager.NameNotFoundException e) {
                    obj.put("status", -1);//不存在
                    e.printStackTrace();
                }
            }
        }
        native2js(id, TextUtils.isEmpty(pkg) ? RET_FAIL : RET_SUCCESS, obj.toString());
    }

    @JavascriptInterface
    public void addLifecycleListener(String id, String json) {
        Log.d(TAG, "addLifecycleListener, id=" + id + ", json=" + json);
        if(id != null)
            lifeCycleListenerSet.add(id);
    }

    @JavascriptInterface
    public void removeLifecycleListener(String id, String json) {
        Log.d(TAG, "removeLifecycleListener, id=" + id + ", json=" + json);
        if(id != null)
            lifeCycleListenerSet.remove(id);
    }

    @Override
    public void onResume() {
        super.onResume();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("event", "resume");
        String params = jsonObject.toJSONString();
        try {
            for(String id : lifeCycleListenerSet) {
                native2js(id, RET_SUCCESS, params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("event", "pause");
        String params = jsonObject.toJSONString();
        try {
            for(String id : lifeCycleListenerSet) {
                native2js(id, RET_SUCCESS, params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void setH5Style(H5Style style) {
        this.style = style;
    }

    private static final String DEF = "";

    private String getBrand() {
        String result = DEF;
        // TODO
        return result;
    }

    private String getModel() {
        String result = DEF;
        // TODO
        return result;
    }

    private String getLanguage() {
        String result = DEF;
        // TODO
        return result;
    }

    private String getVersion() {
        String result = DEF;
        // TODO
        return result;
    }

    private String getPlatform() {
        String result = DEF;
        // TODO
        return result;
    }

    private int getSdkVersion() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 生成设备唯一id
     * @return
     */
    private static String getUniquePsuedoID() {
        try {
            String serial = "serial";
            String m_szDevIDShort = "35" +
                    Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                    Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                    Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                    Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                    Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                    Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                    Build.USER.length() % 10; //13 位

            try {
                //API>=9 使用serial号
                serial = Build.class.getField("SERIAL").get(null).toString();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            //使用硬件信息拼凑出来的15位号码
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception e) {
            return "";
        }
    }
}

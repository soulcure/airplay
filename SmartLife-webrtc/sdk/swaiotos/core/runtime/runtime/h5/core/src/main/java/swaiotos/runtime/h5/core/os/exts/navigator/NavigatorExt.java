package swaiotos.runtime.h5.core.os.exts.navigator;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.greenrobot.eventbus.EventBus;

import java.util.HashSet;
import java.util.Set;

import swaiotos.runtime.base.AppletActivity;
import swaiotos.runtime.base.AppletThread;
import swaiotos.runtime.h5.H5CoreExt;
import swaiotos.runtime.h5.common.event.ControlBarEvent;
import swaiotos.runtime.h5.core.os.exts.utils.ExtLog;

/**
 * @Author: yuzhan
 */
public class NavigatorExt extends H5CoreExt {
    public final static String NAME = "navigator";

    private Context context;
    private AppletActivity.HeaderHandler headerHandler;

    private final static String TAG = "NavigatorExt";

    public static synchronized H5CoreExt get(Context context) {
        return new NavigatorExt(context);
    }

    public NavigatorExt(Context context) {
        this.context = context;
    }

    public void setHeaderHandler(AppletActivity.HeaderHandler hh) {
        this.headerHandler = hh;
    }

    @JavascriptInterface
    public void setNavigationBarTitle(String id, String json) {
        ExtLog.d(TAG, "setNavigationBarTitle, id = " + id + ", json=" + json);
        innerSetNavigatorStyle(json);
        JSONObject params = new JSONObject();
        native2js(id, "success", params.toString());
    }

    @JavascriptInterface
    public void setDarkMode(String id, String json) {
        ExtLog.d(TAG, "setDarkMode, id = " + id + ", json=" + json);
        innerSetNavigatorStyle(json);
        JSONObject params = new JSONObject();
        native2js(id, "success", params.toString());
    }

    @JavascriptInterface
    public void setNavigationBarStyle(String id, String json) {
        ExtLog.d(TAG, "setNavigationBarTitle, id = " + id + ", json=" + json);
        innerSetNavigatorStyle(json);
        JSONObject params = new JSONObject();
        native2js(id, "success", params.toString());
    }

    @JavascriptInterface
    public void getBottomSpace(String id, String json) {
        ExtLog.d(TAG, "setNavigationBarTitle, id = " + id + ", json=" + json);
        innerSetNavigatorStyle(json);
        JSONObject params = new JSONObject();
        native2js(id, "success", params.toString());
    }

    @JavascriptInterface
    public void setControlBarStyle(String id, String json) {
        ExtLog.d(TAG, "setControlBarStyle, id = " + id + ", json=" + json);
        try {
            JSONObject jsonObject = JSON.parseObject(json);
            if(jsonObject.containsKey("display")) {
                ControlBarEvent event = new ControlBarEvent();
                event.visible = jsonObject.getBoolean("display");
                EventBus.getDefault().post(event);
                native2js(id, RET_SUCCESS, new JSONObject().toJSONString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void addControlBarChangedListener(String id) {
        ExtLog.d(TAG, "addControlBarChangedListener, id = " + id);
        if(controlBarListenerSets == null) {
            controlBarListenerSets = new HashSet<>();
            controlBarListenerSets.add(id);
        }
    }

    @JavascriptInterface
    public void removeControlBarChangedListener(String id) {
        ExtLog.d(TAG, "removeControlBarChangedListener, id = " + id);
        if(controlBarListenerSets != null) {
            controlBarListenerSets.remove(id);
        }
    }


    @JavascriptInterface
    public void setMenuButtonStyle(String id, String json) {
        ExtLog.d(TAG, "setMenuButtonStyle, id = " + id + ", json=" + json);
        try {
            JSONObject jsonObject = JSON.parseObject(json);
            if(jsonObject.containsKey("display")) {
                boolean b = jsonObject.getBoolean("display");
                AppletThread.UI(new Runnable() {
                    @Override
                    public void run() {
                        if(headerHandler != null) {
                            headerHandler.setExitButtonVisible(b);
                        }
                    }
                });
                native2js(id, RET_SUCCESS, new JSONObject().toJSONString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Set<String> controlBarListenerSets = null;
    public void onControlBarVisibleChanged(boolean b) {
        ExtLog.d(TAG, "onControlBarVisibleChanged : " + b);
        if(controlBarListenerSets == null)
            return ;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("display", b);
        for(String id : controlBarListenerSets) {
            native2js(id, ON_RECEIVE, jsonObject.toJSONString());
        }
    }


    private void innerSetNavigatorStyle(String json) {
        if(headerHandler == null)
            return ;
        try {
            final NavigatorBean bean = new NavigatorBean();
            JSONObject object = JSON.parseObject(json);
            if(object.getString("title") != null) {
                bean.title = object.getString("title");
            }
            if(object.getString("subTitle") != null) {
                bean.subTitle = object.getString("subTitle");
            }
            if(!TextUtils.isEmpty(object.getString("backgroundColor"))) {
                bean.backgroundColor = object.getString("backgroundColor");
            }
            if(!TextUtils.isEmpty(object.getString("alpha"))) {
                try {
                    bean.alpha = Float.parseFloat(object.getString("alpha"));
                } catch (Exception e){
                }
            }
            if(!TextUtils.isEmpty(object.getString("darkMode"))) {
                try {
                    bean.darkMode = Boolean.parseBoolean(object.getString("darkMode"));
                } catch (Exception e){
                }
            }
            if(!TextUtils.isEmpty(object.getString("backButtonVisible"))) {
                try {
                    bean.backButtonVisible = Boolean.parseBoolean(object.getString("backButtonVisible"));
                } catch (Exception e){
                }
            }
//            final NavigatorBean bean = JSON.parseObject(json, NavigatorBean.class);
            AppletThread.UI(new Runnable() {
                @Override
                public void run() {
                    if(bean.title != null) {
                        headerHandler.setTitle(bean.title, bean.subTitle);
                    }
                    headerHandler.setTitleAlpha(bean.alpha);

                    if(!TextUtils.isEmpty(bean.backgroundColor)) {
                        if(!bean.backgroundColor.startsWith("#")) {
                            bean.backgroundColor = "#" + bean.backgroundColor;
                        }
                        try {
                            headerHandler.setBackgroundColor(Color.parseColor(bean.backgroundColor));
                        } catch (Exception e) {
                            e.printStackTrace();
                            if(bean.darkMode != null)
                                headerHandler.setDarkMode(bean.darkMode);
                        }
                    } else {
                        if(bean.darkMode != null)
                            headerHandler.setDarkMode(bean.darkMode);
                    }
                    headerHandler.setTitleAlpha(bean.alpha);
                    if(bean.backButtonVisible != null) {
                        headerHandler.setBackButtonVisible(bean.backButtonVisible);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

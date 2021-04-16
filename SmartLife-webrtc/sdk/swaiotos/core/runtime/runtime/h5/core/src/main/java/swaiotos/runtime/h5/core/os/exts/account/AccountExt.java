package swaiotos.runtime.h5.core.os.exts.account;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.IUserInfo;
import com.tianci.user.api.SkyUserApi;
import com.tianci.user.api.listener.OnAccountChangedListener;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import swaiotos.runtime.h5.H5CoreExt;
import swaiotos.runtime.h5.core.os.exts.utils.ExtLog;

/**
 * @ClassName: AccountExt
 * @Author: lu
 * @CreateDate: 11/18/20 2:33 PM
 * @Description:
 */
public class AccountExt extends H5CoreExt {
    public static final String TAG = "AccountExt";
    public static final String NAME = "account";

    private static H5CoreExt ext = null;
    private static WeakReference<Context> contextWeakRef;
    private final SkyUserApi userApi;
    private final Set<String> listenerIds = new TreeSet<>();

    public static synchronized H5CoreExt get(Context context) {
        if (ext == null) {
            ext = new AccountExt(context);
            contextWeakRef = new WeakReference<>(context);
        }
        return ext;
    }

    private AccountExt(Context context) {
        userApi = new SkyUserApi(context);
        userApi.registerAccountChanged(accountChangedListener);
    }

    @JavascriptInterface
    public void isLogin(String id) {
        ExtLog.d(TAG, "isLogin(), id: " + id);
        boolean login = userApi.hasLogin();
        ExtLog.i(TAG, "isLogin(), result: " + login);

        JSONObject params = new JSONObject();
        params.put("isLogin", String.valueOf(login));
        native2js(id, "success", params.toJSONString());
//        EventBus.getDefault().post(new OnJsCallbackData(id, "success", params.toJSONString()));
    }

    @JavascriptInterface
    public void startLogin(String id) {
        ExtLog.d(TAG, "startLogin(), id: " + id);
        boolean result = contextWeakRef != null && contextWeakRef.get() != null;
        if (result) {
            SkyUserApi.showAccountManager(contextWeakRef.get());
        } else {
            ExtLog.w(TAG, "startLogin(), result: " + result);
        }

        JSONObject params = new JSONObject();
        params.put("result", String.valueOf(result));
        String name = result ? "success" : "fail";
        native2js(id, name, params.toString());
        // EventBus.getDefault().post(new OnJsCallbackData(id, "onSuccess", String.valueOf
        // (result)));
    }

    @JavascriptInterface
    public void getAccessToken(String id) {
        ExtLog.d(TAG, "getAccessToken(), id: " + id);
        String token = userApi.getToken("ACCESS");
        JSONObject params = new JSONObject();
        params.put("token", token);
        native2js(id, "success", params.toJSONString());
        // EventBus.getDefault().post(new OnJsCallbackData(id, "onSuccess", token));
    }

    @JavascriptInterface
    public void getAccountInfo(String id) {
        ExtLog.d(TAG, "getAccountInfo(), id: " + id);
        Map<String, Object> userInfo = userApi.getAccoutInfo();
        IUserInfo tmpUserInfo = SmartApi.getUserInfo();
        if(tmpUserInfo != null && !TextUtils.isEmpty(tmpUserInfo.tp_token) && userInfo != null) {
            userInfo.put("tp_token", tmpUserInfo.tp_token);
        }
        String info = userInfo == null ? null : JSON.toJSONString(userInfo);
        native2js(id, "success", info);
//        JSONObject params = new JSONObject();
//        params.put("info", info);
//        native2js(id, "success", params.toJSONString());
        // EventBus.getDefault().post(new OnJsCallbackData(id, "onSuccess", info));
    }

    @JavascriptInterface
    public void addAccountChangedListener(String id) {
        synchronized (listenerIds) {
            ExtLog.d(TAG, "addAccountChangedListener(), id: " + id);
            listenerIds.add(id);
        }
        JSONObject params = new JSONObject();
        params.put("listenerId", id);
        native2js(id, "success", params.toJSONString());
    }

    @JavascriptInterface
    public void removeAccountChangedListener(String id) {
        synchronized (listenerIds) {
            ExtLog.d(TAG, "removeAccountChangedListener(), id: " + id);
            listenerIds.remove(id);
        }
        JSONObject params = new JSONObject();
        native2js(id, "success", params.toJSONString());
    }

    /**
     * web端触发账户token刷新，通知native端
     * @param id
     * @param json
     */
    @JavascriptInterface
    public void updateAccessToken(String id, String json) {
        Log.d(TAG, "updateAccessToken, id=" + id + ", json=" + json);
        try {
            JSONObject jsonObject = JSON.parseObject(json);
            String accessToken = jsonObject.getString("accessToken");
            if(!TextUtils.isEmpty(accessToken)) {
                SmartApi.updateAccessToken(accessToken);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private OnAccountChangedListener accountChangedListener = new OnAccountChangedListener() {
        @Override
        public void onAccountChanged() {
            boolean login = userApi.hasLogin();
            ExtLog.d(TAG, "onAccountChanged(), login = " + login);
            JSONObject params = new JSONObject();
            params.put("isLogin", String.valueOf(login));
            String json = params.toString();

            for (String id : listenerIds) {
                native2js(id, ON_RECEIVE, json);
            }
        }
    };
}

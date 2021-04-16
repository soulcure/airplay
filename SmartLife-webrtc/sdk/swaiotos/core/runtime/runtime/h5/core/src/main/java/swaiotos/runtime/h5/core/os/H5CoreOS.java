package swaiotos.runtime.h5.core.os;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import swaiotos.channel.iot.ss.channel.im.IMMessage;
import swaiotos.runtime.base.AppletThread;
import swaiotos.runtime.h5.BaseH5AppletActivity;
import swaiotos.runtime.h5.BuildConfig;
import swaiotos.runtime.h5.H5Core;
import swaiotos.runtime.h5.H5CoreExt;
import swaiotos.runtime.h5.H5NPAppletActivity;
import swaiotos.runtime.h5.H5SSClientService;
import swaiotos.runtime.h5.H5Style;
import swaiotos.runtime.h5.R;
import swaiotos.runtime.h5.common.bean.H5ContentBean;
import swaiotos.runtime.h5.common.bean.SsePushBean;
import swaiotos.runtime.h5.common.event.OnFunctonCBData;
import swaiotos.runtime.h5.common.event.OnGameEngineInfo;
import swaiotos.runtime.h5.common.event.OnGameInfoCBData;
import swaiotos.runtime.h5.common.event.OnJsCallbackData;
import swaiotos.runtime.h5.common.event.OnQrCodeCBData;
import swaiotos.runtime.h5.common.event.OnRemoteAppVersion;
import swaiotos.runtime.h5.common.event.OnRemoteStateData;
import swaiotos.runtime.h5.common.event.OnSetEnableIMReceive;
import swaiotos.runtime.h5.common.event.OnShakeEventCBData;
import swaiotos.runtime.h5.common.event.OnShakeRegisterCBData;
import swaiotos.runtime.h5.common.event.OnUISafeDistanceCBData;
import swaiotos.runtime.h5.common.event.OnUserInfo;
import swaiotos.runtime.h5.common.event.OnVibrateEvent;
import swaiotos.runtime.h5.common.event.SetCastFromShow;
import swaiotos.runtime.h5.common.event.UrlLoadFinishedEvent;
import swaiotos.runtime.h5.common.util.LogUtil;
import swaiotos.runtime.h5.common.util.URLSplitUtil;
import swaiotos.runtime.h5.core.os.exts.SW;
import swaiotos.runtime.h5.core.os.model.SendMessageManager;
import swaiotos.runtime.h5.core.os.webview.AppletJavascriptInterface;
import swaiotos.runtime.h5.core.os.webview.AppletWebChromeClient;
import swaiotos.runtime.h5.core.os.webview.AppletWebViewClient;
import swaiotos.runtime.h5.core.os.webview.IAppletJsPushListener;
import swaiotos.runtime.h5.core.os.webview.LoadingStateWebViewClient;
import swaiotos.runtime.h5.core.os.webview.TVAppletWebViewChromeClient;
import swaiotos.runtime.h5.core.os.webview.TVAppletWebViewClient;
import swaiotos.runtime.h5.core.os.webview.TVWebViewLoading;

import static swaiotos.runtime.h5.core.os.H5RunType.RunType.MOBILE_RUNTYPE_ENUM;


//import swaiotos.runtime.h5.core.os.model.SendMessageManager;

/**
 * @ClassName: H5CoreOS
 * @Author: lu
 * @CreateDate: 2020/10/25 4:29 PM
 * @Description:
 */
public class H5CoreOS implements H5Core, IAppletJsPushListener {

    RelativeLayout rlayout;
    protected WebView mWebView;
    public static final String TAG = "H5CoreOS";

    private static H5RunType.RunType mRunType = MOBILE_RUNTYPE_ENUM;

    private SsePushBean bean;
    private OnGameEngineInfo localGameInfo;

    private Context mContext;
    private AppletJavascriptInterface javascriptInterface;
    private SendMessageManager sendMessageManager;
    protected SW sw;
    private boolean enableIMReceive = false;
    private H5ExtJS extJs;
    protected String id; //用来区分EventBus的消息类型
    private LoadingStateWebViewClient appletWebViewClient;
    private H5Style h5Style;
    private AppletWebChromeClient appletWebChromeClient;

    public H5CoreOS(H5RunType.RunType type, SsePushBean pushBean) {
        mRunType = type;
        bean = pushBean;
        id = String.valueOf(System.currentTimeMillis());
    }

    public H5CoreOS(H5RunType.RunType type, SsePushBean pushBean, String id) {
        mRunType = type;
        bean = pushBean;
        this.id = id;
    }

    public String getLeftBtnType() {
        if (javascriptInterface == null) {
            Log.e(TAG, "getLeftBtnType getLeftBtnType == null");
            return null;
        }
        return javascriptInterface.getLeftBtnType();
    }

    public static void initH5OSRunType(H5RunType.RunType type) {
        mRunType = type;
    }

    public static H5RunType.RunType getH5RunType() {
        return mRunType;
    }

    public void updateSsePushBean(SsePushBean sseBean) {
        bean = sseBean;
    }

    public void updateGameEngine(OnGameEngineInfo engineInfo) {
        localGameInfo = engineInfo;
    }

    public void updateAppletNetType(String netType) {
        if (sendMessageManager != null) {
            sendMessageManager.setNetworkForceType(netType);
        }
    }

    @SuppressLint("JavascriptInterface")
    public void initWebViewDongle(Context context, Map<String, H5CoreExt> extension) {
        Log.d(TAG, "initWebViewDongle()");
        WebView wv = rlayout.findViewById(R.id.tv_webview1);
        mWebView = wv;
        //设置支持JS
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        sendMessageManager = new SendMessageManager(context);
        javascriptInterface = new AppletJavascriptInterface(sendMessageManager, id);
        wv.addJavascriptInterface(javascriptInterface, "coocaaAppletJS");
        if (extension != null) {
            Set<Map.Entry<String, H5CoreExt>> exts = extension.entrySet();
            for (Map.Entry<String, H5CoreExt> ext : exts) {
                ext.getValue().setWebView(wv);
                try {
                    wv.addJavascriptInterface(ext.getValue(), ext.getKey());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (SW.NAME.equals(ext.getKey())) {
                    sw = (SW) ext.getValue();
                }
            }
        }
// 清缓存和记录，缓存引起的白屏
        wv.clearCache(true);
        wv.clearHistory();

        // 设置支持本地存储
        wv.getSettings().setDatabaseEnabled(true);
        //取得缓存路径
        //设置路径
        wv.getSettings().setDatabasePath(context.getDir("cache", Context.MODE_PRIVATE).getPath());
        //设置支持DomStorage
        wv.getSettings().setDomStorageEnabled(true);
        //设置存储模式
        wv.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        //设置适应屏幕
        wv.getSettings().setUseWideViewPort(true);
        wv.getSettings().setLoadWithOverviewMode(true);
        wv.getSettings().setSupportZoom(true);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setDisplayZoomControls(false);
        //设置缓存
        wv.getSettings().setAppCacheEnabled(true);
        wv.requestFocus();

        //设置支持file
        wv.getSettings().setAllowFileAccess(true);
        wv.getSettings().setAllowContentAccess(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            wv.getSettings().setAllowFileAccessFromFileURLs(true);
            wv.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }

        switch (mRunType) {
            case MOBILE_RUNTYPE_ENUM: {
                // 此处需要自动播放音乐
                wv.getSettings().setMediaPlaybackRequiresUserGesture(false);
            }
            break;
            case TV_RUNTYPE_ENUM: {
                // tv 和 dongle 自动播放音乐
                wv.getSettings().setMediaPlaybackRequiresUserGesture(false);
            }
            break;
            default:
                break;
        }
        //下面三个各种监听

        LinearLayout loadingLayout = rlayout.findViewById(R.id.linear_loadingLayout);
        LinearLayout contentLayout = rlayout.findViewById(R.id.linear_miniprog);

        TVWebViewLoading tvWebViewLoading = new TVWebViewLoading(context, new TVWebViewLoading.TVWebViewLoadFailListener() {
            @Override
            public void onWebViewLoadFail() {
                try {
                    ((Activity) mContext).finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, loadingLayout, contentLayout);
        appletWebViewClient = new TVAppletWebViewClient(tvWebViewLoading);
        wv.setWebViewClient(appletWebViewClient);
        try {
            wv.setWebChromeClient(new TVAppletWebViewChromeClient(((BaseH5AppletActivity) mContext), tvWebViewLoading));
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventBus.getDefault().register(this);
    }

    @SuppressLint("JavascriptInterface")
    public void initWebViewMobile(Context context, Map<String, H5CoreExt> extension) {
        WebView wv = rlayout.findViewById(R.id.id_web_view);
        mWebView = wv;
        //设置支持JS
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        sendMessageManager = new SendMessageManager(context);
        javascriptInterface = new AppletJavascriptInterface(sendMessageManager, id);
        wv.addJavascriptInterface(javascriptInterface, "coocaaAppletJS");

        if (extension != null) {
            Set<Map.Entry<String, H5CoreExt>> exts = extension.entrySet();
            for (Map.Entry<String, H5CoreExt> ext : exts) {
                ext.getValue().setWebView(wv);
                try {
                    wv.addJavascriptInterface(ext.getValue(), ext.getKey());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (SW.NAME.equals(ext.getKey())) {
                    sw = (SW) ext.getValue();
                }
            }
        }

        // 清缓存和记录，缓存引起的白屏
        wv.clearCache(true);
        wv.clearHistory();

        // 设置支持本地存储
        wv.getSettings().setDatabaseEnabled(true);
        //取得缓存路径
        //设置路径
        wv.getSettings().setDatabasePath(context.getDir("cache", Context.MODE_PRIVATE).getPath());
        //设置支持DomStorage
        wv.getSettings().setDomStorageEnabled(true);
        //设置存储模式
        wv.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        //设置适应屏幕
        wv.getSettings().setUseWideViewPort(true);
        wv.getSettings().setLoadWithOverviewMode(true);
        wv.getSettings().setSupportZoom(true);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setDisplayZoomControls(false);
        //设置缓存
        wv.getSettings().setAppCacheEnabled(true);
        wv.requestFocus();

        //设置支持file
        wv.getSettings().setAllowFileAccess(true);
        wv.getSettings().setAllowContentAccess(true);

        //增加支持https页面访问http图片
        wv.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        wv.getSettings().setBlockNetworkImage(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            wv.getSettings().setAllowFileAccessFromFileURLs(true);
            wv.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }

        switch (mRunType) {
            case MOBILE_RUNTYPE_ENUM: {
                // 此处需要自动播放音乐
                wv.getSettings().setMediaPlaybackRequiresUserGesture(false);
            }
            break;
            case TV_RUNTYPE_ENUM: {
                // tv 和 dongle 自动播放音乐
                wv.getSettings().setMediaPlaybackRequiresUserGesture(false);
            }
            break;
            default:
                break;
        }
        //下面三个各种监听
        appletWebViewClient = createMobileWebViewClient(context);
        wv.setWebViewClient(appletWebViewClient);

        appletWebChromeClient = new AppletWebChromeClient(((BaseH5AppletActivity) mContext), rlayout);
        try {
            wv.setWebChromeClient(appletWebChromeClient);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button btnRetry = rlayout.findViewById(R.id.btn_retry);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(appletWebViewClient!=null){
                    appletWebViewClient.resetLoadState();
                }
                if(appletWebChromeClient!=null){
                    appletWebChromeClient.resetLoadState();
                }
                wv.reload();
            }
        });

        EventBus.getDefault().register(this);

        onWebViewInited();
    }

    protected void onWebViewInited() {

    }

    protected LoadingStateWebViewClient createMobileWebViewClient(Context context) {
        return new AppletWebViewClient(context, rlayout, this.id);
    }

    @Override
    public View create(Context context, Map<String, H5CoreExt> extension) {
        mContext = context;
        if (rlayout == null) {
            if (H5SSClientService.isTVOrDongle()) {
                LayoutInflater inflater = LayoutInflater.from(context);
                rlayout = (RelativeLayout) inflater.inflate(R.layout.tvloadingwebview, null);
                rlayout.setTag("Screen");
                initWebViewDongle(context, extension);
            } else {
                LayoutInflater inflater = LayoutInflater.from(context);
                rlayout = (RelativeLayout) inflater.inflate(R.layout.loadingwebview, null);
                rlayout.setTag("Mobile");
                initWebViewMobile(context, extension);
            }
        }
        if (sw != null) {
            sw.attach(mContext);
        }
        return rlayout;
    }

    private String readyLoadUrl = null;

    private boolean isLoadUrlSame(String compareUrl) {
        boolean isSame = false;
        if (readyLoadUrl != null && compareUrl != null) {
            if (readyLoadUrl.equals(compareUrl)) {
                isSame = true;
            } else {
                Map<String, String> readyLoadUrlParams = URLSplitUtil.urlSplit(readyLoadUrl);
                Map<String, String> compareUrlParams = URLSplitUtil.urlSplit(compareUrl);
                String readyLoadUrlRuntimeTag = readyLoadUrlParams.get("h5-runtime-tag");
                String compareUrlRuntimeTag = compareUrlParams.get("h5-runtime-tag");

                if (readyLoadUrlRuntimeTag != null && compareUrlRuntimeTag != null) {
                    if (readyLoadUrlRuntimeTag.trim().equals(compareUrlRuntimeTag.trim())) {
                        isSame = true;
                    }
                }
            }
        }
        return isSame;
    }

    @Override
    public void load(String url) {
        LogUtil.d("H5Core OS load url " + url);
        if (extJs != null) {
            //需要额外加载js的，在finish前隐藏webview
            mWebView.setVisibility(View.INVISIBLE);
        }

        if (readyLoadUrl == null && null != mWebView && mContext != null) {
            readyLoadUrl = url;

            mWebView.loadUrl(url);

        } else if (isLoadUrlSame(url) && null != mWebView && mContext != null) {
            if (bean != null) {
                H5ContentBean h5Bean = H5ContentBean.fromJSONString(bean.getData());
                onReceiveMessage(h5Bean.getH5Content());
            }
            if (localGameInfo != null) {
                OnGameInfoCBData infoData = new OnGameInfoCBData("onGameEngineCB", localGameInfo);
                OnGameEngineInfoCallBack(infoData);
            }
        } else {
            if (null != mWebView && mContext != null) {
                mWebView.clearView();
                readyLoadUrl = url;
                mWebView.loadUrl(url);
            }
        }
    }

    @Override
    public String curUrl() {
        return mWebView == null ? null : mWebView.getUrl();
    }

    @Override
    public void setExtJS(String js) {
        this.extJs = H5ExtJS.tryGetInstance(js);
        if (extJs != null) {
            extJs.getExtJsUrl(); //提前加载
        }
    }

    HashMap<String, H5ContentBean> ssePushMap = new HashMap<>();
    LinkedList<String> injectJSCache = new LinkedList<>();
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onSSEEvent(SsePushEvent event) {
//        LogUtil.androidLog("SsePushEvent----");
//        SsePushBean ssePushBean = (SsePushBean) event.getData();
//        H5ContentBean h5Bean = ssePushBean.getH5ContentBean();
//        ssePushMap.put(readyLoadUrl,h5Bean);
//        if(null == readyLoadUrl){
//            Log.d(TAG,"null ==  readyLoadUrl!");
//            load(h5Bean.getH5ReceivedUrl());
//        }else if(null!=readyLoadUrl && readyLoadUrl.equals(h5Bean.getH5ReceivedUrl())){
//            load(h5Bean.getH5ReceivedUrl());
//            onReceiveMessage(h5Bean.getH5Content());
//        }else{
//            Log.d(TAG,"readyLoadUrl == h5Bean.getH5ReceivedUrl");
//            onReceiveMessage(h5Bean.getH5Content());
//        }
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRemoteAppVersion(OnRemoteAppVersion event) {
        LogUtil.androidLog("onRemoteAppVersion event: " + event.toString());
        if (event == null || mWebView == null) {
            LogUtil.androidLog("onReceiveRemoteMsg event == null || mWebView == null");
            return;
        }
        onReceiveMessage(JSON.toJSONString(event));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIMMessage(IMMessage message) {
        if (!enableIMReceive) {
            return;
        }
        LogUtil.androidLog("onIMMessage event: " + message.toString());
        if (message == null || mWebView == null) {
            LogUtil.androidLog("onReceiveRemoteMsg event == null || mWebView == null");
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("event", "onIMMessage");
        map.put("imMessage", message);
        onReceiveMessage(JSON.toJSONString(map));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveRemoteMsg(H5ContentBean event) {
        LogUtil.androidLog("onReceiveRemoteMsg event: " + event.toString());
        if (event == null || mWebView == null) {
            LogUtil.androidLog("onReceiveRemoteMsg event == null || mWebView == null");
            return;
        }
        onReceiveMessage(event.getH5Content());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSetCastFromShow(SetCastFromShow event) {
        LogUtil.androidLog("onSetCastFromShow----");
        if (sendMessageManager != null && event != null) {
            int show = event.getEventType();
            sendMessageManager.setCastFromShow(show != 0 ? true : false);
        } else {
            LogUtil.androidLog("sendMessageManager == null || event == null");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUrlLoadEvent(UrlLoadFinishedEvent event) {
        LogUtil.androidLog("UrlLoadEvent----" + event);

        if (bean != null) {
            H5ContentBean h5Bean = H5ContentBean.fromJSONString(bean.getData());
            ssePushMap.put(readyLoadUrl, h5Bean);
        }

        readyLoadUrl = (String) event.getData();
        LogUtil.androidLog("onUrlLoadFinished, readyLoadUrl=" + readyLoadUrl);
        H5ContentBean bean = ssePushMap.get(readyLoadUrl);
        Handler handler = new Handler();
        if (null != bean) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    onReceiveMessage(bean.getH5Content());
                }
            }, 600);
        }
        String data = null;
        do {
            try {
                if ((this.id != null && event != null && event.getEventId() != null && event.getEventId().equals(this.id)) || this.id == null || H5SSClientService.isTVOrDongle()) {
                    data = injectJSCache.pop();
                    if (data != null) {
                        String finalData = data;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                evaluateJavascript(finalData, false);
                            }
                        }, 600);
                    }
                }

            } catch (NoSuchElementException e) {
                data = null;
            }
        } while (data != null);
        loadExtJs();
    }

    public boolean isPageLoadFinished() {
        return readyLoadUrl != null && TextUtils.equals(readyLoadUrl, mWebView.getUrl());
    }

    public void loadExtJs() {
        if (extJs == null)
            return ;
        LogUtil.androidLog("onUrlLoadFinished, loadExtJs");
        mWebView.setVisibility(View.VISIBLE);
        AppletThread.execute(new Runnable() {
            @Override
            public void run() {
                String extJsContent = extJs.getExtJsContent();
                if (readyLoadUrl != null && !TextUtils.isEmpty(extJsContent) && !TextUtils.equals(readyLoadUrl, extJs.getExtJsUrl())) {
                    LogUtil.androidLog("onUrlLoadFinished, start load extJs url= " + extJs.getExtJsUrl());
                    AppletThread.UI(new Runnable() {
                        @Override
                        public void run() {
                            mWebView.loadUrl(extJsContent);
                        }
                    });
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserInfo(OnUserInfo info) {
        LogUtil.androidLog("onUserInfo event: " + info.toString());
        if (info == null || mWebView == null) {
            LogUtil.androidLog("onUserInfo event == null || mWebView == null");
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("event", info.event);
        map.put("code", info.code);
        map.put("ak", info.accessToken);
        map.put("avatar", info.avatar);
        map.put("nickname", info.nickName);
        map.put("mobile", info.mobile);
        map.put("open_id", info.open_id);
        map.put("callbackId", info.callbackId);
        map.put("callbackCode", info.callbackCode);
        String data = JSON.toJSONString(map);
        evaluateJavascript("__CCCallback.onNativeMessage('" + data + "')");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onShakeRegister(OnShakeRegisterCBData info) {
        if (mRunType == MOBILE_RUNTYPE_ENUM) {
            LogUtil.d("onShakeRegister");
            ((H5NPAppletActivity) mContext).registerShakeListener();

            HashMap<String, Object> map = new HashMap<>();
            map.put("event", info.event);
            map.put("callbackId", info.callbackId);
            map.put("callbackCode", info.callbackCode);
            String data = JSON.toJSONString(map);
            evaluateJavascript("__CCCallback.onNativeMessage('" + data + "')");
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onShakeEvent(OnShakeEventCBData info) {
        if (mRunType == MOBILE_RUNTYPE_ENUM) {
            LogUtil.d("onShakeEvent");
            HashMap<String, Object> map = new HashMap<>();
            map.put("event", info.event);
            String data = JSON.toJSONString(map);
            evaluateJavascript("__CCCallback.onNativeMessage('" + data + "')");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVibrateEvent(OnVibrateEvent event) {

        if (mRunType == MOBILE_RUNTYPE_ENUM) {
            LogUtil.d("onVibrateEvent");
            Vibrator vibrator = (Vibrator) mContext
                    .getSystemService(Service.VIBRATOR_SERVICE);
            int vibrateTime = 60;
            if (event.time != null && event.time == 1) {
                vibrateTime = 180;
            }
            LogUtil.androidLog("onVibrateEvent vibrateTime: " + vibrateTime);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(vibrateTime, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(vibrateTime);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onJsCallBack(OnJsCallbackData data) {
        if (data == null || mWebView == null) {
            LogUtil.androidLog("onJsCallBack event == null || mWebView == null");
            return;
        }
        String json = JSON.toJSONString(data);
        // LogUtil.androidLog("onJsCallBack data: " + data);
        evaluateJavascript("call2js('" + json + "')");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRemoteCtrl(OnRemoteStateData data) {
        LogUtil.androidLog("onRemoteCtrl event: " + data.toString());
        if (data == null || mWebView == null) {
            LogUtil.androidLog("onUserInfo event == null || mWebView == null");
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("event", data.event);
        map.put("play_cmd", data._playCmd);
        map.put("type", data._type);
        map.put("param", data._param);
        String mapData = JSON.toJSONString(map);
        evaluateJavascript("__CCCallback.onNativeMessage('" + mapData + "')");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnFunctinCallback(OnFunctonCBData data) {

        LogUtil.androidLog("OnFunctinCallback event: " + data.toString());
        if (data == null || data.data == null || mWebView == null) {
            LogUtil.androidLog("onUserInfo event == null || mWebView == null");
            return;
        }
        String str = JSONObject.toJSON(data.data).toString();
//        str = str.replace("\\", "\\\\");
//        Log.d(TAG, "OnFunctinCallback() str = [" + str + "]");
        evaluateJavascript("__CCCallback.onNativeMessage('" + str + "')");
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void OnReceiveImMessage(IMMessage message) {
//        if (sw == null) {
//            LogUtil.androidLog("OnChannelMessage sw == null");
//            return;
//        }
//
//        ChannelExt channelExt = (ChannelExt) sw.require(ChannelExt.NAME);
//        LogUtil.androidLog("OnChannelMessage ext: " + channelExt);
//        LogUtil.androidLog("OnChannelMessage msg: " + message);
//        if (channelExt != null) {
//            channelExt.onReceiveMessage(message);
//        }
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnGameEngineInfoCallBack(OnGameInfoCBData engineInfo) {
        LogUtil.androidLog("OnGameEngineInfoCallBack event: " + engineInfo.gameInfo.toString());
        if (engineInfo == null || mWebView == null || (engineInfo != null && engineInfo.gameInfo == null)) {
            LogUtil.androidLog("OnGameEngineInfoCallBack event == null || mWebView == null");
            return;
        }
        if (MOBILE_RUNTYPE_ENUM == mRunType) {
            LogUtil.e("mobile can only handle custom_data");
            if ("tv_destroyed".equals(engineInfo.gameInfo.engineEvent) || "custom_data".equals(engineInfo.gameInfo.engineEvent)) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("event", engineInfo.event);
                map.put("engineInfo", engineInfo.gameInfo);
                String mapData = JSON.toJSONString(map);
                evaluateJavascript("__CCCallback.onNativeMessage('" + mapData + "')");
                updateGameEngine(engineInfo.gameInfo);
            }
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("event", engineInfo.event);
        map.put("engineInfo", engineInfo.gameInfo);
        String mapData = JSON.toJSONString(map);
        evaluateJavascript("__CCCallback.onNativeMessage('" + mapData + "')");

        updateGameEngine(engineInfo.gameInfo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnGetQrCodeCallBack(OnQrCodeCBData qrCode) {
        if (qrCode == null || mWebView == null) {
            LogUtil.androidLog("OnGetQrCodeCallBack event == null || mWebView == null");
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("event", qrCode.event);
        map.put("bindCode", qrCode.bindCode);
        map.put("qrCode", qrCode.qrCode);
        String mapData = JSON.toJSONString(map);
        evaluateJavascript("__CCCallback.onNativeMessage('" + mapData + "')");
    }

    public Display getDispaly(Context context) {
        WindowManager manager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUISafeDistance(OnUISafeDistanceCBData safeDistanceCBData) {
        if ((safeDistanceCBData == null || this.h5Style == null) || mWebView == null) {
            LogUtil.androidLog("onUISafeDistance event == null || mWebView == null");
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("event", safeDistanceCBData.event);
        map.put("top", (this.h5Style.getSafeDistanceTop()) * 100 / getDispaly(mContext).getWidth());
        map.put("bottom", this.h5Style.getSafeDistanceBottom() * 100 / getDispaly(mContext).getWidth());
        map.put("version",safeDistanceCBData.versionCode);//增加版本号兼容旧版本
        String mapData = JSON.toJSONString(map);
        evaluateJavascript("__CCCallback.onNativeMessage('" + mapData + "')");
    }

    @Override
    public void onReceiveMessage(String data) {
        LogUtil.androidLog("onReceiveMessage---- " + data);
        evaluateJavascript("onReceiveMessage('" + data + "');");
    }

    public void evaluateJavascript(String data) {
        evaluateJavascript(data, true);
    }

    @Override
    public void setH5Style(H5Style style) {
        this.h5Style = style;
        if (this.h5Style != null) {
            onUISafeDistance(new OnUISafeDistanceCBData("onUISafeDistance"));
//            EventBus.getDefault().post(new OnUISafeDistanceCBData("onUISafeDistance", this.h5Style.getSafeDistanceTop(), this.h5Style.getSafeDistanceBottom()));
        }
    }

    @Override
    public void onResume() {
        if (sw != null) {
            sw.onResume();
        }
    }

    @Override
    public void onPause() {
        if (sw != null) {
            sw.onPause();
        }
    }

    @Override
    public void onStop() {
        if (sw != null) {
            sw.onStop();
        }
    }

    @Override
    public WebView getWebView() {
        return mWebView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void evaluateJavascript(String data, boolean isNeedCache) {
        LogUtil.androidLog("evaluateJavascript isNeedCache: " + isNeedCache + " data: " + data);
        if (mWebView != null && mContext != null && appletWebViewClient != null && data != null && !data.equals("")) {
            LogUtil.androidLog("evaluateJavascript appletWebViewClient.isLoadOk: " + appletWebViewClient.isLoadOk());
            if (appletWebViewClient.isLoadOk()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    data = data.replace("\\", "\\\\");
                    mWebView.evaluateJavascript(data, null);
                } else {
                    mWebView.loadUrl("javascript:" + data);
                }
            } else if (isNeedCache) {
                LogUtil.androidLog("evaluateJavascript add to injectJSCache ");
                injectJSCache.add(data);
            }
        } else {
            LogUtil.androidLog("evaluateJavascript mWebView == null ");
        }
    }

    public void onLeftBtnClick() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("event", "onLeftBtnClick");
        map.put("leftBtnType", getLeftBtnType());
        String data = JSON.toJSONString(map);
        evaluateJavascript("__CCCallback.onNativeMessage('" + data + "')");
    }

    @Override
    public void onShareBtnClick() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("event", "onShareBtnClick");
        String data = JSON.toJSONString(map);
        evaluateJavascript("__CCCallback.onNativeMessage('" + data + "')");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public boolean onSetEnableIMReceive(OnSetEnableIMReceive msg) {
        enableIMReceive = msg.enable;
        return true;
    }

    @Override
    public boolean onBackPressed() {
        LogUtil.d("H5Core OS onBackPressed");
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        if (mWebView.getParent() != null) {
            ((ViewGroup) mWebView.getParent()).setBackgroundColor(Color.TRANSPARENT);
        }
        mWebView.setBackgroundColor(color);
    }

    @Override
    public void destroy() {
        if (null != mWebView) {
            broadCastDestroyed();
            appletWebViewClient = null;
            mWebView.stopLoading();
            mWebView.getSettings().setJavaScriptEnabled(false);
            mWebView.clearHistory();
            mWebView.removeAllViews();
            mWebView.destroy();
            mWebView = null;
            EventBus.getDefault().unregister(this);
        }
        if (sw != null) {
            sw.destroy(mContext);
        }
        rlayout = null;
    }

    private void broadCastDestroyed() {
        if (null != mContext && null != sendMessageManager && null != localGameInfo) {
            LogUtil.d("broadCastDestroyed game event = " + localGameInfo.engineEvent);

            OnGameEngineInfo engineInfo = new OnGameEngineInfo();
            engineInfo.type = "game_engine";
            if (H5SSClientService.isTVOrDongle()) {
                engineInfo.engineEvent = "tv_destroyed";
            } else {
                engineInfo.engineEvent = "mobile_destroyed";
            }
            engineInfo.tvGameUrl = localGameInfo.tvGameUrl;
            engineInfo.mobileGameUrl = localGameInfo.mobileGameUrl;
            if (localGameInfo.mobileGameUrl != null) {
                Map<String, String> readyLoadUrlParams = URLSplitUtil.urlSplit(localGameInfo.mobileGameUrl);
                String readyLoadUrlRuntimeTag = readyLoadUrlParams.get("appId");
                if (readyLoadUrlRuntimeTag == null || readyLoadUrlRuntimeTag != "gameRemoteCtrl") {
                    sendMessageManager.sendGameEvent(engineInfo, null, null);
                }
            } else {
                sendMessageManager.sendGameEvent(engineInfo, null, null);
            }
        }
    }
}

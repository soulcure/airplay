package swaiotos.runtime.h5;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.coocaa.smartsdk.SmartApi;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import swaiotos.runtime.Applet;
import swaiotos.runtime.base.AppletActivity;
import swaiotos.runtime.h5.common.bean.SsePushBean;
import swaiotos.runtime.h5.common.event.SetHeaderColorEvent;
import swaiotos.runtime.h5.common.event.SetLeftBtnEvent;
import swaiotos.runtime.h5.common.event.SetNativeUI;
import swaiotos.runtime.h5.common.util.LogUtil;
import swaiotos.runtime.h5.core.os.H5CoreOS;
import swaiotos.runtime.h5.core.os.H5RunType;
import swaiotos.runtime.h5.core.os.exts.account.AccountExt;
import swaiotos.runtime.h5.core.os.exts.channel.ChannelExt;
import swaiotos.runtime.h5.core.os.exts.device.DeviceExt;
import swaiotos.runtime.h5.core.os.exts.payment.PaymentExt;
import swaiotos.runtime.h5.core.os.exts.runtime.RuntimeExt;
import swaiotos.runtime.h5.core.os.exts.share.ShareExt;

import static swaiotos.runtime.h5.core.os.H5RunType.RUNTIME_NAV_FLOAT;
import static swaiotos.runtime.h5.core.os.H5RunType.RUNTIME_NAV_KEY;
import static swaiotos.runtime.h5.core.os.H5RunType.RunType.MOBILE_RUNTYPE_ENUM;

@Deprecated
public class H5AppletActivity extends AppletActivity {
    private String runTime;
    private String runTime_nav;

    public static void start(Context context, Applet applet) {
        String h5Uri = applet.getType() + "://" + applet.getId() + applet.getTarget();
        if (null != applet.getAllParams()) {
            for (String param : applet.getAllParams().keySet()) {
                h5Uri += "?" + param + "=" + applet.getAllParams().get(param);
            }
        }
        Intent intent = new Intent();
        intent.setClass(context, H5AppletActivity.class);
        intent.putExtra("url", h5Uri);
        String runTime = applet.getRuntime(H5RunType.RUNTIME_KEY);
        if (null == runTime) {
            runTime = H5RunType.MOBILE_RUNTYPE;
        }
        intent.putExtra(H5RunType.RUNTIME_KEY, runTime);

        String runTime_nav = applet.getRuntime(RUNTIME_NAV_KEY);
        if (null == runTime_nav) {
            runTime_nav = RUNTIME_NAV_FLOAT;
            intent.putExtra(RUNTIME_NAV_KEY, runTime_nav);
        }
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        boolean needAnim = MOBILE_RUNTYPE_ENUM.toString().equals(runTime) && !RUNTIME_NAV_FLOAT.equalsIgnoreCase(runTime_nav);
        intent = AppletActivity.appendApplet(intent, applet);
        if (needAnim) {
            context.startActivity(intent, ActivityOptions.makeCustomAnimation(context, swaiotos.runtime.base.R.anim.applet_launch, swaiotos.runtime.base.R.anim.applet_exit).toBundle());
        } else {
            context.startActivity(intent);
        }
    }

    private H5CoreOS h5core;

    private static boolean isWebViewInit = false;

    @RequiresApi(api = 28)
    public void webviewSetPath(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = getProcessName(context);
            String packageName = context.getPackageName();
            if (!packageName.equals(processName)) {//判断不等于默认进程名称
                WebView.setDataDirectorySuffix(processName);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSetLeftBtnEvent(SetLeftBtnEvent event) {
        LogUtil.d(" onSetLeftBtnEvent type: " + event.getTypeString());
        if (mHeaderHandler == null) {
            LogUtil.d("mHeaderHandler == null is on tv?");
            return;
        }
        if ("search".equalsIgnoreCase(event.getTypeString())) {
            LogUtil.d(" onSetLeftBtnEvent type set search");
            mHeaderHandler.setBackButtonIcon(getResources().getDrawable(R.drawable.search));
            mHeaderHandler.setBackButtonVisible(true);
        } else if ("back".equalsIgnoreCase(event.getTypeString())) {
            mHeaderHandler.setBackButtonIcon(null);
            mHeaderHandler.setBackButtonVisible(true);
        } else {
            mHeaderHandler.setBackButtonVisible(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSetNativeUI(SetNativeUI event) {
        LogUtil.d(" onSetNativeUI data: " + event.getData().toString());
        if (mHeaderHandler == null) {
            LogUtil.d("mHeaderHandler == null is on tv?");
            return;
        }
        try {
            JSONObject obj = (JSONObject) event.getData();
            Object tmp = obj.get("titleText");
            if (tmp != null) {
                mHeaderHandler.setTitle((CharSequence) tmp);
            }
            tmp = obj.get("headerBgColor");
            if (tmp != null) {
                mHeaderHandler.setBackgroundColor(Color.parseColor((String) tmp));
            }
            tmp = obj.get("leftBtnVisible");
            if (tmp != null) {
                mHeaderHandler.setBackButtonVisible((Boolean) tmp);
            }
            tmp = obj.get("leftBtnType");
            if (tmp != null) {
                if ("search".equalsIgnoreCase((String) tmp)) {
                    LogUtil.d(" onSetLeftBtnEvent type set search");
                    mHeaderHandler.setBackButtonIcon(getResources().getDrawable(R.drawable.search));
                    mHeaderHandler.setBackButtonVisible(true);
                } else if ("back".equalsIgnoreCase((String) tmp)) {
                    mHeaderHandler.setBackButtonIcon(null);
                    mHeaderHandler.setBackButtonVisible(true);
                } else {
                    mHeaderHandler.setBackButtonVisible(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSetHeaderColorEvent(SetHeaderColorEvent event) {
        LogUtil.d(" SetHeaderColorEvent type: " + event.getTypeString());
        if (mHeaderHandler == null) {
            LogUtil.d("mHeaderHandler == null is on tv?");
            return;
        }
        String color = event.getTypeString();
        if (color == null) {
            LogUtil.d("color == null?");
            return;
        }
        mHeaderHandler.setBackgroundColor(Color.parseColor(color));
    }

    private String getProcessName(Context context) {
        if (context == null) return null;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == android.os.Process.myPid()) {
                return processInfo.processName;
            }
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!SmartApi.isMobileRuntime())//TV端锁定屏保
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 21) {
//            getWindow().setStatusBarColor(Color.parseColor("#33000000"));
        }
        Bundle extra = getIntent().getExtras();
        LogUtil.d(" onCreate h5 runtime extra:" + extra);
        runTime = getIntent().getStringExtra(H5RunType.RUNTIME_KEY);
        runTime_nav = getIntent().getStringExtra(RUNTIME_NAV_KEY);
        String eventString = getIntent().getStringExtra("event");
        SsePushBean pushBean = null;
        if (eventString != null) {
            pushBean = JSON.parseObject(eventString, SsePushBean.class);
        }
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (!isWebViewInit) {
                webviewSetPath(this);
                isWebViewInit = true;
            }
        }

        //提前初始化open
        H5ChannelInstance.getSingleton().getSSChannel(getApplicationContext());

        h5core = new H5CoreOS(H5RunType.RunType.fromString(runTime), pushBean);

        Map<String, H5CoreExt> extension = new HashMap<>();
        extension.put(RuntimeExt.NAME, RuntimeExt.get(getApplicationContext()));
        extension.put(AccountExt.NAME, AccountExt.get(getApplicationContext()));
        extension.put(PaymentExt.NAME, PaymentExt.get(getApplicationContext()));
        extension.put(ChannelExt.NAME, ChannelExt.get(getApplicationContext()));
        extension.put(ShareExt.NAME, ShareExt.get(getApplicationContext()));
        extension.put(DeviceExt.NAME, DeviceExt.get(getApplicationContext()));

        View view = h5core.create(this, extension);
        setContentView(view);
        load();
        if (mHeaderHandler != null) {
            // fix tv header handler
            mHeaderHandler.setBackButtonOnClickListener(new Runnable() {
                @Override
                public void run() {
                    h5core.onLeftBtnClick();
                }
            });
        }
        EventBus.getDefault().register(this);
    }

    class H5TVAppletLayoutBuilder implements LayoutBuilder {

        @Override
        public View build(View content) {
            return content;
        }
    }

    class H5MobileFloatAppletLayoutBuilder implements LayoutBuilder {
        private Context mContext;

        public H5MobileFloatAppletLayoutBuilder(Context context) {
            this.mContext = context;
        }

        @Override
        public View build(View content) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            FrameLayout root = (FrameLayout) inflater.inflate(R.layout.h5_float_nav_layout, null);
            View nav = root.findViewById(R.id.nav);
            nav.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtil.d("close onClick() called with: v = [" + v + "]");
                    finish();
                }
            });
            nav.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtil.d("more onClick() called with: v = [" + v + "]");
                    share();
                }
            });
            root.addView(content, 0, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return root;
        }
    }

    @Override
    protected AppletActivity.LayoutBuilder createLayoutBuilder() {
        if (MOBILE_RUNTYPE_ENUM.toString().equals(runTime)) {
            LogUtil.d("equal MOBILE_RUNTYPE_ENUM ");
            if (RUNTIME_NAV_FLOAT.equalsIgnoreCase(runTime_nav)) {
                LogUtil.d("equal RUNTIME_NAV_FLOAT ");
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                return new H5MobileFloatAppletLayoutBuilder(this);
            } else {
                LogUtil.d("equal RUNTIME_NAV_TOP ");
                return super.createLayoutBuilder();
            }
        } else {
            LogUtil.d("equal TV_RUNTYPE_ENUM ");
            return new H5TVAppletLayoutBuilder();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LogUtil.d(" onNewIntent h5 runtime");
        super.onNewIntent(intent);
        setIntent(intent);
        load();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    protected void callOnStop() {
        if (h5core != null) {
            h5core.destroy();
        }
        if (!isFinishing()) {
            finish();
        }
    }

    @Override
    protected void back() {
        super.back();
    }

    @Override
    protected void exit() {
        finish();
        overridePendingTransition(0, swaiotos.runtime.base.R.anim.applet_exit);
    }

    private void load() {
        Intent intent = getIntent();
        String url = null;
        if (intent != null) {
            url = intent.getStringExtra("url");
        }
        LogUtil.d("url = " + url);
        String eventString = getIntent().getStringExtra("event");
        if (eventString != null) {
            SsePushBean pushBean = JSON.parseObject(eventString, SsePushBean.class);
            ((H5CoreOS) h5core).updateSsePushBean(pushBean);
        }
        h5core.load(url);
    }

    @Override
    public void onBackPressed() {
        if (!h5core.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        h5core.destroy();
        super.onDestroy();
    }

    @Override
    protected boolean needFitsSystemWindows() {
        return true;
    }
}
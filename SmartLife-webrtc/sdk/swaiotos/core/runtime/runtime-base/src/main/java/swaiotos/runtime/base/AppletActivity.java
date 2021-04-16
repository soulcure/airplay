package swaiotos.runtime.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.transition.Slide;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.coocaa.smartsdk.SmartApi;
import com.coocaa.smartsdk.object.ISmartDeviceInfo;
import com.coocaa.smartsdk.object.IUserInfo;

import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import swaiotos.runtime.Applet;
import swaiotos.runtime.base.style.AppletTitleStyle;
import swaiotos.share.api.ShareApi;
import swaiotos.share.api.define.ShareObject;
import swaiotos.share.api.define.ShareType;

/**
 * @ClassName: AppletActivity
 * @Author: lu
 * @CreateDate: 2020/10/24 7:49 PM
 * @Description:
 */
public abstract class AppletActivity extends AppCompatActivity {
    public interface LayoutBuilder {
        View build(View content);
    }

    public interface HeaderHandler {
        void setCustomHeaderLeftView(View view);

        void setHeaderVisible(boolean visible);

        void setBackButtonIcon(Drawable drawable);

        void setBackButtonVisible(boolean visible);

        void setBackButtonOnClickListener(Runnable callback);

        void setTitle(CharSequence title);

        void setTitle(CharSequence title, CharSequence subTitle);

        void setShareButtonOnClickListener(Runnable callback);

        void setExitButtonOnClickListener(Runnable callback);

        void setBackgroundColor(int color);

        void setDarkMode(boolean darkmode);

        void setTitleAlpha(float alpha);

        void setNavigationBarColor(int color);

        void setTitleStyle(AppletTitleStyle style);

        void setExitButtonVisible(boolean visible);
    }


    protected static final String APPLET_EXTRA = "swaiotos.applet";
    protected static final String APPLET_KEY = "applet";

    protected static Vibrator vibrator;
    protected static long VIBRATE_DURATION = 100L;

    public static Intent appendApplet(Intent intent, Applet applet) {
        String applet1 = Applet.Builder.decode(applet);
        Bundle bundle = new Bundle();
        bundle.putString(APPLET_KEY, applet1);
        intent.putExtra(APPLET_EXTRA, bundle);
        return intent;
    }

    private FrameLayout contentView;

    protected Applet mApplet;
    protected LayoutBuilder mLayoutBuilder;
    protected HeaderHandler mHeaderHandler;
    private boolean needFitsSystemWindows;
    private String TAG = "Applet";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        fullScreenActivity();
        mLayoutBuilder = createLayoutBuilder();
        initAnimation();
        contentView = new FrameLayout(this);
        View rootView = mLayoutBuilder.build(contentView);

        needFitsSystemWindows = needFitsSystemWindows();

        if (mLayoutBuilder instanceof HeaderHandler) {
            mHeaderHandler = (HeaderHandler) mLayoutBuilder;
            mHeaderHandler.setBackButtonOnClickListener(new Runnable() {
                @Override
                public void run() {
                    playVibrator();
                    back();
                }
            });
            mHeaderHandler.setExitButtonOnClickListener(new Runnable() {
                @Override
                public void run() {
                    playVibrator();
                    exit();
                }
            });
            mHeaderHandler.setShareButtonOnClickListener(new Runnable() {
                @Override
                public void run() {
                    playVibrator();
                    share();
                }
            });
        }
        super.setContentView(rootView);
        initApplet();
        if(mApplet != null && !TextUtils.isEmpty(mApplet.getName())) {
            if (mLayoutBuilder instanceof HeaderHandler) {
                ((HeaderHandler) mLayoutBuilder).setTitle(mApplet.getName());
            }
        }
        fitSystemWindow(rootView);

        if(vibrator == null) {
            vibrator = (Vibrator) this.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        }
        submitLog("applet_launch");
    }

    protected void back() {
    }

    protected void exit() {
    }

    private void initAnimation() {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(new Slide().setDuration(2000));
            getWindow().setExitTransition(new Slide().setDuration(2000));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initApplet();
    }

    private void initApplet() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getBundleExtra(APPLET_EXTRA);
            if (bundle != null) {
                String applet = bundle.getString(APPLET_KEY);
                if (!TextUtils.isEmpty(applet)) {
                    Applet applet1 = Applet.Builder.parse(Uri.parse(applet));
                    setApplet(applet1);
                }
            }
        }
    }

    protected LayoutBuilder createLayoutBuilder() {
        return new AppletLayoutBuilder(this, needFitsSystemWindows());
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if (mHeaderHandler != null) {
            mHeaderHandler.setTitle(title.toString());
        }
//        titleView.setText(title);
    }

    public void setTitle(CharSequence title, CharSequence subTitle) {
        super.setTitle(title);
        if (mHeaderHandler != null) {
            mHeaderHandler.setTitle(title, subTitle);
        }
//        titleView.setText(title);
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
        if (mHeaderHandler != null) {
            mHeaderHandler.setTitle(getResources().getString(titleId));
        }
//        titleView.setText(titleId);
    }

    @Override
    public final void setContentView(int layoutResID) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                contentView.removeAllViews();
                contentView.addView(LayoutInflater.from(AppletActivity.this).inflate(layoutResID, null));
            }
        });
    }

    @Override
    public final void setContentView(View view) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                contentView.removeAllViews();
                contentView.addView(view);
            }
        });
    }

    @Override
    public final void setContentView(View view, ViewGroup.LayoutParams params) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                contentView.removeAllViews();
                contentView.addView(view, params);
            }
        });
    }

    @Override
    public final void addContentView(View view, ViewGroup.LayoutParams params) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                contentView.addView(view, params);
            }
        });
    }

    protected void setBackButtonVisible(boolean visiable) {
        if (mHeaderHandler != null) {
            mHeaderHandler.setBackButtonVisible(visiable);
        }
    }

    protected void setHeaderVisible(boolean visiable) {
        if (mHeaderHandler != null) {
            mHeaderHandler.setHeaderVisible(visiable);
        }
    }

    protected void setCustomHeaderLeftView(View view) {
        if (mHeaderHandler != null) {
            mHeaderHandler.setCustomHeaderLeftView(view);
        }
    }

    @Override
    public void finish() {
        submitLog("applet_exit");
        super.finish();
    }

    protected final void setApplet(Applet applet) {
        this.mApplet = applet;
    }

    public Applet getApplet() {
        return mApplet;
    }

    public void share() {
        share(null, null);
    }

    public void share(Map<String, String> params) {
        share(params, null);
    }

    public void share(Map<String, String> params, Map<String, String> runtime) {
        AppletThread.IO(new Runnable() {
            @Override
            public void run() {
                ShareObject shareObject = new ShareObject();
                shareObject.type = ShareType.WEB.toString();
                if(mApplet != null) {
                    shareObject.from = Applet.Builder.decode(mApplet);
                    if(mApplet.getIcon() != null) {
                        shareObject.thumb = mApplet.getIcon();
                    }
                    if(mApplet.getName() != null) {
                        shareObject.title = mApplet.getName();
                        shareObject.text = mApplet.getName();
                    }
                    shareObject.version = String.valueOf(mApplet.getVersion());
                }
                appendShareObject(shareObject);

                ShareApi.share(AppletActivity.this, shareObject, params);
            }
        });

//        String server = "http://tvpi.coocaa.com/swaiot/index.html";
//        Uri uri = Uri.parse(server);
//        Uri.Builder builder = uri.buildUpon();
//        builder.appendQueryParameter("mode", "4");
//
//        if (mApplet == null)
//            return;
//
//        Applet applet = new Applet(
//                mApplet.getType(),
//                mApplet.getId(),
//                mApplet.getTarget(),
//                mApplet.getName(),
//                mApplet.getIcon(),
//                mApplet.getVersion(),
//                runtime != null ? runtime : mApplet.getAllRuntime(),
//                params != null ? params : mApplet.getAllParams()
//        );
//
//        builder.appendQueryParameter("applet", Applet.Builder.parse(applet).toString());
//        String shareUrl = builder.toString();
//
//        ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//        ClipData mClipData = ClipData.newPlainText("分享URL", shareUrl);
//        cmb.setPrimaryClip(mClipData);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(AppletActivity.this, "Applet分享URL已复制到粘贴板（测试）", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    protected void appendShareObject(ShareObject shareObject) {

    }

    private void fitSystemWindow(View rootView) {
        if(needFitsSystemWindows && mLayoutBuilder instanceof AppletLayoutBuilder) {
            rootView.setFitsSystemWindows(true);
            Log.d(TAG, "setFitSystemWindows true");
            rootView.post(new Runnable() {
                @Override
                public void run() {
                    if(rootView.getPaddingTop() > 0) {//解决部分手机高的问题
                        ((AppletLayoutBuilder) mLayoutBuilder).changeHeaderHeight((int) getResources().getDimension(R.dimen.runtime_title_content_height));
                    }
                }
            });
        }
    }

    protected void fullScreenActivity() {
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
//        if (!needFitsSystemWindows()) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        }
////        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        } else {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//        }
        StatusBarHelper.translucent(getWindow());
        StatusBarHelper.setStatusBarLightMode(this);
    }

    protected boolean needFitsSystemWindows() {
        return false;
    }

    @SuppressLint("MissingPermission")
    public void playVibrator() {
//        if(vibrator != null) {
//            vibrator.vibrate(VIBRATE_DURATION);
//        }
    }

    private static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "酷开智屏";
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config,res.getDisplayMetrics() );
        return res;
    }

    private void submitLog(String name) {
        Map<String, String> logParams = new HashMap<>();
        ISmartDeviceInfo deviceInfo = SmartApi.getConnectDeviceInfo();
        if(deviceInfo != null) {
            logParams.put("ss_device_id", deviceInfo.lsid == null ? "" : deviceInfo.lsid);
            logParams.put("ss_device_type", deviceInfo.zpRegisterType == null ? "" : deviceInfo.zpRegisterType);
        }
        IUserInfo userInfo = SmartApi.getUserInfo();
        if(userInfo != null) {
            logParams.put("account", userInfo.open_id == null ? "" : userInfo.open_id);
        }
        logParams.put("applet_id", mApplet == null ? "" : mApplet.getId());
        logParams.put("applet_name", mApplet == null ? "" : mApplet.getName());
        SmartApi.submitLog(name, logParams);
    }
}

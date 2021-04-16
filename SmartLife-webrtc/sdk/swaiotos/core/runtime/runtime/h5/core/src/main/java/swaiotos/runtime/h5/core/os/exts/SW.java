package swaiotos.runtime.h5.core.os.exts;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import java.util.Map;

import swaiotos.runtime.base.AppletActivity;
import swaiotos.runtime.base.utils.ToastUtils;
import swaiotos.runtime.h5.H5CoreExt;
import swaiotos.runtime.h5.H5Style;
import swaiotos.runtime.h5.core.os.exts.channel.ChannelExt;
import swaiotos.runtime.h5.core.os.exts.system.SystemExt;

/**
 * @ClassName: C
 * @Author: lu
 * @CreateDate: 11/18/20 5:01 PM
 * @Description:
 */
public class SW extends H5CoreExt {
    public static final String NAME = "sw";

    public static synchronized SW get(Context context) {
        return new SW(context);
    }

    private final static String TAG = "H5SW";

    private Context context;
    private LifecycleExts lifecycleExts;
    private Map<String, String> runtime;
    private H5Style style;
    private Context activityContext;

    public SW(Context context) {
        super();
        this.context = context;
        lifecycleExts = new LifecycleExts();
        setContext(context);
        ToastUtils.getInstance().init(context.getApplicationContext());
    }

    public SW setHeaderHandler(AppletActivity.HeaderHandler hh) {
        this.lifecycleExts.setHeaderHandler(hh);
        return this;
    }

    public SW setRuntime(Map<String, String> runtime) {
        this.runtime = runtime;
        return this;
    }

    public SW setH5Style(H5Style style) {
        this.style = style;
        return this;
    }

    @Override
    public void attach(Context context) {
        activityContext = context;
        lifecycleExts.attach(context);
    }

    @Override
    public void detach(Context context) {
        super.detach(context);
        activityContext = null;
    }

    public void onResume() {
        lifecycleExts.onResume();
    }

    public void onPause() {
        lifecycleExts.onPause();
    }

    public void onStop() {
        lifecycleExts.onStop();
    }

    public void destroy(Context context) {
        GlobalExts.destroy(context);
        lifecycleExts.destroy(context);
    }

    public void onControlBarVisibleChanged(boolean b) {
        lifecycleExts.onControlBarVisibleChanged(b);
    }

    @JavascriptInterface
    public H5CoreExt require(String module) {
        Log.d(TAG, "require module:" + module);
        H5CoreExt ret = GlobalExts.require(module, context);
        if(ret == null) {
            ret = lifecycleExts.require(module, activityContext);
        }
        if(ret != null) {
            if(ret.getWebView() != getWebView()) {
                ret.setWebView(getWebView()); //update webview.
            }
            onExtInited(ret);
        }
        return ret;
    }

    private void onExtInited(H5CoreExt ext) {
        if(runtime != null && (ext instanceof ChannelExt)) {
            ((ChannelExt) ext).setNetworkForceKey(runtime.get("RUNTIME_NETWORK_FORCE_KEY"));
        }
        if(ext instanceof SystemExt) {
            ((SystemExt) ext).setH5Style(style);
        }
    }

    @JavascriptInterface
    public void log(String tag, String message) {
        Log.d(tag, message);
    }
}

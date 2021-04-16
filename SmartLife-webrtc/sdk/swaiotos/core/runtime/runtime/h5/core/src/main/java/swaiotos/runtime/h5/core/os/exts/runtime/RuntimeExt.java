package swaiotos.runtime.h5.core.os.exts.runtime;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;

import swaiotos.runtime.h5.H5CoreExt;

/**
 * @ClassName: BuildExt
 * @Author: lu
 * @CreateDate: 11/18/20 2:26 PM
 * @Description:
 */
public class RuntimeExt extends H5CoreExt {
    public static final String NAME = "runtime";

    private static H5CoreExt ext = null;

    public static synchronized H5CoreExt get(Context context) {
        if (ext == null) {
            ext = new RuntimeExt();
        }
        return ext;
    }

    @JavascriptInterface
    public String platform() {
        return Build.HARDWARE;
    }

    @JavascriptInterface
    public String version() {
        return "1.0";
    }

    @JavascriptInterface
    public int level() {
        return 1;
    }

    @JavascriptInterface
    public void call(Object object) {
        Log.d("h5ext", "call:" + object);
    }
}

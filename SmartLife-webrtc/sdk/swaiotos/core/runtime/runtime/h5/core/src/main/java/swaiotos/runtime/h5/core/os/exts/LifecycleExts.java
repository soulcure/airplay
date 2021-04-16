package swaiotos.runtime.h5.core.os.exts;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import swaiotos.runtime.base.AppletActivity;
import swaiotos.runtime.h5.H5CoreExt;
import swaiotos.runtime.h5.core.os.exts.channel.ChannelExt;
import swaiotos.runtime.h5.core.os.exts.navigator.NavigatorExt;
import swaiotos.runtime.h5.core.os.exts.payment.PaymentExt;
import swaiotos.runtime.h5.core.os.exts.system.SystemExt;

/**
 * @Author: yuzhan
 */
public class LifecycleExts {

    private static final Map<String, Class<? extends H5CoreExt>> EXTS = new HashMap<>();
    private final Map<String, H5CoreExt> extsImpl = new HashMap<>();
    private AppletActivity.HeaderHandler headerHandler;
    private volatile boolean attach = true;

    private final static String TAG = "H5SW";

    static {
        EXTS.put(ChannelExt.NAME, ChannelExt.class);
        EXTS.put(PaymentExt.NAME, PaymentExt.class);
        EXTS.put(NavigatorExt.NAME, NavigatorExt.class);
        EXTS.put(SystemExt.NAME, SystemExt.class);
    }

    public void setHeaderHandler(AppletActivity.HeaderHandler hh) {
        this.headerHandler = hh;
    }

    public void attach(Context context) {
        attach = true;
        Iterator<H5CoreExt> iterator = extsImpl.values().iterator();
        while(iterator.hasNext()) {
            H5CoreExt ext = iterator.next();
            ext.attach(context);
        }
    }

    public void onResume() {
        Iterator<H5CoreExt> iterator = extsImpl.values().iterator();
        while(iterator.hasNext()) {
            H5CoreExt ext = iterator.next();
            ext.onResume();
        }
    }

    public void onPause() {
        Iterator<H5CoreExt> iterator = extsImpl.values().iterator();
        while(iterator.hasNext()) {
            H5CoreExt ext = iterator.next();
            ext.onPause();
        }
    }

    public void onStop() {
        Iterator<H5CoreExt> iterator = extsImpl.values().iterator();
        while(iterator.hasNext()) {
            H5CoreExt ext = iterator.next();
            ext.onStop();
        }
    }

    public void destroy(Context context) {
        attach = false;
        Iterator<H5CoreExt> iterator = extsImpl.values().iterator();
        while(iterator.hasNext()) {
            H5CoreExt ext = iterator.next();
            ext.detach(context);
        }
    }

    public void onControlBarVisibleChanged(boolean b) {
        Iterator<H5CoreExt> iterator = extsImpl.values().iterator();
        while(iterator.hasNext()) {
            H5CoreExt ext = iterator.next();
            if(ext instanceof NavigatorExt) {
                ((NavigatorExt) ext).onControlBarVisibleChanged(b);
            }
        }
    }

    public H5CoreExt require(String module, Context context) {
        H5CoreExt cacheExt = extsImpl.get(module);
        if (cacheExt != null) {
            return cacheExt;
        }

        Class<? extends H5CoreExt> clazz = EXTS.get(module);
        if (clazz != null) {
            H5CoreExt ext = null;
            try {
                ext = Reflector.on(clazz).method("get", Context.class).call(context);
                extsImpl.put(module, ext);
                onExtInited(ext, context);
                Log.d(TAG, "require lifecycle module:" + module + " obj:" + ext);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ext;
        }
        return null;
    }

    private void onExtInited(H5CoreExt ext, Context context) {
        if(ext instanceof NavigatorExt) {
            ((NavigatorExt) ext).setHeaderHandler(headerHandler);
        }
        if(attach) {
            ext.attach(context);
        }
    }
}

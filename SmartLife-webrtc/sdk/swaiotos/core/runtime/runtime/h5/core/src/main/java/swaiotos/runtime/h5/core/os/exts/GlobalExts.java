package swaiotos.runtime.h5.core.os.exts;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import swaiotos.runtime.h5.H5CoreExt;
import swaiotos.runtime.h5.core.os.exts.account.AccountExt;
import swaiotos.runtime.h5.core.os.exts.device.DeviceExt;
import swaiotos.runtime.h5.core.os.exts.runtime.RuntimeExt;
import swaiotos.runtime.h5.core.os.exts.share.ShareExt;
import swaiotos.runtime.h5.core.os.exts.storage.StorageExt;
import swaiotos.runtime.h5.core.os.exts.websocket.WebSocketExt;

/**
 * @Author: yuzhan
 */
public class GlobalExts {

    private static final Map<String, Class<? extends H5CoreExt>> EXTS = new HashMap<>();
    private static final Map<String, H5CoreExt> extsImpl = new HashMap<>();

    private final static String TAG = "H5SW";

    static {
        EXTS.put(RuntimeExt.NAME, RuntimeExt.class);
        EXTS.put(AccountExt.NAME, AccountExt.class);
        EXTS.put(ShareExt.NAME, ShareExt.class);
        EXTS.put(WebSocketExt.NAME, WebSocketExt.class);
        EXTS.put(DeviceExt.NAME, DeviceExt.class);
        EXTS.put(StorageExt.NAME, StorageExt.class);
    }

    public static void destroy(Context context) {
        Iterator<H5CoreExt> iterator = extsImpl.values().iterator();
        while(iterator.hasNext()) {
            H5CoreExt ext = iterator.next();
            ext.detach(context);
        }
    }

    public static H5CoreExt require(String module, Context context) {
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
                Log.d(TAG, "require module:" + module + " obj:" + ext);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ext;
        }
        return null;
    }
}

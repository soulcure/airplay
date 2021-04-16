package swaiotos.runtime.h5.core.os.exts.share;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import swaiotos.runtime.h5.H5CoreExt;
import swaiotos.runtime.h5.core.os.exts.utils.ExtLog;
import swaiotos.share.api.ShareApi;

/**
 * @ClassName: AccountExt
 * @Author: lu
 * @CreateDate: 11/18/20 2:33 PM
 * @Description:
 */
public class ShareExt extends H5CoreExt {
    public static final String NAME = "share";

    private static final String TAG = "ShareExt";
    private static H5CoreExt ext = null;
    private static WeakReference<Context> contextWeakRef;

    public static synchronized H5CoreExt get(Context context) {
        if (ext == null) {
            ext = new ShareExt();
            contextWeakRef = new WeakReference<>(context);
        }
        return ext;
    }


    @JavascriptInterface
    public void requestShare(String id, String type, String title, String desc, String link, String imgUrl, String from, String appletVersion) {
        ExtLog.d(TAG, "requestShare(), id: " + id + ", type = " + type + ", title = " + title +
                ", desc = " + desc + ", link = " + link + ", imgUrl = " + imgUrl + ", from = " + from +
                ", appletVersion = " + appletVersion);
        Map<String, String> shareData = new HashMap<>();
        shareData.put("type", type);
        shareData.put("title", title);
        shareData.put("desc", desc);
        shareData.put("link", link);
        shareData.put("imgUrl", imgUrl);
        shareData.put("from", from);
        shareData.put("appletVersion", appletVersion);
        boolean result = contextWeakRef != null && contextWeakRef.get() != null;
        if (result) {
            ShareApi.share(contextWeakRef.get(), shareData);
        } else {
            ExtLog.w(TAG, "requestShare(), result: " + result);
        }

        JSONObject params = new JSONObject();
        params.put("result", String.valueOf(result));
        String name = result ? "success" : "fail";
        native2js(id, name, params.toString());

//        native2js(id, "success", params.toJSONString());
    }

}

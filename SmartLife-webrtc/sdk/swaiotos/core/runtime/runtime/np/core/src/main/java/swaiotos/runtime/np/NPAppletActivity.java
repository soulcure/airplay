package swaiotos.runtime.np;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;

import java.util.Map;

import swaiotos.runtime.Applet;
import swaiotos.runtime.base.AppletActivity;
import swaiotos.runtime.base.FloatAppletLayoutBuilder;
import swaiotos.share.api.define.ShareObject;

/**
 * <data
 * android:host="com.coocaa.smart.localpicture"
 * android:pathPattern="/index"
 * android:scheme="np" />
 * <p>
 * 同一个 NPApplet ID的小程序Activity，必须运行在同一个进程
 **/
public abstract class NPAppletActivity extends AppletActivity {

    public static class NPAppletInfo {
        public final String id;
        public final String target;

        public NPAppletInfo(String id, String target) {
            this.id = id;
            this.target = target;
        }
    }

    protected NPAppletInfo mNPAppletInfo;

    private String TAG = "NPApplet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNPAppletInfo = getNPAppletInfo();
        Log.d(TAG, "mNPAppletInfo=" + mNPAppletInfo);
        if (mNPAppletInfo != null) {
            setHeaderVisible(true);
            NPAppletActivityStackManager.manager.push(mNPAppletInfo.id, this);
            if (mNPAppletInfo.target.equals("/index")) {
                setBackButtonVisible(false);
            } else {
                setBackButtonVisible(true);
            }
        } else {
            setHeaderVisible(false);
        }
    }

    @Override
    protected void onDestroy() {
        if (mNPAppletInfo != null) {
            NPAppletActivityStackManager.manager.pop(mNPAppletInfo.id, this);
        }
        super.onDestroy();
    }

    protected void back() {
        finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mNPAppletInfo != null) {
            if(NPAppletActivityStackManager.manager.size(mNPAppletInfo.id) == 1) {
                overridePendingTransition(0, swaiotos.runtime.base.R.anim.applet_exit);
            }
        }
    }

    @Override
    protected void exit() {
        if (mNPAppletInfo != null) {
            NPAppletActivityStackManager.manager.exit(mNPAppletInfo.id);
        } else {
            finish();
        }
    }

    protected NPAppletInfo getNPAppletInfo() {
        if (mApplet != null) {
            return new NPAppletInfo(mApplet.getId(), mApplet.getTarget());
        }
        return null;
    }

    @Override
    protected LayoutBuilder createLayoutBuilder() {
        if(isFloatHeader()) {
            return new FloatAppletLayoutBuilder(this);
        } else {
            return super.createLayoutBuilder();
        }
    }

    protected boolean isFloatHeader() {
        return false;
    }

    @Override
    public void startActivity(Intent intent) {
        if(mNPAppletInfo != null && intent.getComponent() != null && intent.getData() == null && mApplet != null) {
            //减少已有页面改动量，添加支持内部跳转页面的管理
            Class clazz = null;
            try {
                clazz = Class.forName(intent.getComponent().getClassName());
                if(NPAppletActivity.class.isAssignableFrom(clazz)) {
                    Bundle bundle = new Bundle();
                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme(mApplet.getType());
                    builder.authority(mApplet.getId());
                    builder.path("sub_page_" + getClass().getSimpleName());
                    if(!TextUtils.isEmpty(mApplet.getId())) {
                        builder.appendQueryParameter("icon", mApplet.getId());
                    }
                    if(!TextUtils.isEmpty(mApplet.getName())) {
                        builder.appendQueryParameter("name", mApplet.getName());
                    }
                    if(mApplet.getAllRuntime() != null) {
                        builder.appendQueryParameter("runtime", JSON.toJSONString(mApplet.getAllRuntime()));
                    }
                    String uri = builder.build().toString();
                    bundle.putString(APPLET_KEY, uri);
                    intent.putExtra(APPLET_EXTRA, bundle);
                    Log.d(TAG, "append applet info to normal intent with uri : " + uri);
                }
            } catch (Exception e) {
                Log.d(TAG, "try to append applet info to normal intent fail : " + e.toString());
                e.printStackTrace();
            }
        }
        super.startActivity(intent);
    }

    @Override
    protected void appendShareObject(ShareObject shareObject) {
        if(shareObject != null) {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(mApplet.getType());
            builder.authority(mApplet.getId());
            builder.path("index");
            if(mApplet.getAllParams() != null && !mApplet.getAllParams().isEmpty()) {
                builder.appendQueryParameter("params", JSON.toJSONString(mApplet.getAllParams()));
            }
            try {
                Applet applet = NPAppletActivityStackManager.manager.getRootApplet(mApplet.getId());
                Map<String, String> params = applet.getAllParams();
                if(params != null && !params.isEmpty()) {
                    Log.d(TAG, "appendShareObject, params=" + params);
                    builder.appendQueryParameter("params", JSON.toJSONString(params));
                }
            } catch (Exception e) {

            }
            shareObject.from = builder.build().toString();
            Log.d(TAG, "appendShareObject, from=" + shareObject.from);
        }
    }

    protected String getNetworkForceKey() {
        if(mApplet != null && mApplet.getRuntime("RUNTIME_NETWORK_FORCE_KEY") != null) {
            return mApplet.getRuntime("RUNTIME_NETWORK_FORCE_KEY");
        }
        return null;
    }
}
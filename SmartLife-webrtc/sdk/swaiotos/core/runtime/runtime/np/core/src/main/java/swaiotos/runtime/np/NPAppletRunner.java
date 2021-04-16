package swaiotos.runtime.np;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import swaiotos.runtime.Applet;
import swaiotos.runtime.AppletRuntimeManager;
import swaiotos.runtime.base.AppletActivity;
import swaiotos.runtime.base.AppletRunner;

/**
 * @ClassName: NPAppletRunner
 * @Author: lu
 * @CreateDate: 2020/10/24 8:26 PM
 * @Description:
 */
public class NPAppletRunner implements AppletRunner {
    private static final AppletRunner runner = new NPAppletRunner();

    public static AppletRunner get() {
        return runner;
    }

    private NPAppletRunner() {
        super();
    }

    @Override
    public void start(Context context, Applet applet) throws Exception {
        start(context, applet, null);
    }

    @Override
    public void start(Context context, Applet applet, Bundle bundle) throws Exception {
        Uri data = buildUri(applet);

        Intent intent = new Intent();
        intent.setData(data);
        intent.setPackage(context.getPackageName());

        Map<String, String> params = applet.getAllParams();
        if (params != null) {
            Set<String> keys = params.keySet();
            for (String key : keys) {
                intent.putExtra(key, applet.getParams(key));
            }
        }
        if(bundle != null) {
            bundle.setClassLoader(AppletRuntimeManager.classLoader);
            intent.putExtras(new Bundle(bundle));
        }

        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent = AppletActivity.appendApplet(intent, applet);

        String appletFrom = applet.getRuntime(Applet.RUNTIME_APPLET_FROM);
        if (TextUtils.isEmpty(appletFrom) || !appletFrom.equals(applet.getId())) {
//            context.startActivity(intent);
            context.startActivity(intent, ActivityOptions.makeCustomAnimation(context, swaiotos.runtime.base.R.anim.applet_launch, swaiotos.runtime.base.R.anim.applet_stay).toBundle());
        } else {
//            context.startActivity(intent);
            context.startActivity(intent, ActivityOptions.makeCustomAnimation(context, swaiotos.runtime.base.R.anim.applet_in, swaiotos.runtime.base.R.anim.applet_stay).toBundle());
        }
    }

    private Uri buildUri(Applet applet) {
        String type = applet.getType();
        String id = applet.getId();
        String target = applet.getTarget();

        Uri.Builder builder = new Uri.Builder().scheme(type).authority(id).path(target);

        if(applet.getAllParams() != null) {
            builder.appendQueryParameter(Applet.Builder.APPLET_PARAMS, JSON.toJSONString(applet.getAllParams()));
        }

        if(applet.getAllRuntime() != null) {
            builder.appendQueryParameter(Applet.Builder.APPLET_RUNTIME, JSON.toJSONString(applet.getAllRuntime()));
        }

        if(applet.getIcon() != null) {
            builder.appendQueryParameter(Applet.Builder.APPLET_ICON, JSON.toJSONString(applet.getIcon()));
        }

        if(applet.getName() != null) {
            builder.appendQueryParameter(Applet.Builder.APPLET_NAME, JSON.toJSONString(applet.getName()));
        }

        Map<String, String> extParams = applet.getExtMap();
        if(extParams != null && !extParams.isEmpty()) {
            //添加额外参数
            Iterator<Map.Entry<String, String>> iterator = extParams.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
        }

        return builder.build();
    }
}

package swaiotos.runtime.h5;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.util.Map;

import swaiotos.runtime.Applet;
import swaiotos.runtime.base.AppletActivity;
import swaiotos.runtime.base.AppletRunner;
import swaiotos.runtime.h5.core.os.H5RunType;
import swaiotos.runtime.h5.landscape.LandScapeH5FloatNPAppletActivity;

import static swaiotos.runtime.h5.core.os.H5RunType.RUNTIME_NAV_FLOAT;
import static swaiotos.runtime.h5.core.os.H5RunType.RUNTIME_NAV_FLOAT_NP;
import static swaiotos.runtime.h5.core.os.H5RunType.RUNTIME_NAV_KEY;
import static swaiotos.runtime.h5.core.os.H5RunType.RUNTIME_NAV_TOP;
import static swaiotos.runtime.h5.core.os.H5RunType.RUNTIME_NETWORK_FORCE_KEY;
import static swaiotos.runtime.h5.core.os.H5RunType.RUNTIME_ORIENTATION_KEY;
import static swaiotos.runtime.h5.core.os.H5RunType.RUNTIME_ORIENTATION_LANDSCAPE;
import static swaiotos.runtime.h5.core.os.H5RunType.RunType.MOBILE_RUNTYPE_ENUM;

/**
 * @ClassName: H5AppletRunner
 * @Author: lu
 * @CreateDate: 2020/10/24 8:25 PM
 * @Description:
 */
public class H5AppletRunner implements AppletRunner {

    private static final AppletRunner runner = new H5AppletRunner();

    public static AppletRunner get() {
        return runner;
    }

    private H5AppletRunner() {
        super();
    }

    @Override
    public void start(Context context, Applet applet) throws Exception {
        Log.d("H5AppletRunner", "start() called with: context = [" + context + "], applet = [" + applet + "]");
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(applet.getType()).encodedAuthority(applet.getId()).encodedPath(applet.getTarget());
        Log.d("H5AppletRunner", "pre build url=" + builder.build().toString());

        Map<String, String> paramsMap = applet.getAllParams();
        if (null != paramsMap) {
            for (String key : paramsMap.keySet()) {
                builder.appendQueryParameter(key, paramsMap.get(key));
            }
        }

        String runTime = applet.getRuntime(H5RunType.RUNTIME_KEY);
        if (null == runTime) {
            runTime = H5RunType.MOBILE_RUNTYPE;
        }
        builder.appendQueryParameter(H5RunType.RUNTIME_KEY, runTime);

        String runTime_nav = applet.getRuntime(RUNTIME_NAV_KEY);
        if (null == runTime_nav) {
            if (applet.getTarget().contains("barrage/h5") || applet.getTarget().contains("atmosphere/h5")) {
                runTime_nav = RUNTIME_NAV_TOP;
            } else {
                runTime_nav = RUNTIME_NAV_FLOAT;
            }
        }
        builder.appendQueryParameter(H5RunType.RUNTIME_NAV_KEY, runTime_nav);

        String runTime_NetForce = applet.getRuntime(RUNTIME_NETWORK_FORCE_KEY);
        if(null == runTime_NetForce){
            runTime_NetForce  = H5RunType.RUNTIME_NETWORK_NORMAL;// 默认都支持
        }
        builder.appendQueryParameter(H5RunType.RUNTIME_NETWORK_FORCE_KEY, runTime_NetForce);

        Map<String, String> extMap = applet.getExtMap();
        if(null != extMap) {
            for (String key : extMap.keySet()) {
                builder.appendQueryParameter(key, extMap.get(key));
            }
        }
        if(!TextUtils.isEmpty(applet.getFragment())) {
            builder.fragment(applet.getFragment());
        }
        boolean isLandScape = RUNTIME_ORIENTATION_LANDSCAPE.equals(applet.getRuntime(RUNTIME_ORIENTATION_KEY));
        Log.d("H5AppletRunner", "isLandScape=" + isLandScape);
        start(context, Uri.decode(builder.build().toString()), runTime, runTime_nav, runTime_NetForce, applet, isLandScape);
    }

    @Override
    public void start(Context context, Applet applet, Bundle bundle) throws Exception {
        start(context, applet);
    }

    public void start(Context context, String h5Uri, String runTime, String runTime_nav, String runtTime_netForce, Applet applet, boolean isLandScape) throws Exception {
        Intent intent = new Intent();
        intent.putExtra("url", h5Uri);
        intent.putExtra(H5RunType.RUNTIME_KEY, runTime);
        intent.putExtra(RUNTIME_NAV_KEY, runTime_nav);
        intent.putExtra(RUNTIME_NETWORK_FORCE_KEY,runtTime_netForce);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (MOBILE_RUNTYPE_ENUM.toString().equals(runTime)) {
            if (RUNTIME_NAV_FLOAT.equalsIgnoreCase(runTime_nav)) {
                intent.setClass(context, H5FloatAppletActivity.class);
            } else if (RUNTIME_NAV_FLOAT_NP.equalsIgnoreCase(runTime_nav)) {
                intent.setClass(context, isLandScape ? LandScapeH5FloatNPAppletActivity.class : H5FloatNPAppletActivity.class);
            } else {
                intent.setClass(context, H5NPAppletActivity.class);
            }
        } else {
            intent.setClass(context, H5TV2AppletActivity.class);
        }
        if(applet != null)
            intent = AppletActivity.appendApplet(intent, applet);
        boolean needAnim = TextUtils.equals(H5NPAppletActivity.class.getName(), intent.getComponent() == null ? null : intent.getComponent().getClassName());
        if(!needAnim) {
            needAnim = TextUtils.equals(H5FloatNPAppletActivity.class.getName(), intent.getComponent() == null ? null : intent.getComponent().getClassName());
        }
        if(needAnim) {
            context.startActivity(intent, ActivityOptions.makeCustomAnimation(context, swaiotos.runtime.base.R.anim.applet_launch, swaiotos.runtime.base.R.anim.applet_stay).toBundle());
        } else {
            context.startActivity(intent);
        }
    }
}

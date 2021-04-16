package swaiotos.runtime.mp;

import android.content.Context;
import android.os.Bundle;

import swaiotos.runtime.Applet;
import swaiotos.runtime.base.AppletRunner;

/**
 * @ClassName: MPAppletRunner
 * @Author: lu
 * @CreateDate: 2020/10/24 8:27 PM
 * @Description:
 */
public class MPAppletRunner implements AppletRunner {
    private static final AppletRunner runner = new MPAppletRunner();

    public static AppletRunner get() {
        return runner;
    }

    private MPAppletRunner() {
        super();
    }

    @Override
    public void start(Context context, Applet applet) throws Exception {
        MPAppletLauncherActivity.launch(context, Applet.Builder.parse(applet));
    }

    @Override
    public void start(Context context, Applet applet, Bundle bundle) throws Exception {

    }
}

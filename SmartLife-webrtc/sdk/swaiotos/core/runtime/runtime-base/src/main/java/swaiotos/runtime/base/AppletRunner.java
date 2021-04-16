package swaiotos.runtime.base;

import android.content.Context;
import android.os.Bundle;

import swaiotos.runtime.Applet;

/**
 * @ClassName: AppletRunner
 * @Author: lu
 * @CreateDate: 2020/10/24 8:24 PM
 * @Description:
 */
public interface AppletRunner {
    void start(Context context, Applet applet) throws Exception;
    void start(Context context, Applet applet, Bundle bundle) throws Exception;
}

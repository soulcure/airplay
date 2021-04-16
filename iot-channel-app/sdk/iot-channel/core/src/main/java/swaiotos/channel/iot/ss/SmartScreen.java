package swaiotos.channel.iot.ss;

import android.content.Context;

import swaiotos.channel.iot.ss.session.Session;

/**
 * @ClassName: ISmartScreen
 * @Author: lu
 * @CreateDate: 2020/3/17 5:22 PM
 * @Description:
 */
public interface SmartScreen extends SSContext {
    interface OpenHandler {
        void onOpened(SmartScreen ss);

        void onFailed(String error);
    }

    Session open(Context context, OpenHandler handler);

    int close();
}

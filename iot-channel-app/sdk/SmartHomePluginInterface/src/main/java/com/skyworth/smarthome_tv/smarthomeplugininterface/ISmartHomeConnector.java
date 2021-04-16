package com.skyworth.smarthome_tv.smarthomeplugininterface;

import java.io.Serializable;
import java.util.Map;

/**
 * @ClassName: IConnector
 * @Author: XuZeXiao
 * @CreateDate: 2020/7/9 11:16
 * @Description:
 */
public interface ISmartHomeConnector {
    User user();

    Executor executor();

    Logger logger();

    boolean debugMode();

    int homeVersion();

    Iot iot();

    interface UserChangeListener extends Serializable {
        void onUserChanged();
    }

    interface User extends Serializable {
        class UserInfo implements Serializable {
            public String token;
            public Map<String, Object> info;
        }

        UserInfo getUserInfo();

        boolean hasLogin();

        /**
         * 账户登录
         *
         * @param params
         * @param from   业务来源，特殊日志采集需要，一般不需要该值，传null即可
         * @return
         */
        boolean login(Map<String, String> params, boolean needFinish, String from);

        void addUserChangeListener(UserChangeListener listener);

        void removeUserChangeListener(UserChangeListener listener);
    }

    interface Executor extends java.util.concurrent.Executor, Serializable {
        void execute(Runnable runnable);

        void execute(Runnable runnable, long delay);
    }

    interface Logger extends Serializable {
        void pageResumeEvent(String pageName, Map<String, String> params);

        void pagePausedEvent(String pageName, Map<String, String> params);

        void pageFailEvent(String pageName, String result, int errorCode);

        void pageCustomEvent(String eventId, Map<String, String> params);

        /**
         * 提交日志事件
         *
         * @param eventId
         * @param params
         */
        void baseEvent(String eventId, Map<String, String> params);

        /**
         * 同步提交日志事件
         *
         * @param eventId
         * @param params
         */
        void baseEventSync(String eventId, Map<String, String> params);


        void submitBaseEventWithPolicy(String eventId, Map<String, String> params, int policyTime, int policyMaxLine);
    }

    interface IotListener extends Serializable {
        void onAccessTokenChanged(String accessToken);
    }

    interface Iot extends Serializable {
        String accessToken();

        void addListener(IotListener listener);

        void removeListener(IotListener listener);
    }
}

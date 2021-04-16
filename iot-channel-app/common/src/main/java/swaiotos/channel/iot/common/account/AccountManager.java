package swaiotos.channel.iot.common.account;

import android.content.Context;

import java.util.Map;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 2020/5/28
 */
public class AccountManager {

    public interface IAccManager {
        boolean hasLogin();

        void gotoLogin();

        void logout();

        boolean isBindMobile();

        void gotoBindMobile();

        AccountInfo getAccountInfo();

        String getOpenId();

        String getToken();

        String getSession();

        String getAccountValue(Map<String, ?> info, String key);
    }

    private static IAccManager manager = null;

    public static synchronized IAccManager getManager(Context context) {
        if (manager == null) {
            manager = new AccountManagerImpl(context);
        }
        return manager;
    }

}

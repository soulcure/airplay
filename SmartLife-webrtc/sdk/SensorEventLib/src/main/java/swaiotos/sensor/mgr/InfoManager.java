package swaiotos.sensor.mgr;

import android.content.Context;

import swaiotos.sensor.data.AccountInfo;
import swaiotos.sensor.client.data.ClientBusinessInfo;

/**
 * @Author: yuzhan
 */
public final class InfoManager {

    private AccountInfo accountInfo;
    private ClientBusinessInfo businessInfo;
    private String id;
    private static Context appContext;

    public void setAccountInfo(AccountInfo info) {
        accountInfo = info;
    }

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    public void setId(String _id) {
        id = _id;
    }

    public String getId() {
        return id;
    }

    public void setBusinessInfo(ClientBusinessInfo info) {
        businessInfo = info;
    }

    public ClientBusinessInfo getBusinessInfo() {
        return businessInfo;
    }

    public static void setAppContext(Context c) {
        appContext = c.getApplicationContext();
    }

    public static Context getAppContext() {
        return appContext;
    }
}

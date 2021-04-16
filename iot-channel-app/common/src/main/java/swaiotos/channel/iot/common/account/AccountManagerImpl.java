package swaiotos.channel.iot.common.account;

import android.content.Context;
import android.text.TextUtils;

import com.tianci.user.api.SkyUserApi;
import com.tianci.user.data.AccountUtils;
import com.tianci.user.data.UserCmdDefine;

import java.util.HashMap;
import java.util.Map;

import swaiotos.channel.iot.utils.EmptyUtils;

/**
 * @Description:
 * @Author: wzh
 * @CreateDate: 2020/5/27
 */
public class AccountManagerImpl implements AccountManager.IAccManager {

    private SkyUserApi skyUserApi;
    private Context mContext;

    public AccountManagerImpl(Context context) {
        mContext = context;
        skyUserApi = new SkyUserApi(context);
    }

    /**
     * 是否有账号登录
     *
     * @return
     */
    @Override
    public boolean hasLogin() {
        return skyUserApi.hasLogin();
    }

    /**
     * 跳转登录界面
     *
     * @param
     */
    @Override
    public void gotoLogin() {
        Map<String, String> extra = new HashMap<>();
//        extra.put("themeVersion", themeVersion);
        SkyUserApi.showAccountManager(mContext, true, extra);
    }

    @Override
    public void logout() {
        skyUserApi.logout();
    }

    /**
     * 是否绑定手机号
     *
     * @return
     */
    @Override
    public boolean isBindMobile() {
        try {
            String mobile = getAccountValue(skyUserApi.getAccoutInfo(), "mobile");
            if (!TextUtils.isEmpty(mobile)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 跳转绑定手机号的页面
     */
    @Override
    public void gotoBindMobile() {
        Map<String, String> extra = new HashMap<>();
//        extra.put("themeVersion", themeVersion);
        extra.put("accessToken", getToken());
        SkyUserApi.showAccountManager(mContext, true, extra);
    }

    /**
     * 获取已登录的账号信息
     *
     * @return
     */
    @Override
    public AccountInfo getAccountInfo() {
        AccountInfo info = new AccountInfo();
        Map<String, Object> infoMap = skyUserApi.getAccoutInfo();
        if (EmptyUtils.isEmpty(infoMap)) {
            return info;
        }
        info.address = getAccountValue(infoMap, "address");
        info.birthday = getAccountValue(infoMap, "birthday");
        info.gender = Integer.parseInt(getAccountValue(infoMap, "gender"));
        info.slogan = getAccountValue(infoMap, "slogan");
        info.mobile = getAccountValue(infoMap, "mobile");
        info.avatar = getAccountValue(infoMap, "avatar");
        if (TextUtils.isEmpty(info.avatar)) {
            if (info.gender == 2) {
                info.avatar = "USER_ICON_FEMALE";
            } else {
                info.avatar = "USER_ICON_MALE";
            }
        }
        info.user_id = getAccountValue(infoMap, "open_id");
        info.token = getToken();
        info.nick_name = getAccountValue(infoMap, "nick_name");
        return info;
    }

    /**
     * 获取当前用户的OpenId
     *
     * @return
     */
    @Override
    public String getOpenId() {
        return AccountUtils.getAccountValue(skyUserApi.getAccoutInfo(), UserCmdDefine.UserKeyDefine.KEY_OPEN_ID);
    }

    /**
     * 获取账号相关Token
     *
     * @return
     */
    @Override
    public String getToken() {
        return skyUserApi.getToken("ACCESS");
    }

    /**
     * 获取当前用户session
     *
     * @return
     */
    @Override
    public String getSession() {
        return skyUserApi.getSession();
    }

    /**
     * 解析账号信息的方法
     *
     * @param info getAccountInfo()返回的账号信息
     * @param key  需要获取的字段key
     * @return
     */
    @Override
    public String getAccountValue(Map<String, ?> info, String key) {
        return AccountUtils.getAccountValue(info, key);
    }
}

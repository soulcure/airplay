/**
 * Copyright (C) 2012 The SkyTvOS Project
 * <p>
 * Version     Date           Author ───────────────────────────────────── 2013-11-26 guiqingwen
 */

package com.tianci.user.data;

public class UserCmdDefine {
    /**
     * @author wen
     */
    public enum AccountAction {
        REGISTE,
        BIND,
        FIND_PWD,
    }

    public static class UserKeyDefine {
        public static final String KEY_RESULT_SIGN = "resultSign";
        public static final String KEY_RESULT_STRING = "resultString";
        public static final String KEY_RESULT_LIST = "resultList";
        public static final String KEY_DATE = "keyDate";
        public static final String KEY_RES_ID = "keyResId";

        public static final String KEY_TARGET = "commandTarget";

        // 2015-10-22 add for account info
        public static final String KEY_ACCOUNT_MOBILE = "mobile";
        public static final String KEY_ACCOUNT_NICK = "nick_name";
        public static final String KEY_ACCOUNT_AVATAR = "avatar";
        public static final String KEY_ACCOUNT_THIRD = "third_account";
        public static final String KEY_ACCOUNT_CODE = "code";
        public static final String KEY_OPEN_ID = "open_id";
        public static final String KEY_SKY_ID = "sky_id";
        public static final String KEY_ACCOUNT_CURRENT = "current";
        public static final String KEY_EXTERNAL_INFO = "external_info";
        public static final String KEY_EXTERNAL_FLAG = "external_flag";
        public static final String KEY_EXTERNAL_ID = "external_id";
        public static final String KEY_ACCOUNT_RES = "result";
        public static final String KEY_ACCOUNT_RES2 = "result2";
        public static final String KEY_ACCOUNT_CHECK_ADD_MOBILE = "check_add_mobile";

        /**
         * @Fields KEY_CONFIRM_TYPE 弹出确认框类型
         */
        public static final String KEY_CONFIRM_TYPE = "confirm_type";

        /**
         * @Fields KEY_LOGIN_ACCOUNT 登录——账号字段的KEY
         */
        public static final String KEY_ACCOUNT = "account";
        /**
         * @Fields KEY_LOGIN_PASSWORD 登录——密码字段的KEY
         */
        public static final String KEY_PASSWORD = "password";
        /**
         * @Fields KEY_CAPTCHA 验证码字段的KEY
         */
        public static final String KEY_CAPTCHA = "captcha";
        /**
         * @Fields KEY_NEW_BIND 是否新绑定流程字段的KEY
         */
        public static final String KEY_NEW_BIND = "isNew";
        /**
         * @Fields KEY_NEW_PASSWORD 新密码字段的KEY
         */
        public static final String KEY_NEW_PASSWORD = "newPassword";
        /**
         * @Fields KEY_OLD_PASSWORD 旧密码字段的KEY
         */
        public static final String KEY_OLD_PASSWORD = "oldPassword";
        /**
         * @Fields KEY_VERIFY_ACTION 验证手机号的动作的KEY
         */
        public static final String KEY_VERIFY_ACTION = "verifyAction";
    }

    /**
     * Description: User Service start over UserService启动完成
     */
    public static final String START_OVER = "com.tianci.user.start_over";
    /**
     * Description:用户切换
     */
    public static final String USER_CHANGED = "com.tianci.user.user_changed";

    // 用户相关：增加、删除 、更新、获取，及session，uid等信息
    public static final String GET_SESSION = "com.tianci.user.get_session";

    /*
     * 帐号相关
     */
    public static final String ACCOUNT_GET_CAPTCHA = "com.tianci.user.account_get_captcha";
    public static final String ACCOUNT_GET_INFO = "com.tianci.user.account_get_info";
    public static final String ACCOUNT_GET_INFO_STRING = "com.tianci.user.account_get_info_string";
    public static final String ACCOUNT_VERIFY_CAPTCHA = "com.tianci.user.account_verifyt_captcha";
    public static final String ACCOUNT_LOGIN = "com.tianci.user.account_login";
    public static final String ACCOUNT_LOGIN_BY_OPENID = "com.tianci.user.account_login_by_openid";
    public static final String ACCOUNT_LOGOUT = "com.tianci.user.account_logout";
    public static final String ACCOUNT_UPDATE = "com.tianci.user.account_update";
    public static final String ACCOUNT_GET_TOKEN = "com.tianci.user.account_get_token";
    public static final String ACCOUNT_HAS_LOGIN = "com.tianci.user.account_has_login";
    public static final String ACCOUNT_BIND_MOBILE = "com.tianci.user.account_bind_mobile";
    public static final String ACCOUNT_GET_INFO_LIST = "com.tianci.user.account_get_info_list";
    public static final String ACCOUNT_GET_LAST_DATA = "com.tianci.user.account_get_last_data";
    public static final String ACCOUNT_MANAGER = "com.tianci.user.account_manager";
    public static final String ACCOUNT_CHANGED = "com.tianci.user.account_changed";
    public static final String ACCOUNT_REMOVE = "com.tianci.user.account_remove";
    public static final String ACCOUNT_LOGIN_VIEW_STATUS =
            "com.tianci.user.account_login_view_status";
    public static final String ACCOUNT_LOADING = "com.tianci.user.account_loading";
    public static final String ACCOUNT_GET_MOBILE_BY_OPENID =
            "com.tianci.user.account_get_mobile_by_openid";
    public static final String ACCOUNT_VERIFY_MOBILE = "com.tianci.user.account_verify_mobile";
    public static final String ACCOUNT_TOAST = "com.tianci.user.account_toast";
    public static final String ACCOUNT_LOGIN_BY_CAPTCHA =
            "com.tianci.user.account_login_by_captcha";
    /**
     * 微信的accessToken更新
     */
    public static final String ACCOUNT_WX_TOKEN_REFRESH =
            "com.tianci.user.account_wx_token_refresh";
    //    public static final String ACCOUNT_UPDATE_MOBILE = "com.tianci.user.account_update_mobile";

    public static final String ACCOUNT_IS_PUBLIC_ADDRESS =
            "com.tianci.user.account_is_public_address";
    public static final String ACCOUNT_REFRESH_INFO = "com.tianci.user.account_refresh_info";
}

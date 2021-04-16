package com.coocaa.publib.common;

import android.os.Environment;


/**
 * Filename : Constants.java
 *
 * @Description : 常量
 * @Author : 高明峰
 * @Version ：1.0
 * @Date ：2019-9-16 下午10:00:24
 */
public class Constants {

    // 第三方应用的appkey appid
    public static final String APPID_QQ = "1106657226";
    public static final String APPKEY_QQ = "wrYbyyRSBBlEq4nv";

    /*public static final String APPKEY_UM = "5a4c9feef43e48041700000e";
    public static final String MESSAGE_SECRET_UM = "bed4c3acd0225b9a9f41d1c71aab6cf6";
    public static final String APP_MASTER_SECRET_UM = "moggjsubggbu4eawwgsq7mldyn9mlthb";*/

    public static final String APPKEY_UM = "5a7a61a8f43e48366f0001d7";
    public static final String MESSAGE_SECRET_UM = "3e8e412eb91eacb5968abbf0deb83cde";
    public static final String APP_MASTER_SECRET_UM = "xaim1nqbc9gstgkmydpkwgcqxevxzfvf";
    public static final String APP_TEST ="qq";

    public static final String APPID_WECHAT = "wxc0255c6c1cffe0ba";
    public static final String APP_SECRET_WECHAT = "19a92031e83a8633c52ef70e6badf3b1";

    //ceshi svn
    public static final String TAG = "Constants";
    /**
     * 成功编码
     */
    public static final String SUCCESS_CODE = "0";

    public static final int SUCCES_CODE = 0;

    /**
     * 电视派成功编码1
     */
    public static final String CLIENTCENTER_SUCCESS_CODE = "1";

    /**
     * 返回XML的error code key
     */
    public static final String XML_ERROR_CODE_KEY = "Code";

    /**
     * 返回XML的error message key
     */
    public static final String XML_ERROR_MESSAGE_KEY = "Descr";

    /**
     * 启动页保存图片保存名称
     */
    public static final String BGIMAGE_IMAGE = "startPage.image";

    /**
     * 启动页保存图片的路径
     */
    public final static String ALBUM_PATH = Environment.getExternalStorageDirectory() + "/fingertips.image/";

    /**
     * 自动更新提示的日期时间
     */
    public static final String SOFT_UPDATE_DATE = "soft_update_date";

    /**
     * 接口签名串尾部加入固定串 ----获取用户的user_id
     */
    public static final String SIGN_ENCRYPTION_USER = "passport.app.doubimeizhi.com";



    /**
     * 微信支付配置的aes加密的key
     */
    public static final String WECHATCONFIG_SIGN_KEY = "AVl4U1mZP3BIOQkM";

    /**
     * 优惠券的appkey
     */
    public static String COUPONS_APPKEY = "dc83abe9f06d406a8b1c7c3c1b7cd172";

    /**
     * 接口签名串尾部加入固定串 ------ 酷开登录
     */
    public static final String SIGN_COOCAA_SECRET = "o1ERnx2KfSHQmpzy";
    // 测试环境的用12O9xa5Ki3i4I2a1
//    public static final String SIGN_COOCAA_SECRET = "12O9xa5Ki3i4I2a1";

    /**
     * 酷开登录CLIENT_ID
     */
    //新电视派，谢光财给我们分配的 at 2018-04-28
    public static final String COOCAA_CLIENT_ID = "7050748df941410d8ed46172fb72eefe";
    // 测试环境的用6fa6cf5a83c049afab664d59f5b147e2
//    public static final String COOCAA_CLIENT_ID =  "6fa6cf5a83c049afab664d59f5b147e2";

    public static final String VIDEO_CALL_CLIENT_ID = "56e0a352e69644d787adf859b7ec73af";

    public static final String VIDEO_CALL_SIGN_SECRET = "X123oGzsAPuRfjo8";

    /**
     * 第三方账号登录方式， qq QQ
     */
    public static final String EXT_FROM_QQ = "qq";

    /**
     * 第三方账号登录方式， wechat微信
     */
    public static final String EXT_FROM_WECHAT = "wechat";

    /**
     * 第三方账号登录方式， weibo 微博
     */
    public static final String EXT_FROM_WEIBO = "weibo";

    /**
     * 第三方账号登录方式， coocaa 酷开
     */
    public static final String EXT_FROM_COOCAA = "coocaa";

    /**
     * 无效的 access_token，未登陆
     */
    public static final String CODE_INVALID_TOKEN = "403001";
    /**
     * URL 时间戳差距过大，拒绝服务 ，手机的系统时间不对
     */
    public static final String CODE_INVALID_SYSTIME = "403004";

    /**
     * 登录的时候返回 用户不存在 205003
     */
    public static final String CODE_USER_NOTEXISTS = "205003";

    /**
     * 弹出选择框的，1为未登录
     */
    public static final int MYDIALOG_TYPE_LOGIN = 1;

    public static final String DB_NAME = "readkey.db";
    public static final int DB_VERSION = 3;

    /**
     * 淘宝应用的包名
     */
    public static final String TAOBAO_PACKAGENAME = "com.taobao.taobao";

    /**
     * 微信应用的包名
     */
    public static final String WEIXIN_PACKAGENAME = "com.tencent.mm";

    /**
     * 是否安装应用(taobao,tmall,jd)，0未安装
     */
    public static final int NOTINSTALL = 0;    // 未安装

    /**
     * 是否安装应用(taobao,tmall,jd)，1已安装
     */
    public static final int INSTALLED = 1;     // 已安装


    /**
     * 第三方登录标识 -1临时账号
     */
    public static final int THIRD_ACCOUNT_TEMPORARY = -1;

    /**
     * 第三方登录标识 0 手机登录
     */
    public static final int THIRD_ACCOUNT_LOGIN = 0;

    /**
     * 第三方登录标识 1 coocaa
     */
    public static final int THIRD_ACCOUNT_COOCAA = 1;

    /**
     * 第三方登录标识 2 qq
     */
    public static final int THIRD_ACCOUNT_QQ = 2;

    /**
     * 第三方登录标识 3 wechat
     */
    public static final int THIRD_ACCOUNT_WECHAT = 3;

    /**
     * 第三方登录标识 4 微博
     */
    public static final int THIRD_ACCOUNT_WEIBO = 4;

    /**
     * 第三方登录标识 5短信登录
     */
    public static final int THIRD_ACCOUNT_SMS = 5;

    /**
     * 平台名称 QQ
     */
    public static final String PLATFORM_QQ = "QQ";

    /**
     * 平台名称 微信 wechat
     */
    public static final String PLATFORM_Wechat = "Wechat";

    /**
     * 平台名称   微博SinaWeibo
     */
    public static final String PLATFORM_SinaWeibo = "SinaWeibo";

    /**
     * 用户的信息
     */
    public static final String USERINFO = "userInfo";

    /**
     * access_token
     */
    public static final String ACCESS_TOKEN = "access_token";
    public static final String OPEN_ID = "open_id";
    public static final String NICK_NAME = "nick_name";
    public static final String AVATAR = "avatar";

    /**
     * 登录对象保存的dat
     */
    public static final String USERINFO_CENTER_DAT = "UserInfoCenter.dat";

    /**
     * 手机验证码的bid 1注册验证码
     */
    public static final int CAPTCHA_BID_REGISTER = 1;

    /**
     * 手机验证码的bid 2登录验证码
     */
    public static final int CAPTCHA_BID_LOGIN = 2;

    /**
     * 手机验证码的bid 3忘记密码验证码
     */
    public static final int CAPTCHA_BID_FORGETPWD = 3;

    /**
     * 手机验证码的bid 5手机绑定验证码
     */
    public static final int CAPTCHA_BID_MODBILEBIND = 5;

    /**
     * 酷开账户登录的版本号
     */
    public static final int COOCA_LOGIN_CODE = 41;


    /**需要调整到浏览器的路由地址*/  /* coocaa://app.tvpi.com/webout */
//    public static final String OPENBROWESR_ROUTERS ="zhjt://app.zjdj.com/webout";
    public static final String OPENBROWESR_ROUTERS ="coocaa://app.tvpi.com/webout";

    public static final String OPENBROWESR_ROUTERS_WEBIN ="coocaa://app.tvpi.com/webin";

    /*以上旧的Appkey已弃，换成全局的新的appkey，一个客户端使用同一个appkey*/
    public static final String ANDROID_APPKEY = "aa6927eb31c20f85fa4b6cd6ae4cd489";
    public static final String ANDROID_SIGN = "e1b0b77eb5463aa0219ceca9d33f9bf7";

    // 内部网页打开参数
    public static class Cordova {
        /**
         * 访问的url
         */
        public static final String url = "url";
        /**
         * 访问的url资料id
         */
        public static final String urlResId = "urlResId";
        /**
         * 标题显示
         */
        public static final String title = "title";
        /**
         * 标题显示的资源id
         */
        public static final String titleResId = "titleResId";
    }

    // 视频类型 0：短视频， 1：长视频
    public static final String VIDEO_TYPE_SHORT = "0";
    public static final String VIDEO_TYPE_LONG = "1";
}

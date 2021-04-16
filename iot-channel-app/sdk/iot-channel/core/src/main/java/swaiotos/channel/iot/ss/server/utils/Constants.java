package swaiotos.channel.iot.ss.server.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import swaiotos.sal.SAL;
import swaiotos.sal.SalModule;
import swaiotos.sal.system.ISystem;

public class Constants {

    private static final String HDD500 = "HDD500";
    private static final String HDD600 = "HDD600";
    private static final String FARADAY = "faraday";
    private static final String CC2001 = "2A08_CC2001";


    public static boolean isDangle() {
        //return false;   //test 关闭dongle
        return Build.MODEL.equals(Constants.FARADAY)
                || Build.MODEL.equals(Constants.HDD500)
                || Build.MODEL.equals(CC2001)
                || Build.MODEL.equals(Constants.HDD600);
    }


    /**
     * http connection time out
     */
    public static final long CONNECT_TIME_OUT = 20;

    /**
     * http socket time out
     */
    public static final long SOCKET_TIME_OUT = 20;

    /**
     * MAC String N mac地址 bcec234e832e
     * cChip String N 机芯
     * cUDID String N 服务端分配给设备的唯一标识（激活ID）
     * cModel String N 机型
     * cSize String N 尺寸
     * cBrand String  品牌标识
     * cLicense String  牌照商
     */
    public static final String COOCAA_MAC = "MAC";
    public static final String COOCAA_CCHIP = "cChip";
    public static final String COOCAA_CUDID = "cUDID";
    public static final String COOCAA_CMODEL = "cModel";
    public static final String COOCAA_CSIZE = "cSize";
    public static final String COOCAA_CBRAND = "cBrand";
    public static final String COOCAA_CLICENSE = "cLicense";
    public static final String COOCAA_DEVICENAME = "deviceName";
    public static final String COOCAA_IMEI = "imei";
    public static final String COOCAA_CVERSION = "cVersion";

    /**
     * API PATH portion
     */
    public static final String COOCAA_USERINFO = "screen/userinfo";//ozh-通过token获取用户信息
    public static final String COOCAA_QRCODE = "screen/getQrCode";//ozh-获取授权绑定二维码信息接口
    public static final String COOCAA_BIND_DEVICE = "screen/add_relations";//ozh-设备端确认绑定关系接口
    public static final String COOCAA_VALID_CODE = "screen/valid-code"; //ozh-获取验证码接口
    public static final String COOCAA_REGISTER_LOGIN = "screen/register-login";//ozh-设备注册接口
    public static final String COOCAA_SUBMIT = "screen/submit";//ozh-手机端确认提交接口
    public static final String COOCAA_QUERY_CODE = "screen/queryQrCode";//ozh-授权绑定二维码轮询接口

    //  API密钥
    public static final String COOCAA_APIKEY = "coocaa2020";
    /***********************请求key名***********************************/
    public static final String COOCAA_TIME = "time";
    public static final String COOCAA_DEVICE_INFO = "deviceInfo";
    /**
     * 请求参数sign加密
     */
    public static final String COOCAA_SIGN = "sign";

    public static final String COOCAA_ACCESSTOKEN = "accessToken";
    public static final String COOCAA_TEMPBIND= "tempBind";
    public static final String COOCAA_DANGLE = "1";

    // 验证码key名
    public static final String COOCAA_VALIDE_CODE_DEVICEID = "deviceId";

    //register-login key名
    public static final String COOCAA_REGISTER_LOGIN_TYPE = "zpRegisterType";
    public static final String COOCAA_REGISTER_LOGIN_NAME = "zpNickName";
    public static final String COOCAA_REGISTER_LOGIN_CODE = "code";

    /***********************************************************/
    public static final String COOCAA_SUCCESS = "0";
    public static final int COOCAA_ONLIEN = 1;
    public static final int COOCAA_OFFLINE = 0;


    /*************************本地发送数据key名********************************/
    public static final String COOCAA_BINDCODE = "bindCode";
    public static final String COOCAA_EXPIRESIN = "expiresIn";
    public static final String COOCAA_QRCODE_ACTION = "swaiotos.channel.iot.tv.qrcode";

    /************************设备类型******************************************/
    public static final String COOCAA_TV = "tv";
    public static final String COOCAA_PAD = "pad";
    public static final String COOCAA_OPENID = "openid";
    public static final String COOCAA_NJ = "nj";

    /***************************sharePref配置********************************/
    public static final String COOCAA_PREF_ACCESSTOKEN = "pref_accessToken";
    public static final String COOCAA_PREF_EXPIRES_IN = "pref_expires_in";
    public static final String COOCAA_PREF_CURRENTTIME = "pref_currentTimeMillis";
    public static final String COOCAA_PREF_LSID = "pref_lsid";
    public static final String COOCAA_PREF_DEVICEs_LIST = "pref_devices_list";
    public static final String COOCAA_PREF_TEMP_DEVICEs_LIST = "pref_temp_devices_list";
    public static final String COOCAA_PREF_SCREEN_APPS = "pref_screen_apps_list";

    /***************************PUSH消息******************************************/
    public static final String COOCAA_PUSH_ACTION = "swaiotos.channel.iot.tv.push";
    public static final String COOCAA_PUSH_MSG = "pushKey";

    /*************************bind device****************************************/
    public static final String COOCAA_BIND_DEVICE_PUSH_TOKEN = "pushToken";
    public static final String COOCAA_BIND_DEVICE_PUSH_LSID = "LSID";

    /****************授权绑定二维码绑定状态（1：未绑定  2：已绑定）*****************/
    public static final String COOCAA_POLL_SUCCESS = "2";
    public static final String COOCAA_POLL_FAIL = "1";
    public static final String COOCAA_TYPE_1000 = "1000";//LOGIN_ERROR(1000, "Token失效,请重新登录")
    public static final String COOCAA_TYPE_1003 = "1003";//NO_SUCH_ENTITY(1003, "没有找到对象")
    public static final String COOCAA_TYPE_20124 = "20124";//REFRESH_TOKEN_EXPIRED(, "refresh_token 已经失效")
    public static final String COOCAA_TYPE_20005 = "20005";//sid不存在

    public static final String COOCAA_TYPE_20003 = "20003";//ACCOUNT_BINDINGCODE_EXPIRED(20003, "绑定码已失效"),
    public static final String COOCAA_TYPE_10 = "-10";
    public static final String COOCAA_TYPE_11 = "-11";
    public static final String COOCAA_TYPE_12 = "-12";
    public static final String COOCAA_TYPE_13 = "-13";
    public static final String COOCAA_TYPE_14 = "-14";

    public static final String COOCAA_PUSH_MAP = "map";
    public static final String COOCAA_BROADCAST_RECEIVER_TRANSMITTER = "swaiotos.channel.iot.intent.transmitters"; //发送数据action
    public static final String COOCAA_BROADCAST_RECEIVER_RECEIVER = "swaiotos.channel.iot.intent.receiverers";//接收数据action
    public static final String COOCAA_BROADCAST_RECEIVER_COMMAND_1_KEY = "COOCAA_BROADCAST_RECEIVER_COMMAND_1_KEY";//发送端开始获取数据命令
    public static final String COOCAA_BROADCAST_RECEIVER_COMMAND_3_KEY = "COOCAA_BROADCAST_RECEIVER_COMMAND_3_KEY";//发送端停止获取数据命令
    public static final String COOCAA_BROADCAST_RECEIVER_COMMAND_2_KEY = "COOCAA_BROADCAST_RECEIVER_COMMAND_2_KEY";//接收端获取数据key值
    public static final int COOCAA_BROADCAST_RECEIVER_COMMAND_1_VALUE = 1;//发送端指令：1(开始发送数据)
    public static final int COOCAA_BROADCAST_RECEIVER_COMMAND_3_VALUE = 3;//发送端指令：3(停止发送数据)
    public static final int COOCAA_BROADCAST_RECEIVER_COMMAND_2_VALUE = 2;//接收端指令：2
    public static final String COOCAA_BROADCAST_RECEIVER_LIST_DEVICES = "COOCAA_BROADCAST_RECEIVER_LIST_DEVICES";

    public static final String BROADCAST_PERMISSION_DISC = "swaiotos.channel.iot.permissions.MY_BROADCAST";


    //    public static final String SERVER_COOCAA_AIOT_TEST = "http://172.20.151.162:8087/api/";
    private static String IOT_CHANEL;
    private static String IOT_SERVER;
    private static String IOT_APPKEY;

    private static String IOT_SERVER_LOG_URL;
    private static String IOT_APPKEY_LOG;
    private static String IOT_SERVER_APPSTORE_URL;
    public static String LOG_SECRET = "50c08407916141aa878e65564321af5f";

    /******************空间概率基本信息************************/
    public static String COOCAA_IOT_SPACE_ID = "spaceId";

    /**
     *
     * @param  type 1:sse 2:local 0: 网络未联网
     * @param  state 1:断开  0：连接
     */
    public static int COOCAA_IOT_CHANNEL_TYPE_SSE = 1;
    public static int COOCAA_IOT_CHANNEL_TYPE_LOCAL = 2;
    public static int COOCAA_IOT_CHANNEL_TYPE_NOT_NET = 0;

    public static int COOCAA_IOT_CHANNEL_STATE_DISCONNECT = 1;
    public static int COOCAA_IOT_CHANNEL_STATE_CONNECT = 0;

    public static int COOCAA_IOT_CHANNEL_TYPE_CONNECTING = 1;
    public static int COOCAA_IOT_CHANNEL_TYPE_CONNECTED = 0;

    public final static String COOCAA_LAST_CONNECT_SESSION = "last_session";

    /**
     * 获取cUDID信息
     */
    private static String activeID = "";

    /******************************************/

    public static String getIOTChannel(Context mContext) {
        if (TextUtils.isEmpty(IOT_CHANEL))
            IOT_CHANEL = (String) getMetaData(mContext, mContext.getPackageName(), "IOT_CHANEL");
        if (TextUtils.isEmpty(IOT_CHANEL)) {
            IOT_CHANEL = "TV";
        }
        return IOT_CHANEL;
    }

    public static String getIOTServer(Context mContext) {
        if (TextUtils.isEmpty(IOT_SERVER))
            IOT_SERVER = (String) getMetaData(mContext, mContext.getPackageName(), "IOT_SERVER");

        if (TextUtils.isEmpty(IOT_SERVER)) {
            IOT_SERVER = "https://passport.coocaa.com/";
        }
        return IOT_SERVER;
    }

    public static String getAppKey(Context mContext) {
        if (StringUtils.isEmpty(IOT_APPKEY))
            IOT_APPKEY = (String) getMetaData(mContext, mContext.getPackageName(), "IOT_APPKEY");
        if (TextUtils.isEmpty(IOT_APPKEY)) {
            IOT_APPKEY = "KSiVM12wRNu1WNN5";
        }
        Log.d("appkey", "IOT_APPKEY2:" + IOT_APPKEY);
        return IOT_APPKEY;
    }

    public static String getIOTLOGServer(Context mContext) {
        if (TextUtils.isEmpty(IOT_SERVER_LOG_URL))
            IOT_SERVER_LOG_URL = (String) getMetaData(mContext, mContext.getPackageName(), "IOT_SERVER_LOG_URL");

        if (TextUtils.isEmpty(IOT_SERVER_LOG_URL)) {
            IOT_SERVER_LOG_URL = "https://api.skyworthiot.com/";
        }
        return IOT_SERVER_LOG_URL;
    }

    public static String getIotAppStoreServer(Context mContext) {
        if (TextUtils.isEmpty(IOT_SERVER_APPSTORE_URL))
            IOT_SERVER_APPSTORE_URL = (String) getMetaData(mContext, mContext.getPackageName(), "IOT_SERVER_APP_STORE_URL");

        if (TextUtils.isEmpty(IOT_SERVER_APPSTORE_URL)) {
            IOT_SERVER_APPSTORE_URL = "http://tc.skysrt.com/";
        }
        return IOT_SERVER_APPSTORE_URL;
    }

    public static String getLogAppKey(Context mContext) {
        if (StringUtils.isEmpty(IOT_APPKEY_LOG))
            IOT_APPKEY_LOG = (String) getMetaData(mContext, mContext.getPackageName(), "IOT_APPKEY_LOG");
        if (TextUtils.isEmpty(IOT_APPKEY_LOG)) {
            IOT_APPKEY_LOG = "81dbba5e74da4fcd8e42fe70f68295a6";
        }
        Log.d("logAppKey", "IOT_APPKEY2:" + IOT_APPKEY_LOG);
        return IOT_APPKEY_LOG;
    }

    private static Object getMetaData(Context context, String packageName, String key) {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_META_DATA);
            if (applicationInfo != null) {
                Object value = null;
                if (applicationInfo.metaData != null) {
                    value = applicationInfo.metaData.get(key);
                }
                if (value == null) {
                    return null;
                }
                return value;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getVersionCode(Context context) {
        if (context == null) return -1;
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        try {
            if (pm == null) return -1;
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            if (pi == null) return -1;
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }

    public static String getActiveId(Context context) {
        if (!TextUtils.isEmpty(activeID))
            return activeID;
        String channel = Constants.getIOTChannel(context);
        if (channel.equals("TV")) {
            ISystem iSystem = SAL.getModule(context, SalModule.SYSTEM);
            activeID = iSystem.getActiveId();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                activeID = Settings.Global.getString(context.getContentResolver(), "swaiot_activation_id_key");
            }
        }
        return activeID;
    }

}

package swaiotos.channel.iot.common.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import swaiotos.channel.iot.utils.AndroidLog;

public class Constants {

    private static final String CONFIGE_DANGLE = "dangle";

    /** http connection time out */
    public static final long CONNECT_TIME_OUT = 20;

    /** http socket time out */
    public static final long SOCKET_TIME_OUT = 20;

    /**
     * MAC String N mac地址 bcec234e832e
     * cChip String N 机芯
     * cUDID String N 服务端分配给设备的唯一标识（激活ID）
     * cModel String N 机型
     * cSize String N 尺寸
     * cBrand String  品牌标识
     * cLicense String  牌照商
     *
     * */
    public static final String COOCAA_MAC = "MAC";
    public static final String COOCAA_CCHIP = "cChip";
    public static final String COOCAA_CUDID = "cUDID";
    public static final String COOCAA_CMODEL = "cModel";
    public static final String COOCAA_CSIZE = "cSize";
    public static final String COOCAA_CBRAND = "cBrand";
    public static final String COOCAA_CLICENSE = "cLicense";
    public static final String COOCAA_DEVICENAME = "deviceName";
    public static final String COOCAA_IMEI = "imei";

    /**  API PATH portion*/
    public static final String COOCAA_USERINFO = "api/screen/userinfo";//ozh-通过token获取用户信息
    public static final String COOCAA_QRCODE = "api/screen/getQrCode";//ozh-获取授权绑定二维码信息接口
    public static final String COOCAA_BIND_DEVICE = "api/screen/add_relations";//ozh-设备端确认绑定关系接口
    public static final String COOCAA_VALID_CODE = "api/screen/valid-code"; //ozh-获取验证码接口
    public static final String COOCAA_REGISTER_LOGIN = "api/screen/register-login";//ozh-设备注册接口
    public static final String COOCAA_SUBMIT = "api/screen/submit";//ozh-手机端确认提交接口
    public static final String COOCAA_QUERY_CODE = "api/screen/queryQrCode";//ozh-授权绑定二维码轮询接口
    public static final String COOCAA_UPDATE_DEVICEINFO = "api/screen/update-deviceInfo"; //ozh-根据token修改设备硬件信息

    //  API密钥
//    public static final  String COOCAA_APIKEY="coocaa2020";
    /***********************请求key名***********************************/
    public static final String COOCAA_TIME = "time";
    public static final String COOCAA_DEVICE_INFO = "deviceInfo";
    public static final String COOCAA_CREATE_ROOM = "createRoom";
    /**
     * 请求参数sign加密
     * */
    public static final String COOCAA_SIGN = "sign";

    public static final String COOCAA_ACCESSTOKEN= "accessToken";
    public static final String COOCAA_ACC_CREATEROOM = "createRoom"; //1:dangle类型
    public static final String COOCAA_TEMPBIND= "tempBind";
    public static final String COOCAA_DANGLE = "1";
    public static final String CC_REGISTER_TYPE = "registerType";
    public static final String CC_DONGLE = "dongle";

    // 验证码key名
    public static final String COOCAA_VALIDE_CODE_DEVICEID = "deviceId";

    //register-login key名
    public static final String COOCAA_REGISTER_LOGIN_TYPE = "zpRegisterType";
    public static final String COOCAA_REGISTER_LOGIN_NAME = "zpNickName";
    public static final String COOCAA_REGISTER_LOGIN_CODE= "code";

    /***********************************************************/
    public static final String COOCAA_SUCCESS = "0";

    /*************************本地发送数据key名********************************/
    public static final String COOCAA_BINDCODE = "bindCode";
    public static final String COOCAA_OLD_BINDCODE = "oldBindCode";
    public static final String COOCAA_EXPIRESIN = "expiresIn";
    public static final String COOCAA_QRCODE_ACTION = "swaiotos.channel.iot.tv.qrcode";
    public static final String COOCAA_LSID_ACTION = "swaiotos.intent.action.channel.iot.service.LSID";

    /************************设备类型******************************************/
    public static final String COOCAA_TV = "tv";
    public static final String COOCAA_PAD = "pad";
    public static final String COOCAA_OPENID = "openid";
    public static final String COOCAA_NJ = "nj";

    /***************************sharePref配置********************************/
    public static final String COOCAA_PREF_ACCESSTOKEN = "pref_accessToken";
    public static final String COOCAA_PREF_EXPIRES_IN = "pref_expires_in";
    public static final String COOCAA_PREF_CURRENTTIME = "pref_currentTimeMillis";
    public static final String COOCAA_PREF_LSID= "pref_lsid";
    public static final String COOCAA_PREF_DEVICEs_LIST= "pref_devices_list";
    public static final String COOCAA_PREF_DEVICEINFO = "pref_deviceInfo";
    public static final String COOCAA_FILE_ACCESSTOKEN_NAME = "file_accessToken";
    public static final String COOCAA_PREF_TEMP_BINDCODE = "pref_temp_bindCoce";

    /***************************PUSH消息******************************************/
    public static final String COOCAA_PUSH_ACTION = "swaiotos.channel.iot.tv.push";
    public static final String COOCAA_PUSH_MSG = "pushKey";

    /*************************bind device****************************************/
    public static final String COOCAA_BIND_DEVICE_PUSH_TOKEN = "pushToken";
    public static final String COOCAA_BIND_DEVICE_PUSH_LSID= "LSID";
    public static final String COOCAA_UNBIND_POSITION = "position";

    /****************授权绑定二维码绑定状态（1：未绑定  2：已绑定）*****************/
    public static final String COOCAA_POLL_SUCCESS = "2";
    public static final String COOCAA_POLL_FAIL = "1";
    public static final String COOCAA_TYPE_1000 = "1000";//LOGIN_ERROR(1000, "Token失效,请重新登录")
    public static final String COOCAA_TYPE_1003 = "1003";//NO_SUCH_ENTITY(1003, "没有找到对象")
    public static final String COOCAA_TYPE_20124 = "20124";//REFRESH_TOKEN_EXPIRED(, "refresh_token 已经失效")

    public static final String COOCAA_TYPE_20003 = "20003";//ACCOUNT_BINDINGCODE_EXPIRED(20003, "绑定码已失效"),

    public static final String COOCAA_PUSH_MAP = "map";

    /**********************功能：获取ProvisionService的服务******************/
    // 1:获取accessToken的值 99:拉起iot-channel core服务
    public static final String COOCAA_PROVISION_SERVICE_TYPE = "provision_service_type";
    public static final String SYSTEM_START_MESSENGER = "system_intent_messenger";
    public static final String SYSTEM_INTENT_BUNDLE = "system_intent_bundle";
    public static final String COOCAA_REFLUSH_INSTALL = "install_reflush";

    //cudid的action
    public static final String COOCAA_CUDID_ACTION = "swaiot.intent.action.RECEIVE_ACTIVATION_ID";

    //启动模式  跨屏互动启动 or 小维启动
    public static final String COOCAA_START_TYPE = "starttype";
    public static final String COOCAA_START_SWAIOTOS = "1";
    public static final String COOCAA_START_SMALL = "2";

    //新增获取短码url：https://s.skysrt.com/v1/code/short
    public static String IOT_COOCAA_TEMP_BASE_URL = "https://s.skysrt.com/";
    public static String IOT_COOCAA_TEMP_PATH= "v1/code/short";

//    public static final String SERVER_COOCAA_AIOT_TEST = "http://172.20.151.162:8087/api/";
    private static String IOT_CHANEL;
    private static String IOT_SERVER;
    private static String IOT_APPKEY;
    public static String getIOTChannel(Context mContext){
        if (StringUtils.isEmpty(IOT_CHANEL))
            IOT_CHANEL = (String) getMetaData(mContext, mContext.getPackageName(), "IOT_CHANEL");
        return IOT_CHANEL;

    }

    public static String getIOTServer(Context mContext){
        if (StringUtils.isEmpty(IOT_SERVER))
            IOT_SERVER = (String) getMetaData(mContext, mContext.getPackageName(), "IOT_SERVER");
        return IOT_SERVER;
    }

    public static String getAppKey(Context mContext){
        if (swaiotos.channel.iot.ss.server.utils.StringUtils.isEmpty(IOT_APPKEY))
            IOT_APPKEY = (String) getMetaData(mContext, mContext.getPackageName(), "IOT_APPKEY");
        Log.d("appkey","IOT_APPKEY:"+IOT_APPKEY);
        return IOT_APPKEY;
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

    public static boolean isNetworkOnline() {
        AndroidLog.androidLog("---NetworkOnline---start:"+System.currentTimeMillis());
        Runtime runtime = Runtime.getRuntime();
        Process ipProcess = null;
        try {
            ipProcess = runtime.exec("ping -c 1 -w 1 www.baidu.com");
            InputStream input = ipProcess.getInputStream();

            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }

            int exitValue = ipProcess.waitFor();
            if (exitValue == 0) {
                AndroidLog.androidLog("---NetworkOnline---end--success0:"+System.currentTimeMillis());
                //WiFi连接，网络正常
                return true;
            } else {

                if (stringBuffer.indexOf("100% packet loss") != -1) {
                    AndroidLog.androidLog("---NetworkOnline---end:"+System.currentTimeMillis());
                    //网络丢包严重，判断为网络未连接
                    return false;
                } else {
                    AndroidLog.androidLog("---NetworkOnline---end---sucess1:"+System.currentTimeMillis());
                    //网络未丢包，判断为网络连接
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ipProcess != null) {
                ipProcess.destroy();
            }
            runtime.gc();
        }
        AndroidLog.androidLog("---NetworkOnline---end:"+System.currentTimeMillis());
        return false;
    }

    public static boolean outerNetState() {
        URL infoUrl = null;
        HttpURLConnection httpConnection = null;
        try {
            infoUrl = new URL("https://www.baidu.com/");
            URLConnection connection = infoUrl.openConnection();
            httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    httpConnection.disconnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                AndroidLog.androidLog("-------baidu-----------");
                return true;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
